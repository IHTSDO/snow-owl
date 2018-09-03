/*
 * Copyright 2011-2018 B2i Healthcare Pte Ltd, http://b2i.sg
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
package com.b2international.snowowl.snomed.api.impl.traceability;

import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;

import com.b2international.snowowl.core.branch.Branch;
import com.b2international.snowowl.datastore.ICDOCommitChangeSet;
import com.b2international.snowowl.datastore.cdo.CDOCommitInfoUtils;
import com.b2international.snowowl.snomed.api.impl.domain.browser.SnomedBrowserConcept;

/**
 * Represents a traceability entry.
 */
class TraceabilityEntry {
	
	private final String userId;
	private final String commitComment;
	private final String branchPath;
	private final long commitTimestamp;
	private final Map<String, ConceptWithChanges> changes = newHashMap();
	
	public TraceabilityEntry(final ICDOCommitChangeSet changeSet) {
		this.userId = changeSet.getUserId();
		this.commitComment = CDOCommitInfoUtils.removeUuidPrefix(changeSet.getCommitComment());
		this.branchPath = getBranchPath(changeSet);
		this.commitTimestamp = changeSet.getTimestamp();
	}

	public String getUserId() {
		return userId;
	}
	
	public String getCommitComment() {
		return commitComment;
	}

	public String getBranchPath() {
		return branchPath;
	}

	public long getCommitTimestamp() {
		return commitTimestamp;
	}

	public Map<String, ConceptWithChanges> getChanges() {
		return changes;
	}

	public void registerChange(final String conceptId, final TraceabilityChange componentChange) {
		if (!changes.containsKey(conceptId)) {
			changes.put(conceptId, new ConceptWithChanges());
		}
		
		changes.get(conceptId).addChange(componentChange);
	}

	public void setConcept(final String conceptId, final SnomedBrowserConcept convertedConcept) {
		changes.get(conceptId).setConcept(convertedConcept);
	}

	private String getBranchPath(final ICDOCommitChangeSet changeSet) {
		
		final String branchName = changeSet.getView().getBranch().getName();
		final String branchPath = changeSet.getView().getBranch().getPathName();
		
		return branchName.startsWith(Branch.TEMP_PREFIX) ? getBranchPath(branchName, branchPath) : branchPath;
	}

	private String getBranchPath(String tempBranchName, String branchPath) {
		return branchPath.replace(tempBranchName, Branch.BranchNameValidator.DEFAULT.getName(tempBranchName));
	}
}
