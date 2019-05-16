/*
 * Copyright 2011-2019 B2i Healthcare Pte Ltd, http://b2i.sg
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
package com.b2international.snowowl.snomed.api.impl.mergereview;

import static java.util.stream.Collectors.toSet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.b2international.commons.http.ExtendedLocale;
import com.b2international.commons.time.TimeUtil;
import com.b2international.snowowl.core.ApplicationContext;
import com.b2international.snowowl.core.exceptions.BadRequestException;
import com.b2international.snowowl.core.exceptions.ConflictException;
import com.b2international.snowowl.core.exceptions.MergeConflictException;
import com.b2international.snowowl.core.merge.Merge;
import com.b2international.snowowl.core.merge.Merge.Status;
import com.b2international.snowowl.core.merge.MergeConflict;
import com.b2international.snowowl.core.terminology.ComponentCategory;
import com.b2international.snowowl.datastore.request.RepositoryRequests;
import com.b2international.snowowl.datastore.review.ConceptChanges;
import com.b2international.snowowl.datastore.review.MergeReview;
import com.b2international.snowowl.datastore.review.ReviewManager;
import com.b2international.snowowl.eventbus.IEventBus;
import com.b2international.snowowl.eventbus.IHandler;
import com.b2international.snowowl.eventbus.IMessage;
import com.b2international.snowowl.identity.domain.User;
import com.b2international.snowowl.snomed.api.browser.ISnomedBrowserService;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserConcept;
import com.b2international.snowowl.snomed.api.mergereview.ISnomedBrowserMergeReviewDetail;
import com.b2international.snowowl.snomed.api.mergereview.ISnomedManualConceptMergeReviewService;
import com.b2international.snowowl.snomed.api.mergereview.ISnomedMergeReviewService;
import com.b2international.snowowl.snomed.datastore.SnomedDatastoreActivator;
import com.b2international.snowowl.snomed.datastore.id.SnomedIdentifiers;
import com.b2international.snowowl.snomed.datastore.request.SnomedRequests;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * The implementation of the manual concept merge review service.
 * <p>
 * The service relies on {@link ReviewManager} to compute two-way reviews (returning a set of new, changed and deleted
 * components for each side of a fork), and uses the intersection set of changed component IDs to check if there are
 * any non-classification related changes.
 * <p>
 * If the component is eligible for merge review, the full representation is retrieved on each side and an
 * automatically merged representation is created using the two sides as sources.
 *
 * @since 4.5
 */
@Resource
public class SnomedMergeReviewServiceImpl implements ISnomedMergeReviewService {

	private static class MergeReviewCompletionHandler implements IHandler<IMessage> {

		private final String address;
		private final IEventBus bus;
		private final CountDownLatch latch;
		private final Collection<IHandler<Merge>> handlers;
		private Optional<Collection<MergeConflict>> conflictsOptional = Optional.empty();

		@SafeVarargs
		protected MergeReviewCompletionHandler(final String address, final IEventBus bus, final CountDownLatch latch, final IHandler<Merge>... handlers) {
			this.address = address;
			this.bus = bus;
			this.latch = latch;
			this.handlers = Arrays.asList(handlers);
		}

		public void register() {
			bus.registerHandler(address, this);
		}

		@Override
		public void handle(final IMessage message) {
			handleMerge(message.body(Merge.class));
		}

		private void handleMerge(final Merge merge) {
			try {
				conflictsOptional = Optional.ofNullable(merge.getConflicts().isEmpty() ? null : merge.getConflicts());
				final Status status = merge.getStatus();
				if (Merge.Status.COMPLETED.equals(status)) {
					handlers.forEach(handler -> handler.handle(merge));
				}
			} finally {
				latch.countDown();
				bus.unregisterHandler(address, this);
			}
		}

		public Optional<Collection<MergeConflict>> conflictsOptional() {
			return conflictsOptional;
		}
	}

	private static class ConceptUpdateHandler implements IHandler<Merge> {

		private final List<ISnomedBrowserConcept> updates;
		private final String userId;
		private final List<ExtendedLocale> extendedLocales;
		private final ISnomedBrowserService browserService;

		private ConceptUpdateHandler(final List<ISnomedBrowserConcept> updates,
				final String userId,
				final List<ExtendedLocale> extendedLocales,
				final ISnomedBrowserService browserService) {
			this.updates = updates;
			this.userId = userId;
			this.extendedLocales = extendedLocales;
			this.browserService = browserService;
		}

