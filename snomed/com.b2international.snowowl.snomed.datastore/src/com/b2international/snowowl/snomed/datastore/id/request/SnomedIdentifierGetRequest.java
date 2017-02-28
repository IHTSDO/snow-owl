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

import java.util.Map;
import java.util.Set;

import com.b2international.snowowl.core.domain.RepositoryContext;
import com.b2international.snowowl.core.events.BaseRequest;
import com.b2international.snowowl.snomed.datastore.id.ISnomedIdentifierService;
import com.b2international.snowowl.snomed.datastore.id.domain.SctId;
import com.b2international.snowowl.snomed.datastore.id.domain.SctIds;

/**
 * @since 5.5
 */
final class SnomedIdentifierGetRequest extends BaseRequest<RepositoryContext, SctIds> {

	private final Set<String> componentIds;

	SnomedIdentifierGetRequest(final Set<String> componentIds) {
		this.componentIds = componentIds;
	}

	@Override
	public SctIds execute(RepositoryContext context) {
		final Map<String, SctId> sctIds = context.service(ISnomedIdentifierService.class).getSctIds(componentIds);
		return new SctIds(sctIds.values());
	}

	@Override
	protected Class<SctIds> getReturnType() {
		return SctIds.class;
	}

}
