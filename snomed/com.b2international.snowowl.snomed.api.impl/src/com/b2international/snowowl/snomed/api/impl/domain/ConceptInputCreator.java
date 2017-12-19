package com.b2international.snowowl.snomed.api.impl.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.elasticsearch.common.collect.MapBuilder;
import org.snomed.otf.owltoolkit.conversion.ConversionService;
import org.snomed.otf.owltoolkit.domain.AxiomRepresentation;
import org.snomed.otf.owltoolkit.domain.Relationship;

import com.b2international.snowowl.core.branch.Branch;
import com.b2international.snowowl.core.domain.TransactionContext;
import com.b2international.snowowl.core.events.Request;
import com.b2international.snowowl.core.exceptions.BadRequestException;
import com.b2international.snowowl.snomed.SnomedConstants.Concepts;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserAxiom;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserDescription;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserRelationship;
import com.b2international.snowowl.snomed.api.impl.domain.browser.SnomedBrowserConcept;
import com.b2international.snowowl.snomed.common.SnomedRf2Headers;
import com.b2international.snowowl.snomed.core.domain.AssociationType;
import com.b2international.snowowl.snomed.core.domain.CharacteristicType;
import com.b2international.snowowl.snomed.core.domain.InactivationIndicator;
import com.b2international.snowowl.snomed.core.domain.refset.SnomedReferenceSetMember;
import com.b2international.snowowl.snomed.datastore.request.SnomedComponentCreateRequest;
import com.b2international.snowowl.snomed.datastore.request.SnomedComponentUpdateRequest;
import com.b2international.snowowl.snomed.datastore.request.SnomedConceptCreateRequest;
import com.b2international.snowowl.snomed.datastore.request.SnomedConceptCreateRequestBuilder;
import com.b2international.snowowl.snomed.datastore.request.SnomedConceptUpdateRequest;
import com.b2international.snowowl.snomed.datastore.request.SnomedConceptUpdateRequestBuilder;
import com.b2international.snowowl.snomed.datastore.request.SnomedDescriptionCreateRequest;
import com.b2international.snowowl.snomed.datastore.request.SnomedRefSetMemberCreateRequestBuilder;
import com.b2international.snowowl.snomed.datastore.request.SnomedRelationshipCreateRequest;
import com.b2international.snowowl.snomed.datastore.request.SnomedRequests;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

public class ConceptInputCreator extends AbstractInputCreator implements ComponentInputCreator<SnomedConceptCreateRequest, SnomedConceptUpdateRequest, SnomedBrowserConcept> {
	
	private final ConversionService conversionService;
	
	public ConceptInputCreator(final Branch branch, ConversionService axiomConversionService) {
		super(branch);
		this.conversionService = axiomConversionService;
	}
	
	@Override
	public SnomedConceptCreateRequest createInput(SnomedBrowserConcept concept, InputFactory inputFactory) {
		String moduleId = getModuleOrDefault(concept);
		final SnomedConceptCreateRequestBuilder builder = SnomedRequests
				.prepareNewConcept()
				.setModuleId(moduleId)
				.setDefinitionStatus(concept.getDefinitionStatus());
		
		final String conceptId = concept.getConceptId();
		if (conceptId != null) {
			builder.setId(conceptId);
		} else {
			builder.setIdFromNamespace(getDefaultNamespace(), getBranch());
		}

		boolean hasActiveStatedIsaRelationship = concept.getRelationships().stream()
			.anyMatch( relationship -> 
				relationship.getType().getConceptId().equals(Concepts.IS_A) 
				&& relationship.getCharacteristicType() == CharacteristicType.STATED_RELATIONSHIP 
				&& relationship.isActive());
		
		if (!hasActiveStatedIsaRelationship) {
			throw new BadRequestException("At least one active stated IS A relationship is required.");
		}
		
		for (ISnomedBrowserRelationship relationship : concept.getRelationships()) {
			builder.addRelationship(inputFactory.createComponentInput(getBranch().path(), relationship, SnomedRelationshipCreateRequest.class));
		}
		
		for (ISnomedBrowserDescription description : concept.getDescriptions()) {
			builder.addDescription(inputFactory.createComponentInput(getBranch().path(), description, SnomedDescriptionCreateRequest.class));
		}
		
		for (ISnomedBrowserAxiom axiom : concept.getAdditionalAxioms()) {
			boolean namedConceptOnLeft = true;
			convertToMemberRequest(namedConceptOnLeft, axiom, moduleId, builder);
		}
		for (ISnomedBrowserAxiom axiom : concept.getGciAxioms()) {
			boolean namedConceptOnLeft = false;
			convertToMemberRequest(namedConceptOnLeft, axiom, moduleId, builder);
		}

		// TODO remove cast, use only Request interfaces with proper type
		return (SnomedConceptCreateRequest) builder.build();
	}

