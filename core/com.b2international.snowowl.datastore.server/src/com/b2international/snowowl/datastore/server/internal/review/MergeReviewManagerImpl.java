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

import static com.google.common.collect.Sets.newHashSet;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import com.b2international.index.Index;
import com.b2international.index.IndexRead;
import com.b2international.index.IndexWrite;
import com.b2international.index.Searcher;
import com.b2international.index.Writer;
import com.b2international.snowowl.core.branch.Branch;
import com.b2international.snowowl.core.exceptions.NotFoundException;
import com.b2international.snowowl.datastore.internal.InternalRepository;
import com.b2international.snowowl.datastore.review.ConceptChanges;
import com.b2international.snowowl.datastore.review.MergeReview;
import com.b2international.snowowl.datastore.review.MergeReviewManager;
import com.b2international.snowowl.datastore.review.Review;
import com.b2international.snowowl.datastore.review.ReviewStatus;
import com.google.common.collect.ImmutableMap;

/**
 * 
 */
public class MergeReviewManagerImpl implements MergeReviewManager {

	private final ReviewManagerImpl reviews;
	private final Index index;

	public MergeReviewManagerImpl(InternalRepository repository, ReviewManagerImpl reviews) {
		this.index = repository.getIndex();
		this.reviews = reviews;
	}
	
	@Override
	public MergeReview createMergeReview(Branch source, Branch target) {	
		Review sourceToTarget = reviews.createReview(source, target);
		Review targetToSource = reviews.createReview(target, source);

		MergeReviewImpl mergeReview = new MergeReviewImpl(UUID.randomUUID().toString(), source.path(), target.path(), sourceToTarget.id(), targetToSource.id());
		mergeReview.setMergeReviewManager(this);
		mergeReview.setReviewManager(reviews);

		put(mergeReview);
		
		return mergeReview;
	}

	@Override
	public MergeReview getMergeReview(String id) {
		final MergeReviewImpl mergeReview = get(id);

		if (mergeReview == null) {
			throw new NotFoundException(MergeReview.class.getSimpleName(), id);
		} 

		mergeReview.setMergeReviewManager(this);
		mergeReview.setReviewManager(reviews);
		return mergeReview;
	}
	
	@Override
	public Set<String> getMergeReviewIntersection(MergeReview mergeReview) {
		// Get the concept changes for both source to target 
		// and target to source reviews
		
		// Are we all complete and still relevant?
		if (!mergeReview.status().equals(ReviewStatus.CURRENT)){
			throw new IllegalStateException ("Merge Review in invalid state - " + mergeReview.status());
		}
		
		ConceptChanges sourceChanges = reviews.getConceptChanges(mergeReview.sourceToTargetReviewId());
		ConceptChanges targetChanges = reviews.getConceptChanges(mergeReview.targetToSourceReviewId());
		
		Set<String>commonChanges = newHashSet(sourceChanges.changedConcepts());
		// If concepts are new then they won't intersect.  Also if they're deleted, they won't
		// conflict, so we're only interested in the change set.
		commonChanges.retainAll(targetChanges.changedConcepts());
		
		return commonChanges;
	}
	
	public MergeReview deleteMergeReview(MergeReviewImpl mergeReview) {
		Review sourceToTargetReview = reviews.getReview(mergeReview.sourceToTargetReviewId());
		Review targetToSourceReview = reviews.getReview(mergeReview.targetToSourceReviewId());
		sourceToTargetReview.delete();
		targetToSourceReview.delete();
		
		remove(mergeReview.id());
		
		return mergeReview;
	}

	private void put(final MergeReviewImpl newMergeReview) {
		index.write(new IndexWrite<Void>() {
			@Override
			public Void execute(Writer index) throws IOException {
				index.put(newMergeReview.id(), newMergeReview);
				index.commit();
				return null;
			}
		});
	}
	
	private MergeReviewImpl get(final String id) {
		return index.read(new IndexRead<MergeReviewImpl>() {
			@Override
			public MergeReviewImpl execute(Searcher index) throws IOException {
				return index.get(MergeReviewImpl.class, id);
			}
		});
	}
	
	private void remove(final String id) {
		index.write(new IndexWrite<Void>() {
			@Override
			public Void execute(Writer index) throws IOException {
				index.removeAll(ImmutableMap.<Class<?>, Set<String>>of(MergeReviewImpl.class, Collections.singleton(id)));
				index.commit();
				return null;
			}
		});
	}
}
