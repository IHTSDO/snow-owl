/*
 * Copyright 2011-2017 B2i Healthcare Pte Ltd, http://b2i.sg
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
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import com.b2international.snowowl.core.config.SnowOwlConfiguration;
import com.b2international.snowowl.core.domain.IComponentRef;
import com.b2international.snowowl.core.domain.TransactionContext;
import com.b2international.snowowl.core.events.Notifications;
import com.b2international.snowowl.core.events.bulk.BulkRequest;
import com.b2international.snowowl.core.events.bulk.BulkRequestBuilder;
import com.b2international.snowowl.core.events.util.Promise;
import com.b2international.snowowl.core.exceptions.BadRequestException;
import com.b2international.snowowl.core.exceptions.ConflictException;
import com.b2international.snowowl.datastore.BranchPathUtils;
import com.b2international.snowowl.datastore.oplock.IOperationLockTarget;
import com.b2international.snowowl.datastore.oplock.OperationLockException;
import com.b2international.snowowl.datastore.oplock.OperationLockRunner;
import com.b2international.snowowl.datastore.oplock.impl.DatastoreLockContext;
import com.b2international.snowowl.datastore.oplock.impl.DatastoreLockContextDescriptions;
import com.b2international.snowowl.datastore.oplock.impl.DatastoreOperationLockException;
import com.b2international.snowowl.datastore.oplock.impl.IDatastoreOperationLockManager;
import com.b2international.snowowl.datastore.oplock.impl.SingleRepositoryAndBranchLockTarget;
import com.b2international.snowowl.datastore.remotejobs.RemoteJobEntry;
import com.b2international.snowowl.datastore.remotejobs.RemoteJobNotification;
import com.b2international.snowowl.datastore.request.CommitResult;
import com.b2international.snowowl.datastore.request.DeleteRequestBuilder;
import com.b2international.snowowl.datastore.request.RepositoryRequests;
import com.b2international.snowowl.datastore.request.job.JobRequests;
import com.b2international.snowowl.datastore.server.index.SingleDirectoryIndexManager;
import com.b2international.snowowl.eventbus.IEventBus;
import com.b2international.snowowl.snomed.api.ISnomedClassificationService;
import com.b2international.snowowl.snomed.api.browser.ISnomedBrowserService;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserConcept;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserRelationship;
import com.b2international.snowowl.snomed.api.domain.classification.ChangeNature;
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
import com.b2international.snowowl.snomed.core.domain.SnomedConcept;
import com.b2international.snowowl.snomed.core.domain.SnomedConcepts;
import com.b2international.snowowl.snomed.core.domain.SnomedDescription;
import com.b2international.snowowl.snomed.core.domain.SnomedRelationship;
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
import com.b2international.snowowl.snomed.reasoner.classification.ClassificationSettings;
import com.b2international.snowowl.snomed.reasoner.classification.GetResultResponse;
import com.b2international.snowowl.snomed.reasoner.classification.SnomedExternalReasonerService;
import com.b2international.snowowl.snomed.reasoner.classification.SnomedInternalReasonerService;
import com.b2international.snowowl.snomed.reasoner.classification.SnomedReasonerService;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
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
import com.google.common.io.Closeables;

import io.reactivex.disposables.Disposable;

/**
 */
public class SnomedClassificationServiceImpl implements ISnomedClassificationService {

	private static final Logger LOG = LoggerFactory.getLogger(SnomedClassificationServiceImpl.class);
	
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
			
			final Branch branch = getBranchIfExists(branchPath);
			final IClassificationRun classificationRun = getClassificationRun(branchPath, classificationId);
			
			if (!ClassificationStatus.COMPLETED.equals(classificationRun.getStatus())) {
				return;
			}
			
