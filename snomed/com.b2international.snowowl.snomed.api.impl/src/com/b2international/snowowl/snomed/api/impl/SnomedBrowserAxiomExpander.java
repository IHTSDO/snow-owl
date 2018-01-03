package com.b2international.snowowl.snomed.api.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.otf.owltoolkit.conversion.ConversionException;
import org.snomed.otf.owltoolkit.conversion.ConversionService;
import org.snomed.otf.owltoolkit.domain.AxiomRepresentation;
import org.snomed.otf.owltoolkit.domain.Relationship;

import com.b2international.commons.http.ExtendedLocale;
import com.b2international.snowowl.eventbus.IEventBus;
import com.b2international.snowowl.snomed.SnomedConstants.Concepts;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserAxiom;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserRelationship;
import com.b2international.snowowl.snomed.api.impl.domain.browser.SnomedBrowserAxiom;
import com.b2international.snowowl.snomed.api.impl.domain.browser.SnomedBrowserConcept;
import com.b2international.snowowl.snomed.api.impl.domain.browser.SnomedBrowserRelationship;
import com.b2international.snowowl.snomed.api.impl.domain.browser.SnomedBrowserRelationshipTarget;
import com.b2international.snowowl.snomed.api.impl.domain.browser.SnomedBrowserRelationshipType;
import com.b2international.snowowl.snomed.core.domain.DefinitionStatus;
import com.b2international.snowowl.snomed.core.domain.SnomedConcept;
import com.b2international.snowowl.snomed.core.domain.SnomedConcepts;
import com.b2international.snowowl.snomed.core.domain.refset.SnomedReferenceSetMember;
import com.b2international.snowowl.snomed.core.domain.refset.SnomedReferenceSetMembers;
import com.b2international.snowowl.snomed.datastore.SnomedDatastoreActivator;
import com.b2international.snowowl.snomed.datastore.request.SnomedRequests;
import com.google.common.base.Function;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;

