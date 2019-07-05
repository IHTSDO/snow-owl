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
package com.b2international.snowowl.snomed.api.rest.mergereview;

import static com.b2international.snowowl.snomed.SnomedConstants.Concepts.SYNONYM;
import static com.b2international.snowowl.snomed.api.rest.SnomedBranchingRestRequests.createBranch;
import static com.b2international.snowowl.snomed.api.rest.SnomedComponentRestRequests.getComponent;
import static com.b2international.snowowl.snomed.api.rest.SnomedComponentRestRequests.updateComponent;
import static com.b2international.snowowl.snomed.api.rest.SnomedMergeReviewingRestRequests.assertReviewCurrent;
import static com.b2international.snowowl.snomed.api.rest.SnomedMergeReviewingRestRequests.createMergeReview;
import static com.b2international.snowowl.snomed.api.rest.SnomedMergeReviewingRestRequests.createMergeReviewAndGetDetails;
import static com.b2international.snowowl.snomed.api.rest.SnomedMergeReviewingRestRequests.getMergeReviewDetailsResponse;
import static com.b2international.snowowl.snomed.api.rest.SnomedMergeReviewingRestRequests.mergeAndApply;
import static com.b2international.snowowl.snomed.api.rest.SnomedMergeReviewingRestRequests.reviewLocation;
import static com.b2international.snowowl.snomed.api.rest.SnomedMergeReviewingRestRequests.storeConceptForMergeReview;
import static com.b2international.snowowl.snomed.api.rest.SnomedMergeReviewingRestRequests.waitForMergeReviewJob;
import static com.b2international.snowowl.snomed.api.rest.SnomedRefSetRestRequests.updateRefSetComponent;
import static com.b2international.snowowl.snomed.api.rest.SnomedRestFixtures.ACCEPTABLE_ACCEPTABILITY_MAP;
import static com.b2international.snowowl.snomed.api.rest.SnomedRestFixtures.createNewConcept;
import static com.b2international.snowowl.snomed.api.rest.SnomedRestFixtures.createNewDescription;
import static com.b2international.snowowl.snomed.api.rest.SnomedRestFixtures.createNewRefSet;
import static com.b2international.snowowl.snomed.api.rest.SnomedRestFixtures.createNewRefSetMember;
import static com.b2international.snowowl.snomed.api.rest.SnomedRestFixtures.createNewRelationship;
import static com.b2international.snowowl.snomed.api.rest.SnomedRestFixtures.inactivateConcept;
import static com.b2international.snowowl.snomed.api.rest.SnomedRestFixtures.inactivateDescription;
import static com.b2international.snowowl.snomed.api.rest.SnomedRestFixtures.inactivateRelationship;
import static com.b2international.snowowl.snomed.api.rest.SnomedRestFixtures.merge;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.b2international.snowowl.core.api.IBranchPath;
import com.b2international.snowowl.core.merge.Merge;
import com.b2international.snowowl.datastore.BranchPathUtils;
import com.b2international.snowowl.datastore.review.ReviewStatus;
import com.b2international.snowowl.snomed.SnomedConstants.Concepts;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserConcept;
import com.b2international.snowowl.snomed.api.impl.domain.browser.SnomedBrowserConcept;
import com.b2international.snowowl.snomed.api.mergereview.ISnomedBrowserMergeReviewDetail;
import com.b2international.snowowl.snomed.api.rest.AbstractSnomedApiTest;
import com.b2international.snowowl.snomed.api.rest.SnomedApiTestConstants;
import com.b2international.snowowl.snomed.api.rest.SnomedComponentType;
import com.b2international.snowowl.snomed.core.domain.Acceptability;
import com.b2international.snowowl.snomed.core.domain.AssociationType;
import com.b2international.snowowl.snomed.core.domain.CharacteristicType;
import com.b2international.snowowl.snomed.core.domain.DefinitionStatus;
import com.b2international.snowowl.snomed.core.domain.InactivationIndicator;
import com.b2international.snowowl.snomed.core.domain.SnomedConcept;
import com.b2international.snowowl.snomed.snomedrefset.SnomedRefSetType;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * @since 4.6
 */
public class SnomedMergeReviewApiTest extends AbstractSnomedApiTest {

	private static final String FINDING_CONTEXT = "408729009";

