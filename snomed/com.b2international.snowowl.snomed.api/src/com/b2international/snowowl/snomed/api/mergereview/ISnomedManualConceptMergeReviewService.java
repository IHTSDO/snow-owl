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
public interface ISnomedManualConceptMergeReviewService {

	boolean exists(String branchPath, String mergeReviewId, String conceptId);

	ISnomedBrowserConcept retrieve(String branchPath, String mergeReviewId, String conceptId);

	void storeChanges(String branchPath, String mergeRewiewId, ISnomedBrowserConcept concept);

	boolean deleteAll(String branchPath, String mergeReviewId);

}
