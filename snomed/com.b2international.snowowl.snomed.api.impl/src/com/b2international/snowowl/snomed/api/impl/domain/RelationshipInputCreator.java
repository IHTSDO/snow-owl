package com.b2international.snowowl.snomed.api.impl.domain;

import com.b2international.snowowl.core.branch.Branch;
import com.b2international.snowowl.core.domain.TransactionContext;
import com.b2international.snowowl.core.events.Request;
import com.b2international.snowowl.snomed.api.impl.domain.browser.SnomedBrowserRelationship;
import com.b2international.snowowl.snomed.datastore.request.SnomedRelationshipCreateRequest;
import com.b2international.snowowl.snomed.datastore.request.SnomedRelationshipCreateRequestBuilder;
import com.b2international.snowowl.snomed.datastore.request.SnomedRelationshipUpdateRequest;
import com.b2international.snowowl.snomed.datastore.request.SnomedRelationshipUpdateRequestBuilder;
import com.b2international.snowowl.snomed.datastore.request.SnomedRequests;

public class RelationshipInputCreator extends AbstractInputCreator implements ComponentInputCreator<SnomedRelationshipCreateRequest, SnomedRelationshipUpdateRequest, SnomedBrowserRelationship> {
	
	

	public RelationshipInputCreator(final Branch branch) {
		super(branch);
	}
	
	@Override
	public SnomedRelationshipCreateRequest createInput(SnomedBrowserRelationship relationship, InputFactory inputFactory) {
		final SnomedRelationshipCreateRequestBuilder builder = SnomedRequests
				.prepareNewRelationship()
				.setModuleId(getModuleOrDefault(relationship))
				.setTypeId(relationship.getType().getConceptId())
				.setCharacteristicType(relationship.getCharacteristicType())
				.setSourceId(relationship.getSourceId()) // XXX: for a new concept, this value might not be known
				.setDestinationId(relationship.getTarget().getConceptId())
				.setGroup(relationship.getGroupId())
				.setModifier(relationship.getModifier());
		
		if (relationship.getRelationshipId() != null) {
			builder.setId(relationship.getRelationshipId());
		} else {
			
			builder.setIdFromNamespace(getDefaultNamespace(), getBranch());
		}
		
		return (SnomedRelationshipCreateRequest) builder.build();
	}

	@Override
	public SnomedRelationshipUpdateRequest createUpdate(SnomedBrowserRelationship existingRelationship, SnomedBrowserRelationship updatedRelationship) {
		final SnomedRelationshipUpdateRequestBuilder builder = SnomedRequests.prepareUpdateRelationship(existingRelationship.getRelationshipId());
		boolean anyDifference = false;
		
		if (existingRelationship.isActive() != updatedRelationship.isActive()) {
			anyDifference = true;
			builder.setActive(updatedRelationship.isActive());
		}
		
		if (!existingRelationship.getModuleId().equals(updatedRelationship.getModuleId())) {
			anyDifference = true;
			builder.setModuleId(updatedRelationship.getModuleId());
		}
		
		if (existingRelationship.getGroupId() != updatedRelationship.getGroupId()) {
			anyDifference = true;
			builder.setGroup(updatedRelationship.getGroupId());
		}
		
		if (!existingRelationship.getCharacteristicType().equals(updatedRelationship.getCharacteristicType())) {
			anyDifference = true;
			builder.setCharacteristicType(updatedRelationship.getCharacteristicType());
		}
		
		if (!existingRelationship.getModifier().equals(updatedRelationship.getModifier())) {
			anyDifference = true;
			builder.setModifier(updatedRelationship.getModifier());
		}
		
		if (anyDifference) {
			// TODO remove cast, use only Request interfaces with proper types
			return (SnomedRelationshipUpdateRequest) builder.build();
		} else {
			return null;
		}
	}

	@Override
	public boolean canCreateInput(Class<? extends Request<TransactionContext, String>> inputType) {
		return SnomedRelationshipCreateRequest.class.isAssignableFrom(inputType);
	}
	
	@Override
	public boolean canCreateUpdate(Class<? extends Request<TransactionContext, Boolean>> updateType) {
		return SnomedRelationshipUpdateRequest.class.isAssignableFrom(updateType);
	}
}
