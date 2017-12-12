/*
 * Copyright 2011-2016 B2i Healthcare Pte Ltd, http://b2i.sg
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
package com.b2international.snowowl.snomed.api.rest.branches;

import static com.b2international.snowowl.snomed.SnomedConstants.Concepts.SYNONYM;
import static com.b2international.snowowl.snomed.api.rest.SnomedMergeReviewingRestRequests.createMergeReview;
import static com.b2international.snowowl.snomed.api.rest.SnomedMergeReviewingRestRequests.getMergeReview;
import static com.b2international.snowowl.snomed.api.rest.SnomedMergeReviewingRestRequests.getMergeReviewDetailsResponse;
import static com.b2international.snowowl.snomed.api.rest.SnomedMergeReviewingRestRequests.mergeAndApply;
import static com.b2international.snowowl.snomed.api.rest.SnomedMergeReviewingRestRequests.reviewLocation;
import static com.b2international.snowowl.snomed.api.rest.SnomedMergeReviewingRestRequests.storeConceptForMergeReview;
import static com.b2international.snowowl.snomed.api.rest.SnomedMergeReviewingRestRequests.waitForMergeReviewJob;
import static com.b2international.snowowl.test.commons.rest.RestExtensions.givenAuthenticatedRequest;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Map;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

import com.b2international.snowowl.core.api.IBranchPath;
import com.b2international.snowowl.core.branch.Branch;
import com.b2international.snowowl.datastore.BranchPathUtils;
import com.b2international.snowowl.datastore.review.ReviewStatus;
import com.b2international.snowowl.snomed.SnomedConstants.Concepts;
import com.b2international.snowowl.snomed.api.rest.AbstractSnomedApiTest;
import com.b2international.snowowl.snomed.api.rest.SnomedApiTestConstants;
import com.b2international.snowowl.snomed.api.rest.SnomedBranchingRestRequests;
import com.b2international.snowowl.snomed.api.rest.SnomedComponentRestRequests;
import com.b2international.snowowl.snomed.api.rest.SnomedComponentType;
import com.b2international.snowowl.snomed.api.rest.SnomedMergingRestRequests;
import com.b2international.snowowl.snomed.api.rest.SnomedRestFixtures;
import com.b2international.snowowl.snomed.core.domain.Acceptability;
import com.b2international.snowowl.snomed.core.domain.CaseSignificance;
import com.b2international.snowowl.snomed.core.domain.CharacteristicType;
import com.b2international.snowowl.snomed.core.domain.DefinitionStatus;
import com.b2international.snowowl.snomed.core.domain.SnomedConcept;
import com.b2international.snowowl.snomed.snomedrefset.SnomedRefSetType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableMap;
import com.jayway.restassured.http.ContentType;

/**
 * @since 4.6
 */
public class SnomedMergeReviewApiTest extends AbstractSnomedApiTest {

	
	private static final String FINDING_CONTEXT = "408729009";
	
	
	@Test
	public void createMergeReviewEmptyFields() {
		createMergeReview("", "")
			.statusCode(400)
			.body("message", equalTo("2 validation errors"))
			.body("violations", hasItem("'source' may not be empty (was '')"))
			.body("violations", hasItem("'target' may not be empty (was '')"));
	}
	
	@Test
	public void createMergeReviewNonExistentBranch() {
		createMergeReview(branchPath.getPath(), Branch.MAIN_PATH)
			.statusCode(400);
	}
	
	@Test
	public void createReview() {
		SnomedBranchingRestRequests.createBranch(branchPath);
		final String reviewId = reviewLocation(createMergeReview(branchPath.getParentPath(), branchPath.getPath()));
		getMergeReview(reviewId)
			.statusCode(200)
			.body("status", CoreMatchers.anyOf(equalTo(ReviewStatus.CURRENT.toString()), equalTo(ReviewStatus.PENDING.toString())));
		
		assertReviewCurrent(reviewId);
	}

	private void assertReviewCurrent(final String reviewId) {
		waitForMergeReviewJob(reviewId)
			.statusCode(200)
			.body("status", equalTo(ReviewStatus.CURRENT.toString()));
	}

	@Test
	public void createEmptyMergeReview() {
		SnomedBranchingRestRequests.createBranch(branchPath);
		
		final IBranchPath setupBranch = BranchPathUtils.createPath(branchPath, "a"); 
		SnomedBranchingRestRequests.createBranchRecursively(setupBranch);
		
		// Create new inferred relationship on "Finding context"
		SnomedRestFixtures.createNewRelationship(setupBranch, FINDING_CONTEXT, Concepts.IS_A, Concepts.ROOT_CONCEPT, CharacteristicType.INFERRED_RELATIONSHIP);
		
//		// Another inferred relationship goes on the parent branch
		SnomedRestFixtures.createNewRelationship(setupBranch.getParent(), FINDING_CONTEXT, Concepts.IS_A, Concepts.MODULE_ROOT, CharacteristicType.INFERRED_RELATIONSHIP);
		
		// See what happened on the sibling branch before merging changes to its parent
		final String reviewId = reviewLocation(createMergeReview(setupBranch.getPath(), setupBranch.getParentPath()));
		assertReviewCurrent(reviewId);
		
		JsonNode reviewDetails = getMergeReviewDetailsResponse(reviewId);
		
		assertTrue(reviewDetails.isArray());
		assertEquals(0, reviewDetails.size());
	}
	
