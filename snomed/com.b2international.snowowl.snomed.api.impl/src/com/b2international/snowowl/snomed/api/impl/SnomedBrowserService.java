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
import static com.google.common.collect.Sets.newHashSet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.b2international.snowowl.core.events.bulk.BulkRequest;
import com.b2international.snowowl.core.events.bulk.BulkRequestBuilder;
import com.b2international.snowowl.core.exceptions.ComponentNotFoundException;
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
import com.b2international.snowowl.snomed.core.domain.SnomedRelationships;
import com.b2international.snowowl.snomed.datastore.SnomedDatastoreActivator;
import com.b2international.snowowl.snomed.datastore.index.entry.SnomedDescriptionIndexEntry;
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
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

public class SnomedBrowserService implements ISnomedBrowserService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SnomedBrowserService.class);
	private static final Set<SnomedBrowserDescriptionType> SUPPORTED_DESCRIPTION_TYPES = ImmutableSet.of(SnomedBrowserDescriptionType.FSN, SnomedBrowserDescriptionType.SYNONYM); 
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

	@Resource
	private IEventBus bus;

	private IEventBus bus() {
		return Preconditions.checkNotNull(bus == null ? bus = ApplicationContext.getInstance().getServiceChecked(IEventBus.class) : bus,  "bus cannot be null!");
	}
	
	@Override
	public ISnomedBrowserConcept getConceptDetails(final IComponentRef conceptRef, final List<ExtendedLocale> locales) {

		final InternalComponentRef internalConceptRef = ClassUtils.checkAndCast(conceptRef, InternalComponentRef.class);
		internalConceptRef.checkStorageExists();
		
		
		final IBranchPath branchPath = internalConceptRef.getBranch().branchPath();
		final String conceptId = conceptRef.getComponentId();

		
		final SnomedConcept concept = SnomedRequests.prepareGetConcept(conceptId)
				.setLocales(locales)
				.setExpand("fsn(),pt(),inactivationProperties(),descriptions(limit:"+Integer.MAX_VALUE+",expand(inactivationProperties())),relationships(limit:"+Integer.MAX_VALUE+",expand(type(expand(fsn())),destination(expand(fsn()))))")
				.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath.getPath())
				.execute(bus())
				.getSync();
		
		final SnomedDescription fullySpecifiedName = concept.getFsn();
		final SnomedDescription preferredSynonym = concept.getPt();
		final SnomedDescriptions descriptions = concept.getDescriptions();
		final SnomedRelationships relationships = concept.getRelationships();

		final SnomedBrowserConcept result = convertConcept(concept);
		
		// inactivation fields
		result.setInactivationIndicator(concept.getInactivationIndicator());
		result.setAssociationTargets(concept.getAssociationTargets());
		
		populateLeafFields(branchPath.getPath(), concept.getId(), result);
		
		if (fullySpecifiedName != null) {
			result.setFsn(fullySpecifiedName.getTerm());
		} else {
			result.setFsn(result.getConceptId());
		}
		
		if (preferredSynonym != null) {
			result.setPreferredSynonym(preferredSynonym.getTerm());
		}

		result.setDescriptions(convertDescriptions(descriptions));
		result.setRelationships(convertRelationships(relationships, conceptRef, locales));
		
		return result;
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
		InputFactory inputFactory = new InputFactory(getBranch(branchPath));
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
		
		final IComponentRef componentRef = SnomedServiceHelper.createComponentRef(branchPath, createdConceptId);
		return getConceptDetails(componentRef, locales);
	}

	private Branch getBranch(String branchPath) {
		return RepositoryRequests.branching().prepareGet(branchPath).build(SnomedDatastoreActivator.REPOSITORY_UUID).execute(bus()).getSync();
	}
	
	private RepositoryCommitRequestBuilder createBulkCommit(String branch, List<? extends ISnomedBrowserConcept> updatedConcepts, String userId, List<ExtendedLocale> locales, final String commitComment) {
		final Stopwatch watch = Stopwatch.createStarted();
		
		final BulkRequestBuilder<TransactionContext> bulkRequest = BulkRequest.create();
		
		for (ISnomedBrowserConcept concept : updatedConcepts) {
			update(branch, concept, userId, locales, bulkRequest);
		}
		
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
	
	@Override
	public void update(String branch, List<? extends ISnomedBrowserConcept> updatedConcepts, String userId, List<ExtendedLocale> locales) {
		final String commitComment = userId + " Bulk update.";
		createBulkCommit(branch, updatedConcepts, userId, locales, commitComment)
			.build(SnomedDatastoreActivator.REPOSITORY_UUID, branch)
			.execute(bus())
			.getSync();
		
		LOGGER.info("Committed bulk concept changes on {}", branch);
	}
	
	@Override
	public ISnomedBrowserConcept update(String branchPath, ISnomedBrowserConcept newVersionConcept, String userId, List<ExtendedLocale> locales, BulkRequestBuilder<TransactionContext> bulkRequest) {
		LOGGER.info("Update concept start {}", newVersionConcept.getFsn());
		final IComponentRef componentRef = SnomedServiceHelper.createComponentRef(branchPath, newVersionConcept.getConceptId());

		final ISnomedBrowserConcept existingVersionConcept = getConceptDetails(componentRef, locales);

		InputFactory inputFactory = new InputFactory(getBranch(branchPath));
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

		// Inactivate concept last
		if (conceptUpdate != null && conceptInactivation) {
			bulkRequest.add(conceptUpdate);
		}

		// TODO - Add MRCM checks here
		return getConceptDetails(componentRef, locales);
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
	
	private List<ISnomedBrowserRelationship> convertRelationships(final Iterable<SnomedRelationship> relationships, final IComponentRef sourceConceptRef, final List<ExtendedLocale> locales) {
		final InternalComponentRef internalConceptRef = ClassUtils.checkAndCast(sourceConceptRef, InternalComponentRef.class);
		final IBranchPath branchPath = internalConceptRef.getBranch().branchPath();
		final DescriptionService descriptionService = new DescriptionService(bus, sourceConceptRef.getBranchPath());
		
		final ImmutableMap.Builder<String, ISnomedBrowserRelationship> convertedRelationshipBuilder = ImmutableMap.builder();

		
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
			convertedRelationshipBuilder.put(relationship.getId(), convertedRelationship);

		}
		
		final Map<String, ISnomedBrowserRelationship> convertedRelationships = convertedRelationshipBuilder.build();
		
		final List<SnomedBrowserRelationshipType> types = new FsnJoinerOperation<SnomedBrowserRelationshipType>(sourceConceptRef.getComponentId(), locales, descriptionService, SnomedBrowserDescriptionType.FSN) {
			
			@Override
			protected Iterable<SnomedConcept> getConceptEntries(String conceptId) {
				final Set<String> typeIds = newHashSet();
				for (final SnomedRelationship relationship : relationships) {
					typeIds.add(relationship.getTypeId());
				}
				return getConcepts(branchPath, typeIds);
			}

			@Override
			protected SnomedBrowserRelationshipType convertConceptEntry(SnomedConcept conceptEntry, Optional<SnomedDescription> descriptionOptional) {
				final SnomedBrowserRelationshipType type = new SnomedBrowserRelationshipType();
				type.setConceptId(conceptEntry.getId());
				type.setFsn(descriptionOptional.transform(description -> description.getTerm()).or(conceptEntry.getId()));
				return type;
			}
		}.run();
		
		final Map<String, SnomedBrowserRelationshipType> typesById = Maps.uniqueIndex(types, new Function<SnomedBrowserRelationshipType, String>() {
			@Override
			public String apply(SnomedBrowserRelationshipType input) {
				return input.getConceptId();
			}
		});
		
		final List<SnomedBrowserRelationshipTarget> targets = new FsnJoinerOperation<SnomedBrowserRelationshipTarget>(sourceConceptRef.getComponentId(), locales, descriptionService, SnomedBrowserDescriptionType.FSN) {
			
			@Override
			protected Iterable<SnomedConcept> getConceptEntries(String conceptId) {
				final Set<String> destinationConceptIds = newHashSet();
				for (final SnomedRelationship relationship : relationships) {
					destinationConceptIds.add(relationship.getDestinationId());
				}
				return getConcepts(branchPath, destinationConceptIds);
			}

			@Override
			protected SnomedBrowserRelationshipTarget convertConceptEntry(SnomedConcept destinationConcept, Optional<SnomedDescription> descriptionOptional) {
				final SnomedBrowserRelationshipTarget target = new SnomedBrowserRelationshipTarget();
				target.setActive(destinationConcept.isActive());
				target.setConceptId(destinationConcept.getId());
				target.setDefinitionStatus(destinationConcept.getDefinitionStatus());
				target.setEffectiveTime(destinationConcept.getEffectiveTime());
				target.setModuleId(destinationConcept.getModuleId());
				target.setFsn(descriptionOptional.transform(description -> description.getTerm()).or(destinationConcept.getId()));
				return target;
			}
		}.run();
		
		final Map<String, SnomedBrowserRelationshipTarget> targetsById = Maps.uniqueIndex(targets, new Function<SnomedBrowserRelationshipTarget, String>() {
			@Override
			public String apply(SnomedBrowserRelationshipTarget input) {
				return input.getConceptId();
			}
		});
		
		for (SnomedRelationship entry : relationships) {
			SnomedBrowserRelationship rel = (SnomedBrowserRelationship) convertedRelationships.get(entry.getId());
			SnomedBrowserRelationshipType type = typesById.get(entry.getTypeId());
			SnomedBrowserRelationshipTarget target = targetsById.get(entry.getDestinationId());
			rel.setType(type);
			rel.setTarget(target);
		}
		
		return ImmutableList.copyOf(convertedRelationships.values());
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
	public List<ISnomedBrowserChildConcept> getConceptChildren(IComponentRef conceptRef, List<ExtendedLocale> locales, boolean stated) {
		return getConceptChildren(conceptRef, locales, stated, SnomedBrowserDescriptionType.FSN);
	}
	
	@Override
	public List<ISnomedBrowserChildConcept> getConceptChildren(final IComponentRef conceptRef, final List<ExtendedLocale> locales, final boolean stated, final SnomedBrowserDescriptionType preferredDescriptionType) {
		final InternalComponentRef internalConceptRef = ClassUtils.checkAndCast(conceptRef, InternalComponentRef.class);
		final String branch = internalConceptRef.getBranch().path();
		final DescriptionService descriptionService = new DescriptionService(bus, conceptRef.getBranchPath());

		return new FsnJoinerOperation<ISnomedBrowserChildConcept>(conceptRef.getComponentId(), locales, descriptionService, preferredDescriptionType) {
			
			@Override
			protected Iterable<SnomedConcept> getConceptEntries(String conceptId) {
				return SnomedRequests.prepareSearchConcept()
						.all()
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
			.sortBy(SearchResourceRequest.SCORE, new SearchResourceRequest.SortField(SnomedDescriptionIndexEntry.Fields.TERM, true))
			.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath.getPath())
			.execute(bus())
			.getSync()
			.getItems();

		final Set<String> conceptIds = FluentIterable.from(descriptions)
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
		
		for (final SnomedDescription description : descriptions) {
			
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
