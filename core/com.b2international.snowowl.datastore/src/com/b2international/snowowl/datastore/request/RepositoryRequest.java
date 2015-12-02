/*
 * Copyright 2011-2015 B2i Healthcare Pte Ltd, http://b2i.sg
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
package com.b2international.snowowl.datastore.request;

import static com.google.common.base.Preconditions.checkNotNull;

import com.b2international.snowowl.core.ServiceProvider;
import com.b2international.snowowl.core.domain.RepositoryContext;
import com.b2international.snowowl.core.domain.RepositoryContextProvider;
import com.b2international.snowowl.core.events.DelegatingRequest;
import com.b2international.snowowl.core.events.Request;

/**
 * @since 4.5
 */
public final class RepositoryRequest<B> extends DelegatingRequest<ServiceProvider, RepositoryContext, B> {

	private final String repositoryId;

	RepositoryRequest(String repositoryId, Request<RepositoryContext, B> next) {
		super(next);
		this.repositoryId = checkNotNull(repositoryId, "repositoryId");
	}
	
	@Override
	public B execute(final ServiceProvider context) {
		return next(context.service(RepositoryContextProvider.class).get(context, repositoryId));
	}
	
	@Override
	protected String getAddress() {
		return "/"+repositoryId;
	}
	
	@Override
	public String toString() {
		return String.format("{repositoryId:%s, request:%s}", repositoryId, next());
	}
	
}
