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
package com.b2international.snowowl.datastore.request;

import java.util.UUID;

import org.hibernate.validator.constraints.NotEmpty;

import com.b2international.snowowl.core.branch.Branch;
import com.b2international.snowowl.core.branch.BranchManager;
import com.b2international.snowowl.core.domain.RepositoryContext;
import com.b2international.snowowl.core.events.Request;
import com.b2international.snowowl.core.exceptions.ConflictException;
import com.b2international.snowowl.core.exceptions.NotFoundException;
import com.b2international.snowowl.datastore.review.BranchState;
import com.b2international.snowowl.datastore.review.Review;
import com.b2international.snowowl.datastore.review.ReviewManager;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @since 4.6
 */
public abstract class AbstractBranchChangeRequest<R> implements Request<RepositoryContext, R> {

	@JsonProperty
	protected final UUID id;
	@JsonProperty
	@NotEmpty
	protected final String sourcePath;
	@JsonProperty
	@NotEmpty
	protected final String targetPath;
	@JsonProperty
	@NotEmpty
	protected final String userId;
	@JsonProperty
	@NotEmpty
	protected final String commitMessage;
	@JsonProperty
	protected final String reviewId;
	@JsonProperty
	protected final String parentLockContext;

	protected AbstractBranchChangeRequest(UUID id, String sourcePath, String targetPath, String userId, String commitMessage, String reviewId, String parentLockContext) {
		this.id = id;
		this.sourcePath = sourcePath;
		this.targetPath = targetPath;
		this.userId = userId;
		this.commitMessage = commitMessage;
		this.reviewId = reviewId;
		this.parentLockContext = parentLockContext;
	}

	@Override
	public R execute(RepositoryContext context) {
		
		try {
			final BranchManager branchManager = context.service(BranchManager.class);
			final Branch source = branchManager.getBranch(sourcePath);
			final Branch target = branchManager.getBranch(targetPath);
			
			if (reviewId != null) {
				final ReviewManager reviewManager = context.service(ReviewManager.class);
				final Review review = reviewManager.getReview(reviewId);
				final BranchState sourceState = review.source();
				final BranchState targetState = review.target();
				
				if (!sourceState.matches(source)) {
					throw new ConflictException("Source branch '%s' did not match with stored state on review identifier '%s'.", source.path(), reviewId);
				}
				
				if (!targetState.matches(target)) {
					throw new ConflictException("Target branch '%s' did not match with stored state on review identifier '%s'.", target.path(), reviewId);
				}
			}
			
			return execute(context, source, target);
						
		} catch (NotFoundException e) {
			throw e.toBadRequestException();
		}
	}

	protected abstract R execute(RepositoryContext context, Branch source, Branch target);

}
