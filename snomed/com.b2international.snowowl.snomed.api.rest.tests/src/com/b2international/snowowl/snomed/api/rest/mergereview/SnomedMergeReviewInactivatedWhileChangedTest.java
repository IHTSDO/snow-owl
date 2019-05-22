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
import static com.b2international.snowowl.snomed.api.rest.SnomedRestFixtures.inactivateConcept;
import static com.b2international.snowowl.snomed.api.rest.SnomedRestFixtures.inactivateDescription;
import static com.b2international.snowowl.snomed.api.rest.SnomedRestFixtures.inactivateMember;
import static com.b2international.snowowl.snomed.api.rest.SnomedRestFixtures.inactivateRelationship;
import static com.b2international.snowowl.test.commons.rest.RestExtensions.lastPathSegment;
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
import com.b2international.snowowl.snomed.api.rest.SnomedComponentType;
import com.b2international.snowowl.snomed.api.rest.domain.SnomedRefSetMemberRestInput;
import com.b2international.snowowl.snomed.common.SnomedRf2Headers;
import com.b2international.snowowl.snomed.core.domain.CaseSignificance;
import com.b2international.snowowl.snomed.core.domain.CharacteristicType;
import com.b2international.snowowl.snomed.core.domain.DefinitionStatus;
import com.b2international.snowowl.snomed.core.domain.SnomedConcept;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * @since 6.15.1
 */
public class SnomedMergeReviewInactivatedWhileChangedTest extends AbstractSnomedApiTest {

	@Test
	public void mergeReviewPresentForInactivatedConceptVsNewDescription() {

		final String conceptId = createNewConcept(branchPath);

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		inactivateConcept(childBranch, conceptId);

		createNewDescription(branchPath, conceptId);

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(childBranch, branchPath);

		assertEquals(1, results.size());

	}

	@Test
	public void mergeReviewPresentForInactivatedConceptVsNewStatedRelationship() {

		final String conceptId = createNewConcept(branchPath);

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		inactivateConcept(childBranch, conceptId);

		createNewRelationship(branchPath, conceptId, Concepts.PART_OF, Concepts.NAMESPACE_ROOT, CharacteristicType.STATED_RELATIONSHIP);

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(childBranch, branchPath);

		assertEquals(1, results.size());

	}

	@Test
	public void mergeReviewPresentForInactivatedConceptVsNewOwlAxiom() {

		final String conceptId = createNewConcept(branchPath);

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		inactivateConcept(childBranch, conceptId);

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
	public void mergeReviewPresentForInactivatedConceptVsNewFsn() {

		final String conceptId = createNewConcept(branchPath);

		final SnomedConcept concept = getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, "fsn()").extract().as(SnomedConcept.class);
		final String originalFsnId = concept.getFsn().getId();

		final String descriptionId = createNewDescription(branchPath, conceptId, Concepts.FULLY_SPECIFIED_NAME, SnomedApiTestConstants.UK_ACCEPTABLE_MAP);

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		inactivateConcept(childBranch, conceptId);

		final Map<?, ?> originalRequestBody = ImmutableMap.builder().put("acceptability", SnomedApiTestConstants.UK_ACCEPTABLE_MAP)
				.put("commitComment", "Updated description acceptability").build();

		updateComponent(branchPath, SnomedComponentType.DESCRIPTION, originalFsnId, originalRequestBody).statusCode(204);

		final Map<?, ?> newRequestBody = ImmutableMap.builder().put("acceptability", SnomedApiTestConstants.UK_PREFERRED_MAP)
				.put("commitComment", "Updated description acceptability").build();

		updateComponent(branchPath, SnomedComponentType.DESCRIPTION, descriptionId, newRequestBody).statusCode(204);

		assertEquals(descriptionId,
				getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, "fsn()").extract().as(SnomedConcept.class).getFsn().getId());

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(childBranch, branchPath);

