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
package com.b2international.snowowl.datastore.server.snomed.merge.rules;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.StreamSupport;

import org.eclipse.emf.cdo.transaction.CDOTransaction;

import com.b2international.commons.options.OptionsBuilder;
import com.b2international.snowowl.core.SnowOwlApplication;
import com.b2international.snowowl.core.domain.IComponent;
import com.b2international.snowowl.core.merge.MergeConflict;
import com.b2international.snowowl.core.merge.MergeConflict.ConflictType;
import com.b2international.snowowl.core.merge.MergeConflictImpl;
import com.b2international.snowowl.datastore.BranchPathUtils;
import com.b2international.snowowl.datastore.request.BranchRequest;
import com.b2international.snowowl.datastore.request.RepositoryRequest;
import com.b2international.snowowl.datastore.request.RevisionIndexReadRequest;
import com.b2international.snowowl.datastore.utils.ComponentUtils2;
import com.b2international.snowowl.snomed.Concept;
import com.b2international.snowowl.snomed.SnomedPackage;
import com.b2international.snowowl.snomed.common.SnomedRf2Headers;
import com.b2international.snowowl.snomed.core.domain.refset.SnomedReferenceSetMember;
import com.b2international.snowowl.snomed.core.domain.refset.SnomedReferenceSetMembers;
import com.b2international.snowowl.snomed.datastore.SnomedDatastoreActivator;
import com.b2international.snowowl.snomed.datastore.index.entry.SnomedOWLRelationshipDocument;
import com.b2international.snowowl.snomed.datastore.request.SnomedOWLExpressionConverter;
import com.b2international.snowowl.snomed.datastore.request.SnomedOWLExpressionConverterResult;
import com.b2international.snowowl.snomed.datastore.request.SnomedRefSetMemberSearchRequestBuilder;
import com.b2international.snowowl.snomed.datastore.request.SnomedRequests;
import com.b2international.snowowl.snomed.snomedrefset.SnomedOWLExpressionRefSetMember;
import com.b2international.snowowl.snomed.snomedrefset.SnomedRefSetMember;
import com.b2international.snowowl.snomed.snomedrefset.SnomedRefSetPackage;
import com.b2international.snowowl.snomed.snomedrefset.SnomedRefSetType;
import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

/**
 * @since 6.16.2
 */
public class SnomedInvalidOwlExpressionMergeConflictRule extends AbstractSnomedMergeConflictRule {

