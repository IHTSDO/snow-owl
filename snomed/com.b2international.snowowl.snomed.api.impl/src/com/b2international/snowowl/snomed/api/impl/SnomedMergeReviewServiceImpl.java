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
package com.b2international.snowowl.snomed.api.impl;

import static java.util.stream.Collectors.toSet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.b2international.commons.ReflectionUtils;
import com.b2international.commons.http.ExtendedLocale;
import com.b2international.index.revision.RevisionIndex;
import com.b2international.snowowl.core.ApplicationContext;
import com.b2international.snowowl.core.api.IBranchPath;
import com.b2international.snowowl.core.date.EffectiveTimes;
import com.b2international.snowowl.core.exceptions.BadRequestException;
import com.b2international.snowowl.core.exceptions.ConflictException;
import com.b2international.snowowl.core.exceptions.MergeConflictException;
import com.b2international.snowowl.core.merge.Merge;
import com.b2international.snowowl.core.merge.Merge.Status;
import com.b2international.snowowl.core.merge.MergeConflict;
import com.b2international.snowowl.core.terminology.ComponentCategory;
import com.b2international.snowowl.datastore.BranchPathUtils;
import com.b2international.snowowl.datastore.request.RepositoryRequests;
import com.b2international.snowowl.datastore.review.ConceptChanges;
import com.b2international.snowowl.datastore.review.MergeReview;
import com.b2international.snowowl.datastore.review.ReviewManager;
import com.b2international.snowowl.eventbus.IEventBus;
import com.b2international.snowowl.eventbus.IHandler;
import com.b2international.snowowl.eventbus.IMessage;
import com.b2international.snowowl.identity.domain.User;
import com.b2international.snowowl.snomed.SnomedConstants.Concepts;
import com.b2international.snowowl.snomed.api.ISnomedMergeReviewService;
import com.b2international.snowowl.snomed.api.browser.ISnomedBrowserService;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserConcept;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserDescription;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserRelationship;
import com.b2international.snowowl.snomed.api.domain.mergereview.ISnomedBrowserMergeReviewDetail;
import com.b2international.snowowl.snomed.api.impl.domain.SnomedBrowserMergeReviewDetail;
import com.b2international.snowowl.snomed.api.impl.domain.browser.SnomedBrowserConcept;
import com.b2international.snowowl.snomed.core.domain.SnomedComponent;
import com.b2international.snowowl.snomed.core.domain.SnomedConcept;
import com.b2international.snowowl.snomed.core.domain.SnomedDescription;
import com.b2international.snowowl.snomed.core.domain.SnomedDescriptions;
import com.b2international.snowowl.snomed.core.domain.SnomedRelationship;
import com.b2international.snowowl.snomed.core.domain.SnomedRelationships;
import com.b2international.snowowl.snomed.datastore.SnomedDatastoreActivator;
import com.b2international.snowowl.snomed.datastore.id.SnomedIdentifiers;
import com.b2international.snowowl.snomed.datastore.request.SnomedRequests;
import com.google.common.base.Stopwatch;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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

	protected static class MergeReviewCompletionHandler implements IHandler<IMessage> {

		private final String address;
		private final IEventBus bus;
		private CountDownLatch latch;
		private Collection<IHandler<Merge>> handlers;
		private Optional<Collection<MergeConflict>> conflictsOptional = Optional.empty();
		
		@SafeVarargs
		protected MergeReviewCompletionHandler(final String address, final IEventBus bus, CountDownLatch latch, IHandler<Merge>... handlers) {
			this.address = address;
			this.bus = bus;
			this.latch = latch;
			this.handlers = Arrays.asList(handlers);
		}

		public MergeReviewCompletionHandler withLatch(CountDownLatch latch) {
			this.latch = latch;
			return this;
		}
		
		public void register() {
			bus.registerHandler(address, this);
		}
		
		@Override
		public void handle(IMessage message) {
			handleMerge(message.body(Merge.class));
		}

		private void handleMerge(Merge merge) {
			try {
				conflictsOptional = Optional.ofNullable(merge.getConflicts().isEmpty() ? null : merge.getConflicts());
				Status status = merge.getStatus();
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
	
	protected static class ConceptUpdateHandler implements IHandler<Merge> {
		
		private final List<ISnomedBrowserConcept> updates;
		private final String userId;
		private final List<ExtendedLocale> extendedLocales;
		private final ISnomedBrowserService browserService;
		
		private ConceptUpdateHandler(List<ISnomedBrowserConcept> updates,
				String userId, 
				List<ExtendedLocale> extendedLocales, 
				ISnomedBrowserService browserService) {
			this.updates = updates;
			this.userId = userId;
			this.extendedLocales = extendedLocales;
			this.browserService = browserService;
		}
		
		@Override
		public void handle(Merge merge) {
			browserService.updateConcept(merge.getTarget(), updates, userId, extendedLocales);
		}
	}
	
	protected static class ManualMergeDeleteHandler implements IHandler<Merge> {
		
		private final SnomedManualConceptMergeServiceImpl manualMergeService;
		private final String mergeReviewId;
		
		private ManualMergeDeleteHandler(SnomedManualConceptMergeServiceImpl manualMergeService, String mergeReviewId) {
			this.manualMergeService = manualMergeService;
			this.mergeReviewId = mergeReviewId;
		}
		
		@Override
		public void handle(Merge merge) {
			manualMergeService.deleteAll(merge.getTarget(), mergeReviewId);
		}
	}
	
	protected static class MergeReviewDeleteHandler implements IHandler<Merge> {
		
		private final String id;
		
		private MergeReviewDeleteHandler(String mergeReviewId) {
			this.id = mergeReviewId;
		}
		
		@Override
		public void handle(Merge merge) {
			RepositoryRequests
				.mergeReviews()
				.prepareDelete(id)
				.build(SnomedDatastoreActivator.REPOSITORY_UUID)
				.execute(ApplicationContext.getServiceForClass(IEventBus.class))
				.getSync();
		}
	}

	private static final Logger LOG = LoggerFactory.getLogger(SnomedMergeReviewServiceImpl.class);
	
	/**
	 * Checked fields: definitionStatus, effectiveTime, inactivationIndicator, moduleId, subclassDefinitionStatus
	 */
	private static final String[] IGNORED_CONCEPT_FIELDS = new String[] { 
			"class",
			"score",
			"storageKey",
			"iconId",
			
			"statedAncestorIds",
			"statedParentIds",
			"ancestorIds",
			"parentIds",

			"statedAncestors",
			"statedDescendants",
			"ancestors",
			"descendants",
			
			"id",
			"members",
			"referenceSet",
			"relationships",
			"descriptions",
			"fsn",
			"pt"
		};
	
	/**
	 * Checked fields: acceptabilityMap, caseSignificance, conceptId, effectiveTime, inactivationIndicator, languageCode, moduleId, term, typeId 
	 */
	private static final String[] IGNORED_DESCRIPTION_FIELDS = new String[] {
			"class",
			"score",
			"storageKey",
			"iconId",
			
			"id",
			"concept",
			"type",
			
			"members",
			"associationTargets"
		};
	
	/**
	 * Checked fields: characteristicType, destinationId, destinationNegated, effectiveTime, group, modifier, moduleId, sourceId, typeId, unionGroup
	 */
	private static final String[] IGNORED_RELATIONSHIP_FIELDS = new String[] {
			"class",
			"score",
			"iconId",
			"storageKey",
			
			"id",
			"source",
			"type",
			"destination",
			"members"
		};
	
	private static final Set<String> NON_INFERRED_CHARACTERISTIC_TYPES = ImmutableSet.of(
			Concepts.STATED_RELATIONSHIP,
			Concepts.ADDITIONAL_RELATIONSHIP,
			Concepts.QUALIFYING_RELATIONSHIP);

	private static final String FAKE_ID = "FAKE_ID"; 
	private static final SnomedDescription FAKE_DESCRIPTION = new SnomedDescription(FAKE_ID);
	private static final SnomedRelationship FAKE_RELATIONSHIP = new SnomedRelationship(FAKE_ID);
	
	/**
	 * Special value indicating that the concept should not be added to the review, because it did not change (ignoring
	 * any changes related to classification).
	 */
	private static final ISnomedBrowserMergeReviewDetail SKIP_DETAIL = new ISnomedBrowserMergeReviewDetail() {
		
		@Override
		public ISnomedBrowserConcept getTargetConcept() {
			throw new UnsupportedOperationException("getTargetConcept should not be called on empty merge review element.");
		}
		
		@Override
		public ISnomedBrowserConcept getSourceConcept() {
			throw new UnsupportedOperationException("getSourceConcept should not be called on empty merge review element.");
		}
		
		@Override
		public ISnomedBrowserConcept getManuallyMergedConcept() {
			throw new UnsupportedOperationException("getManuallyMergedConcept should not be called on empty merge review element.");
		}
		
		@Override
		public ISnomedBrowserConcept getAutoMergedConcept() {
			throw new UnsupportedOperationException("getAutoMergedConcept should not be called on empty merge review element.");
		}
	};

	/**
	 * Special value indicating that the concept ID should not be added to the intersection set, because it did not change (ignoring
	 * any changes related to classification).
	 */
	private static final String SKIP_ID = "";
	
	private static class MergeReviewParameters {
		private final String sourcePath;
		private final String targetPath;
		private final List<ExtendedLocale> extendedLocales;
		private final String mergeReviewId;
		
		private MergeReviewParameters(final String sourcePath, final String targetPath, final List<ExtendedLocale> extendedLocales,
				final String mergeReviewId) {
			this.sourcePath = sourcePath;
			this.targetPath = targetPath;
			this.extendedLocales = extendedLocales;
			this.mergeReviewId = mergeReviewId;
		}

		public String getSourcePath() {
			return sourcePath;
		}

		public String getTargetPath() {
			return targetPath;
		}

		public List<ExtendedLocale> getExtendedLocales() {
			return extendedLocales;
		}

		public String getMergeReviewId() {
			return mergeReviewId;
		}
	}
	
	private abstract class MergeReviewCallable<T> implements Callable<T> {
		
		protected final String conceptId;
		protected final MergeReviewParameters parameters;

		private MergeReviewCallable(final String conceptId, final MergeReviewParameters parameters) {
			this.conceptId = conceptId;
			this.parameters = parameters;
		}
		
		@Override
		public T call() throws Exception {

			IBranchPath source = BranchPathUtils.createPath(parameters.getSourcePath());
			IBranchPath target = BranchPathUtils.createPath(parameters.getTargetPath());
			
			String basePath;
			
			if (target.getParent().equals(source)) {
				basePath = String.format("%s%s", parameters.getTargetPath(), RevisionIndex.BASE_REF_CHAR);
			} else if (source.getParent().equals(target)) {
				basePath = String.format("%s%s", parameters.getSourcePath(), RevisionIndex.BASE_REF_CHAR);
			} else {
				throw new RuntimeException("There must be a parent-child relationship between the source and the target branches");
			}
			
			final SnomedConcept baseConcept = getConcept(basePath, conceptId);
			final SnomedConcept sourceConcept = getConcept(parameters.getSourcePath(), conceptId);
			final SnomedConcept targetConcept = getConcept(parameters.getTargetPath(), conceptId);
			
			if (hasSinglePropertyChanges(baseConcept, sourceConcept, targetConcept, IGNORED_CONCEPT_FIELDS)) {
				return onSuccess();
			}
			
			if (hasFsnOrPtChanges(baseConcept.getFsn(), sourceConcept.getFsn(), targetConcept.getFsn())) {
				return onSuccess();
			}
			
			if (hasFsnOrPtChanges(baseConcept.getPt(), sourceConcept.getPt(), targetConcept.getPt())) {
				return onSuccess();
			}
			
			if (hasDescriptionChanges(baseConcept, sourceConcept, targetConcept, basePath)) {
				return onSuccess();
			}
			
			if (hasNonInferredRelationshipChanges(baseConcept, sourceConcept, targetConcept, basePath)) {
				return onSuccess();
			}
			
			return onSkip();
		}

		private boolean hasFsnOrPtChanges(SnomedDescription baseDescription, SnomedDescription sourceDescription, SnomedDescription targetDescription) {
			
			if (baseDescription !=null) {
				
				if (sourceDescription != null && targetDescription !=null) {
					
					boolean newTargetDescription = !baseDescription.getId().equals(targetDescription.getId());
					boolean hasSourceChanges = hasSinglePropertyChanges(FAKE_DESCRIPTION, baseDescription /* must be compared against the base */,
							sourceDescription, IGNORED_DESCRIPTION_FIELDS);

					if (hasSourceChanges && newTargetDescription) {
						return true;
					}
					
					boolean newSourceDescription = !baseDescription.getId().equals(sourceDescription.getId());
					boolean hasTargetChanges = hasSinglePropertyChanges(FAKE_DESCRIPTION, baseDescription /* must be compared against the base */,
							targetDescription, IGNORED_DESCRIPTION_FIELDS);
					
					if (hasTargetChanges && newSourceDescription) {
						return true;
					}

				} else if (sourceDescription != null) { // target removed pt or fsn
					
					// if source has changed show merge review
					if (hasSinglePropertyChanges(FAKE_DESCRIPTION, baseDescription /* must be compared against the base */, sourceDescription,
							IGNORED_DESCRIPTION_FIELDS)) {
						return true;
					}
					
				} else if (targetDescription != null) { // source removed pt or fsn

					// if target has changed show merge review
					if (hasSinglePropertyChanges(FAKE_DESCRIPTION, baseDescription /* must be compared against the base */,
							targetDescription, IGNORED_DESCRIPTION_FIELDS)) {
						return true;
					}
					
				} else {
					// fall through -> both sides removed either pt or the fsn
				}
				
			} else if (sourceDescription != null && targetDescription !=null) {
				return true; // both source and target added a new description, must be reviewed
			}
			
			return false;
		}

		private SnomedConcept getConcept(final String path, final String conceptId) {
			return SnomedRequests.prepareGetConcept(conceptId)
				.setExpand("fsn(),pt(),inactivationProperties()")
				.setLocales(parameters.getExtendedLocales())
				.build(SnomedDatastoreActivator.REPOSITORY_UUID, path)
				.execute(getBus())
				.getSync();
		}
		
		private SnomedDescriptions getDescriptions(final String path, final String conceptId) {
			return SnomedRequests.prepareSearchDescription()
				.all()
				.filterByConcept(conceptId)
				.build(SnomedDatastoreActivator.REPOSITORY_UUID, path)
				.execute(getBus())
				.getSync();
		}
		
		private boolean hasDescriptionChanges(final SnomedConcept baseConcept, final SnomedConcept sourceConcept, SnomedConcept targetConcept,
				String basePath) {
	
			boolean sourceChangedActiveStatus = baseConcept.isActive() ^ sourceConcept.isActive();
			boolean targetChangedActiveStatus = baseConcept.isActive() ^ targetConcept.isActive();
			boolean activeStatusDiffers = sourceConcept.isActive() ^ targetConcept.isActive();
			
			final SnomedDescriptions baseDescriptions = getDescriptions(basePath, conceptId);
			final SnomedDescriptions sourceDescriptions = getDescriptions(parameters.getSourcePath(), conceptId);
			final SnomedDescriptions targetDescriptions = getDescriptions(parameters.getTargetPath(), conceptId);
			
			final Map<String, SnomedDescription> baseMap = Maps.uniqueIndex(baseDescriptions, input -> input.getId());
			final Map<String, SnomedDescription> sourceMap = Maps.uniqueIndex(sourceDescriptions, input -> input.getId());
			final Map<String, SnomedDescription> targetMap = Maps.uniqueIndex(targetDescriptions, input -> input.getId());

			Set<String> newSourceDescriptionIds = Sets.difference(sourceMap.keySet(), baseMap.keySet());
			Set<String> newTargetDescriptionIds = Sets.difference(targetMap.keySet(), baseMap.keySet());
			
			// if there are additions on both sides
			if (!newSourceDescriptionIds.isEmpty() && !newTargetDescriptionIds.isEmpty()) {
				if (!Sets.difference(newSourceDescriptionIds, newTargetDescriptionIds).isEmpty() || 
						!Sets.difference(newTargetDescriptionIds, newSourceDescriptionIds).isEmpty()) { // if the are differing additions on both sides
					return true; // always show merge screen when there are additions on both sides
				}
			}
			
			// if there were additions while the other side was inactivated
			if (activeStatusDiffers) {
				if (targetChangedActiveStatus && !newSourceDescriptionIds.isEmpty()) {
					return true;
				} else if (sourceChangedActiveStatus && !newTargetDescriptionIds.isEmpty()) {
					return true;
				}
			}
			
			Set<String> allDescriptionIds = Sets.union(baseMap.keySet(), Sets.union(sourceMap.keySet(), targetMap.keySet()));
			
			for (final String id : allDescriptionIds) {
				
				if (!baseMap.containsKey(id) && (sourceMap.containsKey(id) ^ targetMap.containsKey(id))) {
					continue; // this must be an addition
				} else if (baseMap.containsKey(id) && !sourceMap.containsKey(id) && !targetMap.containsKey(id)) {
					continue; // this must the same deletion on both sides;
				}
				
				final SnomedDescription baseDescription = baseMap.containsKey(id) ? baseMap.get(id) : FAKE_DESCRIPTION;
				final SnomedDescription sourceDescription = sourceMap.containsKey(id) ? sourceMap.get(id) : FAKE_DESCRIPTION;
				final SnomedDescription targetDescription = targetMap.containsKey(id) ? targetMap.get(id) : FAKE_DESCRIPTION;
				
				if (hasSinglePropertyChanges(baseDescription, sourceDescription, targetDescription, IGNORED_DESCRIPTION_FIELDS)) {
					return true;
				}
			}
			
			return false;
		}

		private SnomedRelationships getUnpublishedNonInferredRelationships(final String path, final String conceptId) {
			return SnomedRequests.prepareSearchRelationship()
				.all()
				.filterBySource(conceptId)
				.filterByEffectiveTime(EffectiveTimes.UNSET_EFFECTIVE_TIME_LABEL)
				.filterByCharacteristicTypes(NON_INFERRED_CHARACTERISTIC_TYPES)
				.build(SnomedDatastoreActivator.REPOSITORY_UUID, path)
				.execute(getBus())
				.getSync();
		}
		
		private boolean hasNonInferredRelationshipChanges(final SnomedConcept baseConcept, final SnomedConcept sourceConcept,
				SnomedConcept targetConcept, String basePath) {
	
			boolean sourceChangedActiveStatus = baseConcept.isActive() ^ sourceConcept.isActive();
			boolean targetChangedActiveStatus = baseConcept.isActive() ^ targetConcept.isActive();
			boolean activeStatusDiffers = sourceConcept.isActive() ^ targetConcept.isActive();
			
			final SnomedRelationships baseRelationships = getUnpublishedNonInferredRelationships(basePath, conceptId);
			final SnomedRelationships sourceRelationships = getUnpublishedNonInferredRelationships(parameters.getSourcePath(), conceptId);
			final SnomedRelationships targetRelationships = getUnpublishedNonInferredRelationships(parameters.getTargetPath(), conceptId);
			
			final Map<String, SnomedRelationship> baseMap = FluentIterable.from(baseRelationships).uniqueIndex(input -> input.getId());
			final Map<String, SnomedRelationship> sourceMap = FluentIterable.from(sourceRelationships).uniqueIndex(input -> input.getId());
			final Map<String, SnomedRelationship> targetMap = FluentIterable.from(targetRelationships).uniqueIndex(input -> input.getId());

			Set<String> newSourceRelationshipIds = Sets.difference(sourceMap.keySet(), baseMap.keySet());
			Set<String> newTargetRelationshipIds = Sets.difference(targetMap.keySet(), baseMap.keySet());
			
			// if there are additions on both sides
			if (!newSourceRelationshipIds.isEmpty() && !newTargetRelationshipIds.isEmpty()) {
				if (!Sets.difference(newSourceRelationshipIds, newTargetRelationshipIds).isEmpty() || 
						!Sets.difference(newTargetRelationshipIds, newSourceRelationshipIds).isEmpty()) { // if the are differing additions on both sides
					return true; // always show merge screen when there are additions on both sides
				}
			}
			
			// if there were additions while the other side was inactivated
			if (activeStatusDiffers) {
				if (targetChangedActiveStatus && !newSourceRelationshipIds.isEmpty()) {
					return true;
				} else if (sourceChangedActiveStatus && !newTargetRelationshipIds.isEmpty()) {
					return true;
				}
			}
			
			Set<String> allRelationshipIds = Sets.union(baseMap.keySet(), Sets.union(sourceMap.keySet(), targetMap.keySet()));
			
			for (final String id : allRelationshipIds) {
				
				if (!baseMap.containsKey(id) && (sourceMap.containsKey(id) ^ targetMap.containsKey(id))) {
					continue; // this must be an addition
				} else if (baseMap.containsKey(id) && !sourceMap.containsKey(id) && !targetMap.containsKey(id)) {
					continue; // this must the same deletion on both sides;
				}
				
				final SnomedRelationship baseRelationship = baseMap.containsKey(id) ? baseMap.get(id) : FAKE_RELATIONSHIP;
				final SnomedRelationship sourceRelationship = sourceMap.containsKey(id) ? sourceMap.get(id) : FAKE_RELATIONSHIP;
				final SnomedRelationship targetRelationship = targetMap.containsKey(id) ? targetMap.get(id) : FAKE_RELATIONSHIP;
				
				if (hasSinglePropertyChanges(baseRelationship, sourceRelationship, targetRelationship, IGNORED_RELATIONSHIP_FIELDS)) {
					return true;
				}
			}
			
			return false;
		}
		
		private boolean hasSinglePropertyChanges(final SnomedComponent baseComponent, final SnomedComponent sourceComponent,
				SnomedComponent targetComponent, final String... ignoredProperties) {
	
			final Map<String, Object> baseMap = ReflectionUtils.getBeanMap(baseComponent, ignoredProperties);
			final Map<String, Object> sourceMap = ReflectionUtils.getBeanMap(sourceComponent, ignoredProperties);
			final Map<String, Object> targetMap = ReflectionUtils.getBeanMap(targetComponent, ignoredProperties);
			
		    for (final String key : baseMap.keySet()) {
		    	
		    	Object baseValue = baseMap.get(key);
		    	Object sourceValue = sourceMap.get(key);
		    	Object targetValue = targetMap.get(key);
		        
		    	// skip null comparison
		    	if (baseValue == null && sourceValue == null && targetValue == null) {
		    		continue;
		    	}
		    	
		    	// Skip multi-valued properties
		    	if (sourceValue instanceof Iterable) {
		    		continue;
		    	}
		    	
		    	// Skip arrays as well
		    	if (sourceValue != null && sourceValue.getClass().isArray()) {
		    		continue;
		    	}
		    	
		    	// Compare core components by identifier
		    	if (baseValue instanceof SnomedComponent) {
		    		baseValue = ((SnomedComponent) baseValue).getId();
		    	}
		    	
		    	if (sourceValue instanceof SnomedComponent) {
		    		sourceValue = ((SnomedComponent) sourceValue).getId();
		    	}
		    	
		    	if (targetValue instanceof SnomedComponent) {
		    		targetValue = ((SnomedComponent) targetValue).getId();
		    	}
		    	
				if (baseComponent.getId().equals(FAKE_ID)) {
		    		
		    		if (!Objects.equals(sourceValue, targetValue)) {
				    	return true;
				    }
		    		
		    	} else {
		    		
		    		if (!sourceComponent.getId().equals(FAKE_ID) && !targetComponent.getId().equals(FAKE_ID)) {
		    			
		    			boolean hasSourceChanges = !Objects.equals(sourceValue, baseValue);
		    			boolean hasTargetChanges = !Objects.equals(targetValue, baseValue);
		    			boolean hasConflictingChanges = !Objects.equals(sourceValue, targetValue);
		    			
		    			if (hasSourceChanges && hasTargetChanges && hasConflictingChanges) {
		    				return true;
		    			}
		    			
		    		} else if (sourceComponent.getId().equals(FAKE_ID)) {
		    			
		    			if (!Objects.equals(targetValue, baseValue)) { // has target changes
		    				return true;
		    			}
		    			
		    		} else if (targetComponent.getId().equals(FAKE_ID)) {
		    			
		    			if (!Objects.equals(sourceValue, baseValue)) { // has source changes
		    				return true;
		    			}
		    			
		    		}
		    		
		    	}
		    	
		    }
		    
		    return false;
		}
		
		protected abstract T onSuccess() throws IOException;
		
		protected abstract T onSkip();
	}
	
	private class ComputeMergeReviewCallable extends MergeReviewCallable<ISnomedBrowserMergeReviewDetail> {

		private ComputeMergeReviewCallable(final String conceptId, final MergeReviewParameters parameters) {
			super(conceptId, parameters);
		}

		
		@Override
		protected ISnomedBrowserMergeReviewDetail onSuccess() throws IOException {
			final ISnomedBrowserConcept sourceConcept = getBrowserService().getConceptDetails(parameters.getSourcePath(), conceptId, parameters.getExtendedLocales());
			final ISnomedBrowserConcept targetConcept = getBrowserService().getConceptDetails(parameters.getTargetPath(), conceptId, parameters.getExtendedLocales());

			final ISnomedBrowserConcept autoMergedConcept = mergeConcepts(sourceConcept, targetConcept, parameters.getExtendedLocales());
			final ISnomedBrowserConcept manuallyMergedConcept = getManualConceptMergeService().exists(parameters.getTargetPath(),
					parameters.getMergeReviewId(), conceptId)
							? getManualConceptMergeService().retrieve(parameters.getTargetPath(), parameters.getMergeReviewId(), conceptId)
							: null;

			return new SnomedBrowserMergeReviewDetail(sourceConcept, targetConcept, autoMergedConcept, manuallyMergedConcept);
		}

		private SnomedBrowserConcept mergeConcepts(
				final ISnomedBrowserConcept sourceConcept,
				final ISnomedBrowserConcept targetConcept, 
				final List<ExtendedLocale> locales) {

			final SnomedBrowserConcept mergedConcept = new SnomedBrowserConcept();
			// If one of the concepts is unpublished, then it's values are newer.  If both are unpublished, source would win
			ISnomedBrowserConcept winner = sourceConcept;
			if (targetConcept.getEffectiveTime() == null && sourceConcept.getEffectiveTime() != null) {
				winner = targetConcept;
			}
			// Set directly owned values
			mergedConcept.setConceptId(winner.getConceptId());
			mergedConcept.setActive(winner.isActive());
			mergedConcept.setDefinitionStatus(winner.getDefinitionStatus());
			mergedConcept.setEffectiveTime(winner.getEffectiveTime());
			mergedConcept.setModuleId(winner.getModuleId());
			mergedConcept.setIsLeafInferred(winner.getIsLeafInferred());
			mergedConcept.setIsLeafStated(winner.getIsLeafStated());
			
			mergedConcept.setInactivationIndicator(winner.getInactivationIndicator());
			mergedConcept.setAssociationTargets(winner.getAssociationTargets());
			
			// Merge Descriptions - take all the descriptions from source, and add in from target
			// if they're unpublished, which will cause an overwrite in the Set if the Description Id matches
			// TODO UNLESS the source description is also unpublished (Change to use map?)
			final Set<ISnomedBrowserDescription> mergedDescriptions = new HashSet<ISnomedBrowserDescription>(sourceConcept.getDescriptions());
			for (final ISnomedBrowserDescription thisDescription : targetConcept.getDescriptions()) {
				if (thisDescription.getEffectiveTime() == null) {
					mergedDescriptions.add(thisDescription);
				}
			}
			mergedConcept.setDescriptions(new ArrayList<ISnomedBrowserDescription>(mergedDescriptions));
			
			// Merge Relationships  - same process using Set to remove duplicated
			final Set<ISnomedBrowserRelationship> mergedRelationships = new HashSet<ISnomedBrowserRelationship>(sourceConcept.getRelationships());
			for (final ISnomedBrowserRelationship thisRelationship : targetConcept.getRelationships()) {
				if (thisRelationship.getEffectiveTime() == null) {
					mergedRelationships.add(thisRelationship);
				}
			}
			mergedConcept.setRelationships(new ArrayList<ISnomedBrowserRelationship>(mergedRelationships));
			
			return mergedConcept;
		}

		@Override
		protected ISnomedBrowserMergeReviewDetail onSkip() {
			return SKIP_DETAIL;
		}
	}
	
	private class ComputeIntersectionIdsCallable extends MergeReviewCallable<String> {

		private ComputeIntersectionIdsCallable(final String conceptId, final MergeReviewParameters parameters) {
			super(conceptId, parameters);
		}
		
		@Override
		protected String onSuccess() throws IOException {
			return conceptId;
		}
		
		@Override
		protected String onSkip() {
			return SKIP_ID;
		}
	}

	@Resource
	private IEventBus bus;
	
	@Resource
	protected ISnomedBrowserService browserService;
	
	@Resource
	private SnomedManualConceptMergeServiceImpl manualConceptMergeService;
	
	private final ListeningExecutorService executorService;
	
	public SnomedMergeReviewServiceImpl() {
		executorService = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(3));
	}

	@Override
	public Set<ISnomedBrowserMergeReviewDetail> getMergeReviewDetails(final String mergeReviewId, final List<ExtendedLocale> extendedLocales)
			throws InterruptedException, ExecutionException {
		final Stopwatch stopwatch = Stopwatch.createStarted();
		final MergeReview mergeReview = getMergeReview(mergeReviewId);
		
		try {
			return getConceptDetails(mergeReview, extendedLocales);
		} finally {
			LOG.debug("Merge review details for {} (source: {}, target: {}) computed in {}.", mergeReview.id(), mergeReview.sourcePath(),
					mergeReview.targetPath(), stopwatch);
		}
	}
	
	private Set<ISnomedBrowserMergeReviewDetail> getConceptDetails(final MergeReview mergeReview, final List<ExtendedLocale> extendedLocales)
			throws InterruptedException, ExecutionException {
		final String sourcePath = mergeReview.sourcePath();
		final String targetPath = mergeReview.targetPath();
		
		final Set<String> filteredConceptIds = getFilteredMergeReviewIntersection(mergeReview);
		
		final List<ListenableFuture<ISnomedBrowserMergeReviewDetail>> changeFutures = Lists.newArrayList();
		final MergeReviewParameters parameters = new MergeReviewParameters(sourcePath, targetPath, extendedLocales, mergeReview.id());
		
		for (final String conceptId : filteredConceptIds) {
			changeFutures.add(executorService.submit(new ComputeMergeReviewCallable(conceptId, parameters)));
		}
		
		// Filter out all irrelevant detail objects
		final List<ISnomedBrowserMergeReviewDetail> changes = Futures.allAsList(changeFutures).get();
		final Set<ISnomedBrowserMergeReviewDetail> relevantChanges = changes.stream().filter(change -> change != SKIP_DETAIL).collect(toSet());
		
		LOG.debug("Merge review {} count: {} initial, {} filtered", mergeReview.id(), changes.size(), relevantChanges.size());
		
		return relevantChanges;
	}

	private Set<String> getFilteredMergeReviewIntersection(final MergeReview mergeReview) {
		
		final Set<String> sourceConceptIds = Sets.newHashSet();
		final Set<String> sourceDescriptionIds = Sets.newHashSet();
		final Set<String> sourceRelationshipIds = Sets.newHashSet();
		
		ConceptChanges sourceChanges = RepositoryRequests.reviews()
				.prepareGetConceptChanges(mergeReview.sourceToTargetReviewId())
				.build(SnomedDatastoreActivator.REPOSITORY_UUID)
				.execute(getBus())
				.getSync();
		
		for (String id : sourceChanges.changedConcepts()) {
			ComponentCategory componentCategory = SnomedIdentifiers.getComponentCategory(id);
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
		
		ConceptChanges targetChanges = RepositoryRequests.reviews()
				.prepareGetConceptChanges(mergeReview.targetToSourceReviewId())
				.build(SnomedDatastoreActivator.REPOSITORY_UUID)
				.execute(getBus())
				.getSync();
		
		final Set<String> targetConceptIds = Sets.newHashSet();
		final Set<String> targetDescriptionIds = Sets.newHashSet();
		final Set<String> targetRelationshipIds = Sets.newHashSet();
		
		for (String id : targetChanges.changedConcepts()) {
			ComponentCategory componentCategory = SnomedIdentifiers.getComponentCategory(id);
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

		for (final String conceptId : mergeReviewIntersection) {
			changeFutures.add(executorService.submit(new ComputeIntersectionIdsCallable(conceptId, parameters)));
		}
		
		final List<String> changes = Futures.allAsList(changeFutures).get();
		final Set<String> relevantIntersection = changes.stream().filter(change -> change != SKIP_ID ).collect(toSet());

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
		CountDownLatch latch = new CountDownLatch(1);
		MergeReviewCompletionHandler mergeReviewCompletionHandler = new MergeReviewCompletionHandler(address, getBus(), latch,
				// Set up one-shot handlers that will be notified when the merge completes successfully
				new ConceptUpdateHandler(conceptUpdates, userId, extendedLocales, getBrowserService()), 
				new MergeReviewDeleteHandler(mergeReview.id()),
				new ManualMergeDeleteHandler(getManualConceptMergeService(), mergeReviewId));
		
		mergeReviewCompletionHandler.register();
		
		Merge merge = RepositoryRequests
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
		Optional<Collection<MergeConflict>> conflictsOptional = mergeReviewCompletionHandler.conflictsOptional();
		if (conflictsOptional.isPresent()) {
			Collection<MergeConflict> collection = conflictsOptional.get();
			collection.forEach(conflict -> {
				LOG.info("Found conflict: {}", conflict.getMessage());
			});
			throw new MergeConflictException(collection, "Encountered conflicts while applying merge.");
		}
		return merge;
	}

	@Override
	public void persistManualConceptMerge(final MergeReview mergeReview, final ISnomedBrowserConcept concept) {
		getManualConceptMergeService().storeConceptChanges(mergeReview.targetPath(), mergeReview.id(), concept);
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

	private SnomedManualConceptMergeServiceImpl getManualConceptMergeService() {
		return manualConceptMergeService;
	}
}
