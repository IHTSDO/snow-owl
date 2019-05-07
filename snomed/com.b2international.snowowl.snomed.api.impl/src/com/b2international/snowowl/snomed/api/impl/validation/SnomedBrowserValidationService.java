package com.b2international.snowowl.snomed.api.impl.validation;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;

import org.ihtsdo.drools.RuleExecutor;
import org.ihtsdo.drools.RuleExecutorFactory;
import org.ihtsdo.drools.exception.BadRequestRuleExecutorException;
import org.ihtsdo.drools.response.InvalidContent;
import org.ihtsdo.drools.service.TestResourceProvider;
import org.ihtsdo.otf.resourcemanager.ManualResourceConfiguration;
import org.ihtsdo.otf.resourcemanager.ResourceConfiguration;
import org.ihtsdo.otf.resourcemanager.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.b2international.commons.http.ExtendedLocale;
import com.b2international.snowowl.core.SnowOwlApplication;
import com.b2international.snowowl.core.api.SnowowlRuntimeException;
import com.b2international.snowowl.core.branch.Branch;
import com.b2international.snowowl.core.exceptions.BadRequestException;
import com.b2international.snowowl.datastore.request.RepositoryRequests;
import com.b2international.snowowl.eventbus.IEventBus;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserConcept;
import com.b2international.snowowl.snomed.api.impl.validation.domain.ValidationConcept;
import com.b2international.snowowl.snomed.api.impl.validation.service.ValidationConceptService;
import com.b2international.snowowl.snomed.api.impl.validation.service.ValidationDescriptionService;
import com.b2international.snowowl.snomed.api.impl.validation.service.ValidationRelationshipService;
import com.b2international.snowowl.snomed.api.validation.ISnomedBrowserValidationService;
import com.b2international.snowowl.snomed.api.validation.ISnomedInvalidContent;
import com.b2international.snowowl.snomed.core.domain.BranchMetadataResolver;
import com.b2international.snowowl.snomed.datastore.SnomedDatastoreActivator;
import com.b2international.snowowl.snomed.datastore.config.SnomedCoreConfiguration;
import com.b2international.snowowl.snomed.datastore.config.SnomedDroolsConfiguration;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;

