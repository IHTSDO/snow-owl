/*
 * Copyright 2011-2015 B2i Healthcare Pte Ltd, http://b2i.sg
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
package com.b2international.snowowl.datastore.server.events;

import com.b2international.snowowl.datastore.branch.Branch;

/**
 * Common success reply used when sending {@link BaseBranchEvent}s into the system.
 * 
 * @since 4.1
 */
public class BranchReply {

	private final Branch branch;

	public BranchReply(final Branch branch) {
		this.branch = branch;
	}

	public Branch getBranch() {
		return branch;
	}
}
