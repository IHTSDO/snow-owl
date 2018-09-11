/*
 * Copyright 2018 B2i Healthcare Pte Ltd, http://b2i.sg
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

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.stream.Collectors.toSet;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.otf.owltoolkit.conversion.AxiomRelationshipConversionService;
import org.snomed.otf.owltoolkit.conversion.ConversionException;
import org.snomed.otf.owltoolkit.domain.AxiomRepresentation;
import org.snomed.otf.owltoolkit.domain.Relationship;

import com.b2international.commons.CompareUtils;
import com.b2international.commons.StringUtils;
import com.b2international.commons.http.ExtendedLocale;
import com.b2international.snowowl.core.ApplicationContext;
import com.b2international.snowowl.eventbus.IEventBus;
import com.b2international.snowowl.snomed.SnomedConstants.Concepts;
import com.b2international.snowowl.snomed.api.browser.ISnomedBrowserAxiomService;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserAxiom;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserConcept;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserRelationship;
import com.b2international.snowowl.snomed.api.impl.domain.browser.SnomedBrowserAxiom;
import com.b2international.snowowl.snomed.api.impl.domain.browser.SnomedBrowserConcept;
import com.b2international.snowowl.snomed.api.impl.domain.browser.SnomedBrowserRelationship;
import com.b2international.snowowl.snomed.api.impl.domain.browser.SnomedBrowserRelationshipTarget;
import com.b2international.snowowl.snomed.api.impl.domain.browser.SnomedBrowserRelationshipType;
import com.b2international.snowowl.snomed.common.SnomedRf2Headers;
import com.b2international.snowowl.snomed.core.domain.DefinitionStatus;
import com.b2international.snowowl.snomed.core.domain.SnomedConcept;
import com.b2international.snowowl.snomed.core.domain.SnomedConcepts;
import com.b2international.snowowl.snomed.core.domain.refset.SnomedReferenceSetMember;
import com.b2international.snowowl.snomed.core.domain.refset.SnomedReferenceSetMembers;
import com.b2international.snowowl.snomed.datastore.SnomedDatastoreActivator;
import com.b2international.snowowl.snomed.datastore.request.SnomedRequests;
import com.google.common.base.Joiner;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;

public class SnomedBrowserAxiomService implements ISnomedBrowserAxiomService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SnomedBrowserAxiomService.class);

	private final LoadingCache<Set<String>, AxiomRelationshipConversionService> conversionServiceCache = CacheBuilder.newBuilder().build(new CacheLoader<Set<String>, AxiomRelationshipConversionService>() {
		@Override
		public AxiomRelationshipConversionService load(final Set<String> ungroupedAttributeIds) throws Exception {
			LOGGER.info("Axiom relationship conversion service has been initialized with '{}'", StringUtils.truncate(Joiner.on(",").join(ungroupedAttributeIds), 200));
			return new AxiomRelationshipConversionService(ungroupedAttributeIds.stream().map(Long::valueOf).collect(toSet()));
		}
	});

	@Override
	public Collection<? extends ISnomedBrowserConcept> expandAxioms(final Collection<? extends ISnomedBrowserConcept> concepts, final String branchPath, final List<ExtendedLocale> locales) {

		final Set<String> conceptIds = concepts.stream().map(ISnomedBrowserConcept::getConceptId).collect(Collectors.toSet());

		final SnomedReferenceSetMembers axiomMembers = SnomedRequests.prepareSearchMember()
				.all()
				.filterByRefSet(Sets.newHashSet(Concepts.REFSET_OWL_AXIOM))
				.filterByReferencedComponent(conceptIds)
				.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
				.execute(getBus())
				.getSync();

		if (axiomMembers.getItems().isEmpty()) {
			return concepts;
		}

		final AxiomRelationshipConversionService conversionService = getConversionService(branchPath);

		final Multimap<String, SnomedReferenceSetMember> membersByReferencedComponentId = Multimaps.index(axiomMembers, input -> input.getReferencedComponentId());

		final Set<String> idsToFetch = newHashSet();

		for (final ISnomedBrowserConcept concept : concepts) {

			final List<ISnomedBrowserAxiom> additionalAxioms = newArrayList();
			final List<ISnomedBrowserAxiom> gciAxioms = newArrayList();

			final Collection<SnomedReferenceSetMember> conceptAxiomMembers = membersByReferencedComponentId.get(concept.getId());

			if (!CompareUtils.isEmpty(conceptAxiomMembers)) {

				try {

					for (final SnomedReferenceSetMember axiomMember : conceptAxiomMembers) {

						final String owlExpression = (String) axiomMember.getProperties().get(SnomedRf2Headers.FIELD_OWL_EXPRESSION);
						final Long conceptId = Long.valueOf(concept.getId());
						final AxiomRepresentation axiomRepresentation = conversionService.convertAxiomToRelationships(conceptId, owlExpression);

						if (axiomRepresentation != null) {// Will be null if the axiom is an Ontology Axiom for example a property chain or transitive axiom rather than an Additional Axiom or GCI.
							if (conceptId.equals(axiomRepresentation.getLeftHandSideNamedConcept())) {
								// Concept ID on the left means it's an additional axiom
								additionalAxioms.add(convertToBrowserAxiom(axiomMember, axiomRepresentation.isPrimitive(), axiomRepresentation.getRightHandSideRelationships()));
							} else if (conceptId.equals(axiomRepresentation.getRightHandSideNamedConcept())) {
								// Concept ID on the right means it's a GCI axiom
								gciAxioms.add(convertToBrowserAxiom(axiomMember, axiomRepresentation.isPrimitive(), axiomRepresentation.getLeftHandSideRelationships()));
							}
						}
					}

				} catch (ConversionException | NumberFormatException e) {
					LOGGER.error("Failed to deserialise concept axioms for conept {}.", concept.getId(), e);
				}

			}

			if (!additionalAxioms.isEmpty()) {
				((SnomedBrowserConcept) concept).setAdditionalAxioms(additionalAxioms);
				additionalAxioms.stream()
					.map(axiom -> axiom.getRelationships())
					.flatMap(List::stream)
					.forEach(relationship -> {
						idsToFetch.add(relationship.getType().getConceptId());
						idsToFetch.add(relationship.getTarget().getConceptId());
					});
			}

			if (!gciAxioms.isEmpty()) {
				((SnomedBrowserConcept) concept).setGciAxioms(gciAxioms);
				gciAxioms.stream()
					.map(axiom -> axiom.getRelationships())
					.flatMap(List::stream)
					.forEach(relationship -> {
						idsToFetch.add(relationship.getType().getConceptId());
						idsToFetch.add(relationship.getTarget().getConceptId());
					});
			}

		}

		if (!idsToFetch.isEmpty()) {

			final SnomedConcepts conceptsFromStore = SnomedRequests.prepareSearchConcept()
				.all()
				.setLocales(locales)
				.setExpand("fsn()")
				.filterByIds(idsToFetch)
				.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
				.execute(getBus())
				.getSync();

			final Map<String, SnomedConcept> idToConceptMap = conceptsFromStore.getItems().stream()
					.collect(Collectors.toMap(SnomedConcept::getId, concept -> concept));

			for (final ISnomedBrowserConcept concept : concepts) {

				concept.getAdditionalAxioms().stream()
					.map(axiom -> axiom.getRelationships())
					.flatMap(List::stream)
					.forEach( relationship -> updateRelationshipAttributes(relationship, idToConceptMap, branchPath));

				concept.getGciAxioms().stream()
					.map(axiom -> axiom.getRelationships())
					.flatMap(List::stream)
					.forEach( relationship -> updateRelationshipAttributes(relationship, idToConceptMap, branchPath));

			}

		}

		return concepts;
	}

	@Override
	public AxiomRelationshipConversionService getConversionService(final String branchPath) {
		final SnomedReferenceSetMembers mrcmMembers = SnomedRequests.prepareSearchMember()
				.all()
				.filterByRefSet(Concepts.REFSET_MRCM_ATTRIBUTE_DOMAIN_INTERNATIONAL)
				.filterByActive(true)
				.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
				.execute(getBus())
				.getSync();

		final Set<String> ungroupedAttributes = mrcmMembers.getItems().stream()
			.filter(member -> !(Boolean) member.getProperties().get(SnomedRf2Headers.FIELD_MRCM_GROUPED))
			.map(member -> member.getReferencedComponentId())
			.collect(toSet());

		return conversionServiceCache.getUnchecked(ungroupedAttributes);
	}

	private void updateRelationshipAttributes(final ISnomedBrowserRelationship relationship, final Map<String, SnomedConcept> idToConceptMap, final String branchPath) {

		final SnomedBrowserRelationshipType type = (SnomedBrowserRelationshipType) relationship.getType();

		if (idToConceptMap.containsKey(type.getConceptId())) {

			final SnomedConcept typeConcept = idToConceptMap.get(type.getConceptId());
			type.setFsn(typeConcept.getFsn() == null ? type.getConceptId() : typeConcept.getFsn().getTerm());

		} else {
			LOGGER.warn("Concept {} used in axiom attribute type but does not exist on this branch {}.", type.getConceptId(), branchPath);
		}

		final SnomedBrowserRelationshipTarget target = (SnomedBrowserRelationshipTarget) relationship.getTarget();

		if (idToConceptMap.containsKey(target.getConceptId())) {

			final SnomedConcept targetConcept = idToConceptMap.get(target.getConceptId());
			target.setFsn(targetConcept.getFsn() == null ? target.getConceptId() : targetConcept.getFsn().getTerm());

			target.setActive(targetConcept.isActive());
			target.setEffectiveTime(targetConcept.getEffectiveTime());
			target.setReleased(targetConcept.isReleased());
			target.setModuleId(targetConcept.getModuleId());
			target.setDefinitionStatus(targetConcept.getDefinitionStatus());

		} else {
			LOGGER.warn("Concept {} used in axiom attribute target but does not exist on this branch {}.", target.getConceptId(), branchPath);
		}

	}

	private IEventBus getBus() {
		return ApplicationContext.getServiceForClass(IEventBus.class);
	}

	private ISnomedBrowserAxiom convertToBrowserAxiom(final SnomedReferenceSetMember axiomMember, final boolean primitive, final Map<Integer, List<Relationship>> relationships) {

		final SnomedBrowserAxiom browserAxiom = new SnomedBrowserAxiom();
		browserAxiom.setAxiomId(axiomMember.getId());
		browserAxiom.setActive(axiomMember.isActive());
		browserAxiom.setEffectiveTime(axiomMember.getEffectiveTime());
		browserAxiom.setModuleId(axiomMember.getModuleId());
		browserAxiom.setReleased(axiomMember.isReleased());
		browserAxiom.setReferencedComponentId(axiomMember.getReferencedComponentId());
		browserAxiom.setDefinitionStatus(primitive ? DefinitionStatus.PRIMITIVE : DefinitionStatus.FULLY_DEFINED);
		browserAxiom.setRelationships(convertToBrowserRelationships(relationships, axiomMember.isActive()));

		return browserAxiom;
	}

	private List<ISnomedBrowserRelationship> convertToBrowserRelationships(final Map<Integer, List<Relationship>> relationships, final boolean active) {
		return relationships.values().stream()
				.flatMap(List::stream)
				.map(relationship -> {

					final SnomedBrowserRelationship browserRelationship = new SnomedBrowserRelationship();
					browserRelationship.setActive(active);
					browserRelationship.setGroupId(relationship.getGroup());
					browserRelationship.setType(new SnomedBrowserRelationshipType(String.valueOf(relationship.getTypeId())));
					browserRelationship.setTarget(new SnomedBrowserRelationshipTarget(String.valueOf(relationship.getDestinationId())));

					return browserRelationship;

				}).collect(Collectors.toList());
	}

}
