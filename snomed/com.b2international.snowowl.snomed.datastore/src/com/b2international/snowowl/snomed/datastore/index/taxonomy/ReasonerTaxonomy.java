/*
 * Copyright 2018-2019 B2i Healthcare Pte Ltd, http://b2i.sg
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
package com.b2international.snowowl.snomed.datastore.index.taxonomy;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.Set;

import com.b2international.collections.longs.LongList;
import com.b2international.collections.longs.LongSet;
import com.b2international.snowowl.snomed.datastore.ConcreteDomainFragment;
import com.b2international.snowowl.snomed.datastore.StatementFragment;
import com.google.common.collect.Multimap;

/**
 * Represents a snapshot of the ontology for reasoner input and normal form generation.
 * 
 * @since 7.0
 */
public final class ReasonerTaxonomy implements IReasonerTaxonomy {

	private final InternalIdMap conceptMap;
	private final InternalIdMap definedConceptMap;

	private final InternalIdEdges statedAncestors;
	private final InternalIdEdges statedDescendants;

	private final InternalSctIdSet exhaustiveConcepts;

	private final InternalIdMultimap<StatementFragment> subclassOfStatements;
	private final InternalIdMultimap<StatementFragment> equivalentStatements;
	private final InternalIdMultimap<StatementFragment> existingInferredRelationships;
	private final InternalIdMultimap<StatementFragment> additionalGroupedRelationships;
	
	private final InternalIdMultimap<String> statedAdditionalAxioms;
	private final LongSet neverGroupedTypeIds;
	private Set<PropertyChain> propertyChains;
	
	private final Multimap<String, ConcreteDomainFragment> statedConcreteDomainMembers;
	private final Multimap<String, ConcreteDomainFragment> inferredConcreteDomainMembers;
	private final Multimap<String, ConcreteDomainFragment> additionalGroupedConcreteDomainMembers;

	private final InternalIdEdges inferredAncestors;
	private final InternalSctIdSet unsatisfiableConcepts;
	private final InternalSctIdMultimap equivalentConcepts;
	private final LongList iterationOrder;

	/*package*/ ReasonerTaxonomy(
			final InternalIdMap conceptMap, 
			final InternalIdMap definedConceptMap,
			final InternalIdEdges statedAncestors,
			final InternalIdEdges statedDescendants, 
			
			final InternalSctIdSet exhaustiveConcepts, 
			
			final InternalIdMultimap<StatementFragment> subclassOfStatements,
			final InternalIdMultimap<StatementFragment> equivalentStatements,
			final InternalIdMultimap<StatementFragment> existingInferredRelationships,
			final InternalIdMultimap<StatementFragment> additionalGroupedRelationships, 
			
			final InternalIdMultimap<String> statedAdditionalAxioms,
			final LongSet neverGroupedTypeIds,
			final Set<PropertyChain> propertyChains, 
			
			final Multimap<String, ConcreteDomainFragment> statedConcreteDomainMembers,
			final Multimap<String, ConcreteDomainFragment> inferredConcreteDomainMembers,
			final Multimap<String, ConcreteDomainFragment> additionalGroupedConcreteDomainMembers, 
			
			final InternalIdEdges inferredAncestors,
			final InternalSctIdSet unsatisfiableConcepts,
			final InternalSctIdMultimap equivalentConcepts, 
			final LongList iterationOrder) {

		this.conceptMap = conceptMap;
		this.definedConceptMap = definedConceptMap;
		this.statedAncestors = statedAncestors;
		this.statedDescendants = statedDescendants;
		
		this.exhaustiveConcepts = exhaustiveConcepts;
		
		this.subclassOfStatements = subclassOfStatements;
		this.equivalentStatements = equivalentStatements;
		this.existingInferredRelationships = existingInferredRelationships;
		this.additionalGroupedRelationships = additionalGroupedRelationships;
		
		this.statedAdditionalAxioms = statedAdditionalAxioms;
		this.neverGroupedTypeIds = neverGroupedTypeIds;
		this.propertyChains = propertyChains;
		
		this.statedConcreteDomainMembers = statedConcreteDomainMembers;
		this.inferredConcreteDomainMembers = inferredConcreteDomainMembers;
		this.additionalGroupedConcreteDomainMembers = additionalGroupedConcreteDomainMembers;
		
		this.inferredAncestors = inferredAncestors;
		this.unsatisfiableConcepts = unsatisfiableConcepts;
		this.equivalentConcepts = equivalentConcepts;
		this.iterationOrder = iterationOrder;
	}

