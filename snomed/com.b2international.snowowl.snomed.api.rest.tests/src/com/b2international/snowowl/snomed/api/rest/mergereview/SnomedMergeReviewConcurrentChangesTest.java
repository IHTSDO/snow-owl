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
import static com.b2international.snowowl.snomed.api.rest.SnomedBrowserRestRequests.createBrowserConceptRequest;
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
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;

import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.b2international.snowowl.core.api.IBranchPath;
import com.b2international.snowowl.datastore.BranchPathUtils;
import com.b2international.snowowl.snomed.SnomedConstants.Concepts;
import com.b2international.snowowl.snomed.api.mergereview.ISnomedBrowserMergeReviewDetail;
import com.b2international.snowowl.snomed.api.rest.AbstractSnomedApiTest;
import com.b2international.snowowl.snomed.api.rest.SnomedApiTestConstants;
import com.b2international.snowowl.snomed.api.rest.SnomedBrowserRestRequests;
import com.b2international.snowowl.snomed.api.rest.SnomedComponentType;
import com.b2international.snowowl.snomed.api.rest.SnomedIdentifierRestRequests;
import com.b2international.snowowl.snomed.api.rest.domain.SnomedRefSetMemberRestInput;
import com.b2international.snowowl.snomed.common.SnomedRf2Headers;
import com.b2international.snowowl.snomed.core.domain.AssociationType;
import com.b2international.snowowl.snomed.core.domain.CaseSignificance;
import com.b2international.snowowl.snomed.core.domain.CharacteristicType;
import com.b2international.snowowl.snomed.core.domain.InactivationIndicator;
import com.b2international.snowowl.snomed.core.domain.SnomedConcept;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

/**
 * @since 6.15.1
 */
public class SnomedMergeReviewConcurrentChangesTest extends AbstractSnomedApiTest {