	@Override
	public Collection<MergeConflict> validate(CDOTransaction transaction) {
		
		Set<SnomedOWLExpressionRefSetMember> newOrDirtyOwlMembers = StreamSupport.stream(Iterables.concat(
				ComponentUtils2.getDirtyObjects(transaction, SnomedRefSetMember.class),
				ComponentUtils2.getNewObjects(transaction, SnomedRefSetMember.class)
			).spliterator(), false)
				.filter(SnomedOWLExpressionRefSetMember.class::isInstance)
				.map(SnomedOWLExpressionRefSetMember.class::cast)
				.collect(toSet());

		Set<Concept> newOrDirtyConcepts = StreamSupport.stream(Iterables.concat(
				ComponentUtils2.getDirtyObjects(transaction, Concept.class),
				ComponentUtils2.getNewObjects(transaction, Concept.class)
			).spliterator(), false)
				.collect(toSet());
		
		if (newOrDirtyConcepts.isEmpty() && newOrDirtyOwlMembers.isEmpty()) {
			return emptySet();
		}
		
		String branchPath = BranchPathUtils.createPath(transaction).getPath();
		
		SnomedOWLExpressionConverter converter = new RepositoryRequest<>(SnomedDatastoreActivator.REPOSITORY_UUID,
				new BranchRequest<>(branchPath, 
					new RevisionIndexReadRequest<>(branchContext -> {
						return new SnomedOWLExpressionConverter(branchContext);
					})
				)
			).execute(SnowOwlApplication.INSTANCE.getEnviroment());
		
		// Find members that would cause an inactive reference except the ones that have been fixed in this transaction
		
		Set<String> referencedConceptIds = newHashSet();
		
		Multimap<String, String> newOrDirtyMemberToTypeIdsMap = HashMultimap.create();
		Multimap<String, String> newOrDirtyMemberToDestinationIdsMap = HashMultimap.create();
		
		Map<String, SnomedOWLExpressionConverterResult> newOrDirtyMemberToResultMap = newHashMap();
		
		for (SnomedOWLExpressionRefSetMember member : newOrDirtyOwlMembers) {
			
			if (member.isActive()) {
				
				SnomedOWLExpressionConverterResult result = converter.toSnomedOWLRelationships(member.getReferencedComponentId(), member.getOwlExpression());
				newOrDirtyMemberToResultMap.put(member.getUuid(), result);
				
				if (result.getClassAxiomRelationships() != null) {
					
					result.getClassAxiomRelationships().forEach( axiomRelationship -> {
						newOrDirtyMemberToTypeIdsMap.put(member.getUuid(), axiomRelationship.getTypeId());
						newOrDirtyMemberToDestinationIdsMap.put(member.getUuid(), axiomRelationship.getDestinationId());
					});
					
				} else if (result.getGciAxiomRelationships() != null) {
					
					result.getGciAxiomRelationships().forEach( axiomRelationship -> {
						newOrDirtyMemberToTypeIdsMap.put(member.getUuid(), axiomRelationship.getTypeId());
						newOrDirtyMemberToDestinationIdsMap.put(member.getUuid(), axiomRelationship.getDestinationId());
					});
					
				}
				
			}
			
		}
		
		referencedConceptIds.addAll(newOrDirtyMemberToTypeIdsMap.values());
		referencedConceptIds.addAll(newOrDirtyMemberToDestinationIdsMap.values());
		
		Set<String> inactiveReferencedConceptIds = newHashSet();
		
		if (!referencedConceptIds.isEmpty()) {
			
			Set<String> inactiveReferencedConceptIdsOnTarget = SnomedRequests.prepareSearchConcept()
				.setLimit(referencedConceptIds.size())
				.filterByIds(referencedConceptIds)
				.filterByActive(false)
				.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
				.execute(getEventBus())
				.then(concepts -> concepts.getItems().stream().map(IComponent::getId).collect(toSet()))
				.getSync();

			Set<String> fixedConceptIdsInTransaction = newOrDirtyConcepts.stream() // concepts that have been "fixed" in this transaction
				.filter(concept -> inactiveReferencedConceptIdsOnTarget.contains(concept.getId()) && concept.isActive())
				.map(Concept::getId)
				.collect(toSet());
			
			inactiveReferencedConceptIds.addAll(inactiveReferencedConceptIdsOnTarget);
			inactiveReferencedConceptIds.removeAll(fixedConceptIdsInTransaction);
			
		}
		
		List<MergeConflict> conflicts = newArrayList();
		
		if (!inactiveReferencedConceptIds.isEmpty()) {
			
			for (SnomedOWLExpressionRefSetMember member : newOrDirtyOwlMembers) {

				if (member.isActive()) {
					
					newOrDirtyMemberToTypeIdsMap.get(member.getUuid()).stream()
						.filter(inactiveReferencedConceptIds::contains)
						.forEach( typeId -> {
							conflicts.add(buildMemberConflict(member, typeId, false));
						});
					
					newOrDirtyMemberToDestinationIdsMap.get(member.getUuid()).stream()
						.filter(inactiveReferencedConceptIds::contains)
						.forEach( destinationId -> {
							conflicts.add(buildMemberConflict(member, destinationId, true));
						});
					
				}
				
			}
			
		}
		
		// Find concepts that would cause and inactive reference except the ones that have been fixed in this transaction
		
		Set<String> inactiveConceptIdsInTransaction = newOrDirtyConcepts.stream()
			.filter(concept -> !concept.isActive())
			.map(Concept::getId)
			.collect(toSet());
		
		if (!inactiveConceptIdsInTransaction.isEmpty()) {
			
			SnomedReferenceSetMembers members = SnomedRequests.prepareSearchMember()
				.all()
				.filterByActive(true)
				.filterByRefSetType(SnomedRefSetType.OWL_AXIOM)
				.filterByProps(OptionsBuilder.newBuilder().put(SnomedRefSetMemberSearchRequestBuilder.OWL_EXPRESSION_CONCEPTID, inactiveConceptIdsInTransaction).build())
				.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
				.execute(getEventBus())
				.getSync();
			
			Set<String> fixedMemberIdsInTransaction = newOrDirtyOwlMembers.stream()
				.filter(member -> !member.isActive())
				.map(SnomedOWLExpressionRefSetMember::getUuid)
				.collect(toSet());
			
			Map<String, SnomedOWLExpressionRefSetMember> newOrDirtyMemberIdToMemberMap = newOrDirtyOwlMembers.stream()
				.filter(member -> member.isActive())
				.collect(toMap(member -> member.getUuid(), member -> member));
			
			for (SnomedReferenceSetMember member : members) {
				
				if (fixedMemberIdsInTransaction.contains(member.getId())) {
					continue; // skip members that have been inactivated in this transaction
				}
				
				String owlExpression;
				SnomedOWLExpressionConverterResult result = SnomedOWLExpressionConverterResult.EMPTY;
				
				if (newOrDirtyMemberIdToMemberMap.containsKey(member.getId())) {
					
					SnomedOWLExpressionRefSetMember owlExpressionRefSetMember = newOrDirtyMemberIdToMemberMap.get(member.getId());
					owlExpression = owlExpressionRefSetMember.getOwlExpression();
					result = newOrDirtyMemberToResultMap.getOrDefault(member.getId(), SnomedOWLExpressionConverterResult.EMPTY);
					
				} else {
					
					owlExpression = (String) member.getProperties().get(SnomedRf2Headers.FIELD_OWL_EXPRESSION);
					
					if (!Strings.isNullOrEmpty(owlExpression)) {
						result = converter.toSnomedOWLRelationships(member.getReferencedComponentId(), owlExpression);
					}
					
				}
				
				if (result.getClassAxiomRelationships() != null) {
					
					for (SnomedOWLRelationshipDocument relationship : result.getClassAxiomRelationships()) {
						
						if (inactiveConceptIdsInTransaction.contains(relationship.getDestinationId())) {
							if (conflicts.stream().noneMatch(conflict -> conflict.getComponentId().equals(relationship.getDestinationId())
									&& conflict.getType() == ConflictType.CAUSES_INACTIVE_REFERENCE)) {
								conflicts.add(buildConceptConflict(member.getId(), owlExpression, relationship.getDestinationId(), true));
								break;
							}
						} else if (inactiveConceptIdsInTransaction.contains(relationship.getTypeId())) {
							if (conflicts.stream().noneMatch(conflict -> conflict.getComponentId().equals(relationship.getTypeId())
									&& conflict.getType() == ConflictType.CAUSES_INACTIVE_REFERENCE)) {
								conflicts.add(buildConceptConflict(member.getId(), owlExpression, relationship.getTypeId(), false));
								break;
							}
						}
						
					}
					
				} else if (result.getGciAxiomRelationships() != null) {
					
					for (SnomedOWLRelationshipDocument relationship : result.getGciAxiomRelationships()) {
						
						if (inactiveConceptIdsInTransaction.contains(relationship.getDestinationId())) {
							if (conflicts.stream().noneMatch(conflict -> conflict.getComponentId().equals(relationship.getDestinationId())
									&& conflict.getType() == ConflictType.CAUSES_INACTIVE_REFERENCE)) {
								conflicts.add(buildConceptConflict(member.getId(), owlExpression, relationship.getDestinationId(), true));
								break;
							}
						} else if (inactiveConceptIdsInTransaction.contains(relationship.getTypeId())) {
							if (conflicts.stream().noneMatch(conflict -> conflict.getComponentId().equals(relationship.getTypeId())
									&& conflict.getType() == ConflictType.CAUSES_INACTIVE_REFERENCE)) {
								conflicts.add(buildConceptConflict(member.getId(), owlExpression, relationship.getTypeId(), false));
								break;
							}
						}
						
					}

				}
				
			}
			
		}
		
		return conflicts;
	}

