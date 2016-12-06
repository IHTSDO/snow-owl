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
package com.b2international.snowowl.datastore.events;

import java.util.Collection;

import com.b2international.commons.collections.Collections3;
import com.b2international.snowowl.core.events.RepositoryEvent;

/**
 * @since 5.0
 */
public final class RepositoryCommitNotification extends RepositoryEvent {
	
	private static final long serialVersionUID = 1L;
	
	private final String branchPath;
	private final long commitTimestamp;
	private final String userId;
	private final String comment;
	private final Collection<String> newComponents;
	private final Collection<String> changedComponents;
	private final Collection<String> deletedComponents;

	public RepositoryCommitNotification(final String repositoryId,
			final String branchPath,
			final long commitTimestamp,
			final String userId,
			final String comment,
			final Collection<String> newComponents, 
			final Collection<String> changedComponents, 
			final Collection<String> deletedComponents) {
		super(repositoryId);
		this.branchPath = branchPath;
		this.commitTimestamp = commitTimestamp;
		this.userId = userId;
		this.comment = comment;
		this.newComponents = Collections3.toImmutableSet(newComponents);
		this.changedComponents = Collections3.toImmutableSet(changedComponents);
		this.deletedComponents = Collections3.toImmutableList(deletedComponents);
	}

	public String getBranchPath() {
		return branchPath;
	}
	
	public long getCommitTimestamp() {
		return commitTimestamp;
	}
	
	public String getUserId() {
		return userId;
	}
	
	public String getComment() {
		return comment;
	}
	
	public Collection<String> getNewComponents() {
		return newComponents;
	}
	
	public Collection<String> getChangedComponents() {
		return changedComponents;
	}
	
	public Collection<String> getDeletedComponents() {
		return deletedComponents;
	}
	
}
