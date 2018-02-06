/*
 * Copyright 2011-2017 B2i Healthcare Pte Ltd, http://b2i.sg
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.b2international.snowowl.snomed.api.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.otf.owltoolkit.conversion.ConversionService;

import com.b2international.commons.ClassUtils;
import com.b2international.commons.collections.Procedure;
import com.b2international.commons.http.ExtendedLocale;
import com.b2international.snowowl.core.ApplicationContext;
import com.b2international.snowowl.core.api.IBranchPath;
import com.b2international.snowowl.core.branch.Branch;
import com.b2international.snowowl.core.domain.IComponent;
import com.b2international.snowowl.core.domain.IComponentRef;
import com.b2international.snowowl.core.domain.IStorageRef;
import com.b2international.snowowl.core.domain.TransactionContext;
import com.b2international.snowowl.core.events.Request;
import com.b2international.snowowl.core.events.bulk.BulkRequest;
import com.b2international.snowowl.core.events.bulk.BulkRequestBuilder;
import com.b2international.snowowl.core.exceptions.ComponentNotFoundException;
import com.b2international.snowowl.core.exceptions.NotFoundException;
import com.b2international.snowowl.datastore.request.CommitResult;
import com.b2international.snowowl.datastore.request.RepositoryCommitRequestBuilder;
import com.b2international.snowowl.datastore.request.RepositoryRequests;
import com.b2international.snowowl.datastore.request.SearchResourceRequest;
import com.b2international.snowowl.datastore.server.domain.InternalComponentRef;
import com.b2international.snowowl.datastore.server.domain.InternalStorageRef;
import com.b2international.snowowl.eventbus.IEventBus;
import com.b2international.snowowl.snomed.SnomedConstants.Concepts;
import com.b2international.snowowl.snomed.api.browser.ISnomedBrowserService;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserBulkChangeRun;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserChildConcept;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserConcept;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserConstant;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserDescription;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserDescriptionResult;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserParentConcept;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserRelationship;
import com.b2international.snowowl.snomed.api.domain.browser.SnomedBrowserBulkChangeStatus;
import com.b2international.snowowl.snomed.api.domain.browser.SnomedBrowserDescriptionType;
import com.b2international.snowowl.snomed.api.domain.browser.TaxonomyNode;
import com.b2international.snowowl.snomed.api.impl.domain.InputFactory;
import com.b2international.snowowl.snomed.api.impl.domain.SnomedBrowserAxiomUpdateHelper;
import com.b2international.snowowl.snomed.api.impl.domain.browser.SnomedBrowserBulkChangeRun;
import com.b2international.snowowl.snomed.api.impl.domain.browser.SnomedBrowserChildConcept;
import com.b2international.snowowl.snomed.api.impl.domain.browser.SnomedBrowserConcept;
import com.b2international.snowowl.snomed.api.impl.domain.browser.SnomedBrowserConstant;
import com.b2international.snowowl.snomed.api.impl.domain.browser.SnomedBrowserDescription;
import com.b2international.snowowl.snomed.api.impl.domain.browser.SnomedBrowserDescriptionResult;
import com.b2international.snowowl.snomed.api.impl.domain.browser.SnomedBrowserDescriptionResultDetails;
import com.b2international.snowowl.snomed.api.impl.domain.browser.SnomedBrowserParentConcept;
import com.b2international.snowowl.snomed.api.impl.domain.browser.SnomedBrowserRelationship;
import com.b2international.snowowl.snomed.api.impl.domain.browser.SnomedBrowserRelationshipTarget;
import com.b2international.snowowl.snomed.api.impl.domain.browser.SnomedBrowserRelationshipType;
import com.b2international.snowowl.snomed.core.domain.CaseSignificance;
import com.b2international.snowowl.snomed.core.domain.CharacteristicType;
import com.b2international.snowowl.snomed.core.domain.ConceptEnum;
import com.b2international.snowowl.snomed.core.domain.DefinitionStatus;
import com.b2international.snowowl.snomed.core.domain.RelationshipModifier;
import com.b2international.snowowl.snomed.core.domain.SnomedConcept;
import com.b2international.snowowl.snomed.core.domain.SnomedConcepts;
import com.b2international.snowowl.snomed.core.domain.SnomedDescription;
import com.b2international.snowowl.snomed.core.domain.SnomedDescriptions;
import com.b2international.snowowl.snomed.core.domain.SnomedRelationship;
import com.b2international.snowowl.snomed.core.domain.refset.SnomedReferenceSetMembers;
import com.b2international.snowowl.snomed.datastore.SnomedDatastoreActivator;
import com.b2international.snowowl.snomed.datastore.request.SnomedConceptCreateRequest;
import com.b2international.snowowl.snomed.datastore.request.SnomedConceptUpdateRequest;
import com.b2international.snowowl.snomed.datastore.request.SnomedDescriptionCreateRequest;
import com.b2international.snowowl.snomed.datastore.request.SnomedDescriptionUpdateRequest;
import com.b2international.snowowl.snomed.datastore.request.SnomedRelationshipCreateRequest;
import com.b2international.snowowl.snomed.datastore.request.SnomedRelationshipUpdateRequest;
import com.b2international.snowowl.snomed.datastore.request.SnomedRequests;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;

@SuppressWarnings("deprecation")
public class SnomedBrowserService implements ISnomedBrowserService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SnomedBrowserService.class);
	private static final List<ConceptEnum> CONCEPT_ENUMS;

	static {
		final ImmutableList.Builder<ConceptEnum> conceptEnumsBuilder = ImmutableList.builder();
		
		conceptEnumsBuilder.add(DefinitionStatus.values());
		conceptEnumsBuilder.add(CharacteristicType.values());
		conceptEnumsBuilder.add(CaseSignificance.values());
		conceptEnumsBuilder.add(SnomedBrowserDescriptionType.values());
		conceptEnumsBuilder.add(RelationshipModifier.values());
		
		CONCEPT_ENUMS = conceptEnumsBuilder.build();
	}

	private static final Map<String, String> dialectMatchesUsToGb;

	static {
		dialectMatchesUsToGb = new HashMap<>();
		String fileName = "/opt/termserver/resources/test-resources/spelling_variants.txt";

		File file = new File(fileName);
		FileReader fileReader;
		BufferedReader bufferedReader;
		try {
			fileReader = new FileReader(file);
			bufferedReader = new BufferedReader(fileReader);
			String line;
			// skip header line
			// TODO Check whether header line exists in file
			bufferedReader.readLine();
			while ((line = bufferedReader.readLine()) != null) {
				String[] words = line.split("\\s+");

				dialectMatchesUsToGb.put(words[0], words[1]);
			}
			fileReader.close();
			LOGGER.info("Loaded " + dialectMatchesUsToGb.size() + " spelling variants from: " + fileName);
		} catch (IOException e) {
			LOGGER.info("Failed to retrieve case sensitive words file: " + fileName);

		}
	}
	
	
	private final Cache<String, SnomedBrowserBulkChangeRun> bulkChangeRuns = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.DAYS).build();
	
	private final ExecutorService executorService = Executors.newCachedThreadPool();
	
	private final SnomedBrowserAxiomExpander axiomExpander = new SnomedBrowserAxiomExpander();

	@Resource
	private IEventBus bus;

	private IEventBus bus() {
		return Preconditions.checkNotNull(bus == null ? bus = ApplicationContext.getInstance().getServiceChecked(IEventBus.class) : bus,  "bus cannot be null!");
	}

	@Override
	public ISnomedBrowserConcept getConceptDetails(final String branchPath, final String conceptId, final List<ExtendedLocale> extendedLocales) {
		Set<ISnomedBrowserConcept> concepts = getConceptDetailsInBulk(branchPath, Collections.singleton(conceptId), extendedLocales);
		if (concepts.isEmpty()) {
			throw new NotFoundException("Snomed Concept", conceptId);			
		}
		return concepts.iterator().next();
	}
	
	@Override
	public Set<ISnomedBrowserConcept> getConceptDetailsInBulk(final String branchPath, final Set<String> conceptIds, final List<ExtendedLocale> extendedLocales) {
		if (conceptIds.isEmpty()) {
			return Collections.emptySet();
		}
		
		SnomedConcepts concepts = SnomedRequests.prepareSearchConcept()
			.all()
			.filterByIds(conceptIds)
			.setLocales(extendedLocales)
			.setExpand("fsn(),pt(),inactivationProperties(),descriptions(limit:"+Integer.MAX_VALUE+",expand(inactivationProperties())),relationships(limit:"+Integer.MAX_VALUE+",expand(type(expand(fsn())),destination(expand(fsn()))))")
			.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
			.execute(bus())
			.getSync();

		ConversionService conversionService = getAxiomConversionService(branchPath, bus);

		Set<SnomedBrowserConcept> results = new HashSet<>();
		for (SnomedConcept concept : concepts.getItems()) {
			final SnomedBrowserConcept result = convertConcept(concept);
			
			// inactivation fields
			result.setInactivationIndicator(concept.getInactivationIndicator());
			result.setAssociationTargets(concept.getAssociationTargets());
			
			final SnomedDescription fullySpecifiedName = concept.getFsn();
			if (fullySpecifiedName != null) {
				result.setFsn(fullySpecifiedName.getTerm());
			} else {
				result.setFsn(result.getConceptId());
			}
			
			final SnomedDescription preferredSynonym = concept.getPt();
			if (preferredSynonym != null) {
				result.setPreferredSynonym(preferredSynonym.getTerm());
			}
	
			final SnomedDescriptions descriptions = concept.getDescriptions();
			result.setDescriptions(convertDescriptions(descriptions));
	
			result.setRelationships(convertRelationships(concept.getRelationships()));
			results.add(result);
		}
		axiomExpander.expandAxioms(results, conversionService, extendedLocales, branchPath, bus());
		
		return results.stream().collect(Collectors.toSet());
	}
	
	public static ConversionService getAxiomConversionService(final String branchPath, IEventBus eventBus) {
		final SnomedReferenceSetMembers mrcmMembers = SnomedRequests.prepareSearchMember()
				.all()
				.filterByRefSet(Sets.newHashSet(Concepts.REFSET_MRCM_ATTRIBUTE_DOMAIN_INTERNATIONAL))
				.filterByActive(true)
				.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
				.execute(eventBus)
				.getSync();
		
		Set<Long> ungroupedAttributes = mrcmMembers.getItems().stream().filter(member -> {
			Object object = member.getProperties().get("grouped");
			Boolean grouped = (Boolean) object;
			return !grouped;
		}).map(member -> Long.parseLong(member.getReferencedComponent().getId())).collect(Collectors.toSet());
		return new org.snomed.otf.owltoolkit.conversion.ConversionService(ungroupedAttributes);
	}

	private SnomedBrowserConcept convertConcept(SnomedConcept concept) {
		final SnomedBrowserConcept result = new SnomedBrowserConcept();
		
		result.setActive(concept.isActive());
		result.setReleased(concept.isReleased());
		result.setConceptId(concept.getId());
		result.setDefinitionStatus(concept.getDefinitionStatus());
		result.setEffectiveTime(concept.getEffectiveTime());
		result.setModuleId(concept.getModuleId());
		
		return result;
	}

	@Override
	public ISnomedBrowserConcept create(String branchPath, ISnomedBrowserConcept newConcept, String userId, List<ExtendedLocale> locales) {
		// If calling from an Autowired context, bus might not have been set
		if (bus == null) {
			bus = com.b2international.snowowl.core.ApplicationContext.getInstance().getServiceChecked(IEventBus.class);
		}
		InputFactory inputFactory = new InputFactory(getBranch(branchPath), new SnomedBrowserAxiomUpdateHelper(getAxiomConversionService(branchPath, bus)));
		final SnomedConceptCreateRequest req = inputFactory.createComponentInput(branchPath, newConcept, SnomedConceptCreateRequest.class);
		final String commitComment = getCommitComment(userId, newConcept, "creating");
		
		final String createdConceptId = SnomedRequests
				.prepareCommit()
				.setCommitComment(commitComment)
				.setBody(req)
				.setUserId(userId)
				.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
				.execute(bus())
				.getSync()
				.getResultAs(String.class);
		
		return getConceptDetails(branchPath, createdConceptId, locales);
	}

	private Branch getBranch(String branchPath) {
		return RepositoryRequests.branching().prepareGet(branchPath).build(SnomedDatastoreActivator.REPOSITORY_UUID).execute(bus()).getSync();
	}
	
	@Override
	public void update(String branch, List<? extends ISnomedBrowserConcept> updatedConcepts, String userId, List<ExtendedLocale> locales) {
		final String commitComment = userId + " Bulk update.";
		createBulkCommit(branch, updatedConcepts, userId, locales, commitComment)
			.build(SnomedDatastoreActivator.REPOSITORY_UUID, branch)
			.execute(bus())
			.getSync();
		
		LOGGER.info("Committed bulk concept changes on {}", branch);
	}
	
	private RepositoryCommitRequestBuilder createBulkCommit(String branchPath, List<? extends ISnomedBrowserConcept> updatedConcepts, String userId, List<ExtendedLocale> locales, final String commitComment) {
		final Stopwatch watch = Stopwatch.createStarted();
		
		final BulkRequestBuilder<TransactionContext> bulkRequest = BulkRequest.create();

		// Create services once and reuse
		SnomedBrowserAxiomUpdateHelper axiomUpdateHelper = new SnomedBrowserAxiomUpdateHelper(getAxiomConversionService(branchPath, bus));		
		InputFactory inputFactory = new InputFactory(getBranch(branchPath), axiomUpdateHelper);

		// Process concepts in batches of 1000
		for (List<? extends ISnomedBrowserConcept> updatedConceptsBatch : Lists.partition(updatedConcepts, 1000)) {
			// Load existing versions in bulk
			Set<String> conceptIds = updatedConceptsBatch.stream().map(ISnomedBrowserConcept::getConceptId).collect(Collectors.toSet());
			Set<ISnomedBrowserConcept> existingConcepts = getConceptDetailsInBulk(branchPath, conceptIds, locales);
			Map<String, ISnomedBrowserConcept> existingConceptsMap = existingConcepts.stream().collect(Collectors.toMap(ISnomedBrowserConcept::getConceptId, concept -> concept));
			
			// For each concept add component updates to the bulk request
			for (ISnomedBrowserConcept concept : updatedConceptsBatch) {
				ISnomedBrowserConcept existingConcept = existingConceptsMap.get(concept.getConceptId());
				if (existingConcept == null) {
					// If one existing concept is not found fail the whole commit 
					throw new NotFoundException("Snomed Concept", concept.getConceptId());
				}
				update(branchPath, concept, existingConcept, userId, locales, bulkRequest, inputFactory);
			}
		}
		
		// Commit everything at once
		final RepositoryCommitRequestBuilder commit = SnomedRequests
			.prepareCommit()
			.setUserId(userId)
			.setCommitComment(commitComment)
			.setPreparationTime(watch.elapsed(TimeUnit.MILLISECONDS))
			.setBody(bulkRequest);

		return commit;
	}
	
	@Override
	public SnomedBrowserBulkChangeRun beginBulkChange(final String branch, final List<? extends ISnomedBrowserConcept> updatedConcepts, final String userId, final List<ExtendedLocale> locales) {
		final SnomedBrowserBulkChangeRun run = new SnomedBrowserBulkChangeRun();
		run.start();

		executorService.submit(new Runnable() {
			@Override
			public void run() {
				try {
				
					final String commitComment = userId + " Bulk update.";
					createBulkCommit(branch, updatedConcepts, userId, locales, commitComment)
						.build(SnomedDatastoreActivator.REPOSITORY_UUID, branch)
						.execute(bus())
						.then(new Function<CommitResult, Void>() {
							@Override public Void apply(CommitResult input) { return onSuccess(); }
						})
						.fail(new Procedure<Throwable>() {
							@Override protected void doApply(Throwable throwable) { onFailure(throwable, "during commit"); }
						});
					
				} catch(Exception e) {
					onFailure(e, "while building commit");
				}
			}
			
			private Void onSuccess() {
				run.end(SnomedBrowserBulkChangeStatus.COMPLETED);
				LOGGER.info("Committed bulk concept changes on {}", branch);
				return null;
			}

			private void onFailure(final Throwable throwable, final String phase) {
				run.end(SnomedBrowserBulkChangeStatus.FAILED);
				LOGGER.error("Bulk concept changes failed {} on {}", phase, branch, throwable);
			}
		});
		
		bulkChangeRuns.put(run.getId(), run);
		return run;
	}
	
	@Override
	public ISnomedBrowserBulkChangeRun getBulkChange(String bulkChangeId) {
		return bulkChangeRuns.getIfPresent(bulkChangeId);
	}
	
	private void update(String branchPath, ISnomedBrowserConcept newVersionConcept, ISnomedBrowserConcept existingVersionConcept, String userId, List<ExtendedLocale> locales, 
			BulkRequestBuilder<TransactionContext> bulkRequest, InputFactory inputFactory) {
		
		LOGGER.info("Update concept start {}", newVersionConcept.getFsn());

		// Concept update
		final SnomedConceptUpdateRequest conceptUpdate = inputFactory.createComponentUpdate(existingVersionConcept, newVersionConcept, SnomedConceptUpdateRequest.class);
		
		// Description updates
		final List<ISnomedBrowserDescription> existingVersionDescriptions = existingVersionConcept.getDescriptions();
		final List<ISnomedBrowserDescription> newVersionDescriptions = newVersionConcept.getDescriptions();
		Set<String> descriptionDeletionIds = inputFactory.getComponentDeletions(existingVersionDescriptions, newVersionDescriptions);
		Map<String, SnomedDescriptionUpdateRequest> descriptionUpdates = inputFactory.createComponentUpdates(existingVersionDescriptions, newVersionDescriptions, SnomedDescriptionUpdateRequest.class);
		List<SnomedDescriptionCreateRequest> descriptionInputs = inputFactory.createComponentInputs(branchPath, newVersionDescriptions, SnomedDescriptionCreateRequest.class);
		LOGGER.info("Got description changes +{} -{} m{}, {}", descriptionInputs.size(), descriptionDeletionIds.size(), descriptionUpdates.size(), newVersionConcept.getFsn());

		// Relationship updates
		final List<ISnomedBrowserRelationship> existingVersionRelationships = existingVersionConcept.getRelationships();
		final List<ISnomedBrowserRelationship> newVersionRelationships = newVersionConcept.getRelationships();
		Set<String> relationshipDeletionIds = inputFactory.getComponentDeletions(existingVersionRelationships, newVersionRelationships);
		Map<String, SnomedRelationshipUpdateRequest> relationshipUpdates = inputFactory.createComponentUpdates(existingVersionRelationships, newVersionRelationships, SnomedRelationshipUpdateRequest.class);
		List<SnomedRelationshipCreateRequest> relationshipInputs = inputFactory.createComponentInputs(branchPath, newVersionRelationships, SnomedRelationshipCreateRequest.class);
		LOGGER.info("Got relationship changes +{} -{} m{}, {}", relationshipInputs.size(), relationshipDeletionIds.size(), relationshipUpdates.size(), newVersionConcept.getFsn());
		
		SnomedBrowserAxiomUpdateHelper axiomUpdateHelper = inputFactory.getAxiomUpdateHelper();
		
		// Additional Axiom persistence requests
		List<Request<TransactionContext, ?>> additionalAxiomRequests = axiomUpdateHelper.getAxiomPersistenceRequests(
				existingVersionConcept.getAdditionalAxioms(), newVersionConcept.getAdditionalAxioms(), true, Long.parseLong(existingVersionConcept.getConceptId()), newVersionConcept.getModuleId());
		
		// GCI Axiom persistence requests
		List<Request<TransactionContext, ?>> gciAxiomRequests = axiomUpdateHelper.getAxiomPersistenceRequests(
				existingVersionConcept.getGciAxioms(), newVersionConcept.getGciAxioms(), false, Long.parseLong(existingVersionConcept.getConceptId()), newVersionConcept.getModuleId());
		
		// In the case of inactivation, other updates seem to go more smoothly if this is done later
		
		boolean conceptInactivation = conceptUpdate != null && conceptUpdate.isActive() != null && Boolean.FALSE.equals(conceptUpdate.isActive());
		if (conceptUpdate != null && !conceptInactivation) {
			bulkRequest.add(conceptUpdate);
		}
		
		for (String descriptionId : descriptionUpdates.keySet()) {
			bulkRequest.add(descriptionUpdates.get(descriptionId));
		}
		for (SnomedDescriptionCreateRequest descriptionReq : descriptionInputs) {
			descriptionReq.setConceptId(existingVersionConcept.getConceptId());
			bulkRequest.add(descriptionReq);
		}
		for (String descriptionDeletionId : descriptionDeletionIds) {
			bulkRequest.add(SnomedRequests.prepareDeleteDescription(descriptionDeletionId).build());
		}

		for (String relationshipId : relationshipUpdates.keySet()) {
			bulkRequest.add(relationshipUpdates.get(relationshipId));
		}
		for (SnomedRelationshipCreateRequest relationshipReq : relationshipInputs) {
			bulkRequest.add(relationshipReq);
		}
		for (String relationshipDeletionId : relationshipDeletionIds) {
			bulkRequest.add(SnomedRequests.prepareDeleteRelationship(relationshipDeletionId).build());
		}
		
		for (Request<TransactionContext, ?> axiomRequest : additionalAxiomRequests) {
			bulkRequest.add(axiomRequest);
		}

		for (Request<TransactionContext, ?> axiomRequest : gciAxiomRequests) {
			bulkRequest.add(axiomRequest);
		}

		// Inactivate concept last
		if (conceptUpdate != null && conceptInactivation) {
			bulkRequest.add(conceptUpdate);
		}
	}

	private String getCommitComment(String userId, ISnomedBrowserConcept snomedConceptInput, String action) {
		String fsn = getFsn(snomedConceptInput);
		return userId + " " + action + " concept " + fsn;
	}

	private String getFsn(ISnomedBrowserConcept concept) {
		if (concept.getFsn() != null) {
			return concept.getFsn();
		}
		
		List<ISnomedBrowserDescription> descriptions = concept.getDescriptions();
		if (descriptions == null) {
			return concept.getConceptId();
		}

		for (ISnomedBrowserDescription description : descriptions) {
			if (!description.isActive()) { continue; }
			if (!Concepts.FULLY_SPECIFIED_NAME.equals(description.getType().getConceptId())) { continue; }
			// Found an active FSN, good to go
			return description.getTerm();	
		}
		
		return concept.getConceptId();
	}
	
	private List<ISnomedBrowserDescription> convertDescriptions(final Iterable<SnomedDescription> descriptions) {
		final ImmutableList.Builder<ISnomedBrowserDescription> convertedDescriptionBuilder = ImmutableList.builder();
		for (final SnomedDescription description : descriptions) {
			final SnomedBrowserDescription convertedDescription = new SnomedBrowserDescription();
	
			final SnomedBrowserDescriptionType descriptionType = SnomedBrowserDescriptionType.getByConceptId(description.getTypeId());
			if (null == descriptionType) {
				LOGGER.warn("Unsupported description type ID {} on description {}, ignoring.", description.getTypeId(), description.getId());
				continue;
			}
			
			convertedDescription.setActive(description.isActive());
			convertedDescription.setReleased(description.isReleased());
			convertedDescription.setCaseSignificance(description.getCaseSignificance());
			convertedDescription.setConceptId(description.getConceptId());
			convertedDescription.setDescriptionId(description.getId());
			convertedDescription.setEffectiveTime(description.getEffectiveTime());
			convertedDescription.setLang(description.getLanguageCode());
			convertedDescription.setModuleId(description.getModuleId());
			convertedDescription.setTerm(description.getTerm());
			convertedDescription.setType(descriptionType);
			convertedDescription.setAcceptabilityMap(description.getAcceptabilityMap());
			
			convertedDescription.setInactivationIndicator(description.getInactivationIndicator());
			convertedDescription.setAssociationTargets(description.getAssociationTargets());
			
			convertedDescriptionBuilder.add(convertedDescription);
		}
	
		return convertedDescriptionBuilder.build();
	}
	
	private List<ISnomedBrowserRelationship> convertRelationships(final Iterable<SnomedRelationship> relationships) {
		final LoadingCache<SnomedConcept, SnomedBrowserRelationshipType> types = CacheBuilder.newBuilder().build(new CacheLoader<SnomedConcept, SnomedBrowserRelationshipType>() {
			@Override
			public SnomedBrowserRelationshipType load(SnomedConcept key) throws Exception {
				return convertBrowserRelationshipType(key);
			}
		});
		
		final LoadingCache<SnomedConcept, SnomedBrowserRelationshipTarget> targets = CacheBuilder.newBuilder().build(new CacheLoader<SnomedConcept, SnomedBrowserRelationshipTarget>() {
			@Override
			public SnomedBrowserRelationshipTarget load(SnomedConcept key) throws Exception {
				return convertBrowserRelationshipTarget(key);
			}
		});
		
		final ImmutableList.Builder<ISnomedBrowserRelationship> convertedRelationshipBuilder = ImmutableList.builder();
		
		for (final SnomedRelationship relationship : relationships) {
			final SnomedBrowserRelationship convertedRelationship = new SnomedBrowserRelationship(relationship.getId());
			
			convertedRelationship.setActive(relationship.isActive());
			convertedRelationship.setCharacteristicType(relationship.getCharacteristicType());
			convertedRelationship.setEffectiveTime(relationship.getEffectiveTime());
			convertedRelationship.setGroupId(relationship.getGroup());
			convertedRelationship.setModifier(relationship.getModifier());
			convertedRelationship.setModuleId(relationship.getModuleId());
			convertedRelationship.setRelationshipId(relationship.getId());
			convertedRelationship.setReleased(relationship.isReleased());
			convertedRelationship.setSourceId(relationship.getSourceId());
			
			convertedRelationship.setTarget(targets.getUnchecked(relationship.getDestination()));
			convertedRelationship.setType(types.getUnchecked(relationship.getType()));
			
			convertedRelationshipBuilder.add(convertedRelationship);
		}
		
		return convertedRelationshipBuilder.build();
	}
	
	/* package */ SnomedBrowserRelationshipType convertBrowserRelationshipType(SnomedConcept concept) {
		final SnomedBrowserRelationshipType result = new SnomedBrowserRelationshipType();
		
		result.setConceptId(concept.getId());
		
		if (concept.getFsn() != null) {
			result.setFsn(concept.getFsn().getTerm());
		} else {
			result.setFsn(concept.getId());
		}
		
		return result;
	}

	/* package */ SnomedBrowserRelationshipTarget convertBrowserRelationshipTarget(SnomedConcept concept) {
		final SnomedBrowserRelationshipTarget result = new SnomedBrowserRelationshipTarget();
		
		result.setActive(concept.isActive());
		result.setConceptId(concept.getId());
		result.setDefinitionStatus(concept.getDefinitionStatus());
		result.setEffectiveTime(concept.getEffectiveTime());
		
		if (concept.getFsn() != null) {
			result.setFsn(concept.getFsn().getTerm());
		} else {
			result.setFsn(concept.getId());
		}
		
		result.setModuleId(concept.getModuleId());
		result.setReleased(concept.isReleased());
		
		return result;
	}

	private SnomedConcepts getConcepts(final IBranchPath branchPath, final Set<String> destinationConceptIds) {
		if (destinationConceptIds.isEmpty()) {
			return new SnomedConcepts(0, 0, 0);
		}
		return SnomedRequests.prepareSearchConcept()
				.all()
				.filterByIds(destinationConceptIds)
				.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath.getPath())
				.execute(bus())
				.getSync();
	}

	protected SnomedBrowserRelationshipTarget getSnomedBrowserRelationshipTarget(SnomedConcept destinationConcept, String branch, List<ExtendedLocale> locales) {
		final DescriptionService descriptionService = new DescriptionService(bus, branch);
		final SnomedBrowserRelationshipTarget target = new SnomedBrowserRelationshipTarget();

		target.setActive(destinationConcept.isActive());
		target.setConceptId(destinationConcept.getId());
		target.setDefinitionStatus(destinationConcept.getDefinitionStatus());
		target.setEffectiveTime(destinationConcept.getEffectiveTime());
		target.setModuleId(destinationConcept.getModuleId());

		SnomedDescription fullySpecifiedName = descriptionService.getFullySpecifiedName(destinationConcept.getId(), locales);
		if (fullySpecifiedName != null) {
			target.setFsn(fullySpecifiedName.getTerm());
		} else {
			target.setFsn(destinationConcept.getId());
		}
		
		return target;
	}

	@Override
	public List<ISnomedBrowserParentConcept> getConceptParents(final IComponentRef conceptRef, final List<ExtendedLocale> locales) {
		return getConceptParents(conceptRef, locales, SnomedBrowserDescriptionType.FSN);
	}
	
	public List<ISnomedBrowserParentConcept> getConceptParents(final IComponentRef conceptRef, final List<ExtendedLocale> locales, SnomedBrowserDescriptionType preferredDescriptionType) {
		final InternalComponentRef internalConceptRef = ClassUtils.checkAndCast(conceptRef, InternalComponentRef.class);
		final IBranchPath branchPath = internalConceptRef.getBranch().branchPath();
		final DescriptionService descriptionService = new DescriptionService(bus, conceptRef.getBranchPath());

		return new FsnJoinerOperation<ISnomedBrowserParentConcept>(conceptRef.getComponentId(), locales, descriptionService, preferredDescriptionType) {
			
			@Override
			protected Iterable<SnomedConcept> getConceptEntries(String conceptId) {
				return SnomedRequests.prepareGetConcept(conceptId)
						.setExpand("ancestors(form:\"inferred\",direct:true)")
						.setLocales(locales)
						.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath.getPath())
						.execute(bus())
						.getSync().getAncestors();
			}

			@Override
			protected ISnomedBrowserParentConcept convertConceptEntry(SnomedConcept conceptEntry, Optional<SnomedDescription> descriptionOptional) {
				final String childConceptId = conceptEntry.getId();
				final SnomedBrowserParentConcept convertedConcept = new SnomedBrowserParentConcept(); 

				convertedConcept.setConceptId(childConceptId);
				convertedConcept.setDefinitionStatus(conceptEntry.getDefinitionStatus());
				
				String term = descriptionOptional.transform(description -> description.getTerm()).or(conceptEntry.getId());
				if (preferredDescriptionType == SnomedBrowserDescriptionType.FSN) {
					convertedConcept.setFsn(term);
				} else if (preferredDescriptionType == SnomedBrowserDescriptionType.SYNONYM) {
					convertedConcept.setPreferredSynonym(term);
				}
				
				return convertedConcept;
			}
			
		}.run();
	}
	
	
	@Override
	// TODO Remove unused method
	public List<ISnomedBrowserChildConcept> getConceptChildren(IComponentRef conceptRef, List<ExtendedLocale> locales, boolean stated) {
		return getConceptChildren(conceptRef, locales, stated, SnomedBrowserDescriptionType.FSN, 0, 2000);
	}
	
	@Override
	public List<ISnomedBrowserChildConcept> getConceptChildren(final IComponentRef conceptRef, final List<ExtendedLocale> locales, final boolean stated, 
			final SnomedBrowserDescriptionType preferredDescriptionType, final int offset, final int limit) {
		final InternalComponentRef internalConceptRef = ClassUtils.checkAndCast(conceptRef, InternalComponentRef.class);
		final String branch = internalConceptRef.getBranch().path();
		final DescriptionService descriptionService = new DescriptionService(bus, conceptRef.getBranchPath());

		return new FsnJoinerOperation<ISnomedBrowserChildConcept>(conceptRef.getComponentId(), locales, descriptionService, preferredDescriptionType) {
			
			@Override
			protected Iterable<SnomedConcept> getConceptEntries(String conceptId) {
				return SnomedRequests.prepareSearchConcept()
						.setOffset(offset)
						.setLimit(limit)
						.filterByActive(true)
						.filterByParent(stated ? null : conceptId)
						.filterByStatedParent(stated ? conceptId : null)
						.build(SnomedDatastoreActivator.REPOSITORY_UUID, branch)
						.execute(bus())
						.getSync();
				
			}

			@Override
			protected ISnomedBrowserChildConcept convertConceptEntry(SnomedConcept conceptEntry, Optional<SnomedDescription> descriptionOptional) {
				final String childConceptId = conceptEntry.getId();
				final SnomedBrowserChildConcept convertedConcept = new SnomedBrowserChildConcept(); 

				convertedConcept.setConceptId(childConceptId);
				convertedConcept.setActive(conceptEntry.isActive());
				convertedConcept.setDefinitionStatus(conceptEntry.getDefinitionStatus());
				convertedConcept.setModuleId(conceptEntry.getModuleId());
				
				
				String term = descriptionOptional.transform(description -> description.getTerm()).or(conceptEntry.getId());
				if (preferredDescriptionType == SnomedBrowserDescriptionType.FSN) {
					convertedConcept.setFsn(term);
				} else if (preferredDescriptionType == SnomedBrowserDescriptionType.SYNONYM) {
					convertedConcept.setPreferredSynonym(term);
				}
				populateLeafFields(branch, childConceptId, convertedConcept);

				return convertedConcept;
			}
			
		}.run();
	}

	private void populateLeafFields(final String branch, final String conceptId, final TaxonomyNode node) {
		node.setIsLeafStated(!hasInboundRelationships(branch, conceptId, Concepts.STATED_RELATIONSHIP));
		node.setIsLeafInferred(!hasInboundRelationships(branch, conceptId, Concepts.INFERRED_RELATIONSHIP));
	}

	private boolean hasInboundRelationships(String branch, String conceptId, String characteristicTypeId) {
		return SnomedRequests.prepareSearchRelationship()
				.filterByActive(true)
				.filterByCharacteristicType(characteristicTypeId)
				.filterByDestination(conceptId)
				.filterByType(Concepts.IS_A)
				.setLimit(0)
				.build(SnomedDatastoreActivator.REPOSITORY_UUID, branch)
				.execute(bus())
				.getSync().getTotal() > 0;
	}

	@Override
	public List<ISnomedBrowserDescriptionResult> getDescriptions(final IStorageRef storageRef, final String query, final List<ExtendedLocale> locales, final SnomedBrowserDescriptionType resultConceptTermType, final int offset, final int limit) {
		checkNotNull(storageRef, "Storage reference may not be null.");
		checkNotNull(query, "Query may not be null.");
		checkArgument(query.length() >= 3, "Query must be at least 3 characters long.");

		final InternalStorageRef internalStorageRef = ClassUtils.checkAndCast(storageRef, InternalStorageRef.class);
		internalStorageRef.checkStorageExists();

		final IBranchPath branchPath = internalStorageRef.getBranch().branchPath();
		final DescriptionService descriptionService = new DescriptionService(bus, storageRef.getBranchPath());
		
		final Collection<SnomedDescription> descriptions = SnomedRequests.prepareSearchDescription()
			.setOffset(offset)
			.setLimit(limit)
			.filterByTerm(query)
			.sortBy(SearchResourceRequest.SCORE)
			.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath.getPath())
			.execute(bus())
			.getSync()
			.getItems();
		
		final List<SnomedDescription> sortedDescriptions = FluentIterable.from(descriptions)
			.toSortedList(new Comparator<SnomedDescription>() {
				@Override
				public int compare(SnomedDescription d1, SnomedDescription d2) {
					int result = d2.getScore().compareTo(d1.getScore());

					if (result == 0) {
						result = Ints.compare(d1.getTerm().length(), d2.getTerm().length());
					}
					
					return result;
				}
			});

		final Set<String> conceptIds = FluentIterable.from(sortedDescriptions)
			.transform(new Function<SnomedDescription, String>() {
				@Override public String apply(SnomedDescription input) {
					return input.getConceptId();
				}
			})
			.toSet();
		
		final Iterable<SnomedConcept> conceptIndexEntries = getConcepts(branchPath, conceptIds);
		final Map<String, SnomedConcept> conceptMap = Maps.uniqueIndex(conceptIndexEntries, IComponent.ID_FUNCTION);
		
		final Map<String, SnomedDescription> descriptionByConceptIdMap; 
		switch (resultConceptTermType) {
		case FSN:
			descriptionByConceptIdMap = descriptionService.getFullySpecifiedNames(conceptIds, locales);
			break;
		default:
			descriptionByConceptIdMap = descriptionService.getPreferredTerms(conceptIds, locales);
			break;
		}
		
		final Cache<String, SnomedBrowserDescriptionResultDetails> detailCache = CacheBuilder.newBuilder().build();
		final ImmutableList.Builder<ISnomedBrowserDescriptionResult> resultBuilder = ImmutableList.builder();
		
		for (final SnomedDescription description : sortedDescriptions) {
			
			final String typeId = description.getTypeId();
			if (!Concepts.FULLY_SPECIFIED_NAME.equals(typeId) && !Concepts.SYNONYM.equals(typeId)) {
				continue;
			}
			
			final SnomedBrowserDescriptionResult descriptionResult = new SnomedBrowserDescriptionResult();
			descriptionResult.setActive(description.isActive());
			descriptionResult.setTerm(description.getTerm());
			
			try {
				final SnomedBrowserDescriptionResultDetails details = detailCache.get(description.getConceptId(), 
						new Callable<SnomedBrowserDescriptionResultDetails>() {
					
					@Override
					public SnomedBrowserDescriptionResultDetails call() throws Exception {
						final String typeId = description.getTypeId();
						final String conceptId = description.getConceptId();
						final SnomedConcept conceptIndexEntry = conceptMap.get(conceptId);
						final SnomedBrowserDescriptionResultDetails details = new SnomedBrowserDescriptionResultDetails();
						
						if (conceptIndexEntry != null) {
							details.setActive(conceptIndexEntry.isActive());
							details.setConceptId(conceptIndexEntry.getId());
							details.setDefinitionStatus(conceptIndexEntry.getDefinitionStatus());
							details.setModuleId(conceptIndexEntry.getModuleId());
							
							if (resultConceptTermType == SnomedBrowserDescriptionType.FSN) {
								if (descriptionByConceptIdMap.containsKey(conceptId)) {
									details.setFsn(descriptionByConceptIdMap.get(conceptId).getTerm());
								} else {
									details.setFsn(conceptId);
								}
							} else {
								if (descriptionByConceptIdMap.containsKey(conceptId)) {
									details.setPreferredSynonym(descriptionByConceptIdMap.get(conceptId).getTerm());
								} else {
									details.setPreferredSynonym(conceptId);
								}
							}
							
						} else {
							LOGGER.warn("Concept {} not found in map, properties will not be set.", conceptId);
						}
						
						return details;
					}
				});
				
				descriptionResult.setConcept(details);
			} catch (ExecutionException e) {
				LOGGER.error("Exception thrown during computing details for concept {}, properties will not be set.", description.getConceptId(), e);
			}
			
			resultBuilder.add(descriptionResult);
		}

		return resultBuilder.build();
	}

	@Override
	public Map<String, ISnomedBrowserConstant> getConstants(final String branch, final List<ExtendedLocale> locales) {
		final ImmutableMap.Builder<String, ISnomedBrowserConstant> resultBuilder = ImmutableMap.builder();
		final DescriptionService descriptionService = new DescriptionService(bus, branch);
		
		for (final ConceptEnum conceptEnum : CONCEPT_ENUMS) {
			try {
				final String conceptId = conceptEnum.getConceptId();

				// Check if the corresponding concept exists
				SnomedRequests.prepareGetConcept(conceptId)
						.build(SnomedDatastoreActivator.REPOSITORY_UUID, branch)
						.execute(bus())
						.getSync();
				
				final SnomedBrowserConstant constant = new SnomedBrowserConstant();
				constant.setConceptId(conceptId);
				
				final SnomedDescription fullySpecifiedName = descriptionService.getFullySpecifiedName(conceptId, locales);
				if (fullySpecifiedName != null) {
					constant.setFsn(fullySpecifiedName.getTerm());
				} else {
					constant.setFsn(conceptId);
				}
				
				resultBuilder.put(conceptEnum.name(), constant);
			} catch (ComponentNotFoundException e) {
				// ignore
			}
		}
		
		return resultBuilder.build();
	}
}
