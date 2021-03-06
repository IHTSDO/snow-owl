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
import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.otf.owltoolkit.conversion.ConversionException;
import org.snomed.otf.owltoolkit.domain.AxiomRepresentation;
import org.snomed.otf.owltoolkit.domain.Relationship;

import com.b2international.commons.CompareUtils;
import com.b2international.commons.http.ExtendedLocale;
import com.b2international.snowowl.core.ApplicationContext;
import com.b2international.snowowl.datastore.server.snomed.SnomedBrowserAxiomConverter;
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
import com.b2international.snowowl.snomed.api.impl.domain.browser.SnomedBrowserTerm;
import com.b2international.snowowl.snomed.common.SnomedRf2Headers;
import com.b2international.snowowl.snomed.core.domain.CharacteristicType;
import com.b2international.snowowl.snomed.core.domain.DefinitionStatus;
import com.b2international.snowowl.snomed.core.domain.SnomedConcept;
import com.b2international.snowowl.snomed.core.domain.SnomedConcepts;
import com.b2international.snowowl.snomed.core.domain.refset.SnomedReferenceSetMember;
import com.b2international.snowowl.snomed.core.domain.refset.SnomedReferenceSetMembers;
import com.b2international.snowowl.snomed.datastore.SnomedDatastoreActivator;
import com.b2international.snowowl.snomed.datastore.request.SnomedRequests;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;

