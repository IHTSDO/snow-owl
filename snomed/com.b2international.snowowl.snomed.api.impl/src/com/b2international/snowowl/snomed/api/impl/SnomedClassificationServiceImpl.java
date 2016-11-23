/*
 * Copyright 2011-2015 B2i Healthcare Pte Ltd, http://b2i.sg
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.b2international.snowowl.snomed.api.impl;

import static com.google.common.collect.Sets.newHashSet;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.b2international.commons.http.ExtendedLocale;
import com.b2international.snowowl.core.ApplicationContext;
import com.b2international.snowowl.core.SnowOwlApplication;
import com.b2international.snowowl.core.branch.Branch;
import com.b2international.snowowl.core.domain.TransactionContext;
import com.b2international.snowowl.core.events.bulk.BulkRequest;
import com.b2international.snowowl.core.events.bulk.BulkRequestBuilder;
import com.b2international.snowowl.core.events.util.Promise;
import com.b2international.snowowl.core.exceptions.BadRequestException;
import com.b2international.snowowl.core.exceptions.ConflictException;
import com.b2international.snowowl.datastore.BranchPathUtils;
import com.b2international.snowowl.datastore.oplock.IOperationLockTarget;
import com.b2international.snowowl.datastore.oplock.OperationLockException;
import com.b2international.snowowl.datastore.oplock.impl.DatastoreLockContext;
import com.b2international.snowowl.datastore.oplock.impl.DatastoreLockContextDescriptions;
import com.b2international.snowowl.datastore.oplock.impl.DatastoreOperationLockException;
import com.b2international.snowowl.datastore.oplock.impl.IDatastoreOperationLockManager;
import com.b2international.snowowl.datastore.oplock.impl.SingleRepositoryAndBranchLockTarget;
import com.b2international.snowowl.datastore.remotejobs.AbstractRemoteJobEvent;
import com.b2international.snowowl.datastore.remotejobs.IRemoteJobManager;
import com.b2international.snowowl.datastore.remotejobs.RemoteJobChangedEvent;
import com.b2international.snowowl.datastore.remotejobs.RemoteJobEntry;
import com.b2international.snowowl.datastore.remotejobs.RemoteJobEventBusHandler;
import com.b2international.snowowl.datastore.remotejobs.RemoteJobEventSwitch;
import com.b2international.snowowl.datastore.remotejobs.RemoteJobState;
import com.b2international.snowowl.datastore.remotejobs.RemoteJobUtils;
import com.b2international.snowowl.datastore.request.CommitInfo;
import com.b2international.snowowl.datastore.request.DeleteRequestBuilder;
import com.b2international.snowowl.datastore.server.index.SingleDirectoryIndexManager;
import com.b2international.snowowl.eventbus.IEventBus;
import com.b2international.snowowl.eventbus.IHandler;
import com.b2international.snowowl.eventbus.IMessage;
import com.b2international.snowowl.snomed.api.ISnomedClassificationService;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserConcept;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserRelationship;
import com.b2international.snowowl.snomed.api.domain.classification.ClassificationStatus;
import com.b2international.snowowl.snomed.api.domain.classification.IClassificationRun;
import com.b2international.snowowl.snomed.api.domain.classification.IEquivalentConcept;
import com.b2international.snowowl.snomed.api.domain.classification.IEquivalentConceptSet;
import com.b2international.snowowl.snomed.api.domain.classification.IRelationshipChange;
import com.b2international.snowowl.snomed.api.domain.classification.IRelationshipChangeList;
import com.b2international.snowowl.snomed.api.impl.domain.browser.SnomedBrowserConcept;
import com.b2international.snowowl.snomed.api.impl.domain.browser.SnomedBrowserRelationship;
import com.b2international.snowowl.snomed.api.impl.domain.browser.SnomedBrowserRelationshipTarget;
import com.b2international.snowowl.snomed.api.impl.domain.browser.SnomedBrowserRelationshipType;
import com.b2international.snowowl.snomed.api.impl.domain.classification.ClassificationRun;
import com.b2international.snowowl.snomed.api.impl.domain.classification.EquivalentConcept;
import com.b2international.snowowl.snomed.common.SnomedRf2Headers;
import com.b2international.snowowl.snomed.core.domain.BranchMetadataResolver;
import com.b2international.snowowl.snomed.core.domain.CharacteristicType;
import com.b2international.snowowl.snomed.core.domain.ISnomedConcept;
import com.b2international.snowowl.snomed.core.domain.ISnomedDescription;
import com.b2international.snowowl.snomed.core.domain.ISnomedRelationship;
import com.b2international.snowowl.snomed.core.domain.SnomedConcepts;
import com.b2international.snowowl.snomed.core.domain.SnomedRelationships;
import com.b2international.snowowl.snomed.core.domain.refset.SnomedReferenceSetMember;
import com.b2international.snowowl.snomed.core.domain.refset.SnomedReferenceSetMembers;
import com.b2international.snowowl.snomed.datastore.SnomedDatastoreActivator;
import com.b2international.snowowl.snomed.datastore.config.SnomedCoreConfiguration;
import com.b2international.snowowl.snomed.datastore.id.SnomedIdentifiers;
import com.b2international.snowowl.snomed.datastore.request.SnomedRefSetMemberUpdateRequestBuilder;
import com.b2international.snowowl.snomed.datastore.request.SnomedRelationshipCreateRequestBuilder;
import com.b2international.snowowl.snomed.datastore.request.SnomedRelationshipUpdateRequestBuilder;
import com.b2international.snowowl.snomed.datastore.request.SnomedRequests;
import com.b2international.snowowl.snomed.reasoner.classification.AbstractResponse.Type;
import com.b2international.snowowl.snomed.reasoner.classification.entry.AbstractChangeEntry.Nature;
import com.b2international.snowowl.snomed.reasoner.classification.ClassificationRequest;
import com.b2international.snowowl.snomed.reasoner.classification.GetResultResponse;
import com.b2international.snowowl.snomed.reasoner.classification.SnomedReasonerService;
import com.google.common.base.Function;
import com.google.common.base.Stopwatch;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;

/**
 */
