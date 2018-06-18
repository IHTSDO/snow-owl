package com.b2international.snowowl.snomed.api.impl.validation;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.ihtsdo.drools.RuleExecutor;
import org.ihtsdo.drools.exception.BadRequestRuleExecutorException;
import org.ihtsdo.drools.response.InvalidContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.b2international.commons.http.ExtendedLocale;
import com.b2international.snowowl.core.branch.Branch;
import com.b2international.snowowl.core.exceptions.BadRequestException;
import com.b2international.snowowl.datastore.request.RepositoryRequests;
import com.b2international.snowowl.eventbus.IEventBus;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserConcept;
import com.b2international.snowowl.snomed.api.impl.DescriptionService;
import com.b2international.snowowl.snomed.api.impl.validation.domain.ValidationConcept;
import com.b2international.snowowl.snomed.api.impl.validation.service.ValidationConceptService;
import com.b2international.snowowl.snomed.api.impl.validation.service.ValidationDescriptionService;
import com.b2international.snowowl.snomed.api.impl.validation.service.ValidationRelationshipService;
import com.b2international.snowowl.snomed.api.validation.ISnomedBrowserValidationService;
import com.b2international.snowowl.snomed.api.validation.ISnomedInvalidContent;
import com.b2international.snowowl.snomed.core.domain.BranchMetadataResolver;
import com.b2international.snowowl.snomed.datastore.SnomedDatastoreActivator;
import com.b2international.snowowl.snomed.datastore.config.SnomedCoreConfiguration;
import com.google.common.base.Strings;

public class SnomedBrowserValidationService implements ISnomedBrowserValidationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SnomedBrowserValidationService.class);
	
	@Resource
	private IEventBus bus;
	private RuleExecutor ruleExecutor;
	
	public SnomedBrowserValidationService() {
		ruleExecutor = newRuleExecutor();
	}

	@Override
	public List<ISnomedInvalidContent> validateConcepts(String branchPath, List<? extends ISnomedBrowserConcept> concepts, List<ExtendedLocale> locales) {
		
		final Branch branch = RepositoryRequests.branching().prepareGet(branchPath)
				.build(SnomedDatastoreActivator.REPOSITORY_UUID)
				.execute(bus)
				.getSync();
		
		final String assertionGroupNamesString = BranchMetadataResolver.getEffectiveBranchMetadataValue(branch, SnomedCoreConfiguration.BRANCH_ASSERTION_GROUP_NAMES_KEY);
		if (Strings.isNullOrEmpty(assertionGroupNamesString)) {
			throw new BadRequestException("No assertion groups configured for this branch.");
		}
		
		Set<String> assertionGroups = newHashSet(assertionGroupNamesString.split("\\,"));
		LOGGER.info("Branch {}, and assertionGroupNames: {}", branchPath, assertionGroups);
		
		ValidationConceptService validationConceptService = new ValidationConceptService(branchPath, bus);
		ValidationDescriptionService validationDescriptionService = new ValidationDescriptionService(new DescriptionService(bus, branchPath), branchPath, bus);
		ValidationRelationshipService validationRelationshipService = new ValidationRelationshipService(branchPath, bus);
		
		try {
			
			List<ValidationConcept> validationConcepts = concepts.stream().map(concept -> new ValidationConcept(concept)).collect(toList());
			
			List<InvalidContent> invalidContent = ruleExecutor.execute(assertionGroups,
					validationConcepts,
					validationConceptService,
					validationDescriptionService,
					validationRelationshipService,
					false, false);
			
			return invalidContent.stream().map(content -> new SnomedInvalidContent(content)).collect(toList());
			
		} catch (BadRequestRuleExecutorException e) {
			throw new BadRequestException(e.getMessage());
		}
	}

	@Override
	public int reloadRules() {
		ruleExecutor = newRuleExecutor();
		return ruleExecutor.getTotalRulesLoaded();
	}

	private RuleExecutor newRuleExecutor() {
		// TODO: Move path to configuration
		return new RuleExecutor("/opt/termserver/snomed-drools-rules");
	}

}