		@Override
		public void handle(final Merge merge) {
			browserService.updateConcept(merge.getTarget(), updates, userId, extendedLocales);
		}
	}

	private static class ManualMergeDeleteHandler implements IHandler<Merge> {

		private final ISnomedManualConceptMergeReviewService manualMergeService;
		private final String mergeReviewId;

		private ManualMergeDeleteHandler(final ISnomedManualConceptMergeReviewService manualMergeService, final String mergeReviewId) {
			this.manualMergeService = manualMergeService;
			this.mergeReviewId = mergeReviewId;
		}

		@Override
		public void handle(final Merge merge) {
			manualMergeService.deleteAll(merge.getTarget(), mergeReviewId);
		}
	}

	private static class MergeReviewDeleteHandler implements IHandler<Merge> {

		private final String id;

		private MergeReviewDeleteHandler(final String mergeReviewId) {
			this.id = mergeReviewId;
		}

		@Override
		public void handle(final Merge merge) {
			RepositoryRequests
				.mergeReviews()
				.prepareDelete(id)
				.build(SnomedDatastoreActivator.REPOSITORY_UUID)
				.execute(ApplicationContext.getServiceForClass(IEventBus.class))
				.getSync();
		}
	}

	private static final Logger LOG = LoggerFactory.getLogger(SnomedMergeReviewServiceImpl.class);

	@Resource
	private IEventBus bus;

	@Resource
	protected ISnomedBrowserService browserService;

	@Resource
	private ISnomedManualConceptMergeReviewService manualConceptMergeService;

	@Override
	public Set<ISnomedBrowserMergeReviewDetail> getMergeReviewDetails(final String mergeReviewId, final List<ExtendedLocale> extendedLocales)
			throws InterruptedException, ExecutionException {

		final Stopwatch stopwatch = Stopwatch.createStarted();

		try {
			return getConceptDetails(getMergeReview(mergeReviewId), extendedLocales);
		} finally {
			LOG.info("Processing merge review with id '{}' took {}", mergeReviewId, TimeUtil.toString(stopwatch));
		}
	}

	private Set<ISnomedBrowserMergeReviewDetail> getConceptDetails(final MergeReview mergeReview, final List<ExtendedLocale> extendedLocales)
			throws InterruptedException, ExecutionException {

		final String sourcePath = mergeReview.sourcePath();
		final String targetPath = mergeReview.targetPath();

		final Set<String> filteredConceptIds = getFilteredMergeReviewIntersection(mergeReview);

		final List<ListenableFuture<ISnomedBrowserMergeReviewDetail>> changeFutures = Lists.newArrayList();
		final MergeReviewParameters parameters = new MergeReviewParameters(sourcePath, targetPath, extendedLocales, mergeReview.id());

		final ListeningExecutorService executorService = getExecutorService(filteredConceptIds.size());

		for (final String conceptId : filteredConceptIds) {
			changeFutures.add(executorService.submit(new ComputeMergeReviewCallable(conceptId, parameters)));
		}

		// Filter out all irrelevant detail objects
		final List<ISnomedBrowserMergeReviewDetail> changes = Futures.allAsList(changeFutures).get();
		final Set<ISnomedBrowserMergeReviewDetail> relevantChanges = changes.stream()
				.filter(change -> change != ISnomedBrowserMergeReviewDetail.SKIP_DETAIL)
				.collect(toSet());

		LOG.debug("Merge review {} count: {} initial, {} filtered", mergeReview.id(), changes.size(), relevantChanges.size());

		return relevantChanges;
	}