public class SnomedClassificationServiceImpl implements ISnomedClassificationService {

	private static final Logger LOG = LoggerFactory.getLogger(SnomedClassificationServiceImpl.class);

	private static final int MAX_INDEXED_RESULTS = 1000;
	private static final int RELATIONSHIP_BLOCK_SIZE = 100;

	private static final long BRANCH_READ_TIMEOUT = 5000L;
	private static final long BRANCH_LOCK_TIMEOUT = 500L;
	
	private final class PersistChangesRunnable implements Runnable {
		private final String branchPath;
		private final String classificationId;
		private final String userId;

		private PersistChangesRunnable(final String branchPath, final String classificationId, final String userId) {
			this.branchPath = branchPath;
			this.classificationId = classificationId;
			this.userId = userId;
		}

		@Override
		public void run() {
			final DatastoreLockContext context = new DatastoreLockContext(userId, DatastoreLockContextDescriptions.CLASSIFY_WITH_REVIEW);
			final IOperationLockTarget target = new SingleRepositoryAndBranchLockTarget(SnomedDatastoreActivator.REPOSITORY_UUID, BranchPathUtils.createPath(branchPath));
			lockBranch(branchPath, context, target);
				
			final Branch branch = getBranchIfExists(branchPath);
			final IClassificationRun classificationRun = getClassificationRun(branchPath, classificationId);
				
			if (!ClassificationStatus.COMPLETED.equals(classificationRun.getStatus())) {
				unlockBranch(branchPath, context, target);
				return;
			}
			
			final UUID uuid = UUID.fromString(classificationId);
			if (branch.headTimestamp() > classificationRun.getLastCommitDate().getTime()) {
				try {
					updateStatus(uuid, ClassificationStatus.STALE);
					return;
				} finally {
					unlockBranch(branchPath, context, target);
				}
				
			} else {
				updateStatus(uuid, ClassificationStatus.SAVING_IN_PROGRESS);
			}

			try {

				final Stopwatch persistStopwatch = Stopwatch.createStarted();
				final BulkRequestBuilder<TransactionContext> builder = BulkRequest.create();
				final String defaultModuleId = BranchMetadataResolver.getEffectiveBranchMetadataValue(branch, SnomedCoreConfiguration.BRANCH_DEFAULT_MODULE_ID_KEY);
				final String defaultNamespace = BranchMetadataResolver.getEffectiveBranchMetadataValue(branch, SnomedCoreConfiguration.BRANCH_DEFAULT_REASONER_NAMESPACE_KEY);
				final Map<String, String> moduleMap = Maps.newHashMap();
				
				int offset = 0;
				IRelationshipChangeList relationshipChanges = getRelationshipChanges(branchPath, classificationId, offset, RELATIONSHIP_BLOCK_SIZE);
				
				while (offset < relationshipChanges.getTotal()) {
					final Set<String> sourceIds = getInferredSourceIds(relationshipChanges);
					final Set<String> removeOrDeactivateIds = Sets.newHashSet();

					sourceIds.removeAll(moduleMap.keySet());
					populateModuleMap(branchPath, sourceIds, moduleMap);
					
					for (IRelationshipChange change : relationshipChanges.getChanges()) {
						
						switch (change.getChangeNature()) {
							case INFERRED:
								final SnomedRelationshipCreateRequestBuilder inferredRelationshipBuilder = createInferredRelationship(change, 
										moduleMap, 
										defaultModuleId, 
										defaultNamespace);
								
								builder.add(inferredRelationshipBuilder);
								break;

							case REDUNDANT:
								removeOrDeactivateIds.add(change.getId());
								break;
								
							default:
								throw new IllegalStateException("Unhandled relationship change value '" + change.getChangeNature() + "'.");
						}
					}

					if (!removeOrDeactivateIds.isEmpty()) {
						
						// TODO: only remove/inactivate components in the current module?
						final SnomedRelationships removeOrDeactivateRelationships = SnomedRequests.prepareSearchRelationship()
								.setComponentIds(removeOrDeactivateIds)
								.setLimit(removeOrDeactivateIds.size())
								.build(branchPath)
								.execute(bus)
								.getSync();
	
						final SnomedReferenceSetMembers referringMembers = SnomedRequests.prepareSearchMember()
								.all()
								.filterByActive(true)
								.filterByReferencedComponent(removeOrDeactivateIds)
								.build(branchPath)
								.execute(bus)
								.getSync();
						
						removeOrDeactivate(builder, removeOrDeactivateRelationships, referringMembers);
					}
					
					offset += relationshipChanges.getChanges().size();
					relationshipChanges = getRelationshipChanges(branchPath, classificationId, offset, RELATIONSHIP_BLOCK_SIZE);
				}
				
				commitChanges(branchPath, userId, builder, persistStopwatch)
						.then(new Function<CommitInfo, Void>() { @Override public Void apply(final CommitInfo input) { return updateStatus(uuid, ClassificationStatus.SAVED); }})
						.fail(new Function<Throwable, Void>() { @Override public Void apply(final Throwable input) {
							LOG.error("Failed to save classification changes on branch {}.", branchPath, input);
							return updateStatus(uuid, ClassificationStatus.SAVE_FAILED); 
						}})
						.getSync();
			
			} finally {
				unlockBranch(branchPath, context, target);
			}
		}

