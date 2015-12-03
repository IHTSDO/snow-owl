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
import com.b2international.snowowl.core.domain.TransactionContext;
import com.b2international.snowowl.core.events.BaseRequestBuilder;
import com.b2international.snowowl.core.events.Request;

/**
 * @since 4.5
 */
public abstract class BaseTransactionalRequestBuilder<B extends BaseTransactionalRequestBuilder<B, R>, R>
		extends BaseRequestBuilder<B, TransactionContext, R> {

	private final String repositoryId;

	protected BaseTransactionalRequestBuilder(String repositoryId) {
		this.repositoryId = checkNotNull(repositoryId, "repositoryId");
	}

	public final Request<ServiceProvider, CommitInfo> build(String userId, String branch, String commitComment) {
		return createCommitBuilder(repositoryId).setUserId(userId).setBranch(branch).setCommitComment(commitComment).setBody(this).build();
	}

	protected RepositoryCommitRequestBuilder createCommitBuilder(String repositoryId) {
		return new RepositoryCommitRequestBuilder(repositoryId);
	}

}
