package com.b2international.snowowl.snomed.api.rest;

import static com.google.common.collect.Lists.newArrayList;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import com.b2international.commons.http.ExtendedLocale;
import com.b2international.snowowl.core.exceptions.BadRequestException;
import com.b2international.snowowl.eventbus.IEventBus;
import com.b2international.snowowl.snomed.api.impl.DescriptionService;
import com.b2international.snowowl.snomed.api.rest.domain.ExpandableRelationshipChange;
import com.b2international.snowowl.snomed.api.rest.domain.SnomedConceptMini;
import com.b2international.snowowl.snomed.core.domain.SnomedConcept;
import com.b2international.snowowl.snomed.core.domain.SnomedConcepts;
import com.b2international.snowowl.snomed.core.domain.SnomedDescription;
import com.b2international.snowowl.snomed.core.domain.classification.RelationshipChange;
import com.b2international.snowowl.snomed.core.domain.classification.RelationshipChanges;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;

public class SnomedResourceExpander {

	@Resource
	protected IEventBus bus;

	private static final String SOURCE_FSN = "source.fsn";
	private static final String TYPE_FSN = "type.fsn";
	private static final String DESTINATION_FSN = "destination.fsn";

	public static final String FSN = "fsn";

	public RelationshipChanges expandRelationshipChanges(String branchPath, RelationshipChanges relationshipChanges, List<ExtendedLocale> locales, List<String> expantions) {

		final DescriptionService descriptionService = new DescriptionService(bus, branchPath);
		List<RelationshipChange> extendedChanges = newArrayList();
		
		for (RelationshipChange change : relationshipChanges) {
			extendedChanges.add(new ExpandableRelationshipChange(change));
		}

		boolean expandSource = false;
		boolean expandType = false;
		boolean expandDestination = false;
		for (String expantion : expantions) {
			if (SOURCE_FSN.equals(expantion)) {
				expandSource = true;
			} else if (TYPE_FSN.equals(expantion)) {
				expandType = true;
			} else if (DESTINATION_FSN.equals(expantion)) {
				expandDestination = true;
			} else {
				throw new BadRequestException("Unrecognised expand parameter '%s'.", expantion);
			}
		}
		
		Set<String> conceptIds = new HashSet<>();
		for (RelationshipChange change : relationshipChanges) {
			if (expandSource) {
				conceptIds.add(change.getSourceId());
			}
			if (expandType) {
				conceptIds.add(change.getTypeId());
			}
			if (expandDestination) {
				conceptIds.add(change.getDestinationId());
			}			
		}
		
		Map<String, SnomedDescription> fullySpecifiedNames = descriptionService.getFullySpecifiedNames(conceptIds, locales);
		
		for (RelationshipChange change : extendedChanges) {
			if (change instanceof ExpandableRelationshipChange) {
				ExpandableRelationshipChange expandedChange = (ExpandableRelationshipChange) change;
				if (expandSource) {
					expandedChange.setSource(createConceptMini(change.getSourceId(), fullySpecifiedNames));
				}
				if (expandType) {
					expandedChange.setType(createConceptMini(change.getTypeId(), fullySpecifiedNames));
				}
				if (expandDestination) {
					expandedChange.setDestination(createConceptMini(change.getDestinationId(), fullySpecifiedNames));
				}			
			}
		}
		
		return RelationshipChanges.of(extendedChanges, relationshipChanges.getOffset(), relationshipChanges.getLimit(), relationshipChanges.getTotal());
	}
	
	public SnomedConcepts expandConcepts(String branchPath, SnomedConcepts concepts, 
			List<ExtendedLocale> locales, List<String> expantions) {
		
		if (expantions.isEmpty() || concepts.getItems().isEmpty()) {
			return concepts;
		}

		for (String expantion : expantions) {
			if (expantion.equals(FSN)) {
				final DescriptionService descriptionService = new DescriptionService(bus, branchPath);

				Set<String> conceptIds = FluentIterable.from(concepts.getItems())
						.transform(new Function<SnomedConcept, String>() {
							@Override public String apply(SnomedConcept input) {
								return input.getId();
							}})
						.toSet();
				
				Map<String, SnomedDescription> fullySpecifiedNames = descriptionService.getFullySpecifiedNames(conceptIds, locales);
				for (SnomedConcept iSnomedConcept : concepts) {
					SnomedConcept concept = (SnomedConcept) iSnomedConcept;
					concept.setFsn(fullySpecifiedNames.get(concept.getId()));
				}
			} else {
				throw new BadRequestException("Unrecognised expand parameter '%s'.", expantion);
			}
		}
		
		return concepts;
	}
	
	private SnomedConceptMini createConceptMini(String conceptId, Map<String, SnomedDescription> fullySpecifiedNames) {
		return new SnomedConceptMini(conceptId, getFsn(fullySpecifiedNames, conceptId));
	}

	private String getFsn(Map<String, SnomedDescription> fsnMap, String conceptId) {
		if (fsnMap.containsKey(conceptId)) {
			return fsnMap.get(conceptId).getTerm();
		} else {
			return conceptId;
		}
	}

}
