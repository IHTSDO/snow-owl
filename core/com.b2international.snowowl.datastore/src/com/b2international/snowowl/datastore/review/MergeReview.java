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
package com.b2international.snowowl.datastore.review;

import java.util.Set;

import com.b2international.index.Doc;
import com.google.common.collect.ImmutableSet;

/**
 * Represents a terminology review comparing bidirectional changes on branches while catering for manual merges.
 */
@Doc(type="merge_review")
public class MergeReview {

	public static final class Fields {
		public static final String ID = "id";
		public static final String STATUS = "status";
		public static final String SOURCE_PATH = "sourcePath";
		public static final String TARGET_PATH = "targetPath";
		public static final Set<String> ALL = ImmutableSet.of(ID, SOURCE_PATH, TARGET_PATH, STATUS);
	}
	
	private final String id;
	private final String sourcePath;
	private final String targetPath;
	private final String sourceToTargetReviewId;
	private final String targetToSourceReviewId;
	private final ReviewStatus status;
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static Builder builder(MergeReview mergeReviewToCopy) {
		return builder()
				.id(mergeReviewToCopy.id())
				.sourcePath(mergeReviewToCopy.sourcePath())
				.targetPath(mergeReviewToCopy.targetPath())
				.sourceToTargetReviewId(mergeReviewToCopy.sourceToTargetReviewId())
				.targetToSourceReviewId(mergeReviewToCopy.targetToSourceReviewId())
				.status(mergeReviewToCopy.status());
	}
	
	public static class Builder {
		
		private String id;
		private String sourcePath;
		private String targetPath;
		private String sourceToTargetReviewId;
		private String targetToSourceReviewId;
		private ReviewStatus status = ReviewStatus.PENDING; // default status
		
		public Builder id(String id) {
			this.id = id;
			return this;
		}
		
		public Builder sourcePath(String sourcePath) {
			this.sourcePath = sourcePath;
			return this;
		}
		
		public Builder targetPath(String targetPath) {
			this.targetPath = targetPath;
			return this;
		}
		
		public Builder sourceToTargetReviewId(String sourceToTargetReviewId) {
			this.sourceToTargetReviewId = sourceToTargetReviewId;
			return this;
		}
		
		public Builder targetToSourceReviewId(String targetToSourceReviewId) {
			this.targetToSourceReviewId = targetToSourceReviewId;
			return this;
		}
		
		public Builder status(ReviewStatus status) {
			this.status = status;
			return this;
		}
		
		public MergeReview build() {
			return new MergeReview(this.id, this.sourcePath, this.targetPath, this.sourceToTargetReviewId, this.targetToSourceReviewId, this.status);
		}
		
	}
	
	private MergeReview(final String id, final String sourcePath, final String targetPath, 
			final String sourceToTargetReviewId, final String targetToSourceReviewId, ReviewStatus status) {
		this.id = id;
		this.sourcePath = sourcePath;
		this.targetPath = targetPath;
		this.sourceToTargetReviewId = sourceToTargetReviewId;
		this.targetToSourceReviewId = targetToSourceReviewId;
		this.status = status;
	}
	
	/**
	 * Returns the unique identifier of this review.
	 */
	public String id() {
		return this.id;
	}

	/**
	 * Returns the branch used as the comparison target.
	 */
	public String targetPath() {
		return targetPath;
	}

	/**
	 * Returns the branch used as the comparison source.
	 */
	public String sourcePath() {
		return sourcePath;
	}
	
	/**
	 * Returns the unique identifier of the source to target review
	 */
	public String sourceToTargetReviewId() {
		return sourceToTargetReviewId;
	}

	/**
	 * Returns the unique identifier of the target to source review
	 */
	public String targetToSourceReviewId() {
		return targetToSourceReviewId;
	}

	/**
	 * Returns the current status of this merge review.
	 */
	public ReviewStatus status() {
		return status;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		MergeReview other = (MergeReview) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		return true;
	}

}
