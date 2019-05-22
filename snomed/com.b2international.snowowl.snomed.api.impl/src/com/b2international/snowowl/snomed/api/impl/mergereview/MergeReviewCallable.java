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

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Function;

import com.b2international.commons.ReflectionUtils;
import com.b2international.index.revision.RevisionIndex;
import com.b2international.snowowl.core.ApplicationContext;
import com.b2international.snowowl.core.api.IBranchPath;
import com.b2international.snowowl.core.exceptions.NotFoundException;
import com.b2international.snowowl.datastore.BranchPathUtils;
import com.b2international.snowowl.eventbus.IEventBus;
import com.b2international.snowowl.snomed.SnomedConstants.Concepts;
import com.b2international.snowowl.snomed.api.browser.ISnomedBrowserService;
import com.b2international.snowowl.snomed.api.mergereview.ISnomedManualConceptMergeReviewService;
import com.b2international.snowowl.snomed.common.SnomedRf2Headers;
import com.b2international.snowowl.snomed.core.domain.SnomedComponent;
import com.b2international.snowowl.snomed.core.domain.SnomedConcept;
import com.b2international.snowowl.snomed.core.domain.SnomedCoreComponent;
import com.b2international.snowowl.snomed.core.domain.SnomedDescription;
import com.b2international.snowowl.snomed.core.domain.SnomedRelationship;
import com.b2international.snowowl.snomed.core.domain.refset.SnomedReferenceSetMember;
import com.b2international.snowowl.snomed.datastore.SnomedDatastoreActivator;
import com.b2international.snowowl.snomed.datastore.request.SnomedRequests;
import com.b2international.snowowl.snomed.snomedrefset.SnomedRefSetType;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

/**
 * @since 4.5
 */
public abstract class MergeReviewCallable<T> implements Callable<T> {

	private static final Set<String> CONCEPT_FIELDS = ImmutableSet.<String>of("moduleId", "effectiveTime", "definitionStatus");

	private static final Set<String> DESCRIPTION_FIELDS = ImmutableSet.<String>of("moduleId", "active", "effectiveTime", "typeId", "term",
			"languageCode", "conceptId", "caseSignificance", "acceptabilityMap");

	private static final Set<String> RELATIONSHIP_FIELDS = ImmutableSet.<String>of("moduleId", "active", "effectiveTime", "sourceId", "typeId",
			"destinationId", "characteristicTypeId", "destinationNegated", "group", "modifierId", "unionGroup");

	private static final Set<String> MEMBER_FIELDS = ImmutableSet.<String>of("moduleId", "active", "effectiveTime", "referencedComponentId", "referenceSetId");

	private static final Multimap<SnomedRefSetType, String> REFERENCE_SET_MEMBER_FIELDS_TO_COMPARE = ImmutableMultimap.<SnomedRefSetType, String>builder()
			.put(SnomedRefSetType.OWL_AXIOM, SnomedRf2Headers.FIELD_OWL_EXPRESSION)
			.put(SnomedRefSetType.LANGUAGE, SnomedRf2Headers.FIELD_ACCEPTABILITY_ID)
			.put(SnomedRefSetType.ASSOCIATION, SnomedRf2Headers.FIELD_TARGET_COMPONENT)
			.put(SnomedRefSetType.ATTRIBUTE_VALUE, SnomedRf2Headers.FIELD_VALUE_ID)
			.build();

	private static final String FAKE_ID = "FAKE_ID";
	private static final SnomedConcept FAKE_CONCEPT = new SnomedConcept(FAKE_ID);
	private static final SnomedDescription FAKE_DESCRIPTION = new SnomedDescription(FAKE_ID);
	private static final SnomedRelationship FAKE_RELATIONSHIP = new SnomedRelationship(FAKE_ID);
	private static final SnomedReferenceSetMember FAKE_MEMBER = new SnomedReferenceSetMember(FAKE_ID);

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