		private Set<String> getInferredSourceIds(final IRelationshipChangeList relationshipChanges) {
			final Set<String> sourceIds = Sets.newHashSet();
			for (final IRelationshipChange change : relationshipChanges.getChanges()) {
				if (Nature.INFERRED.equals(change.getChangeNature())) {
					sourceIds.add(change.getSourceId());
				}
			}
			return sourceIds;
		}

		private void populateModuleMap(final String branchPath, final Set<String> conceptIds, final Map<String, String> moduleMap) {
			SnomedRequests.prepareSearchConcept()
					.setComponentIds(conceptIds)
					.setLimit(conceptIds.size())
					.build(branchPath)
					.execute(bus)
					.then(new Function<SnomedConcepts, Void>() {
						@Override
						public Void apply(SnomedConcepts input) {
							for (ISnomedConcept concept : input) {
								moduleMap.put(concept.getId(), concept.getModuleId());
							}
							return null;
						}
					})
					.getSync();
		}

		private SnomedRelationshipCreateRequestBuilder createInferredRelationship(IRelationshipChange relationshipChange,
				final Map<String, String> moduleMap, 
				final String defaultModuleId,
				final String defaultNamespace) {
		
			// Use module and/or namespace from source concept, if not given
			final String moduleId = (defaultModuleId != null) 
					? defaultModuleId
					: moduleMap.get(relationshipChange.getSourceId());
			
			final String namespace = (defaultNamespace != null) 
					? defaultNamespace 
					: SnomedIdentifiers.create(relationshipChange.getSourceId()).getNamespace();
			
			final SnomedRelationshipCreateRequestBuilder inferredRelationshipBuilder = SnomedRequests.prepareNewRelationship()
					.setActive(true)
					.setCharacteristicType(CharacteristicType.INFERRED_RELATIONSHIP)
					.setDestinationId(relationshipChange.getDestinationId())
					.setDestinationNegated(false)
					.setGroup(relationshipChange.getGroup())
					.setModifier(relationshipChange.getModifier())
					.setSourceId(relationshipChange.getSourceId())
					.setTypeId(relationshipChange.getTypeId())
					.setUnionGroup(relationshipChange.getUnionGroup())
					.setModuleId(moduleId)
					.setIdFromNamespace(namespace);
			
			return inferredRelationshipBuilder;
		}

