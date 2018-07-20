/*
 * Copyright 2011-2018 B2i Healthcare Pte Ltd, http://b2i.sg
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
package com.b2international.snowowl.snomed.api.browser;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.b2international.commons.http.ExtendedLocale;
import com.b2international.commons.options.Options;
import com.b2international.snowowl.core.exceptions.ComponentNotFoundException;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserBulkChangeRun;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserChildConcept;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserConcept;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserConstant;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserDescriptionResult;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserParentConcept;
import com.b2international.snowowl.snomed.api.domain.browser.SnomedBrowserDescriptionType;

/**
 * The interface for the IHTSDO SNOMED CT Browser service.
 */
public interface ISnomedBrowserService {

	/**
	 * Retrieves information strongly connected to a concept in a single request.
	 * 
	 * @param branchPath - the branch to use
	 * @param conceptId - the concept id to use
	 * @param extendedLocales - the {@link ExtendedLocale}s to inspect when determining FSN and preferred synonym, in decreasing order of preference
	 * @return the aggregated content for the requested concept
	 * @throws ComponentNotFoundException if the component identifier does not match any concept on the given task
	 */
	ISnomedBrowserConcept getConceptDetails(final String branchPath, final String conceptId, List<ExtendedLocale> extendedLocales);

	/**
	 * Retrieves information strongly connected to a concept in bulk.
	 * 
	 * @param branchPath - the branch to use
	 * @param conceptIds - the concept ids to use
	 * @param extendedLocales - the {@link ExtendedLocale}s to inspect when determining FSN and preferred synonym, in decreasing order of preference
	 * @return the aggregated content for the requested concept
	 * @throws ComponentNotFoundException if the component identifier does not match any concept on the given task
	 */
	Set<ISnomedBrowserConcept> getConceptDetailsInBulk(final String branchPath, final Set<String> conceptIds, List<ExtendedLocale> extendedLocales);

	/**
	 * Retrieves a list of parent concepts for a single identifier.
	 * 
	 * @param branchPath - the branch to use
	 * @param conceptId - the concept id to use
	 * @param extendedLocales - the {@link ExtendedLocale}s to inspect when determining FSN, in decreasing order of preference
	 * @param isStatedForm - {@code true} if stated children should be returned, {@code false} if inferred
	 * @param preferredDescriptionType
	 * @return the parent concept list for the requested concept
	 * @throws ComponentNotFoundException if the component identifier does not match any concept on the given task
	 */
	List<ISnomedBrowserParentConcept> getConceptParents(String branchPath, String conceptId, List<ExtendedLocale> extendedLocales, boolean isStatedForm, SnomedBrowserDescriptionType preferredDescriptionType);
	
	/**
	 * Retrieves a list of child concepts for a single identifier.
	 * 
	 * @param branchPath - the branch to use
	 * @param conceptId - the parent concept id to use to fetch his children
	 * @param extendedLocales the {@link ExtendedLocale}s to inspect when determining FSN, in decreasing order of preference
	 * @param isStatedForm {@code true} if stated children should be returned, {@code false} if inferred
	 * @param preferredDescriptionType 
	 * @param scrollKeepAlive
	 * @param scrollId
	 * @param limit - the maximal number of results to return
	 * @return the child concept list for the requested concept
	 * @throws ComponentNotFoundException if the component identifier does not match any concept on the given task
	 */
	List<ISnomedBrowserChildConcept> getConceptChildren(String branchPath, String conceptId, List<ExtendedLocale> extendedLocales, boolean isStatedForm, SnomedBrowserDescriptionType preferredDescriptionType, int limit);
	
	/**
	 * Retrieves a list of descriptions matching the entered query string.
	 * 
	 * @param branchPath - the branch to use
	 * @param query - the query text (must be at least 3 characters long)
	 * @param extendedLocales - the {@link ExtendedLocale}s to inspect when determining FSN, in decreasing order of preference
	 * @param preferredDescriptionType
	 * @param scrollKeepAlive
	 * @param scrollId
	 * @param limit - the maximal number of results to return
	 * @return the search result list of descriptions
	 * @throws IllegalArgumentException if the query is {@code null} or too short
	 */
	List<ISnomedBrowserDescriptionResult> getDescriptions(String branchPath, String query, List<ExtendedLocale> extendedLocales, SnomedBrowserDescriptionType preferredDescriptionType, String scrollKeepAlive, String scrollId, int limit);

	/**
	 * Retrieves a map of enum constants and corresponding concepts.
	 *
	 * @param branchPath - the branch to use
	 * @param extendedLocales - the {@link ExtendedLocale}s to inspect when determining FSN, in decreasing order of preference
	 * @return a map with keys as constant identifiers, and values as corresponding concept ID-FSN pairs
	 */
	Map<String, ISnomedBrowserConstant> getConstants(String branchPath, List<ExtendedLocale> extendedLocales);

	ISnomedBrowserConcept createConcept(String branchPath, ISnomedBrowserConcept concept, String userId, List<ExtendedLocale> extendedLocales);

	void updateConcept(String branchPath, List<? extends ISnomedBrowserConcept> concept, String userId, List<ExtendedLocale> extendedLocales);
	
	ISnomedBrowserBulkChangeRun beginBulkChange(String branchPath, List<? extends ISnomedBrowserConcept> newVersionConcepts, Boolean allowCreate, String userId, List<ExtendedLocale> extendedLocales);

	ISnomedBrowserBulkChangeRun getBulkChange(String branchPath, String bulkChangeId, List<ExtendedLocale> extendedLocales, Options expand);

}