	@Test
	public void createMergeReviewWithEmptyFields() {
		createMergeReview("", "")
		.statusCode(400)
		.body("message", equalTo("2 validation errors"))
		.body("violations", hasItem("'source' may not be empty (was '')"))
		.body("violations", hasItem("'target' may not be empty (was '')"));
	}

	@Test
	public void createMergeReviewNonExistentBranch() {
		createMergeReview(branchPath.getPath(), "123").statusCode(400);
	}

	@Test
	public void createEmptyMergeReview() {
		assertTrue(createMergeReviewAndGetDetails(branchPath.getParent(), branchPath).isEmpty());
	}

	@Test
	public void setReviewStale() throws Exception {

		final IBranchPath childBranch =  BranchPathUtils.createPath(branchPath, "A");
		createBranch(childBranch);

		final String reviewId = reviewLocation(createMergeReview(branchPath, childBranch));
		assertReviewCurrent(reviewId);

		createNewConcept(childBranch);

		// wait 1s before checking review state
		Thread.sleep(1000);

		waitForMergeReviewJob(reviewId)
		.statusCode(200)
		.body("status", equalTo(ReviewStatus.STALE.toString()));
	}

	@Test
	public void setReviewStaleAfterParentRebase() throws Exception {

		final IBranchPath a = BranchPathUtils.createPath(branchPath, "a");
		createBranch(a);

		final IBranchPath b = BranchPathUtils.createPath(a, "b");
		createBranch(b);

		createNewConcept(branchPath);

		final String reviewId = reviewLocation(createMergeReview(a, b));
		assertReviewCurrent(reviewId);

		merge(branchPath, a, "Rebased child branch over new concept").body("status", equalTo(Merge.Status.COMPLETED.name()));

		// wait 1s before checking review state
		Thread.sleep(1000);

		waitForMergeReviewJob(reviewId)
		.statusCode(200)
		.body("status", equalTo(ReviewStatus.STALE.toString()));
	}

	@Test
	public void mergeReviewAbsentForChangedPtVsReparentedConcept() {

		// parent concept
		final String parentConceptId = createNewConcept(branchPath, Concepts.ROOT_CONCEPT);

		final SnomedConcept parentConcept = getComponent(branchPath, SnomedComponentType.CONCEPT, parentConceptId, "relationships()").extract().as(SnomedConcept.class);

		final String parentStatedIsaId = parentConcept.getRelationships().getItems().stream()
				.filter(r -> r.getCharacteristicTypeId().equals(Concepts.STATED_RELATIONSHIP)).findFirst().get().getId();

		final String parentInferredIsaId = createNewRelationship(branchPath, parentConceptId, Concepts.IS_A, Concepts.ROOT_CONCEPT, CharacteristicType.INFERRED_RELATIONSHIP);

		// child concept
		final String childConceptId = createNewConcept(branchPath, parentConceptId);
		createNewRelationship(branchPath, childConceptId, Concepts.IS_A, parentConceptId, CharacteristicType.INFERRED_RELATIONSHIP);

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		// re-parent parent concept so ancestors of child must be recalculated

		createNewRelationship(branchPath, parentConceptId, Concepts.IS_A, FINDING_CONTEXT, CharacteristicType.STATED_RELATIONSHIP);
		createNewRelationship(branchPath, parentConceptId, Concepts.IS_A, FINDING_CONTEXT, CharacteristicType.INFERRED_RELATIONSHIP);

		inactivateRelationship(branchPath, parentStatedIsaId);
		inactivateRelationship(branchPath, parentInferredIsaId);

		// change description of child concept on task branch

		final SnomedConcept childConcept = getComponent(childBranch, SnomedComponentType.CONCEPT, childConceptId, "descriptions()").extract().as(SnomedConcept.class);

		final String childSynonymId = childConcept.getDescriptions().getItems().stream().filter(d -> d.getTypeId().equals(Concepts.SYNONYM)).findFirst().get().getId();

		inactivateDescription(childBranch, childSynonymId); // inactivate old pt
		createNewDescription(childBranch, childConceptId, Concepts.SYNONYM, SnomedApiTestConstants.UK_PREFERRED_MAP); // add new pt

		// now try to request a merge review for task rebase

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(branchPath, childBranch);

		// there should be no merge screen triggered in such scenarios
		assertTrue(results.isEmpty());

	}

