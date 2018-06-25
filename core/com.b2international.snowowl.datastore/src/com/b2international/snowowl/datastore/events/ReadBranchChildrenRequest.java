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
package com.b2international.snowowl.datastore.events;

import java.util.Collection;
import java.util.List;

import com.b2international.snowowl.core.branch.Branch;
import com.b2international.snowowl.core.branch.BranchManager;
import com.b2international.snowowl.core.branch.Branches;
import com.b2international.snowowl.core.domain.RepositoryContext;
import com.google.common.collect.ImmutableList;

/**
 * @since 4.1
 */
public final class ReadBranchChildrenRequest extends BranchRequest<Branches> {
	
	private boolean immediateChildrenOnly;
	
	public ReadBranchChildrenRequest(String branchPath, boolean immediateChildrenOnly) {
		super(branchPath);
		this.immediateChildrenOnly = immediateChildrenOnly;
	}

	@Override
	public Branches execute(RepositoryContext context) {
		final Branch branch = context.service(BranchManager.class).getBranch(getBranchPath());
		if (immediateChildrenOnly) {
			Collection<? extends Branch> immediateChildren = branch.immediateChildren();
			return new Branches(ImmutableList.copyOf(immediateChildren), null, null, immediateChildren.size(), immediateChildren.size());
		}
		final List<Branch> children = ImmutableList.copyOf(branch.children());
		return new Branches(children, null, null, children.size(), children.size());
	}
}
