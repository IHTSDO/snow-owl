/*
 * Copyright 2018 B2i Healthcare Pte Ltd, http://b2i.sg
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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import com.b2international.snowowl.core.branch.Branch;
import com.b2international.snowowl.datastore.review.MergeReview;
import com.b2international.snowowl.datastore.review.ReviewStatus;
import com.b2international.snowowl.datastore.server.internal.JsonSupport;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 * @since 6.5 
 */
public class MergeReviewSerializationTest {

	private MergeReview mergeReview;
	private ObjectMapper mapper = JsonSupport.getDefaultObjectMapper();
	
	@Before
	public void before() {
		
		final Branch source = mock(Branch.class);
		when(source.path()).thenReturn("MAIN/a");
		
		final Branch target = mock(Branch.class);
		when(target.path()).thenReturn("MAIN/a/b");
		
		this.mergeReview = MergeReview.builder()
				.id("id")
				.sourcePath(source.path())
				.targetPath(target.path())
				.sourceToTargetReviewId("sourceToTargetReviewId")
				.targetToSourceReviewId("targetToSourceReviewId")
				.build();
	}
	
	@Test
	public void serializeMergeReview() throws Exception {
		
		String json = mapper.writeValueAsString(mergeReview);
		
		assertEquals("{\"id\":\"id\","
				+ "\"sourcePath\":\"MAIN/a\","
				+ "\"targetPath\":\"MAIN/a/b\","
				+ "\"sourceToTargetReviewId\":\"sourceToTargetReviewId\","
				+ "\"targetToSourceReviewId\":\"targetToSourceReviewId\","
				+ "\"status\":\"PENDING\"}", json);
		
	}

	@Test
	public void deserializeMergeReview() throws Exception {
		
		String json = mapper.writeValueAsString(mergeReview);
		MergeReview value = mapper.readValue(json, MergeReview.class);
		
		assertEquals("id", value.id());
		assertEquals("MAIN/a", value.sourcePath());
		assertEquals("MAIN/a/b", value.targetPath());
		assertEquals("sourceToTargetReviewId", value.sourceToTargetReviewId());
		assertEquals("targetToSourceReviewId", value.targetToSourceReviewId());
		assertEquals(ReviewStatus.PENDING, value.status());
		
	}
	
}
