/*
 * Copyright 2011-2017 B2i Healthcare Pte Ltd, http://b2i.sg
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
package com.b2international.snowowl.snomed.core.domain;

import java.util.Collections;
import java.util.List;

import com.b2international.snowowl.core.domain.PageableCollectionResource;
import com.b2international.snowowl.snomed.datastore.index.entry.SnomedConceptDocument;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Function;

/**
 * @since 4.5
 */
public final class SnomedConcepts extends PageableCollectionResource<SnomedConcept> {

	public static final Function<SnomedConcepts, List<SnomedConceptDocument>> TO_DOCS = new Function<SnomedConcepts, List<SnomedConceptDocument>>() {
		@Override
		public List<SnomedConceptDocument> apply(SnomedConcepts input) {
			return SnomedConceptDocument.fromConcepts(input);
		}
	};

	public SnomedConcepts(int offset, int limit, int total) {
		super(Collections.emptyList(), offset, limit, total);
	}
	
	@JsonCreator
	public SnomedConcepts(
			@JsonProperty("items") List<SnomedConcept> items, 
			@JsonProperty("offset") int offset, 
			@JsonProperty("limit") int limit, 
			@JsonProperty("total") int total) {
		super(items, offset, limit, total);
	}

}
