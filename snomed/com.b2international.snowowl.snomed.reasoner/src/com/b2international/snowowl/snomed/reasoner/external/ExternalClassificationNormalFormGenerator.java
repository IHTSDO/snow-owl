/*
 * Copyright 2019 B2i Healthcare Pte Ltd, http://b2i.sg
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.b2international.snowowl.snomed.reasoner.external;

import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import com.b2international.commons.BooleanUtils;
import com.b2international.snowowl.core.domain.BranchContext;
import com.b2international.snowowl.snomed.SnomedConstants.Concepts;
import com.b2international.snowowl.snomed.core.domain.RelationshipModifier;
import com.b2international.snowowl.snomed.datastore.ConcreteDomainFragment;
import com.b2international.snowowl.snomed.datastore.StatementFragment;
import com.b2international.snowowl.snomed.datastore.index.entry.SnomedRelationshipIndexEntry;
import com.b2international.snowowl.snomed.datastore.request.SnomedRequests;
import com.b2international.snowowl.snomed.reasoner.classification.INormalFormGenerator;
import com.b2international.snowowl.snomed.reasoner.diff.OntologyChangeProcessor;
import com.google.common.base.Strings;

/**
 *
 * @since 6.12.2
 */
public class ExternalClassificationNormalFormGenerator implements INormalFormGenerator {

	private BranchContext context;
	private Collection<List<String>> relationships;

	public ExternalClassificationNormalFormGenerator(final BranchContext context, Collection<List<String>> relationships) {
		this.context = context;
		this.relationships = relationships;
	}
	
	@Override
	public void computeChanges(IProgressMonitor monitor, OntologyChangeProcessor<StatementFragment> statementProcessor,
			OntologyChangeProcessor<ConcreteDomainFragment> concreteDomainProcessor) {
		
		relationships.forEach(elements -> {
			
			String relationshipId = elements.get(0);
			boolean active = BooleanUtils.valueOf(elements.get(2));
			String sourceId = elements.get(4);
			String destinationId = elements.get(5);
			int group = Integer.valueOf(elements.get(6));
			String typeId = elements.get(7);
			boolean isUniversal = RelationshipModifier.UNIVERSAL.getConceptId().equals(elements.get(9));
			
			if (active) { // active -> INFERRED or UPDATED
				
				
				if (Strings.isNullOrEmpty(relationshipId)) { // INFERRED
					
					StatementFragment statementFragment = new StatementFragment(
						Long.valueOf(typeId),
						Long.valueOf(destinationId),
						false, // destination negated
						group,
						0, // union group
						isUniversal,
						-1L, // relationship id
						false, // is released
						false); // has stated pair
					
					statementProcessor.handleAddedSubject(sourceId, statementFragment);
					
				} else { // UPDATED
					
					StatementFragment statementFragment = new StatementFragment(
						Long.valueOf(typeId),
						Long.valueOf(destinationId),
						false, // destination negated
						group,
						0, // union group
						isUniversal,
						Long.valueOf(relationshipId), // relationship id
						false, // is released
						false); // has stated pair
					
					statementProcessor.handleUpdatedSubject(sourceId, statementFragment);
					
				}
				
			} else { // inactive -> REDUNDANT
				
				StatementFragment statementFragment = new StatementFragment(
						Long.valueOf(typeId),
						Long.valueOf(destinationId),
						false, // destination negated
						group,
						0, // union group
						isUniversal,
						Long.valueOf(relationshipId),
						isReleased(relationshipId),
						isStatedRelationshipPairExists(sourceId, typeId, destinationId, group));
				
				statementProcessor.handleRemovedSubject(sourceId, statementFragment);
				
			}
			
		});
		
	}

	private boolean isReleased(String relationshipId) {
		return SnomedRequests.prepareGetRelationship(relationshipId)
				.setFields(SnomedRelationshipIndexEntry.Fields.RELEASED)
				.build()
				.execute(context)
				.isReleased();
	}

	private boolean isStatedRelationshipPairExists(String sourceId, String typeId, String destinationId, int group) {
		return SnomedRequests.prepareSearchRelationship()
					.setLimit(0)
					.filterByActive(true)
					.filterBySource(sourceId)
					.filterByType(typeId)
					.filterByDestination(destinationId)
					.filterByGroup(group)
					.filterByCharacteristicType(Concepts.STATED_RELATIONSHIP)
					.build()
					.execute(context)
					.getTotal() > 0;
	}

}
