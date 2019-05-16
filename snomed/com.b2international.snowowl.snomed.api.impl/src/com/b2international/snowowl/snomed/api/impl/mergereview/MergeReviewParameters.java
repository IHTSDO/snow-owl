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

import java.util.List;

import com.b2international.commons.http.ExtendedLocale;

/**
 * @since 4.5
 */
public class MergeReviewParameters {

	private final String sourcePath;
	private final String targetPath;
	private final List<ExtendedLocale> extendedLocales;
	private final String mergeReviewId;

	protected MergeReviewParameters(final String sourcePath, final String targetPath, final List<ExtendedLocale> extendedLocales, final String mergeReviewId) {
		this.sourcePath = sourcePath;
		this.targetPath = targetPath;
		this.extendedLocales = extendedLocales;
		this.mergeReviewId = mergeReviewId;
	}

	public String getSourcePath() {
		return sourcePath;
	}

	public String getTargetPath() {
		return targetPath;
	}

	public List<ExtendedLocale> getExtendedLocales() {
		return extendedLocales;
	}

	public String getMergeReviewId() {
		return mergeReviewId;
	}

}
