/*
 * Copyright 2019 B2i Healthcare Pte Ltd, http://b2i.sg
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

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;

import com.b2international.commons.ReflectionUtils;
import com.b2international.index.revision.RevisionIndex;
import com.b2international.snowowl.core.ApplicationContext;
import com.b2international.snowowl.core.api.IBranchPath;
import com.b2international.snowowl.core.date.EffectiveTimes;
import com.b2international.snowowl.datastore.BranchPathUtils;
import com.b2international.snowowl.eventbus.IEventBus;
import com.b2international.snowowl.snomed.SnomedConstants.Concepts;
import com.b2international.snowowl.snomed.api.browser.ISnomedBrowserService;
import com.b2international.snowowl.snomed.api.mergereview.ISnomedManualConceptMergeReviewService;
import com.b2international.snowowl.snomed.core.domain.SnomedComponent;
import com.b2international.snowowl.snomed.core.domain.SnomedConcept;
import com.b2international.snowowl.snomed.core.domain.SnomedDescription;
import com.b2international.snowowl.snomed.core.domain.SnomedDescriptions;
import com.b2international.snowowl.snomed.core.domain.SnomedRelationship;
import com.b2international.snowowl.snomed.core.domain.SnomedRelationships;
import com.b2international.snowowl.snomed.datastore.SnomedDatastoreActivator;
import com.b2international.snowowl.snomed.datastore.request.SnomedRequests;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * @since 4.5
 */
public abstract class MergeReviewCallable<T> implements Callable<T> {

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
	 * Special value indicating that the concept ID should not be added to the intersection set, because it did not change (ignoring
	 * any changes related to classification).
	 */
	protected static final String SKIP_ID = "";

	protected final String conceptId;
	protected final MergeReviewParameters parameters;

	protected MergeReviewCallable(final String conceptId, final MergeReviewParameters parameters) {
		this.conceptId = conceptId;
		this.parameters = parameters;
	}

	@Override
	public T call() throws Exception {

		final IBranchPath source = BranchPathUtils.createPath(parameters.getSourcePath());
		final IBranchPath target = BranchPathUtils.createPath(parameters.getTargetPath());

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

	private boolean hasFsnOrPtChanges(final SnomedDescription baseDescription, final SnomedDescription sourceDescription, final SnomedDescription targetDescription) {

		if (baseDescription !=null) {

			if (sourceDescription != null && targetDescription !=null) {

				final boolean newTargetDescription = !baseDescription.getId().equals(targetDescription.getId());
				final boolean hasSourceChanges = hasSinglePropertyChanges(FAKE_DESCRIPTION, baseDescription /* must be compared against the base */,
						sourceDescription, IGNORED_DESCRIPTION_FIELDS);

				if (hasSourceChanges && newTargetDescription) {
					return true;
				}

				final boolean newSourceDescription = !baseDescription.getId().equals(sourceDescription.getId());
				final boolean hasTargetChanges = hasSinglePropertyChanges(FAKE_DESCRIPTION, baseDescription /* must be compared against the base */,
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

	private boolean hasDescriptionChanges(final SnomedConcept baseConcept, final SnomedConcept sourceConcept, final SnomedConcept targetConcept,
			final String basePath) {

		final boolean sourceChangedActiveStatus = baseConcept.isActive() ^ sourceConcept.isActive();
		final boolean targetChangedActiveStatus = baseConcept.isActive() ^ targetConcept.isActive();
		final boolean activeStatusDiffers = sourceConcept.isActive() ^ targetConcept.isActive();

		final SnomedDescriptions baseDescriptions = getDescriptions(basePath, conceptId);
		final SnomedDescriptions sourceDescriptions = getDescriptions(parameters.getSourcePath(), conceptId);
		final SnomedDescriptions targetDescriptions = getDescriptions(parameters.getTargetPath(), conceptId);

		final Map<String, SnomedDescription> baseMap = Maps.uniqueIndex(baseDescriptions, input -> input.getId());
		final Map<String, SnomedDescription> sourceMap = Maps.uniqueIndex(sourceDescriptions, input -> input.getId());
		final Map<String, SnomedDescription> targetMap = Maps.uniqueIndex(targetDescriptions, input -> input.getId());

		final Set<String> newSourceDescriptionIds = Sets.difference(sourceMap.keySet(), baseMap.keySet());
		final Set<String> newTargetDescriptionIds = Sets.difference(targetMap.keySet(), baseMap.keySet());

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

		final Set<String> allDescriptionIds = Sets.union(baseMap.keySet(), Sets.union(sourceMap.keySet(), targetMap.keySet()));

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
			final SnomedConcept targetConcept, final String basePath) {

		final boolean sourceChangedActiveStatus = baseConcept.isActive() ^ sourceConcept.isActive();
		final boolean targetChangedActiveStatus = baseConcept.isActive() ^ targetConcept.isActive();
		final boolean activeStatusDiffers = sourceConcept.isActive() ^ targetConcept.isActive();

		final SnomedRelationships baseRelationships = getUnpublishedNonInferredRelationships(basePath, conceptId);
		final SnomedRelationships sourceRelationships = getUnpublishedNonInferredRelationships(parameters.getSourcePath(), conceptId);
		final SnomedRelationships targetRelationships = getUnpublishedNonInferredRelationships(parameters.getTargetPath(), conceptId);

		final Map<String, SnomedRelationship> baseMap = FluentIterable.from(baseRelationships).uniqueIndex(input -> input.getId());
		final Map<String, SnomedRelationship> sourceMap = FluentIterable.from(sourceRelationships).uniqueIndex(input -> input.getId());
		final Map<String, SnomedRelationship> targetMap = FluentIterable.from(targetRelationships).uniqueIndex(input -> input.getId());

		final Set<String> newSourceRelationshipIds = Sets.difference(sourceMap.keySet(), baseMap.keySet());
		final Set<String> newTargetRelationshipIds = Sets.difference(targetMap.keySet(), baseMap.keySet());

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

		final Set<String> allRelationshipIds = Sets.union(baseMap.keySet(), Sets.union(sourceMap.keySet(), targetMap.keySet()));

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
			final SnomedComponent targetComponent, final String... ignoredProperties) {

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

					final boolean hasSourceChanges = !Objects.equals(sourceValue, baseValue);
					final boolean hasTargetChanges = !Objects.equals(targetValue, baseValue);
					final boolean hasConflictingChanges = !Objects.equals(sourceValue, targetValue);

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

	protected IEventBus getBus() {
		return ApplicationContext.getServiceForClass(IEventBus.class);
	}

	protected ISnomedBrowserService getBrowserService() {
		return ApplicationContext.getServiceForClass(ISnomedBrowserService.class);
	}

	protected ISnomedManualConceptMergeReviewService getManualConceptMergeService() {
		return ApplicationContext.getServiceForClass(ISnomedManualConceptMergeReviewService.class);
	}

}
