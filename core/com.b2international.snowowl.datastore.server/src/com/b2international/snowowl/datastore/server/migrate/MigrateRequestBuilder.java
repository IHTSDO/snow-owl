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
package com.b2international.snowowl.datastore.server.migrate;

import com.b2international.snowowl.core.Repository;
import com.b2international.snowowl.core.RepositoryInfo;
import com.b2international.snowowl.core.RepositoryInfo.Health;
import com.b2international.snowowl.core.domain.RepositoryContext;
import com.b2international.snowowl.core.events.BaseRequestBuilder;
import com.b2international.snowowl.core.events.Request;
import com.b2international.snowowl.datastore.request.RepositoryRequestBuilder;

/**
 * @since 5.11
 */
public class MigrateRequestBuilder extends BaseRequestBuilder<MigrateRequestBuilder, RepositoryContext, MigrationResult> implements RepositoryRequestBuilder<MigrationResult> {

	private final String remoteLocation;
	private String scriptLocation;
	private long commitTimestamp;
	
	MigrateRequestBuilder(String remoteLocation) {
		this.remoteLocation = remoteLocation;
	}
	
	public MigrateRequestBuilder setCommitTimestamp(long commitTimestamp) {
		this.commitTimestamp = commitTimestamp;
		return getSelf();
	}
	
	public MigrateRequestBuilder setScriptLocation(String scriptLocation) {
		this.scriptLocation = scriptLocation;
		return getSelf();
	}
	
	@Override
	protected Request<RepositoryContext, MigrationResult> doBuild() {
		MigrateRequest req = new MigrateRequest(remoteLocation);
		req.setCommitTimestamp(commitTimestamp);
		req.setScriptLocation(scriptLocation);
		return req;
	}
	
	@Override
	public Health[] allowedHealthstates() {
		return new Repository.Health[] { RepositoryInfo.Health.GREEN };
	}

}
