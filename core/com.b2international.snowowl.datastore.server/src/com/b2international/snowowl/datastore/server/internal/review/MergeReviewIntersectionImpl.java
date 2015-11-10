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
package com.b2international.snowowl.datastore.server.internal.review;

import java.util.Set;

import com.b2international.snowowl.datastore.server.review.MergeReviewIntersection;

/**
 * @since 4.2
 */
public class MergeReviewIntersectionImpl implements MergeReviewIntersection {

	private String id;
	
	private Set<String> intersectingConcepts;
	
	private String sourceBranch;
	
	private String targetBranch;

	public String id() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Set<String> getIntersectingConcepts() {
		return intersectingConcepts;
	}

	public void setIntersectingConcepts(Set<String> intersectingConcepts) {
		this.intersectingConcepts = intersectingConcepts;
	}

	public String getSourceBranch() {
		return sourceBranch;
	}

	public void setSourceBranch(String sourceBranch) {
		this.sourceBranch = sourceBranch;
	}

	public String getTargetBranch() {
		return targetBranch;
	}

	public void setTargetBranch(String targetBranch) {
		this.targetBranch = targetBranch;
	}

}
