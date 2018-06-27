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
package com.b2international.snowowl.datastore.server.internal.review;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

import com.b2international.index.DocSearcher;
import com.b2international.index.Index;
import com.b2international.index.IndexRead;
import com.b2international.index.IndexWrite;
import com.b2international.index.Writer;
import com.b2international.snowowl.core.branch.Branch;
import com.b2international.snowowl.core.exceptions.NotFoundException;
import com.b2international.snowowl.datastore.internal.InternalRepository;
import com.b2international.snowowl.datastore.review.MergeReview;
import com.b2international.snowowl.datastore.review.MergeReviewManager;
import com.b2international.snowowl.datastore.review.Review;
import com.b2international.snowowl.datastore.review.ReviewStatus;
import com.google.common.collect.ImmutableMap;

/**
 * 
 */
public class MergeReviewManagerImpl implements MergeReviewManager {

	private final ReviewManagerImpl reviewManager;
	private final Index store;

	public MergeReviewManagerImpl(InternalRepository repository, ReviewManagerImpl reviewManager) {
		this.store = repository.getIndex();
		this.reviewManager = reviewManager;
	}
	
	@Override
	public MergeReview createMergeReview(Branch source, Branch target) {	
		Review sourceToTarget = reviewManager.createReview(source, target);
		Review targetToSource = reviewManager.createReview(target, source);

		MergeReview mergeReview = MergeReview.builder()
			.id(UUID.randomUUID().toString())
			.sourcePath(source.path())
			.targetPath(target.path())
			.sourceToTargetReviewId(sourceToTarget.id())
			.targetToSourceReviewId(targetToSource.id())
			.build();
		
		put(mergeReview);
		
		return mergeReview;
	}

	@Override
	public MergeReview getMergeReview(String id) {
		
		MergeReview mergeReview = get(id);
		ReviewStatus newMergeReviewStatus = getMergeReviewStatus(mergeReview);
		
		if (mergeReview.status() != newMergeReviewStatus) { // FIXME
			
			MergeReview updatedMergeReview = MergeReview.builder(mergeReview)
				.status(newMergeReviewStatus)
				.build();
			
			put(updatedMergeReview);
			
			mergeReview = updatedMergeReview;
		}
		
		return mergeReview;
	}
	
	private ReviewStatus getMergeReviewStatus(MergeReview currentMergeReview) {
		try {
			Review left = reviewManager.getReview(currentMergeReview.sourceToTargetReviewId());
			Review right = reviewManager.getReview(currentMergeReview.targetToSourceReviewId());
			if (left.status() == ReviewStatus.FAILED || right.status() == ReviewStatus.FAILED) {
				return ReviewStatus.FAILED;
			} else if (left.status() == ReviewStatus.PENDING || right.status() == ReviewStatus.PENDING) {
				return ReviewStatus.PENDING;
			} else if (left.status() == ReviewStatus.STALE || right.status() == ReviewStatus.STALE) {
				return ReviewStatus.STALE;
			} else  if (left.status() == ReviewStatus.CURRENT && right.status() == ReviewStatus.CURRENT) {
				return ReviewStatus.CURRENT;
			} else {
				throw new IllegalStateException("Unexpected state combination: " + left.status() + " / " + right.status());
			}
		} catch (NotFoundException e) {
			return ReviewStatus.STALE;
		}
	}

	@Override
	public void deleteMergeReview(String mergeReviewId) {
		MergeReview mergeReview = get(mergeReviewId);
		reviewManager.delete(mergeReview.sourceToTargetReviewId());
		reviewManager.delete(mergeReview.targetToSourceReviewId());
		delete(mergeReview.id());
	}

	private void put(final MergeReview mergeReview) {
		store.write(new IndexWrite<Void>() {
			@Override
			public Void execute(Writer index) throws IOException {
				index.put(mergeReview.id(), mergeReview);
				index.commit();
				return null;
			}
		});
	}
	
	private MergeReview get(final String id) {
		MergeReview mergeReview = store.read(new IndexRead<MergeReview>() {
			@Override
			public MergeReview execute(DocSearcher index) throws IOException {
				return index.get(MergeReview.class, id);
			}
		});
		
		if (mergeReview == null) {
			throw new NotFoundException(MergeReview.class.getSimpleName(), id);
		}
		
		return mergeReview;
	}
	
	private void delete(final String mergeReviewId) {
		store.write(new IndexWrite<Void>() {
			@Override
			public Void execute(Writer index) throws IOException {
				index.removeAll(ImmutableMap.of(MergeReview.class, Collections.singleton(mergeReviewId)));
				index.commit();
				return null;
			}
		});
	}
}
