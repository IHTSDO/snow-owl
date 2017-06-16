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
package com.b2international.snowowl.snomed.api.rest.branches;

import static com.b2international.snowowl.datastore.BranchPathUtils.createPath;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;

import java.util.Map;

import org.junit.Test;

import com.b2international.snowowl.core.api.IBranchPath;
import com.b2international.snowowl.datastore.BranchPathUtils;
import com.b2international.snowowl.snomed.api.rest.AbstractSnomedApiTest;
import com.b2international.snowowl.snomed.api.rest.SnomedBranchingRestRequests;
import com.google.common.collect.ImmutableMap;

/**
 * @since 2.0
 */
public class SnomedBranchingApiTest extends AbstractSnomedApiTest {

	@Test
	public void readNonExistentBranch() {
		SnomedBranchingRestRequests.getBranch(createPath("MAIN/x/y/z")).statusCode(404);
	}

	@Test
	public void createBranchWithNonexistentParent() {
		SnomedBranchingRestRequests.createBranch(createPath("MAIN/x/y/z")).statusCode(400);
	}

	@Test
	public void updateMetadata() throws Exception {
		SnomedBranchingRestRequests.updateBranch(branchPath, ImmutableMap.of("key", "value"));
		SnomedBranchingRestRequests.getBranch(branchPath).body("metadata.key", equalTo("value"));
	}

	@Test
	public void updateMainMetadata() throws Exception {
		// XXX: modifies MAIN branch, may affect other tests
		SnomedBranchingRestRequests.updateBranch(BranchPathUtils.createMainPath(), ImmutableMap.of("key", "value"));
		SnomedBranchingRestRequests.getBranch(BranchPathUtils.createMainPath()).body("metadata.key", equalTo("value"));
	}

	@Test
	public void createChildBranch() {
		IBranchPath a = BranchPathUtils.createPath(branchPath, "a");
		SnomedBranchingRestRequests.createBranch(a).statusCode(201);
		SnomedBranchingRestRequests.getBranch(a).statusCode(200);
	}

	@Test
	public void createChildBranchWithMetadata() {
		IBranchPath a = BranchPathUtils.createPath(branchPath, "a");
		SnomedBranchingRestRequests.createBranch(a, ImmutableMap.of("key", "value")).statusCode(201);
		SnomedBranchingRestRequests.getBranch(a).statusCode(200).body("metadata.key", equalTo("value"));
	}

	@Test
	public void createBranchWithMetadataAndUpdateMetadata() {
		final String description = "Description of branch";
		final Map<String, String> metadata = ImmutableMap.of("description", description);
		
		
		IBranchPath a = BranchPathUtils.createPath(branchPath, "a");
		SnomedBranchingRestRequests.createBranch(a, metadata).statusCode(201);
		SnomedBranchingRestRequests.getBranch(a).statusCode(200).body("metadata.description", equalTo(description));
		
		final String descriptionUpdated = "Description of branch";
		final String title = "Title of branch";
		
		final Map<?, ?> metadataUpdated = ImmutableMap.of("description", descriptionUpdated, "title", title);
		SnomedBranchingRestRequests.updateBranch(a, metadataUpdated);
		
		SnomedBranchingRestRequests.getBranch(a)
			.body("metadata.description", equalTo(descriptionUpdated))
			.body("metadata.title", equalTo(title));
	}

	@Test
	public void createBranchTwice() {
		IBranchPath a = BranchPathUtils.createPath(branchPath, "a");
		SnomedBranchingRestRequests.createBranch(a).statusCode(201);
		SnomedBranchingRestRequests.createBranch(a).statusCode(409);
	}

	@Test
	public void deleteChildBranch() {
		IBranchPath a = BranchPathUtils.createPath(branchPath, "a");
		SnomedBranchingRestRequests.createBranch(a).statusCode(201);
		SnomedBranchingRestRequests.deleteBranch(a).statusCode(204);
		SnomedBranchingRestRequests.getBranch(a).statusCode(200).body("deleted", equalTo(true));
	}

	@Test
	public void deleteBranchRecursively() {
		IBranchPath a = createPath(branchPath, "a");
		IBranchPath b = createPath(a, "b");

		SnomedBranchingRestRequests.createBranchRecursively(b);
		SnomedBranchingRestRequests.getBranch(b).statusCode(200);
		SnomedBranchingRestRequests.getBranch(a).statusCode(200);

		SnomedBranchingRestRequests.deleteBranch(a).statusCode(204);
		SnomedBranchingRestRequests.getBranch(a).statusCode(200).body("deleted", equalTo(true));
		SnomedBranchingRestRequests.getBranch(b).statusCode(200).body("deleted", equalTo(true));
	}

	@Test
	public void createBranchOnDeletedBranch() {
		IBranchPath a = createPath(branchPath, "a");
		IBranchPath b = createPath(a, "b");

		SnomedBranchingRestRequests.createBranch(a).statusCode(201);
		SnomedBranchingRestRequests.deleteBranch(a).statusCode(204);

		SnomedBranchingRestRequests.createBranch(b).statusCode(400);
	}

	@Test
	public void getChildren() {
		IBranchPath a = createPath(branchPath, "a");
		IBranchPath b = createPath(a, "b");

		SnomedBranchingRestRequests.createBranchRecursively(b);

		SnomedBranchingRestRequests.getAllBranches().statusCode(200)
				.body("items.name", hasItem(a.lastSegment()))
				.body("items.name", hasItem(b.lastSegment()));

		SnomedBranchingRestRequests.getBranchChildren(branchPath).statusCode(200)
				.body("items.name", hasItem(a.lastSegment()))
				.body("items.name", hasItem(b.lastSegment()));
	}

}
