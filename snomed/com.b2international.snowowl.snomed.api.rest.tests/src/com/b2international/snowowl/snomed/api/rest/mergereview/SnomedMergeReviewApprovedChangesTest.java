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
package com.b2international.snowowl.snomed.api.rest.mergereview;

import static com.b2international.snowowl.snomed.api.rest.SnomedBranchingRestRequests.createBranch;
import static com.b2international.snowowl.snomed.api.rest.SnomedComponentRestRequests.createComponent;
import static com.b2international.snowowl.snomed.api.rest.SnomedComponentRestRequests.getComponent;
import static com.b2international.snowowl.snomed.api.rest.SnomedComponentRestRequests.updateComponent;
import static com.b2international.snowowl.snomed.api.rest.SnomedMergeReviewingRestRequests.createMergeReviewAndGetDetails;
import static com.b2international.snowowl.snomed.api.rest.SnomedRefSetRestRequests.updateRefSetComponent;
import static com.b2international.snowowl.snomed.api.rest.SnomedRestFixtures.createConceptRequestBody;
import static com.b2international.snowowl.snomed.api.rest.SnomedRestFixtures.createNewConcept;
import static com.b2international.snowowl.snomed.api.rest.SnomedRestFixtures.createNewDescription;
import static com.b2international.snowowl.snomed.api.rest.SnomedRestFixtures.createNewRelationship;
import static com.b2international.snowowl.snomed.api.rest.SnomedRestFixtures.createRefSetMemberRequestBody;
import static com.b2international.snowowl.test.commons.rest.RestExtensions.lastPathSegment;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.b2international.snowowl.core.api.IBranchPath;
import com.b2international.snowowl.datastore.BranchPathUtils;
import com.b2international.snowowl.snomed.SnomedConstants.Concepts;
import com.b2international.snowowl.snomed.api.mergereview.ISnomedBrowserMergeReviewDetail;
import com.b2international.snowowl.snomed.api.rest.AbstractSnomedApiTest;
import com.b2international.snowowl.snomed.api.rest.SnomedApiTestConstants;
import com.b2international.snowowl.snomed.api.rest.SnomedComponentType;
import com.b2international.snowowl.snomed.api.rest.domain.SnomedRefSetMemberRestInput;
import com.b2international.snowowl.snomed.common.SnomedRf2Headers;
import com.b2international.snowowl.snomed.core.domain.CharacteristicType;
import com.b2international.snowowl.snomed.core.domain.SnomedConcept;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * @since 6.15.1
 */
public class SnomedMergeReviewApprovedChangesTest extends AbstractSnomedApiTest {

	@Test
	public void mergeReviewAbsentForNewDescriptionVsChangedConceptProperty() {

		final String conceptId = createNewConcept(branchPath);

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		createNewDescription(childBranch, conceptId);

		final Map<?, ?> updateRequestBody = ImmutableMap.<String, Object>builder()
				.put("moduleId", Concepts.MODULE_ROOT)
				.put("commitComment", "Changed module of concept")
				.build();

		updateComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, updateRequestBody).statusCode(204);

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(childBranch, branchPath);