		assertEquals(1, results.size());

	}

	@Test
	public void mergeReviewPresentForInactivatedConceptVsNewPt() {

		final String conceptId = createNewConcept(branchPath);

		final SnomedConcept concept = getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, "pt()").extract().as(SnomedConcept.class);
		final String originalPtId = concept.getPt().getId();

		final String descriptionId = createNewDescription(branchPath, conceptId, Concepts.SYNONYM, SnomedApiTestConstants.UK_ACCEPTABLE_MAP);

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		inactivateConcept(childBranch, conceptId);

		final Map<?, ?> originalRequestBody = ImmutableMap.builder().put("acceptability", SnomedApiTestConstants.UK_ACCEPTABLE_MAP)
				.put("commitComment", "Updated description acceptability").build();

		updateComponent(branchPath, SnomedComponentType.DESCRIPTION, originalPtId, originalRequestBody).statusCode(204);

		final Map<?, ?> newRequestBody = ImmutableMap.builder().put("acceptability", SnomedApiTestConstants.UK_PREFERRED_MAP)
				.put("commitComment", "Updated description acceptability").build();

		updateComponent(branchPath, SnomedComponentType.DESCRIPTION, descriptionId, newRequestBody).statusCode(204);

		assertEquals(descriptionId,
				getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, "pt()").extract().as(SnomedConcept.class).getPt().getId());

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(childBranch, branchPath);

		assertEquals(1, results.size());

	}

	@Test
	public void mergeReviewPresentForInactivatedConceptVsChangedConceptProperty() {

		final String conceptId = createNewConcept(branchPath);

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		inactivateConcept(childBranch, conceptId);

		final Map<?, ?> updateRequestBody = ImmutableMap.<String, Object>builder().put("definitionStatus", DefinitionStatus.FULLY_DEFINED)
				.put("commitComment", "Changed definition status of concept to fully defined").build();

		updateComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, updateRequestBody).statusCode(204);

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(childBranch, branchPath);

		assertEquals(1, results.size());

	}

	@Test
	public void mergeReviewPresentForInactivatedConceptVsChangedDescription() {

		final String conceptId = createNewConcept(branchPath);
		final String descriptionId = createNewDescription(branchPath, conceptId);

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		inactivateConcept(childBranch, conceptId);

		final Map<?, ?> updateRequestBody = ImmutableMap.builder().put("caseSignificance", CaseSignificance.CASE_INSENSITIVE)
				.put("commitComment", "Updated description case significance").build();

		updateComponent(branchPath, SnomedComponentType.DESCRIPTION, descriptionId, updateRequestBody).statusCode(204);

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(childBranch, branchPath);

		assertEquals(1, results.size());

	}

	@Test
	public void mergeReviewPresentForInactivatedConceptVsChangedStatedRelationship() {

		final String conceptId = createNewConcept(branchPath);
		final String relationshipId = createNewRelationship(branchPath, conceptId, Concepts.PART_OF, Concepts.NAMESPACE_ROOT,
				CharacteristicType.STATED_RELATIONSHIP);

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		inactivateConcept(childBranch, conceptId);

		final Map<?, ?> update = ImmutableMap.builder().put(SnomedRf2Headers.FIELD_TYPE_ID, Concepts.DEFINING_RELATIONSHIP)
				.put("commitComment", "Updated relationship typeId").build();

		updateComponent(branchPath, SnomedComponentType.RELATIONSHIP, relationshipId, update).statusCode(204);

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(childBranch, branchPath);

		assertEquals(1, results.size());

	}

	@Test
	public void mergeReviewPresentForInactivatedConceptVsChangedOwlAxiom() {

		final String owlSubclassOfExpression = String.format("SubClassOf(:%s :%s)", Concepts.FULLY_SPECIFIED_NAME, Concepts.AMBIGUOUS);

		final Map<?, ?> memberRequestBody = ImmutableMap.<String, Object>builder().put("moduleId", Concepts.MODULE_SCT_CORE)
				.put("referenceSetId", Concepts.REFSET_OWL_AXIOM).put(SnomedRefSetMemberRestInput.ADDITIONAL_FIELDS,
						ImmutableMap.<String, Object>builder().put(SnomedRf2Headers.FIELD_OWL_EXPRESSION, owlSubclassOfExpression).build())
				.build();

		final Map<?, ?> conceptRequestBody = createConceptRequestBody(Concepts.ROOT_CONCEPT).put("members", ImmutableList.of(memberRequestBody))
				.put("commitComment", "Created concept with owl axiom reference set member").build();

		final String conceptId = lastPathSegment(
				createComponent(branchPath, SnomedComponentType.CONCEPT, conceptRequestBody).statusCode(201).extract().header("Location"));

		final SnomedConcept concept = getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, "members()").extract().as(SnomedConcept.class);
		final String memberId = concept.getMembers().stream().filter(member -> member.getRefsetId().equals(Concepts.REFSET_OWL_AXIOM)).findFirst().get()
				.getId();

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		inactivateConcept(childBranch, conceptId);

		final String newOwlExpression = String.format("SubClassOf(:%s :%s)", Concepts.FULLY_SPECIFIED_NAME, Concepts.NAMESPACE_ROOT);

		final Map<?, ?> updateRequest = ImmutableMap.builder()
				.put(SnomedRefSetMemberRestInput.ADDITIONAL_FIELDS, ImmutableMap.<String, Object>builder()
						.putAll(ImmutableMap.<String, Object>builder().put(SnomedRf2Headers.FIELD_OWL_EXPRESSION, newOwlExpression).build()).build())
				.put("commitComment", "Updated reference set member").build();

		updateRefSetComponent(branchPath, SnomedComponentType.MEMBER, memberId, updateRequest, false).statusCode(204);

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(childBranch, branchPath);

		assertEquals(1, results.size());

	}

	@Test
	public void mergeReviewPresentForInactivatedConceptVsChangedFsn() {

		final String conceptId = createNewConcept(branchPath);

		final SnomedConcept concept = getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, "fsn()").extract().as(SnomedConcept.class);
		final String fsnId = concept.getFsn().getId();

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		inactivateConcept(childBranch, conceptId);

		final Map<?, ?> updateRequest = ImmutableMap.builder()
				.put(SnomedRf2Headers.FIELD_TERM, "Updated FSN term")
				.put("commitComment", "Update description term")
				.build();

		updateComponent(branchPath, SnomedComponentType.DESCRIPTION, fsnId, updateRequest).statusCode(204);

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(childBranch, branchPath);

		assertEquals(1, results.size());

	}

	@Test
	public void mergeReviewPresentForInactivatedConceptVsChangedPt() {

		final String conceptId = createNewConcept(branchPath);

		final SnomedConcept concept = getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, "pt()").extract().as(SnomedConcept.class);
		final String ptId = concept.getPt().getId();

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		inactivateConcept(childBranch, conceptId);

		final Map<?, ?> updateRequest = ImmutableMap.builder()
				.put(SnomedRf2Headers.FIELD_TERM, "Updated PT term")
				.put("commitComment", "Update description term")
				.build();

		updateComponent(branchPath, SnomedComponentType.DESCRIPTION, ptId, updateRequest).statusCode(204);

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(childBranch, branchPath);

		assertEquals(1, results.size());

	}

	@Test
	public void mergeReviewPresentForInactivatedDescriptionVsChangedDescription() {

		final String conceptId = createNewConcept(branchPath);
		final String descriptionId = createNewDescription(branchPath, conceptId);

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		inactivateDescription(childBranch, descriptionId);

		final Map<?, ?> updateRequestBody = ImmutableMap.builder().put("caseSignificance", CaseSignificance.CASE_INSENSITIVE)
				.put("commitComment", "Updated description case significance").build();

		updateComponent(branchPath, SnomedComponentType.DESCRIPTION, descriptionId, updateRequestBody).statusCode(204);

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(childBranch, branchPath);

		assertEquals(1, results.size());

	}

	@Test
	public void mergeReviewPresentForInactivatedStatedRelationshipVsChangedStatedRelationship() {

		final String conceptId = createNewConcept(branchPath);
		final String relationshipId = createNewRelationship(branchPath, conceptId, Concepts.PART_OF, Concepts.NAMESPACE_ROOT,
				CharacteristicType.STATED_RELATIONSHIP);

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		inactivateRelationship(childBranch, relationshipId);

		final Map<?, ?> update = ImmutableMap.builder().put(SnomedRf2Headers.FIELD_TYPE_ID, Concepts.DEFINING_RELATIONSHIP)
				.put("commitComment", "Updated relationship typeId").build();

		updateComponent(branchPath, SnomedComponentType.RELATIONSHIP, relationshipId, update).statusCode(204);

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(childBranch, branchPath);

		assertEquals(1, results.size());

	}

	@Test
	public void mergeReviewPresentForInactivatedOwlAxiomVsChangedOwlAxiom() {

		final String owlSubclassOfExpression = String.format("SubClassOf(:%s :%s)", Concepts.FULLY_SPECIFIED_NAME, Concepts.AMBIGUOUS);

		final Map<?, ?> memberRequestBody = ImmutableMap.<String, Object>builder().put("moduleId", Concepts.MODULE_SCT_CORE)
				.put("referenceSetId", Concepts.REFSET_OWL_AXIOM).put(SnomedRefSetMemberRestInput.ADDITIONAL_FIELDS,
						ImmutableMap.<String, Object>builder().put(SnomedRf2Headers.FIELD_OWL_EXPRESSION, owlSubclassOfExpression).build())
				.build();

		final Map<?, ?> conceptRequestBody = createConceptRequestBody(Concepts.ROOT_CONCEPT).put("members", ImmutableList.of(memberRequestBody))
				.put("commitComment", "Created concept with owl axiom reference set member").build();

		final String conceptId = lastPathSegment(
				createComponent(branchPath, SnomedComponentType.CONCEPT, conceptRequestBody).statusCode(201).extract().header("Location"));

		final SnomedConcept concept = getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, "members()").extract().as(SnomedConcept.class);
		final String memberId = concept.getMembers().stream().filter(member -> member.getRefsetId().equals(Concepts.REFSET_OWL_AXIOM)).findFirst().get()
				.getId();

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		inactivateMember(childBranch, memberId);

		final String newOwlExpression = String.format("SubClassOf(:%s :%s)", Concepts.FULLY_SPECIFIED_NAME, Concepts.NAMESPACE_ROOT);

		final Map<?, ?> updateRequest = ImmutableMap.builder()
				.put(SnomedRefSetMemberRestInput.ADDITIONAL_FIELDS, ImmutableMap.<String, Object>builder()
						.putAll(ImmutableMap.<String, Object>builder().put(SnomedRf2Headers.FIELD_OWL_EXPRESSION, newOwlExpression).build()).build())
				.put("commitComment", "Updated reference set member").build();

		updateRefSetComponent(branchPath, SnomedComponentType.MEMBER, memberId, updateRequest, false).statusCode(204);

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(childBranch, branchPath);

		assertEquals(1, results.size());

	}

	@Test
	public void mergeReviewPresentForInactivatedFsnVsChangedFsn() {

		final String conceptId = createNewConcept(branchPath);

		final SnomedConcept concept = getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, "fsn()").extract().as(SnomedConcept.class);
		final String fsnId = concept.getFsn().getId();

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		inactivateDescription(childBranch, fsnId);

		final Map<?, ?> updateRequest = ImmutableMap.builder()
				.put(SnomedRf2Headers.FIELD_TERM, "Updated FSN term")
				.put("commitComment", "Update description term")
				.build();

		updateComponent(branchPath, SnomedComponentType.DESCRIPTION, fsnId, updateRequest).statusCode(204);

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(childBranch, branchPath);

		assertEquals(1, results.size());

	}

	@Test
	public void mergeReviewPresentForInactivatedPtVsChangedPt() {

		final String conceptId = createNewConcept(branchPath);

		final SnomedConcept concept = getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, "pt()").extract().as(SnomedConcept.class);
		final String ptId = concept.getPt().getId();

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		inactivateDescription(childBranch, ptId);

		final Map<?, ?> updateRequest = ImmutableMap.builder()
				.put(SnomedRf2Headers.FIELD_TERM, "Updated PT term")
				.put("commitComment", "Update description term")
				.build();

		updateComponent(branchPath, SnomedComponentType.DESCRIPTION, ptId, updateRequest).statusCode(204);

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(childBranch, branchPath);

		assertEquals(1, results.size());

	}


}
