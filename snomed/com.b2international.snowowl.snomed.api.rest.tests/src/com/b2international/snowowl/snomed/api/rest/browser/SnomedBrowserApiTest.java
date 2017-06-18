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
package com.b2international.snowowl.snomed.api.rest.browser;

import static com.b2international.snowowl.snomed.SnomedConstants.Concepts.MODULE_SCT_CORE;
import static com.b2international.snowowl.snomed.SnomedConstants.Concepts.ROOT_CONCEPT;
import static com.b2international.snowowl.snomed.api.rest.SnomedBranchingRestRequests.deleteBranch;
import static com.b2international.snowowl.snomed.api.rest.SnomedBrowserRestRequests.createBrowserConcept;
import static com.b2international.snowowl.snomed.api.rest.SnomedBrowserRestRequests.updateBrowserConcept;
import static com.b2international.snowowl.snomed.api.rest.SnomedComponentRestRequests.deleteComponent;
import static com.b2international.snowowl.snomed.api.rest.SnomedRestFixtures.createNewConcept;
import static com.b2international.snowowl.snomed.api.rest.SnomedRestFixtures.reserveComponentId;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.b2international.snowowl.core.api.IBranchPath;
import com.b2international.snowowl.core.terminology.ComponentCategory;
import com.b2international.snowowl.datastore.BranchPathUtils;
import com.b2international.snowowl.snomed.SnomedConstants.Concepts;
import com.b2international.snowowl.snomed.api.domain.browser.SnomedBrowserDescriptionType;
import com.b2international.snowowl.snomed.api.rest.AbstractSnomedApiTest;
import com.b2international.snowowl.snomed.api.rest.CodeSystemVersionRestRequests;
import com.b2international.snowowl.snomed.api.rest.SnomedApiTestConstants;
import com.b2international.snowowl.snomed.api.rest.SnomedBrowserRestRequests;
import com.b2international.snowowl.snomed.api.rest.SnomedComponentRestRequests;
import com.b2international.snowowl.snomed.api.rest.SnomedComponentType;
import com.b2international.snowowl.snomed.api.rest.SnomedIdentifierRestRequests;
import com.b2international.snowowl.snomed.core.domain.AssociationType;
import com.b2international.snowowl.snomed.core.domain.CaseSignificance;
import com.b2international.snowowl.snomed.core.domain.CharacteristicType;
import com.b2international.snowowl.snomed.core.domain.DefinitionStatus;
import com.b2international.snowowl.snomed.core.domain.RelationshipModifier;
import com.b2international.snowowl.snomed.datastore.SnomedInactivationPlan.InactivationReason;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Maps;
import com.jayway.restassured.response.ValidatableResponse;

/**
 * @since 4.5
 */
public class SnomedBrowserApiTest extends AbstractSnomedApiTest {

	private static final String FINDING_CONTEXT = "408729009";
	
	private static Map<String, Object> createBrowserConceptRequest() {
		ImmutableMap.Builder<String, Object> conceptBuilder = ImmutableMap.<String, Object>builder()
				.put("fsn", "FSN of new concept")
				.put("preferredSynonym", "PT of new concept")
				.put("moduleId", Concepts.MODULE_SCT_CORE)
				.put("definitionStatus", DefinitionStatus.PRIMITIVE)
				.put("descriptions", createDefaultDescriptions())
				.put("relationships", createIsaRelationship());

		return conceptBuilder.build();
	}

