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
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.StreamSupport;

import org.eclipse.emf.cdo.transaction.CDOTransaction;

import com.b2international.commons.options.OptionsBuilder;
import com.b2international.snowowl.core.SnowOwlApplication;
import com.b2international.snowowl.core.domain.IComponent;
import com.b2international.snowowl.core.merge.ConflictingAttributeImpl;
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
				.filter(SnomedOWLExpressionRefSetMember::isActive)
				.collect(toSet());

		String branchPath = BranchPathUtils.createPath(transaction).getPath();
		
		SnomedOWLExpressionConverter converter = new RepositoryRequest<>(SnomedDatastoreActivator.REPOSITORY_UUID,
				new BranchRequest<>(branchPath, 
					new RevisionIndexReadRequest<>(branchContext -> {
						return new SnomedOWLExpressionConverter(branchContext);
					})
				)
			).execute(SnowOwlApplication.INSTANCE.getEnviroment());
		
		Set<String> referencedConceptIds = newHashSet();
		
		Multimap<String, String> memberToTypeIdsMap = HashMultimap.create();
		Multimap<String, String> memberToDestinationIdsMap = HashMultimap.create();
		
		for (SnomedOWLExpressionRefSetMember member : newOrDirtyOwlMembers) {
			
			SnomedOWLExpressionConverterResult result = converter.toSnomedOWLRelationships(member.getReferencedComponentId(), member.getOwlExpression());
			
			if (result.getClassAxiomRelationships() != null) {
				
				result.getClassAxiomRelationships().forEach( axiomRelationship -> {
					memberToTypeIdsMap.put(member.getUuid(), axiomRelationship.getTypeId());
					memberToDestinationIdsMap.put(member.getUuid(), axiomRelationship.getDestinationId());
				});
				
			} else if (result.getGciAxiomRelationships() != null) {
				
				result.getGciAxiomRelationships().forEach( axiomRelationship -> {
					memberToTypeIdsMap.put(member.getUuid(), axiomRelationship.getTypeId());
					memberToDestinationIdsMap.put(member.getUuid(), axiomRelationship.getDestinationId());
				});
				
			}
			
		}
		
		referencedConceptIds.addAll(memberToTypeIdsMap.values());
		referencedConceptIds.addAll(memberToDestinationIdsMap.values());
		
		Set<String> existingInactivateConceptIds = SnomedRequests.prepareSearchConcept()
				.filterByIds(referencedConceptIds)
				.filterByActive(false)
				.all()
				.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
				.execute(getEventBus())
				.then(concepts -> concepts.getItems().stream().map(IComponent::getId).collect(toSet()))
				.getSync();
		
		Iterable<Concept> newOrDirtyConcepts = Iterables.concat(ComponentUtils2.getDirtyObjects(transaction, Concept.class),
				ComponentUtils2.getNewObjects(transaction, Concept.class));

		Set<String> newInactivateConceptIds = newHashSet();
		
		for (Concept concept : newOrDirtyConcepts) {
			
			String conceptId = concept.getId();
			
			if (referencedConceptIds.contains(conceptId)) {
				
				if (concept.isActive()) {
					existingInactivateConceptIds.remove(conceptId);
				} else {
					existingInactivateConceptIds.add(conceptId);
				}
				
			} else if (!concept.isActive()) {
				newInactivateConceptIds.add(concept.getId());
			}
			
		}

		// None of the concepts referenced by an active owl expression member were inactive
		if (existingInactivateConceptIds.isEmpty() && newInactivateConceptIds.isEmpty()) {
			return emptySet();
		}
		
		List<MergeConflict> conflicts = newArrayList();
		
		if (!existingInactivateConceptIds.isEmpty()) {
			
			for (SnomedOWLExpressionRefSetMember member : newOrDirtyOwlMembers) {
				
				memberToTypeIdsMap.get(member.getUuid()).stream()
				.filter(existingInactivateConceptIds::contains)
				.forEach( typeId -> {
					conflicts.add(buildMemberConflict(member.getUuid(), typeId, false));
				});
				
				memberToDestinationIdsMap.get(member.getUuid()).stream()
				.filter(existingInactivateConceptIds::contains)
				.forEach( destinationId -> {
					conflicts.add(buildMemberConflict(member.getUuid(), destinationId, true));
				});
				
			}
			
		}
		
		if (!newInactivateConceptIds.isEmpty()) {
			
			SnomedReferenceSetMembers members = SnomedRequests.prepareSearchMember()
				.all()
				.filterByActive(true)
				.filterByRefSetType(SnomedRefSetType.OWL_AXIOM)
				.filterByProps(OptionsBuilder.newBuilder().put(SnomedRefSetMemberSearchRequestBuilder.OWL_EXPRESSION_CONCEPTID, newInactivateConceptIds).build())
				.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
				.execute(getEventBus())
				.getSync();
			
			for (SnomedReferenceSetMember member : members) {
				
				String owlExpression = (String) member.getProperties().get(SnomedRf2Headers.FIELD_OWL_EXPRESSION);
				
				if (!Strings.isNullOrEmpty(owlExpression)) {
					
					SnomedOWLExpressionConverterResult result = converter.toSnomedOWLRelationships(member.getReferencedComponentId(), owlExpression);
					
					if (result.getClassAxiomRelationships() != null) {
						
						for (SnomedOWLRelationshipDocument relationship : result.getClassAxiomRelationships()) {
							
							if (newInactivateConceptIds.contains(relationship.getDestinationId())) {
								if (conflicts.stream().noneMatch(conflict -> conflict.getComponentId().equals(relationship.getDestinationId())
										&& conflict.getType() == ConflictType.CAUSES_INACTIVE_REFERENCE)) {
									conflicts.add(buildConceptConflict(relationship.getDestinationId()));
									break;
								}
							} else if (newInactivateConceptIds.contains(relationship.getTypeId())) {
								if (conflicts.stream().noneMatch(conflict -> conflict.getComponentId().equals(relationship.getTypeId())
										&& conflict.getType() == ConflictType.CAUSES_INACTIVE_REFERENCE)) {
									conflicts.add(buildConceptConflict(relationship.getTypeId()));
									break;
								}
							}
							
						}
						
					} else if (result.getGciAxiomRelationships() != null) {
						
						for (SnomedOWLRelationshipDocument relationship : result.getGciAxiomRelationships()) {
							
							if (newInactivateConceptIds.contains(relationship.getDestinationId())) {
								if (conflicts.stream().noneMatch(conflict -> conflict.getComponentId().equals(relationship.getDestinationId())
										&& conflict.getType() == ConflictType.CAUSES_INACTIVE_REFERENCE)) {
									conflicts.add(buildConceptConflict(relationship.getDestinationId()));
									break;
								}
							} else if (newInactivateConceptIds.contains(relationship.getTypeId())) {
								if (conflicts.stream().noneMatch(conflict -> conflict.getComponentId().equals(relationship.getTypeId())
										&& conflict.getType() == ConflictType.CAUSES_INACTIVE_REFERENCE)) {
									conflicts.add(buildConceptConflict(relationship.getTypeId()));
									break;
								}
							}
							
						}

					}
					
				}
				
			}
			
		}
		
		return conflicts;
	}

	private MergeConflictImpl buildMemberConflict(String memberId, String referencedId, boolean isDestinationId) {
		
		ConflictingAttributeImpl attribute = ConflictingAttributeImpl.builder()
				.property(isDestinationId ? "destinationId" : "typeId")
				.value(referencedId)
				.build();
		
		return MergeConflictImpl.builder()
				.componentId(memberId)
				.componentType(SnomedRefSetPackage.Literals.SNOMED_OWL_EXPRESSION_REF_SET_MEMBER.getName())
				.conflictingAttribute(attribute)
				.type(ConflictType.HAS_INACTIVE_REFERENCE)
				.build();
		
	}
	
	private MergeConflictImpl buildConceptConflict(String conceptId) {
		
		ConflictingAttributeImpl attribute = ConflictingAttributeImpl.builder()
				.property("active")
				.oldValue("true")
				.value("false")
				.build();
		
		return MergeConflictImpl.builder()
				.componentId(conceptId)
				.componentType(SnomedPackage.Literals.CONCEPT.getName())
				.conflictingAttribute(attribute)
				.type(ConflictType.CAUSES_INACTIVE_REFERENCE)
				.build();
		
	}

}