	@Test
	public void createConflictingStatedMergeReview() {
		final IBranchPath setupBranch = BranchPathUtils.createPath(branchPath, "a");
		SnomedBranchingRestRequests.createBranchRecursively(setupBranch);
		
		// Create new stated relationship on "Finding context"
		SnomedRestFixtures.createNewRelationship(setupBranch, FINDING_CONTEXT, Concepts.IS_A, Concepts.ROOT_CONCEPT, CharacteristicType.STATED_RELATIONSHIP);
		
		// Another stated relationship goes on the parent branch
		SnomedRestFixtures.createNewRelationship(setupBranch.getParent(), FINDING_CONTEXT, Concepts.IS_A, Concepts.MODULE_ROOT, CharacteristicType.STATED_RELATIONSHIP);
		
		// See what happened on the sibling branch before merging changes to its parent
		String reviewId = reviewLocation(createMergeReview(setupBranch.getPath(), setupBranch.getParentPath()));
		assertReviewCurrent(reviewId);
		
		JsonNode reviewDetails = getMergeReviewDetailsResponse(reviewId);
		
		assertTrue(reviewDetails.isArray());
		assertEquals(1, reviewDetails.size());
	}
	
	@Test
	public void createConflictingStatedAndInferredMergeReview() {
		SnomedBranchingRestRequests.createBranch(branchPath);
		final IBranchPath setupBranch = BranchPathUtils.createPath(branchPath, "a");
		SnomedBranchingRestRequests.createBranchRecursively(setupBranch);
		
		// Create new stated relationship on "Finding context"
		SnomedRestFixtures.createNewRelationship(setupBranch, FINDING_CONTEXT, Concepts.IS_A, Concepts.ROOT_CONCEPT, CharacteristicType.STATED_RELATIONSHIP);
		
		// Another inferred relationship goes on the parent branch
		SnomedRestFixtures.createNewRelationship(setupBranch.getParent(), FINDING_CONTEXT, Concepts.IS_A, Concepts.MODULE_ROOT, CharacteristicType.INFERRED_RELATIONSHIP);
		
		String reviewId = reviewLocation(createMergeReview(setupBranch.getPath(), setupBranch.getParentPath()));
		// See what happened on the sibling branch before merging changes to its parent
		assertReviewCurrent(reviewId);
		JsonNode reviewDetails = getMergeReviewDetailsResponse(reviewId);
		
		// Concept will still be returned as requiring manual merge.
		assertTrue(reviewDetails.isArray());
		assertEquals(0, reviewDetails.size());
	}
	
	@Test
	public void createInvalidMergeReviewChangesForTheSameConcept() {
		
		String parentConceptId = SnomedRestFixtures.createNewConcept(branchPath, Concepts.ROOT_CONCEPT); // parent concept

		SnomedConcept parentConcept = SnomedComponentRestRequests
				.getComponent(branchPath, SnomedComponentType.CONCEPT, parentConceptId, "relationships()")
				.statusCode(200)
				.extract()
				.as(SnomedConcept.class);

		String parentStatedIsaId = parentConcept.getRelationships().getItems().stream()
				.filter(r -> r.getCharacteristicType() == CharacteristicType.STATED_RELATIONSHIP).findFirst().get().getId();

		String parentInferredIsaId = SnomedRestFixtures.createNewRelationship(branchPath, parentConceptId, Concepts.IS_A, Concepts.ROOT_CONCEPT,
				CharacteristicType.INFERRED_RELATIONSHIP);

		String childConceptId = SnomedRestFixtures.createNewConcept(branchPath, parentConceptId); // child concept
		SnomedRestFixtures.createNewRelationship(branchPath, childConceptId, Concepts.IS_A, parentConceptId,
				CharacteristicType.INFERRED_RELATIONSHIP);

		final IBranchPath taskBranch = BranchPathUtils.createPath(branchPath, "a");
		SnomedBranchingRestRequests.createBranchRecursively(taskBranch);

		// reparent parent concept so ancestors of child must be recalculated

		SnomedRestFixtures.createNewRelationship(branchPath, parentConceptId, Concepts.IS_A, FINDING_CONTEXT, CharacteristicType.STATED_RELATIONSHIP);
		SnomedRestFixtures.createNewRelationship(branchPath, parentConceptId, Concepts.IS_A, FINDING_CONTEXT,
				CharacteristicType.INFERRED_RELATIONSHIP);

		SnomedRestFixtures.inactivateRelationship(branchPath, parentStatedIsaId);
		SnomedRestFixtures.inactivateRelationship(branchPath, parentInferredIsaId);

		// change description of child concept on task branch

		SnomedConcept childConcept = SnomedComponentRestRequests
				.getComponent(taskBranch, SnomedComponentType.CONCEPT, childConceptId, "descriptions()")
				.statusCode(200)
				.extract()
				.as(SnomedConcept.class);

		String childSynonymId = childConcept.getDescriptions().getItems().stream().filter(d -> d.getTypeId().equals(Concepts.SYNONYM)).findFirst()
				.get().getId();

		SnomedRestFixtures.inactivateDescription(taskBranch, childSynonymId); // inactivate old pt
		SnomedRestFixtures.createNewDescription(taskBranch, childConceptId, Concepts.SYNONYM, SnomedApiTestConstants.UK_PREFERRED_MAP); // add new pt

		// now try to request a merge review for task rebase

		String reviewId = reviewLocation(createMergeReview(branchPath.getPath(), taskBranch.getPath()));
		assertReviewCurrent(reviewId);
		JsonNode reviewDetails = getMergeReviewDetailsResponse(reviewId);

		// there should be no merge screen triggered in such scenarios
		assertTrue(reviewDetails.isArray());
		assertEquals(0, reviewDetails.size());
	}
	
