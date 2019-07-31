/*
 * Copyright 2011-2019 B2i Healthcare Pte Ltd, http://b2i.sg
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
package com.b2international.snowowl.snomed.api.rest;

import static com.b2international.snowowl.snomed.api.domain.browser.SnomedBrowserBulkChangeStatus.COMPLETED;
import static com.b2international.snowowl.snomed.api.domain.browser.SnomedBrowserBulkChangeStatus.FAILED;
import static com.b2international.snowowl.test.commons.rest.RestExtensions.givenAuthenticatedRequest;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.b2international.commons.CompareUtils;
import com.b2international.snowowl.core.api.IBranchPath;
import com.b2international.snowowl.snomed.SnomedConstants.Concepts;
import com.b2international.snowowl.snomed.api.domain.browser.SnomedBrowserDescriptionType;
import com.b2international.snowowl.snomed.core.domain.CaseSignificance;
import com.b2international.snowowl.snomed.core.domain.CharacteristicType;
import com.b2international.snowowl.snomed.core.domain.DefinitionStatus;
import com.b2international.snowowl.snomed.core.domain.RelationshipModifier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import io.restassured.response.ValidatableResponse;

/**
 * @since 4.5
 */
public abstract class SnomedBrowserRestRequests {
	
	private static ImmutableSet<String> FINISH_STATES = ImmutableSet.of(COMPLETED.name(), FAILED.name());

	public static final String JSON_CONTENT_UTF8_CHARSET = "application/json; charset=UTF-8";
	
	public static ValidatableResponse createBrowserConcept(final IBranchPath conceptPath, final Map<?, ?> requestBody) {
		return givenAuthenticatedRequest(SnomedApiTestConstants.SCT_API)
				.with().contentType(JSON_CONTENT_UTF8_CHARSET)
				.and().body(requestBody)
				.when().post("/browser/{path}/concepts", conceptPath.getPath())
				.then();
	}

	public static ValidatableResponse updateBrowserConcept(final IBranchPath branchPath, final String conceptId, final Map<?, ?> requestBody) {
		return givenAuthenticatedRequest(SnomedApiTestConstants.SCT_API)
				.with().contentType(JSON_CONTENT_UTF8_CHARSET)
				.and().body(requestBody)
				.when().put("/browser/{path}/concepts/{conceptId}", branchPath.getPath(), conceptId)
				.then();
	}

	public static ValidatableResponse getBrowserConcept(final IBranchPath branchPath, final String conceptId) {
		return givenAuthenticatedRequest(SnomedApiTestConstants.SCT_API)
				.with().contentType(JSON_CONTENT_UTF8_CHARSET)
				.when().get("/browser/{path}/concepts/{conceptId}", branchPath.getPath(), conceptId)
				.then();
	}

	public static ValidatableResponse getBrowserConceptChildren(final IBranchPath branchPath, final String conceptId, final String form) {
		return givenAuthenticatedRequest(SnomedApiTestConstants.SCT_API)
				.with().header("Accept-Language", "en-US;q=0.8,en-GB;q=0.6")
				.with().contentType(JSON_CONTENT_UTF8_CHARSET)
				.when().get("/browser/{path}/concepts/{conceptId}/children?form={form}", branchPath.getPath(), conceptId, form)
				.then();
	}
	
	public static ValidatableResponse getBrowserConceptParents(final IBranchPath branchPath, final String conceptId, final String form) {
		return givenAuthenticatedRequest(SnomedApiTestConstants.SCT_API)
				.with().header("Accept-Language", "en-US;q=0.8,en-GB;q=0.6")
				.with().contentType(JSON_CONTENT_UTF8_CHARSET)
				.when().get("/browser/{path}/concepts/{conceptId}/parents?form={form}", branchPath.getPath(), conceptId, form)
				.then();
	}
	
	public static ValidatableResponse searchDescriptionsReturnPT(final IBranchPath branchPath, final String query) {
		return givenAuthenticatedRequest(SnomedApiTestConstants.SCT_API)
				.with().header("Accept-Language", "en-US;q=0.8,en-GB;q=0.6")
				.with().contentType(JSON_CONTENT_UTF8_CHARSET)
				.when().get("/browser/{path}/descriptions?query={query}&preferredDescriptionType=SYNONYM", branchPath.getPath(), query)
				.then();
	}
	
	public static ValidatableResponse searchDescriptionsReturnFSN(final IBranchPath branchPath, final String query) {
		return givenAuthenticatedRequest(SnomedApiTestConstants.SCT_API)
				.with().header("Accept-Language", "en-US;q=0.8,en-GB;q=0.6")
				.with().contentType(JSON_CONTENT_UTF8_CHARSET)
				.with().get("/browser/{path}/descriptions?query={query}", branchPath.getPath(), query)
				.then();
	}
	
	public static ValidatableResponse bulkUpdateBrowserConcepts(final IBranchPath branchPath, final List<Map<String, Object>> requestBodies) {
		return givenAuthenticatedRequest(SnomedApiTestConstants.SCT_API)
				.with().contentType(JSON_CONTENT_UTF8_CHARSET)
				.and().body(requestBodies)
				.when().post("/browser/{path}/concepts/bulk", branchPath.getPath())
				.then();
	}
	