		assertTrue(results.isEmpty());

	}

	@Test
	public void mergeReviewAbsentForNewDescriptionVsChangedStatedRelationship() {

		final String conceptId = createNewConcept(branchPath);
		final String relationshipId = createNewRelationship(branchPath, conceptId, Concepts.PART_OF, Concepts.NAMESPACE_ROOT, CharacteristicType.STATED_RELATIONSHIP);

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		createNewDescription(childBranch, conceptId);

		final Map<?, ?> updateRequest = ImmutableMap.builder()
				.put(SnomedRf2Headers.FIELD_TYPE_ID, Concepts.DEFINING_RELATIONSHIP)
				.put("commitComment", "Updated relationship typeId")
				.build();

		updateComponent(branchPath, SnomedComponentType.RELATIONSHIP, relationshipId, updateRequest).statusCode(204);

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(childBranch, branchPath);

		assertTrue(results.isEmpty());

	}

	@Test
	public void mergeReviewAbsentForNewDescriptionVsChangedOwlAxiom() {

		final String owlSubclassOfExpression = String.format("SubClassOf(:%s :%s)", Concepts.FULLY_SPECIFIED_NAME, Concepts.AMBIGUOUS);

		final Map<?, ?> memberRequestBody = ImmutableMap.<String, Object>builder()
				.put("moduleId", Concepts.MODULE_SCT_CORE)
				.put("referenceSetId", Concepts.REFSET_OWL_AXIOM)
				.put(SnomedRefSetMemberRestInput.ADDITIONAL_FIELDS, ImmutableMap.<String, Object>builder()
						.put(SnomedRf2Headers.FIELD_OWL_EXPRESSION, owlSubclassOfExpression)
						.build()
						).build();

		final Map<?, ?> conceptRequestBody = createConceptRequestBody(Concepts.ROOT_CONCEPT)
				.put("members", ImmutableList.of(memberRequestBody))
				.put("commitComment", "Created concept with owl axiom reference set member")
				.build();

		final String conceptId = lastPathSegment(createComponent(branchPath, SnomedComponentType.CONCEPT, conceptRequestBody)
				.statusCode(201)
				.extract().header("Location"));

		final SnomedConcept concept = getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, "members()").extract().as(SnomedConcept.class);
		final String memberId = concept.getMembers().stream().filter(member -> member.getRefsetId().equals(Concepts.REFSET_OWL_AXIOM)).findFirst().get().getId();

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		createNewDescription(childBranch, conceptId);

		final String newOwlExpression = String.format("SubClassOf(:%s :%s)", Concepts.FULLY_SPECIFIED_NAME, Concepts.NAMESPACE_ROOT);

		final Map<?, ?> updateRequest = ImmutableMap.builder()
				.put(SnomedRefSetMemberRestInput.ADDITIONAL_FIELDS, ImmutableMap.<String, Object>builder()
						.putAll(ImmutableMap.<String, Object>builder()
								.put(SnomedRf2Headers.FIELD_OWL_EXPRESSION, newOwlExpression)
								.build())
						.build())
				.put("commitComment", "Updated reference set member")
				.build();

		updateRefSetComponent(branchPath, SnomedComponentType.MEMBER, memberId, updateRequest, false).statusCode(204);

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(childBranch, branchPath);

		assertTrue(results.isEmpty());

	}

	@Test
	public void mergeReviewAbsentForNewDescriptionVsChangedFsn() {

		final String conceptId = createNewConcept(branchPath);

		final SnomedConcept concept = getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, "fsn()").extract().as(SnomedConcept.class);
		final String fsnId = concept.getFsn().getId();

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		createNewDescription(childBranch, conceptId);

		final Map<?, ?> updateRequest = ImmutableMap.builder()
				.put(SnomedRf2Headers.FIELD_TERM, "Updated FSN term")
				.put("commitComment", "Update description term")
				.build();

		updateComponent(branchPath, SnomedComponentType.DESCRIPTION, fsnId, updateRequest).statusCode(204);

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(childBranch, branchPath);

		assertTrue(results.isEmpty());

	}

	@Test
	public void mergeReviewAbsentForNewDescriptionVsChangedPt() {

		final String conceptId = createNewConcept(branchPath);

		final SnomedConcept concept = getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, "pt()").extract().as(SnomedConcept.class);
		final String ptId = concept.getPt().getId();

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		createNewDescription(childBranch, conceptId);

		final Map<?, ?> updateRequest = ImmutableMap.builder()
				.put(SnomedRf2Headers.FIELD_TERM, "Updated PT term")
				.put("commitComment", "Update description term")
				.build();

		updateComponent(branchPath, SnomedComponentType.DESCRIPTION, ptId, updateRequest).statusCode(204);

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(childBranch, branchPath);

		assertTrue(results.isEmpty());

	}

	@Test
	public void mergeReviewAbsentForNewInferredRelationshipVsChangedConceptProperty() {

		final String conceptId = createNewConcept(branchPath);

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		createNewRelationship(childBranch, conceptId, Concepts.IS_A, Concepts.NAMESPACE_ROOT, CharacteristicType.INFERRED_RELATIONSHIP);

		final Map<?, ?> updateRequestBody = ImmutableMap.<String, Object>builder()
				.put("moduleId", Concepts.MODULE_ROOT)
				.put("commitComment", "Changed module of concept")
				.build();

		updateComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, updateRequestBody).statusCode(204);

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(childBranch, branchPath);

		assertTrue(results.isEmpty());

	}

	@Test
	public void mergeReviewAbsentForNewInferredRelationshipVsChangedStatedRelationship() {

		final String conceptId = createNewConcept(branchPath);
		final String relationshipId = createNewRelationship(branchPath, conceptId, Concepts.PART_OF, Concepts.NAMESPACE_ROOT, CharacteristicType.STATED_RELATIONSHIP);

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		createNewRelationship(childBranch, conceptId, Concepts.IS_A, Concepts.NAMESPACE_ROOT, CharacteristicType.INFERRED_RELATIONSHIP);

		final Map<?, ?> updateRequest = ImmutableMap.builder()
				.put(SnomedRf2Headers.FIELD_TYPE_ID, Concepts.DEFINING_RELATIONSHIP)
				.put("commitComment", "Updated relationship typeId")
				.build();

		updateComponent(branchPath, SnomedComponentType.RELATIONSHIP, relationshipId, updateRequest).statusCode(204);

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(childBranch, branchPath);

		assertTrue(results.isEmpty());

	}

	@Test
	public void mergeReviewAbsentForNewInferredRelationshipVsChangedOwlAxiom() {

		final String owlSubclassOfExpression = String.format("SubClassOf(:%s :%s)", Concepts.FULLY_SPECIFIED_NAME, Concepts.AMBIGUOUS);

		final Map<?, ?> memberRequestBody = ImmutableMap.<String, Object>builder()
				.put("moduleId", Concepts.MODULE_SCT_CORE)
				.put("referenceSetId", Concepts.REFSET_OWL_AXIOM)
				.put(SnomedRefSetMemberRestInput.ADDITIONAL_FIELDS, ImmutableMap.<String, Object>builder()
						.put(SnomedRf2Headers.FIELD_OWL_EXPRESSION, owlSubclassOfExpression)
						.build()
						).build();

		final Map<?, ?> conceptRequestBody = createConceptRequestBody(Concepts.ROOT_CONCEPT)
				.put("members", ImmutableList.of(memberRequestBody))
				.put("commitComment", "Created concept with owl axiom reference set member")
				.build();

		final String conceptId = lastPathSegment(createComponent(branchPath, SnomedComponentType.CONCEPT, conceptRequestBody)
				.statusCode(201)
				.extract().header("Location"));

		final SnomedConcept concept = getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, "members()").extract().as(SnomedConcept.class);
		final String memberId = concept.getMembers().stream().filter(member -> member.getRefsetId().equals(Concepts.REFSET_OWL_AXIOM)).findFirst().get().getId();

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		createNewRelationship(childBranch, conceptId, Concepts.IS_A, Concepts.NAMESPACE_ROOT, CharacteristicType.INFERRED_RELATIONSHIP);

		final String newOwlExpression = String.format("SubClassOf(:%s :%s)", Concepts.FULLY_SPECIFIED_NAME, Concepts.NAMESPACE_ROOT);

		final Map<?, ?> updateRequest = ImmutableMap.builder()
				.put(SnomedRefSetMemberRestInput.ADDITIONAL_FIELDS, ImmutableMap.<String, Object>builder()
						.putAll(ImmutableMap.<String, Object>builder()
								.put(SnomedRf2Headers.FIELD_OWL_EXPRESSION, newOwlExpression)
								.build())
						.build())
				.put("commitComment", "Updated reference set member")
				.build();

		updateRefSetComponent(branchPath, SnomedComponentType.MEMBER, memberId, updateRequest, false).statusCode(204);

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(childBranch, branchPath);

		assertTrue(results.isEmpty());

	}

	@Test
	public void mergeReviewAbsentForNewInferredRelationshipVsChangedFsn() {

		final String conceptId = createNewConcept(branchPath);

		final SnomedConcept concept = getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, "fsn()").extract().as(SnomedConcept.class);
		final String fsnId = concept.getFsn().getId();

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		createNewRelationship(childBranch, conceptId, Concepts.IS_A, Concepts.NAMESPACE_ROOT, CharacteristicType.INFERRED_RELATIONSHIP);

		final Map<?, ?> updateRequest = ImmutableMap.builder()
				.put(SnomedRf2Headers.FIELD_TERM, "Updated FSN term")
				.put("commitComment", "Update description term")
				.build();

		updateComponent(branchPath, SnomedComponentType.DESCRIPTION, fsnId, updateRequest).statusCode(204);

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(childBranch, branchPath);

		assertTrue(results.isEmpty());

	}

	@Test
	public void mergeReviewAbsentForNewInferredRelationshipVsChangedPt() {

		final String conceptId = createNewConcept(branchPath);

		final SnomedConcept concept = getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, "pt()").extract().as(SnomedConcept.class);
		final String ptId = concept.getPt().getId();

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		createNewRelationship(childBranch, conceptId, Concepts.IS_A, Concepts.NAMESPACE_ROOT, CharacteristicType.INFERRED_RELATIONSHIP);

		final Map<?, ?> updateRequest = ImmutableMap.builder()
				.put(SnomedRf2Headers.FIELD_TERM, "Updated PT term")
				.put("commitComment", "Update description term")
				.build();

		updateComponent(branchPath, SnomedComponentType.DESCRIPTION, ptId, updateRequest).statusCode(204);

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(childBranch, branchPath);

		assertTrue(results.isEmpty());

	}

	@Test
	public void mergeReviewAbsentForNewInferredRelationshipVsNewInferredRelationship() {

		final String conceptId = createNewConcept(branchPath);

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		createNewRelationship(childBranch, conceptId, Concepts.IS_A, Concepts.NAMESPACE_ROOT, CharacteristicType.INFERRED_RELATIONSHIP);
		createNewRelationship(branchPath, conceptId, Concepts.IS_A, Concepts.MODULE_ROOT, CharacteristicType.INFERRED_RELATIONSHIP);

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(childBranch, branchPath);

		assertTrue(results.isEmpty());

	}

	@Test
	public void mergeReviewAbsentForNewInferredRelationshipVsNewStatedRelationship() {

		final String conceptId = createNewConcept(branchPath);

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		createNewRelationship(childBranch, conceptId, Concepts.IS_A, Concepts.NAMESPACE_ROOT, CharacteristicType.INFERRED_RELATIONSHIP);
		createNewRelationship(branchPath, conceptId, Concepts.IS_A, Concepts.MODULE_ROOT, CharacteristicType.STATED_RELATIONSHIP);

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(childBranch, branchPath);

		assertTrue(results.isEmpty());

	}

	@Test
	public void mergeReviewAbsentForNewInferredRelationshipVsNewOwlAxiom() {

		final String conceptId = createNewConcept(branchPath);

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		createNewRelationship(childBranch, conceptId, Concepts.IS_A, Concepts.NAMESPACE_ROOT, CharacteristicType.INFERRED_RELATIONSHIP);

		final String owlSubclassOfExpression = String.format("SubClassOf(:%s :%s)", Concepts.FULLY_SPECIFIED_NAME, Concepts.AMBIGUOUS);

		final Map<?, ?> requestBody = createRefSetMemberRequestBody(Concepts.REFSET_OWL_AXIOM, conceptId)
				.put(SnomedRefSetMemberRestInput.ADDITIONAL_FIELDS, ImmutableMap.<String, Object>builder()
						.put(SnomedRf2Headers.FIELD_OWL_EXPRESSION, owlSubclassOfExpression)
						.build())
				.put("commitComment", "Created new OWL Axiom reference set member")
				.build();

		lastPathSegment(createComponent(branchPath, SnomedComponentType.MEMBER, requestBody)
				.statusCode(201)
				.extract().header("Location"));

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(childBranch, branchPath);

		assertTrue(results.isEmpty());

	}

	@Test
	public void mergeReviewAbsentForNewInferredRelationshipVsNewFsn() {

		final String conceptId = createNewConcept(branchPath);

		final SnomedConcept concept = getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, "fsn()").extract().as(SnomedConcept.class);
		final String originalFsnId = concept.getFsn().getId();

		final String descriptionId = createNewDescription(branchPath, conceptId, Concepts.FULLY_SPECIFIED_NAME, SnomedApiTestConstants.UK_ACCEPTABLE_MAP);

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		createNewRelationship(childBranch, conceptId, Concepts.IS_A, Concepts.NAMESPACE_ROOT, CharacteristicType.INFERRED_RELATIONSHIP);

		final Map<?, ?> originalRequestBody = ImmutableMap.builder()
				.put("acceptability", SnomedApiTestConstants.UK_ACCEPTABLE_MAP)
				.put("commitComment", "Updated description acceptability")
				.build();

		updateComponent(branchPath, SnomedComponentType.DESCRIPTION, originalFsnId, originalRequestBody).statusCode(204);

		final Map<?, ?> newRequestBody = ImmutableMap.builder()
				.put("acceptability", SnomedApiTestConstants.UK_PREFERRED_MAP)
				.put("commitComment", "Updated description acceptability")
				.build();

		updateComponent(branchPath, SnomedComponentType.DESCRIPTION, descriptionId, newRequestBody).statusCode(204);

		assertEquals(descriptionId, getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, "fsn()").extract().as(SnomedConcept.class).getFsn().getId());

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(childBranch, branchPath);

		assertTrue(results.isEmpty());

	}

	@Test
	public void mergeReviewAbsentForNewInferredRelationshipVsNewPt() {

		final String conceptId = createNewConcept(branchPath);

		final SnomedConcept concept = getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, "pt()").extract().as(SnomedConcept.class);
		final String originalPtId = concept.getPt().getId();

		final String descriptionId = createNewDescription(branchPath, conceptId, Concepts.SYNONYM, SnomedApiTestConstants.UK_ACCEPTABLE_MAP);

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		createNewRelationship(childBranch, conceptId, Concepts.IS_A, Concepts.NAMESPACE_ROOT, CharacteristicType.INFERRED_RELATIONSHIP);

		final Map<?, ?> originalRequestBody = ImmutableMap.builder()
				.put("acceptability", SnomedApiTestConstants.UK_ACCEPTABLE_MAP)
				.put("commitComment", "Updated description acceptability")
				.build();

		updateComponent(branchPath, SnomedComponentType.DESCRIPTION, originalPtId, originalRequestBody).statusCode(204);

		final Map<?, ?> newRequestBody = ImmutableMap.builder()
				.put("acceptability", SnomedApiTestConstants.UK_PREFERRED_MAP)
				.put("commitComment", "Updated description acceptability")
				.build();

		updateComponent(branchPath, SnomedComponentType.DESCRIPTION, descriptionId, newRequestBody).statusCode(204);

		assertEquals(descriptionId, getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, "pt()").extract().as(SnomedConcept.class).getPt().getId());

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(childBranch, branchPath);

		assertTrue(results.isEmpty());

	}
}
