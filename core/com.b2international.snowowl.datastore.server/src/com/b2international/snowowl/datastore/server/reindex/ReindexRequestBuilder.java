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
package com.b2international.snowowl.datastore.server.reindex;

import com.b2international.snowowl.core.RepositoryInfo;
import com.b2international.snowowl.core.domain.RepositoryContext;
import com.b2international.snowowl.core.events.BaseRequestBuilder;
import com.b2international.snowowl.core.events.Request;
import com.b2international.snowowl.datastore.request.RepositoryRequestBuilder;

/**
 * @since 5.0
 */
public final class ReindexRequestBuilder extends BaseRequestBuilder<ReindexRequestBuilder, RepositoryContext, ReindexResult> implements RepositoryRequestBuilder<ReindexResult> {
	
	private long failedCommitTimestamp = -1;

	ReindexRequestBuilder() {}
	
	public ReindexRequestBuilder setFailedCommitTimestamp(long failedCommitTimestamp) {
		this.failedCommitTimestamp = failedCommitTimestamp;
		return this;
	}
	
	@Override
	protected Request<RepositoryContext, ReindexResult> doBuild() {
		final ReindexRequest req = new ReindexRequest();
		req.setFailedCommitTimestamp(failedCommitTimestamp);
		return req;
	}

	@Override
	public RepositoryInfo.Health[] allowedHealthstates() {
		return new RepositoryInfo.Health[] { RepositoryInfo.Health.RED }; 
	}
}
