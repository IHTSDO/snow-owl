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
package com.b2international.snowowl.snomed.datastore.id.request;

import java.util.Set;

import com.b2international.snowowl.datastore.request.BaseRepositoryRequestBuilder;
import com.google.common.collect.ImmutableSet;

/**
 * @since 5.5
 */
public abstract class AbstractSnomedIdentifierEnumeratedRequestBuilder<B extends AbstractSnomedIdentifierEnumeratedRequestBuilder<B, R>, R> extends BaseRepositoryRequestBuilder<B, R> {

	protected Set<String> componentIds;

	public B setComponentId(String componentId) {
		this.componentIds = ImmutableSet.of(componentId);
		return getSelf();
	}
	
	public B setComponentIds(Set<String> componentIds) {
		this.componentIds = ImmutableSet.copyOf(componentIds);
		return getSelf();
	}

}
