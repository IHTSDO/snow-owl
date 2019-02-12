package com.b2international.snowowl.snomed.api.impl.domain;

import com.b2international.snowowl.core.domain.TransactionContext;
import com.b2international.snowowl.core.events.Request;
import com.b2international.snowowl.core.exceptions.BadRequestException;
import com.b2international.snowowl.snomed.SnomedConstants.Concepts;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserAxiom;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserDescription;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserRelationship;
import com.b2international.snowowl.snomed.api.impl.domain.browser.SnomedBrowserAxiom;
import com.b2international.snowowl.snomed.api.impl.domain.browser.SnomedBrowserConcept;
import com.b2international.snowowl.snomed.core.domain.AssociationType;
import com.b2international.snowowl.snomed.core.domain.CharacteristicType;
import com.b2international.snowowl.snomed.core.domain.InactivationIndicator;
import com.b2international.snowowl.snomed.datastore.request.SnomedConceptCreateRequest;
import com.b2international.snowowl.snomed.datastore.request.SnomedConceptCreateRequestBuilder;
import com.b2international.snowowl.snomed.datastore.request.SnomedConceptUpdateRequest;
import com.b2international.snowowl.snomed.datastore.request.SnomedConceptUpdateRequestBuilder;
import com.b2international.snowowl.snomed.datastore.request.SnomedDescriptionCreateRequest;
import com.b2international.snowowl.snomed.datastore.request.SnomedRefSetMemberCreateRequest;
import com.b2international.snowowl.snomed.datastore.request.SnomedRelationshipCreateRequest;
import com.b2international.snowowl.snomed.datastore.request.SnomedRequests;
import com.google.common.collect.Multimap;

public class ConceptInputCreator extends AbstractInputCreator implements ComponentInputCreator<SnomedConceptCreateRequest, SnomedConceptUpdateRequest, SnomedBrowserConcept> {
	
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
			builder.setIdFromMetadata();
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
			builder.addRelationship(inputFactory.createComponentInput(relationship, SnomedRelationshipCreateRequest.class));
		}
		
		for (ISnomedBrowserDescription description : concept.getDescriptions()) {
			builder.addDescription(inputFactory.createComponentInput(description, SnomedDescriptionCreateRequest.class));
		}
		
		// An SCTID is required for generating axiom OWL expressions
		// We don't yet have the concept ID so we will use a known temporary ID and replace it during concept creation.
		for (ISnomedBrowserAxiom axiom : concept.getClassAxioms()) {
			SnomedBrowserAxiom browserAxiom = (SnomedBrowserAxiom) axiom;
			browserAxiom.setNamedConceptOnLeft(true);
			browserAxiom.setReferencedComponentId(Concepts.TEMPORARY_AXIOM_CONCEPT_PLACEHOLDER);
			browserAxiom.setModuleId(moduleId);
			builder.addMember(inputFactory.createComponentInput(browserAxiom, SnomedRefSetMemberCreateRequest.class));
		}
		
		for (ISnomedBrowserAxiom axiom : concept.getGciAxioms()) {
			SnomedBrowserAxiom browserAxiom = (SnomedBrowserAxiom) axiom;
			browserAxiom.setNamedConceptOnLeft(false);
			browserAxiom.setReferencedComponentId(Concepts.TEMPORARY_AXIOM_CONCEPT_PLACEHOLDER);
			browserAxiom.setModuleId(moduleId);
			builder.addMember(inputFactory.createComponentInput(browserAxiom, SnomedRefSetMemberCreateRequest.class));
		}

		// TODO remove cast, use only Request interfaces with proper type
		return (SnomedConceptCreateRequest) builder.build();
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
	public boolean canCreateInput(Class<? extends Request<TransactionContext, String>> inputType) {
		return SnomedConceptCreateRequest.class.isAssignableFrom(inputType);
	}

	@Override
	public boolean canCreateUpdate(Class<? extends Request<TransactionContext, Boolean>> updateType) {
		return SnomedConceptUpdateRequest.class.isAssignableFrom(updateType);
	}
}