			if (classificationRun.getLastCommitDate() != null && (branch.headTimestamp() > classificationRun.getLastCommitDate().getTime())) {
				updateStatus(classificationId, ClassificationStatus.STALE);
				return;
			} else {
				updateStatus(classificationId, ClassificationStatus.SAVING_IN_PROGRESS);
			}

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
									defaultNamespace, 
									branch);
							
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
							.filterByIds(removeOrDeactivateIds)
							.setLimit(removeOrDeactivateIds.size())
							.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
							.execute(getBus())
							.getSync();

					final SnomedReferenceSetMembers referringMembers = SnomedRequests.prepareSearchMember()
							.all()
							.filterByActive(true)
							.filterByReferencedComponent(removeOrDeactivateIds)
							.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
							.execute(getBus())
							.getSync();
					
					removeOrDeactivate(builder, removeOrDeactivateRelationships, referringMembers);
				}
				
				offset += relationshipChanges.getChanges().size();
				relationshipChanges = getRelationshipChanges(branchPath, classificationId, offset, RELATIONSHIP_BLOCK_SIZE);
			}
			
			commitChanges(branchPath, userId, builder, persistStopwatch)
					.then(new Function<CommitResult, Void>() { @Override public Void apply(final CommitResult input) {
						LOG.info("Classification changes saved on branch {}.", branchPath);
						return updateStatus(classificationId, ClassificationStatus.SAVED); 
					}})
					.fail(new Function<Throwable, Void>() { @Override public Void apply(final Throwable input) {
						LOG.error("Failed to save classification changes on branch {}.", branchPath, input);
						return updateStatus(classificationId, ClassificationStatus.SAVE_FAILED); 
					}})
					.getSync();
		}

		private Set<String> getInferredSourceIds(final IRelationshipChangeList relationshipChanges) {
			final Set<String> sourceIds = Sets.newHashSet();
			for (final IRelationshipChange change : relationshipChanges.getChanges()) {
				if (ChangeNature.INFERRED.equals(change.getChangeNature())) {
					sourceIds.add(change.getSourceId());
				}
			}
			return sourceIds;
		}

		private void populateModuleMap(final String branchPath, final Set<String> conceptIds, final Map<String, String> moduleMap) {
			SnomedRequests.prepareSearchConcept()
					.filterByIds(conceptIds)
					.setLimit(conceptIds.size())
					.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
					.execute(getBus())
					.then(new Function<SnomedConcepts, Void>() {
						@Override
						public Void apply(SnomedConcepts input) {
							for (SnomedConcept concept : input) {
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
				final String defaultNamespace,
				final Branch branch) {
		
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
					.setIdFromNamespace(namespace, branch);
			
			return inferredRelationshipBuilder;
		}

		private Promise<CommitResult> commitChanges(final String branchPath, 
				final String userId, 
				final BulkRequestBuilder<TransactionContext> builder,
				final Stopwatch persistStopwatch) {
			
			return SnomedRequests.prepareCommit()
					.setUserId(userId)
					.setCommitComment("Classified ontology.") // Same message in PersistChangesRemoteJob
					.setPreparationTime(persistStopwatch.elapsed(TimeUnit.MILLISECONDS))
					.setParentContextDescription(DatastoreLockContextDescriptions.CLASSIFY_WITH_REVIEW)
					.setBody(builder)
					.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
					.execute(getBus());
		}

		private void removeOrDeactivate(final BulkRequestBuilder<TransactionContext> builder,
				final SnomedRelationships removeOrDeactivateRelationships,
				final SnomedReferenceSetMembers referringMembers) {
			
			final Multimap<String, SnomedReferenceSetMember> referringMembersById = Multimaps.index(referringMembers, input -> input.getReferencedComponent().getId());

			for (SnomedRelationship relationship : removeOrDeactivateRelationships) {
				
				if (relationship.isReleased()) {
					
					for (SnomedReferenceSetMember snomedReferenceSetMember : referringMembersById.get(relationship.getId())) {
						SnomedRefSetMemberUpdateRequestBuilder updateMemberBuilder = SnomedRequests.prepareUpdateMember()
								.setMemberId(snomedReferenceSetMember.getId())
								.setSource(ImmutableMap.<String, Object>of(SnomedRf2Headers.FIELD_ACTIVE, Boolean.FALSE));
						
						builder.add(updateMemberBuilder);
					}
					
					SnomedRelationshipUpdateRequestBuilder updateRequestBuilder = SnomedRequests.prepareUpdateRelationship(relationship.getId())
							.setActive(false);

					builder.add(updateRequestBuilder);
				} else {
					
					for (SnomedReferenceSetMember snomedReferenceSetMember : referringMembersById.get(relationship.getId())) {
						DeleteRequestBuilder deleteMemberBuilder = SnomedRequests.prepareDeleteMember(snomedReferenceSetMember.getId());
						
						builder.add(deleteMemberBuilder);
					}
					
					DeleteRequestBuilder deleteRelationshipBuilder = SnomedRequests.prepareDeleteRelationship(relationship.getId());
					
					builder.add(deleteRelationshipBuilder);
				}
			}
		}
	}

	private ClassificationRunIndex indexService;
	private ExecutorService executorService;
	private Disposable remoteJobSubscription;
	private volatile boolean initialized = false;

	@Resource
	private SnomedBrowserService browserService;
	
	@Resource
	private Integer maxReasonerRuns;
	
	@Resource
	private IEventBus bus;
	
	@PostConstruct
	protected void init() {
		
		LOG.info("Initializing classification service; keeping indexed data for {} recent run(s).", getMaxReasonerRuns()); 
		
		final File dir = new File(new File(SnowOwlApplication.INSTANCE.getEnviroment().getDataDirectory(), "indexes"), "classification_runs");
		indexService = new ClassificationRunIndex(dir);
		ApplicationContext.getInstance().getServiceChecked(SingleDirectoryIndexManager.class).registerIndex(indexService);

		try {
			indexService.trimIndex(getMaxReasonerRuns());
			indexService.invalidateClassificationRuns();
		} catch (final IOException e) {
			LOG.error("Failed to run housekeeping tasks for the classification index.", e);
		}

		// TODO: common ExecutorService for asynchronous work?
		executorService = Executors.newCachedThreadPool();
		remoteJobSubscription = getNotifications()
				.ofType(RemoteJobNotification.class)
				.subscribe(this::onRemoteJobNotification);
		
		initialized = true;
	}
	
	private void checkServices() {
		if (!initialized) { 
			init();
		}
	}

	private void onRemoteJobNotification(RemoteJobNotification notification) {
		
		if (!RemoteJobNotification.isChanged(notification)) {
			return;
		}
		
		JobRequests.prepareSearch()
			.all()
			.filterByIds(notification.getJobIds())
			.buildAsync()
			.execute(getBus())
			.then(remoteJobs -> {
				for (RemoteJobEntry remoteJob : remoteJobs) {
					onRemoteJobChanged(remoteJob);
				}
				return remoteJobs;
			});
	}

	private void onRemoteJobChanged(RemoteJobEntry remoteJob) {
		
		String type = (String) remoteJob.getParameters().get("type");
		
		switch (type) {
			case "ExternalClassifyRequest": // fall through
			case "ClassifyRequest":
				onClassifyJobChanged(remoteJob);
				break;
			default:
				break;
		}
	}
	
	private void onClassifyJobChanged(RemoteJobEntry remoteJob) {
		checkServices();
		try {
			
			switch (remoteJob.getState()) {
			case CANCELED:
				indexService.updateClassificationRunStatus(remoteJob.getId(), ClassificationStatus.CANCELED);
				break;
			case FAILED:
				indexService.updateClassificationRunStatus(remoteJob.getId(), ClassificationStatus.FAILED);
				break;
			case FINISHED: 
				onClassifyJobFinished(remoteJob);
				break;
			case RUNNING:
				indexService.updateClassificationRunStatus(remoteJob.getId(), ClassificationStatus.RUNNING);
				break;
			case SCHEDULED:
				indexService.updateClassificationRunStatus(remoteJob.getId(), ClassificationStatus.SCHEDULED);
				break;
			case CANCEL_REQUESTED:
				// Nothing to do for this state change
				break;
			default:
				throw new IllegalStateException(MessageFormat.format("Unexpected remote job state ''{0}''.", remoteJob.getState()));
			}
			
		} catch (final IOException e) {
			LOG.error("Caught IOException while updating classification status.", e);
		}
	}

	private void onClassifyJobFinished(RemoteJobEntry remoteJob) {
		checkServices();
		executorService.submit(new Runnable() {
			@Override
			public void run() {
				try {
					
					boolean isExternalClassificationRequest = isExternalClassificationRequest(remoteJob);
					final GetResultResponse result = getReasonerService(isExternalClassificationRequest).getResult(remoteJob.getId());
					final Type responseType = result.getType();
	
					switch (responseType) {
						case NOT_AVAILABLE: 
							indexService.updateClassificationRunStatus(remoteJob.getId(), ClassificationStatus.FAILED);
							break;
						case STALE: 
							indexService.updateClassificationRunStatus(remoteJob.getId(), ClassificationStatus.STALE);
							break;
						case SUCCESS:
							indexService.updateClassificationRunStatus(remoteJob.getId(), ClassificationStatus.COMPLETED, result.getChanges());
							break;
						default:
							throw new IllegalStateException(MessageFormat.format("Unexpected response type ''{0}''.", responseType));
					}
					
					// Remove reasoner taxonomy immediately after processing it
					getReasonerService(isExternalClassificationRequest).removeResult(remoteJob.getId());
	
				} catch (final IOException e) {
					LOG.error("Caught IOException while registering classification data.", e);
				}
			}

		});
	}
	
	@SuppressWarnings("unchecked")
	private boolean isExternalClassificationRequest(RemoteJobEntry remoteJobEntry) {
		Map<String, Object> settings = (Map<String, Object>) remoteJobEntry.getParameters().get("settings");
		return (Boolean) settings.get("useExternalService");
	}

	@PreDestroy
	protected void shutdown() {
		if (null != remoteJobSubscription) {
			remoteJobSubscription.dispose();
			remoteJobSubscription = null;
		}

		if (null != executorService) {
			executorService.shutdown();
			executorService = null;
		}
		
		if (null != indexService) {
			ApplicationContext.getInstance().getServiceChecked(SingleDirectoryIndexManager.class).unregisterIndex(indexService);
			try {
				Closeables.close(indexService, true);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			indexService = null;
		}
		
		LOG.info("Classification service shut down.");
	}

	private SnomedBrowserService getBrowserService() {
		if (browserService == null) {
			browserService = (SnomedBrowserService) ApplicationContext.getInstance().getServiceChecked(ISnomedBrowserService.class);
		}
		return Preconditions.checkNotNull(browserService, "browserService cannot be null!");
	}

	private IEventBus getBus() {
		if (bus == null) {
			bus = ApplicationContext.getInstance().getServiceChecked(IEventBus.class);
		}
		return Preconditions.checkNotNull(bus, "bus cannot be null!");
	}

	private Integer getMaxReasonerRuns() {
		if (maxReasonerRuns == null) {
			maxReasonerRuns = ApplicationContext.getInstance().getServiceChecked(SnowOwlConfiguration.class)
					.getModuleConfig(SnomedCoreConfiguration.class).getClassificationConfig().getMaxReasonerRuns();
		}
		return Preconditions.checkNotNull(maxReasonerRuns, "maximum number of reasoner runs must be configured");
	}

	private Branch getBranchIfExists(final String branchPath) {
		final Branch branch = RepositoryRequests.branching()
				.prepareGet(branchPath)
				.build(SnomedDatastoreActivator.REPOSITORY_UUID)
				.execute(getBus())
				.getSync(BRANCH_READ_TIMEOUT, TimeUnit.MILLISECONDS);
		
		if (branch.isDeleted()) {
			throw new BadRequestException("Branch '%s' has been deleted and cannot accept further modifications.", branchPath);
		} else {
			return branch;
		}
	}

	private static SnomedReasonerService getReasonerService(boolean isExternalClassificationRequest) {
		if (isExternalClassificationRequest) {
			return ApplicationContext.getServiceForClass(SnomedExternalReasonerService.class);
		}
		return ApplicationContext.getServiceForClass(SnomedInternalReasonerService.class);
	}

	private static Notifications getNotifications() {
		return ApplicationContext.getServiceForClass(Notifications.class);
	}

	@Override
	public List<IClassificationRun> getAllClassificationRuns(final String branchPath) {
		checkServices();
		getBranchIfExists(branchPath);
		try {
			return indexService.getAllClassificationRuns(branchPath);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public IClassificationRun beginClassification(final String branchPath, final String reasonerId, final boolean useExternalService, final String userId) {
		checkServices();
		Branch branch = getBranchIfExists(branchPath);

		final ClassificationSettings settings = new ClassificationSettings(userId, branch.branchPath())
				.withParentContextDescription(DatastoreLockContextDescriptions.ROOT)
				.withExternalService(useExternalService)
				.withReasonerId(reasonerId);

		final ClassificationRun classificationRun = new ClassificationRun();
		classificationRun.setId(settings.getClassificationId());
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
		
		getReasonerService(useExternalService).beginClassification(settings);
		return classificationRun;
	}

	@Override
	public IClassificationRun getClassificationRun(final String branchPath, final String classificationId) {
		checkServices();
		try {
			return indexService.getClassificationRun(branchPath, classificationId);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<IEquivalentConceptSet> getEquivalentConceptSets(final String branchPath, final String classificationId, final List<ExtendedLocale> locales) {
		checkServices();
		getClassificationRun(branchPath, classificationId);
		
		try {
			final List<IEquivalentConceptSet> conceptSets = indexService.getEquivalentConceptSets(branchPath, classificationId);
			final Set<String> conceptIds = newHashSet();
			
			for (final IEquivalentConceptSet conceptSet : conceptSets) {
				for (final IEquivalentConcept equivalentConcept : conceptSet.getEquivalentConcepts()) {
					conceptIds.add(equivalentConcept.getId());
				}
			}

			final Map<String, SnomedDescription> fsnMap = new DescriptionService(getBus(), branchPath).getFullySpecifiedNames(conceptIds, locales);
			for (final IEquivalentConceptSet conceptSet : conceptSets) {
				for (final IEquivalentConcept equivalentConcept : conceptSet.getEquivalentConcepts()) {
					final String equivalentConceptId = equivalentConcept.getId();
					final SnomedDescription fsn = fsnMap.get(equivalentConceptId);
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
		checkServices();
		getClassificationRun(branchPath, classificationId);
		try {
			return indexService.getRelationshipChanges(branchPath, classificationId, conceptId, offset, limit);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public ISnomedBrowserConcept getConceptPreview(String branchPath, String classificationId, String conceptId, List<ExtendedLocale> locales) {
		final IComponentRef componentRef = SnomedServiceHelper.createComponentRef(branchPath, conceptId);
		final SnomedBrowserConcept conceptDetails = (SnomedBrowserConcept) getBrowserService().getConceptDetails(componentRef, locales);

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
				.filterByIds(relatedIds)
				.setLocales(locales)
				.setExpand("fsn()")
				.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
				.execute(getBus())
				.getSync();
		
		final Map<String, SnomedConcept> relatedConceptsById = Maps.uniqueIndex(relatedConcepts, input -> input.getId());
		
		final LoadingCache<SnomedConcept, SnomedBrowserRelationshipType> types = CacheBuilder.newBuilder().build(new CacheLoader<SnomedConcept, SnomedBrowserRelationshipType>() {
			@Override
			public SnomedBrowserRelationshipType load(SnomedConcept key) throws Exception {
				return getBrowserService().convertBrowserRelationshipType(key);
			}
		});
		
		final LoadingCache<SnomedConcept, SnomedBrowserRelationshipTarget> targets = CacheBuilder.newBuilder().build(new CacheLoader<SnomedConcept, SnomedBrowserRelationshipTarget>() {
			@Override
			public SnomedBrowserRelationshipTarget load(SnomedConcept key) throws Exception {
				return getBrowserService().convertBrowserRelationshipTarget(key);
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
				
					SnomedConcept destinationConcept = relatedConceptsById.get(relationshipChange.getDestinationId());
					SnomedConcept typeConcept = relatedConceptsById.get(relationshipChange.getTypeId());
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
		checkServices();
		IClassificationRun classificationRun = getClassificationRun(branchPath, classificationId);

		if (ClassificationStatus.COMPLETED.equals(classificationRun.getStatus())) {
			
			final DatastoreLockContext context = new DatastoreLockContext(userId, DatastoreLockContextDescriptions.CLASSIFY_WITH_REVIEW);
			final IOperationLockTarget target = new SingleRepositoryAndBranchLockTarget(SnomedDatastoreActivator.REPOSITORY_UUID, BranchPathUtils.createPath(branchPath));
			
			executorService.execute(new Runnable() {
				@Override
				public void run() {
					try {
						OperationLockRunner.with(getLockManager()).run(new PersistChangesRunnable(branchPath, classificationId, userId), context, BRANCH_LOCK_TIMEOUT, target);
					} catch (DatastoreOperationLockException e) {
						final DatastoreLockContext otherContext = e.getContext(target);
						throw new ConflictException("Failed to acquire or release lock for branch %s because %s is %s.", branchPath, otherContext.getUserId(), otherContext.getDescription());
					} catch (OperationLockException e) {
						throw new ConflictException("Failed to acquire or release lock for branch %s.", branchPath);
					} catch (InvocationTargetException e) {
						LOG.error("Caught exception while persisting changes for ID {}.", classificationId, e);
						updateStatus(classificationId, ClassificationStatus.SAVE_FAILED);
					} catch (InterruptedException e) {
						throw new ConflictException("Interrupted while acquiring or releasing lock for branch %s.", branchPath);
					}
				}
			});
		}
		
	}

	private static IDatastoreOperationLockManager getLockManager() {
		return ApplicationContext.getServiceForClass(IDatastoreOperationLockManager.class);
	}
	
	private Void updateStatus(final String id, final ClassificationStatus status) {
		try {
			indexService.updateClassificationRunStatus(id, status);
			return null;
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void removeClassificationRun(final String branchPath, final String classificationId) {
		checkServices();
		JobRequests.prepareDelete(classificationId)
			.buildAsync()
			.execute(getBus())
			.then(ignored -> {
				try {
					indexService.deleteClassificationData(classificationId);
				} catch (IOException e) {
					LOG.error("Caught IOException while deleting classification data for ID {}.", classificationId, e);
				}
				return ignored;
			})
			.getSync();
		
	}
	
}
