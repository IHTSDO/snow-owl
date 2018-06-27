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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class MergeReviewMixin {

    @JsonCreator
    private MergeReviewMixin(
    		@JsonProperty("id") final String id, 
    		@JsonProperty("sourcePath") final String sourcePath,
    		@JsonProperty("targetPath") final String targetPath, 
    		@JsonProperty("sourceToTargetReviewId") final String sourceToTargetReviewId, 
    		@JsonProperty("targetToSourceReviewId") final String targetToSourceReviewId) {
        // Empty constructor body for mixin
    }

    @JsonProperty
    public abstract String id();

    @JsonProperty
    public abstract String sourcePath();

    @JsonProperty
    public abstract String targetPath();

    @JsonProperty
    public abstract String sourceToTargetReviewId();

    @JsonProperty
    public abstract String targetToSourceReviewId();
}