	private ListeningExecutorService getExecutorService(final int size) {
		return size > 1 ? MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(Math.min(4, size))) : MoreExecutors.newDirectExecutorService() ;
	}

	private Set<String> getFilteredMergeReviewIntersection(final MergeReview mergeReview) {

		final Set<String> sourceConceptIds = Sets.newHashSet();
		final Set<String> sourceDescriptionIds = Sets.newHashSet();
		final Set<String> sourceRelationshipIds = Sets.newHashSet();

		final ConceptChanges sourceChanges = RepositoryRequests.reviews()
				.prepareGetConceptChanges(mergeReview.sourceToTargetReviewId())
				.build(SnomedDatastoreActivator.REPOSITORY_UUID)
				.execute(getBus())
				.getSync();

		for (final String id : sourceChanges.changedConcepts()) {
			final ComponentCategory componentCategory = SnomedIdentifiers.getComponentCategory(id);
			if (componentCategory == ComponentCategory.CONCEPT) {
				sourceConceptIds.add(id);
			} else if (componentCategory == ComponentCategory.DESCRIPTION) {
				sourceDescriptionIds.add(id);
			} else if (componentCategory == ComponentCategory.RELATIONSHIP) {
				sourceRelationshipIds.add(id);
			} else {
				LOG.warn("Changed concept set contained invalid component id: {}", id);
			}
		}

		if (!sourceDescriptionIds.isEmpty()) {

			sourceConceptIds.addAll(SnomedRequests.prepareSearchDescription()
					.filterByIds(sourceDescriptionIds)
					.setLimit(sourceDescriptionIds.size())
					.build(SnomedDatastoreActivator.REPOSITORY_UUID, mergeReview.sourcePath())
					.execute(getBus())
					.then(input -> input.getItems().stream().map(d -> d.getConceptId()).collect(toSet()))
					.getSync());

		}

		if (!sourceRelationshipIds.isEmpty()) {

			sourceConceptIds.addAll(SnomedRequests.prepareSearchRelationship()
					.filterByIds(sourceRelationshipIds)
					.setLimit(sourceRelationshipIds.size())
					.build(SnomedDatastoreActivator.REPOSITORY_UUID, mergeReview.sourcePath())
					.execute(getBus())
					.then(input -> input.getItems().stream().map(d -> d.getSourceId()).collect(toSet()))
					.getSync());

		}

		sourceConceptIds.removeAll(sourceChanges.deletedConcepts());

		final ConceptChanges targetChanges = RepositoryRequests.reviews()
				.prepareGetConceptChanges(mergeReview.targetToSourceReviewId())
				.build(SnomedDatastoreActivator.REPOSITORY_UUID)
				.execute(getBus())
				.getSync();

		final Set<String> targetConceptIds = Sets.newHashSet();
		final Set<String> targetDescriptionIds = Sets.newHashSet();
		final Set<String> targetRelationshipIds = Sets.newHashSet();

		for (final String id : targetChanges.changedConcepts()) {
			final ComponentCategory componentCategory = SnomedIdentifiers.getComponentCategory(id);
			if (componentCategory == ComponentCategory.CONCEPT) {
				targetConceptIds.add(id);
			} else if (componentCategory == ComponentCategory.DESCRIPTION) {
				targetDescriptionIds.add(id);
			} else if (componentCategory == ComponentCategory.RELATIONSHIP) {
				targetRelationshipIds.add(id);
			} else {
				LOG.warn("Changed concept set contained invalid component id: {}", id);
			}
		}

		if (!targetDescriptionIds.isEmpty()) {

			targetConceptIds.addAll(SnomedRequests.prepareSearchDescription()
					.filterByIds(targetDescriptionIds)
					.setLimit(targetDescriptionIds.size())
					.build(SnomedDatastoreActivator.REPOSITORY_UUID, mergeReview.targetPath())
					.execute(getBus())
					.then(input -> input.getItems().stream().map(d -> d.getConceptId()).collect(toSet()))
					.getSync());


		}

		if (!targetRelationshipIds.isEmpty()) {

			targetConceptIds.addAll(SnomedRequests.prepareSearchRelationship()
					.filterByIds(targetRelationshipIds)
					.setLimit(targetRelationshipIds.size())
					.build(SnomedDatastoreActivator.REPOSITORY_UUID, mergeReview.targetPath())
					.execute(getBus())
					.then(input -> input.getItems().stream().map(d -> d.getSourceId()).collect(toSet()))
					.getSync());
		}

		targetConceptIds.removeAll(targetChanges.deletedConcepts());

		sourceConceptIds.retainAll(targetConceptIds);

		return sourceConceptIds;
	}

	@Override
	public Merge mergeAndReplayConceptUpdates(final String mergeReviewId, final String userId, final List<ExtendedLocale> extendedLocales)
			throws IOException, InterruptedException, ExecutionException, ConflictException {

		final MergeReview mergeReview = getMergeReview(mergeReviewId);
		final String sourcePath = mergeReview.sourcePath();
		final String targetPath = mergeReview.targetPath();

		// Check we have a full set of manually merged concepts
		final Set<String> mergeReviewIntersection = getFilteredMergeReviewIntersection(mergeReview);
		final List<ListenableFuture<String>> changeFutures = Lists.newArrayList();
		final MergeReviewParameters parameters = new MergeReviewParameters(sourcePath, targetPath, extendedLocales, mergeReview.id());

		final ListeningExecutorService executorService = getExecutorService(mergeReviewIntersection.size());

		for (final String conceptId : mergeReviewIntersection) {
			changeFutures.add(executorService.submit(new ComputeIntersectionIdsCallable(conceptId, parameters)));
		}

		final List<String> changes = Futures.allAsList(changeFutures).get();
		final Set<String> relevantIntersection = changes.stream().filter(change -> change != MergeReviewCallable.SKIP_ID ).collect(toSet());

		final List<ISnomedBrowserConcept> conceptUpdates = new ArrayList<ISnomedBrowserConcept>();
		for (final String conceptId : relevantIntersection) {
			if (!getManualConceptMergeService().exists(targetPath, mergeReviewId, conceptId)) {
				throw new BadRequestException("Manually merged concept %s does not exist for merge review %s", conceptId, mergeReviewId);
			} else {
				conceptUpdates.add(getManualConceptMergeService().retrieve(targetPath, mergeReviewId, conceptId));
			}
		}

		final UUID mergeId = UUID.randomUUID();
		final String address = String.format(Merge.ADDRESS_TEMPLATE, SnomedDatastoreActivator.REPOSITORY_UUID, mergeId);

		// using a latch here because the merge sends notification to the handlers on a different thread, so the actual concept apply may/or may not
		// happen by the end of this method.
		// the latch here makes sure that the handlers run before returning from this method.
		final CountDownLatch latch = new CountDownLatch(1);
		final MergeReviewCompletionHandler mergeReviewCompletionHandler = new MergeReviewCompletionHandler(address, getBus(), latch,
				// Set up one-shot handlers that will be notified when the merge completes successfully
				new ConceptUpdateHandler(conceptUpdates, userId, extendedLocales, getBrowserService()),
				new MergeReviewDeleteHandler(mergeReview.id()),
				new ManualMergeDeleteHandler(getManualConceptMergeService(), mergeReviewId));

		mergeReviewCompletionHandler.register();

		final Merge merge = RepositoryRequests
				.merging()
				.prepareCreate()
				.setId(mergeId)
				.setUserId(User.SYSTEM.getUsername())
				.setSource(sourcePath)
				.setTarget(targetPath)
				.setReviewId(mergeReview.sourceToTargetReviewId())
				.setCommitComment("Auto merging branches before applying manually merged concepts. " + sourcePath + " > " + targetPath)
				.build(SnomedDatastoreActivator.REPOSITORY_UUID)
				.execute(getBus())
				.getSync();

		latch.await(20, TimeUnit.MINUTES);
		final Optional<Collection<MergeConflict>> conflictsOptional = mergeReviewCompletionHandler.conflictsOptional();
		if (conflictsOptional.isPresent()) {
			final Collection<MergeConflict> collection = conflictsOptional.get();
			collection.forEach(conflict -> {
				LOG.info("Found conflict: {}", conflict.getMessage());
			});
			throw new MergeConflictException(collection, "Encountered conflicts while applying merge.");
		}
		return merge;
	}

	@Override
	public void persistManualConceptMerge(final MergeReview mergeReview, final ISnomedBrowserConcept concept) {
		getManualConceptMergeService().storeChanges(mergeReview.targetPath(), mergeReview.id(), concept);
	}

	private MergeReview getMergeReview(final String mergeReviewId) {
		return RepositoryRequests.mergeReviews()
				.prepareGet(mergeReviewId)
				.build(SnomedDatastoreActivator.REPOSITORY_UUID)
				.execute(getBus())
				.getSync();
	}

	private IEventBus getBus() {
		return bus;
	}

	private ISnomedBrowserService getBrowserService() {
		return browserService;
	}

	private ISnomedManualConceptMergeReviewService getManualConceptMergeService() {
		return manualConceptMergeService;
	}
}
