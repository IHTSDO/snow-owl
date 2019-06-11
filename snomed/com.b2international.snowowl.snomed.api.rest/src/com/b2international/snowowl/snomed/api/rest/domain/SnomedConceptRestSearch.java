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

/**
 * @since 6.16
 */
public final class SnomedConceptRestSearch {

	// concept filters
	@ApiParam(value = "The Concept identifier(s) to match")
	private Set<String> conceptIds;
	@ApiParam(value = "The effective time to match (yyyyMMdd, exact matches only)")
	private String effectiveTimeFilter;

	@ApiParam(value = "The concept status to match")
	private Boolean activeFilter = null;
	@ApiParam(value = "The concept module identifier to match")
	private String moduleFilter;
	@ApiParam(value = "The definition status to match")
	private String definitionStatusFilter;
	@ApiParam(value = "The namespace to match")
	private String namespaceFilter;

	// query expressions
	@ApiParam(value = "The ECL expression to match on the inferred form")
	private String eclFilter;
	@ApiParam(value = "The ECL expression to match on the stated form")
	private String statedEclFilter;
	@ApiParam(value = "The SNOMED CT Query expression to match (inferred form only)")
	private String queryFilter;

	// description filters
	@ApiParam(value = "Description semantic tag(s) to match")
	private String[] semanticTags;
	@ApiParam(value = "The description term to match")
	private String termFilter;
	@ApiParam(value = "Description type ECL expression to match")
	private String descriptionTypeFilter;
	@ApiParam(value = "The description active state to match")
	private Boolean descriptionActiveFilter = null;

	// hierarchy filters
	@ApiParam(value = "The inferred parent(s) to match")
	private String[] parent;
	@ApiParam(value = "The inferred ancestor(s) to match")
	private String[] ancestor;
	@ApiParam(value = "The stated parent(s) to match")
	private String[] statedParent;
	@ApiParam(value = "The stated ancestor(s) to match")
	private String[] statedAncestor;

	// scrolling/paging/expansion/sorting
	@ApiParam(value = "Expansion parameters")
	private String expand;
	@ApiParam(value = "The scrollKeepAlive to start a scroll using this query")
	private String scrollKeepAlive;
	@ApiParam(value = "A scrollId to continue scrolling a previous query")
	private String scrollId;
	@ApiParam(value = "The search key to use for retrieving the next page of results")
	private String searchAfter;
	@ApiParam(value = "Sort keys")
	private List<String> sort;
	@ApiParam(value = "The maximum number of items to return", defaultValue = "50")
	private int limit = 50;

	public Set<String> getConceptIds() {
		return conceptIds;
	}

	public void setConceptIds(Set<String> conceptIds) {
		this.conceptIds = conceptIds;
	}

	public String getEffectiveTimeFilter() {
		return effectiveTimeFilter;
	}

	public void setEffectiveTimeFilter(String effectiveTimeFilter) {
		this.effectiveTimeFilter = effectiveTimeFilter;
	}

	public Boolean getActiveFilter() {
		return activeFilter;
	}

	public void setActiveFilter(Boolean activeFilter) {
		this.activeFilter = activeFilter;
	}

	public String getModuleFilter() {
		return moduleFilter;
	}

	public void setModuleFilter(String moduleFilter) {
		this.moduleFilter = moduleFilter;
	}

	public String getDefinitionStatusFilter() {
		return definitionStatusFilter;
	}

	public void setDefinitionStatusFilter(String definitionStatusFilter) {
		this.definitionStatusFilter = definitionStatusFilter;
	}

	public String getNamespaceFilter() {
		return namespaceFilter;
	}

	public void setNamespaceFilter(String namespaceFilter) {
		this.namespaceFilter = namespaceFilter;
	}

	public String getEclFilter() {
		return eclFilter;
	}

	public void setEclFilter(String eclFilter) {
		this.eclFilter = eclFilter;
	}

	public String getStatedEclFilter() {
		return statedEclFilter;
	}

	public void setStatedEclFilter(String statedEclFilter) {
		this.statedEclFilter = statedEclFilter;
	}

	public String getQueryFilter() {
		return queryFilter;
	}

	public void setQueryFilter(String queryFilter) {
		this.queryFilter = queryFilter;
	}

	public String[] getSemanticTags() {
		return semanticTags;
	}

	public void setSemanticTags(String[] semanticTags) {
		this.semanticTags = semanticTags;
	}

	public String getTermFilter() {
		return termFilter;
	}

	public void setTermFilter(String termFilter) {
		this.termFilter = termFilter;
	}

	public String getDescriptionTypeFilter() {
		return descriptionTypeFilter;
	}

	public void setDescriptionTypeFilter(String descriptionTypeFilter) {
		this.descriptionTypeFilter = descriptionTypeFilter;
	}

	public Boolean getDescriptionActiveFilter() {
		return descriptionActiveFilter;
	}

	public void setDescriptionActiveFilter(Boolean descriptionActiveFilter) {
		this.descriptionActiveFilter = descriptionActiveFilter;
	}

	public String[] getParent() {
		return parent;
	}

	public void setParent(String[] parent) {
		this.parent = parent;
	}

	public String[] getAncestor() {
		return ancestor;
	}

	public void setAncestor(String[] ancestor) {
		this.ancestor = ancestor;
	}

	public String[] getStatedParent() {
		return statedParent;
	}

	public void setStatedParent(String[] statedParent) {
		this.statedParent = statedParent;
	}

	public String[] getStatedAncestor() {
		return statedAncestor;
	}

	public void setStatedAncestor(String[] statedAncestor) {
		this.statedAncestor = statedAncestor;
	}

	public String getExpand() {
		return expand;
	}

	public void setExpand(String expand) {
		this.expand = expand;
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

	public List<String> getSort() {
		return sort;
	}

	public void setSort(List<String> sort) {
		this.sort = sort;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

}