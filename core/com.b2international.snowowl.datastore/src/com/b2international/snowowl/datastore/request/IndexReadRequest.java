/*
 * Copyright 2011-2016 B2i Healthcare Pte Ltd, http://b2i.sg
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

import java.io.IOException;

import com.b2international.index.Index;
import com.b2international.index.IndexRead;
import com.b2international.index.Searcher;
import com.b2international.index.query.QueryParseException;
import com.b2international.snowowl.core.domain.DelegatingBranchContext;
import com.b2international.snowowl.core.domain.RepositoryContext;
import com.b2international.snowowl.core.events.DelegatingRequest;
import com.b2international.snowowl.core.events.Request;
import com.b2international.snowowl.core.exceptions.IllegalQueryParameterException;

/**
 * @since 5.2
 */
public final class IndexReadRequest<B> extends DelegatingRequest<RepositoryContext, RepositoryContext, B> {

	IndexReadRequest(final Request<RepositoryContext, B> next) {
		super(next);
	}

	@Override
	public B execute(final RepositoryContext context) {
		return context.service(Index.class).read(new IndexRead<B>() {
			@Override
			public B execute(Searcher index) throws IOException {
				try {
					return next(DelegatingBranchContext
							.basedOn(context)
							.bind(Searcher.class, index)
							.build());
				} catch (QueryParseException e) {
					throw new IllegalQueryParameterException(e.getMessage());
				}
			}
		});
	}

}
