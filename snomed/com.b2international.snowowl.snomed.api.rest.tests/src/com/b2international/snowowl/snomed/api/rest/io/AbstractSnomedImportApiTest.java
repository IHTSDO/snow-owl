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
package com.b2international.snowowl.snomed.api.rest.io;

import static com.b2international.snowowl.test.commons.rest.RestExtensions.givenAuthenticatedRequest;
import static com.b2international.snowowl.test.commons.rest.RestExtensions.lastPathSegment;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.b2international.commons.platform.PlatformUtil;
import com.b2international.snowowl.snomed.api.rest.AbstractSnomedApiTest;
import com.b2international.snowowl.snomed.api.rest.SnomedApiTestConstants;
import com.b2international.snowowl.snomed.core.domain.Rf2ReleaseType;
import com.b2international.snowowl.test.commons.rest.RestExtensions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

/**
 * @since 2.0
 */
public abstract class AbstractSnomedImportApiTest extends AbstractSnomedApiTest {

	private static final Set<String> FINISH_STATES = ImmutableSet.of("COMPLETED", "FAILED");

	private static final long POLL_INTERVAL = TimeUnit.SECONDS.toMillis(1L);

	private static final long POLL_TIMEOUT = TimeUnit.SECONDS.toMillis(30L);

	protected void assertImportFileCanBeUploaded(final String importId, final String importFile) {
		givenAuthenticatedRequest(SnomedApiTestConstants.SCT_API).with().multiPart(new File(PlatformUtil.toAbsolutePath(getClass(), importFile).toString()))
		.when().post("/imports/{id}/archive", importId)
		.then().assertThat().statusCode(204);
	}

	protected void assertImportCompletes(final String importId) {
		assertImportFinishState(importId, "COMPLETED");
	}

	protected void assertImportFails(final String importId) {
		assertImportFinishState(importId, "FAILED");
	}

	private void assertImportFinishState(final String importId, final String expectedStatus) {
		final long endTime = System.currentTimeMillis() + POLL_TIMEOUT;

		long currentTime;
		String currentStatus;

		do {

			try {
				Thread.sleep(POLL_INTERVAL);
			} catch (final InterruptedException e) {
				fail(e.toString());
			}

			currentStatus = givenAuthenticatedRequest(SnomedApiTestConstants.SCT_API)
					.when().get("/imports/{id}", importId)
					.then().assertThat().statusCode(200)
					.and().extract().response().body().path("status");

			currentTime = System.currentTimeMillis();

		} while (!FINISH_STATES.contains(currentStatus) && currentTime < endTime);

		assertEquals("End state should be " + expectedStatus + ".", expectedStatus, currentStatus);
	}

	protected void assertImportFileCanBeImported(final String importFile) {
		final Map<?, ?> importConfiguration = newImportConfiguration();
		final String importId = assertImportConfigurationCanBeCreated(importConfiguration);
		assertImportFileCanBeUploaded(importId, importFile);
		assertImportCompletes(importId);
	}

	protected void assertImportFileCanNotBeImported(final String importFile) {
		final Map<?, ?> importConfiguration = newImportConfiguration();
		final String importId = assertImportConfigurationCanBeCreated(importConfiguration);
		assertImportFileCanBeUploaded(importId, importFile);
		assertImportFails(importId);
	}
	
	protected void assertPatchImportFileCanBeImported(final String importFile, final String patchReleaseVersion) {
		final Map<?, ?> importConfiguration = newPatchImportConfiguration(patchReleaseVersion);
		final String importId = assertImportConfigurationCanBeCreated(importConfiguration);
		assertImportFileCanBeUploaded(importId, importFile);
		assertImportCompletes(importId);
	}

	protected void assertPatchImportFileCanNotBeImported(final String importFile, final String patchReleaseVersion) {
		final Map<?, ?> importConfiguration = newPatchImportConfiguration(patchReleaseVersion);
		final String importId = assertImportConfigurationCanBeCreated(importConfiguration);
		assertImportFileCanBeUploaded(importId, importFile);
		assertImportFails(importId);
	}

	private Map<?, ?> newPatchImportConfiguration(
			final String patchReleaseVersion) {
		final Map<?, ?> importConfiguration = ImmutableMap.builder()
				.put("type", Rf2ReleaseType.DELTA.name())
				.put("branchPath", branchPath.getPath())
				.put("patchReleaseVersion", patchReleaseVersion)
				.build();
		return importConfiguration;
	}

	private Map<?, ?> newImportConfiguration() {
		final Map<?, ?> importConfiguration = ImmutableMap.builder()
				.put("type", Rf2ReleaseType.DELTA.name())
				.put("branchPath", branchPath.getPath())
				.put("createVersions", true)
				.build();
		return importConfiguration;
	}

	protected Response whenCreatingImportConfiguration(final Map<?, ?> importConfiguration) {
		final String endpoint = importConfiguration.containsKey("patchReleaseVersion") ? "/imports/release-patch" : "/imports"; 
		return givenAuthenticatedRequest(SnomedApiTestConstants.SCT_API)
				.with().contentType(ContentType.JSON)
				.and().body(importConfiguration)
				.when().post(endpoint);
	}

	protected String assertImportConfigurationCanBeCreated(final Map<?, ?> importConfiguration) {
		final Response response = whenCreatingImportConfiguration(importConfiguration);

		final String location = RestExtensions.expectStatus(response, 201)
				.and().extract().response().header("Location");
		
		return lastPathSegment(location);
	}
}