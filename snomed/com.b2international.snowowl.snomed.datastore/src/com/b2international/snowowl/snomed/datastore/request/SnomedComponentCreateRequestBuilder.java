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
package com.b2international.snowowl.snomed.datastore.request;

import javax.annotation.OverridingMethodsMustInvokeSuper;

import com.b2international.snowowl.core.domain.TransactionContext;
import com.b2international.snowowl.core.events.BaseRequestBuilder;
import com.b2international.snowowl.core.events.Request;
import com.b2international.snowowl.snomed.core.domain.ConstantIdStrategy;
import com.b2international.snowowl.snomed.core.domain.IdGenerationStrategy;
import com.b2international.snowowl.snomed.core.domain.MetadataIdStrategy;
import com.b2international.snowowl.snomed.core.domain.NamespaceIdStrategy;

/**
 * @since 4.5
 */
public abstract class SnomedComponentCreateRequestBuilder<B extends SnomedComponentCreateRequestBuilder<B>> 
		extends BaseRequestBuilder<B, TransactionContext, String>
		implements SnomedTransactionalRequestBuilder<String> {
	
	private String moduleId;
	private Boolean active = Boolean.TRUE;
	private IdGenerationStrategy idGenerationStrategy;
	
	protected SnomedComponentCreateRequestBuilder() { 
		super();
	}
	
	public final B setId(String id) {
		this.idGenerationStrategy = new ConstantIdStrategy(id);
		return getSelf();
	}
	
	public final B setIdFromNamespace(final String namespace) {
		this.idGenerationStrategy = new NamespaceIdStrategy(namespace);
		return getSelf();
	}
	
	public final B setIdFromMetadata() {
		this.idGenerationStrategy = new MetadataIdStrategy();
		return getSelf();
	}
	
	public final B setIdGenerationStrategy(final IdGenerationStrategy idGenerationStrategy) {
		this.idGenerationStrategy = idGenerationStrategy;
		return getSelf();
	}
	
	public final B setActive(Boolean active) {
		this.active = active;
		return getSelf();
	}
	
	public final B setModuleId(String moduleId) {
		this.moduleId = moduleId;
		return getSelf();
	}
	
	IdGenerationStrategy getIdGenerationStrategy() {
		return idGenerationStrategy;
	}

	@Override
	protected final Request<TransactionContext, String> doBuild() {
		final BaseSnomedComponentCreateRequest req = createRequest();
		req.setIdGenerationStrategy(idGenerationStrategy);
		req.setModuleId(moduleId);
		req.setActive(active == null ? Boolean.TRUE : active);
		init(req);
		return req;
	}

	@OverridingMethodsMustInvokeSuper
	protected abstract void init(BaseSnomedComponentCreateRequest req);

	protected abstract BaseSnomedComponentCreateRequest createRequest();
	
}