	public InternalIdMap getConceptMap() {
		return conceptMap;
	}
	
	public InternalIdMap getDefinedConceptMap() {
		return definedConceptMap;
	}

	public InternalIdEdges getStatedAncestors() {
		return statedAncestors;
	}

	public InternalIdEdges getStatedDescendants() {
		return statedDescendants;
	}

	public InternalIdEdges getInferredAncestors() {
		return checkNotNull(inferredAncestors, "Inferred ancestors are unset on this taxonomy.");
	}

	@Override
	public InternalSctIdSet getUnsatisfiableConcepts() {
		return checkNotNull(unsatisfiableConcepts, "Unsatisfiable concept IDs are unset on this taxonomy.");
	}

	@Override
	public InternalSctIdMultimap getEquivalentConcepts() {
		return checkNotNull(equivalentConcepts, "Inferred equivalences are unset on this taxonomy.");
	}

	public InternalSctIdSet getExhaustiveConcepts() {
		return exhaustiveConcepts;
	}

	public InternalIdMultimap<StatementFragment> getSubclassOfStatements() {
		return subclassOfStatements;
	}
	
	public InternalIdMultimap<StatementFragment> getEquivalentStatements() {
		return equivalentStatements;
	}
	
	public InternalIdMultimap<StatementFragment> getExistingInferredRelationships() {
		return existingInferredRelationships;
	}
	
	public InternalIdMultimap<StatementFragment> getAdditionalGroupedRelationships() {
		return additionalGroupedRelationships;
	}

	public InternalIdMultimap<String> getStatedAdditionalAxioms() {
		return statedAdditionalAxioms;
	}
	
	public LongSet getNeverGroupedTypeIds() {
		return neverGroupedTypeIds;
	}
	
	public Set<PropertyChain> getPropertyChains() {
		return propertyChains;
	}

	public Multimap<String, ConcreteDomainFragment> getStatedConcreteDomainMembers() {
		return statedConcreteDomainMembers;
	}
	
	public Multimap<String, ConcreteDomainFragment> getInferredConcreteDomainMembers() {
		return inferredConcreteDomainMembers;
	}
	
	public Multimap<String, ConcreteDomainFragment> getAdditionalGroupedConcreteDomainMembers() {
		return additionalGroupedConcreteDomainMembers;
	}
	
	public LongList getIterationOrder() {
		return iterationOrder;
	}

	public ReasonerTaxonomy withInferences(final InternalIdEdges newInferredAncestors, 
			final InternalSctIdSet newUnsatisfiableConcepts,
			final InternalSctIdMultimap newEquivalentConcepts, 
			final LongList iterationOrder) {

		checkNotNull(newInferredAncestors, "Inferred ancestors may not be null.");
		checkNotNull(newUnsatisfiableConcepts, "Inferred unsatisfiable concepts may not be null.");
		checkNotNull(newEquivalentConcepts, "Inferred equivalent concept sets may not be null.");
		checkNotNull(iterationOrder, "Inferred concept iteration order may not be null.");

		checkState(this.inferredAncestors == null, "Inferred ancestors are already present in this taxonomy.");
		checkState(this.unsatisfiableConcepts == null, "Inferred unsatisfiable concepts are already present in this taxonomy.");
		checkState(this.equivalentConcepts == null, "Inferred equivalent concept sets are already present in this taxonomy.");
		checkState(this.iterationOrder == null, "Inferred concept iteration order is already set in this taxonomy.");

		return new ReasonerTaxonomy(conceptMap, 
				definedConceptMap, 
				statedAncestors, 
				statedDescendants, 
				
				exhaustiveConcepts,
				
				subclassOfStatements,
				equivalentStatements,
				existingInferredRelationships,
				additionalGroupedRelationships,
				
				statedAdditionalAxioms,
				neverGroupedTypeIds,
				propertyChains,
				
				statedConcreteDomainMembers, 
				inferredConcreteDomainMembers,
				additionalGroupedConcreteDomainMembers,
				
				newInferredAncestors, 
				newUnsatisfiableConcepts,
				newEquivalentConcepts,
				iterationOrder);
	}
}
