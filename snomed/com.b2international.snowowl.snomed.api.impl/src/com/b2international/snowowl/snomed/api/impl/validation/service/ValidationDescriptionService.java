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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.b2international.commons.http.ExtendedLocale;
import com.b2international.snowowl.core.ApplicationContext;
import com.b2international.snowowl.eventbus.IEventBus;
import com.b2international.snowowl.snomed.SnomedConstants.Concepts;
import com.b2international.snowowl.snomed.SnomedConstants.LanguageCodeReferenceSetIdentifierMapping;
import com.b2international.snowowl.snomed.api.impl.DescriptionService;
import com.b2international.snowowl.snomed.api.impl.validation.domain.ValidationSnomedDescription;
import com.b2international.snowowl.snomed.core.domain.CaseSignificance;
import com.b2international.snowowl.snomed.core.domain.SnomedConcept;
import com.b2international.snowowl.snomed.core.domain.SnomedConcepts;
import com.b2international.snowowl.snomed.core.domain.SnomedDescription;
import com.b2international.snowowl.snomed.datastore.SnomedDatastoreActivator;
import com.b2international.snowowl.snomed.datastore.index.entry.SnomedConceptDocument;
import com.b2international.snowowl.snomed.datastore.request.SnomedRequests;
import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

public class ValidationDescriptionService implements org.ihtsdo.drools.service.DescriptionService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ValidationDescriptionService.class);
	
	private static final Splitter WHITESPACE_SPLITTER = Splitter.on(CharMatcher.WHITESPACE);
	
	private DescriptionService descriptionService;
	private String branchPath;
	private IEventBus bus;

	private Set<String> caseSignificantWords;
	private Multimap<String, String> refsetToLanguageSpecificWordsMap;
	private Set<String> topLevelConceptIds;

	public ValidationDescriptionService(String branchPath, Set<String> caseSignificantWords, Multimap<String, String> refsetToLanguageSpecificWordsMap) {
		this.branchPath = branchPath;
		this.bus = ApplicationContext.getServiceForClass(IEventBus.class);
		this.descriptionService = new DescriptionService(bus, branchPath);
		this.caseSignificantWords = caseSignificantWords;
		this.refsetToLanguageSpecificWordsMap = refsetToLanguageSpecificWordsMap;
		this.topLevelConceptIds = getTopLevelConceptIds();
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

		// Only check active synonyms
		if (!description.getTypeId().equals(Concepts.SYNONYM) || !description.isActive()) {
			return errorMessage;
		}
		
		Iterable<String> words = WHITESPACE_SPLITTER.split(description.getTerm());

		// convenience variables
		String usAcc = description.getAcceptabilityMap().get(Concepts.REFSET_LANGUAGE_TYPE_US);
		String gbAcc = description.getAcceptabilityMap().get(Concepts.REFSET_LANGUAGE_TYPE_UK);

		// NOTE: Supports international only at this point
		for (String word : words) {

			// Step 1: Check en-us preferred synonyms for en-gb spellings
			if (usAcc != null 
					&& refsetToLanguageSpecificWordsMap.containsKey(Concepts.REFSET_LANGUAGE_TYPE_UK)
					&& refsetToLanguageSpecificWordsMap.get(Concepts.REFSET_LANGUAGE_TYPE_UK).contains(word.toLowerCase())) {
				
				errorMessage += "Synonym is preferred in the en-us refset but refers to a word that has en-gb spelling: " + word + "\n";
			}

			// Step 2: Check en-gb preferred synonyms for en-us spellings
			if (gbAcc != null 
					&& refsetToLanguageSpecificWordsMap.containsKey(Concepts.REFSET_LANGUAGE_TYPE_US)
					&& refsetToLanguageSpecificWordsMap.get(Concepts.REFSET_LANGUAGE_TYPE_US).contains(word.toLowerCase())) {
				
				errorMessage += "Synonym is preferred in the en-gb refset but refers to a word that has en-us spelling: " + word + "\n";
			}
		}

		return errorMessage;

	}

	@Override
	public String getCaseSensitiveWordsErrorMessage(Description description) {
		
		String result = "";

		// return immediately if description or term null
		if (description == null || description.getTerm() == null) {
			return result;
		}

		Iterable<String> words = WHITESPACE_SPLITTER.split(description.getTerm());

		for (String word : words) {

			// NOTE: Simple test to see if a case-sensitive term exists as
			// written. Original check for mis-capitalization, but false
			// positives, e.g. "oF" appears in list but spuriously reports "of"
			// Map preserved for lower-case matching in future
			if (caseSignificantWords.contains(word)) {

				// term starting with case sensitive word must be ETCS
				if (description.getTerm().startsWith(word)
						&& !CaseSignificance.ENTIRE_TERM_CASE_SENSITIVE.getConceptId().equals(description.getCaseSignificanceId())) {
					
					result += "Description starts with case-sensitive word but is not marked entire term case sensitive: " + word + ".\n";
				}

				// term containing case sensitive word (not at start) must be
				// ETCS or OICCI
				else if (!CaseSignificance.ENTIRE_TERM_CASE_SENSITIVE.getConceptId().equals(description.getCaseSignificanceId())
							&& !CaseSignificance.INITIAL_CHARACTER_CASE_INSENSITIVE.getConceptId().equals(description.getCaseSignificanceId())) {
					
					result += "Description contains case-sensitive word but is not marked entire term case sensitive or only initial character case insensitive: " + word + ".\n";
				}
			}
		}
		
		return result;
	}

	@Override
	public Set<String> findParentsNotContainSematicTag(Concept concept, String termSemanticTag, String... languageRefsetIds) {
		
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
