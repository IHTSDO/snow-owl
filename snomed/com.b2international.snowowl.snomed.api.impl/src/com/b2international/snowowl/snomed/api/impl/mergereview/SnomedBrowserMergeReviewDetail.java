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
package com.b2international.snowowl.snomed.api.impl.mergereview;

import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserConcept;
import com.b2international.snowowl.snomed.api.mergereview.ISnomedBrowserMergeReviewDetail;

/**
 * @since 4.5
 */
public class SnomedBrowserMergeReviewDetail implements ISnomedBrowserMergeReviewDetail {

	private final ISnomedBrowserConcept sourceConcept;
	private final ISnomedBrowserConcept targetConcept;
	private final ISnomedBrowserConcept autoMergedConcept;
	private final ISnomedBrowserConcept manuallyMergedConcept;

	public SnomedBrowserMergeReviewDetail(final ISnomedBrowserConcept sourceConcept,
			final ISnomedBrowserConcept targetConcept,
			final ISnomedBrowserConcept autoMergedConcept,
			final ISnomedBrowserConcept manuallyMergedConcept) {
		this.sourceConcept = sourceConcept;
		this.targetConcept = targetConcept;
		this.autoMergedConcept = autoMergedConcept;
		this.manuallyMergedConcept = manuallyMergedConcept;
	}

	@Override
	public ISnomedBrowserConcept getSourceConcept() {
		return sourceConcept;
	}

	@Override
	public ISnomedBrowserConcept getTargetConcept() {
		return targetConcept;
	}

	@Override
	public ISnomedBrowserConcept getAutoMergedConcept() {
		return autoMergedConcept;
	}

	@Override
	public ISnomedBrowserConcept getManuallyMergedConcept() {
		return manuallyMergedConcept;
	}

}
