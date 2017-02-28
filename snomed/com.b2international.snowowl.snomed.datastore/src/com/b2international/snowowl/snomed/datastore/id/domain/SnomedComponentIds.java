/*
 * Copyright 2017 B2i Healthcare Pte Ltd, http://b2i.sg
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
package com.b2international.snowowl.snomed.datastore.id.domain;

import java.util.NoSuchElementException;
import java.util.Set;

import com.b2international.snowowl.core.domain.CollectionResource;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

/**
 * @since 5.5
 */
public final class SnomedComponentIds extends CollectionResource<String> {

	public SnomedComponentIds(final Set<String> componentIds) {
		super(ImmutableSet.copyOf(componentIds).asList());
	}
	
	/**
	 * @return the single item in this collection resource
	 * @throws NoSuchElementException if this resource is empty
	 * @throws IllegalArgumentException if this resource contains multiple elements
	 */
	public String getOnlyItem() {
		return Iterables.getOnlyElement(getItems());
	}

}