	private void convertToMemberRequest(boolean namedConceptOnLeft, ISnomedBrowserAxiom axiom, String moduleId,
			final SnomedConceptCreateRequestBuilder builder) {
		
		AxiomRepresentation axiomRepresentation = new AxiomRepresentation();
		axiomRepresentation.setPrimitive(axiom.getDefinitionStatus().isPrimitive());
		Map<Integer, List<Relationship>> relationships = new HashMap<>();
		for (ISnomedBrowserRelationship rel : axiom.getRelationships()) {
			String targetId = rel.getTarget().getConceptId();
			relationships.computeIfAbsent(rel.getGroupId(), group -> new ArrayList<>())
					.add(new Relationship(rel.getGroupId(), Long.parseLong(rel.getType().getConceptId()), Long.parseLong(targetId)));
		}
		
		// An SCTID is required for generating the OWL expression
		// We don't yet have the concept ID so we will use a known temporary ID and replace it during concept creation.
		long tempConceptId = Long.parseLong(Concepts.TEMPORARY_AXIOM_CONCEPT_PLACEHOLDER);
		if (namedConceptOnLeft) {
			axiomRepresentation.setLeftHandSideNamedConcept(tempConceptId);
			axiomRepresentation.setRightHandSideRelationships(relationships);
		} else {
			axiomRepresentation.setRightHandSideNamedConcept(tempConceptId);
			axiomRepresentation.setLeftHandSideRelationships(relationships);
		}
		String owlExpression = conversionService.convertRelationshipsToAxiom(axiomRepresentation);
		builder.addMember(SnomedRequests
				.prepareNewMember()
				.setModuleId(moduleId)
				.setReferenceSetId(Concepts.REFSET_OWL_AXIOM)
				.setProperties(MapBuilder.newMapBuilder(new HashMap<String, Object>())
						.put(SnomedRf2Headers.FIELD_OWL_EXPRESSION, owlExpression).map()));
	}

	@Override
	public SnomedConceptUpdateRequest createUpdate(final SnomedBrowserConcept existingConcept, final SnomedBrowserConcept updatedConcept) {
		final SnomedConceptUpdateRequestBuilder builder = SnomedRequests.prepareUpdateConcept(existingConcept.getConceptId());
		boolean anyDifference = false;

		/* 
		 * XXX: Snow Owl's concept update request treats null values as "no change", if the concept remains inactive, 
		 * but the browser has PUT semantics, so a value of null should mean "set to default". We're also trying to 
		 * do a null-safe comparison at the same time, to see if the concept update request needs to include a change at all.
		 */
		if (existingConcept.isActive() && !updatedConcept.isActive()) {
			
			// Set the updated concept's values when inactivating (null is OK here, they will be set to the default)
			anyDifference = true;
			builder.setActive(updatedConcept.isActive());
			builder.setInactivationIndicator(updatedConcept.getInactivationIndicator());
			builder.setAssociationTargets(updatedConcept.getAssociationTargets());
			
		} else if (!existingConcept.isActive() && updatedConcept.isActive()) {
			
			// Re-activation will clean out these values, don't set anything
			anyDifference = true;
			builder.setActive(updatedConcept.isActive());
			builder.setInactivationIndicator(null);
			builder.setAssociationTargets(null);
			
		} else if (!existingConcept.isActive() && !updatedConcept.isActive()) {
			
			// Convert null values to the corresponding default and apply if changed
			final InactivationIndicator existingInactivationIndicator = existingConcept.getInactivationIndicator() != null ? existingConcept.getInactivationIndicator() : InactivationIndicator.RETIRED;
			final InactivationIndicator updatedInactivationIndicator = updatedConcept.getInactivationIndicator() != null ? updatedConcept.getInactivationIndicator() : InactivationIndicator.RETIRED;
			
			if (!existingInactivationIndicator.equals(updatedInactivationIndicator)) {
				anyDifference = true;
				builder.setInactivationIndicator(updatedInactivationIndicator);
			}
			
			final Multimap<AssociationType, String> existingAssociationTargets = nullToEmptyMultimap(existingConcept.getAssociationTargets());
			final Multimap<AssociationType, String> updatedAssociationTargets = nullToEmptyMultimap(updatedConcept.getAssociationTargets());
			
			if (!existingAssociationTargets.equals(updatedAssociationTargets)) {
				anyDifference = true;
				builder.setAssociationTargets(updatedAssociationTargets);
			}
			
		} else /* if (existingConcept.isActive() && updatedConcept.isActive()) */ {
			// Nothing to do when the concept remains active
		}

		if (!existingConcept.getModuleId().equals(updatedConcept.getModuleId())) {
			anyDifference = true;
			builder.setModuleId(updatedConcept.getModuleId());
		}
		
		if (!existingConcept.getDefinitionStatus().equals(updatedConcept.getDefinitionStatus())) {
			anyDifference = true;
			builder.setDefinitionStatus(updatedConcept.getDefinitionStatus());
		}

		if (anyDifference) {
			// TODO remove cast, use only Request interfaces with proper types
			return (SnomedConceptUpdateRequest) builder.build();
		} else {
			return null;
		}
	}

	@Override
	public boolean canCreateInput(Class<? extends SnomedComponentCreateRequest> inputType) {
		return SnomedConceptCreateRequest.class.isAssignableFrom(inputType);
	}

	@Override
	public boolean canCreateUpdate(Class<? extends SnomedComponentUpdateRequest> updateType) {
		return SnomedConceptUpdateRequest.class.isAssignableFrom(updateType);
	}
}
