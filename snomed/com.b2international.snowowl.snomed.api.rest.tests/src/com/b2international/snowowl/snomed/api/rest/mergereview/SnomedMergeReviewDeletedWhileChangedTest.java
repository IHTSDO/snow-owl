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
import static com.b2international.snowowl.snomed.api.rest.SnomedComponentRestRequests.deleteComponent;
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
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
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
import com.b2international.snowowl.snomed.core.domain.AssociationType;
import com.b2international.snowowl.snomed.core.domain.CaseSignificance;
import com.b2international.snowowl.snomed.core.domain.CharacteristicType;
import com.b2international.snowowl.snomed.core.domain.DefinitionStatus;
import com.b2international.snowowl.snomed.core.domain.InactivationIndicator;
import com.b2international.snowowl.snomed.core.domain.SnomedConcept;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * @since 6.15.1
 */
public class SnomedMergeReviewDeletedWhileChangedTest extends AbstractSnomedApiTest {

	@Test
	public void mergeReviewPresentForDeletedConceptVsNewDescription() {

		final String conceptId = createNewConcept(branchPath);

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		getComponent(childBranch, SnomedComponentType.CONCEPT, conceptId).statusCode(200);
		deleteComponent(childBranch, SnomedComponentType.CONCEPT, conceptId, false);
		getComponent(childBranch, SnomedComponentType.CONCEPT, conceptId).statusCode(404);

		createNewDescription(branchPath, conceptId);

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(childBranch, branchPath);

		assertEquals(1, results.size());
		assertNull(results.stream().findFirst().get().getSourceConcept());

	}

	@Test
	public void mergeReviewPresentForDeletedConceptVsNewStatedRelationship() {

		final String conceptId = createNewConcept(branchPath);

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		getComponent(childBranch, SnomedComponentType.CONCEPT, conceptId).statusCode(200);
		deleteComponent(childBranch, SnomedComponentType.CONCEPT, conceptId, false);
		getComponent(childBranch, SnomedComponentType.CONCEPT, conceptId).statusCode(404);

		createNewRelationship(branchPath, conceptId, Concepts.PART_OF, Concepts.NAMESPACE_ROOT, CharacteristicType.STATED_RELATIONSHIP);

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(childBranch, branchPath);

		assertEquals(1, results.size());
		assertNull(results.stream().findFirst().get().getSourceConcept());

	}

	@Test
	public void mergeReviewPresentForDeletedConceptVsNewOwlAxiom() {

		final String conceptId = createNewConcept(branchPath);

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		getComponent(childBranch, SnomedComponentType.CONCEPT, conceptId).statusCode(200);
		deleteComponent(childBranch, SnomedComponentType.CONCEPT, conceptId, false);
		getComponent(childBranch, SnomedComponentType.CONCEPT, conceptId).statusCode(404);

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
		assertNull(results.stream().findFirst().get().getSourceConcept());

	}

	@Test
	public void mergeReviewPresentForDeletedConceptVsNewInactivationReason() {

		final String conceptId = createNewConcept(branchPath);

		final Map<?, ?> inactivationRequestBody = ImmutableMap.<String, Object>builder()
				.put("active", false)
				.put("commitComment", "Inactivated concept")
				.build();

		updateComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, inactivationRequestBody).statusCode(204);

