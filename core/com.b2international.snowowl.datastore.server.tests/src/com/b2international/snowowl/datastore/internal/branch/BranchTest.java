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
package com.b2international.snowowl.datastore.internal.branch;

import static com.b2international.snowowl.datastore.internal.branch.BranchAssertions.assertState;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;

import com.b2international.snowowl.core.MetadataImpl;
import com.b2international.snowowl.core.branch.Branch;
import com.b2international.snowowl.core.branch.Branch.BranchState;
import com.b2international.snowowl.core.exceptions.BadRequestException;

/**
 * @since 4.1
 */
public class BranchTest {

	private AtomicLongTimestampAuthority clock = new AtomicLongTimestampAuthority();
	private BranchManagerImpl manager;
	private InternalBranch main;
	private InternalBranch branchA;
	private String testBranchName;
	
	@Before
	public void before() {
		manager = mock(BranchManagerImpl.class);
		main = new MainBranchImpl(currentTimestamp());
		main.setBranchManager(manager);
		branchA = createBranch(main, "a");
		testBranchName = "TEST";
	}
	
	private long currentTimestamp() {
		return clock.getTimestamp();
	}
	
	@Test(expected=BadRequestException.class)
	public void createWithNameShouldNotContainSeparator() {
		createBranch(main, "a/s");
	}

	@Test(expected=BadRequestException.class)
	public void createWithNameShouldNotBeEmpty() {
		createBranch(main, "");
	}

	@Test(expected=BadRequestException.class)
	public void createWithNameShouldNotHaveInvalidCharacters() {
		createBranch(main, "?b");
	}

	@Test(expected=BadRequestException.class)
	public void createWithNameShouldNotHaveLongerThan50Char() {
		createBranch(main, "123456789012345678901234567890123456789012345678901");
	}

	@Test
	public void createWith50CharName() {
		createBranch(main, "12345678901234567890123456789012345678901234567890");
	}

	@Test
	public void createWithMainChildPath() {
		Branch branch = createBranch(main, "p1");
		BranchAssertions.assertPath(branch, "MAIN/p1");
	}
	
	@Test
	public void createWithSamePathAndBaseTimestampShouldBeEqual() {
		Branch branch1 = createBranch(main, "p1");
		Branch branch2 = createBranch(main, "p1", branch1.baseTimestamp());
		assertEquals(branch1, branch2);
	}

	@Test
	public void createWithDifferentPathsButSameBasetimestampsShouldNotBeEqual() {
		Branch branch1 = createBranch(main, "p1");
		Branch branch2 = createBranch(main, "p2");
		assertNotEquals(branch1, branch2);
	}
	
	@Test
	public void createWithSamePathButDifferentBasetimestampShouldNotBeEqual() throws Exception {
		Branch branch1 = createBranch(main, "p1");
		Branch branch2 = createBranch(main, "p1");
		assertNotEquals(branch1, branch2);
	}
	
	@Test
	public void createWithDifferentPathAndBaseTimestampShouldNotBeEqual() throws Exception {
		Branch branch1 = createBranch(main, "p1");
		Branch branch2 = createBranch(main, "p2");
		assertNotEquals(branch1, branch2);
	}
	
	@Test
	public void createWithPathName() {
		Branch branch2 = createBranch(branchA, "b");
		assertEquals("a", branchA.name());
		assertEquals("b", branch2.name());
	}

	@Test
	public void initialStateIsUpToDate() {
		assertState(branchA, main, BranchState.UP_TO_DATE);
	}
	
	@Test
	public void testForwardState() throws Exception {
		assertState(commit(branchA), main, BranchState.FORWARD);
	}

	@Test
	public void testBehindState() throws Exception {
		assertState(branchA, commit(main), BranchState.BEHIND);
	}

	@Test
	public void testDivergedState() throws Exception {
		assertState(commit(branchA), commit(main), BranchState.DIVERGED);
	}
	
	@Test
	public void testSuccessOfCheckNameWithValidTempBranchName() {
		final String tempBranchName = createTempBranchName(testBranchName);
		Branch.BranchNameValidator.DEFAULT.checkName(tempBranchName);
	}
	
	@Test
	public void testSuccessOfCheckNameWithValidBranchName() {
		Branch.BranchNameValidator.DEFAULT.checkName(testBranchName);
	}
	
	@Test(expected = BadRequestException.class)
	public void testFailureOfCheckNameWithMalformedTempBranchName() {
		final String tempBranchName = createTempBranchName("TEST$$");
		Branch.BranchNameValidator.DEFAULT.checkName(tempBranchName);
	}
	
	@Test(expected = BadRequestException.class)
	public void testFailureOfCheckNameWithEmptyBranchName() {
		Branch.BranchNameValidator.DEFAULT.checkName("");
	}
	
	@Test(expected = BadRequestException.class)
	public void testFailureOfCheckNameWithMalformedBranchName() {
		Branch.BranchNameValidator.DEFAULT.checkName("/TEST");
	}
	
	@Test
	public void testSuccessOfGetNameWithValidBranchName() {
		final String result = Branch.BranchNameValidator.DEFAULT.getName(testBranchName);
		assertEquals(testBranchName, result);
	}
	
	@Test
	public void testSuccessOfGetNameWithValidTempBranchName() {
		final String tempBranchName = createTempBranchName(testBranchName);
		final String result = Branch.BranchNameValidator.DEFAULT.getName(tempBranchName);
		assertEquals(testBranchName, result);
	}
	
	@Test(expected = BadRequestException.class)
	public void testFailureOfGetNameWithMalformedBranchName() {
		Branch.BranchNameValidator.DEFAULT.getName("TEST$$");
	}
	
	@Test(expected = BadRequestException.class)
	public void testFailureOfGetNameWithEmptyBranchName() {
		Branch.BranchNameValidator.DEFAULT.getName("");
	}
	
	@Test(expected = BadRequestException.class)
	public void testFailureOfGetNameWithMalformedTempBranchName() {
		final String tempBranchName = createTempBranchName("$TEST");
		Branch.BranchNameValidator.DEFAULT.getName(tempBranchName);
	}
	
	private String createTempBranchName(String name) {
		return String.format(Branch.TEMP_BRANCH_NAME_FORMAT, Branch.TEMP_PREFIX, name, System.currentTimeMillis());
	}

	private InternalBranch commit(InternalBranch branch) {
		return branch.withHeadTimestamp(currentTimestamp());		
	}
	
	private InternalBranch createBranch(Branch parent, String name) {
		return createBranch(parent, name, currentTimestamp());
	}

	private InternalBranch createBranch(Branch parent, String name, long currentTimestamp) {
		final BranchImpl branch = new BranchImpl(name, parent.path(), currentTimestamp, new MetadataImpl());
		branch.setBranchManager(manager);
		return branch;
	}
}