	@Test
	public void conflictingFsnChangeShouldCauseMergeReview() {
		
		String conceptId = SnomedRestFixtures.createNewConcept(branchPath, Concepts.ROOT_CONCEPT); // parent concept

		SnomedConcept concept = SnomedComponentRestRequests
				.getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, "descriptions()")
				.statusCode(200)
				.extract()
				.as(SnomedConcept.class);

		String fsnId = concept.getDescriptions().getItems().stream()
				.filter(r -> r.getTypeId().equals(Concepts.FULLY_SPECIFIED_NAME)).findFirst().get().getId();

		final IBranchPath taskBranch = BranchPathUtils.createPath(branchPath, "a");
		SnomedBranchingRestRequests.createBranchRecursively(taskBranch);

		// change case significance on parent branch
		SnomedRestFixtures.changeCaseSignificance(branchPath, fsnId, CaseSignificance.ENTIRE_TERM_CASE_SENSITIVE);

		// add new fsn on task branch
		SnomedRestFixtures.inactivateDescription(taskBranch, fsnId);
		SnomedRestFixtures.createNewDescription(taskBranch, conceptId, Concepts.FULLY_SPECIFIED_NAME, SnomedApiTestConstants.UK_PREFERRED_MAP);
		
		// now try to request a merge review for task rebase
		String reviewId = reviewLocation(createMergeReview(branchPath.getPath(), taskBranch.getPath()));
		assertReviewCurrent(reviewId);
		JsonNode reviewDetails = getMergeReviewDetailsResponse(reviewId);

