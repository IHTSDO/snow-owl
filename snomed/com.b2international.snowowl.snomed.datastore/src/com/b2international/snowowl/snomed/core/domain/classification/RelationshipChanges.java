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
package com.b2international.snowowl.snomed.core.domain.classification;

import java.util.List;

import com.b2international.commons.StringUtils;
import com.b2international.snowowl.core.domain.CollectionResource;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

/**
 * @since 6.8.1
 */
public class RelationshipChanges extends CollectionResource<RelationshipChange> {

	private static final long serialVersionUID = -6654249578840695814L;
	
	private int offset;
	private int limit;
	private int total;
	
	protected RelationshipChanges(List<RelationshipChange> items, int offset, int limit, int total) {
		super(items);
		this.offset = offset;
		this.limit = limit;
		this.total = total;
	}
	
	public int getOffset() {
		return offset;
	}
	
	public int getLimit() {
		return limit;
	}
	
	public int getTotal() {
		return total;
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(RelationshipChanges.class)
					.add("items", StringUtils.limitedToString(getItems(), 10))
					.add("offset", getOffset())
					.add("limit", getLimit())
					.add("total", getTotal()).toString();
	}
	
	@JsonCreator
	public static RelationshipChanges of(
			@JsonProperty("items") List<RelationshipChange> items,
			@JsonProperty("offset") int offset,
			@JsonProperty("limit") int limit,
			@JsonProperty("total") int total) {
		return new RelationshipChanges(items, offset, limit, total);
	}
}
