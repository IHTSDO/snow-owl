/*
 * Copyright 2011-2017 B2i Healthcare Pte Ltd, http://b2i.sg
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
package com.b2international.snowowl.snomed.api.rest.classification.ext;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

import com.b2international.snowowl.core.ApplicationContext;
import com.b2international.snowowl.snomed.datastore.config.SnomedClassificationConfiguration;
import com.b2international.snowowl.snomed.datastore.config.SnomedCoreConfiguration;

/**
 * @since 5.10.10
 */
public class SnomedExternalClassificationServiceConfigTests {

	private static SnomedClassificationConfiguration config;

	@BeforeClass
	public static void before() {
		config = ApplicationContext.getServiceForClass(SnomedCoreConfiguration.class).getClassificationConfig();
	}
	
	@Test
	public void testMaxReasonerCount() {
		assertEquals(1, config.getMaxReasonerCount());
	}
	
	@Test
	public void testDefaultReasoner() {
		assertEquals(SnomedClassificationConfiguration.DEFAULT_REASONER, config.getDefaultReasoner());
	}
	
	@Test
	public void testShowReasonerUsageWarning() {
		assertEquals(false, config.isShowReasonerUsageWarning());
	}
	
	@Test
	public void testMaxReasonerResults() {
		assertEquals(20, config.getMaxReasonerResults());
	}
	
	@Test
	public void testMaxReasonerRuns() {
		assertEquals(10000, config.getMaxReasonerRuns());
	}
	
	@Test
	public void testExternalServiceUrl() {
		assertEquals("http://localhost:8089", config.getExternalService().getUrl());
	}
	
	@Test
	public void testExternalServiceUserName() {
		assertEquals("extServiceUserName", config.getExternalService().getUserName());
	}
	
	@Test
	public void testExternalServicePassword() {
		assertEquals("extServicePassword", config.getExternalService().getPassword());
	}
	
	@Test
	public void testExternalServiceNumberOfPollTries() {
		assertEquals(1L, config.getExternalService().getNumberOfPollTries());
	}
	
	@Test
	public void testExternalServiceTimeBetweenPollTries() {
		assertEquals(10L, config.getExternalService().getTimeBetweenPollTries());
	}
}
