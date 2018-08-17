/*
 * Copyright 2018 B2i Healthcare Pte Ltd, http://b2i.sg
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
package com.b2international.snowowl.snomed.api.rest.components;

import static com.b2international.snowowl.snomed.api.rest.CodeSystemRestRequests.createCodeSystem;
import static com.b2international.snowowl.snomed.api.rest.CodeSystemVersionRestRequests.createVersion;
import static com.b2international.snowowl.snomed.api.rest.CodeSystemVersionRestRequests.getNextAvailableEffectiveDateAsString;
import static com.b2international.snowowl.snomed.api.rest.SnomedRestFixtures.createNewConcept;
import static com.b2international.snowowl.snomed.api.rest.SnomedRestFixtures.merge;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.b2international.snowowl.core.api.IBranchPath;
import com.b2international.snowowl.core.merge.Merge;
import com.b2international.snowowl.datastore.BranchPathUtils;
import com.b2international.snowowl.snomed.SnomedConstants.Concepts;
import com.b2international.snowowl.snomed.api.rest.AbstractSnomedApiTest;
import com.b2international.snowowl.snomed.api.rest.SnomedBranchingRestRequests;
import com.b2international.snowowl.snomed.core.domain.AssociationType;
import com.b2international.snowowl.snomed.core.domain.SnomedConcept;
import com.b2international.snowowl.snomed.core.domain.refset.SnomedReferenceSetMember;
import com.b2international.snowowl.snomed.core.domain.refset.SnomedReferenceSetMembers;
import com.b2international.snowowl.snomed.datastore.SnomedDatastoreActivator;
import com.b2international.snowowl.snomed.datastore.request.SnomedRequests;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * @since 5.10.14
 */
public class SnomedAssociationMemberRequestTest extends AbstractSnomedApiTest {

	private static final String REPOSITORY_UUID = SnomedDatastoreActivator.REPOSITORY_UUID;

	private static final String SAME_AS_REFSET_ID = Concepts.REFSET_SAME_AS_ASSOCIATION;
 
	@Test
	public void testAssociationTargetChanged() {
		final String conceptId = createNewConcept(branchPath);
		final String expectedTargetComponentId = Concepts.IS_A;
		
		final Multimap<AssociationType, String> parentAssociationTargets = ArrayListMultimap.create();
		parentAssociationTargets.put(AssociationType.SAME_AS, Concepts.MODULE_SCT_CORE);
		
		// inactivate concept and update association targets
		updateConcept(conceptId, branchPath, parentAssociationTargets);
		
		// create branch on top of the current branch
		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "child");
		SnomedBranchingRestRequests.createBranch(childBranch).statusCode(201);
		SnomedBranchingRestRequests.getBranch(childBranch).statusCode(200);
		
		final Multimap<AssociationType, String> childBranchAssociationTargets = ArrayListMultimap.create();
		
		childBranchAssociationTargets.put(AssociationType.SAME_AS, expectedTargetComponentId);
		
		// change association targets on child branch
		updateConcept(conceptId, childBranch, childBranchAssociationTargets);
		
		// merge them, merge should succeed (previously it had conflicts on these types of changes)
		merge(childBranch, branchPath, "Merge association target changes").body("status", equalTo(Merge.Status.COMPLETED.name()));
		
		final SnomedReferenceSetMembers sameAsAssociationMembersAfterMerge = getMembers(branchPath);
		
		sameAsAssociationMembersAfterMerge.forEach(member -> {
			final Object targetComponent = member.getProperties().get("targetComponent");
			if (targetComponent instanceof SnomedConcept) {
				final SnomedConcept targetConcept = (SnomedConcept) targetComponent;
				if (conceptId.equals(member.getReferencedComponentId())) {
					assertEquals(expectedTargetComponentId, targetConcept.getId());
				}
				
			}
			
		});
		
	}
	
	@Test
	public void testReleasedAssociationMemberTargetChange() {
		final String shortName = "SNOMEDCT-ASSOCIATION";
		createCodeSystem(branchPath, shortName).statusCode(201);
		final String conceptId = createNewConcept(branchPath);
		final String expectedTargetComponentId = Concepts.IS_A;
		final String originalTargetComponentId = Concepts.MODULE_SCT_CORE;
		
		final Multimap<AssociationType, String> parentAssociationTargets = ArrayListMultimap.create();
		parentAssociationTargets.put(AssociationType.SAME_AS, originalTargetComponentId);
		
		// inactivate concept and update association targets
		updateConcept(conceptId, branchPath, parentAssociationTargets);
		
		final String effectiveTime = getNextAvailableEffectiveDateAsString(shortName);
		createVersion(shortName, "2018-08-16", effectiveTime).statusCode(201);
		
		// create branch on top of the current branch
		final IBranchPath childBranch = BranchPathUtils.createPath(branchPath, "child");
		SnomedBranchingRestRequests.createBranch(childBranch).statusCode(201);
		SnomedBranchingRestRequests.getBranch(childBranch).statusCode(200);
			
		final Multimap<AssociationType, String> childBranchAssociationTargets = ArrayListMultimap.create();
		
		childBranchAssociationTargets.put(AssociationType.SAME_AS, expectedTargetComponentId);
			
		// change association targets on child branch
		updateConcept(conceptId, childBranch, childBranchAssociationTargets);
		
		merge(childBranch, branchPath, "Merge association target changes").body("status", equalTo(Merge.Status.COMPLETED.name()));
		
		final SnomedReferenceSetMembers sameAsAssociationMembersAfterMerge = getMembers(branchPath);
		
		for (SnomedReferenceSetMember member : sameAsAssociationMembersAfterMerge) {
			
			final Object targetComponent = member.getProperties().get("targetComponent");
			if (targetComponent instanceof SnomedConcept) {
				final SnomedConcept targetConcept = (SnomedConcept) targetComponent;
				if (conceptId.equals(member.getReferencedComponentId())) {
					if (originalTargetComponentId.equals(targetConcept.getId())) {
						assertEquals("The old member shouldn't be active ", false, member.isActive());
						assertEquals(true, member.isReleased());
					} else if (expectedTargetComponentId.equals(targetConcept.getId())) {
						assertEquals("The new member should be active ", true, member.isActive());
						assertEquals("The new members effective time shouldn't be set", null, member.getEffectiveTime());
						assertEquals(false, member.isReleased());
						
					}
				}
				
			}
		}
		
	}

	private SnomedReferenceSetMembers getMembers(IBranchPath branchPath) {
		final SnomedReferenceSetMembers sameAsAssociationMembersAfterMerge = SnomedRequests.prepareSearchMember()
				.all()
				.filterByRefSet(SAME_AS_REFSET_ID)
				.build(REPOSITORY_UUID, branchPath.getPath())
				.execute(getBus())
				.getSync();
		return sameAsAssociationMembersAfterMerge;
	}

	private void updateConcept(final String conceptId, final IBranchPath branchPath, final Multimap<AssociationType, String> newAssociationTargets) {
		SnomedRequests.prepareUpdateConcept(conceptId)
			.setActive(false)
			.setAssociationTargets(newAssociationTargets)
			.build(REPOSITORY_UUID, branchPath.getPath(), "info@b2international.com", "Update assocation target")
			.execute(getBus())
			.getSync();
	}

}