		// merge screen should be triggered in such scenarios
		assertTrue(reviewDetails.isArray());
		assertEquals(1, reviewDetails.size());
	}
	
	@Test
	public void singleFsnChangeShouldNotCauseMergeReview() {
		
		String conceptId = SnomedRestFixtures.createNewConcept(branchPath, Concepts.ROOT_CONCEPT);

		SnomedConcept concept = SnomedComponentRestRequests
				.getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, "descriptions()")
				.statusCode(200)
				.extract()
				.as(SnomedConcept.class);

		String fsnId = concept.getDescriptions().getItems().stream()
				.filter(r -> r.getTypeId().equals(Concepts.FULLY_SPECIFIED_NAME)).findFirst().get().getId();

		final IBranchPath taskBranch = BranchPathUtils.createPath(branchPath, "a");
		SnomedBranchingRestRequests.createBranchRecursively(taskBranch);

		// add new concept on parent branch
		SnomedRestFixtures.createNewConcept(branchPath, Concepts.ROOT_CONCEPT);
		
		// add new fsn on task branch
		SnomedRestFixtures.inactivateDescription(taskBranch, fsnId);
		SnomedRestFixtures.createNewDescription(taskBranch, conceptId, Concepts.FULLY_SPECIFIED_NAME, SnomedApiTestConstants.UK_PREFERRED_MAP);
		
		// now try to request a merge review for task rebase
		String reviewId = reviewLocation(createMergeReview(branchPath.getPath(), taskBranch.getPath()));
		assertReviewCurrent(reviewId);
		JsonNode reviewDetails = getMergeReviewDetailsResponse(reviewId);

		// there should be no merge screen triggered in such scenarios
		assertTrue(reviewDetails.isArray());
		assertEquals(0, reviewDetails.size());
		
	}
	
	@Test
	public void inferredChangesShouldNotCauseMergeReview() {
		
		// create new concept
		String conceptId = SnomedRestFixtures.createNewConcept(branchPath, Concepts.ROOT_CONCEPT); // parent concept

		// create task branch
		final IBranchPath taskBranch = BranchPathUtils.createPath(branchPath, "a");
		SnomedBranchingRestRequests.createBranchRecursively(taskBranch);
		
		// add inferred relationship on parent branch
		SnomedRestFixtures.createNewRelationship(branchPath, conceptId, Concepts.IS_A, Concepts.ROOT_CONCEPT, CharacteristicType.INFERRED_RELATIONSHIP);

		// add inferred relationship on task branch
		SnomedRestFixtures.createNewRelationship(taskBranch, conceptId, Concepts.IS_A, FINDING_CONTEXT, CharacteristicType.INFERRED_RELATIONSHIP);

		// now try to request a merge review for task rebase
		String reviewId = reviewLocation(createMergeReview(branchPath.getPath(), taskBranch.getPath()));
		assertReviewCurrent(reviewId);
		JsonNode reviewDetails = getMergeReviewDetailsResponse(reviewId);

		// there should be no merge screen triggered in such scenarios
		assertTrue(reviewDetails.isArray());
		assertEquals(0, reviewDetails.size());
	}
	
	@Test
	public void concurrentRelationshipAndDescriptionChangesShouldNotCauseMergeReview() {
		
		String conceptId = SnomedRestFixtures.createNewConcept(branchPath, Concepts.ROOT_CONCEPT);

		SnomedConcept concept = SnomedComponentRestRequests
				.getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, "descriptions()")
				.statusCode(200)
				.extract()
				.as(SnomedConcept.class);

		String ptId = concept.getDescriptions().getItems().stream().filter(r -> r.getTypeId().equals(Concepts.SYNONYM)).findFirst().get().getId();

		final IBranchPath taskBranch = BranchPathUtils.createPath(branchPath, "a");
		SnomedBranchingRestRequests.createBranchRecursively(taskBranch);

		// add new relationship to concept on parent branch
		SnomedRestFixtures.createNewRelationship(branchPath, conceptId, Concepts.IS_A, Concepts.ROOT_CONCEPT, CharacteristicType.INFERRED_RELATIONSHIP);
		
		// add new pt on task branch
		SnomedRestFixtures.inactivateDescription(taskBranch, ptId);
		SnomedRestFixtures.createNewDescription(taskBranch, conceptId, Concepts.SYNONYM, SnomedApiTestConstants.UK_PREFERRED_MAP);
		
		// now try to request a merge review for task rebase
		String reviewId = reviewLocation(createMergeReview(branchPath.getPath(), taskBranch.getPath()));
		assertReviewCurrent(reviewId);
		JsonNode reviewDetails = getMergeReviewDetailsResponse(reviewId);

		// there should be no merge screen triggered in such scenarios
		assertTrue(reviewDetails.isArray());
		assertEquals(0, reviewDetails.size());
	}
	
	@Test
	public void concurrentConceptPropertyAndDescriptionChangesShouldNotCauseMergeReview() {
		
		String conceptId = SnomedRestFixtures.createNewConcept(branchPath, Concepts.ROOT_CONCEPT);

		SnomedConcept concept = SnomedComponentRestRequests
				.getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, "descriptions()")
				.statusCode(200)
				.extract()
				.as(SnomedConcept.class);

		String ptId = concept.getDescriptions().getItems().stream().filter(r -> r.getTypeId().equals(Concepts.SYNONYM)).findFirst().get().getId();

		final IBranchPath taskBranch = BranchPathUtils.createPath(branchPath, "a");
		SnomedBranchingRestRequests.createBranchRecursively(taskBranch);

		// change definition status of concept on parent branch
		SnomedRestFixtures.changeToDefining(branchPath, conceptId);
		
		// add new pt on task branch
		SnomedRestFixtures.inactivateDescription(taskBranch, ptId);
		SnomedRestFixtures.createNewDescription(taskBranch, conceptId, Concepts.SYNONYM, SnomedApiTestConstants.UK_PREFERRED_MAP);
		
		// now try to request a merge review for task rebase
		String reviewId = reviewLocation(createMergeReview(branchPath.getPath(), taskBranch.getPath()));
		assertReviewCurrent(reviewId);
		JsonNode reviewDetails = getMergeReviewDetailsResponse(reviewId);

		// there should be no merge screen triggered in such scenarios
		assertTrue(reviewDetails.isArray());
		assertEquals(0, reviewDetails.size());
	}
	
	@Test
	public void setReviewStale() throws Exception {
		SnomedBranchingRestRequests.createBranch(branchPath);

		// Set up a review...
		String reviewId = reviewLocation(createMergeReview(branchPath.getParentPath(), branchPath.getPath()));
		assertReviewCurrent(reviewId);
		
		// ...then commit to the branch.
		SnomedRestFixtures.createNewConcept(branchPath);
		// wait 1s before checking review state 
		Thread.sleep(1000);
		
		waitForMergeReviewJob(reviewId)
			.statusCode(200)
			.body("status", equalTo(ReviewStatus.STALE.toString()));		
	}
	
	@Test
	public void setReviewStaleAfterParentRebase() {
		SnomedBranchingRestRequests.createBranch(branchPath);
		// Create all branches down to MAIN/test/A
		IBranchPath nestedBranchPath =  BranchPathUtils.createPath(branchPath, "A");
		SnomedBranchingRestRequests.createBranchRecursively(nestedBranchPath);

		// Set up review for a rebase of MAIN/test/A
		final String reviewId = reviewLocation(createMergeReview(branchPath.getPath(), nestedBranchPath.getPath()));
		assertReviewCurrent(reviewId);

		// Rebase branchPath on MAIN
		SnomedMergingRestRequests.createMerge(nestedBranchPath, branchPath, "Rebasing branchPath on MAIN", reviewId);

		// Create a new concept on MAIN
		SnomedRestFixtures.createNewConcept(nestedBranchPath);

		waitForMergeReviewJob(reviewId)
			.statusCode(200)
			.body("status", equalTo(ReviewStatus.STALE.toString()));		
	}
	
	@Test
	public void noDescriptionIdsInChangedConceptIdsWhenConceptChangedForMergeReview() {
		SnomedBranchingRestRequests.createBranch(branchPath); // project branch
		
		IBranchPath firstTaskPath = BranchPathUtils.createPath(branchPath, "1");
		SnomedBranchingRestRequests.createBranch(firstTaskPath);
		
		String concept = SnomedRestFixtures.createNewConcept(firstTaskPath);
		SnomedRestFixtures.merge(firstTaskPath, firstTaskPath.getParent(), "Merging first task into project");
		SnomedComponentRestRequests.getComponent(branchPath, SnomedComponentType.CONCEPT, concept).statusCode(200);
		
		IBranchPath secondTaskPath = BranchPathUtils.createPath(branchPath, "2");
		SnomedBranchingRestRequests.createBranch(secondTaskPath);
		SnomedRestFixtures.createNewDescription(secondTaskPath, concept, SYNONYM);
		
		IBranchPath thirdTaskPath = BranchPathUtils.createPath(branchPath, "3");
		SnomedBranchingRestRequests.createBranch(thirdTaskPath);
		SnomedRestFixtures.createNewDescription(thirdTaskPath, concept, SYNONYM);
		
		SnomedRestFixtures.merge(thirdTaskPath, thirdTaskPath.getParent(), "Merging new description to project");
		
		final String reviewId = reviewLocation(createMergeReview(branchPath.getPath(), secondTaskPath.getPath()));
		assertReviewCurrent(reviewId);
		
		JsonNode reviewDetails = getMergeReviewDetailsResponse(reviewId);
		
		assertTrue(reviewDetails.isArray());
		assertEquals(1, reviewDetails.size());
		
		JsonNode jsonNode = reviewDetails.get(0).get("autoMergedConcept");
		
		givenAuthenticatedRequest(SnomedApiTestConstants.SCT_API)
			.with().header("Accept-Language", "en-GB")
			.content(jsonNode)
			.contentType(ContentType.JSON)
			.when()
			.post("/merge-reviews/{id}/{conceptId}", reviewId, concept)
			.then()
			.statusCode(200);
		
		givenAuthenticatedRequest(SnomedApiTestConstants.SCT_API)
			.with().header("Accept-Language", "en-GB")
			.when()
			.post("/merge-reviews/{id}/apply", reviewId)
			.then()
			.statusCode(204);
		
	}
	
	@Test
	public void taskRebaseWithMergeReviewApplyAutomergedConcept() throws InterruptedException {
		// create branches: project, task-A, task-B
		SnomedBranchingRestRequests.createBranch(branchPath);
		
		IBranchPath task_A_path = BranchPathUtils.createPath(branchPath, "TASK-A");
		SnomedBranchingRestRequests.createBranch(task_A_path);
		
		IBranchPath task_B_path = BranchPathUtils.createPath(branchPath, "TASK-B");
		SnomedBranchingRestRequests.createBranch(task_B_path);
		
		// add new description to concept on task-A
		SnomedRestFixtures.createNewDescription(task_A_path, FINDING_CONTEXT, SYNONYM);
		SnomedComponentRestRequests.getComponent(task_A_path, SnomedComponentType.CONCEPT, FINDING_CONTEXT, "descriptions()", "relationships()").statusCode(200)
			.body("active", equalTo(true))
			.body("descriptions.total", equalTo(3));

		// inactivate concept on task-B
		SnomedRestFixtures.inactivateConcept(task_B_path, FINDING_CONTEXT);
		SnomedComponentRestRequests.getComponent(task_B_path, SnomedComponentType.CONCEPT, FINDING_CONTEXT, "descriptions()", "relationships()").statusCode(200)
			.body("active", equalTo(false))
			.body("descriptions.total", equalTo(2))
			.body("descriptions.items[0].active", equalTo(true))
			.body("descriptions.items[1].active", equalTo(true));
		
		// promote task-A to parent project
		SnomedRestFixtures.merge(task_A_path, branchPath, "promote task-A to parent project");
		SnomedComponentRestRequests.getComponent(branchPath, SnomedComponentType.CONCEPT, FINDING_CONTEXT, "descriptions()", "relationships()").statusCode(200)
			.body("active", equalTo(true))
			.body("descriptions.total", equalTo(3));

		// pull in changes from parent project to task-B
		// by creating a review to resolve merge conflict 		
		final String reviewId = reviewLocation(createMergeReview(branchPath.getPath(), task_B_path.getPath()));
		assertReviewCurrent(reviewId);
		
		JsonNode reviewDetails = getMergeReviewDetailsResponse(reviewId);
		assertTrue(reviewDetails.isArray());
		assertEquals(1, reviewDetails.size());
		
		// select suggested (middle) review as correct
		JsonNode jsonNode = reviewDetails.get(0).get("autoMergedConcept");
		
		// store and apply proposed merged concept
		storeConceptForMergeReview(reviewId, FINDING_CONTEXT, jsonNode);
		mergeAndApply(reviewId);
		
		// assert that after review the correct content is merged to task-B
		SnomedComponentRestRequests.getComponent(task_B_path, SnomedComponentType.CONCEPT, FINDING_CONTEXT, "descriptions()", "relationships()").statusCode(200)
			.body("active", equalTo(false))
			.body("descriptions.total", equalTo(3));
	}
	
	
	@Test
	public void taskRebaseWithMergeReviewApplyManuallyMergedConcept() throws InterruptedException {
		// create branches: project, task-A, task-B
		SnomedBranchingRestRequests.createBranch(branchPath);
		
		IBranchPath task_A_path = BranchPathUtils.createPath(branchPath, "TASK-A");
		SnomedBranchingRestRequests.createBranch(task_A_path);
		
		IBranchPath task_B_path = BranchPathUtils.createPath(branchPath, "TASK-B");
		SnomedBranchingRestRequests.createBranch(task_B_path);
		
		// add new description to concept on task-A
		SnomedRestFixtures.createNewDescription(task_A_path, FINDING_CONTEXT, SYNONYM);
		SnomedComponentRestRequests.getComponent(task_A_path, SnomedComponentType.CONCEPT, FINDING_CONTEXT, "descriptions()", "relationships()").statusCode(200)
			.body("active", equalTo(true))
			.body("descriptions.total", equalTo(3));
		
		
		// inactivate concept on task-B
		SnomedRestFixtures.inactivateConcept(task_B_path, FINDING_CONTEXT);
		SnomedComponentRestRequests.getComponent(task_B_path, SnomedComponentType.CONCEPT, FINDING_CONTEXT, "descriptions()", "relationships()").statusCode(200)
			.body("active", equalTo(false))
			.body("descriptions.total", equalTo(2))
			.body("descriptions.items[0].active", equalTo(true))
			.body("descriptions.items[1].active", equalTo(true));
		
		// promote task-A to parent project
		SnomedRestFixtures.merge(task_A_path, branchPath, "promote task-A to parent project");
		SnomedComponentRestRequests.getComponent(branchPath, SnomedComponentType.CONCEPT, FINDING_CONTEXT, "descriptions()", "relationships()").statusCode(200)
			.body("active", equalTo(true))
			.body("descriptions.total", equalTo(3));
		
		
		// pull in changes from parent project to task-B by creating a review to manually resolve merge conflict 		
		final String reviewId = reviewLocation(createMergeReview(branchPath.getPath(), task_B_path.getPath()));
		assertReviewCurrent(reviewId);
		
		JsonNode reviewDetails = getMergeReviewDetailsResponse(reviewId);
		assertTrue(reviewDetails.isArray());
		assertEquals(1, reviewDetails.size());
		
		// select the project version of the concept as correct and modify it a bit
		JsonNode jsonNode = reviewDetails.get(0).get("sourceConcept");
		ObjectNode objectNode = (ObjectNode) jsonNode;
		objectNode.put("definitionStatus", DefinitionStatus.FULLY_DEFINED.name());
		
		// store and apply merged concept
		storeConceptForMergeReview(reviewId, FINDING_CONTEXT, jsonNode);
		mergeAndApply(reviewId);
		
		// assert that after review the correct/merged content is present on task-B
		SnomedComponentRestRequests.getComponent(task_B_path, SnomedComponentType.CONCEPT, FINDING_CONTEXT, "descriptions()").statusCode(200)
			.body("active", equalTo(true))
			.body("descriptions.total", equalTo(3))
			.body("definitionStatus", equalTo(DefinitionStatus.FULLY_DEFINED.name()));
	}
	
	@Test
	public void noDescriptionIdsInChangedConceptIdsWhenConceptDeletedForMergeReview() {
		
		SnomedBranchingRestRequests.createBranch(branchPath); // project branch
		
		IBranchPath firstTaskPath = BranchPathUtils.createPath(branchPath, "1");
		SnomedBranchingRestRequests.createBranch(firstTaskPath);
		
		String concept = SnomedRestFixtures.createNewConcept(firstTaskPath);
		SnomedRestFixtures.merge(firstTaskPath, firstTaskPath.getParent(), "Merging first task into project");
		SnomedComponentRestRequests.getComponent(branchPath, SnomedComponentType.CONCEPT, concept).statusCode(200);
		
		IBranchPath secondTaskPath = BranchPathUtils.createPath(branchPath, "2");
		SnomedBranchingRestRequests.createBranch(secondTaskPath);
		
		SnomedRestFixtures.createNewDescription(secondTaskPath, concept, SYNONYM);
		
		IBranchPath thirdTaskPath = BranchPathUtils.createPath(branchPath, "3");
		SnomedBranchingRestRequests.createBranch(thirdTaskPath);
		SnomedRestFixtures.createNewDescription(thirdTaskPath, concept, SYNONYM);
		
		
		SnomedComponentRestRequests.deleteComponent(thirdTaskPath, SnomedComponentType.CONCEPT, concept, true);
		SnomedComponentRestRequests.getComponent(thirdTaskPath, SnomedComponentType.CONCEPT, concept).statusCode(404);
		
		SnomedRestFixtures.merge(thirdTaskPath, thirdTaskPath.getParent(), "Merging concept deletion to project");
		
		SnomedComponentRestRequests.getComponent(branchPath, SnomedComponentType.CONCEPT, concept).statusCode(404);
		
		final String reviewId = reviewLocation(createMergeReview(branchPath.getPath(), secondTaskPath.getPath()));
		assertReviewCurrent(reviewId);
		
		JsonNode reviewDetails = getMergeReviewDetailsResponse(reviewId);
		
		assertTrue(reviewDetails.isArray());
		assertEquals(0, reviewDetails.size());
		
	}
	
	@Test
	public void noDescriptionIdsInChangedConceptIdsForAcceptedMergeReview() {
		
		SnomedBranchingRestRequests.createBranch(branchPath); // project branch
		
		IBranchPath firstTaskPath = BranchPathUtils.createPath(branchPath, "1");
		SnomedBranchingRestRequests.createBranch(firstTaskPath);
		
		String conceptId = SnomedRestFixtures.createNewConcept(firstTaskPath);
		SnomedComponentRestRequests.getComponent(firstTaskPath, SnomedComponentType.CONCEPT, conceptId).statusCode(200);
		
		String descriptionId = SnomedRestFixtures.createNewDescription(firstTaskPath, conceptId, SYNONYM);
		SnomedComponentRestRequests.getComponent(firstTaskPath, SnomedComponentType.DESCRIPTION, descriptionId).statusCode(200);
		
		final Map<Object, Object> descriptionUpdateBody = ImmutableMap.<Object, Object>builder()
				.put("acceptability", Collections.emptyMap())
				.put("descriptionId", descriptionId)
				.put("commitComment", "Update description")
				.build();
		
		SnomedComponentRestRequests.updateComponent(firstTaskPath, SnomedComponentType.DESCRIPTION, descriptionId, descriptionUpdateBody);
		SnomedComponentRestRequests.getComponent(firstTaskPath, SnomedComponentType.DESCRIPTION, descriptionId).body("acceptability", equalTo(Collections.emptyMap()));
		
		SnomedRestFixtures.merge(firstTaskPath, firstTaskPath.getParent(), "Merging first task into project");
		SnomedComponentRestRequests.getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId).statusCode(200);
		SnomedComponentRestRequests.getComponent(branchPath, SnomedComponentType.DESCRIPTION, descriptionId).statusCode(200);

		IBranchPath secondTaskPath = BranchPathUtils.createPath(branchPath, "2");
		SnomedBranchingRestRequests.createBranch(secondTaskPath);
		
		final Map<Object, Object> descriptionUpdateBodyUk = ImmutableMap.<Object, Object>builder()
				.put("acceptability", SnomedRestFixtures.ACCEPTABLE_ACCEPTABILITY_MAP)
				.put("descriptionId", descriptionId)
				.put("commitComment", "Update description on second task")
				.build();
		
		SnomedComponentRestRequests.updateComponent(secondTaskPath, SnomedComponentType.DESCRIPTION, descriptionId, descriptionUpdateBodyUk);
		
		IBranchPath thirdTaskPath = BranchPathUtils.createPath(branchPath, "3");
		SnomedBranchingRestRequests.createBranch(thirdTaskPath);
		
		Map<String, Acceptability> usAcceptabilityMapPreferred = ImmutableMap.of(Concepts.REFSET_LANGUAGE_TYPE_US, Acceptability.PREFERRED);
		
		final Map<Object, Object> descriptionUpdateBodyUs = ImmutableMap.<Object, Object>builder()
				.put("acceptability", usAcceptabilityMapPreferred)
				.put("descriptionId", descriptionId)
				.put("commitComment", "Update description on third task")
				.build();
		
		SnomedComponentRestRequests.updateComponent(thirdTaskPath, SnomedComponentType.DESCRIPTION, descriptionId, descriptionUpdateBodyUs);
		
		SnomedRestFixtures.merge(thirdTaskPath, thirdTaskPath.getParent(), "Merging third task to project");
		
		final String reviewId = reviewLocation(createMergeReview(branchPath.getPath(), secondTaskPath.getPath()));
		assertReviewCurrent(reviewId);
		
		JsonNode reviewDetails = getMergeReviewDetailsResponse(reviewId);
		
		assertTrue(reviewDetails.isArray());
		assertEquals(1, reviewDetails.size());
		
		JsonNode jsonNode = reviewDetails.get(0).get("autoMergedConcept");
		
		givenAuthenticatedRequest(SnomedApiTestConstants.SCT_API)
			.with().header("Accept-Language", "en-GB")
			.content(jsonNode)
			.contentType(ContentType.JSON)
			.when()
			.post("/merge-reviews/{id}/{conceptId}", reviewId, conceptId)
			.then()
			.statusCode(200);
		
		givenAuthenticatedRequest(SnomedApiTestConstants.SCT_API)
			.with().header("Accept-Language", "en-GB")
			.when()
			.post("/merge-reviews/{id}/apply", reviewId)
			.then()
			.statusCode(204);
		
	}
	
	@Test
	public void noRelationshipIdsInChangedConceptIdsForMergeReview() {
		
		SnomedBranchingRestRequests.createBranch(branchPath); // project branch
		
		IBranchPath firstTaskPath = BranchPathUtils.createPath(branchPath, "1");
		SnomedBranchingRestRequests.createBranch(firstTaskPath);
		
		String conceptId = SnomedRestFixtures.createNewConcept(firstTaskPath);
		SnomedComponentRestRequests.getComponent(firstTaskPath, SnomedComponentType.CONCEPT, conceptId).statusCode(200);
		
		final String relationshipId = SnomedRestFixtures.createNewRelationship(firstTaskPath, conceptId, Concepts.IS_A, FINDING_CONTEXT, CharacteristicType.STATED_RELATIONSHIP);
		SnomedComponentRestRequests.getComponent(firstTaskPath, SnomedComponentType.RELATIONSHIP, relationshipId).statusCode(200);
		
		final String refSetId = SnomedRestFixtures.createNewRefSet(firstTaskPath, SnomedRefSetType.SIMPLE);
		SnomedComponentRestRequests.getComponent(firstTaskPath, SnomedComponentType.REFSET, refSetId).statusCode(200);
		
		final String memberId = SnomedRestFixtures.createNewRefSetMember(firstTaskPath, conceptId, refSetId);
		SnomedComponentRestRequests.getComponent(firstTaskPath, SnomedComponentType.MEMBER, memberId).statusCode(200);

		SnomedRestFixtures.merge(firstTaskPath, firstTaskPath.getParent(), "Merging first task into project");
		SnomedComponentRestRequests.getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId).statusCode(200);
		
		IBranchPath secondTaskPath = BranchPathUtils.createPath(branchPath, "2");
		SnomedBranchingRestRequests.createBranch(secondTaskPath);

		SnomedComponentRestRequests.getComponent(secondTaskPath, SnomedComponentType.MEMBER, memberId, "referencedComponent()")
				.body("moduleId", CoreMatchers.equalTo(Concepts.MODULE_SCT_CORE));
		
		final Map<?, ?> moduleUpdate = ImmutableMap.of("moduleId", "900000000000012004", "commitComment", "Update member module: " + memberId);

		givenAuthenticatedRequest(SnomedApiTestConstants.SCT_API)
			.with().contentType(ContentType.JSON)
			.and().body(moduleUpdate)
			.when().put("/{path}/{componentType}/{id}", secondTaskPath, SnomedComponentType.MEMBER.toLowerCasePlural(), memberId);
		
		SnomedComponentRestRequests.getComponent(secondTaskPath, SnomedComponentType.MEMBER, memberId, "referencedComponent()")
			.body("moduleId", CoreMatchers.equalTo("900000000000012004"));

		IBranchPath thirdTaskPath = BranchPathUtils.createPath(branchPath, "3");
		SnomedBranchingRestRequests.createBranch(thirdTaskPath);
		
		SnomedComponentRestRequests.getComponent(thirdTaskPath, SnomedComponentType.MEMBER, memberId, "referencedComponent()")
			.body("active", CoreMatchers.equalTo(true));
	
		final Map<?, ?> inactivationReq = ImmutableMap.of("active", false, "commitComment", "Inactivate member: " + memberId);
		
		givenAuthenticatedRequest(SnomedApiTestConstants.SCT_API)
			.with().contentType(ContentType.JSON)
			.and().body(inactivationReq)
			.when().put("/{path}/{componentType}/{id}", thirdTaskPath.getPath(), SnomedComponentType.MEMBER.toLowerCasePlural(), memberId);
		
		SnomedComponentRestRequests.getComponent(thirdTaskPath, SnomedComponentType.MEMBER, memberId, "referencedComponent()")
			.body("active", CoreMatchers.equalTo(false));
		
		SnomedRestFixtures.merge(thirdTaskPath, thirdTaskPath.getParent(), "Merging third task to project");
		
		final String reviewId = reviewLocation(createMergeReview(branchPath.getPath(), secondTaskPath.getPath()));
		assertReviewCurrent(reviewId);
		
		JsonNode reviewDetails = getMergeReviewDetailsResponse(reviewId);
		
		assertTrue(reviewDetails.isArray());
		assertEquals(0, reviewDetails.size());
		
	}
}
