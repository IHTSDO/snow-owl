/*
 * Copyright 2018 B2i Healthcare Pte Ltd, http://b2i.sg
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
package com.b2international.snowowl.core.domain;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;

/**
 *
 * @since 6.8.1
 */
public class CustomPageableCollectionResource {

	private final String searchAfter;
	private final int limit;
	private final int total;

	protected CustomPageableCollectionResource(String searchAfter, int limit, int total) {
		this.searchAfter = searchAfter;
		this.limit = limit;
		this.total = total;
	}

	public String getSearchAfter() {
		return searchAfter;
	}

	public int getLimit() {
		return limit;
	}

	public int getTotal() {
		return total;
	}
	
	@Override
	public String toString() {
		
		ToStringHelper stringHelper = MoreObjects.toStringHelper(this);
		
		appendToString(stringHelper);
		
		stringHelper
			.add("searchAfter", getSearchAfter())
			.add("limit", getLimit())
			.add("total", getTotal());
		
		return stringHelper.toString();
	}

	protected void appendToString(ToStringHelper stringHelper) {}
	
}