	@Test
	public void noDescriptionIdsInChangedConceptIdsWhenConceptChangedForMergeReview() {

		final String conceptId = createNewConcept(branchPath);

		final IBranchPath firstTaskPath = BranchPathUtils.createPath(branchPath, "a");
		createBranch(firstTaskPath);

		createNewDescription(firstTaskPath, conceptId, SYNONYM);

		final IBranchPath secondTaskPath = BranchPathUtils.createPath(branchPath, "b");
		createBranch(secondTaskPath);

		createNewDescription(secondTaskPath, conceptId, SYNONYM);

		merge(secondTaskPath, secondTaskPath.getParent(), "Merging new description to project");

		final String reviewId = reviewLocation(createMergeReview(branchPath, firstTaskPath));
		assertReviewCurrent(reviewId);

		final Set<ISnomedBrowserMergeReviewDetail> results = getMergeReviewDetailsResponse(reviewId);

		assertEquals(1, results.size());

		final ISnomedBrowserConcept autoMergedConcept = results.stream().findFirst().get().getAutoMergedConcept();

		storeConceptForMergeReview(reviewId, conceptId, autoMergedConcept).statusCode(200);

		mergeAndApply(reviewId).statusCode(204);

	}

	@Test
	public void taskRebaseWithMergeReviewApplyAutomergedConcept() throws InterruptedException {

		final IBranchPath firstBranchPath = BranchPathUtils.createPath(branchPath, "a");
		createBranch(firstBranchPath);

		final IBranchPath secondBranchPath = BranchPathUtils.createPath(branchPath, "b");
		createBranch(secondBranchPath);

		// add new description to concept on first task
		createNewDescription(firstBranchPath, FINDING_CONTEXT, SYNONYM);

		getComponent(firstBranchPath, SnomedComponentType.CONCEPT, FINDING_CONTEXT, "descriptions()")
		.body("active", equalTo(true))
		.body("descriptions.total", equalTo(3));

		// inactivate concept on second task
		inactivateConcept(secondBranchPath, FINDING_CONTEXT);

		getComponent(secondBranchPath, SnomedComponentType.CONCEPT, FINDING_CONTEXT, "descriptions()")
		.body("active", equalTo(false))
		.body("descriptions.total", equalTo(2));

		// promote first task to parent branch
		merge(firstBranchPath, branchPath, "Promote first task to parent branch");

		getComponent(branchPath, SnomedComponentType.CONCEPT, FINDING_CONTEXT, "descriptions()")
		.body("active", equalTo(true))
		.body("descriptions.total", equalTo(3));

		// create merge review between second task and parent branch
		final String reviewId = reviewLocation(createMergeReview(branchPath, secondBranchPath));
		assertReviewCurrent(reviewId);

		final Set<ISnomedBrowserMergeReviewDetail> results = getMergeReviewDetailsResponse(reviewId);
		assertEquals(1, results.size());

		// select suggested (middle) review as correct
		final ISnomedBrowserConcept autoMergedConcept = results.stream().findFirst().get().getAutoMergedConcept();

		// store and apply proposed merged concept
		storeConceptForMergeReview(reviewId, FINDING_CONTEXT, autoMergedConcept).statusCode(200);
		mergeAndApply(reviewId).statusCode(204);

		// assert that after review the correct content is merged to second task
		getComponent(secondBranchPath, SnomedComponentType.CONCEPT, FINDING_CONTEXT, "descriptions()")
		.body("active", equalTo(false))
		.body("descriptions.total", equalTo(2));

	}


