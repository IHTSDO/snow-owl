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
package com.b2international.snowowl.snomed.datastore.server.events;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;

import com.b2international.snowowl.core.api.IBranchPath;
import com.b2international.snowowl.core.domain.RepositoryContext;
import com.b2international.snowowl.snomed.core.domain.SnomedConcepts;
import com.b2international.snowowl.snomed.datastore.SnomedConceptIndexEntry;
import com.b2international.snowowl.snomed.datastore.index.SnomedConceptReducedQueryAdapter;
import com.b2international.snowowl.snomed.datastore.index.SnomedIndexService;
import com.b2international.snowowl.snomed.datastore.services.SnomedBranchRefSetMembershipLookupService;
import com.google.common.collect.Lists;

/**
 * @since 4.5
 */
class SnomedConceptReadAllRequest extends SnomedConceptRequest<RepositoryContext, SnomedConcepts> {

	private int offset;
	private int limit;

	public SnomedConceptReadAllRequest(int offset, int limit) {
		checkArgument(offset >= 0, "Offset should be greater than or equal to zero");
		checkArgument(limit > 0, "Limit should be greater than zero");
		this.offset = offset;
		this.limit = limit;
	}

	@Override
	public SnomedConcepts execute(RepositoryContext context) {
		final IBranchPath branchPath = context.branch().branchPath();
		final SnomedIndexService index = context.service(SnomedIndexService.class);
		
		final SnomedConceptReducedQueryAdapter queryAdapter = new SnomedConceptReducedQueryAdapter();
		final int total = index.getHitCount(branchPath, queryAdapter);
		final List<SnomedConceptIndexEntry> hits = index.search(branchPath, queryAdapter, offset, limit); 
		
		return new SnomedConcepts(Lists.transform(hits, new SnomedConceptConverter(new SnomedBranchRefSetMembershipLookupService(branchPath))), offset, limit, total);
	}
	
	@Override
	protected Class<SnomedConcepts> getReturnType() {
		return SnomedConcepts.class;
	}
	
}