public class SnomedBrowserAxiomService implements ISnomedBrowserAxiomService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SnomedBrowserAxiomService.class);
	
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

		final Multimap<String, SnomedReferenceSetMember> membersByReferencedComponentId = Multimaps.index(axiomMembers, input -> input.getReferencedComponentId());

		final Set<String> idsToFetch = newHashSet();

		for (final ISnomedBrowserConcept concept : concepts) {

			final List<ISnomedBrowserAxiom> classAxioms = newArrayList();
			final List<ISnomedBrowserAxiom> gciAxioms = newArrayList();

			final Collection<SnomedReferenceSetMember> conceptAxiomMembers = membersByReferencedComponentId.get(concept.getId());

			if (!CompareUtils.isEmpty(conceptAxiomMembers)) {

				try {

					for (final SnomedReferenceSetMember axiomMember : conceptAxiomMembers) {

						final String owlExpression = (String) axiomMember.getProperties().get(SnomedRf2Headers.FIELD_OWL_EXPRESSION);
						final Long conceptId = Long.valueOf(concept.getId());
						final AxiomRepresentation axiomRepresentation = convertAxiomToRelationships(owlExpression, branchPath);

						if (axiomRepresentation != null) {// Will be null if the axiom is an Ontology Axiom for example a property chain or transitive axiom rather than a Class Axiom or GCI.
							if (conceptId.equals(axiomRepresentation.getLeftHandSideNamedConcept())) {
								// Concept ID on the left means it's a class axiom
								classAxioms.add(convertToBrowserAxiom(axiomMember, axiomRepresentation.isPrimitive(), axiomRepresentation.getRightHandSideRelationships(), true));
							} else if (conceptId.equals(axiomRepresentation.getRightHandSideNamedConcept())) {
								// Concept ID on the right means it's a GCI axiom
								gciAxioms.add(convertToBrowserAxiom(axiomMember, axiomRepresentation.isPrimitive(), axiomRepresentation.getLeftHandSideRelationships(), false));
							}
						}
					}

				} catch (ConversionException e) {
					LOGGER.error("Failed to deserialise concept axioms for conept {}.", concept.getId(), e);
				}

			}

			if (!classAxioms.isEmpty()) {
				((SnomedBrowserConcept) concept).setClassAxioms(classAxioms);
				classAxioms.stream()
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
				.setExpand("fsn(),pt()")
				.filterByIds(idsToFetch)
				.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
				.execute(getBus())
				.getSync();

			final Map<String, SnomedConcept> idToConceptMap = conceptsFromStore.getItems().stream()
					.collect(Collectors.toMap(SnomedConcept::getId, concept -> concept));

			for (final ISnomedBrowserConcept concept : concepts) {

				concept.getClassAxioms().stream()
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
	public AxiomRepresentation convertAxiomToRelationships(String owlExpression, String branchPath) throws ConversionException {
		return new SnomedBrowserAxiomConverter().convertAxiomToRelationships(owlExpression, branchPath);
	}
	
	@Override
	public String convertRelationshipsToAxiom(AxiomRepresentation axiomRepresentation, String branchPath) {
		return new SnomedBrowserAxiomConverter().convertRelationshipsToAxiom(axiomRepresentation, branchPath);
	}
	
	private void updateRelationshipAttributes(final ISnomedBrowserRelationship relationship, final Map<String, SnomedConcept> idToConceptMap, final String branchPath) {

		final SnomedBrowserRelationshipType type = (SnomedBrowserRelationshipType) relationship.getType();

		if (idToConceptMap.containsKey(type.getConceptId())) {

			final SnomedConcept typeConcept = idToConceptMap.get(type.getConceptId());
			type.setFsn(typeConcept.getFsn() == null ? new SnomedBrowserTerm(type.getConceptId()) : new SnomedBrowserTerm(typeConcept.getFsn()));
			type.setPt(typeConcept.getPt() == null ? new SnomedBrowserTerm(type.getConceptId()) : new SnomedBrowserTerm(typeConcept.getPt()));

		} else {
			LOGGER.warn("Concept {} used in axiom attribute type but does not exist on this branch {}.", type.getConceptId(), branchPath);
		}

		final SnomedBrowserRelationshipTarget target = (SnomedBrowserRelationshipTarget) relationship.getTarget();

		if (idToConceptMap.containsKey(target.getConceptId())) {

			final SnomedConcept targetConcept = idToConceptMap.get(target.getConceptId());
			target.setFsn(targetConcept.getFsn() == null ? new SnomedBrowserTerm(target.getConceptId()) : new SnomedBrowserTerm(targetConcept.getFsn()));
			target.setPt(targetConcept.getPt() == null ? new SnomedBrowserTerm(target.getConceptId()) : new SnomedBrowserTerm(targetConcept.getPt()));

			target.setActive(targetConcept.isActive());
			target.setEffectiveTime(targetConcept.getEffectiveTime());
			target.setReleased(targetConcept.isReleased());
			target.setModuleId(targetConcept.getModuleId());
			target.setDefinitionStatus(DefinitionStatus.getByConceptId(targetConcept.getDefinitionStatusId()));

		} else {
			LOGGER.warn("Concept {} used in axiom attribute target but does not exist on this branch {}.", target.getConceptId(), branchPath);
		}

	}

	private IEventBus getBus() {
		return ApplicationContext.getServiceForClass(IEventBus.class);
	}

	private ISnomedBrowserAxiom convertToBrowserAxiom(final SnomedReferenceSetMember axiomMember, final boolean primitive, final Map<Integer, List<Relationship>> relationships, boolean isNamedConceptOnLeft) {

		final SnomedBrowserAxiom browserAxiom = new SnomedBrowserAxiom();
		browserAxiom.setAxiomId(axiomMember.getId());
		browserAxiom.setActive(axiomMember.isActive());
		browserAxiom.setEffectiveTime(axiomMember.getEffectiveTime());
		browserAxiom.setModuleId(axiomMember.getModuleId());
		browserAxiom.setReleased(axiomMember.isReleased());
		browserAxiom.setReferencedComponentId(axiomMember.getReferencedComponentId());
		browserAxiom.setDefinitionStatus(primitive ? DefinitionStatus.PRIMITIVE : DefinitionStatus.FULLY_DEFINED);
		browserAxiom.setOwlExpression((String) axiomMember.getProperties().get(SnomedRf2Headers.FIELD_OWL_EXPRESSION));
		browserAxiom.setNamedConceptOnLeft(isNamedConceptOnLeft);
		
		List<Relationship> axiomRelationships = relationships.values().stream().flatMap(List::stream).collect(toList());
		
		Set<String> namedConceptIds = newHashSet(axiomMember.getReferencedComponentId());
		axiomRelationships.forEach(relationship -> {
			namedConceptIds.add(Long.toString(relationship.getTypeId()));
			namedConceptIds.add(Long.toString(relationship.getDestinationId()));
		});
		
		browserAxiom.setNamedConceptIds(namedConceptIds);
		browserAxiom.setRelationships(convertToBrowserRelationships(axiomRelationships, axiomMember));

		return browserAxiom;
	}

	private List<ISnomedBrowserRelationship> convertToBrowserRelationships(final List<Relationship> axiomRelationships, final SnomedReferenceSetMember axiomMember) {
		return axiomRelationships.stream()
				.map(relationship -> {

					final SnomedBrowserRelationship browserRelationship = new SnomedBrowserRelationship();
					browserRelationship.setActive(axiomMember.isActive());
					browserRelationship.setGroupId(relationship.getGroup());
					browserRelationship.setCharacteristicType(CharacteristicType.STATED_RELATIONSHIP);
					browserRelationship.setSourceId(axiomMember.getReferencedComponentId());
					browserRelationship.setType(new SnomedBrowserRelationshipType(Long.toString(relationship.getTypeId())));
					browserRelationship.setTarget(new SnomedBrowserRelationshipTarget(Long.toString(relationship.getDestinationId())));

					return browserRelationship;

				}).collect(Collectors.toList());
	}

}