	@Test
	public void taskRebaseWithMergeReviewApplyManuallyMergedConcept() throws InterruptedException {

		final IBranchPath firstBranchPath = BranchPathUtils.createPath(branchPath, "a");
		createBranch(firstBranchPath);

		final IBranchPath secondBranchPath = BranchPathUtils.createPath(branchPath, "b");
		createBranch(secondBranchPath);

		// add new description to concept on first task
		createNewDescription(firstBranchPath, FINDING_CONTEXT, SYNONYM);

		getComponent(firstBranchPath, SnomedComponentType.CONCEPT, FINDING_CONTEXT, "descriptions()")
		.body("active", equalTo(true))
		.body("descriptions.total", equalTo(3));

		// inactivate concept on second task
		inactivateConcept(secondBranchPath, FINDING_CONTEXT);

		getComponent(secondBranchPath, SnomedComponentType.CONCEPT, FINDING_CONTEXT, "descriptions()")
		.body("active", equalTo(false))
		.body("descriptions.total", equalTo(2));

		// promote first task to parent project
		merge(firstBranchPath, branchPath, "Promote first task to parent branch");
		getComponent(branchPath, SnomedComponentType.CONCEPT, FINDING_CONTEXT, "descriptions()")
		.body("active", equalTo(true))
		.body("descriptions.total", equalTo(3));

		// create merge review between second task and parent branch
		final String reviewId = reviewLocation(createMergeReview(branchPath, secondBranchPath));
		assertReviewCurrent(reviewId);

		final Set<ISnomedBrowserMergeReviewDetail> results = getMergeReviewDetailsResponse(reviewId);
		assertEquals(1, results.size());

		// select the project version of the concept as correct and modify it a bit
		final SnomedBrowserConcept sourceConcept = (SnomedBrowserConcept) results.stream().findFirst().get().getSourceConcept();
		sourceConcept.setDefinitionStatus(DefinitionStatus.FULLY_DEFINED);

		// store and apply merged concept
		storeConceptForMergeReview(reviewId, FINDING_CONTEXT, sourceConcept).statusCode(200);
		mergeAndApply(reviewId).statusCode(204);

		// assert that after review the correct/merged content is present on task-B
		getComponent(secondBranchPath, SnomedComponentType.CONCEPT, FINDING_CONTEXT, "descriptions()")
		.body("active", equalTo(true))
		.body("descriptions.total", equalTo(3))
		.body("definitionStatus", equalTo(DefinitionStatus.FULLY_DEFINED.name()));

	}

	@Test
	public void noDescriptionIdsInChangedConceptIdsForAcceptedMergeReview() {

		final IBranchPath firstTaskPath = BranchPathUtils.createPath(branchPath, "1");
		createBranch(firstTaskPath);

		final String conceptId = createNewConcept(firstTaskPath);
		getComponent(firstTaskPath, SnomedComponentType.CONCEPT, conceptId).statusCode(200);

		final String descriptionId = createNewDescription(firstTaskPath, conceptId, SYNONYM);
		getComponent(firstTaskPath, SnomedComponentType.DESCRIPTION, descriptionId).statusCode(200);

		final Map<Object, Object> descriptionUpdateBody = ImmutableMap.<Object, Object>builder()
				.put("acceptability", Collections.emptyMap())
				.put("descriptionId", descriptionId)
				.put("commitComment", "Update description")
				.build();

		updateComponent(firstTaskPath, SnomedComponentType.DESCRIPTION, descriptionId, descriptionUpdateBody);
		getComponent(firstTaskPath, SnomedComponentType.DESCRIPTION, descriptionId).body("acceptability", equalTo(Collections.emptyMap()));

		merge(firstTaskPath, firstTaskPath.getParent(), "Merging first task into project");

		getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId).statusCode(200);
		getComponent(branchPath, SnomedComponentType.DESCRIPTION, descriptionId).statusCode(200);

		final IBranchPath secondTaskPath = BranchPathUtils.createPath(branchPath, "2");
		createBranch(secondTaskPath);

		final Map<Object, Object> descriptionUpdateRequestUk = ImmutableMap.<Object, Object>builder()
				.put("acceptability", ACCEPTABLE_ACCEPTABILITY_MAP)
				.put("descriptionId", descriptionId)
				.put("commitComment", "Update description on second task")
				.build();

		updateComponent(secondTaskPath, SnomedComponentType.DESCRIPTION, descriptionId, descriptionUpdateRequestUk);

		final IBranchPath thirdTaskPath = BranchPathUtils.createPath(branchPath, "3");
		createBranch(thirdTaskPath);

		final Map<String, Acceptability> usAcceptabilityMapPreferred = ImmutableMap.of(Concepts.REFSET_LANGUAGE_TYPE_US, Acceptability.PREFERRED);

		final Map<Object, Object> descriptionUpdateBodyUs = ImmutableMap.<Object, Object>builder()
				.put("acceptability", usAcceptabilityMapPreferred)
				.put("descriptionId", descriptionId)
				.put("commitComment", "Update description on third task")
				.build();