		final SnomedConcept concept = getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, "members()").extract().body().as(SnomedConcept.class);
		assertTrue(concept.getMembers().stream().noneMatch(member -> member.getRefsetId().equals(Concepts.REFSET_CONCEPT_INACTIVITY_INDICATOR)));

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		getComponent(childBranch, SnomedComponentType.CONCEPT, conceptId).statusCode(200);
		deleteComponent(childBranch, SnomedComponentType.CONCEPT, conceptId, false);
		getComponent(childBranch, SnomedComponentType.CONCEPT, conceptId).statusCode(404);

		final Map<?, ?> udpateRequest = ImmutableMap.<String, Object>builder()
				.put("inactivationIndicator", InactivationIndicator.ERRONEOUS)
				.put("commitComment", "Add inactivation indicator")
				.build();

		updateComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, udpateRequest).statusCode(204);

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(childBranch, branchPath);

		assertEquals(1, results.size());
		assertNull(results.stream().findFirst().get().getSourceConcept());

	}

	@Test
	public void mergeReviewPresentForDeletedConceptVsNewAssociationTarget() {

		final String conceptId = createNewConcept(branchPath);

		final Map<?, ?> inactivationRequestBody = ImmutableMap.<String, Object>builder()
				.put("active", false)
				.put("commitComment", "Inactivated concept")
				.build();

		updateComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, inactivationRequestBody).statusCode(204);

		final SnomedConcept concept = getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, "members()").extract().body().as(SnomedConcept.class);

		assertTrue(concept.getMembers().stream().noneMatch(member -> AssociationType.getByConceptId(member.getRefsetId()) != null));

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		getComponent(childBranch, SnomedComponentType.CONCEPT, conceptId).statusCode(200);
		deleteComponent(childBranch, SnomedComponentType.CONCEPT, conceptId, false);
		getComponent(childBranch, SnomedComponentType.CONCEPT, conceptId).statusCode(404);

		final Map<?, ?> updateRequest = ImmutableMap.<String, Object>builder()
				.put("associationTargets", ImmutableMap.of(AssociationType.POSSIBLY_EQUIVALENT_TO, ImmutableList.of(Concepts.NAMESPACE_ROOT)))
				.put("commitComment", "Add new association target")
				.build();

		updateComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, updateRequest).statusCode(204);

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(childBranch, branchPath);

		assertEquals(1, results.size());
		assertNull(results.stream().findFirst().get().getSourceConcept());

	}

	@Test
	public void mergeReviewPresentForDeletedConceptVsNewFsn() {

		final String conceptId = createNewConcept(branchPath);

		final SnomedConcept concept = getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, "fsn()").extract().as(SnomedConcept.class);
		final String originalFsnId = concept.getFsn().getId();

		final String descriptionId = createNewDescription(branchPath, conceptId, Concepts.FULLY_SPECIFIED_NAME, SnomedApiTestConstants.UK_ACCEPTABLE_MAP);

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		getComponent(childBranch, SnomedComponentType.CONCEPT, conceptId).statusCode(200);
		deleteComponent(childBranch, SnomedComponentType.CONCEPT, conceptId, false);
		getComponent(childBranch, SnomedComponentType.CONCEPT, conceptId).statusCode(404);

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
		assertNull(results.stream().findFirst().get().getSourceConcept());

	}

	@Test
	public void mergeReviewPresentForDeletedConceptVsNewPt() {

		final String conceptId = createNewConcept(branchPath);

		final SnomedConcept concept = getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, "pt()").extract().as(SnomedConcept.class);
		final String originalPtId = concept.getPt().getId();

		final String descriptionId = createNewDescription(branchPath, conceptId, Concepts.SYNONYM, SnomedApiTestConstants.UK_ACCEPTABLE_MAP);

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		getComponent(childBranch, SnomedComponentType.CONCEPT, conceptId).statusCode(200);
		deleteComponent(childBranch, SnomedComponentType.CONCEPT, conceptId, false);
		getComponent(childBranch, SnomedComponentType.CONCEPT, conceptId).statusCode(404);

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
		assertNull(results.stream().findFirst().get().getSourceConcept());

	}

	@Test
	public void mergeReviewPresentForDeletedConceptVsChangedConceptProperty() {

		final String conceptId = createNewConcept(branchPath);

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		getComponent(childBranch, SnomedComponentType.CONCEPT, conceptId).statusCode(200);
		deleteComponent(childBranch, SnomedComponentType.CONCEPT, conceptId, false);
		getComponent(childBranch, SnomedComponentType.CONCEPT, conceptId).statusCode(404);

		final Map<?, ?> updateRequestBody = ImmutableMap.<String, Object>builder()
				.put("definitionStatus", DefinitionStatus.FULLY_DEFINED)
				.put("commitComment", "Changed definition status of concept to fully defined")
				.build();

		updateComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, updateRequestBody).statusCode(204);

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(childBranch, branchPath);

		assertEquals(1, results.size());
		assertNull(results.stream().findFirst().get().getSourceConcept());

	}

	@Test
	public void mergeReviewPresentForDeletedConceptVsChangedDescription() {

		final String conceptId = createNewConcept(branchPath);
		final String descriptionId = createNewDescription(branchPath, conceptId);

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		getComponent(childBranch, SnomedComponentType.CONCEPT, conceptId).statusCode(200);
		deleteComponent(childBranch, SnomedComponentType.CONCEPT, conceptId, false);
		getComponent(childBranch, SnomedComponentType.CONCEPT, conceptId).statusCode(404);

		final Map<?, ?> updateRequestBody = ImmutableMap.builder()
				.put("caseSignificance", CaseSignificance.CASE_INSENSITIVE)
				.put("commitComment", "Updated description case significance")
				.build();

		updateComponent(branchPath, SnomedComponentType.DESCRIPTION, descriptionId, updateRequestBody).statusCode(204);

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(childBranch, branchPath);

		assertEquals(1, results.size());
		assertNull(results.stream().findFirst().get().getSourceConcept());

	}

	@Test
	public void mergeReviewPresentForDeletedConceptVsChangedStatedRelationship() {

		final String conceptId = createNewConcept(branchPath);
		final String relationshipId = createNewRelationship(branchPath, conceptId, Concepts.PART_OF, Concepts.NAMESPACE_ROOT, CharacteristicType.STATED_RELATIONSHIP);

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		getComponent(childBranch, SnomedComponentType.CONCEPT, conceptId).statusCode(200);
		deleteComponent(childBranch, SnomedComponentType.CONCEPT, conceptId, false);
		getComponent(childBranch, SnomedComponentType.CONCEPT, conceptId).statusCode(404);

		final Map<?, ?> update = ImmutableMap.builder()
				.put(SnomedRf2Headers.FIELD_TYPE_ID, Concepts.DEFINING_RELATIONSHIP)
				.put("commitComment", "Updated relationship typeId")
				.build();

		updateComponent(branchPath, SnomedComponentType.RELATIONSHIP, relationshipId, update).statusCode(204);

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(childBranch, branchPath);

		assertEquals(1, results.size());
		assertNull(results.stream().findFirst().get().getSourceConcept());

	}

	@Test
	public void mergeReviewPresentForDeletedConceptVsChangedOwlAxiom() {

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

		getComponent(childBranch, SnomedComponentType.CONCEPT, conceptId).statusCode(200);
		deleteComponent(childBranch, SnomedComponentType.CONCEPT, conceptId, false);
		getComponent(childBranch, SnomedComponentType.CONCEPT, conceptId).statusCode(404);

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
		assertNull(results.stream().findFirst().get().getSourceConcept());

	}

	@Test
	public void mergeReviewPresentForDeletedConceptVsChangedInactivationReason() {

		final String conceptId = createNewConcept(branchPath);

		final Map<?, ?> inactivationRequestBody = ImmutableMap.<String, Object>builder()
				.put("active", false)
				.put("inactivationIndicator", InactivationIndicator.AMBIGUOUS)
				.put("commitComment", "Inactivated concept")
				.build();

		updateComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, inactivationRequestBody).statusCode(204);

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		getComponent(childBranch, SnomedComponentType.CONCEPT, conceptId).statusCode(200);
		deleteComponent(childBranch, SnomedComponentType.CONCEPT, conceptId, false);
		getComponent(childBranch, SnomedComponentType.CONCEPT, conceptId).statusCode(404);

		final Map<?, ?> udpateRequest = ImmutableMap.<String, Object>builder()
				.put("inactivationIndicator", InactivationIndicator.ERRONEOUS)
				.put("commitComment", "Update inactivation indicator")
				.build();

		updateComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, udpateRequest).statusCode(204);

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(childBranch, branchPath);

		assertEquals(1, results.size());
		assertNull(results.stream().findFirst().get().getSourceConcept());

	}

	@Test
	public void mergeReviewPresentForDeletedConceptVsChangedAssociationTarget() {

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

		getComponent(childBranch, SnomedComponentType.CONCEPT, conceptId).statusCode(200);
		deleteComponent(childBranch, SnomedComponentType.CONCEPT, conceptId, false);
		getComponent(childBranch, SnomedComponentType.CONCEPT, conceptId).statusCode(404);

		final Map<?, ?> updateRequest = ImmutableMap.<String, Object>builder()
				.put("associationTargets", ImmutableMap.of(AssociationType.POSSIBLY_EQUIVALENT_TO, ImmutableList.of(Concepts.NAMESPACE_ROOT)))
				.put("commitComment", "Update association target")
				.build();

		updateComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, updateRequest).statusCode(204);

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(childBranch, branchPath);

		assertEquals(1, results.size());
		assertNull(results.stream().findFirst().get().getSourceConcept());

	}

	@Test
	public void mergeReviewPresentForDeletedConceptVsChangedFsn() {

		final String conceptId = createNewConcept(branchPath);

		final SnomedConcept concept = getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, "fsn()").extract().as(SnomedConcept.class);
		final String fsnId = concept.getFsn().getId();

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		getComponent(childBranch, SnomedComponentType.CONCEPT, conceptId).statusCode(200);
		deleteComponent(childBranch, SnomedComponentType.CONCEPT, conceptId, false);
		getComponent(childBranch, SnomedComponentType.CONCEPT, conceptId).statusCode(404);

		final Map<?, ?> updateRequest = ImmutableMap.builder()
				.put(SnomedRf2Headers.FIELD_TERM, "Updated FSN term")
				.put("commitComment", "Update description term")
				.build();

		updateComponent(branchPath, SnomedComponentType.DESCRIPTION, fsnId, updateRequest).statusCode(204);

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(childBranch, branchPath);

		assertEquals(1, results.size());
		assertNull(results.stream().findFirst().get().getSourceConcept());

	}

	@Test
	public void mergeReviewPresentForDeletedConceptVsChangedPt() {

		final String conceptId = createNewConcept(branchPath);

		final SnomedConcept concept = getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, "pt()").extract().as(SnomedConcept.class);
		final String ptId = concept.getPt().getId();

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		getComponent(childBranch, SnomedComponentType.CONCEPT, conceptId).statusCode(200);
		deleteComponent(childBranch, SnomedComponentType.CONCEPT, conceptId, false);
		getComponent(childBranch, SnomedComponentType.CONCEPT, conceptId).statusCode(404);

		final Map<?, ?> updateRequest = ImmutableMap.builder()
				.put(SnomedRf2Headers.FIELD_TERM, "Updated PT term")
				.put("commitComment", "Update description term")
				.build();

		updateComponent(branchPath, SnomedComponentType.DESCRIPTION, ptId, updateRequest).statusCode(204);

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(childBranch, branchPath);

		assertEquals(1, results.size());
		assertNull(results.stream().findFirst().get().getSourceConcept());

	}

	@Test
	public void mergeReviewPresentForDeletedDescriptionVsChangedDescription() {

		final String conceptId = createNewConcept(branchPath);
		final String descriptionId = createNewDescription(branchPath, conceptId);

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		deleteComponent(childBranch, SnomedComponentType.DESCRIPTION, descriptionId, false);
		getComponent(childBranch, SnomedComponentType.DESCRIPTION, descriptionId).statusCode(404);

		final Map<?, ?> updateRequestBody = ImmutableMap.builder()
				.put("caseSignificance", CaseSignificance.CASE_INSENSITIVE)
				.put("commitComment", "Updated description case significance")
				.build();

		updateComponent(branchPath, SnomedComponentType.DESCRIPTION, descriptionId, updateRequestBody).statusCode(204);

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(childBranch, branchPath);

		assertEquals(1, results.size());

	}

	@Test
	public void mergeReviewPresentForDeletedStatedRelationshipVsChangedStatedRelationship() {

		final String conceptId = createNewConcept(branchPath);
		final String relationshipId = createNewRelationship(branchPath, conceptId, Concepts.PART_OF, Concepts.NAMESPACE_ROOT, CharacteristicType.STATED_RELATIONSHIP);

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		deleteComponent(childBranch, SnomedComponentType.RELATIONSHIP, relationshipId, false);
		getComponent(childBranch, SnomedComponentType.RELATIONSHIP, relationshipId).statusCode(404);

		final Map<?, ?> update = ImmutableMap.builder()
				.put(SnomedRf2Headers.FIELD_TYPE_ID, Concepts.DEFINING_RELATIONSHIP)
				.put("commitComment", "Updated relationship typeId")
				.build();

		updateComponent(branchPath, SnomedComponentType.RELATIONSHIP, relationshipId, update).statusCode(204);

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(childBranch, branchPath);

		assertEquals(1, results.size());

	}

	@Test
	public void mergeReviewPresentForDeletedOwlAxiomVsChangedOwlAxiom() {

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

		deleteComponent(childBranch, SnomedComponentType.MEMBER, memberId, false);
		getComponent(childBranch, SnomedComponentType.MEMBER, memberId).statusCode(404);

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
	public void mergeReviewPresentForDeletedInactivationReasonVsChangedInactivationReason() {

		final String conceptId = createNewConcept(branchPath);

		final Map<?, ?> inactivationRequestBody = ImmutableMap.<String, Object>builder()
				.put("active", false)
				.put("inactivationIndicator", InactivationIndicator.AMBIGUOUS)
				.put("commitComment", "Inactivated concept")
				.build();

		updateComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, inactivationRequestBody).statusCode(204);

		final SnomedConcept concept = getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, "members()").extract().body().as(SnomedConcept.class);
		final String memberId = concept.getMembers().stream().filter(member -> member.getRefsetId().equals(Concepts.REFSET_CONCEPT_INACTIVITY_INDICATOR)).findFirst().get().getId();

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		deleteComponent(childBranch, SnomedComponentType.MEMBER, memberId, false);
		getComponent(childBranch, SnomedComponentType.MEMBER, memberId).statusCode(404);

		final Map<?, ?> udpateRequest = ImmutableMap.<String, Object>builder()
				.put("inactivationIndicator", InactivationIndicator.ERRONEOUS)
				.put("commitComment", "Update inactivation indicator")
				.build();

		updateComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, udpateRequest).statusCode(204);

		getComponent(branchPath, SnomedComponentType.MEMBER, memberId)
		.body(SnomedRefSetMemberRestInput.ADDITIONAL_FIELDS + "." + SnomedRf2Headers.FIELD_VALUE_ID, equalTo(InactivationIndicator.ERRONEOUS.getConceptId()));

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(childBranch, branchPath);

		assertEquals(1, results.size());

	}

	@Test
	public void mergeReviewPresentForDeletedAssociationTargetVsChangedAssociationTarget() {

		final String conceptId = createNewConcept(branchPath);

		final Map<?, ?> inactivationRequestBody = ImmutableMap.<String, Object>builder()
				.put("active", false)
				.put("inactivationIndicator", InactivationIndicator.AMBIGUOUS)
				.put("associationTargets", ImmutableMap.of(AssociationType.POSSIBLY_EQUIVALENT_TO, ImmutableList.of(Concepts.ROOT_CONCEPT)))
				.put("commitComment", "Inactivated concept")
				.build();

		updateComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, inactivationRequestBody).statusCode(204);

		final SnomedConcept concept = getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, "members()").extract().body().as(SnomedConcept.class);
		final String memberId = concept.getMembers().stream().filter(member -> member.getRefsetId().equals(AssociationType.POSSIBLY_EQUIVALENT_TO.getConceptId())).findFirst().get().getId();

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		deleteComponent(childBranch, SnomedComponentType.MEMBER, memberId, false);
		getComponent(childBranch, SnomedComponentType.MEMBER, memberId).statusCode(404);

		final Map<?, ?> updateRequest = ImmutableMap.<String, Object>builder()
				.put("associationTargets", ImmutableMap.of(AssociationType.POSSIBLY_EQUIVALENT_TO, ImmutableList.of(Concepts.NAMESPACE_ROOT)))
				.put("commitComment", "Update association target")
				.build();

		updateComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, updateRequest).statusCode(204);

		getComponent(branchPath, SnomedComponentType.MEMBER, memberId)
		.body(SnomedRefSetMemberRestInput.ADDITIONAL_FIELDS + "." + SnomedRf2Headers.FIELD_TARGET_COMPONENT, equalTo(ImmutableMap.of("id", Concepts.NAMESPACE_ROOT)));

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(childBranch, branchPath);

		assertEquals(1, results.size());

	}

	@Test
	public void mergeReviewPresentForDeletedFsnVsChangedFsn() {

		final String conceptId = createNewConcept(branchPath);

		final SnomedConcept concept = getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, "fsn()").extract().as(SnomedConcept.class);
		final String fsnId = concept.getFsn().getId();

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		deleteComponent(childBranch, SnomedComponentType.DESCRIPTION, fsnId, false);
		getComponent(childBranch, SnomedComponentType.DESCRIPTION, fsnId).statusCode(404);

		final Map<?, ?> updateRequest = ImmutableMap.builder()
				.put(SnomedRf2Headers.FIELD_TERM, "Updated FSN term")
				.put("commitComment", "Update description term")
				.build();

		updateComponent(branchPath, SnomedComponentType.DESCRIPTION, fsnId, updateRequest).statusCode(204);

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(childBranch, branchPath);

		assertEquals(1, results.size());

	}

	@Test
	public void mergeReviewPresentForDeletedPtVsChangedPt() {

		final String conceptId = createNewConcept(branchPath);

		final SnomedConcept concept = getComponent(branchPath, SnomedComponentType.CONCEPT, conceptId, "pt()").extract().as(SnomedConcept.class);
		final String ptId = concept.getPt().getId();

		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "a");
		createBranch(childBranch);

		deleteComponent(childBranch, SnomedComponentType.DESCRIPTION, ptId, false);
		getComponent(childBranch, SnomedComponentType.DESCRIPTION, ptId).statusCode(404);

		final Map<?, ?> updateRequest = ImmutableMap.builder()
				.put(SnomedRf2Headers.FIELD_TERM, "Updated PT term")
				.put("commitComment", "Update description term")
				.build();

		updateComponent(branchPath, SnomedComponentType.DESCRIPTION, ptId, updateRequest).statusCode(204);

		final Set<ISnomedBrowserMergeReviewDetail> results = createMergeReviewAndGetDetails(childBranch, branchPath);

		assertEquals(1, results.size());

	}

}