		private Promise<CommitInfo> commitChanges(final String branchPath, 
				final String userId, 
				final BulkRequestBuilder<TransactionContext> builder,
				final Stopwatch persistStopwatch) {
			
			return SnomedRequests.prepareCommit()
					.setUserId(userId)
					.setCommitComment("Classified ontology.") // Same message in PersistChangesRemoteJob
					.setPreparationTime(persistStopwatch.elapsed(TimeUnit.MILLISECONDS))
					.setParentLockContextDescription(DatastoreLockContextDescriptions.CLASSIFY_WITH_REVIEW)
					.setBody(builder)
					.setBranch(branchPath)
					.build()
					.execute(bus);
		}

		private void removeOrDeactivate(final BulkRequestBuilder<TransactionContext> builder,
				final SnomedRelationships removeOrDeactivateRelationships,
				final SnomedReferenceSetMembers referringMembers) {
			
			final Multimap<String, SnomedReferenceSetMember> referringMembersById = Multimaps.index(referringMembers, new Function<SnomedReferenceSetMember, String>() {
				@Override public String apply(SnomedReferenceSetMember input) { return input.getReferencedComponent().getId(); }
			});

			for (ISnomedRelationship relationships : removeOrDeactivateRelationships) {
				
				if (relationships.isReleased()) {
					
					for (SnomedReferenceSetMember snomedReferenceSetMember : referringMembersById.get(relationships.getId())) {
						SnomedRefSetMemberUpdateRequestBuilder updateMemberBuilder = SnomedRequests.prepareUpdateMember()
								.setMemberId(snomedReferenceSetMember.getId())
								.setSource(ImmutableMap.<String, Object>of(SnomedRf2Headers.FIELD_ACTIVE, Boolean.FALSE));
						
						builder.add(updateMemberBuilder);
					}
					
					SnomedRelationshipUpdateRequestBuilder updateRequestBuilder = SnomedRequests.prepareUpdateRelationship(relationships.getId())
							.setActive(false);

					builder.add(updateRequestBuilder);
				} else {
					
					for (SnomedReferenceSetMember snomedReferenceSetMember : referringMembersById.get(relationships.getId())) {
						DeleteRequestBuilder deleteMemberBuilder = SnomedRequests.prepareDeleteMember()
								.setComponentId(snomedReferenceSetMember.getId());
						
						builder.add(deleteMemberBuilder);
					}
					
					DeleteRequestBuilder deleteRelationshipBuilder = SnomedRequests.prepareDeleteRelationship()
							.setComponentId(relationships.getId());
					
					builder.add(deleteRelationshipBuilder);
				}
			}
		}
	}

