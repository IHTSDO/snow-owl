package com.b2international.snowowl.snomed.api.impl.validation.service;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.ihtsdo.drools.domain.Concept;
import org.ihtsdo.drools.domain.Description;
import org.ihtsdo.drools.domain.Relationship;
import org.ihtsdo.drools.helper.DescriptionHelper;
import org.ihtsdo.drools.service.TestResourceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.b2international.commons.http.ExtendedLocale;
import com.b2international.snowowl.core.ApplicationContext;
import com.b2international.snowowl.eventbus.IEventBus;
import com.b2international.snowowl.snomed.SnomedConstants.Concepts;
import com.b2international.snowowl.snomed.SnomedConstants.LanguageCodeReferenceSetIdentifierMapping;
import com.b2international.snowowl.snomed.api.impl.DescriptionService;
import com.b2international.snowowl.snomed.api.impl.validation.domain.ValidationSnomedDescription;
import com.b2international.snowowl.snomed.core.domain.SnomedConcept;
import com.b2international.snowowl.snomed.core.domain.SnomedConcepts;
import com.b2international.snowowl.snomed.core.domain.SnomedDescription;
import com.b2international.snowowl.snomed.datastore.SnomedDatastoreActivator;
import com.b2international.snowowl.snomed.datastore.index.entry.SnomedConceptDocument;
import com.b2international.snowowl.snomed.datastore.request.SnomedRequests;
import com.google.common.collect.Sets;

