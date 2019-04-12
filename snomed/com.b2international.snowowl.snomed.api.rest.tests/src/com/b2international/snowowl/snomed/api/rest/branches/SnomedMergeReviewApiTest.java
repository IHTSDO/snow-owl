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
package com.b2international.snowowl.snomed.api.rest.branches;

import static com.b2international.snowowl.snomed.SnomedConstants.Concepts.SYNONYM;
import static com.b2international.snowowl.snomed.api.rest.SnomedBranchingRestRequests.createBranch;
import static com.b2international.snowowl.snomed.api.rest.SnomedBranchingRestRequests.createBranchRecursively;
import static com.b2international.snowowl.snomed.api.rest.SnomedComponentRestRequests.getComponent;
import static com.b2international.snowowl.snomed.api.rest.SnomedComponentRestRequests.updateComponent;
import static com.b2international.snowowl.snomed.api.rest.SnomedMergeReviewingRestRequests.createMergeReview;
import static com.b2international.snowowl.snomed.api.rest.SnomedMergeReviewingRestRequests.getMergeReview;
import static com.b2international.snowowl.snomed.api.rest.SnomedMergeReviewingRestRequests.getMergeReviewDetailsResponse;
import static com.b2international.snowowl.snomed.api.rest.SnomedMergeReviewingRestRequests.mergeAndApply;
import static com.b2international.snowowl.snomed.api.rest.SnomedMergeReviewingRestRequests.reviewLocation;
import static com.b2international.snowowl.snomed.api.rest.SnomedMergeReviewingRestRequests.storeConceptForMergeReview;
import static com.b2international.snowowl.snomed.api.rest.SnomedMergeReviewingRestRequests.waitForMergeReviewJob;
import static com.b2international.snowowl.snomed.api.rest.SnomedRefSetRestRequests.updateRefSetComponent;
import static com.b2international.snowowl.snomed.api.rest.SnomedRestFixtures.ACCEPTABLE_ACCEPTABILITY_MAP;
import static com.b2international.snowowl.snomed.api.rest.SnomedRestFixtures.changeCaseSignificance;
import static com.b2international.snowowl.snomed.api.rest.SnomedRestFixtures.changeToDefining;
import static com.b2international.snowowl.snomed.api.rest.SnomedRestFixtures.createNewConcept;
import static com.b2international.snowowl.snomed.api.rest.SnomedRestFixtures.createNewDescription;
import static com.b2international.snowowl.snomed.api.rest.SnomedRestFixtures.createNewRefSet;
import static com.b2international.snowowl.snomed.api.rest.SnomedRestFixtures.createNewRefSetMember;
import static com.b2international.snowowl.snomed.api.rest.SnomedRestFixtures.createNewRelationship;
import static com.b2international.snowowl.snomed.api.rest.SnomedRestFixtures.inactivateConcept;
import static com.b2international.snowowl.snomed.api.rest.SnomedRestFixtures.inactivateDescription;
import static com.b2international.snowowl.snomed.api.rest.SnomedRestFixtures.inactivateRelationship;
import static com.b2international.snowowl.snomed.api.rest.SnomedRestFixtures.merge;
import static com.b2international.snowowl.test.commons.rest.RestExtensions.givenAuthenticatedRequest;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.nullValue;
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
import com.b2international.snowowl.snomed.api.rest.SnomedComponentRestRequests;
import com.b2international.snowowl.snomed.api.rest.SnomedComponentType;
import com.b2international.snowowl.snomed.api.rest.SnomedMergingRestRequests;
import com.b2international.snowowl.snomed.core.domain.Acceptability;
import com.b2international.snowowl.snomed.core.domain.AssociationType;
import com.b2international.snowowl.snomed.core.domain.CaseSignificance;
import com.b2international.snowowl.snomed.core.domain.CharacteristicType;
import com.b2international.snowowl.snomed.core.domain.DefinitionStatus;
import com.b2international.snowowl.snomed.core.domain.InactivationIndicator;
import com.b2international.snowowl.snomed.core.domain.SnomedConcept;
import com.b2international.snowowl.snomed.snomedrefset.SnomedRefSetType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import io.restassured.http.ContentType;

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
		
		final IBranchPath setupBranch = BranchPathUtils.createPath(branchPath, "a"); 
		createBranchRecursively(setupBranch);
		
		// Create new inferred relationship on "Finding context"
		createNewRelationship(setupBranch, FINDING_CONTEXT, Concepts.IS_A, Concepts.ROOT_CONCEPT, CharacteristicType.INFERRED_RELATIONSHIP);
		
		// Another inferred relationship goes on the parent branch
		createNewRelationship(setupBranch.getParent(), FINDING_CONTEXT, Concepts.IS_A, Concepts.MODULE_ROOT, CharacteristicType.INFERRED_RELATIONSHIP);
		
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
		createBranchRecursively(setupBranch);
		
		// Create new stated relationship on "Finding context"
		createNewRelationship(setupBranch, FINDING_CONTEXT, Concepts.IS_A, Concepts.ROOT_CONCEPT, CharacteristicType.STATED_RELATIONSHIP);
		
		// Another stated relationship goes on the parent branch
		createNewRelationship(setupBranch.getParent(), FINDING_CONTEXT, Concepts.IS_A, Concepts.MODULE_ROOT, CharacteristicType.STATED_RELATIONSHIP);
		
		// See what happened on the sibling branch before merging changes to its parent
		String reviewId = reviewLocation(createMergeReview(setupBranch.getPath(), setupBranch.getParentPath()));
		assertReviewCurrent(reviewId);
		
		JsonNode reviewDetails = getMergeReviewDetailsResponse(reviewId);
		
		assertTrue(reviewDetails.isArray());
		assertEquals(1, reviewDetails.size());
	}
	
	@Test
	public void createConflictingStatedAndInferredMergeReview() {
		
		final IBranchPath setupBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranchRecursively(setupBranch);
		
		// Create new stated relationship on "Finding context"
		createNewRelationship(setupBranch, FINDING_CONTEXT, Concepts.IS_A, Concepts.ROOT_CONCEPT, CharacteristicType.STATED_RELATIONSHIP);
		
		// Another inferred relationship goes on the parent branch
		createNewRelationship(setupBranch.getParent(), FINDING_CONTEXT, Concepts.IS_A, Concepts.MODULE_ROOT, CharacteristicType.INFERRED_RELATIONSHIP);
		
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
		
		String parentConceptId = createNewConcept(branchPath, Concepts.ROOT_CONCEPT); // parent concept

		SnomedConcept parentConcept = SnomedComponentRestRequests
				.getComponent(branchPath, SnomedComponentType.CONCEPT, parentConceptId, "relationships()")
				.statusCode(200)
				.extract()
				.as(SnomedConcept.class);

		String parentStatedIsaId = parentConcept.getRelationships().getItems().stream()
				.filter(r -> r.getCharacteristicType() == CharacteristicType.STATED_RELATIONSHIP).findFirst().get().getId();

		String parentInferredIsaId = createNewRelationship(branchPath, parentConceptId, Concepts.IS_A, Concepts.ROOT_CONCEPT,
				CharacteristicType.INFERRED_RELATIONSHIP);

		String childConceptId = createNewConcept(branchPath, parentConceptId); // child concept
		createNewRelationship(branchPath, childConceptId, Concepts.IS_A, parentConceptId,
				CharacteristicType.INFERRED_RELATIONSHIP);

		final IBranchPath taskBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranchRecursively(taskBranch);

		// reparent parent concept so ancestors of child must be recalculated

		createNewRelationship(branchPath, parentConceptId, Concepts.IS_A, FINDING_CONTEXT, CharacteristicType.STATED_RELATIONSHIP);
		createNewRelationship(branchPath, parentConceptId, Concepts.IS_A, FINDING_CONTEXT,
				CharacteristicType.INFERRED_RELATIONSHIP);

		inactivateRelationship(branchPath, parentStatedIsaId);
		inactivateRelationship(branchPath, parentInferredIsaId);

		// change description of child concept on task branch

		SnomedConcept childConcept = SnomedComponentRestRequests
				.getComponent(taskBranch, SnomedComponentType.CONCEPT, childConceptId, "descriptions()")
				.statusCode(200)
				.extract()
				.as(SnomedConcept.class);

		String childSynonymId = childConcept.getDescriptions().getItems().stream().filter(d -> d.getTypeId().equals(Concepts.SYNONYM)).findFirst()
				.get().getId();

		inactivateDescription(taskBranch, childSynonymId); // inactivate old pt
		createNewDescription(taskBranch, childConceptId, Concepts.SYNONYM, SnomedApiTestConstants.UK_PREFERRED_MAP); // add new pt

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
		
		String conceptId = createNewConcept(branchPath, Concepts.ROOT_CONCEPT); // parent concept

		SnomedConcept concept = SnomedComponentRestRequests
				.getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, "descriptions()")
				.statusCode(200)
				.extract()
				.as(SnomedConcept.class);

		String fsnId = concept.getDescriptions().getItems().stream()
				.filter(r -> r.getTypeId().equals(Concepts.FULLY_SPECIFIED_NAME)).findFirst().get().getId();

		final IBranchPath taskBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranchRecursively(taskBranch);

		// change case significance on parent branch
		changeCaseSignificance(branchPath, fsnId, CaseSignificance.ENTIRE_TERM_CASE_SENSITIVE);

		// add new fsn on task branch
		inactivateDescription(taskBranch, fsnId);
		createNewDescription(taskBranch, conceptId, Concepts.FULLY_SPECIFIED_NAME, SnomedApiTestConstants.UK_PREFERRED_MAP);
		
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
		
		String conceptId = createNewConcept(branchPath, Concepts.ROOT_CONCEPT);

		SnomedConcept concept = SnomedComponentRestRequests
				.getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, "descriptions()")
				.statusCode(200)
				.extract()
				.as(SnomedConcept.class);

		String fsnId = concept.getDescriptions().getItems().stream()
				.filter(r -> r.getTypeId().equals(Concepts.FULLY_SPECIFIED_NAME)).findFirst().get().getId();

		final IBranchPath taskBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranchRecursively(taskBranch);

		// add new concept on parent branch
		createNewConcept(branchPath, Concepts.ROOT_CONCEPT);
		
		// add new fsn on task branch
		inactivateDescription(taskBranch, fsnId);
		createNewDescription(taskBranch, conceptId, Concepts.FULLY_SPECIFIED_NAME, SnomedApiTestConstants.UK_PREFERRED_MAP);
		
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
		String conceptId = createNewConcept(branchPath, Concepts.ROOT_CONCEPT); // parent concept

		// create task branch
		final IBranchPath taskBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranchRecursively(taskBranch);
		
		// add inferred relationship on parent branch
		createNewRelationship(branchPath, conceptId, Concepts.IS_A, Concepts.ROOT_CONCEPT, CharacteristicType.INFERRED_RELATIONSHIP);

		// add inferred relationship on task branch
		createNewRelationship(taskBranch, conceptId, Concepts.IS_A, FINDING_CONTEXT, CharacteristicType.INFERRED_RELATIONSHIP);

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
		
		String conceptId = createNewConcept(branchPath, Concepts.ROOT_CONCEPT);

		SnomedConcept concept = SnomedComponentRestRequests
				.getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, "descriptions()")
				.statusCode(200)
				.extract()
				.as(SnomedConcept.class);

		String ptId = concept.getDescriptions().getItems().stream().filter(r -> r.getTypeId().equals(Concepts.SYNONYM)).findFirst().get().getId();

		final IBranchPath taskBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranchRecursively(taskBranch);

		// add new relationship to concept on parent branch
		createNewRelationship(branchPath, conceptId, Concepts.IS_A, Concepts.ROOT_CONCEPT, CharacteristicType.INFERRED_RELATIONSHIP);
		
		// add new pt on task branch
		inactivateDescription(taskBranch, ptId);
		createNewDescription(taskBranch, conceptId, Concepts.SYNONYM, SnomedApiTestConstants.UK_PREFERRED_MAP);
		
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
		
		String conceptId = createNewConcept(branchPath, Concepts.ROOT_CONCEPT);

		SnomedConcept concept = SnomedComponentRestRequests
				.getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, "descriptions()")
				.statusCode(200)
				.extract()
				.as(SnomedConcept.class);

		String ptId = concept.getDescriptions().getItems().stream().filter(r -> r.getTypeId().equals(Concepts.SYNONYM)).findFirst().get().getId();

		final IBranchPath taskBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranchRecursively(taskBranch);

		// change definition status of concept on parent branch
		changeToDefining(branchPath, conceptId);
		
		// add new pt on task branch
		inactivateDescription(taskBranch, ptId);
		createNewDescription(taskBranch, conceptId, Concepts.SYNONYM, SnomedApiTestConstants.UK_PREFERRED_MAP);
		
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

		// Set up a review...
		String reviewId = reviewLocation(createMergeReview(branchPath.getParentPath(), branchPath.getPath()));
		assertReviewCurrent(reviewId);
		
		// ...then commit to the branch.
		createNewConcept(branchPath);
		// wait 1s before checking review state 
		Thread.sleep(1000);
		
		waitForMergeReviewJob(reviewId)
			.statusCode(200)
			.body("status", equalTo(ReviewStatus.STALE.toString()));		
	}
	
	@Test
	public void setReviewStaleAfterParentRebase() {
		
		// Create all branches down to MAIN/test/A
		IBranchPath nestedBranchPath =  BranchPathUtils.createPath(branchPath, "A");
		createBranchRecursively(nestedBranchPath);

		// Set up review for a rebase of MAIN/test/A
		final String reviewId = reviewLocation(createMergeReview(branchPath.getPath(), nestedBranchPath.getPath()));
		assertReviewCurrent(reviewId);

		// Rebase branchPath on MAIN
		SnomedMergingRestRequests.createMerge(nestedBranchPath, branchPath, "Rebasing branchPath on MAIN", reviewId);

		// Create a new concept on MAIN
		createNewConcept(nestedBranchPath);

		waitForMergeReviewJob(reviewId)
			.statusCode(200)
			.body("status", equalTo(ReviewStatus.STALE.toString()));		
	}
	
	@Test
	public void noDescriptionIdsInChangedConceptIdsWhenConceptChangedForMergeReview() {
		 
		// branchPath is the project branch
		
		IBranchPath firstTaskPath = BranchPathUtils.createPath(branchPath, "1");
		createBranch(firstTaskPath);
		
		String concept = createNewConcept(firstTaskPath);
		merge(firstTaskPath, firstTaskPath.getParent(), "Merging first task into project");
		SnomedComponentRestRequests.getComponent(branchPath, SnomedComponentType.CONCEPT, concept).statusCode(200);
		
		IBranchPath secondTaskPath = BranchPathUtils.createPath(branchPath, "2");
		createBranch(secondTaskPath);
		createNewDescription(secondTaskPath, concept, SYNONYM);
		
		IBranchPath thirdTaskPath = BranchPathUtils.createPath(branchPath, "3");
		createBranch(thirdTaskPath);
		createNewDescription(thirdTaskPath, concept, SYNONYM);
		
		merge(thirdTaskPath, thirdTaskPath.getParent(), "Merging new description to project");
		
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
		
		// create branches: project (branchPath), task-A, task-B
		
		IBranchPath task_A_path = BranchPathUtils.createPath(branchPath, "TASK-A");
		createBranch(task_A_path);
		
		IBranchPath task_B_path = BranchPathUtils.createPath(branchPath, "TASK-B");
		createBranch(task_B_path);
		
		// add new description to concept on task-A
		createNewDescription(task_A_path, FINDING_CONTEXT, SYNONYM);
		SnomedComponentRestRequests.getComponent(task_A_path, SnomedComponentType.CONCEPT, FINDING_CONTEXT, "descriptions()", "relationships()").statusCode(200)
			.body("active", equalTo(true))
			.body("descriptions.total", equalTo(3));

		// inactivate concept on task-B
		inactivateConcept(task_B_path, FINDING_CONTEXT);
		SnomedComponentRestRequests.getComponent(task_B_path, SnomedComponentType.CONCEPT, FINDING_CONTEXT, "descriptions()", "relationships()").statusCode(200)
			.body("active", equalTo(false))
			.body("descriptions.total", equalTo(2))
			.body("descriptions.items[0].active", equalTo(true))
			.body("descriptions.items[1].active", equalTo(true));
		
		// promote task-A to parent project
		merge(task_A_path, branchPath, "promote task-A to parent project");
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

		// create branches: project (branchPath), task-A, task-B
		
		IBranchPath task_A_path = BranchPathUtils.createPath(branchPath, "TASK-A");
		createBranch(task_A_path);
		
		IBranchPath task_B_path = BranchPathUtils.createPath(branchPath, "TASK-B");
		createBranch(task_B_path);
		
		// add new description to concept on task-A
		createNewDescription(task_A_path, FINDING_CONTEXT, SYNONYM);
		SnomedComponentRestRequests.getComponent(task_A_path, SnomedComponentType.CONCEPT, FINDING_CONTEXT, "descriptions()", "relationships()").statusCode(200)
			.body("active", equalTo(true))
			.body("descriptions.total", equalTo(3));
		
		
		// inactivate concept on task-B
		inactivateConcept(task_B_path, FINDING_CONTEXT);
		SnomedComponentRestRequests.getComponent(task_B_path, SnomedComponentType.CONCEPT, FINDING_CONTEXT, "descriptions()", "relationships()").statusCode(200)
			.body("active", equalTo(false))
			.body("descriptions.total", equalTo(2))
			.body("descriptions.items[0].active", equalTo(true))
			.body("descriptions.items[1].active", equalTo(true));
		
		// promote task-A to parent project
		merge(task_A_path, branchPath, "promote task-A to parent project");
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
		
		// brancPath is the project branch
		
		IBranchPath firstTaskPath = BranchPathUtils.createPath(branchPath, "1");
		createBranch(firstTaskPath);
		
		String concept = createNewConcept(firstTaskPath);
		merge(firstTaskPath, firstTaskPath.getParent(), "Merging first task into project");
		SnomedComponentRestRequests.getComponent(branchPath, SnomedComponentType.CONCEPT, concept).statusCode(200);
		
		IBranchPath secondTaskPath = BranchPathUtils.createPath(branchPath, "2");
		createBranch(secondTaskPath);
		
		createNewDescription(secondTaskPath, concept, SYNONYM);
		
		IBranchPath thirdTaskPath = BranchPathUtils.createPath(branchPath, "3");
		createBranch(thirdTaskPath);
		createNewDescription(thirdTaskPath, concept, SYNONYM);
		
		
		SnomedComponentRestRequests.deleteComponent(thirdTaskPath, SnomedComponentType.CONCEPT, concept, true);
		SnomedComponentRestRequests.getComponent(thirdTaskPath, SnomedComponentType.CONCEPT, concept).statusCode(404);
		
		merge(thirdTaskPath, thirdTaskPath.getParent(), "Merging concept deletion to project");
		
		SnomedComponentRestRequests.getComponent(branchPath, SnomedComponentType.CONCEPT, concept).statusCode(404);
		
		final String reviewId = reviewLocation(createMergeReview(branchPath.getPath(), secondTaskPath.getPath()));
		assertReviewCurrent(reviewId);
		
		JsonNode reviewDetails = getMergeReviewDetailsResponse(reviewId);
		
		assertTrue(reviewDetails.isArray());
		assertEquals(0, reviewDetails.size());
		
	}
	
	@Test
	public void noDescriptionIdsInChangedConceptIdsForAcceptedMergeReview() {
		
		// branchPath is the project branch
		
		IBranchPath firstTaskPath = BranchPathUtils.createPath(branchPath, "1");
		createBranch(firstTaskPath);
		
		String conceptId = createNewConcept(firstTaskPath);
		SnomedComponentRestRequests.getComponent(firstTaskPath, SnomedComponentType.CONCEPT, conceptId).statusCode(200);
		
		String descriptionId = createNewDescription(firstTaskPath, conceptId, SYNONYM);
		SnomedComponentRestRequests.getComponent(firstTaskPath, SnomedComponentType.DESCRIPTION, descriptionId).statusCode(200);
		
		final Map<Object, Object> descriptionUpdateBody = ImmutableMap.<Object, Object>builder()
				.put("acceptability", Collections.emptyMap())
				.put("descriptionId", descriptionId)
				.put("commitComment", "Update description")
				.build();
		
		SnomedComponentRestRequests.updateComponent(firstTaskPath, SnomedComponentType.DESCRIPTION, descriptionId, descriptionUpdateBody);
		SnomedComponentRestRequests.getComponent(firstTaskPath, SnomedComponentType.DESCRIPTION, descriptionId).body("acceptability", equalTo(Collections.emptyMap()));
		
		merge(firstTaskPath, firstTaskPath.getParent(), "Merging first task into project");
		SnomedComponentRestRequests.getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId).statusCode(200);
		SnomedComponentRestRequests.getComponent(branchPath, SnomedComponentType.DESCRIPTION, descriptionId).statusCode(200);

		IBranchPath secondTaskPath = BranchPathUtils.createPath(branchPath, "2");
		createBranch(secondTaskPath);
		
		final Map<Object, Object> descriptionUpdateBodyUk = ImmutableMap.<Object, Object>builder()
				.put("acceptability", ACCEPTABLE_ACCEPTABILITY_MAP)
				.put("descriptionId", descriptionId)
				.put("commitComment", "Update description on second task")
				.build();
		
		SnomedComponentRestRequests.updateComponent(secondTaskPath, SnomedComponentType.DESCRIPTION, descriptionId, descriptionUpdateBodyUk);
		
		IBranchPath thirdTaskPath = BranchPathUtils.createPath(branchPath, "3");
		createBranch(thirdTaskPath);
		
		Map<String, Acceptability> usAcceptabilityMapPreferred = ImmutableMap.of(Concepts.REFSET_LANGUAGE_TYPE_US, Acceptability.PREFERRED);
		
		final Map<Object, Object> descriptionUpdateBodyUs = ImmutableMap.<Object, Object>builder()
				.put("acceptability", usAcceptabilityMapPreferred)
				.put("descriptionId", descriptionId)
				.put("commitComment", "Update description on third task")
				.build();
		
		SnomedComponentRestRequests.updateComponent(thirdTaskPath, SnomedComponentType.DESCRIPTION, descriptionId, descriptionUpdateBodyUs);
		
		merge(thirdTaskPath, thirdTaskPath.getParent(), "Merging third task to project");
		
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
		
		// branchPath is the project branch
		
		IBranchPath firstTaskPath = BranchPathUtils.createPath(branchPath, "1");
		createBranch(firstTaskPath);
		
		String conceptId = createNewConcept(firstTaskPath);
		SnomedComponentRestRequests.getComponent(firstTaskPath, SnomedComponentType.CONCEPT, conceptId).statusCode(200);
		
		final String relationshipId = createNewRelationship(firstTaskPath, conceptId, Concepts.IS_A, FINDING_CONTEXT, CharacteristicType.STATED_RELATIONSHIP);
		SnomedComponentRestRequests.getComponent(firstTaskPath, SnomedComponentType.RELATIONSHIP, relationshipId).statusCode(200);
		
		final String refSetId = createNewRefSet(firstTaskPath, SnomedRefSetType.SIMPLE);
		SnomedComponentRestRequests.getComponent(firstTaskPath, SnomedComponentType.REFSET, refSetId).statusCode(200);
		
		final String memberId = createNewRefSetMember(firstTaskPath, conceptId, refSetId);
		SnomedComponentRestRequests.getComponent(firstTaskPath, SnomedComponentType.MEMBER, memberId).statusCode(200);

		merge(firstTaskPath, firstTaskPath.getParent(), "Merging first task into project");
		SnomedComponentRestRequests.getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId).statusCode(200);
		
		IBranchPath secondTaskPath = BranchPathUtils.createPath(branchPath, "2");
		createBranch(secondTaskPath);

		SnomedComponentRestRequests.getComponent(secondTaskPath, SnomedComponentType.MEMBER, memberId, "referencedComponent()")
				.body("moduleId", CoreMatchers.equalTo(Concepts.MODULE_SCT_CORE));
		
		final Map<?, ?> moduleUpdate = ImmutableMap.builder()
				.put("moduleId", Concepts.MODULE_SCT_MODEL_COMPONENT)
				.put("commitComment", "Update member module: " + memberId)
				.build();

		updateRefSetComponent(secondTaskPath, SnomedComponentType.MEMBER, memberId, moduleUpdate, false).statusCode(204);
		
		SnomedComponentRestRequests.getComponent(secondTaskPath, SnomedComponentType.MEMBER, memberId, "referencedComponent()")
			.body("moduleId", CoreMatchers.equalTo(Concepts.MODULE_SCT_MODEL_COMPONENT));

		IBranchPath thirdTaskPath = BranchPathUtils.createPath(branchPath, "3");
		createBranch(thirdTaskPath);
		
		SnomedComponentRestRequests.getComponent(thirdTaskPath, SnomedComponentType.MEMBER, memberId, "referencedComponent()")
			.body("active", CoreMatchers.equalTo(true));
	
		final Map<?, ?> inactivationReq = ImmutableMap.builder()
				.put("active", false)
				.put("commitComment", "Inactivate member: " + memberId)
				.build();
		
		updateRefSetComponent(thirdTaskPath, SnomedComponentType.MEMBER, memberId, inactivationReq, false).statusCode(204);
		
		SnomedComponentRestRequests.getComponent(thirdTaskPath, SnomedComponentType.MEMBER, memberId, "referencedComponent()")
			.body("active", CoreMatchers.equalTo(false));
		
		merge(thirdTaskPath, thirdTaskPath.getParent(), "Merging third task to project");
		
		final String reviewId = reviewLocation(createMergeReview(branchPath.getPath(), secondTaskPath.getPath()));
		assertReviewCurrent(reviewId);
		
		JsonNode reviewDetails = getMergeReviewDetailsResponse(reviewId);
		
		assertTrue(reviewDetails.isArray());
		assertEquals(0, reviewDetails.size());
		
	}
	
	/**
	 * Scenario 1: Two different inactivation reasons, one has a historical association target, the other one does not
	 */
	@Test
	public void testDifferingInactivationReasonsWithOnlyOneHistoricalAssociation() {
		
		// Create concept
		final String conceptId = createNewConcept(branchPath);
		
		Map<?, ?> inactivationRequestBody = ImmutableMap.<String, Object>builder()
				.put("active", false)
				.put("inactivationIndicator", InactivationIndicator.AMBIGUOUS)
				.put("associationTargets", ImmutableMap.of(AssociationType.POSSIBLY_EQUIVALENT_TO, ImmutableList.of(Concepts.ROOT_CONCEPT)))
				.put("commitComment", "Inactivated concept")
				.build();

		updateComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, inactivationRequestBody).statusCode(204);
		
		getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, "inactivationProperties()").statusCode(200)
			.body("active", equalTo(false))
			.body("inactivationIndicator", equalTo(InactivationIndicator.AMBIGUOUS.toString()))
			.body("associationTargets." + AssociationType.POSSIBLY_EQUIVALENT_TO.name(), hasItem(Concepts.ROOT_CONCEPT));
		
		final IBranchPath taskBranch = BranchPathUtils.createPath(branchPath, "task");
		createBranch(taskBranch);
		
		// Change inactivation indicator to Duplicate on the project branch
		
		Map<?, ?> inactivationRequestBodyOnProject = ImmutableMap.<String, Object>builder()
				.put("inactivationIndicator", InactivationIndicator.DUPLICATE)
				.put("commitComment", "Update inactivation indicator")
				.build();
		
		updateComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, inactivationRequestBodyOnProject).statusCode(204);
		
		getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, "inactivationProperties()").statusCode(200)
			.body("inactivationIndicator", equalTo(InactivationIndicator.DUPLICATE.toString()));
		
		// Remove association targets from the task and change Indicator to Erroneous on the task
		
		Map<?, ?> inactivationRequestBodyOnTask = ImmutableMap.<String, Object>builder()
				.put("inactivationIndicator", InactivationIndicator.ERRONEOUS)
				.put("associationTargets", ImmutableMap.of(AssociationType.POSSIBLY_EQUIVALENT_TO, ImmutableList.of()))
				.put("commitComment", "Update inactivation indicator")
				.build();
		
		updateComponent(taskBranch, SnomedComponentType.CONCEPT, conceptId, inactivationRequestBodyOnTask).statusCode(204);
		
		getComponent(taskBranch, SnomedComponentType.CONCEPT, conceptId, "inactivationProperties()").statusCode(200)
			.body("inactivationIndicator", equalTo(InactivationIndicator.ERRONEOUS.toString()))
			.body("associationTargets." + AssociationType.POSSIBLY_EQUIVALENT_TO.name(), nullValue());
		
		final String reviewId = reviewLocation(createMergeReview(branchPath.getPath(), taskBranch.getPath()));
		assertReviewCurrent(reviewId);
		
		final JsonNode reviewDetails = getMergeReviewDetailsResponse(reviewId);
		
		assertTrue(reviewDetails.isArray());
		assertEquals(1, reviewDetails.size());		
	}
	
	/**
	 * Scenario 2: Two different inactivation reasons, but same target for historical association
	 */
	@Test
	public void testDifferingInactivationReasonsWithSameHistoricalAssociation() {
		
		// Create concept
		final String conceptId = createNewConcept(branchPath);
		
		Map<?, ?> inactivationRequestBody = ImmutableMap.<String, Object>builder()
				.put("active", false)
				.put("inactivationIndicator", InactivationIndicator.AMBIGUOUS)
				.put("associationTargets", ImmutableMap.of(AssociationType.POSSIBLY_EQUIVALENT_TO, ImmutableList.of(Concepts.ROOT_CONCEPT)))
				.put("commitComment", "Inactivated concept")
				.build();

		updateComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, inactivationRequestBody).statusCode(204);
		
		getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, "inactivationProperties()").statusCode(200)
			.body("active", equalTo(false))
			.body("inactivationIndicator", equalTo(InactivationIndicator.AMBIGUOUS.toString()))
			.body("associationTargets." + AssociationType.POSSIBLY_EQUIVALENT_TO.name(), hasItem(Concepts.ROOT_CONCEPT));
		
		final IBranchPath taskBranch = BranchPathUtils.createPath(branchPath, "task");
		createBranch(taskBranch);
		
		// Change inactivation indicator to Duplicate on the project branch
		
		Map<?, ?> inactivationRequestBodyOnProject = ImmutableMap.<String, Object>builder()
				.put("inactivationIndicator", InactivationIndicator.DUPLICATE)
				.put("commitComment", "Update inactivation indicator")
				.build();
		
		updateComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, inactivationRequestBodyOnProject).statusCode(204);
		
		getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, "inactivationProperties()").statusCode(200)
			.body("inactivationIndicator", equalTo(InactivationIndicator.DUPLICATE.toString()));
		
		// Change inactivation indicator to Erroneous on the task branch
		
		Map<?, ?> inactivationRequestBodyOnTask = ImmutableMap.<String, Object>builder()
				.put("inactivationIndicator", InactivationIndicator.ERRONEOUS)
				.put("commitComment", "Update inactivation indicator")
				.build();
		
		updateComponent(taskBranch, SnomedComponentType.CONCEPT, conceptId, inactivationRequestBodyOnTask).statusCode(204);
		
		getComponent(taskBranch, SnomedComponentType.CONCEPT, conceptId, "inactivationProperties()").statusCode(200)
			.body("inactivationIndicator", equalTo(InactivationIndicator.ERRONEOUS.toString()));
		
		final String reviewId = reviewLocation(createMergeReview(branchPath.getPath(), taskBranch.getPath()));
		assertReviewCurrent(reviewId);
		
		final JsonNode reviewDetails = getMergeReviewDetailsResponse(reviewId);

		assertTrue(reviewDetails.isArray());
		assertEquals(1, reviewDetails.size());		
	}
	
	/**
	 * Scenario 3: Same inactivation reason, but different targets for historical association 
	 */
	@Test
	public void testSameInactivationReasonWithDifferentHistoricalAssociationTargets() {
		
		// Create concept
		final String conceptId = createNewConcept(branchPath);
		
		Map<?, ?> inactivationRequestBody = ImmutableMap.<String, Object>builder()
				.put("active", false)
				.put("inactivationIndicator", InactivationIndicator.AMBIGUOUS)
				.put("associationTargets", ImmutableMap.of(AssociationType.POSSIBLY_EQUIVALENT_TO, ImmutableList.of(Concepts.ROOT_CONCEPT)))
				.put("commitComment", "Inactivated concept")
				.build();

		updateComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, inactivationRequestBody).statusCode(204);
		
		getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, "inactivationProperties()").statusCode(200)
			.body("active", equalTo(false))
			.body("inactivationIndicator", equalTo(InactivationIndicator.AMBIGUOUS.toString()))
			.body("associationTargets." + AssociationType.POSSIBLY_EQUIVALENT_TO.name(), hasItem(Concepts.ROOT_CONCEPT));
		
		final IBranchPath taskBranch = BranchPathUtils.createPath(branchPath, "task");
		createBranch(taskBranch);
		
		Map<?, ?> inactivationRequestBodyOnProject = ImmutableMap.<String, Object>builder()
				.put("associationTargets", ImmutableMap.of(AssociationType.POSSIBLY_EQUIVALENT_TO, ImmutableList.of(Concepts.MODULE_ROOT)))
				.put("commitComment", "Update association target")
				.build();
		
		updateComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, inactivationRequestBodyOnProject).statusCode(204);
		
		getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, "inactivationProperties()").statusCode(200)
			.body("associationTargets." + AssociationType.POSSIBLY_EQUIVALENT_TO.name(), hasItem(Concepts.MODULE_ROOT));
		
		Map<?, ?> inactivationRequestBodyOnTask = ImmutableMap.<String, Object>builder()
				.put("associationTargets", ImmutableMap.of(AssociationType.POSSIBLY_EQUIVALENT_TO, ImmutableList.of(Concepts.REFSET_ROOT_CONCEPT)))
				.put("commitComment", "Update association target")
				.build();
		
		updateComponent(taskBranch, SnomedComponentType.CONCEPT, conceptId, inactivationRequestBodyOnTask).statusCode(204);
		
		getComponent(taskBranch, SnomedComponentType.CONCEPT, conceptId, "inactivationProperties()").statusCode(200)
			.body("associationTargets." + AssociationType.POSSIBLY_EQUIVALENT_TO.name(), hasItem(Concepts.REFSET_ROOT_CONCEPT));
	
		final String reviewId = reviewLocation(createMergeReview(branchPath.getPath(), taskBranch.getPath()));
		assertReviewCurrent(reviewId);
		
		final JsonNode reviewDetails = getMergeReviewDetailsResponse(reviewId);
	
		assertTrue(reviewDetails.isArray());
		assertEquals(1, reviewDetails.size());		
		
	}
 	
}
