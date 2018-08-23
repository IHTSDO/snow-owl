package com.b2international.snowowl.snomed.api.impl.validation.service;

import java.util.Set;

import org.ihtsdo.drools.service.RelationshipService;

import com.b2international.snowowl.core.ApplicationContext;
import com.b2international.snowowl.eventbus.IEventBus;
import com.b2international.snowowl.snomed.core.domain.CharacteristicType;
import com.b2international.snowowl.snomed.datastore.SnomedDatastoreActivator;
import com.b2international.snowowl.snomed.datastore.request.SnomedRequests;
import com.google.common.collect.ImmutableSet;

public class ValidationRelationshipService implements RelationshipService {

	private static final Set<String> NON_INFERRED_CHARACTERISTIC_TYPES = ImmutableSet.<String>builder()
				.add(CharacteristicType.STATED_RELATIONSHIP.getConceptId())
				.add(CharacteristicType.ADDITIONAL_RELATIONSHIP.getConceptId())
				.add(CharacteristicType.QUALIFYING_RELATIONSHIP.getConceptId())
				.build();
	
	private String branchPath;
	private IEventBus bus;

	public ValidationRelationshipService(String branchPath) {
		this.branchPath = branchPath;
		this.bus = ApplicationContext.getServiceForClass(IEventBus.class);
	}

	@Override
	public boolean hasActiveInboundStatedRelationship(String conceptId) {
		return hasActiveInboundStatedRelationship(conceptId, null);
	}

	@Override
	public boolean hasActiveInboundStatedRelationship(String conceptId, String relationshipTypeId) {
		return SnomedRequests.prepareSearchRelationship()
				.setLimit(0)
				.filterByActive(true)
				.filterByDestination(conceptId)
				.filterByType(relationshipTypeId)
				.filterByCharacteristicTypes(NON_INFERRED_CHARACTERISTIC_TYPES)
				.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
				.execute(bus)
				.getSync()
				.getTotal() > 0;
	}

}