public class ValidationDescriptionService implements org.ihtsdo.drools.service.DescriptionService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ValidationDescriptionService.class);
	
	private DescriptionService descriptionService;
	private String branchPath;
	private IEventBus bus;

	private Set<String> topLevelConceptIds;

	private TestResourceProvider testResourceProvider;

	public ValidationDescriptionService(String branchPath, TestResourceProvider testResourceProvider) {
		this.branchPath = branchPath;
		this.bus = ApplicationContext.getServiceForClass(IEventBus.class);
		this.descriptionService = new DescriptionService(bus, branchPath);
		this.topLevelConceptIds = getTopLevelConceptIds();
		this.testResourceProvider = testResourceProvider;
	}

	@Override
	public Set<String> getFSNs(Set<String> conceptIds, String... languageRefsetIds) {
		
		List<ExtendedLocale> locales = Stream.of(languageRefsetIds)
			.map(languageRefsetId -> {
				String languageCode = LanguageCodeReferenceSetIdentifierMapping.getLanguageCode(languageRefsetId);
				return ExtendedLocale.valueOf(languageCode + "-x-" + languageRefsetId);
			})
			.collect(toList());
		
		return descriptionService.getFullySpecifiedNames(conceptIds, locales).values().stream()
			.map(SnomedDescription::getTerm)
			.collect(toSet());
		
	}
	
	@Override
	public Set<Description> findActiveDescriptionByExactTerm(String exactTerm) {
		
		Set<Description> results = newHashSet();
		
		results.addAll(SnomedRequests.prepareSearchDescription()
				.all()
				.filterByActive(true)
				.filterByExactTerm(exactTerm)
				.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
				.execute(bus)
				.then(descriptions -> {
					return descriptions.getItems().stream()
						.filter(description -> description.getTerm().equals(exactTerm)) // see com.b2international.index.Analyzers.EXACT
						.map(description -> new ValidationSnomedDescription(description, description.getConceptId()))
						.collect(toSet());
				})
				.getSync());

		return results;
	}

	@Override
	public Set<Description> findInactiveDescriptionByExactTerm(String exactTerm) {
		
		Set<Description> results = newHashSet();
		
		results.addAll(SnomedRequests.prepareSearchDescription()
				.all()
				.filterByActive(false)
				.filterByExactTerm(exactTerm)
				.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
				.execute(bus)
				.then(descriptions -> {
					return descriptions.getItems().stream()
						.filter(description -> description.getTerm().equals(exactTerm)) // see com.b2international.index.Analyzers.EXACT
						.map(description -> new ValidationSnomedDescription(description, description.getConceptId()))
						.collect(toSet());
				})
				.getSync());

		return results;
	}

	@Override
	public Set<Description> findMatchingDescriptionInHierarchy(Concept concept, Description description) {

		List<SnomedDescription> matchingDescriptions = SnomedRequests.prepareSearchDescription()
			.all()
			.filterByActive(true)
			.filterByExactTerm(description.getTerm())
			.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
			.execute(bus)
			.then(descriptions -> {
				return descriptions.getItems().stream()
					.filter(d -> d.getTerm().equals(description.getTerm())) // see com.b2international.index.Analyzers.EXACT
					.collect(toList());
			})
			.getSync();

		if (matchingDescriptions.isEmpty()) {
			return emptySet();
		}
		
		Set<String> conceptIdsWithSameTerm = matchingDescriptions.stream().map(SnomedDescription::getConceptId).collect(toSet()); 
		
		Set<String> conceptIdsToFetch = newHashSet(conceptIdsWithSameTerm);
		conceptIdsToFetch.add(concept.getId());
		
		SnomedConcepts concepts = SnomedRequests.prepareSearchConcept()
			.setLimit(conceptIdsToFetch.size())
			.filterByActive(true)
			.filterByIds(conceptIdsToFetch)
			.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
			.execute(bus)
			.getSync();
			
		Optional<SnomedConcept> currentConcept = concepts.getItems().stream().filter(c -> c.getId().equals(concept.getId())).findFirst();
		
		if (currentConcept.isPresent()) {
			
			Set<String> topLevelIds = Sets.intersection(SnomedConcept.GET_ANCESTORS.apply(currentConcept.get()), topLevelConceptIds);
			
			Set<String> conceptIdsWithSameTermInHierarchy = concepts.getItems().stream()
				.filter(c -> !c.getId().equals(concept.getId()))
				.filter(c -> {
					Set<String> topLevelIdsForMatchingConcept = Sets.intersection(SnomedConcept.GET_ANCESTORS.apply(c), topLevelConceptIds);
					return !Sets.intersection(topLevelIdsForMatchingConcept, topLevelIds).isEmpty();
				})
				.map(SnomedConcept::getId)
				.collect(toSet());
			
			return matchingDescriptions.stream()
				.filter(desc -> conceptIdsWithSameTermInHierarchy.contains(desc.getConceptId()))
				.map(desc -> new ValidationSnomedDescription(desc, desc.getConceptId()))
				.collect(toSet());
			
		}
		
		return emptySet();
	}

	@Override
	public String getLanguageSpecificErrorMessage(Description description) {

		String errorMessage = "";

		// null checks
		if (description == null || description.getAcceptabilityMap() == null || description.getTerm() == null) {
			return errorMessage;
		}
		
		return DescriptionHelper.getLanguageSpecificErrorMessage(description, testResourceProvider.getUsToGbTermMap());

	}

	@Override
	public String getCaseSensitiveWordsErrorMessage(Description description) {
		
		String result = "";

		// return immediately if description or term null
		if (description == null || description.getTerm() == null) {
			return result;
		}
		
		return DescriptionHelper.getCaseSensitiveWordsErrorMessage(description, testResourceProvider.getCaseSignificantWords());
	}

	@Override
	public Set<String> findParentsNotContainingSemanticTag(Concept concept, String termSemanticTag, String... languageRefsetIds) {
		
		Set<String> statedParentIds = concept.getRelationships().stream()
			.filter(r -> r.isActive() && Concepts.STATED_RELATIONSHIP.equals(r.getCharacteristicTypeId()) && Concepts.IS_A.equals(r.getTypeId()))
			.map(Relationship::getDestinationId)
			.collect(toSet());
		
		List<ExtendedLocale> locales = Stream.of(languageRefsetIds)
			.map(languageRefsetId -> {
				String languageCode = LanguageCodeReferenceSetIdentifierMapping.getLanguageCode(languageRefsetId);
				return ExtendedLocale.valueOf(languageCode + "-x-" + languageRefsetId);
			})
			.collect(toList());

		return descriptionService.getFullySpecifiedNames(statedParentIds, locales).values().stream()
			.filter(description -> !termSemanticTag.equals(DescriptionHelper.getTag(description.getTerm())))
			.map(SnomedDescription::getConceptId)
			.collect(toSet());
		
	}
	
	@Override
	public boolean isRecognisedSemanticTag(String semanticTag) {
		return semanticTag != null && !semanticTag.isEmpty() && testResourceProvider.getSemanticTags().contains(semanticTag);
	}

	private Set<String> getTopLevelConceptIds() {
		
		Set<String> topLevelIds = SnomedRequests.prepareSearchConcept()
			.all()
			.setFields(SnomedConceptDocument.Fields.ID)
			.filterByActive(true)
			.filterByParent(Concepts.ROOT_CONCEPT)
			.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
			.execute(bus)
			.then(concepts -> concepts.getItems().stream().map(SnomedConcept::getId).collect(toSet()))
			.getSync();
		
		LOGGER.info("Cached {} top level concept ids for validation", topLevelIds.size());
		
		return topLevelIds;
	}
}
