package com.b2international.snowowl.snomed.api.impl.validation.service;

import java.util.*;

import org.ihtsdo.drools.domain.Concept;
import org.ihtsdo.drools.domain.Constants;
import org.ihtsdo.drools.domain.Description;
import org.ihtsdo.drools.domain.Relationship;
import org.ihtsdo.drools.helper.DescriptionHelper;
import org.ihtsdo.drools.service.RelationshipService;

import com.b2international.snowowl.eventbus.IEventBus;
import com.b2international.snowowl.snomed.core.domain.CharacteristicType;
import com.b2international.snowowl.snomed.core.domain.SnomedConcept;
import com.b2international.snowowl.snomed.core.domain.SnomedDescription;
import com.b2international.snowowl.snomed.datastore.SnomedDatastoreActivator;
import com.b2international.snowowl.snomed.datastore.request.SnomedConceptGetRequestBuilder;
import com.b2international.snowowl.snomed.datastore.request.SnomedRequests;

public class ValidationRelationshipService implements RelationshipService {

	private String branchPath;
	private IEventBus bus;

	public ValidationRelationshipService(String branchPath, IEventBus bus) {
		this.branchPath = branchPath;
		this.bus = bus;
	}

	@Override
	public boolean hasActiveInboundStatedRelationship(String conceptId) {
		return hasActiveInboundStatedRelationship(conceptId, null);
	}

	@Override
	public boolean hasActiveInboundStatedRelationship(String conceptId, String relationshipTypeId) {
		int totalInbound = SnomedRequests
				.prepareSearchRelationship()
				.filterByDestination(conceptId)
				.filterByActive(true)
				.filterByType(relationshipTypeId)
				.setOffset(0)
				.setLimit(0)
				.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
				.execute(bus)
				.getSync()
				.getTotal();
		
		int totalInboundInferred = SnomedRequests
				.prepareSearchRelationship()
				.filterByDestination(conceptId)
				.filterByActive(true)
				.filterByType(relationshipTypeId)
				.filterByCharacteristicType(CharacteristicType.INFERRED_RELATIONSHIP.getConceptId())
				.setOffset(0)
				.setLimit(0)
				.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
				.execute(bus)
				.getSync()
				.getTotal();
		
		// This covers all types of relationship other than inferred.
		return totalInbound - totalInboundInferred > 0;
	}

	@Override
    Set<String> findParentsNotContainSematicTag(Concept c, String sematicTag) {
    	Set<String> parentIds = new HashSet<>();
		for (Relationship r: c.getRelationships()) {
			if(r.isActive() && Constants.IS_A.equals(r.getTypeId()) && !Constants.INFERRED_RELATIONSHIP.equals(r.getCharacteristicTypeId())) {
				SnomedConcept parentConcept = SnomedRequests.prepareGetConcept(r.getDestinationId())
						.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
						.execute(bus)
						.getSync();
				for (SnomedDescription d : parentConcept.getDescriptions()) {
					if (d.isActive() && Constants.FSN.equals(d.getTypeId())) {
						String parentSematicTag = DescriptionHelper.getTag(d.getTerm());
						if (!sematicTag.equals(parentSematicTag)) {
							parentIds.add(parentConcept.getId());
						}
					}
				}				
			}
		}
    	return parentIds;
    }

}