	private static List<?> createDefaultDescriptions(String pt, String fsn) {
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
	
	private static List<?> createDefaultDescriptions() {
		return createDefaultDescriptions("PT of new concept", "FSN of new concept");
	}

	private static List<?> createIsaRelationship() {
		return createIsaRelationship(Concepts.ROOT_CONCEPT, CharacteristicType.STATED_RELATIONSHIP);
	}

	private static List<?> createIsaRelationship(String parentId, CharacteristicType characteristicType) {
		return createIsaRelationship(parentId, characteristicType, true);
	}
	private static List<?> createIsaRelationship(String parentId, CharacteristicType characteristicType, boolean relationshipActive) {
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

	@SuppressWarnings("unchecked")
	private static List<Object> getListElement(Map<String, Object> concept, String elementName) {
		Object listElement = concept.get(elementName);

		if (listElement instanceof List<?>) {
			return (List<Object>) listElement;
		} else {
			return null;
		}
	}

	private static List<?> createNewDescriptions(int quantity) {
		List<Map<String, Object>> results = newArrayList();

		for (int i = 0; i < quantity; i++) {

			Map<String, Object> description = ImmutableMap.<String, Object>builder()
					.put("active", true)
					.put("term", String.format("New extra Synonym %s", i))
					.put("type", SnomedBrowserDescriptionType.SYNONYM)
					.put("lang", "en")
					.put("moduleId", MODULE_SCT_CORE)
					.put("caseSignificance", CaseSignificance.CASE_INSENSITIVE)
					.put("acceptabilityMap", SnomedApiTestConstants.UK_ACCEPTABLE_MAP)
					.build();

			results.add(description);
		}

		return results;
	}

	private static List<?> createNewRelationships(int quantity, String sourceId) {
		List<Map<String, Object>> results = newArrayList();

		for (int i = 0; i < quantity; i++) {

			Map<?, ?> type = ImmutableMap.<String, Object>builder()
					.put("conceptId", Concepts.PART_OF)
					.put("fsn", "Part of (attribute)")
					.build();

			Map<?, ?> target = ImmutableMap.<String, Object>builder()
					.put("active", true)
					.put("moduleId", MODULE_SCT_CORE)
					.put("conceptId", Concepts.NAMESPACE_ROOT)
					.put("fsn", "Destination of new relationship")
					.put("definitionStatus", DefinitionStatus.PRIMITIVE)
					.build();

			Map<String, Object> relationship = ImmutableMap.<String, Object>builder()
					.put("sourceId", sourceId)
					.put("modifier", RelationshipModifier.EXISTENTIAL)
					.put("groupId", "0")
					.put("characteristicType", CharacteristicType.ADDITIONAL_RELATIONSHIP)
					.put("active", true)
					.put("type", type)
					.put("moduleId", MODULE_SCT_CORE)
					.put("target", target)
					.build();

			results.add(relationship);
		}

		return results;
	}

	@Test		
  	public void createConceptWithInferredParentOnly() {		
  		Map<String, Object> conceptRequest = Maps.newHashMap(createBrowserConceptRequest());
  		conceptRequest.put("relationships", createIsaRelationship(ROOT_CONCEPT, CharacteristicType.INFERRED_RELATIONSHIP));
  		
  		SnomedBrowserRestRequests.createBrowserConcept(branchPath, conceptRequest).statusCode(400)
  					.body("message", equalTo("At least one active stated IS A relationship is required."));
  	}		
  			
  	@Test		
  	public void createConceptWithInactiveStatedParent() {		
		Map<String, Object> conceptRequest = Maps.newHashMap(createBrowserConceptRequest());
  		conceptRequest.put("relationships", createIsaRelationship(ROOT_CONCEPT, CharacteristicType.STATED_RELATIONSHIP, false));
  		
  		SnomedBrowserRestRequests.createBrowserConcept(branchPath, conceptRequest).statusCode(400)
  				.body("message", equalTo("At least one active stated IS A relationship is required."));
  		
  	}
	
	@Test		
  	public void createConceptWithGeneratedId() {
  		
  		String componentId = SnomedIdentifierRestRequests.generateSctId(SnomedComponentType.CONCEPT, null).statusCode(201)
  					.body("id", notNullValue())
  					.extract().body()
  					.path("id");
  		
  		Map<String, Object> requestBody = Maps.newHashMap(createBrowserConceptRequest());
  		requestBody.put("conceptId", componentId);
  		SnomedBrowserRestRequests.createBrowserConcept(branchPath, requestBody).statusCode(200);
  	}
  	
	
	@Test		
  	public void changeInactivationReason() throws Exception {		
  		Map<String, Object> requestBody = Maps.newHashMap(createBrowserConceptRequest());
  		final ValidatableResponse response = SnomedBrowserRestRequests.createBrowserConcept(branchPath, requestBody).statusCode(200);
  		
  		
  		final String conceptId = response.extract().jsonPath().getString("conceptId");		
  		final Map<String, Object> responseBody = response.extract().jsonPath().get();		
  				
  		responseBody.put("active", false);		
  		responseBody.put("inactivationIndicator", InactivationReason.DUPLICATE.name());	// XXX: 
  		responseBody.put("associationTargets", ImmutableMultimap.<String, Object>of(AssociationType.REPLACED_BY.name(), MODULE_SCT_CORE).asMap());		
  		final Map<String, Object> responseBody2 = SnomedBrowserRestRequests.updateBrowserConcept(branchPath, conceptId, responseBody).statusCode(200)
  									.and().body("inactivationIndicator", equalTo(InactivationReason.DUPLICATE.name()))		
  									.and().extract().jsonPath().get();
  		
  		
  		responseBody2.put("inactivationIndicator", InactivationReason.AMBIGUOUS.name());		
  		SnomedBrowserRestRequests.updateBrowserConcept(branchPath, conceptId, responseBody2).statusCode(200)		
  									.and().body("inactivationIndicator", equalTo(InactivationReason.AMBIGUOUS.name()));
  		
  		
  	}
	
	
	
	@Test
	public void reactivateConcept() throws Exception {
		Map<String, Object> requestBody = Maps.newHashMap(createBrowserConceptRequest());
  		final ValidatableResponse response = SnomedBrowserRestRequests.createBrowserConcept(branchPath, requestBody)
  												.statusCode(200);
		
		
		final String conceptId = response.extract().jsonPath().getString("conceptId");
		final Map<String, Object> responseBody = response.extract().jsonPath().get();
		
		responseBody.put("active", false);
		responseBody.put("inactivationIndicator", InactivationReason.DUPLICATE.name());
		responseBody.put("associationTargets", ImmutableMultimap.<String, Object>of(AssociationType.REPLACED_BY.name(), MODULE_SCT_CORE).asMap());
		
		
		
		
		final Map<String, Object> responseBody2 = SnomedBrowserRestRequests.updateBrowserConcept(branchPath, conceptId, responseBody)
										.statusCode(200)
										.and().body("inactivationIndicator", equalTo(InactivationReason.DUPLICATE.name()))
										.and().extract().jsonPath().get();
		
		responseBody2.put("active", true);
		
		final List<Map<String, Object>> relationshipsResponse = (List<Map<String, Object>>) responseBody2.get("relationships");
		relationshipsResponse.get(0).put("active", true);
		
		
		SnomedBrowserRestRequests.updateBrowserConcept(branchPath, conceptId, responseBody2)
			.statusCode(200)
			.and().body("inactivationIndicator", nullValue())
				.and().body("associationTargets", nullValue())
				.and().body("active", equalTo(true))
				.and().body("descriptions[0].active", equalTo(true))
				.and().body("descriptions[0].inactivationIndicator", nullValue())
				.and().body("descriptions[1].active", equalTo(true))
				.and().body("descriptions[1].inactivationIndicator", nullValue())
				.and().body("relationships[0].active", equalTo(true));
	}
		
	

	@Test		
	public void releasedFlagOnReleasedConceptAndComponents() {
		
		Map<String, Object> requestBody = Maps.newHashMap(createBrowserConceptRequest());
  		final ValidatableResponse response = SnomedBrowserRestRequests.createBrowserConcept(branchPath, requestBody)
  												.statusCode(200).body("released", equalTo(false));

  		final String conceptId = response.extract().jsonPath().getString("conceptId");
				
		assertConceptIndexedBrowserPropertyIsNull(branchPath, conceptId, "effectiveTime");		
		assertConceptIndexedBrowserPropertyEquals(branchPath, conceptId, "released", false);		
		assertConceptIndexedBrowserPropertyEquals(branchPath, conceptId, "descriptions.released[0]", false);		
		assertConceptIndexedBrowserPropertyEquals(branchPath, conceptId, "relationships.released[0]", false);		
		
		final String effectiveDate = CodeSystemVersionRestRequests.getNextAvailableEffectiveDateAsString("SNOMEDCT");		
//		whenCreatingVersion("2017-01-31", effectiveDate).then().assertThat().statusCode(201);
		CodeSystemVersionRestRequests.createVersion("SNOMEDCT", effectiveDate, effectiveDate)
							.assertThat().statusCode(201);
				
		assertConceptIndexedBrowserPropertyEquals(branchPath, conceptId, "effectiveTime", effectiveDate);		
		assertConceptIndexedBrowserPropertyEquals(branchPath, conceptId, "released", true);		
		assertConceptIndexedBrowserPropertyEquals(branchPath, conceptId, "descriptions.released[0]", true);		
		assertConceptIndexedBrowserPropertyEquals(branchPath, conceptId, "relationships.released[0]", true);		
	}
	
	private void assertConceptIndexedBrowserPropertyEquals(IBranchPath branchPath, String conceptId, String propertyName, Object propertyValue) {
		SnomedBrowserRestRequests.getBrowserConcept(branchPath, conceptId)
						.assertThat().statusCode(200)
						.and().body(propertyName, equalTo(propertyValue));
	}

	private void assertConceptIndexedBrowserPropertyIsNull(IBranchPath branchPath, String conceptId, String propertyName) {
		SnomedBrowserRestRequests.getBrowserConcept(branchPath, conceptId)
						.assertThat().statusCode(200)
						.and().body(propertyName, nullValue());
		
	}

	private Map<String, Object> getFsn(final Map<String, Object> conceptOne) {
		@SuppressWarnings("unchecked")
		final List<Map<String, Object>> descs = (List<Map<String, Object>>) conceptOne.get("descriptions");
		for (Map<String, Object> desc : descs) {
			if ("FSN".equals(desc.get("type"))) {
				return desc;
			}
		}
		return null;
	}
	
	@Test		
	public void bulkUpdateConcepts() throws JsonProcessingException, IOException, InterruptedException {		
				
		Map<String, Object> requestBody_1 = Maps.newHashMap(createBrowserConceptRequest());
		requestBody_1.put("descriptions", createDefaultDescriptions("pt-1", "fsn-1"));
		final ValidatableResponse response_1 = SnomedBrowserRestRequests.createBrowserConcept(branchPath, requestBody_1)
									.assertThat().statusCode(200);
		final String conceptId_1 = response_1.extract().jsonPath().getString("conceptId");
		final Map<String, Object> responseBody_1 = Maps.newHashMap(response_1.extract().jsonPath().get());
		
		final Map<String, Object> fsnOne = getFsn(responseBody_1);
		
  		
		Map<String, Object> requestBody_2 = Maps.newHashMap(createBrowserConceptRequest());
  		requestBody_2.put("descriptions", createDefaultDescriptions("pt-2", "fsn-2"));
  		final ValidatableResponse response_2 = SnomedBrowserRestRequests.createBrowserConcept(branchPath, requestBody_2)
  									.assertThat().statusCode(200);
  		
  		final String conceptId_2 = response_2.extract().jsonPath().getString("conceptId");
  		final Map<String, Object> responseBody_2 = Maps.newHashMap(response_2.extract().jsonPath().get());
  		
  		final Map<String, Object> fsnTwo = getFsn(responseBody_2);	
  		
		// Prepare updates		
		List<Map<String, Object>> concepts = new ArrayList<>();		
		
		fsnOne.put("term", "OneA");		
		fsnOne.remove("descriptionId");		
		concepts.add(responseBody_1);		
				
		fsnTwo.put("term", "TwoA");		
		fsnTwo.remove("descriptionId");		
		concepts.add(responseBody_2);		
		
		// Post updates		
		final String bulkId = SnomedBrowserRestRequests.bulkUpdateBrowserConcepts(branchPath, concepts).assertThat().statusCode(201).extract().header("Location");
				
		// Wait for completion		
		SnomedBrowserRestRequests.waitForGetBrowserConceptChanges(branchPath, bulkId);
				
		// Load concepts and assert updated
		SnomedComponentRestRequests.getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId_1, "fsn()").assertThat().statusCode(200).body("fsn.term", equalTo("OneA"));
		SnomedComponentRestRequests.getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId_2, "fsn()").assertThat().statusCode(200).body("fsn.term", equalTo("TwoA"));
	}
	
	
	@Test		
	public void getConceptChildren() throws IOException {		
		
		// Create child of Finding context		
		Map<String, Object> requestBody = Maps.newHashMap(createBrowserConceptRequest());
		requestBody.put("relationships", createIsaRelationship(FINDING_CONTEXT, CharacteristicType.STATED_RELATIONSHIP));
		String fsn = "Special finding context (attribute)";
		String pt = "Special finding context";
		requestBody.put("descriptions", createDefaultDescriptions(pt, fsn));
		
		
		SnomedBrowserRestRequests.createBrowserConcept(branchPath, requestBody)
									.assertThat().statusCode(200);
		
		SnomedBrowserRestRequests.getBrowserConceptChildren(branchPath, FINDING_CONTEXT, "stated&preferredDescriptionType=FSN")
				.assertThat().statusCode(200)		
				.and().body("size()", equalTo(1))		
				.and().body("[0].fsn", equalTo(fsn));
		
		SnomedBrowserRestRequests.getBrowserConceptChildren(branchPath, FINDING_CONTEXT, "stated&preferredDescriptionType=SYNONYM")
				.assertThat().statusCode(200)		
				.and().body("size()", equalTo(1))		
				.and().body("[0].preferredSynonym", equalTo(pt));
	}
	
	
	@Test		
	public void searchDescriptions() throws IOException {
		
		// Create child of Finding context with unique PT and FSN		
		Map<String, Object> requestBody = Maps.newHashMap(createBrowserConceptRequest());
		requestBody.put("relationships", createIsaRelationship(FINDING_CONTEXT, CharacteristicType.STATED_RELATIONSHIP));
		String fsn = "Visotactile finding context (attribute)";
		String pt = "Circulatory finding context";
		requestBody.put("descriptions", createDefaultDescriptions(pt, fsn));
		
		
		SnomedBrowserRestRequests.createBrowserConcept(branchPath, requestBody)
									.assertThat().statusCode(200);
				
		SnomedBrowserRestRequests.searchDescriptionsFSN(branchPath, "visotactile")
				.assertThat().statusCode(200)		
				.and().body("size()", equalTo(1))		
				.and().body("[0].concept.fsn", equalTo(fsn));
		
		SnomedBrowserRestRequests.searchDescriptionsPT(branchPath, "circulatory")
				.assertThat().statusCode(200)		
				.and().body("size()", equalTo(1))		
				.and().body("[0].concept.preferredSynonym", equalTo(pt));
	}
	