	private final class RemoteJobChangeHandler implements IHandler<IMessage> {
		@Override
		public void handle(final IMessage message) {
			new RemoteJobEventSwitch() {

				@Override
				protected void caseChanged(final RemoteJobChangedEvent event) {

					try {

						if (RemoteJobEntry.PROP_STATE.equals(event.getPropertyName())) {
							final RemoteJobState newState = (RemoteJobState) event.getNewValue();
							final UUID id = event.getId();

							switch (newState) {
								case CANCEL_REQUESTED:
									// Nothing to do
									break;
								case FAILED:
									indexService.updateClassificationRunStatus(id, ClassificationStatus.FAILED);
									break;
								case FINISHED: 
									// Handled in RemoteJobCompletionHandler
									break;
								case RUNNING:
									indexService.updateClassificationRunStatus(id, ClassificationStatus.RUNNING);
									break;
								case SCHEDULED:
									// Nothing to do
									break;
								default:
									throw new IllegalStateException(MessageFormat.format("Unexpected remote job state ''{0}''.", newState));
							}
						}

					} catch (final IOException e) {
						LOG.error("Caught IOException while updating classification status.", e);
					}
				}

			}.doSwitch(message.body(AbstractRemoteJobEvent.class));
		}
	}

	private final class RemoteJobCompletionHandler extends RemoteJobEventBusHandler {

		public RemoteJobCompletionHandler(final UUID remoteJobId) {
			super(remoteJobId);
		}

		@Override
		protected void handleResult(final UUID remoteJobId, final boolean cancelRequested) {
			executorService.submit(new Runnable() {
				@Override
				public void run() {
					try {
						
						if (cancelRequested) {
							indexService.updateClassificationRunStatus(remoteJobId, ClassificationStatus.CANCELED);
							return;
						} 

						final GetResultResponse result = getReasonerService().getResult(remoteJobId);
						final Type responseType = result.getType();

						switch (responseType) {
							case NOT_AVAILABLE: 
								indexService.updateClassificationRunStatus(remoteJobId, ClassificationStatus.FAILED);
								break;
							case STALE: 
								indexService.updateClassificationRunStatus(remoteJobId, ClassificationStatus.STALE, result.getChanges());
								break;
							case SUCCESS:
								indexService.updateClassificationRunStatus(remoteJobId, ClassificationStatus.COMPLETED, result.getChanges());
								break;
							default:
								throw new IllegalStateException(MessageFormat.format("Unexpected response type ''{0}''.", responseType));
						}
						
						// Remove reasoner taxonomy immediately after processing it
						getReasonerService().removeResult(remoteJobId);

					} catch (final IOException e) {
						LOG.error("Caught IOException while registering classification data.", e);
					}
				}
			});
		}
	}

	private ClassificationRunIndex indexService;
	private RemoteJobChangeHandler changeHandler;
	private ExecutorService executorService;

	@Resource
	private SnomedBrowserService browserService;
	
	@Resource
	private IEventBus bus;
	
	@Resource
	private int maxReasonerRuns;

	@PostConstruct
	protected void init() {
		LOG.info("Initializing classification service; keeping indexed data for {} recent run(s).", maxReasonerRuns); 
		
		final File dir = new File(new File(SnowOwlApplication.INSTANCE.getEnviroment().getDataDirectory(), "indexes"), "classification_runs");
		indexService = new ClassificationRunIndex(dir);
		ApplicationContext.getInstance().getServiceChecked(SingleDirectoryIndexManager.class).registerIndex(indexService);
		
		try {
			indexService.trimIndex(maxReasonerRuns);
			indexService.invalidateClassificationRuns();
		} catch (final IOException e) {
			LOG.error("Failed to run housekeeping tasks for the classification index.", e);
		}

		try {
			indexService.trimIndex(MAX_INDEXED_RESULTS);
			indexService.invalidateClassificationRuns();
		} catch (final IOException e) {
			LOG.error("Failed to run housekeeping tasks for the classification index.", e);
		}

		// TODO: common ExecutorService for asynchronous work?
		executorService = Executors.newCachedThreadPool(); 
		changeHandler = new RemoteJobChangeHandler();
		bus.registerHandler(IRemoteJobManager.ADDRESS_REMOTE_JOB_CHANGED, changeHandler);
	}