	private MergeConflictImpl buildMemberConflict(SnomedOWLExpressionRefSetMember member, String referencedId, boolean isDestinationId) {
		
		String memberConflictMessage = String.format(MergeConflictImpl.DEFAULT_CONFLICT_MESSAGE,
				SnomedRefSetPackage.Literals.SNOMED_OWL_EXPRESSION_REF_SET_MEMBER.getName(),
				member.getUuid(),
				ConflictType.HAS_INACTIVE_REFERENCE,
				String.format(", OWL expression '%s' is referencing inactive concept: %s -> %s",
						member.getOwlExpression(),
						isDestinationId ? "destinationId" : "typeId",
						referencedId));
		
		return MergeConflictImpl.builder()
				.componentId(member.getUuid())
				.componentType(SnomedRefSetPackage.Literals.SNOMED_OWL_EXPRESSION_REF_SET_MEMBER.getName())
				.type(ConflictType.HAS_INACTIVE_REFERENCE)
				.message(memberConflictMessage)
				.build();
		
	}
	
	private MergeConflictImpl buildConceptConflict(String memberId, String owlExpression, String conceptId, boolean isDestinationId) {
		
		String conceptConflictMessage = String.format(MergeConflictImpl.DEFAULT_CONFLICT_MESSAGE,
				SnomedPackage.Literals.CONCEPT.getName(),
				conceptId,
				ConflictType.CAUSES_INACTIVE_REFERENCE,
				String.format(", OWL expression with ID '%s' is referencing it as '%s' -> '%s'",
					memberId,
					isDestinationId ? "destinationId" : "typeId",
					owlExpression));
		
		return MergeConflictImpl.builder()
				.componentId(conceptId)
				.componentType(SnomedPackage.Literals.CONCEPT.getName())
				.type(ConflictType.CAUSES_INACTIVE_REFERENCE)
				.message(conceptConflictMessage)
				.build();
		
	}

}