	public static ValidatableResponse bulkGetBrowserConceptChanges(final IBranchPath branchPath, final String bulkChangeId, String...expand) {
		return givenAuthenticatedRequest(SnomedApiTestConstants.SCT_API)
				.with().contentType(JSON_CONTENT_UTF8_CHARSET)
				.when()
				.queryParam("expand", CompareUtils.isEmpty(expand) ? Collections.emptyList() : Arrays.asList(expand))
				.get("/browser/{path}/concepts/bulk/{bulkChangeId}", branchPath.getPath(), bulkChangeId)
				.then();
	}
	
	public static ValidatableResponse waitForGetBrowserConceptChanges(final IBranchPath branchPath, final String bulkChangeId, String...expand) {
		return waitForJob(branchPath, bulkChangeId, FINISH_STATES, expand);
	}
	
	public static Map<String, Object> createBrowserConceptRequest() {
		ImmutableMap.Builder<String, Object> conceptBuilder = ImmutableMap.<String, Object>builder()
				.put("fsn", "FSN of new concept")
				.put("preferredSynonym", "PT of new concept")
				.put("moduleId", Concepts.MODULE_SCT_CORE)
				.put("definitionStatus", DefinitionStatus.PRIMITIVE)
				.put("descriptions", createDefaultDescriptions())
				.put("relationships", createIsaRelationship());

		return conceptBuilder.build();
	}

	public static List<?> createDefaultDescriptions(String pt, String fsn) {
		Map<?, ?> fsnDescription = ImmutableMap.<String, Object>builder()
				.put("active", true)
				.put("term", fsn)
				.put("type", SnomedBrowserDescriptionType.FSN)
				.put("lang", "en")
				.put("moduleId", Concepts.MODULE_SCT_CORE)
				.put("caseSignificance", CaseSignificance.CASE_INSENSITIVE)
				.put("acceptabilityMap", SnomedApiTestConstants.UK_PREFERRED_MAP)
				.build();
		
		Map<?, ?> ptDescription = ImmutableMap.<String, Object>builder()
				.put("active", true)
				.put("term", pt)
				.put("type", SnomedBrowserDescriptionType.SYNONYM)
				.put("lang", "en")
				.put("moduleId", Concepts.MODULE_SCT_CORE)
				.put("caseSignificance", CaseSignificance.CASE_INSENSITIVE)
				.put("acceptabilityMap", SnomedApiTestConstants.UK_PREFERRED_MAP)
				.build();
		
		return ImmutableList.of(fsnDescription, ptDescription);
	}
	
	public static List<?> createDefaultDescriptions() {
		return createDefaultDescriptions("PT of new concept", "FSN of new concept");
	}

	public static List<?> createIsaRelationship() {
		return createIsaRelationship(Concepts.ROOT_CONCEPT, CharacteristicType.STATED_RELATIONSHIP);
	}

	public static List<?> createIsaRelationship(String parentId, CharacteristicType characteristicType) {
		return createIsaRelationship(parentId, characteristicType, true);
	}
	
	public static List<?> createIsaRelationship(String parentId, CharacteristicType characteristicType, boolean relationshipActive) {
		Map<?, ?> type = ImmutableMap.<String, Object>builder()
				.put("conceptId", Concepts.IS_A)
				.put("fsn", "Is a (attribute)")
				.build();

		Map<?, ?> target = ImmutableMap.<String, Object>builder()
				.put("active", true)
				.put("moduleId", Concepts.MODULE_SCT_CORE)
				.put("conceptId", parentId)
				.put("fsn", "Parent of new concept")
				.put("definitionStatus", DefinitionStatus.PRIMITIVE)
				.build();

		Map<?, ?> isaRelationship = ImmutableMap.<String, Object>builder()
				.put("modifier", RelationshipModifier.EXISTENTIAL)
				.put("groupId", "0")
				.put("characteristicType", characteristicType)
				.put("active", relationshipActive)
				.put("type", type)
				.put("moduleId", Concepts.MODULE_SCT_CORE)
				.put("target", target)
				.build();

		return ImmutableList.of(isaRelationship);
	}
	
	private static ValidatableResponse waitForJob(IBranchPath branchPath, String bulkChangeId, Set<String> exitStates, String...expand) {
		long endTime = System.currentTimeMillis() + SnomedApiTestConstants.POLL_TIMEOUT;
		long currentTime;
		ValidatableResponse response = null;
		String jobStatus = null;

		do {

			try {
				Thread.sleep(SnomedApiTestConstants.POLL_INTERVAL);
			} catch (InterruptedException e) {
				fail(e.toString());
			}

			response = bulkGetBrowserConceptChanges(branchPath, bulkChangeId, expand).statusCode(200);
			jobStatus = response.extract().path("status");
			currentTime = System.currentTimeMillis();

		} while (!exitStates.contains(jobStatus) && currentTime < endTime);

		assertNotNull(response);
		return response;
	}

	private SnomedBrowserRestRequests() {
		throw new UnsupportedOperationException("This class is not supposed to be instantiated.");
	}

}
