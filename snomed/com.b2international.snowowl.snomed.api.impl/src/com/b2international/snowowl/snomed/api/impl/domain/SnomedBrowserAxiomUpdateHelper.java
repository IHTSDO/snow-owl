package com.b2international.snowowl.snomed.api.impl.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.elasticsearch.common.collect.MapBuilder;
import org.snomed.otf.owltoolkit.conversion.AxiomRelationshipConversionService;
import org.snomed.otf.owltoolkit.domain.AxiomRepresentation;
import org.snomed.otf.owltoolkit.domain.Relationship;

import com.b2international.snowowl.core.domain.TransactionContext;
import com.b2international.snowowl.core.events.Request;
import com.b2international.snowowl.snomed.SnomedConstants.Concepts;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserAxiom;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserRelationship;
import com.b2international.snowowl.snomed.api.impl.domain.browser.SnomedBrowserAxiom;
import com.b2international.snowowl.snomed.common.SnomedRf2Headers;
import com.b2international.snowowl.snomed.datastore.request.SnomedRefSetMemberCreateRequestBuilder;
import com.b2international.snowowl.snomed.datastore.request.SnomedRefSetMemberUpdateRequestBuilder;
import com.b2international.snowowl.snomed.datastore.request.SnomedRequests;

public class SnomedBrowserAxiomUpdateHelper {
	
	private AxiomRelationshipConversionService axiomConversionService;
	
	public SnomedBrowserAxiomUpdateHelper(AxiomRelationshipConversionService axiomConversionService) {
		this.axiomConversionService = axiomConversionService;
	}

	public ArrayList<Request<TransactionContext, ?>> getAxiomPersistenceRequests(List<ISnomedBrowserAxiom> existingVersionAxioms, List<ISnomedBrowserAxiom> newVersionAxioms, 
			boolean namedConceptOnLeft, Long conceptId, String moduleId) {
		
		ArrayList<Request<TransactionContext, ?>> requests = new ArrayList<>();
		
		// Gather IDs or existing and new version axioms
		Set<String> existingVersionAxiomIds = existingVersionAxioms.stream().filter(axiom -> axiom.getAxiomId() != null).map(ISnomedBrowserAxiom::getAxiomId).collect(Collectors.toSet());
		Set<String> newVersionAxiomsIds = newVersionAxioms.stream().filter(axiom -> axiom.getAxiomId() != null).map(ISnomedBrowserAxiom::getAxiomId).collect(Collectors.toSet());

		// Build deletion requests
		existingVersionAxioms.stream()
				.filter(existingVersionAxiom -> !newVersionAxiomsIds.contains(existingVersionAxiom.getAxiomId()))
				.forEach(existingVersionAxiom -> {
						String id = existingVersionAxiom.getAxiomId();
						requests.add(SnomedRequests.prepareDeleteMember(id).build());
					});
		
		// Clear IDs assigned by the client
		// Recycle IDs of inactive members
		Iterator<String> inactiveAxiomIds = existingVersionAxioms.stream().filter(axiom -> !axiom.isActive()).map(ISnomedBrowserAxiom::getAxiomId).collect(Collectors.toSet()).iterator();
		newVersionAxioms.stream()
				.filter(axiom -> axiom.getAxiomId() != null && !existingVersionAxiomIds.contains(axiom.getAxiomId()))
				.forEach(axiom -> {
					SnomedBrowserAxiom axiomImpl = ((SnomedBrowserAxiom)axiom);
					axiomImpl.setAxiomId(null);
					if (inactiveAxiomIds.hasNext()) {
						axiomImpl.setAxiomId(inactiveAxiomIds.next());
					}
				});

		// Build creation requests
		newVersionAxioms.stream().filter(axiom -> axiom.getAxiomId() == null).forEach(axiom -> {
			requests.add(getAxiomCreateRequest(namedConceptOnLeft, axiom, conceptId, moduleId).build());
		});

		// Build update requests
		Map<String, ISnomedBrowserAxiom> existingAxiomMap = existingVersionAxioms.stream().collect(Collectors.toMap(ISnomedBrowserAxiom::getAxiomId, axiom -> axiom));
		newVersionAxioms.stream().filter(axiom -> axiom.getAxiomId() != null).forEach(axiom -> {
			ISnomedBrowserAxiom existingVersion = existingAxiomMap.get(axiom.getAxiomId());
			requests.add(getAxiomUpdateRequest(namedConceptOnLeft, axiom, existingVersion, conceptId, moduleId).build());
		});

		return requests;
	}

	private SnomedRefSetMemberUpdateRequestBuilder getAxiomUpdateRequest(boolean namedConceptOnLeft, ISnomedBrowserAxiom newVersionAxiom, ISnomedBrowserAxiom existingVersion,
			Long conceptId, String moduleId) {

		String owlExpression = toOwlExpression(namedConceptOnLeft, newVersionAxiom, conceptId);
		return SnomedRequests
				.prepareUpdateMember()
				.setMemberId(newVersionAxiom.getAxiomId())
				.setSource(MapBuilder.newMapBuilder(new HashMap<String, Object>())
						.put(SnomedRf2Headers.FIELD_ACTIVE, newVersionAxiom.isActive())
						.put(SnomedRf2Headers.FIELD_MODULE_ID, newVersionAxiom.getModuleId())
						.put(SnomedRf2Headers.FIELD_OWL_EXPRESSION, owlExpression)
						.map());
	}

	public SnomedRefSetMemberCreateRequestBuilder getAxiomCreateRequest(boolean namedConceptOnLeft, ISnomedBrowserAxiom axiom, 
			Long conceptId, String moduleId) {
		
		String owlExpression = toOwlExpression(namedConceptOnLeft, axiom, conceptId);
		return SnomedRequests
				.prepareNewMember()
				.setModuleId(moduleId)
				.setReferenceSetId(Concepts.REFSET_OWL_AXIOM)
				.setReferencedComponentId(conceptId.toString())
				.setProperties(MapBuilder.newMapBuilder(new HashMap<String, Object>())
						.put(SnomedRf2Headers.FIELD_OWL_EXPRESSION, owlExpression).map());
	}

	private String toOwlExpression(boolean namedConceptOnLeft, ISnomedBrowserAxiom axiom, long conceptId) {
		AxiomRepresentation axiomRepresentation = new AxiomRepresentation();
		axiomRepresentation.setPrimitive(axiom.getDefinitionStatus().isPrimitive());
		Map<Integer, List<Relationship>> relationships = new HashMap<>();
		for (ISnomedBrowserRelationship rel : axiom.getRelationships()) {
			String targetId = rel.getTarget().getConceptId();
			relationships.computeIfAbsent(rel.getGroupId(), group -> new ArrayList<>())
					.add(new Relationship(rel.getGroupId(), Long.parseLong(rel.getType().getConceptId()), Long.parseLong(targetId)));
		}
		
		if (namedConceptOnLeft) {
			axiomRepresentation.setLeftHandSideNamedConcept(conceptId);
			axiomRepresentation.setRightHandSideRelationships(relationships);
		} else {
			axiomRepresentation.setRightHandSideNamedConcept(conceptId);
			axiomRepresentation.setLeftHandSideRelationships(relationships);
		}
		
		return axiomConversionService.convertRelationshipsToAxiom(axiomRepresentation);
	}
	
}
