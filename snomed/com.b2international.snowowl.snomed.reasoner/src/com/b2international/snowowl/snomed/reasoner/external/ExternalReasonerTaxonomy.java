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

import static com.google.common.collect.Maps.newHashMap;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.b2international.snowowl.snomed.datastore.index.taxonomy.IInternalSctIdMultimap;
import com.b2international.snowowl.snomed.datastore.index.taxonomy.IInternalSctIdSet;
import com.b2international.snowowl.snomed.datastore.index.taxonomy.IReasonerTaxonomy;
import com.b2international.snowowl.snomed.datastore.index.taxonomy.InternalIdMap;
import com.b2international.snowowl.snomed.datastore.index.taxonomy.InternalSctIdMultimap;
import com.b2international.snowowl.snomed.datastore.index.taxonomy.InternalSctIdMultimap.Builder;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 *
 * @since 6.12.2
 */
public class ExternalReasonerTaxonomy implements IReasonerTaxonomy {

	private Multimap<String, String> equivalentConceptIds;

	public ExternalReasonerTaxonomy(Collection<List<String>> equivalentConceptLines) {
		this.equivalentConceptIds = processEquivalentConceptLines(equivalentConceptLines);
	}
	
	private Multimap<String, String> processEquivalentConceptLines(Collection<List<String>> equivalentConceptLines) {
		
		ArrayListMultimap<String, String> equivalentConcepts = ArrayListMultimap.create();
		Map<String, String> knownEquivalentSetIds = newHashMap();
		
		equivalentConceptLines.forEach(elements -> {
			
			String referencedComponent = elements.get(5); // equivalent concept
			String mapTarget = elements.get(6); // id of equivalent set
			
			if (knownEquivalentSetIds.containsKey(mapTarget)) {
				equivalentConcepts.put(knownEquivalentSetIds.get(mapTarget), referencedComponent);
			} else {
				knownEquivalentSetIds.put(mapTarget, referencedComponent);
			}
			
		});
		
		return equivalentConcepts;
		
	}

	@Override
	public IInternalSctIdSet getUnsatisfiableConcepts() {
		return IInternalSctIdSet.EMPTY;
	}

	@Override
	public IInternalSctIdMultimap getEquivalentConcepts() {
		Builder builder = InternalSctIdMultimap.builder(InternalIdMap.builder()
				.addAll(equivalentConceptIds.keySet())
				.addAll(equivalentConceptIds.values())
				.build());
		equivalentConceptIds.asMap().forEach( (id,values) -> builder.putAll(id, values));
		return builder.build();
	}

}