	@PreDestroy
	protected void shutdown() {
		bus.unregisterHandler(IRemoteJobManager.ADDRESS_REMOTE_JOB_CHANGED, changeHandler);
		changeHandler = null;

		if (null != executorService) {
			executorService.shutdown();
			executorService = null;
		}
		
		if (null != indexService) {
			ApplicationContext.getInstance().getServiceChecked(SingleDirectoryIndexManager.class).unregisterIndex(indexService);
			indexService.dispose();
			indexService = null;
		}
		
		LOG.info("Classification service shut down.");
	}

	private static SnomedReasonerService getReasonerService() {
		return ApplicationContext.getServiceForClass(SnomedReasonerService.class);
	}

	private static IRemoteJobManager getRemoteJobManager() {
		return ApplicationContext.getServiceForClass(IRemoteJobManager.class);
	}

	private static IDatastoreOperationLockManager getLockManager() {
		return ApplicationContext.getServiceForClass(IDatastoreOperationLockManager.class);
	}

	@Override
	public List<IClassificationRun> getAllClassificationRuns(final String branchPath) {
		getBranchIfExists(branchPath);
		
		try {
			return indexService.getAllClassificationRuns(branchPath);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public IClassificationRun beginClassification(final String branchPath, final String reasonerId, final String userId) {
		
		final Branch branch = getBranchIfExists(branchPath);
		final ClassificationRequest classificationRequest = new ClassificationRequest(userId, branch.branchPath())
				.withParentContextDescription(DatastoreLockContextDescriptions.ROOT)
				.withReasonerId(reasonerId);

		final UUID remoteJobId = classificationRequest.getClassificationId();
		final RemoteJobCompletionHandler completionHandler = new RemoteJobCompletionHandler(remoteJobId);
		bus.registerHandler(RemoteJobUtils.getJobSpecificAddress(IRemoteJobManager.ADDRESS_REMOTE_JOB_COMPLETED, remoteJobId), completionHandler);

		final ClassificationRun classificationRun = new ClassificationRun();
		classificationRun.setId(remoteJobId.toString());
		classificationRun.setReasonerId(reasonerId);
		classificationRun.setLastCommitDate(new Date(branch.headTimestamp()));
		classificationRun.setCreationDate(new Date());
		classificationRun.setUserId(userId);
		classificationRun.setStatus(ClassificationStatus.SCHEDULED);

		try {
			indexService.upsertClassificationRun(branch.branchPath(), classificationRun);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}

		getReasonerService().beginClassification(classificationRequest);				
		return classificationRun;
	}

	private Branch getBranchIfExists(final String branchPath) {
		final Branch branch = SnomedRequests.branching()
				.prepareGet(branchPath)
				.execute(bus)
				.getSync(BRANCH_READ_TIMEOUT, TimeUnit.MILLISECONDS);
		
		if (branch.isDeleted()) {
			throw new BadRequestException("Branch '%s' has been deleted and cannot accept further modifications.", branchPath);
		} else {
			return branch;
		}
	}

	@Override
	public IClassificationRun getClassificationRun(final String branchPath, final String classificationId) {
		getBranchIfExists(branchPath);

		try {
			return indexService.getClassificationRun(branchPath, classificationId);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<IEquivalentConceptSet> getEquivalentConceptSets(final String branchPath, final String classificationId, final List<ExtendedLocale> locales) {
		getClassificationRun(branchPath, classificationId);

		try {
			final List<IEquivalentConceptSet> conceptSets = indexService.getEquivalentConceptSets(branchPath, classificationId);
			final Set<String> conceptIds = newHashSet();
			
			for (final IEquivalentConceptSet conceptSet : conceptSets) {
				for (final IEquivalentConcept equivalentConcept : conceptSet.getEquivalentConcepts()) {
					conceptIds.add(equivalentConcept.getId());
				}
			}

			final Map<String, ISnomedDescription> fsnMap = new DescriptionService(bus, branchPath).getFullySpecifiedNames(conceptIds, locales);
			for (final IEquivalentConceptSet conceptSet : conceptSets) {
				for (final IEquivalentConcept equivalentConcept : conceptSet.getEquivalentConcepts()) {
					final String equivalentConceptId = equivalentConcept.getId();
					final ISnomedDescription fsn = fsnMap.get(equivalentConceptId);
					if (fsn != null) {
						((EquivalentConcept) equivalentConcept).setLabel(fsn.getTerm());
					} else {
						((EquivalentConcept) equivalentConcept).setLabel(equivalentConceptId);
					}
				}
			}
			
			return conceptSets;
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public IRelationshipChangeList getRelationshipChanges(final String branchPath, final String classificationId, final int offset, final int limit) {
		return getRelationshipChanges(branchPath, classificationId, null, offset, limit);
	}

	private IRelationshipChangeList getRelationshipChanges(String branchPath, String classificationId, String conceptId, int offset, int limit) {
		getClassificationRun(branchPath, classificationId);

		try {
			return indexService.getRelationshipChanges(branchPath, classificationId, conceptId, offset, limit);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public ISnomedBrowserConcept getConceptPreview(String branchPath, String classificationId, String conceptId, List<ExtendedLocale> locales) {
		final SnomedBrowserConcept conceptDetails = (SnomedBrowserConcept) browserService.getConceptDetails(branchPath, conceptId, locales);

		final List<ISnomedBrowserRelationship> relationships = Lists.newArrayList(conceptDetails.getRelationships());
		final IRelationshipChangeList relationshipChanges = getRelationshipChanges(branchPath, classificationId, conceptId, 0, 10000);

		/* 
		 * XXX: We don't want to match anything that is part of the inferred set below, so we remove relationships from the existing list, 
		 * all in advance. (Revisit should this assumption prove to be incorrect.)
		 */
		for (IRelationshipChange relationshipChange : relationshipChanges.getChanges()) {
			switch (relationshipChange.getChangeNature()) {
				case REDUNDANT:
					relationships.remove(findRelationship(relationships, relationshipChange));
					break;
				default:
					break;
			}
		}
		
		// Collect all concept representations that will be required for the conversion
		final Set<String> relatedIds = Sets.newHashSet();
		for (IRelationshipChange relationshipChange : relationshipChanges.getChanges()) {
			switch (relationshipChange.getChangeNature()) {
				case INFERRED:
					relatedIds.add(relationshipChange.getDestinationId());
					relatedIds.add(relationshipChange.getTypeId());
					break;
				default:
					break;
			}
		}
		
		final SnomedConcepts relatedConcepts = SnomedRequests.prepareSearchConcept()
				.setLimit(relatedIds.size())
				.setComponentIds(relatedIds)
				.setLocales(locales)
				.setExpand("fsn()")
				.build(branchPath)
				.executeSync(bus);
		
		final Map<String, ISnomedConcept> relatedConceptsById = Maps.uniqueIndex(relatedConcepts, new Function<ISnomedConcept, String>() {
			@Override public String apply(ISnomedConcept input) { return input.getId(); }
		});
		
		final LoadingCache<ISnomedConcept, SnomedBrowserRelationshipType> types = CacheBuilder.newBuilder().build(new CacheLoader<ISnomedConcept, SnomedBrowserRelationshipType>() {
			@Override
			public SnomedBrowserRelationshipType load(ISnomedConcept key) throws Exception {
				return browserService.convertBrowserRelationshipType(key);
			}
		});
		
		final LoadingCache<ISnomedConcept, SnomedBrowserRelationshipTarget> targets = CacheBuilder.newBuilder().build(new CacheLoader<ISnomedConcept, SnomedBrowserRelationshipTarget>() {
			@Override
			public SnomedBrowserRelationshipTarget load(ISnomedConcept key) throws Exception {
				return browserService.convertBrowserRelationshipTarget(key);
			}
		});
		
		for (IRelationshipChange relationshipChange : relationshipChanges.getChanges()) {
			switch (relationshipChange.getChangeNature()) {
				case INFERRED:
					final SnomedBrowserRelationship inferred = new SnomedBrowserRelationship();
					
					// XXX: Default and/or not populated values are shown as commented lines below
					inferred.setActive(true);
					inferred.setCharacteristicType(CharacteristicType.INFERRED_RELATIONSHIP);
					// inferred.setEffectiveTime(null);
					inferred.setGroupId(relationshipChange.getGroup());
					inferred.setModifier(relationshipChange.getModifier());
					// inferred.setModuleId(null);
					// inferred.setRelationshipId(null);
					// inferred.setReleased(false);
					inferred.setSourceId(relationshipChange.getSourceId());
				
					ISnomedConcept destinationConcept = relatedConceptsById.get(relationshipChange.getDestinationId());
					ISnomedConcept typeConcept = relatedConceptsById.get(relationshipChange.getTypeId());
					inferred.setTarget(targets.getUnchecked(destinationConcept));
					inferred.setType(types.getUnchecked(typeConcept));

					relationships.add(inferred);
					break;
				default:
					break;
			}
		}
		
		// Replace immutable relationship list with preview
		conceptDetails.setRelationships(relationships);
		return conceptDetails;
	}

	private ISnomedBrowserRelationship findRelationship(List<ISnomedBrowserRelationship> relationships, IRelationshipChange relationshipChange) {
		for (ISnomedBrowserRelationship relationship : relationships) {
			if (relationship.isActive()
					&& relationship.getSourceId().equals(relationshipChange.getSourceId())
					&& relationship.getType().getConceptId().equals(relationshipChange.getTypeId())
					&& relationship.getTarget().getConceptId().equals(relationshipChange.getDestinationId())
					&& relationship.getGroupId() == relationshipChange.getGroup()
					&& relationship.getCharacteristicType().equals(CharacteristicType.INFERRED_RELATIONSHIP)
					&& relationship.getModifier().equals(relationshipChange.getModifier())) {					
				return relationship;
			}
		}
		return null;
	}

	@Override
	public void persistChanges(final String branchPath, final String classificationId, final String userId) {
		final IClassificationRun classificationRun = getClassificationRun(branchPath, classificationId);

		if (ClassificationStatus.COMPLETED.equals(classificationRun.getStatus())) {
			executorService.submit(new PersistChangesRunnable(branchPath, classificationId, userId));
		}
	}

	private void lockBranch(final String branchPath, final DatastoreLockContext context, final IOperationLockTarget target) {
		
		try {
			getLockManager().lock(context, BRANCH_LOCK_TIMEOUT, target);
		} catch (DatastoreOperationLockException e) {
			final DatastoreLockContext otherContext = e.getContext(target);
			throw new ConflictException("Failed to acquire lock for branch %s because %s is %s.", branchPath, otherContext.getUserId(), otherContext.getDescription());
		} catch (OperationLockException e) {
			throw new ConflictException("Failed to acquire lock for branch %s.", branchPath);
		} catch (InterruptedException e) {
			throw new ConflictException("Interrupted while acquiring lock for branch %s.", branchPath);
		}
	}
	
	private void unlockBranch(final String branchPath, final DatastoreLockContext context, final IOperationLockTarget target) {
		
		try {
			getLockManager().unlock(context, target);
		} catch (OperationLockException e) {
			LOG.warn("Failed to release lock for branch {}.", branchPath, e);
		}
	}

	private Void updateStatus(final UUID uuid, final ClassificationStatus status) {
		try {
			indexService.updateClassificationRunStatus(uuid, status);
			return null;
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void removeClassificationRun(final String branchPath, final String classificationId) {
		// Check if it exists
		getClassificationRun(branchPath, classificationId);
		getRemoteJobManager().cancelRemoteJob(UUID.fromString(classificationId));
		
		try {
			indexService.deleteClassificationData(classificationId);
		} catch (final IOException e) {
			LOG.error("Caught IOException while deleting classification data for ID {}.", classificationId, e);
		}					
	}
}
