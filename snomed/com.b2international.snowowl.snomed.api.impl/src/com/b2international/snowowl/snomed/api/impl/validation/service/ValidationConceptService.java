package com.b2international.snowowl.snomed.api.impl.validation.service;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.ihtsdo.drools.domain.Concept;
import org.ihtsdo.drools.domain.Relationship;
import org.ihtsdo.drools.service.ConceptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.b2international.snowowl.core.ApplicationContext;
import com.b2international.snowowl.core.domain.IComponent;
import com.b2international.snowowl.eventbus.IEventBus;
import com.b2international.snowowl.snomed.SnomedConstants.Concepts;
import com.b2international.snowowl.snomed.core.domain.SnomedConcept;
import com.b2international.snowowl.snomed.datastore.SnomedDatastoreActivator;
import com.b2international.snowowl.snomed.datastore.request.SnomedRequests;
import com.google.common.base.Joiner;
import com.google.common.collect.Sets;

public class ValidationConceptService implements ConceptService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ValidationConceptService.class);
	
	private static final Joiner COMMA_JOINER = Joiner.on(", ");
	
	private final String branchPath;
	private final IEventBus bus;
	
	public ValidationConceptService(String branchPath) {
		this.branchPath = branchPath;
		this.bus = ApplicationContext.getServiceForClass(IEventBus.class);
	}

	@Override
	public boolean isActive(String conceptId) {
		return SnomedRequests.prepareSearchConcept()
				.filterByActive(true)
				.filterById(conceptId)
				.setLimit(0)
				.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
				.execute(bus)
				.getSync()
				.getTotal() > 0;
	}

	@Override
	public Set<String> findStatedAncestorsOfConcept(Concept concept) {
		
		Set<String> statedParentIds = concept.getRelationships().stream()
			.filter(r -> r.isActive() && Concepts.STATED_RELATIONSHIP.equals(r.getCharacteristicTypeId()) && Concepts.IS_A.equals(r.getTypeId()))
			.map(Relationship::getDestinationId)
			.collect(toSet());
		
		if (statedParentIds.isEmpty()) {
			return emptySet();
		}
		
		Set<String> statedAncestorsAndSelfIds = getStatedAncestorsAndSelfIds(concept.getId(), statedParentIds);
		
		LOGGER.info("Stated ancestors of concept '{}': '{}'", concept.getId(), COMMA_JOINER.join(statedAncestorsAndSelfIds));
		
		return statedAncestorsAndSelfIds;
	}

	@Override
	public Set<String> findTopLevelHierarchiesOfConcept(Concept concept) {
		
		Set<String> statedParentIds = concept.getRelationships().stream()
				.filter(r -> r.isActive() && Concepts.STATED_RELATIONSHIP.equals(r.getCharacteristicTypeId()) && Concepts.IS_A.equals(r.getTypeId()))
				.map(Relationship::getDestinationId)
				.collect(toSet());
		
		if (statedParentIds.isEmpty()) {
			return emptySet();
		}
		
		Set<String> topLevelConceptIds = getAllTopLevelHierarchies();
		
		if (topLevelConceptIds.contains(concept.getId())) {
			LOGGER.info("Top level hierarchies of concept '{}': '{}'", concept.getId(), concept.getId());
			return Collections.singleton(concept.getId());
		}
		
		Set<String> directTopLevelChildrenIds = Sets.intersection(topLevelConceptIds, statedParentIds);
		
		Set<String> results;
		if (!directTopLevelChildrenIds.isEmpty()) {
			results = directTopLevelChildrenIds;
		} else {
			results = Sets.intersection(topLevelConceptIds, getStatedAncestorsAndSelfIds(concept.getId(), statedParentIds)); 
		}
		
		LOGGER.info("Top level hierarchies of concept '{}': '{}'", concept.getId(), COMMA_JOINER.join(results));
		
		return results;
	}

	@Override
	public Set<String> getAllTopLevelHierarchies() {
		return SnomedRequests.prepareSearchConcept()
					.all()
					.filterByActive(true)
					.filterByStatedParent(Concepts.ROOT_CONCEPT)
					.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
					.execute(bus)
					.then(concepts -> concepts.getItems().stream().map(SnomedConcept::getId).collect(toSet()))
					.getSync();
	}
    
	@Override
	public Set<String> findStatedAncestorsOfConcepts(List<String> conceptIds) {
		
		Set<String> statedAncestorAndSelfIds = SnomedRequests.prepareSearchConcept()
			.setLimit(conceptIds.size())
			.filterByIds(conceptIds)
			.filterByActive(true)
			.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
			.execute(bus)
			.then(concepts -> {
				
				Set<String> statedAncestorIds = newHashSet();
				
				concepts.forEach(c -> {
					
					for (long id : c.getStatedAncestorIds()) {
						statedAncestorIds.add(String.valueOf(id));
					}
					
					for (long id : c.getStatedParentIds()) {
						statedAncestorIds.add(String.valueOf(id));
					}
					
					statedAncestorIds.add(c.getId());
					
				});
				
				statedAncestorIds.remove(IComponent.ROOT_ID);
				statedAncestorIds.remove(Concepts.ROOT_CONCEPT);
				
				return statedAncestorIds;
			})
			.getSync();
		
		LOGGER.info("Stated ancestors of concepts '{}': '{}'", COMMA_JOINER.join(conceptIds), COMMA_JOINER.join(statedAncestorAndSelfIds));
		
		return statedAncestorAndSelfIds;
	}

	private Set<String> getStatedAncestorsAndSelfIds(String conceptId, Set<String> statedParentIds) {
		return SnomedRequests.prepareSearchConcept()
			.setLimit(statedParentIds.size())
			.filterByIds(statedParentIds)
			.filterByActive(true)
			.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
			.execute(bus)
			.then(concepts -> {
				
				Set<String> statedAncestorIds = newHashSet();
				
				concepts.forEach(c -> {
					
					for (long id : c.getStatedAncestorIds()) {
						statedAncestorIds.add(String.valueOf(id));
					}
					
					for (long id : c.getStatedParentIds()) {
						statedAncestorIds.add(String.valueOf(id));
					}
					
				});
				
				statedAncestorIds.add(conceptId);
				statedAncestorIds.remove(IComponent.ROOT_ID);
				statedAncestorIds.remove(Concepts.ROOT_CONCEPT);
				
				return statedAncestorIds;
			})
			.getSync();
	}
}
