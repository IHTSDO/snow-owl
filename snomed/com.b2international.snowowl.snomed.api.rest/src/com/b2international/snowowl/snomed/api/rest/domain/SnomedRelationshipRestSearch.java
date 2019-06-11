/*
 * Copyright 2019 B2i Healthcare Pte Ltd, http://b2i.sg
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
import java.util.Set;

import io.swagger.annotations.ApiParam;

public final class SnomedRelationshipRestSearch {

	@ApiParam(value = "The Relationship ID(s) to match")
	private Set<String> id;

	@ApiParam(value = "The effective time to match (yyyyMMdd, exact matches only)")
	private String effectiveTime;

	@ApiParam(value = "The status to match")
	private Boolean active;

	@ApiParam(value = "The module identifier to match")
	private String module;

	@ApiParam(value = "The namespace to match")
	private String namespace;

	@ApiParam(value = "The source concept to match")
	private String source;

	@ApiParam(value = "The type concept to match")
	private String type;

	@ApiParam(value = "The destination concept to match")
	private String destination;

	@ApiParam(value = "The characteristic type to match")
	private String characteristicType;

	@ApiParam(value = "The group to match")
	private Integer group;

	@ApiParam(value = "The union group to match")
	private Integer unionGroup;

	@ApiParam(value = "The scrollKeepAlive to start a scroll using this query")
	private String scrollKeepAlive;

	@ApiParam(value = "A scrollId to continue scrolling a previous query")
	private String scrollId;

	@ApiParam(value = "The search key to use for retrieving the next page of results")
	private String searchAfter;

	@ApiParam(value = "The maximum number of items to return", defaultValue = "50")
	private int limit = 50;

	@ApiParam(value = "Expansion parameters")
	private String expand;

	@ApiParam(value = "Sort keys")
	private List<String> sort;

	public Set<String> getId() {
		return id;
	}

	public void setId(Set<String> id) {
		this.id = id;
	}

	public String getEffectiveTime() {
		return effectiveTime;
	}

	public void setEffectiveTime(String effectiveTime) {
		this.effectiveTime = effectiveTime;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public String getCharacteristicType() {
		return characteristicType;
	}

	public void setCharacteristicType(String characteristicType) {
		this.characteristicType = characteristicType;
	}

	public Integer getGroup() {
		return group;
	}

	public void setGroup(Integer group) {
		this.group = group;
	}

	public Integer getUnionGroup() {
		return unionGroup;
	}

	public void setUnionGroup(Integer unionGroup) {
		this.unionGroup = unionGroup;
	}

	public String getScrollKeepAlive() {
		return scrollKeepAlive;
	}

	public void setScrollKeepAlive(String scrollKeepAlive) {
		this.scrollKeepAlive = scrollKeepAlive;
	}

	public String getScrollId() {
		return scrollId;
	}

	public void setScrollId(String scrollId) {
		this.scrollId = scrollId;
	}

	public String getSearchAfter() {
		return searchAfter;
	}

	public void setSearchAfter(String searchAfter) {
		this.searchAfter = searchAfter;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public String getExpand() {
		return expand;
	}

	public void setExpand(String expand) {
		this.expand = expand;
	}

	public List<String> getSort() {
		return sort;
	}

	public void setSort(List<String> sort) {
		this.sort = sort;
	}

}
