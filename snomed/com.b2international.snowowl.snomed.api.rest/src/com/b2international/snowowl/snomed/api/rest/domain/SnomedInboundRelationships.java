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
package com.b2international.snowowl.snomed.api.rest.domain;

import java.util.List;

import com.b2international.snowowl.core.domain.CustomPageableCollectionResource;
import com.b2international.snowowl.snomed.core.domain.SnomedRelationships;
import com.google.common.base.MoreObjects.ToStringHelper;

/**
 * @since 1.0
 */
public class SnomedInboundRelationships extends CustomPageableCollectionResource {

	protected SnomedInboundRelationships(String searchAfter, int limit, int total) {
		super(searchAfter, limit, total);
	}

	private List<ExpandableSnomedRelationship> inboundRelationships;

	public List<ExpandableSnomedRelationship> getInboundRelationships() {
		return inboundRelationships;
	}

	public void setInboundRelationships(final List<ExpandableSnomedRelationship> inboundRelationships) {
		this.inboundRelationships = inboundRelationships;
	}

	@Override
	protected void appendToString(ToStringHelper stringHelper) {
		stringHelper.add("inboundRelationships", getInboundRelationships());
	}
	
	public static SnomedInboundRelationships of(SnomedRelationships relationships) {
		return new SnomedInboundRelationships(relationships.getSearchAfter(), relationships.getLimit(), relationships.getTotal());
	}
	
}