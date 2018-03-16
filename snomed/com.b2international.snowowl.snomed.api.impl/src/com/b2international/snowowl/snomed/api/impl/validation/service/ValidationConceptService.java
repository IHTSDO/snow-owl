package com.b2international.snowowl.snomed.api.impl.validation.service;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import org.ihtsdo.drools.domain.Concept;
import org.ihtsdo.drools.service.ConceptService;

import com.b2international.snowowl.eventbus.IEventBus;
import com.b2international.snowowl.snomed.SnomedConstants.Concepts;
import com.b2international.snowowl.snomed.core.domain.SnomedConcept;
import com.b2international.snowowl.snomed.core.domain.SnomedConcepts;
import com.b2international.snowowl.snomed.datastore.SnomedDatastoreActivator;
import com.b2international.snowowl.snomed.datastore.request.SnomedRequests;

public class ValidationConceptService implements ConceptService {

	private final String branchPath;
	private final IEventBus bus;

	public ValidationConceptService(String branchPath, IEventBus bus) {
		this.branchPath = branchPath;
		this.bus = bus;
	}

	@Override
	public boolean isActive(String conceptId) {
		return SnomedRequests.prepareGetConcept(conceptId)
				.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
				.execute(bus)
				.getSync()
				.isActive();
	}

	@Override
	public Set<String> findStatedAncestorsOfConcept(Concept concept) {
		
		return Collections.emptySet();
	}

	@Override
	public Set<String> findTopLevelHierachiesOfConcept(Concept concept) {
		SnomedConcepts concepts = SnomedRequests.prepareSearchConcept()
				.filterByEcl(">" + concept.getId() + " AND <!" + Concepts.ROOT_CONCEPT)
				.filterByActive(true)
				.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
				.execute(bus)
				.getSync();
			
		return concepts.getItems().stream().map(SnomedConcept::getId).collect(Collectors.toSet());
	}

	@Override
	public Set<String> getAllTopLevelHierachies() {
		SnomedConcepts concepts = SnomedRequests.prepareSearchConcept()
				.filterByEcl("<!" + Concepts.ROOT_CONCEPT)
				.filterByActive(true)
				.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
				.execute(bus)
				.getSync();
			
		return concepts.getItems().stream().map(SnomedConcept::getId).collect(Collectors.toSet());
	}

}
