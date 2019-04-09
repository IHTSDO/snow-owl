/*
 * Copyright 2017 B2i Healthcare Pte Ltd, http://b2i.sg
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
package com.b2international.snowowl.snomed.api.rest;

import static com.b2international.snowowl.snomed.api.rest.SnomedApiTestConstants.SCT_API;
import static com.b2international.snowowl.test.commons.rest.RestExtensions.givenAuthenticatedRequest;
import static com.b2international.snowowl.test.commons.rest.RestExtensions.lastPathSegment;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Set;

import com.b2international.snowowl.core.api.IBranchPath;
import com.b2international.snowowl.datastore.review.ReviewStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;

/**
 * @since 5.10.6
 */
public abstract class SnomedMergeReviewingRestRequests {

	private static final Set<String> FINISH_STATES = ImmutableSet.of(
			ReviewStatus.CURRENT.name(), 
			ReviewStatus.FAILED.name(), 
			ReviewStatus.STALE.name());

	public static ValidatableResponse createMergeReview(IBranchPath sourcePath, IBranchPath targetPath) {
		return createMergeReview(sourcePath.getPath(), targetPath.getPath());
	}

	public static ValidatableResponse createMergeReview(String source, String target) {
				ImmutableMap.Builder<String, Object> requestBuilder = ImmutableMap.<String, Object>builder()
				.put("source", source)
				.put("target", target);
		
		return givenAuthenticatedRequest(SCT_API)
				.contentType(ContentType.JSON)
				.body(requestBuilder.build())
				.post("/merge-reviews")
				.then();
	}
	
	

	public static String reviewLocation(ValidatableResponse response) {
		final String location = response
			.statusCode(201)
			.header("Location", notNullValue())
			.extract().header("Location");

		return lastPathSegment(location);
	}

	public static ValidatableResponse getMergeReview(String id) {
		return givenAuthenticatedRequest(SnomedApiTestConstants.SCT_API)
				.when()
				.get("/merge-reviews/{id}", id)
				.then();
	}
	
	public static JsonNode getMergeReviewDetailsResponse(String reviewId) {
		return givenAuthenticatedRequest(SnomedApiTestConstants.SCT_API)
				.with().header("Accept-Language", "en-GB")
				.when()
				.get("/merge-reviews/{id}/details", reviewId)
				.then()
				.statusCode(200)
				.extract()
				.body()
				.as(JsonNode.class);
	}
	
	
	public static void storeConceptForMergeReview(String reviewId, String conceptId, JsonNode concept) {
		givenAuthenticatedRequest(SnomedApiTestConstants.SCT_API)
		.with().header("Accept-Language", "en-GB")
		.content(concept)
		.contentType(ContentType.JSON)
		.when()
		.post("/merge-reviews/{id}/{conceptId}", reviewId, conceptId)
		.then()
		.statusCode(200);
	}
	
	public static void mergeAndApply(String reviewId) {
		givenAuthenticatedRequest(SnomedApiTestConstants.SCT_API)
		.with().header("Accept-Language", "en-GB")
		.when()
		.post("/merge-reviews/{id}/apply", reviewId)
		.then()
		.statusCode(204);
	}
	
	public static ValidatableResponse waitForMergeReviewJob(String id) {

		long endTime = System.currentTimeMillis() + SnomedApiTestConstants.POLL_TIMEOUT;
		long currentTime;
		ValidatableResponse response = null;
		String mergeStatus = null;

		do {

			try {
				Thread.sleep(SnomedApiTestConstants.POLL_INTERVAL);
			} catch (InterruptedException e) {
				fail(e.toString());
			}

			response = getMergeReview(id).statusCode(200);
			mergeStatus = response.extract().path("status");
			currentTime = System.currentTimeMillis();

		} while (!FINISH_STATES.contains(mergeStatus) && currentTime < endTime);

		assertNotNull(response);
		return response;
	}

	private SnomedMergeReviewingRestRequests() {
		throw new UnsupportedOperationException("This class is not supposed to be instantiated.");
	}
}
