package com.b2international.snowowl.snomed.api.impl.validation;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.ihtsdo.drools.RuleExecutor;
import org.ihtsdo.drools.exception.BadRequestRuleExecutorException;
import org.ihtsdo.drools.response.InvalidContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.b2international.commons.http.ExtendedLocale;
import com.b2international.snowowl.core.SnowOwlApplication;
import com.b2international.snowowl.core.branch.Branch;
import com.b2international.snowowl.core.exceptions.BadRequestException;
import com.b2international.snowowl.datastore.request.RepositoryRequests;
import com.b2international.snowowl.eventbus.IEventBus;
import com.b2international.snowowl.snomed.SnomedConstants.Concepts;
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
import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class SnomedBrowserValidationService implements ISnomedBrowserValidationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SnomedBrowserValidationService.class);
	
	private static final Splitter COMMA_SPLITTER = Splitter.on(',');
	private static final Splitter WHITESPACE_SPLITTER = Splitter.on(CharMatcher.WHITESPACE);
	private static final Joiner COMMA_JOINER = Joiner.on(", ");
	
	private static final String US_TERMS_FILENAME = "usTerms.txt";
	private static final String GB_TERMS_FILENAME = "gbTerms.txt";
	private static final String CASE_SIGNIFICANT_WORDS_FILENAME = "cs_words.txt";
	
	@Resource
	private IEventBus bus;
	
	private RuleExecutor ruleExecutor;
	private SnomedDroolsConfiguration droolsConfig;
	private Set<String> caseSignificantWords;
	private Multimap<String, String> refsetToLanguageSpecificWordsMap;
	
	public SnomedBrowserValidationService() {
		droolsConfig = SnowOwlApplication.INSTANCE.getConfiguration().getModuleConfig(SnomedCoreConfiguration.class).getDroolsConfig();
		ruleExecutor = newRuleExecutor(false);
		String path = "/opt/termserver/resources/test-resources"; // TODO move to config
		caseSignificantWords = loadCaseSignificantWords(path);
		refsetToLanguageSpecificWordsMap = loadLanguageSpecificWords(path);
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
					new ValidationDescriptionService(branchPath, caseSignificantWords, refsetToLanguageSpecificWordsMap),
					new ValidationRelationshipService(branchPath),
					false, false);
			
			return invalidContent.stream().map(content -> new SnomedInvalidContent(content)).collect(toList());
			
		} catch (BadRequestRuleExecutorException e) {
			throw new BadRequestException(e.getMessage());
		}
	}

	@Override
	public int reloadRules() {
		ruleExecutor = newRuleExecutor(true);
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

	private RuleExecutor newRuleExecutor(boolean releadSemanticTags) {
		return new RuleExecutor(droolsConfig.getRulesDirectory(), droolsConfig.getAwsKey(), droolsConfig.getAwsPrivateKey(), droolsConfig.getResourcesBucket(), droolsConfig.getResourcesPath(), releadSemanticTags);
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
	
	private Set<String> loadCaseSignificantWords(String path) {
		
		Path wordsFilePath = Paths.get(path, CASE_SIGNIFICANT_WORDS_FILENAME);
		
		if (!Files.exists(wordsFilePath)) {
			LOGGER.info("Failed to retrieve case sensitive words file at {}", wordsFilePath);
			return emptySet();
		}
		
		Set<String> words = newHashSet();
		
		try {
			
			Files.lines(wordsFilePath)
				.skip(1) // skip header
				.forEach( line -> {
					Iterator<String> lineElements = WHITESPACE_SPLITTER.split(line).iterator();
					String word = lineElements.next();
					String type = lineElements.next();
					if (type.equals("1")) {
						words.add(word); 
					}
				});
			
		} catch (IOException e) {
			LOGGER.info("Failed to retrieve case sensitive words file at {}", wordsFilePath);
			return emptySet();
		}
		
		LOGGER.info("Loaded {} case sensitive words into cache from: {}", words.size(), wordsFilePath);
		
		return words;
	}

	private Multimap<String, String> loadLanguageSpecificWords(String path) {
		Multimap<String, String> refsetToWordsMap = HashMultimap.create();
		refsetToWordsMap.putAll(Concepts.REFSET_LANGUAGE_TYPE_UK, loadLanguageSpecificWordsFromFile(Paths.get(path, GB_TERMS_FILENAME)));
		refsetToWordsMap.putAll(Concepts.REFSET_LANGUAGE_TYPE_US, loadLanguageSpecificWordsFromFile(Paths.get(path, US_TERMS_FILENAME)));
		return refsetToWordsMap;
	}

	private Set<String> loadLanguageSpecificWordsFromFile(Path filePath) {
		
		if (!Files.exists(filePath)) {
			LOGGER.info("Failed to retrieve language-specific terms from {}", filePath);
			return emptySet();
		}
		
		try {
			
			Set<String> words = Files.lines(filePath)
				.skip(1) // skip header
				.map(String::toLowerCase)
				.collect(toSet());
			
			LOGGER.info("Loaded {} language-specific spellings into cache from: {}", words.size(), filePath);
			
			return words;
			
		} catch (IOException e) {
			LOGGER.info("Failed to retrieve language-specific terms from {}", filePath);
			return emptySet();
		}
		
	}

}