public class SnomedBrowserAxiomExpander {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SnomedBrowserAxiomExpander.class);
	
	void expandAxioms(Set<SnomedBrowserConcept> concepts, ConversionService conversionService, final List<ExtendedLocale> locales, final String branchPath, IEventBus eventBus) {
		final Set<String> conceptIds = concepts.stream().map(SnomedBrowserConcept::getConceptId).collect(Collectors.toSet());
		
		// Load relevant members from OWL Axiom Reference Set
		final SnomedReferenceSetMembers axiomMembers = SnomedRequests.prepareSearchMember()
				.all()
				.filterByRefSet(Sets.newHashSet(Concepts.REFSET_OWL_AXIOM))
				.filterByReferencedComponent(conceptIds)
				.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
				.execute(eventBus)
				.getSync();

		if (axiomMembers.getItems().isEmpty()) {
			return;
		}
		
		final Multimap<String, SnomedReferenceSetMember> membersByReferencedComponentId = Multimaps.index(axiomMembers, new Function<SnomedReferenceSetMember, String>() {
			@Override
			public String apply(SnomedReferenceSetMember input) {
				return input.getReferencedComponent().getId();
			}
		});

		// Use Open Source Project 'Snomed OWL Toolkit' to deserialise axioms
		Map<String, SnomedBrowserRelationshipType> typesToFetch = new HashMap<>();
		Map<String, SnomedBrowserRelationshipTarget> targetsToFetch = new HashMap<>();
		for (SnomedBrowserConcept concept : concepts) {
			List<ISnomedBrowserAxiom> additionalAxioms = new ArrayList<>();
			List<ISnomedBrowserAxiom> gciAxioms = new ArrayList<>();
			Collection<SnomedReferenceSetMember> conceptAxiomMembers = membersByReferencedComponentId.get(concept.getId());
			if (conceptAxiomMembers != null) {
				try {
					for (SnomedReferenceSetMember axiomMember : conceptAxiomMembers) {
						String owlExpression = (String) axiomMember.getProperties().get("owlExpression");
						Long conceptId = Long.parseLong(concept.getId());
						AxiomRepresentation axiomRepresentation = conversionService.convertAxiomToRelationships(conceptId, owlExpression);
						if (conceptId.equals(axiomRepresentation.getLeftHandSideNamedConcept())) {
							// Concept ID on the left means it's an additional axiom
							additionalAxioms.add(convertToBrowserAxiom(axiomMember, axiomRepresentation.isPrimitive(), axiomRepresentation.getRightHandSideRelationships(), typesToFetch, targetsToFetch));
						} else if (conceptId.equals(axiomRepresentation.getRightHandSideNamedConcept())) {
							// Concept ID on the right means it's a GCI axiom
							gciAxioms.add(convertToBrowserAxiom(axiomMember, axiomRepresentation.isPrimitive(), axiomRepresentation.getLeftHandSideRelationships(), typesToFetch, targetsToFetch));
						}
					}
				} catch (ConversionException | NumberFormatException e) {
					LOGGER.error("Failed to deserialise concept axioms for conept {}.", concept.getId(), e);
				}
			}
			if (!additionalAxioms.isEmpty()) {
				concept.setAdditionalAxioms(additionalAxioms);
			}
			if (!gciAxioms.isEmpty()) {
				concept.setGciAxioms(gciAxioms);
			}
		}
		
		// Lazy load the concept details for each generated relationship target
		if (!targetsToFetch.isEmpty()) {
			
			// Grab IDs of all types and targets
			Set<String> conceptIdsToFetch = Sets.union(typesToFetch.keySet(), targetsToFetch.keySet());

			// Load all required concepts including FSNs in one hit
			SnomedConcepts conceptsFromStore = SnomedRequests.prepareSearchConcept()
				.setLocales(locales)
				.setExpand("fsn()")
				.filterByIds(conceptIdsToFetch)
				.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
				.execute(eventBus)
				.getSync();

			// Build id to concept map for easy access
			Map<String, SnomedConcept> conceptsFromStoreMap = 
					conceptsFromStore.getItems().stream().collect(Collectors.toMap(SnomedConcept::getId, concept -> concept));

			// Fill in type information in each relationship generated from axioms
			typesToFetch.values().forEach(type -> {
				SnomedConcept conceptFromStore = conceptsFromStoreMap.get(type.getConceptId());
				if (conceptFromStore != null) {
					if (conceptFromStore.getFsn() != null) {
						type.setFsn(conceptFromStore.getFsn().getTerm());
					} else {
						type.setFsn(conceptFromStore.getId());
					}
				} else {
					LOGGER.warn("Concept {} used in axiom attribute type but does not exist on this branch {}.", type.getConceptId(), branchPath);
				}
			});
			
			// Fill in target information in each relationship generated from axioms
			targetsToFetch.values().forEach(target -> {
				SnomedConcept conceptFromStore = conceptsFromStoreMap.get(target.getConceptId());
				if (conceptFromStore != null) {
					target.setActive(conceptFromStore.isActive());
					target.setDefinitionStatus(conceptFromStore.getDefinitionStatus());
					target.setEffectiveTime(conceptFromStore.getEffectiveTime());
					
					if (conceptFromStore.getFsn() != null) {
						target.setFsn(conceptFromStore.getFsn().getTerm());
					} else {
						target.setFsn(conceptFromStore.getId());
					}
					
					target.setModuleId(conceptFromStore.getModuleId());
					target.setReleased(conceptFromStore.isReleased());
				} else {
					LOGGER.warn("Concept {} used in axiom attribute target but does not exist on this branch {}.", target.getConceptId(), branchPath);
				}
			});
		}
	}

	private ISnomedBrowserAxiom convertToBrowserAxiom(SnomedReferenceSetMember axiomMember, boolean primitive, Map<Integer, List<Relationship>> relationships, 
			Map<String, SnomedBrowserRelationshipType> typesToFetch, Map<String, SnomedBrowserRelationshipTarget> targetsToFetch) {
		
		SnomedBrowserAxiom axiom = new SnomedBrowserAxiom();
		axiom.setAxiomId(axiomMember.getId());
		axiom.setActive(axiomMember.isActive());
		axiom.setEffectiveTime(axiomMember.getEffectiveTime());
		axiom.setModuleId(axiomMember.getModuleId());
		axiom.setReleased(axiomMember.isReleased());
		axiom.setDefinitionStatus(primitive ? DefinitionStatus.PRIMITIVE : DefinitionStatus.FULLY_DEFINED);
		axiom.setRelationships(convertToBrowserRelationships(relationships, axiomMember.isActive(), typesToFetch, targetsToFetch));
		return axiom;
	}

	private List<ISnomedBrowserRelationship> convertToBrowserRelationships(Map<Integer, List<Relationship>> relationships, boolean active,
			Map<String, SnomedBrowserRelationshipType> typesToFetch, Map<String, SnomedBrowserRelationshipTarget> targetsToFetch) {
		
		return relationships.values().stream().flatMap(List::stream).map(relationship -> {
			SnomedBrowserRelationship browserRelationship = new SnomedBrowserRelationship();
			browserRelationship.setActive(active);
			browserRelationship.setGroupId(relationship.getGroup());
			
			// Fetch type information later
			browserRelationship.setType(typesToFetch.computeIfAbsent(relationship.getTypeId() + "", id -> new SnomedBrowserRelationshipType(id + "")));
			
			browserRelationship.setTarget(targetsToFetch.computeIfAbsent(relationship.getDestinationId() + "", id -> {
				SnomedBrowserRelationshipTarget target = new SnomedBrowserRelationshipTarget();
				target.setConceptId(relationship.getDestinationId() + "");				
				return target;
			}));
			
			return browserRelationship;
		}).collect(Collectors.toList());
	}

}
