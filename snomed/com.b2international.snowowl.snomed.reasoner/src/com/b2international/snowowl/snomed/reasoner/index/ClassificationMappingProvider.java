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
package com.b2international.snowowl.snomed.reasoner.index;

import java.util.Collection;

import com.b2international.snowowl.datastore.index.MappingProvider;
import com.b2international.snowowl.snomed.common.SnomedTerminologyComponentConstants;
import com.google.common.collect.ImmutableSet;

/**
 * @since 6.12 (originally introduced in 7.x)
 */
public final class ClassificationMappingProvider implements MappingProvider {

	@Override
	public Collection<Class<?>> getMappings() {
		return ImmutableSet.<Class<?>>builder()
				.add(ClassificationTaskDocument.class)
				.add(EquivalentConceptSetDocument.class)
				.add(ConceptChangeDocument.class)
				.add(DescriptionChangeDocument.class)
				.add(RelationshipChangeDocument.class)
				.add(ConcreteDomainChangeDocument.class)
				.build();
	}

	@Override
	public String getToolingId() {
		return SnomedTerminologyComponentConstants.TERMINOLOGY_ID;
	}
}
