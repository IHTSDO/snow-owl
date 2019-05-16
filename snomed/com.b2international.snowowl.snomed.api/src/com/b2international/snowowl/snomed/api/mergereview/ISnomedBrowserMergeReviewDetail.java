/*
 * Copyright 2019 B2i Healthcare Pte Ltd, http://b2i.sg
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
package com.b2international.snowowl.snomed.api.mergereview;

import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserConcept;

/**
 * @since 4.5
 */
public interface ISnomedBrowserMergeReviewDetail {

	ISnomedBrowserConcept getSourceConcept();

	ISnomedBrowserConcept getTargetConcept();

	ISnomedBrowserConcept getAutoMergedConcept();

	ISnomedBrowserConcept getManuallyMergedConcept();

	/**
	 * Special value indicating that the concept should not be added to the review, because it did not change (ignoring
	 * any changes related to classification).
	 */
	static final ISnomedBrowserMergeReviewDetail SKIP_DETAIL = new ISnomedBrowserMergeReviewDetail() {

		@Override
		public ISnomedBrowserConcept getTargetConcept() {
			throw new UnsupportedOperationException("getTargetConcept should not be called on empty merge review element.");
		}

		@Override
		public ISnomedBrowserConcept getSourceConcept() {
			throw new UnsupportedOperationException("getSourceConcept should not be called on empty merge review element.");
		}

		@Override
		public ISnomedBrowserConcept getManuallyMergedConcept() {
			throw new UnsupportedOperationException("getManuallyMergedConcept should not be called on empty merge review element.");
		}

		@Override
		public ISnomedBrowserConcept getAutoMergedConcept() {
			throw new UnsupportedOperationException("getAutoMergedConcept should not be called on empty merge review element.");
		}
	};

}