	@Test
	public void createConceptNonExistentBranch() {
		createBrowserConcept(BranchPathUtils.createPath("MAIN/x/y/z"), createBrowserConceptRequest()).statusCode(404);
	}

	@Test
	public void createConceptWithoutParent() {
		Map<?, ?> conceptRequest = newHashMap(createBrowserConceptRequest());
		conceptRequest.remove("relationships");

		createBrowserConcept(branchPath, conceptRequest).statusCode(400)
		.body("message", equalTo("At least one active stated IS A relationship is required."));
	}

	@Test
	public void createConceptWithNonexistentParent() {
		String conceptId = createNewConcept(branchPath);

		deleteComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, false).statusCode(204);

		Map<String, Object> conceptRequest = newHashMap(createBrowserConceptRequest());
		conceptRequest.put("relationships", createIsaRelationship(conceptId, CharacteristicType.STATED_RELATIONSHIP));

		createBrowserConcept(branchPath, conceptRequest).statusCode(400);
	}

	@Test
	public void createRegularConcept() {
		createBrowserConcept(branchPath, createBrowserConceptRequest()).statusCode(200);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void createConceptWithReservedId() {
		String expectedConceptId = reserveComponentId(null, ComponentCategory.CONCEPT);

		Map<String, Object> conceptRequest = newHashMap(createBrowserConceptRequest());
		conceptRequest.put("conceptId", expectedConceptId);

		Map<String, Object> conceptResponse = createBrowserConcept(branchPath, conceptRequest).statusCode(200)
				.extract().as(Map.class);

		String actualConceptId = (String) conceptResponse.get("conceptId");
		assertEquals(expectedConceptId, actualConceptId);
	}

	@Test
	public void createConceptOnDeletedBranch() {
		deleteBranch(branchPath).statusCode(204);
		createBrowserConcept(branchPath, createBrowserConceptRequest()).statusCode(400);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void inactivateConcept() throws Exception {
		Map<String, Object> conceptRequest = createBrowserConcept(branchPath, createBrowserConceptRequest()).statusCode(200)
				.extract().as(Map.class);

		String conceptId = (String) conceptRequest.get("conceptId");
		conceptRequest.put("active", false);
		updateBrowserConcept(branchPath, conceptId, conceptRequest).statusCode(200);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void removeAllRelationshipsFromConcept() throws Exception {
		Map<String, Object> conceptRequest = createBrowserConcept(branchPath, createBrowserConceptRequest()).statusCode(200)
				.extract().as(Map.class);

		String conceptId = (String) conceptRequest.get("conceptId");
		conceptRequest.remove("relationships");
		updateBrowserConcept(branchPath, conceptId, conceptRequest).statusCode(200);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void updateConceptWithNewComponents() {
		Map<String, Object> conceptRequest = createBrowserConcept(branchPath, createBrowserConceptRequest()).statusCode(200)
				.extract().as(Map.class);

		String conceptId = (String) conceptRequest.get("conceptId");

		List<Object> descriptionsList = getListElement(conceptRequest, "descriptions");
		descriptionsList.addAll(createNewDescriptions(5));

		List<Object> relationshipsList = getListElement(conceptRequest, "relationships");
		relationshipsList.addAll(createNewRelationships(5, conceptId));

		Map<String, Object> updatedConcept = updateBrowserConcept(branchPath, conceptId, conceptRequest).statusCode(200)
				.extract().as(Map.class);

		List<Object> updatedDescriptions = getListElement(updatedConcept, "descriptions");
		assertEquals(7, updatedDescriptions.size());

		List<Object> updatedRelationships = getListElement(updatedConcept, "relationships");
		assertEquals(6, updatedRelationships.size());
	}
}