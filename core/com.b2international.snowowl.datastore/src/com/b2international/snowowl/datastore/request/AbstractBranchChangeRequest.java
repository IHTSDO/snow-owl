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
package com.b2international.snowowl.datastore.request;

import com.b2international.snowowl.core.branch.Branch;
import com.b2international.snowowl.core.branch.BranchManager;
import com.b2international.snowowl.core.domain.RepositoryContext;
import com.b2international.snowowl.core.events.BaseRequest;
import com.b2international.snowowl.core.exceptions.ConflictException;
import com.b2international.snowowl.core.exceptions.NotFoundException;
import com.b2international.snowowl.datastore.review.Review;
import com.b2international.snowowl.datastore.review.ReviewManager;
import com.google.common.base.Strings;

/**
 * @since 4.6
 */
public abstract class AbstractBranchChangeRequest extends BaseRequest<RepositoryContext, Branch> {

	protected final String sourcePath;
	protected final String targetPath;
	protected final String commitMessage;
	protected final String reviewId;

	protected AbstractBranchChangeRequest(String sourcePath, String targetPath, String commitMessage, String reviewId) {
		this.sourcePath = sourcePath;
		this.targetPath = targetPath;
		this.commitMessage = commitMessage;
		this.reviewId = reviewId;
	}

	@Override
	public Branch execute(RepositoryContext context) {
		try {
			final BranchManager branchManager = context.service(BranchManager.class);
			final ReviewManager reviewManager = context.service(ReviewManager.class);
			
			final Branch source = branchManager.getBranch(sourcePath);
			final Branch target = branchManager.getBranch(targetPath);
			
			if (reviewId != null) {
				Review review = reviewManager.getReview(reviewId);
				com.b2international.snowowl.datastore.review.BranchState sourceState = review.source();
				com.b2international.snowowl.datastore.review.BranchState targetState = review.target();
				
				if (!sourceState.matches(source)) {
					throw new ConflictException("Source branch '%s' did not match with stored state on review identifier '%s'.", source.path(), reviewId);
				}
				
				if (!targetState.matches(target)) {
					throw new ConflictException("Target branch '%s' did not match with stored state on review identifier '%s'.", target.path(), reviewId);
				}
			}
			
			return executeChange(context, source, target);
						
		} catch (NotFoundException e) {
			throw e.toBadRequestException();
		}
	}

	protected abstract Branch executeChange(RepositoryContext context, Branch source, Branch target);

	@Override
	protected Class<Branch> getReturnType() {
		return Branch.class;
	}

	@Override
	public String toString() {
		return String.format("{type:%s, source:%s, target:%s, commitMessage:%s, reviewId:%s}", 
				getClass().getSimpleName(),
				sourcePath,
				targetPath,
				commitMessage,
				reviewId);
	}
}