public class SnomedBrowserValidationService implements ISnomedBrowserValidationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SnomedBrowserValidationService.class);
	
	private static final Splitter COMMA_SPLITTER = Splitter.on(',');
	private static final Joiner COMMA_JOINER = Joiner.on(", ");
	
	@Resource
	private IEventBus bus;
	
	private SnomedDroolsConfiguration droolsConfig;

	private RuleExecutor ruleExecutor;
	private TestResourceProvider testResourceProvider;
	
	public SnomedBrowserValidationService() {
		droolsConfig = SnowOwlApplication.INSTANCE.getConfiguration().getModuleConfig(SnomedCoreConfiguration.class).getDroolsConfig();
		createNewRuleExecutor();
	}

	@Override
	public List<ISnomedInvalidContent> validateConcepts(String branchPath, List<? extends ISnomedBrowserConcept> concepts, List<ExtendedLocale> locales) {
		
		if (concepts.isEmpty()) {
			return Collections.emptyList();
		}
		
		List<String> assertionGroups = getAssertionGroups(branchPath);

		LOGGER.info(getLogMessage(branchPath, assertionGroups, concepts));
		
		try {
			
			List<ValidationConcept> validationConcepts = concepts.stream().map(concept -> new ValidationConcept(concept)).collect(toList());
			
			List<InvalidContent> invalidContent = ruleExecutor.execute(newHashSet(assertionGroups),
					validationConcepts,
					new ValidationConceptService(branchPath),
					new ValidationDescriptionService(branchPath, testResourceProvider),
					new ValidationRelationshipService(branchPath),
					false, false);
			
			return invalidContent.stream().map(content -> new SnomedInvalidContent(content)).collect(toList());
			
		} catch (BadRequestRuleExecutorException e) {
			throw new BadRequestException(e.getMessage());
		}
	}

	@Override
	public int reloadRules() {
		createNewRuleExecutor();
		return ruleExecutor.getTotalRulesLoaded();
	}
	
	private List<String> getAssertionGroups(String branchPath) {
		
		final Branch branch = RepositoryRequests.branching().prepareGet(branchPath)
				.build(SnomedDatastoreActivator.REPOSITORY_UUID)
				.execute(bus)
				.getSync();
		
		final String assertionGroupNamesString = BranchMetadataResolver.getEffectiveBranchMetadataValue(branch, SnomedCoreConfiguration.BRANCH_ASSERTION_GROUP_NAMES_KEY);
		if (Strings.isNullOrEmpty(assertionGroupNamesString)) {
			throw new BadRequestException("No assertion groups configured for this branch.");
		}
		
		List<String> assertionGroups = COMMA_SPLITTER.splitToList(assertionGroupNamesString);
		
		return assertionGroups;
	}

	private void createNewRuleExecutor() {
		
		String droolsRulesDir;
		
		if (Files.exists(Paths.get(droolsConfig.getRulesDirectory()))) {
			droolsRulesDir = droolsConfig.getRulesDirectory();
		} else {
			droolsRulesDir = getDefaultDroolsDir().toString();
		}
		
		LOGGER.info("Initializing Drools Engine rule executor using path '{}'", droolsRulesDir);
		ruleExecutor = new RuleExecutorFactory().createRuleExecutor(droolsRulesDir);
		
		testResourceProvider = createTestResourceProvider();
	}

	private TestResourceProvider createTestResourceProvider() {
		
		// in case S3 config is not present fall back to the "old" setup
		if (Strings.isNullOrEmpty(droolsConfig.getAwsKey()) || Strings.isNullOrEmpty(droolsConfig.getAwsPrivateKey()) 
				|| Strings.isNullOrEmpty(droolsConfig.getResourcesBucket()) || Strings.isNullOrEmpty(droolsConfig.getResourcesPath())) {

			String resourcesPath;
			
			if (Files.exists(Paths.get(droolsConfig.getTermValidationResourcesPath()))) {
				resourcesPath = droolsConfig.getTermValidationResourcesPath();
				LOGGER.info("Initializing Drools Engine resources at default local path '{}'", resourcesPath);
			} else {
				resourcesPath = getDefaultDroolsDir().toString();
				LOGGER.info("Initializing Drools Engine resources at fall-back path '{}'", resourcesPath);
				createDefaultResourceFiles(resourcesPath);
			}
			
			resourcesPath = resourcesPath.startsWith("/") ? '/' + resourcesPath : resourcesPath; // XXX ResourceConfiguration.Local removes leading slash from path :'(
			
			ManualResourceConfiguration manualConfig = new ManualResourceConfiguration(true, false, new ResourceConfiguration.Local(resourcesPath), null);
			ResourceManager resourceManager = new ResourceManager(manualConfig, null);
			return ruleExecutor.newTestResourceProvider(resourceManager);
			
		}
		
		LOGGER.info("Initializing Drools Engine resources using S3 bucket '{}'", droolsConfig.getResourcesBucket());
		return ruleExecutor.newTestResourceProvider(droolsConfig.getAwsKey(), droolsConfig.getAwsPrivateKey(), droolsConfig.getResourcesBucket(), droolsConfig.getResourcesPath());
	}
	
	private void createDefaultResourceFiles(String resourcesPath) {
		try {
			createDefaultResourceFile(resourcesPath, "semantic-tags.txt");
			createDefaultResourceFile(resourcesPath, "cs_words.txt");
			createDefaultResourceFile(resourcesPath, "us-to-gb-terms-map.txt");
		} catch (IOException e) {
			throw new SnowowlRuntimeException(e);
		}
	}

	private void createDefaultResourceFile(String resourcesPath, String fileName) throws IOException {
		Path filePath = Paths.get(resourcesPath, fileName);
		if (!Files.exists(filePath)) {
			Path resultPath = Files.createFile(filePath);
			LOGGER.info("Created empty Drools Engine resource file: '{}'", resultPath);
		}
	}

	private Path getDefaultDroolsDir() {
		try {
			return Files.createDirectories(Paths.get(SnowOwlApplication.INSTANCE.getEnviroment().getDataDirectory().toPath().toString(), "snomed-drools-rules"));
		} catch (IOException e) {
			throw new SnowowlRuntimeException(e);
		}
	}
	
	private String getLogMessage(String branchPath, List<String> assertionGroups, List<? extends ISnomedBrowserConcept> concepts) {
		String message = "Validation has been requested on branch '%s' with assertion groups: '%s' for '%s%s'";
		String conceptIds = COMMA_JOINER.join(concepts.stream().map(ISnomedBrowserConcept::getId).limit(10).collect(toList()));
		String more = "";
		if (concepts.size() > 10) {
			more = String.format(" and %s more", concepts.size() - 10);
		}
		return String.format(message, branchPath, COMMA_JOINER.join(assertionGroups), conceptIds, more);
	}

}
