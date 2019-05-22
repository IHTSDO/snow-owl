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
import com.b2international.snowowl.snomed.api.impl.domain.browser.SnomedBrowserConcept;
import com.b2international.snowowl.snomed.api.mergereview.ISnomedBrowserMergeReviewDetail;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * @since 4.5
 */
public class SnomedBrowserMergeReviewDetail implements ISnomedBrowserMergeReviewDetail {

	@JsonDeserialize(as=SnomedBrowserConcept.class)
	private ISnomedBrowserConcept sourceConcept;
	
	@JsonDeserialize(as=SnomedBrowserConcept.class)
	private ISnomedBrowserConcept targetConcept;
	
	@JsonDeserialize(as=SnomedBrowserConcept.class)
	private ISnomedBrowserConcept autoMergedConcept;
	
	@JsonDeserialize(as=SnomedBrowserConcept.class)
	private ISnomedBrowserConcept manuallyMergedConcept;

	public SnomedBrowserMergeReviewDetail() {}
	
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

	public void setSourceConcept(ISnomedBrowserConcept sourceConcept) {
		this.sourceConcept = sourceConcept;
	}

	public void setTargetConcept(ISnomedBrowserConcept targetConcept) {
		this.targetConcept = targetConcept;
	}

	public void setAutoMergedConcept(ISnomedBrowserConcept autoMergedConcept) {
		this.autoMergedConcept = autoMergedConcept;
	}

	public void setManuallyMergedConcept(ISnomedBrowserConcept manuallyMergedConcept) {
		this.manuallyMergedConcept = manuallyMergedConcept;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SnomedBrowserMergeReviewDetail [");
		if (sourceConcept != null) {
			builder.append("sourceConcept=");
			builder.append(sourceConcept);
			builder.append(", ");
		}
		if (targetConcept != null) {
			builder.append("targetConcept=");
			builder.append(targetConcept);
			builder.append(", ");
		}
		if (autoMergedConcept != null) {
			builder.append("autoMergedConcept=");
			builder.append(autoMergedConcept);
			builder.append(", ");
		}
		if (manuallyMergedConcept != null) {
			builder.append("manuallyMergedConcept=");
			builder.append(manuallyMergedConcept);
		}
		builder.append("]");
		return builder.toString();
	}
	
}