		if (hasConflictingConceptChanges(baseConcept, sourceConcept, targetConcept)) {
			return onSuccess();
		}

		return onSkip();
	}

	private boolean hasConflictingConceptChanges(final SnomedConcept baseConcept, final SnomedConcept sourceConcept, final SnomedConcept targetConcept) {

		if (baseConcept != null) {

			if (sourceConcept == null && targetConcept != null) { // if source has been deleted while target has changed

				if (hasConceptChanges(baseConcept, targetConcept, true)) {
					return true;
				}

			} else if (sourceConcept != null && targetConcept == null) { // if target has been deleted while source has changed

				if (hasConceptChanges(baseConcept, sourceConcept, true)) {
					return true;
				}

			} else if (sourceConcept != null && targetConcept != null) {

				// if there was an inactivation on one side and a change on the other

				if (sourceConcept.isActive() ^ targetConcept.isActive()) { // if active status is different

					final boolean sourceChangedActiveStatus = baseConcept.isActive() ^ sourceConcept.isActive();
					final boolean targetHasChanges = hasConceptChanges(baseConcept, targetConcept, true);

					if (sourceChangedActiveStatus && targetHasChanges) {
						return true;
					}

					final boolean targetChangedActiveStatus = baseConcept.isActive() ^ targetConcept.isActive();
					final boolean sourceHasChanges = hasConceptChanges(baseConcept, sourceConcept, true);

					if (targetChangedActiveStatus && sourceHasChanges) {
						return true;
					}

				} else { // both side has the same status

					if (hasConceptChanges(baseConcept, sourceConcept, targetConcept)) {
						return true;
					} else if (hasConceptChanges(baseConcept, sourceConcept, false) && hasConceptChanges(baseConcept, targetConcept, false)) {
						return true;
					}

				}

			}

			// if the same new concept was added on both sides, raise merge review anyway
		} else if (sourceConcept != null && targetConcept != null) {
			return true;
		}

		return false;
	}

	/**
	 * If target concept has changes compared to source or base
	 */
	private boolean hasConceptChanges(final SnomedConcept sourceConcept, final SnomedConcept targetConcept, final boolean includeDescriptionChanges) {

		// check difference of simple concept properties
		if (hasSinglePropertyChanges(sourceConcept, targetConcept)) {
			return true;
		}

		if (includeDescriptionChanges) {

			// check difference of concept descriptions, including reference set members of descriptions as well (language, association and attribute value types)
			if (hasComponentChanges(getDescriptions(sourceConcept), getDescriptions(targetConcept), SnomedDescription.class)) {
				return true;
			}

		}

		// check difference of concept stated relationships
		if (hasComponentChanges(getStatedRelationships(sourceConcept), getStatedRelationships(targetConcept), SnomedRelationship.class)) {
			return true;
		}

		// check difference of concept reference set members, including association, attribute value and OWL axiom types
		if (hasComponentChanges(getReferenceSetMembers(sourceConcept), getReferenceSetMembers(targetConcept), SnomedReferenceSetMember.class)) {
			return true;
		}

		// check conflicting FSN changes
		if (hasFsnOrPtChanges(sourceConcept.getFsn(), targetConcept.getFsn())) {
			return true;
		}

		// check conflicting PT changes
		if (hasFsnOrPtChanges(sourceConcept.getPt(), targetConcept.getPt())) {
			return true;
		}

		return false;
	}

	/**
	 * If there are concept changes using a three-way compare (base-to-source, base-to-target, source-to-target)
	 */
	private boolean hasConceptChanges(final SnomedConcept baseConcept, final SnomedConcept sourceConcept, final SnomedConcept targetConcept) {

		// check difference of simple concept properties
		if (hasSinglePropertyChanges(baseConcept, sourceConcept, targetConcept)) {
			return true;
		}

		// check difference of concept descriptions, including reference set members of descriptions as well (language, association and attribute value types)
		if (hasComponentChanges(getDescriptions(baseConcept), getDescriptions(sourceConcept), getDescriptions(targetConcept), SnomedDescription.class)) {
			return true;
		}

		// check difference of concept stated relationships
		if (hasComponentChanges(getStatedRelationships(baseConcept), getStatedRelationships(sourceConcept), getStatedRelationships(targetConcept),
				SnomedRelationship.class)) {
			return true;
		}

		// check difference of concept reference set members, including association, attribute value and OWL axiom types
		if (hasComponentChanges(getReferenceSetMembers(baseConcept), getReferenceSetMembers(sourceConcept), getReferenceSetMembers(targetConcept), SnomedReferenceSetMember.class)) {
			return true;
		}

		// check conflicting FSN changes
		if (hasFsnOrPtChanges(baseConcept.getFsn(), sourceConcept.getFsn(), targetConcept.getFsn())) {
			return true;
		}

		// check conflicting PT changes
		if (hasFsnOrPtChanges(baseConcept.getPt(), sourceConcept.getPt(), targetConcept.getPt())) {
			return true;
		}

		return false;
	}

	private <U extends SnomedComponent> boolean hasComponentChanges(final U sourceComponent, final U targetComponent) {

		if (hasSinglePropertyChanges(sourceComponent, targetComponent)) {
			return true;
		}

		// check reference set member changes
		if (hasReferenceSetMemberChanges(sourceComponent, targetComponent)) {
			return true;
		}

		return false;
	}

	private <U extends SnomedComponent> boolean hasComponentChanges(final Collection<U> sourceComponents, final Collection<U> targetComponents, final Class<U> clazz) {

		if (sourceComponents.isEmpty() && targetComponents.isEmpty()) {
			return false;
		}

		final Map<String, U> sourceComponentsMap = sourceComponents.stream().collect(toMap(SnomedComponent::getId, Function.identity()));
		final Map<String, U> targetComponentsMap = targetComponents.stream().collect(toMap(SnomedComponent::getId, Function.identity()));

		final Set<String> newComponentIds = Sets.difference(targetComponentsMap.keySet(), sourceComponentsMap.keySet());

		// there are additions compared to source / base
		if (!newComponentIds.isEmpty()) {
			return true;
		}

		final Set<String> deletedComponentIds = Sets.difference(sourceComponentsMap.keySet(), targetComponentsMap.keySet());

		// there are deletions compared to source / base
		if (!deletedComponentIds.isEmpty()) {
			return true;
		}

		// source and target key set must be equivalent at this point
		for (final String id : sourceComponentsMap.keySet()) {

			final U sourceComponent = sourceComponentsMap.get(id);
			final U targetComponent = targetComponentsMap.get(id);

			if (hasComponentChanges(sourceComponent, targetComponent)) {
				return true;
			}

		}

		return false;
	}

	private <U extends SnomedComponent> boolean hasComponentChanges(final Collection<U> baseComponents, final Collection<U> sourceComponents, final Collection<U> targetComponents, final Class<U> clazz) {

		if (baseComponents.isEmpty() && sourceComponents.isEmpty() && targetComponents.isEmpty()) {
			return false;
		}

		final Map<String, U> baseMap = baseComponents.stream().collect(toMap(SnomedComponent::getId, Function.identity()));
		final Map<String, U> sourceMap = sourceComponents.stream().collect(toMap(SnomedComponent::getId, Function.identity()));
		final Map<String, U> targetMap = targetComponents.stream().collect(toMap(SnomedComponent::getId, Function.identity()));

		final Set<String> newSourceIds = Sets.difference(sourceMap.keySet(), baseMap.keySet());
		final Set<String> newTargetIds = Sets.difference(targetMap.keySet(), baseMap.keySet());
		final Set<String> deletedSourceIds = Sets.difference(baseMap.keySet(), sourceMap.keySet());
		final Set<String> deletedTargetIds = Sets.difference(baseMap.keySet(), targetMap.keySet());

		// show merge screen when there are different additions on both sides
		if (!Sets.difference(newSourceIds, newTargetIds).isEmpty() && !newTargetIds.isEmpty()) {
			return true;
		}

		if (!Sets.difference(newTargetIds, newSourceIds).isEmpty() && !newSourceIds.isEmpty()) {
			return true;
		}

		// show merge screen when there are different deletions on both sides
		if (!Sets.difference(deletedSourceIds, deletedTargetIds).isEmpty() && !deletedTargetIds.isEmpty()) {
			return true;
		}

		if (!Sets.difference(deletedTargetIds, deletedSourceIds).isEmpty() && !deletedSourceIds.isEmpty()) {
			return true;
		}

		// show merge screen when there is an addition and a deletion at the same time
		if (!newSourceIds.isEmpty() && !deletedTargetIds.isEmpty()) {
			return true;
		}

		if (!newTargetIds.isEmpty() && !deletedSourceIds.isEmpty()) {
			return true;
		}

		final Set<String> allComponentIds = Sets.union(baseMap.keySet(), Sets.union(sourceMap.keySet(), targetMap.keySet()));

		for (final String id : allComponentIds) {

			final U sourceComponent = sourceMap.get(id);
			final U targetComponent = targetMap.get(id);

			if (!baseMap.containsKey(id)) {

				// the same component was added on both sides -> two way compare
				if (sourceMap.containsKey(id) && targetMap.containsKey(id)) {

					if (hasComponentChanges(sourceComponent, targetComponent)) {
						return true;
					}

				}

			} else {

				final U baseComponent = baseMap.get(id);

				if (sourceMap.containsKey(id) && targetMap.containsKey(id)) {

					if (sourceComponent.isActive() ^ targetComponent.isActive()) { // if active status is different

						final boolean sourceChangedActiveStatus = baseComponent.isActive() ^ sourceComponent.isActive();
						final boolean targetHasChanges = hasComponentChanges(baseComponent, targetComponent);

						if (sourceChangedActiveStatus && targetHasChanges) {
							return true;
						}

						final boolean targetChangedActiveStatus = baseComponent.isActive() ^ targetComponent.isActive();
						final boolean sourceHasChanges = hasComponentChanges(baseComponent, sourceComponent);

						if (targetChangedActiveStatus && sourceHasChanges) {
							return true;
						}

					} else {

						if (hasSinglePropertyChanges(baseComponent, sourceComponent, targetComponent)) {
							return true;
						}

						// if these are components with reference set members then compare member equivalence as well
						if (hasReferenceSetMemberChanges(baseComponent, sourceComponent, targetComponent)) {
							return true;
						}

					}

				} else if (sourceMap.containsKey(id)) {

					// if there was a change on one side and a deletion on the other
					// if these are components with reference set members then compare member equivalence as well
					if (hasComponentChanges(baseComponent, sourceComponent)) {
						return true;
					}

				} else if (targetMap.containsKey(id)) {

					// if there was a change on one side and a deletion on the other
					// if these are components with reference set members then compare member equivalence as well
					if (hasComponentChanges(baseComponent, targetComponent)) {
						return true;
					}

				}

			}

		}

		return false;
	}

	private boolean hasReferenceSetMemberChanges(final SnomedComponent sourceComponent, final SnomedComponent targetComponent) {

		if (sourceComponent instanceof SnomedCoreComponent && targetComponent instanceof SnomedCoreComponent) {

			final SnomedCoreComponent sourceCoreComponent = (SnomedCoreComponent) sourceComponent;
			final SnomedCoreComponent targetCoreComponent = (SnomedCoreComponent) targetComponent;

			if (hasComponentChanges(getReferenceSetMembers(sourceCoreComponent), getReferenceSetMembers(targetCoreComponent), SnomedReferenceSetMember.class)) {
				return true;
			}

		} else if (sourceComponent instanceof SnomedReferenceSetMember && targetComponent instanceof SnomedReferenceSetMember) {

			final SnomedReferenceSetMember sourceMember = (SnomedReferenceSetMember) sourceComponent;
			final SnomedReferenceSetMember targetMember = (SnomedReferenceSetMember) targetComponent;

			// raise merge review if id is the same but type is different
			if (sourceMember.type() != targetMember.type()) {
				return true;
			}

			if (hasConflictingMemberPropertyChanges(sourceMember, targetMember)) {
				return true;
			}

		}

		return false;
	}

	private boolean hasReferenceSetMemberChanges(final SnomedComponent baseComponent, final SnomedComponent sourceComponent, final SnomedComponent targetComponent) {

		if (baseComponent instanceof SnomedCoreComponent && sourceComponent instanceof SnomedCoreComponent && targetComponent instanceof SnomedCoreComponent) {

			final SnomedCoreComponent baseCoreComponent = (SnomedCoreComponent) baseComponent;
			final SnomedCoreComponent sourceCoreComponent = (SnomedCoreComponent) sourceComponent;
			final SnomedCoreComponent targetCoreComponent = (SnomedCoreComponent) targetComponent;

			if (hasComponentChanges(getReferenceSetMembers(baseCoreComponent), getReferenceSetMembers(sourceCoreComponent), getReferenceSetMembers(targetCoreComponent), SnomedReferenceSetMember.class)) {
				return true;
			}

		} else if (baseComponent instanceof SnomedReferenceSetMember && sourceComponent instanceof SnomedReferenceSetMember && targetComponent instanceof SnomedReferenceSetMember) {

			final SnomedReferenceSetMember baseMember = (SnomedReferenceSetMember) baseComponent;
			final SnomedReferenceSetMember sourceMember = (SnomedReferenceSetMember) sourceComponent;
			final SnomedReferenceSetMember targetMember = (SnomedReferenceSetMember) targetComponent;

			// raise merge review if id is the same but type is different
			if (ImmutableSet.of(baseMember.type(), sourceMember.type(), targetMember.type()).size() != 1) {
				return true;
			}

			if (hasConflictingMemberPropertyChanges(baseMember, sourceMember, targetMember)) {
				return true;
			}

		}

		return false;
	}

	private boolean hasConflictingMemberPropertyChanges(final SnomedReferenceSetMember sourceMember, final SnomedReferenceSetMember targetMember) {

		// make sure this is a supported member type
		if (REFERENCE_SET_MEMBER_FIELDS_TO_COMPARE.containsKey(sourceMember.type())) {

			// iterate over all specified reference set member properties and compare them
			for (final String property : REFERENCE_SET_MEMBER_FIELDS_TO_COMPARE.get(sourceMember.type())) {

				String sourceProperty;
				String targetProperty;

				if (SnomedRf2Headers.FIELD_TARGET_COMPONENT.equals(property)) {

					sourceProperty = ((SnomedComponent) sourceMember.getProperties().get(property)).getId();
					targetProperty = ((SnomedComponent) targetMember.getProperties().get(property)).getId();

				} else {

					sourceProperty = (String) sourceMember.getProperties().get(property);
					targetProperty = (String) targetMember.getProperties().get(property);

				}


				if (!Strings.isNullOrEmpty(sourceProperty) && !Strings.isNullOrEmpty(targetProperty)) {

					// if value of property does not match, raise merge screen
					if (!Objects.equals(sourceProperty, targetProperty)) {
						return true;
					}

				} else if (!Strings.isNullOrEmpty(sourceProperty) || !Strings.isNullOrEmpty(targetProperty)) {
					// if either of source or target's property is set but the other is not
					return true;
				}

			}

		}

		return false;
	}

	private boolean hasConflictingMemberPropertyChanges(final SnomedReferenceSetMember baseMember, final SnomedReferenceSetMember sourceMember, final SnomedReferenceSetMember targetMember) {

		// make sure this is a supported member type
		if (REFERENCE_SET_MEMBER_FIELDS_TO_COMPARE.containsKey(sourceMember.type())) {

			// iterate over all specified reference set member properties and compare them
			for (final String property : REFERENCE_SET_MEMBER_FIELDS_TO_COMPARE.get(sourceMember.type())) {

				String baseProperty;
				String sourceProperty;
				String targetProperty;

				if (SnomedRf2Headers.FIELD_TARGET_COMPONENT.equals(property)) {

					baseProperty = ((SnomedComponent) baseMember.getProperties().get(property)).getId();
					sourceProperty = ((SnomedComponent) sourceMember.getProperties().get(property)).getId();
					targetProperty = ((SnomedComponent) targetMember.getProperties().get(property)).getId();

				} else {

					baseProperty = (String) baseMember.getProperties().get(property);
					sourceProperty = (String) sourceMember.getProperties().get(property);
					targetProperty = (String) targetMember.getProperties().get(property);

				}


				if (Strings.isNullOrEmpty(baseProperty)) {

					if (!Strings.isNullOrEmpty(sourceProperty) && !Strings.isNullOrEmpty(targetProperty)) {

						if (!Objects.equals(sourceProperty, targetProperty)) {
							return true;
						}

					}

				} else {

					if (!Strings.isNullOrEmpty(sourceProperty) && !Strings.isNullOrEmpty(targetProperty)) {

						final boolean hasSourceChanges = !Objects.equals(baseProperty, sourceProperty);
						final boolean hasTargetChanges = !Objects.equals(baseProperty, targetProperty);
						final boolean hasConflictingChanges = !Objects.equals(sourceProperty, targetProperty);

						if (hasSourceChanges && hasTargetChanges && hasConflictingChanges) {
							return true;
						}

					} else if (!Strings.isNullOrEmpty(sourceProperty)) {

						if (!Objects.equals(baseProperty, sourceProperty)) {
							return true;
						}

					} else if (!Strings.isNullOrEmpty(targetProperty)) {

						if (!Objects.equals(baseProperty, targetProperty)) {
							return true;
						}

					}

				}

			}

		}

		return false;
	}

	private boolean hasFsnOrPtChanges(final SnomedDescription sourceDescription, final SnomedDescription targetDescription) {

		if (sourceDescription != null && targetDescription != null) {

			if (!sourceDescription.getId().equals(targetDescription.getId())) {
				return true;
			} else if (hasSinglePropertyChanges(sourceDescription, targetDescription)) {
				return true;
			}

		} else if (sourceDescription != null) {
			return true;
		} else if (targetDescription != null) {
			return true;
		}

		return false;
	}

	private boolean hasFsnOrPtChanges(final SnomedDescription baseDescription, final SnomedDescription sourceDescription, final SnomedDescription targetDescription) {

		if (baseDescription != null) {

			if (sourceDescription != null && targetDescription != null) {

				final Set<String> ids = ImmutableSet.of(baseDescription.getId(), sourceDescription.getId(), targetDescription.getId());

				if (ids.size() == 1) {

					if (hasSinglePropertyChanges(baseDescription, sourceDescription, targetDescription)) {
						return true;
					}

				} else if (hasFsnOrPtChanges(baseDescription, sourceDescription)
						&& hasFsnOrPtChanges(baseDescription, targetDescription)
						&& hasFsnOrPtChanges(sourceDescription, targetDescription)) {
					return true;
				}

			} else if (sourceDescription != null) {

				// if target was deleted and source has changed
				if (hasFsnOrPtChanges(baseDescription, sourceDescription)) {
					return true;
				}

			} else if (targetDescription != null) {

				// if source was deleted and target has changed
				if (hasFsnOrPtChanges(baseDescription, targetDescription)) {
					return true;
				}

			}

			// if there are two new additions on source and target, compare them against each other
		} else if (sourceDescription != null && targetDescription != null) {

			if (hasFsnOrPtChanges(sourceDescription, targetDescription)) {
				return true;
			}

		}

		return false;
	}

	private SnomedConcept getConcept(final String path, final String conceptId) {

		try {

			return SnomedRequests.prepareGetConcept(conceptId)
					.setExpand("fsn(),pt(),descriptions(expand(members())),relationships(), members()")
					.setLocales(parameters.getExtendedLocales())
					.build(SnomedDatastoreActivator.REPOSITORY_UUID, path)
					.execute(getBus())
					.getSync();

		} catch (final NotFoundException e) {
			return null;
		}
	}

	private Collection<SnomedRelationship> getStatedRelationships(final SnomedConcept concept) {
		return concept.getRelationships().stream()
				.filter(relationship -> Concepts.STATED_RELATIONSHIP.equals(relationship.getCharacteristicTypeId()))
				.collect(toSet());
	}

	private Collection<SnomedDescription> getDescriptions(final SnomedConcept concept) {
		return concept.getDescriptions().getItems();
	}

	private Collection<SnomedReferenceSetMember> getReferenceSetMembers(final SnomedCoreComponent component) {
		if (component.getMembers() == null) {
			return emptySet();
		}
		return component.getMembers().stream()
				.filter(member -> REFERENCE_SET_MEMBER_FIELDS_TO_COMPARE.containsKey(member.type()))
				.collect(toSet());
	}

	private <U extends SnomedComponent> Set<String> getComponentFields(final Class<U> clazz) {
		if (clazz.isAssignableFrom(SnomedConcept.class)) {
			return CONCEPT_FIELDS;
		} else if (clazz.isAssignableFrom(SnomedDescription.class)) {
			return DESCRIPTION_FIELDS;
		} else if (clazz.isAssignableFrom(SnomedRelationship.class)) {
			return RELATIONSHIP_FIELDS;
		} else if (clazz.isAssignableFrom(SnomedReferenceSetMember.class)) {
			return MEMBER_FIELDS;
		}
		throw new RuntimeException("Unsupported type: " + clazz);
	}

	private <U extends SnomedComponent> SnomedComponent getFakeComponent(final Class<U> clazz) {
		if (clazz.isAssignableFrom(SnomedConcept.class)) {
			return FAKE_CONCEPT;
		} else if (clazz.isAssignableFrom(SnomedDescription.class)) {
			return FAKE_DESCRIPTION;
		} else if (clazz.isAssignableFrom(SnomedRelationship.class)) {
			return FAKE_RELATIONSHIP;
		} else if (clazz.isAssignableFrom(SnomedReferenceSetMember.class)) {
			return FAKE_MEMBER;
		}
		throw new RuntimeException("Unsupported type: " + clazz);
	}

	private boolean hasSinglePropertyChanges(final SnomedComponent sourceComponent, final SnomedComponent targetComponent) {
		return hasSinglePropertyChanges(getFakeComponent(sourceComponent.getClass()), sourceComponent, targetComponent);
	}

	private boolean hasSinglePropertyChanges(final SnomedComponent baseComponent, final SnomedComponent sourceComponent, final SnomedComponent targetComponent) {

		for (final String property : getComponentFields(sourceComponent.getClass())) {

			Object baseValue = ReflectionUtils.getPropertyValue(baseComponent, property);
			Object sourceValue = ReflectionUtils.getPropertyValue(sourceComponent, property);
			Object targetValue = ReflectionUtils.getPropertyValue(targetComponent, property);

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

				final boolean hasSourceChanges = !Objects.equals(sourceValue, baseValue);
				final boolean hasTargetChanges = !Objects.equals(targetValue, baseValue);
				final boolean hasConflictingChanges = !Objects.equals(sourceValue, targetValue);

				if (hasSourceChanges && hasTargetChanges && hasConflictingChanges) {
					return true;
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
