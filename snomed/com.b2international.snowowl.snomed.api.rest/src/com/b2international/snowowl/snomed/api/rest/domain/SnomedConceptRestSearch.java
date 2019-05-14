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
package com.b2international.snowowl.snomed.api.rest.domain;

import java.util.Set;

public class SnomedConceptRestSearch {

	private Boolean activeFilter = null;
	private String moduleFilter;
	private String namespaceFilter;
	private String effectiveTimeFilter;
	private String definitionStatusFilter;
	private String termFilter;
	private Boolean descriptionActiveFilter = null;
	private String eclFilter;
	private String statedEclFilter;
	private String queryFilter;
	private String[] semanticTags;
	private String descriptionTypeFilter;
	private Set<String> conceptIds;
	private String expand;
	private String scrollKeepAlive;
	private String scrollId;
	private String searchAfter;
	private int limit = 50;
	
	public String getTermFilter() {
		return termFilter;
	}

	public void setTermFilter(String termFilter) {
		this.termFilter = termFilter;
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
	
	public Set<String> getConceptIds() {
		return conceptIds;
	}

	public void setConceptIds(Set<String> conceptIds) {
		this.conceptIds = conceptIds;
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

	public Boolean getActiveFilter() {
		return activeFilter;
	}

	public void setActiveFilter(Boolean activeFilter) {
		this.activeFilter = activeFilter;
	}
	
	public Boolean getDescriptionActiveFilter() {
		return descriptionActiveFilter;
	}
	
	public void setDescriptionActiveFilter(Boolean descriptionActiveFilter) {
		this.descriptionActiveFilter = descriptionActiveFilter;
	}

	public String getEffectiveTimeFilter() {
		return effectiveTimeFilter;
	}
	
	public void setEffectiveTimeFilter(String effectiveTimeFilter) {
		this.effectiveTimeFilter = effectiveTimeFilter;
	}
	
	public String getNamespaceFilter() {
		return namespaceFilter;
	}
	
	public void setNamespaceFilter(String namespaceFilter) {
		this.namespaceFilter = namespaceFilter;
	}

	public String[] getSemanticTags() {
		return semanticTags;
	}

	public void setSemanticTags(String[] semanticTags) {
		this.semanticTags = semanticTags;
	}

	public String getDescriptionTypeFilter() {
		return descriptionTypeFilter;
	}

	public void setDescriptionTypeFilter(String descriptionTypeFilter) {
		this.descriptionTypeFilter = descriptionTypeFilter;
	}

	public String getExpand() {
		return expand;
	}

	public String getScrollKeepAlive() {
		return scrollKeepAlive;
	}

	public String getScrollId() {
		return scrollId;
	}

	public String getSearchAfter() {
		return searchAfter;
	}

	public int getLimit() {
		return limit;
	}

	public void setExpand(String expand) {
		this.expand = expand;
	}

	public void setScrollKeepAlive(String scrollKeepAlive) {
		this.scrollKeepAlive = scrollKeepAlive;
	}

	public void setScrollId(String scrollId) {
		this.scrollId = scrollId;
	}

	public void setSearchAfter(String searchAfter) {
		this.searchAfter = searchAfter;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}
	
}
