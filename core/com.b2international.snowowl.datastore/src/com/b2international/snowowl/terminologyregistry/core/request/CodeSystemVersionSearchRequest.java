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
package com.b2international.snowowl.terminologyregistry.core.request;

import java.io.IOException;
import java.util.Collections;

import com.b2international.commons.StringUtils;
import com.b2international.index.Hits;
import com.b2international.index.Searcher;
import com.b2international.index.query.Expressions;
import com.b2international.index.query.Expressions.ExpressionBuilder;
import com.b2international.snowowl.core.domain.RepositoryContext;
import com.b2international.snowowl.datastore.CodeSystemVersionEntry;
import com.b2international.snowowl.datastore.CodeSystemVersions;
import com.b2international.snowowl.datastore.request.SearchResourceRequest;

/**
 * @since 4.7
 */
final class CodeSystemVersionSearchRequest extends SearchResourceRequest<RepositoryContext, CodeSystemVersions> {

	private static final long serialVersionUID = 1L;

	private String codeSystemShortName;
	private String versionId;
	
	CodeSystemVersionSearchRequest() {
	}

	void setCodeSystemShortName(String codeSystemShortName) {
		this.codeSystemShortName = codeSystemShortName;
	}
	
	void setVersionId(String versionId) {
		this.versionId = versionId;
	}

	@Override
	protected CodeSystemVersions doExecute(final RepositoryContext context) throws IOException {
		final ExpressionBuilder query = Expressions.builder();

		if (!StringUtils.isEmpty(codeSystemShortName)) {
			query.filter(CodeSystemVersionEntry.Expressions.shortName(codeSystemShortName));
		}
		
		if (!StringUtils.isEmpty(versionId)) {
			query.filter(CodeSystemVersionEntry.Expressions.versionId(versionId));
		}
		
		final Searcher searcher = context.service(Searcher.class);
		
		final Hits<CodeSystemVersionEntry> hits = searcher.search(select(CodeSystemVersionEntry.class)
				.where(query.build())
				.sortBy(sortBy())
				.offset(offset())
				.limit(limit())
				.build());
		
		return new CodeSystemVersions(hits.getHits(), offset(), limit(), hits.getTotal());
	}
	
	@Override
	protected CodeSystemVersions createEmptyResult(int offset, int limit) {
		return new CodeSystemVersions(Collections.emptyList(), offset, limit, 0);
	}

}
