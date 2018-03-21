package com.b2international.snowowl.snomed.api.impl.validation.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.ihtsdo.drools.domain.Concept;
import org.ihtsdo.drools.domain.Relationship;
import org.ihtsdo.drools.service.ConceptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.b2international.commons.FileUtils;
import com.b2international.snowowl.eventbus.IEventBus;
import com.b2international.snowowl.snomed.SnomedConstants.Concepts;
import com.b2international.snowowl.snomed.core.domain.SnomedConcept;
import com.b2international.snowowl.snomed.core.domain.SnomedConcepts;
import com.b2international.snowowl.snomed.datastore.SnomedDatastoreActivator;
import com.b2international.snowowl.snomed.datastore.request.SnomedRequests;

public class ValidationConceptService implements ConceptService {

	private final String branchPath;
	private final IEventBus bus;
	private static final Logger LOGGER = LoggerFactory.getLogger(FileUtils.class);
	
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
		List<String> statedParents = new ArrayList<>(concept.getRelationships().stream()
			.filter(r -> Concepts.STATED_RELATIONSHIP.equals(r.getCharacteristicTypeId()) && Concepts.IS_A.equals(r.getTypeId()))
			.map(Relationship::getDestinationId).collect(Collectors.toSet()));
		
		if (statedParents.isEmpty()) {
			return Collections.emptySet();
		}
		// Descendant of root
		String ecl = "<" + Concepts.ROOT_CONCEPT + " AND ";
		if (statedParents.size() > 1) {
			// We need to open brackets only if more than one parent
			ecl += "(";
		}
		for (int i = 0; i < statedParents.size(); i++) {
			if (i > 0) {
				ecl += " OR ";
			}
			// Ancestor and self of stated parent
			ecl += ">>" + statedParents.get(i);
		}
		if (statedParents.size() > 1) {
			ecl += ")";
		}
		
		SnomedConcepts concepts = SnomedRequests.prepareSearchConcept()
				.filterByEcl(ecl)
				.filterByActive(true)
				.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
				.execute(bus)
				.getSync();
		
		if(concepts.isEmpty()) {
			return Collections.emptySet();
		}
		
		Set<String> list = concepts.getItems().stream().map(SnomedConcept::getId).collect(Collectors.toSet());
		LOGGER.info("findStatedAncestorsOfConcept : " + list);	
		return list;
	}

	@Override
	public Set<String> findTopLevelHierachiesOfConcept(Concept concept) {
		// Can not perform ECL on the Concept because it may not be saved
		// Gather the stated parents and use those in ECL
		List<String> statedParents = new ArrayList<>(concept.getRelationships().stream()
			.filter(r -> Concepts.STATED_RELATIONSHIP.equals(r.getCharacteristicTypeId()) && Concepts.IS_A.equals(r.getTypeId()))
			.map(Relationship::getDestinationId).collect(Collectors.toSet()));
		LOGGER.info("Class name : " + this.getClass().getName() + ". Method: findTopLevelHierachiesOfConcept. statedParents : " +  statedParents);
		if (statedParents.isEmpty()) {
			return Collections.emptySet();
		}
		
		// Direct descendant of root
		String ecl = "<!" + Concepts.ROOT_CONCEPT + " AND ";
		if (statedParents.size() > 1) {
			// We need to open brackets only if more than one parent
			ecl += "(";
		}
		for (int a = 0; a < statedParents.size(); a++) {
			if (a > 0) {
				ecl += " OR ";
			}
			// Ancestor and self of stated parent
			ecl += ">>" + statedParents.get(a);
		}
		if (statedParents.size() > 1) {
			ecl += ")";
		}
		
		LOGGER.info("Class name : " + this.getClass().getName() + ". Method: findTopLevelHierachiesOfConcept. ecl : " +  ecl);
		SnomedConcepts concepts = SnomedRequests.prepareSearchConcept()
				.filterByEcl(ecl)
				.filterByActive(true)
				.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
				.execute(bus)
				.getSync();
		LOGGER.info("Class name : " + this.getClass().getName() + ". Method: findTopLevelHierachiesOfConcept. concepts : " +  concepts.getItems().stream().map(SnomedConcept::getId).collect(Collectors.toSet()));	
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