		updateComponent(thirdTaskPath, SnomedComponentType.DESCRIPTION, descriptionId, descriptionUpdateBodyUs);

		merge(thirdTaskPath, thirdTaskPath.getParent(), "Merging third task to project");

		final String reviewId = reviewLocation(createMergeReview(branchPath, secondTaskPath));
		assertReviewCurrent(reviewId);

		final Set<ISnomedBrowserMergeReviewDetail> results = getMergeReviewDetailsResponse(reviewId);

		assertEquals(1, results.size());

		final ISnomedBrowserConcept autoMergedConcept = results.stream().findFirst().get().getAutoMergedConcept();

		storeConceptForMergeReview(reviewId, conceptId, autoMergedConcept).statusCode(200);
		mergeAndApply(reviewId).statusCode(204);

	}

	@Test
	public void noRelationshipIdsInChangedConceptIdsForMergeReview() {

		final IBranchPath firstTaskPath = BranchPathUtils.createPath(branchPath, "1");
		createBranch(firstTaskPath);

		final String conceptId = createNewConcept(firstTaskPath);
		getComponent(firstTaskPath, SnomedComponentType.CONCEPT, conceptId).statusCode(200);

		final String relationshipId = createNewRelationship(firstTaskPath, conceptId, Concepts.IS_A, FINDING_CONTEXT, CharacteristicType.STATED_RELATIONSHIP);
		getComponent(firstTaskPath, SnomedComponentType.RELATIONSHIP, relationshipId).statusCode(200);

		final String refSetId = createNewRefSet(firstTaskPath, SnomedRefSetType.SIMPLE);
		getComponent(firstTaskPath, SnomedComponentType.REFSET, refSetId).statusCode(200);

		final String memberId = createNewRefSetMember(firstTaskPath, conceptId, refSetId);
		getComponent(firstTaskPath, SnomedComponentType.MEMBER, memberId).statusCode(200);

		merge(firstTaskPath, firstTaskPath.getParent(), "Merging first task into project");
		getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId).statusCode(200);

		final IBranchPath secondTaskPath = BranchPathUtils.createPath(branchPath, "2");
		createBranch(secondTaskPath);

		getComponent(secondTaskPath, SnomedComponentType.MEMBER, memberId, "referencedComponent()")
		.body("moduleId", equalTo(Concepts.MODULE_SCT_CORE));

		final Map<?, ?> moduleUpdate = ImmutableMap.builder()
				.put("moduleId", Concepts.MODULE_SCT_MODEL_COMPONENT)
				.put("commitComment", "Update member module: " + memberId)
				.build();

		updateRefSetComponent(secondTaskPath, SnomedComponentType.MEMBER, memberId, moduleUpdate, false).statusCode(204);

		getComponent(secondTaskPath, SnomedComponentType.MEMBER, memberId, "referencedComponent()")
		.body("moduleId", equalTo(Concepts.MODULE_SCT_MODEL_COMPONENT));

		final IBranchPath thirdTaskPath = BranchPathUtils.createPath(branchPath, "3");
		createBranch(thirdTaskPath);

		getComponent(thirdTaskPath, SnomedComponentType.MEMBER, memberId, "referencedComponent()")
		.body("active", equalTo(true));

		final Map<?, ?> inactivationReq = ImmutableMap.builder()
				.put("active", false)
				.put("commitComment", "Inactivate member: " + memberId)
				.build();

		updateRefSetComponent(thirdTaskPath, SnomedComponentType.MEMBER, memberId, inactivationReq, false).statusCode(204);

		getComponent(thirdTaskPath, SnomedComponentType.MEMBER, memberId, "referencedComponent()")
		.body("active", equalTo(false));

		merge(thirdTaskPath, thirdTaskPath.getParent(), "Merging third task to project");

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(branchPath, secondTaskPath);

		assertTrue(results.isEmpty());

	}

	/**
	 * Scenario 1: Two different inactivation reasons, one has a historical association target, the other one does not
	 */
	@Test
	public void testDifferingInactivationReasonsWithOnlyOneHistoricalAssociation() {

		// Create concept
		final String conceptId = createNewConcept(branchPath);

		final Map<?, ?> inactivationRequestBody = ImmutableMap.<String, Object>builder()
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

		final Map<?, ?> inactivationRequestBodyOnProject = ImmutableMap.<String, Object>builder()
				.put("inactivationIndicator", InactivationIndicator.DUPLICATE)
				.put("commitComment", "Update inactivation indicator")
				.build();

		updateComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, inactivationRequestBodyOnProject).statusCode(204);

		getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, "inactivationProperties()").statusCode(200)
		.body("inactivationIndicator", equalTo(InactivationIndicator.DUPLICATE.toString()));

		// Remove association targets from the task and change Indicator to Erroneous on the task

		final Map<?, ?> inactivationRequestBodyOnTask = ImmutableMap.<String, Object>builder()
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

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(branchPath, taskBranch);
		assertEquals(1, results.size());

	}

	/**
	 * Scenario 2: Two different inactivation reasons, but same target for historical association
	 */
	@Test
	public void testDifferingInactivationReasonsWithSameHistoricalAssociation() {

		// Create concept
		final String conceptId = createNewConcept(branchPath);

		final Map<?, ?> inactivationRequestBody = ImmutableMap.<String, Object>builder()
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

		final Map<?, ?> inactivationRequestBodyOnProject = ImmutableMap.<String, Object>builder()
				.put("inactivationIndicator", InactivationIndicator.DUPLICATE)
				.put("commitComment", "Update inactivation indicator")
				.build();

		updateComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, inactivationRequestBodyOnProject).statusCode(204);

		getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, "inactivationProperties()").statusCode(200)
		.body("inactivationIndicator", equalTo(InactivationIndicator.DUPLICATE.toString()));

		// Change inactivation indicator to Erroneous on the task branch

		final Map<?, ?> inactivationRequestBodyOnTask = ImmutableMap.<String, Object>builder()
				.put("inactivationIndicator", InactivationIndicator.ERRONEOUS)
				.put("commitComment", "Update inactivation indicator")
				.build();

		updateComponent(taskBranch, SnomedComponentType.CONCEPT, conceptId, inactivationRequestBodyOnTask).statusCode(204);

		getComponent(taskBranch, SnomedComponentType.CONCEPT, conceptId, "inactivationProperties()").statusCode(200)
		.body("inactivationIndicator", equalTo(InactivationIndicator.ERRONEOUS.toString()));

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(branchPath, taskBranch);
		assertEquals(1, results.size());
	}

	/**
	 * Scenario 3: Same inactivation reason, but different targets for historical association
	 */
	@Test
	public void testSameInactivationReasonWithDifferentHistoricalAssociationTargets() {

		// Create concept
		final String conceptId = createNewConcept(branchPath);

		final Map<?, ?> inactivationRequestBody = ImmutableMap.<String, Object>builder()
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

		final Map<?, ?> inactivationRequestBodyOnProject = ImmutableMap.<String, Object>builder()
				.put("associationTargets", ImmutableMap.of(AssociationType.POSSIBLY_EQUIVALENT_TO, ImmutableList.of(Concepts.MODULE_ROOT)))
				.put("commitComment", "Update association target")
				.build();

		updateComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, inactivationRequestBodyOnProject).statusCode(204);

		getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, "inactivationProperties()").statusCode(200)
		.body("associationTargets." + AssociationType.POSSIBLY_EQUIVALENT_TO.name(), hasItem(Concepts.MODULE_ROOT));

		final Map<?, ?> inactivationRequestBodyOnTask = ImmutableMap.<String, Object>builder()
				.put("associationTargets", ImmutableMap.of(AssociationType.POSSIBLY_EQUIVALENT_TO, ImmutableList.of(Concepts.REFSET_ROOT_CONCEPT)))
				.put("commitComment", "Update association target")
				.build();

		updateComponent(taskBranch, SnomedComponentType.CONCEPT, conceptId, inactivationRequestBodyOnTask).statusCode(204);

		getComponent(taskBranch, SnomedComponentType.CONCEPT, conceptId, "inactivationProperties()").statusCode(200)
		.body("associationTargets." + AssociationType.POSSIBLY_EQUIVALENT_TO.name(), hasItem(Concepts.REFSET_ROOT_CONCEPT));

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(branchPath, taskBranch);
		assertEquals(1, results.size());

	}

}
