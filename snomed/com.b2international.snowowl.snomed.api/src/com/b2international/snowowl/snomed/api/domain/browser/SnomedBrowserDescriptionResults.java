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
package com.b2international.snowowl.snomed.api.domain.browser;

import java.util.List;

import com.b2international.snowowl.core.domain.CustomPageableCollectionResource;
import com.b2international.snowowl.snomed.core.domain.SnomedDescriptions;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ImmutableList;

/**
 *
 * @since v6.8.1
 */
public class SnomedBrowserDescriptionResults extends CustomPageableCollectionResource {

	private List<ISnomedBrowserDescriptionResult> items;

	protected SnomedBrowserDescriptionResults(List<ISnomedBrowserDescriptionResult> items, String searchAfter, int limit, int total) {
		super(searchAfter, limit, total);
		this.items = ImmutableList.copyOf(items);
	}
	
	public List<ISnomedBrowserDescriptionResult> getItems() {
		return items;
	}
	
	public void setItems(List<ISnomedBrowserDescriptionResult> items) {
		this.items = items;
	}
	
	@Override
	protected void appendToString(ToStringHelper stringHelper) {
		stringHelper.add("items", getItems());
	}
	
	public static SnomedBrowserDescriptionResults of(List<ISnomedBrowserDescriptionResult> items, SnomedDescriptions snomedDescriptions) {
		return new SnomedBrowserDescriptionResults(items, snomedDescriptions.getSearchAfter(), snomedDescriptions.getLimit(), snomedDescriptions.getTotal());
	}

}