	@Test
	public void mergeReviewPresentForSameNewConceptOnTwoDifferentTask() {

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		final String conceptId = SnomedIdentifierRestRequests.generateSctId(SnomedComponentType.CONCEPT, null).statusCode(201)
				.body("id", notNullValue())
				.extract().body()
				.path("id");

		final Map<String, Object> requestBody = Maps.newHashMap(createBrowserConceptRequest());
		requestBody.put("conceptId", conceptId);

		// create concept on child branch
		getComponent(childBranch, SnomedComponentType.CONCEPT, conceptId).statusCode(404);
		SnomedBrowserRestRequests.createBrowserConcept(childBranch, requestBody).statusCode(200);
		getComponent(childBranch, SnomedComponentType.CONCEPT, conceptId).statusCode(200);

		// create concept on parent branch
		getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId).statusCode(404);
		SnomedBrowserRestRequests.createBrowserConcept(branchPath, requestBody).statusCode(200);
		getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId).statusCode(200);

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(childBranch, branchPath);

		assertEquals(1, results.size());

	}

	@Test
	public void mergeReviewPresentForChangedConceptPropertyVsChangedConceptProperty() {

		final String conceptId = createNewConcept(branchPath);

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		final Map<?, ?> childUpdateRequestBody = ImmutableMap.<String, Object>builder()
				.put("moduleId", Concepts.MODULE_SCT_MODEL_COMPONENT)
				.put("commitComment", "Changed module of concept")
				.build();

		updateComponent(childBranch, SnomedComponentType.CONCEPT, conceptId, childUpdateRequestBody).statusCode(204);

		final Map<?, ?> updateRequestBody = ImmutableMap.<String, Object>builder()
				.put("moduleId", Concepts.MODULE_ROOT)
				.put("commitComment", "Changed module of concept")
				.build();

		updateComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, updateRequestBody).statusCode(204);

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(childBranch, branchPath);

		assertEquals(1, results.size());

	}

	@Test
	public void mergeReviewPresentForChangedDescriptionVsChangedDescription() {

		final String conceptId = createNewConcept(branchPath);
		final String descriptionId = createNewDescription(branchPath, conceptId);

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		final Map<?, ?> childUpdateRequestBody = ImmutableMap.builder()
				.put("caseSignificance", CaseSignificance.ENTIRE_TERM_CASE_SENSITIVE)
				.put("commitComment", "Updated description case significance")
				.build();

		updateComponent(childBranch, SnomedComponentType.DESCRIPTION, descriptionId, childUpdateRequestBody).statusCode(204);

		final Map<?, ?> updateRequestBody = ImmutableMap.builder()
				.put("caseSignificance", CaseSignificance.CASE_INSENSITIVE)
				.put("commitComment", "Updated description case significance")
				.build();

		updateComponent(branchPath, SnomedComponentType.DESCRIPTION, descriptionId, updateRequestBody).statusCode(204);

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(childBranch, branchPath);

		assertEquals(1, results.size());

	}

	@Test
	public void mergeReviewPresentForChangedStatedRelationshipVsChangedStatedRelationship() {

		final String conceptId = createNewConcept(branchPath);
		final String relationshipId = createNewRelationship(branchPath, conceptId, Concepts.PART_OF, Concepts.NAMESPACE_ROOT, CharacteristicType.STATED_RELATIONSHIP);

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		final Map<?, ?> childUpdateRequest = ImmutableMap.builder()
				.put(SnomedRf2Headers.FIELD_TYPE_ID, Concepts.IS_A)
				.put("commitComment", "Updated relationship typeId")
				.build();

		updateComponent(childBranch, SnomedComponentType.RELATIONSHIP, relationshipId, childUpdateRequest).statusCode(204);

		final Map<?, ?> updateRequest = ImmutableMap.builder()
				.put(SnomedRf2Headers.FIELD_TYPE_ID, Concepts.DEFINING_RELATIONSHIP)
				.put("commitComment", "Updated relationship typeId")
				.build();

		updateComponent(branchPath, SnomedComponentType.RELATIONSHIP, relationshipId, updateRequest).statusCode(204);

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(childBranch, branchPath);

		assertEquals(1, results.size());

	}

	@Test
	public void mergeReviewPresentForChangedOwlAxiomVsChangedOwlAxiom() {

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

		final String newChildOwlExpression = String.format("SubClassOf(:%s :%s)", Concepts.FULLY_SPECIFIED_NAME, Concepts.MODULE_ROOT);

		final Map<?, ?> childUpdateRequest = ImmutableMap.builder()
				.put(SnomedRefSetMemberRestInput.ADDITIONAL_FIELDS, ImmutableMap.<String, Object>builder()
						.putAll(ImmutableMap.<String, Object>builder()
								.put(SnomedRf2Headers.FIELD_OWL_EXPRESSION, newChildOwlExpression)
								.build())
						.build())
				.put("commitComment", "Updated reference set member")
				.build();

		updateRefSetComponent(childBranch, SnomedComponentType.MEMBER, memberId, childUpdateRequest, false).statusCode(204);

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

		assertEquals(1, results.size());

	}

	@Test
	public void mergeReviewPresentForChangedInactivationReasonVsChangedInactivationReason() {

		final String conceptId = createNewConcept(branchPath);

		final Map<?, ?> inactivationRequestBody = ImmutableMap.<String, Object>builder()
				.put("active", false)
				.put("inactivationIndicator", InactivationIndicator.AMBIGUOUS)
				.put("commitComment", "Inactivated concept")
				.build();

		updateComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, inactivationRequestBody).statusCode(204);

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		final Map<?, ?> inactivationRequestBodyOnTask = ImmutableMap.<String, Object>builder()
				.put("inactivationIndicator", InactivationIndicator.DUPLICATE)
				.put("commitComment", "Update inactivation indicator")
				.build();

		updateComponent(childBranch, SnomedComponentType.CONCEPT, conceptId, inactivationRequestBodyOnTask).statusCode(204);

		final Map<?, ?> updateRequest = ImmutableMap.<String, Object>builder()
				.put("inactivationIndicator", InactivationIndicator.ERRONEOUS)
				.put("commitComment", "Update inactivation indicator")
				.build();

		updateComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, updateRequest).statusCode(204);

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(childBranch, branchPath);

		assertEquals(1, results.size());

	}

	@Test
	public void mergeReviewPresentForChangedAssociationTargetVsChangedAssociationTarget() {

		final String conceptId = createNewConcept(branchPath);

		final Map<?, ?> inactivationRequestBody = ImmutableMap.<String, Object>builder()
				.put("active", false)
				.put("inactivationIndicator", InactivationIndicator.AMBIGUOUS)
				.put("associationTargets", ImmutableMap.of(AssociationType.POSSIBLY_EQUIVALENT_TO, ImmutableList.of(Concepts.ROOT_CONCEPT)))
				.put("commitComment", "Inactivated concept")
				.build();

		updateComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, inactivationRequestBody).statusCode(204);

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		final Map<?, ?> childUpdateRequest = ImmutableMap.<String, Object>builder()
				.put("associationTargets", ImmutableMap.of(AssociationType.POSSIBLY_EQUIVALENT_TO, ImmutableList.of(Concepts.MODULE_ROOT)))
				.put("commitComment", "Update association target")
				.build();

		updateComponent(childBranch, SnomedComponentType.CONCEPT, conceptId, childUpdateRequest).statusCode(204);

		final Map<?, ?> updateRequest = ImmutableMap.<String, Object>builder()
				.put("associationTargets", ImmutableMap.of(AssociationType.POSSIBLY_EQUIVALENT_TO, ImmutableList.of(Concepts.NAMESPACE_ROOT)))
				.put("commitComment", "Update association target")
				.build();

		updateComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, updateRequest).statusCode(204);

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(childBranch, branchPath);

		assertEquals(1, results.size());

	}

	@Test
	public void mergeReviewPresentForChangedAssociationTypeVsChangedAssociationType() {

		final String conceptId = createNewConcept(branchPath);

		final Map<?, ?> inactivationRequestBody = ImmutableMap.<String, Object>builder()
				.put("active", false)
				.put("inactivationIndicator", InactivationIndicator.AMBIGUOUS)
				.put("associationTargets", ImmutableMap.of(AssociationType.POSSIBLY_EQUIVALENT_TO, ImmutableList.of(Concepts.ROOT_CONCEPT)))
				.put("commitComment", "Inactivated concept")
				.build();

		updateComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, inactivationRequestBody).statusCode(204);

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		final Map<?, ?> childUpdateRequest = ImmutableMap.<String, Object>builder()
				.put("associationTargets", ImmutableMap.of(AssociationType.REPLACED_BY, ImmutableList.of(Concepts.ROOT_CONCEPT)))
				.put("commitComment", "Update association target")
				.build();

		updateComponent(childBranch, SnomedComponentType.CONCEPT, conceptId, childUpdateRequest).statusCode(204);

		final Map<?, ?> updateRequest = ImmutableMap.<String, Object>builder()
				.put("associationTargets", ImmutableMap.of(AssociationType.SAME_AS, ImmutableList.of(Concepts.ROOT_CONCEPT)))
				.put("commitComment", "Update association target")
				.build();

		updateComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, updateRequest).statusCode(204);

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(childBranch, branchPath);

		assertEquals(1, results.size());

	}

	@Test
	public void mergeReviewPresentForChangedConceptPropertyVsChangedStatedRelationship() {

		final String conceptId = createNewConcept(branchPath);
		final SnomedConcept concept = getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, "relationships()")
				.extract().body().as(SnomedConcept.class);

		final String relationshipId = concept.getRelationships().stream()
				.filter(relationship -> relationship.getCharacteristicTypeId().equals(Concepts.STATED_RELATIONSHIP))
				.findFirst()
				.get()
				.getId();

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		final Map<?, ?> childUpdateRequestBody = ImmutableMap.<String, Object>builder()
				.put("moduleId", Concepts.MODULE_SCT_MODEL_COMPONENT)
				.put("commitComment", "Changed module of concept")
				.build();

		updateComponent(childBranch, SnomedComponentType.CONCEPT, conceptId, childUpdateRequestBody).statusCode(204);

		final Map<?, ?> updateRequest = ImmutableMap.builder()
				.put("moduleId", Concepts.MODULE_SCT_MODEL_COMPONENT)
				.put("commitComment", "Updated relationship module id")
				.build();

		updateComponent(branchPath, SnomedComponentType.RELATIONSHIP, relationshipId, updateRequest).statusCode(204);

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(childBranch, branchPath);

		assertEquals(1, results.size());

	}

	@Test
	public void mergeReviewPresentForChangedConceptPropertyVsChangedOwlAxiom() {

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

		final Map<?, ?> childUpdateRequestBody = ImmutableMap.<String, Object>builder()
				.put("moduleId", Concepts.MODULE_SCT_MODEL_COMPONENT)
				.put("commitComment", "Changed module of concept")
				.build();

		updateComponent(childBranch, SnomedComponentType.CONCEPT, conceptId, childUpdateRequestBody).statusCode(204);

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

		assertEquals(1, results.size());

	}

	@Test
	public void mergeReviewPresentForChangedConceptPropertyVsChangedInactivationReason() {

		final String conceptId = createNewConcept(branchPath);

		final Map<?, ?> inactivationRequestBody = ImmutableMap.<String, Object>builder()
				.put("active", false)
				.put("inactivationIndicator", InactivationIndicator.AMBIGUOUS)
				.put("commitComment", "Inactivated concept")
				.build();

		updateComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, inactivationRequestBody).statusCode(204);

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		final Map<?, ?> inactivationRequestBodyOnTask = ImmutableMap.<String, Object>builder()
				.put("inactivationIndicator", InactivationIndicator.DUPLICATE)
				.put("commitComment", "Update inactivation indicator")
				.build();

		updateComponent(childBranch, SnomedComponentType.CONCEPT, conceptId, inactivationRequestBodyOnTask).statusCode(204);

		final Map<?, ?> updateRequestBody = ImmutableMap.<String, Object>builder()
				.put("moduleId", Concepts.MODULE_ROOT)
				.put("commitComment", "Changed module of concept")
				.build();

		updateComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, updateRequestBody).statusCode(204);

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(childBranch, branchPath);

		assertEquals(1, results.size());

	}

	@Test
	public void mergeReviewPresentForChangedConceptPropertyVsChangedAssociationTarget() {

		final String conceptId = createNewConcept(branchPath);

		final Map<?, ?> inactivationRequestBody = ImmutableMap.<String, Object>builder()
				.put("active", false)
				.put("inactivationIndicator", InactivationIndicator.AMBIGUOUS)
				.put("associationTargets", ImmutableMap.of(AssociationType.POSSIBLY_EQUIVALENT_TO, ImmutableList.of(Concepts.ROOT_CONCEPT)))
				.put("commitComment", "Inactivated concept")
				.build();

		updateComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, inactivationRequestBody).statusCode(204);

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		final Map<?, ?> childUpdateRequest = ImmutableMap.<String, Object>builder()
				.put("associationTargets", ImmutableMap.of(AssociationType.POSSIBLY_EQUIVALENT_TO, ImmutableList.of(Concepts.MODULE_ROOT)))
				.put("commitComment", "Update association target")
				.build();

		updateComponent(childBranch, SnomedComponentType.CONCEPT, conceptId, childUpdateRequest).statusCode(204);

		final Map<?, ?> updateRequestBody = ImmutableMap.<String, Object>builder()
				.put("moduleId", Concepts.MODULE_ROOT)
				.put("commitComment", "Changed module of concept")
				.build();

		updateComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, updateRequestBody).statusCode(204);

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(childBranch, branchPath);

		assertEquals(1, results.size());

	}

	@Test
	public void mergeReviewPresentForChangedConceptPropertyVsChangedFsn() {

		final String conceptId = createNewConcept(branchPath);

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		final String fsnId = getComponent(childBranch, SnomedComponentType.CONCEPT, conceptId, "fsn()").extract().as(SnomedConcept.class).getFsn().getId();

		final Map<?, ?> childUpdateRequest = ImmutableMap.builder()
				.put(SnomedRf2Headers.FIELD_TERM, "Updated FSN term")
				.put("commitComment", "Update description term")
				.build();

		updateComponent(childBranch, SnomedComponentType.DESCRIPTION, fsnId, childUpdateRequest).statusCode(204);

		final Map<?, ?> updateRequestBody = ImmutableMap.<String, Object>builder()
				.put("moduleId", Concepts.MODULE_ROOT)
				.put("commitComment", "Changed module of concept")
				.build();

		updateComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, updateRequestBody).statusCode(204);

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(childBranch, branchPath);

		assertEquals(1, results.size());

	}

	@Test
	public void mergeReviewPresentForChangedConceptPropertyVsChangedPt() {

		final String conceptId = createNewConcept(branchPath);

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		final String ptId = getComponent(childBranch, SnomedComponentType.CONCEPT, conceptId, "pt()").extract().as(SnomedConcept.class).getPt().getId();

		final Map<?, ?> childUpdateRequest = ImmutableMap.builder()
				.put(SnomedRf2Headers.FIELD_TERM, "Updated PT term")
				.put("commitComment", "Update description term")
				.build();

		updateComponent(childBranch, SnomedComponentType.DESCRIPTION, ptId, childUpdateRequest).statusCode(204);

		final Map<?, ?> updateRequestBody = ImmutableMap.<String, Object>builder()
				.put("moduleId", Concepts.MODULE_ROOT)
				.put("commitComment", "Changed module of concept")
				.build();

		updateComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, updateRequestBody).statusCode(204);

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(childBranch, branchPath);

		assertEquals(1, results.size());

	}

	@Test
	public void mergeReviewPresentForChangedStatedRelationshipVsChangedOwlAxiom() {

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
		final String relationshipId = createNewRelationship(branchPath, conceptId, Concepts.PART_OF, Concepts.NAMESPACE_ROOT, CharacteristicType.STATED_RELATIONSHIP);

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		final Map<?, ?> childUpdateRequest = ImmutableMap.builder()
				.put(SnomedRf2Headers.FIELD_TYPE_ID, Concepts.IS_A)
				.put("commitComment", "Updated relationship typeId")
				.build();

		updateComponent(childBranch, SnomedComponentType.RELATIONSHIP, relationshipId, childUpdateRequest).statusCode(204);

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

		assertEquals(1, results.size());

	}

	@Test
	public void mergeReviewPresentForChangedStatedRelationshipVsChangedFsn() {

		final String conceptId = createNewConcept(branchPath);
		final String relationshipId = createNewRelationship(branchPath, conceptId, Concepts.PART_OF, Concepts.NAMESPACE_ROOT, CharacteristicType.STATED_RELATIONSHIP);

		final String fsnId = getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, "fsn()").extract().as(SnomedConcept.class).getFsn().getId();

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		final Map<?, ?> childUpdateRequest = ImmutableMap.builder()
				.put(SnomedRf2Headers.FIELD_TYPE_ID, Concepts.IS_A)
				.put("commitComment", "Updated relationship typeId")
				.build();

		updateComponent(childBranch, SnomedComponentType.RELATIONSHIP, relationshipId, childUpdateRequest).statusCode(204);

		final Map<?, ?> updateRequest = ImmutableMap.builder()
				.put(SnomedRf2Headers.FIELD_TERM, "Updated FSN term")
				.put("commitComment", "Update description term")
				.build();

		updateComponent(branchPath, SnomedComponentType.DESCRIPTION, fsnId, updateRequest).statusCode(204);

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(childBranch, branchPath);

		assertEquals(1, results.size());

	}

	@Test
	public void mergeReviewPresentForChangedStatedRelationshipVsChangedPt() {

		final String conceptId = createNewConcept(branchPath);
		final String relationshipId = createNewRelationship(branchPath, conceptId, Concepts.PART_OF, Concepts.NAMESPACE_ROOT, CharacteristicType.STATED_RELATIONSHIP);

		final String ptId = getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, "pt()").extract().as(SnomedConcept.class).getPt().getId();

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		final Map<?, ?> childUpdateRequest = ImmutableMap.builder()
				.put(SnomedRf2Headers.FIELD_TYPE_ID, Concepts.IS_A)
				.put("commitComment", "Updated relationship typeId")
				.build();

		updateComponent(childBranch, SnomedComponentType.RELATIONSHIP, relationshipId, childUpdateRequest).statusCode(204);

		final Map<?, ?> updateRequest = ImmutableMap.builder()
				.put(SnomedRf2Headers.FIELD_TERM, "Updated PT term")
				.put("commitComment", "Update description term")
				.build();

		updateComponent(branchPath, SnomedComponentType.DESCRIPTION, ptId, updateRequest).statusCode(204);

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(childBranch, branchPath);

		assertEquals(1, results.size());

	}

	@Test
	public void mergeReviewPresentForChangedOwlAxiomVsChangedFsn() {

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

		final SnomedConcept concept = getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, "members(),fsn()").extract().as(SnomedConcept.class);
		final String memberId = concept.getMembers().stream().filter(member -> member.getRefsetId().equals(Concepts.REFSET_OWL_AXIOM)).findFirst().get().getId();
		final String fsnId = concept.getFsn().getId();

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		final String newChildOwlExpression = String.format("SubClassOf(:%s :%s)", Concepts.FULLY_SPECIFIED_NAME, Concepts.MODULE_ROOT);

		final Map<?, ?> childUpdateRequest = ImmutableMap.builder()
				.put(SnomedRefSetMemberRestInput.ADDITIONAL_FIELDS, ImmutableMap.<String, Object>builder()
						.putAll(ImmutableMap.<String, Object>builder()
								.put(SnomedRf2Headers.FIELD_OWL_EXPRESSION, newChildOwlExpression)
								.build())
						.build())
				.put("commitComment", "Updated reference set member")
				.build();

		updateRefSetComponent(childBranch, SnomedComponentType.MEMBER, memberId, childUpdateRequest, false).statusCode(204);

		final Map<?, ?> updateRequest = ImmutableMap.builder()
				.put(SnomedRf2Headers.FIELD_TERM, "Updated FSN term")
				.put("commitComment", "Update description term")
				.build();

		updateComponent(branchPath, SnomedComponentType.DESCRIPTION, fsnId, updateRequest).statusCode(204);

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(childBranch, branchPath);

		assertEquals(1, results.size());

	}

	@Test
	public void mergeReviewPresentForChangedOwlAxiomVsChangedPt() {

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

		final SnomedConcept concept = getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, "members(),pt()").extract().as(SnomedConcept.class);
		final String memberId = concept.getMembers().stream().filter(member -> member.getRefsetId().equals(Concepts.REFSET_OWL_AXIOM)).findFirst().get().getId();
		final String ptId = concept.getPt().getId();

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		final String newChildOwlExpression = String.format("SubClassOf(:%s :%s)", Concepts.FULLY_SPECIFIED_NAME, Concepts.MODULE_ROOT);

		final Map<?, ?> childUpdateRequest = ImmutableMap.builder()
				.put(SnomedRefSetMemberRestInput.ADDITIONAL_FIELDS, ImmutableMap.<String, Object>builder()
						.putAll(ImmutableMap.<String, Object>builder()
								.put(SnomedRf2Headers.FIELD_OWL_EXPRESSION, newChildOwlExpression)
								.build())
						.build())
				.put("commitComment", "Updated reference set member")
				.build();

		updateRefSetComponent(childBranch, SnomedComponentType.MEMBER, memberId, childUpdateRequest, false).statusCode(204);

		final Map<?, ?> updateRequest = ImmutableMap.builder()
				.put(SnomedRf2Headers.FIELD_TERM, "Updated PT term")
				.put("commitComment", "Update description term")
				.build();

		updateComponent(branchPath, SnomedComponentType.DESCRIPTION, ptId, updateRequest).statusCode(204);

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(childBranch, branchPath);

		assertEquals(1, results.size());

	}

	@Test
	public void mergeReviewPresentForChangedFsnVsChangedFsn() {

		final String conceptId = createNewConcept(branchPath);

		final SnomedConcept concept = getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, "fsn()").extract().as(SnomedConcept.class);
		final String fsnId = concept.getFsn().getId();

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		final Map<?, ?> childUpdateRequest = ImmutableMap.builder()
				.put(SnomedRf2Headers.FIELD_TERM, "Updated FSN term")
				.put("commitComment", "Update description term")
				.build();

		updateComponent(childBranch, SnomedComponentType.DESCRIPTION, fsnId, childUpdateRequest).statusCode(204);

		final Map<?, ?> updateRequest = ImmutableMap.builder()
				.put(SnomedRf2Headers.FIELD_TERM, "Updated FSN term 2")
				.put("commitComment", "Update description term")
				.build();

		updateComponent(branchPath, SnomedComponentType.DESCRIPTION, fsnId, updateRequest).statusCode(204);

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(childBranch, branchPath);

		assertEquals(1, results.size());

	}

	@Test
	public void mergeReviewPresentForChangedFsnVsChangedPt() {

		final String conceptId = createNewConcept(branchPath);

		final SnomedConcept concept = getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, "fsn(),pt()").extract().as(SnomedConcept.class);
		final String fsnId = concept.getFsn().getId();
		final String ptId = concept.getPt().getId();

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		final Map<?, ?> childUpdateRequest = ImmutableMap.builder()
				.put(SnomedRf2Headers.FIELD_TERM, "Updated FSN term")
				.put("commitComment", "Update description term")
				.build();

		updateComponent(childBranch, SnomedComponentType.DESCRIPTION, fsnId, childUpdateRequest).statusCode(204);

		final Map<?, ?> updateRequest = ImmutableMap.builder()
				.put(SnomedRf2Headers.FIELD_TERM, "Updated PT term")
				.put("commitComment", "Update description term")
				.build();

		updateComponent(branchPath, SnomedComponentType.DESCRIPTION, ptId, updateRequest).statusCode(204);

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(childBranch, branchPath);

		assertEquals(1, results.size());

	}

	@Test
	public void mergeReviewPresentForChangedPtVsChangedPt() {

		final String conceptId = createNewConcept(branchPath);

		final SnomedConcept concept = getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, "pt()").extract().as(SnomedConcept.class);
		final String ptId = concept.getPt().getId();

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		final Map<?, ?> childUpdateRequest = ImmutableMap.builder()
				.put(SnomedRf2Headers.FIELD_TERM, "Updated PT term")
				.put("commitComment", "Update description term")
				.build();

		updateComponent(childBranch, SnomedComponentType.DESCRIPTION, ptId, childUpdateRequest).statusCode(204);

		final Map<?, ?> updateRequest = ImmutableMap.builder()
				.put(SnomedRf2Headers.FIELD_TERM, "Updated PT term 2")
				.put("commitComment", "Update description term")
				.build();

		updateComponent(branchPath, SnomedComponentType.DESCRIPTION, ptId, updateRequest).statusCode(204);

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(childBranch, branchPath);

		assertEquals(1, results.size());

	}

	@Test
	public void mergeReviewPresentForNewStatedRelationshipVsNewStatedRelationship() {

		final String conceptId = createNewConcept(branchPath);

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		createNewRelationship(childBranch, conceptId, Concepts.PART_OF, Concepts.NAMESPACE_ROOT, CharacteristicType.STATED_RELATIONSHIP);

		createNewRelationship(branchPath, conceptId, Concepts.PART_OF, Concepts.MODULE_ROOT, CharacteristicType.STATED_RELATIONSHIP);

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(childBranch, branchPath);

		assertEquals(1, results.size());

	}

	@Test
	public void mergeReviewPresentForNewStatedRelationshipVsNewOwlAxiom() {

		final String conceptId = createNewConcept(branchPath);

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		createNewRelationship(childBranch, conceptId, Concepts.PART_OF, Concepts.NAMESPACE_ROOT, CharacteristicType.STATED_RELATIONSHIP);

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

		assertEquals(1, results.size());

	}

	@Test
	public void mergeReviewPresentForNewStatedRelationshipVsNewFsn() {

		final String conceptId = createNewConcept(branchPath);

		final SnomedConcept concept = getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, "fsn()").extract().as(SnomedConcept.class);
		final String originalFsnId = concept.getFsn().getId();

		final String descriptionId = createNewDescription(branchPath, conceptId, Concepts.FULLY_SPECIFIED_NAME, SnomedApiTestConstants.UK_ACCEPTABLE_MAP);

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		createNewRelationship(childBranch, conceptId, Concepts.PART_OF, Concepts.NAMESPACE_ROOT, CharacteristicType.STATED_RELATIONSHIP);

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

		assertEquals(1, results.size());

	}

	@Test
	public void mergeReviewPresentForNewStatedRelationshipVsNewPt() {

		final String conceptId = createNewConcept(branchPath);

		final SnomedConcept concept = getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, "pt()").extract().as(SnomedConcept.class);
		final String originalPtId = concept.getPt().getId();

		final String descriptionId = createNewDescription(branchPath, conceptId, Concepts.SYNONYM, SnomedApiTestConstants.UK_ACCEPTABLE_MAP);

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		createNewRelationship(childBranch, conceptId, Concepts.PART_OF, Concepts.NAMESPACE_ROOT, CharacteristicType.STATED_RELATIONSHIP);

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

		assertEquals(1, results.size());

	}

	@Test
	public void mergeReviewPresentForNewOwlAxiomVsNewOwlAxiom() {

		final String conceptId = createNewConcept(branchPath);

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		final String childOwlExpression = String.format("SubClassOf(:%s :%s)", Concepts.FULLY_SPECIFIED_NAME, Concepts.NAMESPACE_ROOT);

		final Map<?, ?> childRequestBody = createRefSetMemberRequestBody(Concepts.REFSET_OWL_AXIOM, conceptId)
				.put(SnomedRefSetMemberRestInput.ADDITIONAL_FIELDS, ImmutableMap.<String, Object>builder()
						.put(SnomedRf2Headers.FIELD_OWL_EXPRESSION, childOwlExpression)
						.build())
				.put("commitComment", "Created new OWL Axiom reference set member")
				.build();

		lastPathSegment(createComponent(childBranch, SnomedComponentType.MEMBER, childRequestBody)
				.statusCode(201)
				.extract().header("Location"));

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

		assertEquals(1, results.size());

	}

	@Test
	public void mergeReviewPresentForNewOwlAxiomVsNewFsn() {

		final String conceptId = createNewConcept(branchPath);

		final SnomedConcept concept = getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, "fsn()").extract().as(SnomedConcept.class);
		final String originalFsnId = concept.getFsn().getId();

		final String descriptionId = createNewDescription(branchPath, conceptId, Concepts.FULLY_SPECIFIED_NAME, SnomedApiTestConstants.UK_ACCEPTABLE_MAP);

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		final String childOwlExpression = String.format("SubClassOf(:%s :%s)", Concepts.FULLY_SPECIFIED_NAME, Concepts.NAMESPACE_ROOT);

		final Map<?, ?> childRequestBody = createRefSetMemberRequestBody(Concepts.REFSET_OWL_AXIOM, conceptId)
				.put(SnomedRefSetMemberRestInput.ADDITIONAL_FIELDS, ImmutableMap.<String, Object>builder()
						.put(SnomedRf2Headers.FIELD_OWL_EXPRESSION, childOwlExpression)
						.build())
				.put("commitComment", "Created new OWL Axiom reference set member")
				.build();

		lastPathSegment(createComponent(childBranch, SnomedComponentType.MEMBER, childRequestBody)
				.statusCode(201)
				.extract().header("Location"));

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

		assertEquals(1, results.size());

	}

	@Test
	public void mergeReviewPresentForNewOwlAxiomVsNewPt() {

		final String conceptId = createNewConcept(branchPath);

		final SnomedConcept concept = getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, "pt()").extract().as(SnomedConcept.class);
		final String originalPtId = concept.getPt().getId();

		final String descriptionId = createNewDescription(branchPath, conceptId, Concepts.SYNONYM, SnomedApiTestConstants.UK_ACCEPTABLE_MAP);

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		final String childOwlExpression = String.format("SubClassOf(:%s :%s)", Concepts.FULLY_SPECIFIED_NAME, Concepts.NAMESPACE_ROOT);

		final Map<?, ?> childRequestBody = createRefSetMemberRequestBody(Concepts.REFSET_OWL_AXIOM, conceptId)
				.put(SnomedRefSetMemberRestInput.ADDITIONAL_FIELDS, ImmutableMap.<String, Object>builder()
						.put(SnomedRf2Headers.FIELD_OWL_EXPRESSION, childOwlExpression)
						.build())
				.put("commitComment", "Created new OWL Axiom reference set member")
				.build();

		lastPathSegment(createComponent(childBranch, SnomedComponentType.MEMBER, childRequestBody)
				.statusCode(201)
				.extract().header("Location"));

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

		assertEquals(1, results.size());

	}

	@Test
	public void mergeReviewPresentForNewDescriptionVsNewDescription() {

		final String conceptId = createNewConcept(branchPath);

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		createNewDescription(childBranch, conceptId);
		createNewDescription(branchPath, conceptId);

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(childBranch, branchPath);

		assertEquals(1, results.size());

	}

	@Test
	public void mergeReviewPresentForNewFsnVsNewFsn() {

		final String conceptId = createNewConcept(branchPath);

		final SnomedConcept concept = getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, "fsn()").extract().as(SnomedConcept.class);
		final String originalFsnId = concept.getFsn().getId();

		final String newFsnId = createNewDescription(branchPath, conceptId, Concepts.FULLY_SPECIFIED_NAME, SnomedApiTestConstants.UK_ACCEPTABLE_MAP);
		final String newChildFsnId = createNewDescription(branchPath, conceptId, Concepts.FULLY_SPECIFIED_NAME, SnomedApiTestConstants.UK_ACCEPTABLE_MAP);

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		final Map<?, ?> originalChildRequestBody = ImmutableMap.builder()
				.put("acceptability", SnomedApiTestConstants.UK_ACCEPTABLE_MAP)
				.put("commitComment", "Updated description acceptability")
				.build();

		updateComponent(childBranch, SnomedComponentType.DESCRIPTION, originalFsnId, originalChildRequestBody).statusCode(204);

		final Map<?, ?> newChildRequestBody = ImmutableMap.builder()
				.put("acceptability", SnomedApiTestConstants.UK_PREFERRED_MAP)
				.put("commitComment", "Updated description acceptability")
				.build();

		updateComponent(childBranch, SnomedComponentType.DESCRIPTION, newChildFsnId, newChildRequestBody).statusCode(204);

		assertEquals(newChildFsnId, getComponent(childBranch, SnomedComponentType.CONCEPT, conceptId, "fsn()").extract().as(SnomedConcept.class).getFsn().getId());

		final Map<?, ?> originalRequestBody = ImmutableMap.builder()
				.put("acceptability", SnomedApiTestConstants.UK_ACCEPTABLE_MAP)
				.put("commitComment", "Updated description acceptability")
				.build();

		updateComponent(branchPath, SnomedComponentType.DESCRIPTION, originalFsnId, originalRequestBody).statusCode(204);

		final Map<?, ?> newRequestBody = ImmutableMap.builder()
				.put("acceptability", SnomedApiTestConstants.UK_PREFERRED_MAP)
				.put("commitComment", "Updated description acceptability")
				.build();

		updateComponent(branchPath, SnomedComponentType.DESCRIPTION, newFsnId, newRequestBody).statusCode(204);

		assertEquals(newFsnId, getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, "fsn()").extract().as(SnomedConcept.class).getFsn().getId());

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(childBranch, branchPath);

		assertEquals(1, results.size());

	}

	@Test
	public void mergeReviewPresentForNewPtVsNewPt() {

		final String conceptId = createNewConcept(branchPath);

		final SnomedConcept concept = getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, "pt()").extract().as(SnomedConcept.class);
		final String originalPtId = concept.getPt().getId();

		final String newPtId = createNewDescription(branchPath, conceptId, Concepts.SYNONYM, SnomedApiTestConstants.UK_ACCEPTABLE_MAP);
		final String newChildPtId = createNewDescription(branchPath, conceptId, Concepts.SYNONYM, SnomedApiTestConstants.UK_ACCEPTABLE_MAP);

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		final Map<?, ?> originalChildRequestBody = ImmutableMap.builder()
				.put("acceptability", SnomedApiTestConstants.UK_ACCEPTABLE_MAP)
				.put("commitComment", "Updated description acceptability")
				.build();

		updateComponent(childBranch, SnomedComponentType.DESCRIPTION, originalPtId, originalChildRequestBody).statusCode(204);

		final Map<?, ?> newChildRequestBody = ImmutableMap.builder()
				.put("acceptability", SnomedApiTestConstants.UK_PREFERRED_MAP)
				.put("commitComment", "Updated description acceptability")
				.build();

		updateComponent(childBranch, SnomedComponentType.DESCRIPTION, newChildPtId, newChildRequestBody).statusCode(204);

		assertEquals(newChildPtId, getComponent(childBranch, SnomedComponentType.CONCEPT, conceptId, "pt()").extract().as(SnomedConcept.class).getPt().getId());

		final Map<?, ?> originalRequestBody = ImmutableMap.builder()
				.put("acceptability", SnomedApiTestConstants.UK_ACCEPTABLE_MAP)
				.put("commitComment", "Updated description acceptability")
				.build();

		updateComponent(branchPath, SnomedComponentType.DESCRIPTION, originalPtId, originalRequestBody).statusCode(204);

		final Map<?, ?> newRequestBody = ImmutableMap.builder()
				.put("acceptability", SnomedApiTestConstants.UK_PREFERRED_MAP)
				.put("commitComment", "Updated description acceptability")
				.build();

		updateComponent(branchPath, SnomedComponentType.DESCRIPTION, newPtId, newRequestBody).statusCode(204);

		assertEquals(newPtId, getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, "pt()").extract().as(SnomedConcept.class).getPt().getId());

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(childBranch, branchPath);

		assertEquals(1, results.size());

	}

}
