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
package com.b2international.snowowl.snomed.api.rest;

import static com.b2international.snowowl.test.commons.rest.RestExtensions.givenAuthenticatedRequest;
import static com.b2international.snowowl.test.commons.rest.RestExtensions.lastPathSegment;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;

import java.util.Map;
import java.util.Optional;

import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import com.b2international.snowowl.core.ApplicationContext;
import com.b2international.snowowl.core.api.IBranchPath;
import com.b2international.snowowl.core.branch.Branch;
import com.b2international.snowowl.datastore.BranchPathUtils;
import com.b2international.snowowl.eventbus.IEventBus;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;

/**
 * @since 2.0
 */
@BranchBase(Branch.MAIN_PATH)
public abstract class AbstractSnomedApiTest {
	
	protected static final String CONTENT_TYPE_TXT_CSV = "text/csv";
	protected static final String CONTENT_TYPE_UTF_8_JSON = "application/json; charset=UTF-8";
	
	private final class CustomTestWatcher extends TestWatcher {
		@Override
		protected void starting(Description description) {
			System.out.println("===== Start of " + description + " =====");
	
			Class<?> testClass = description.getTestClass();
			BranchBase branchBaseAnnotation = getBranchBaseAnnotation(testClass);
			String testBasePath = getTestBasePath(branchBaseAnnotation);
	
			if (isolateTests(branchBaseAnnotation)) {
				String testClassName = testClass.getSimpleName();
				String testMethodName = description.getMethodName()
						.replace("[", "_") // Remove special characters from parameterized test names
						.replace("]", "");
				// branch path segment cannot be longer than 50 chars (and we have very long test names...)
				testMethodName = testMethodName.substring(0, Math.min(50, testMethodName.length()));

				branchPath = BranchPathUtils.createPath(SnomedApiTestConstants.PATH_JOINER.skipNulls().join(testBasePath, testClassName, testMethodName));
			} else {
				branchPath = BranchPathUtils.createPath(testBasePath);
			}
		
			SnomedBranchingRestRequests.createBranchRecursively(branchPath);
		}
		
		@Override
		protected void finished(Description description) {
			System.out.println("===== End of " + description + " =====");
		}
		
		private BranchBase getBranchBaseAnnotation(Class<?> type) {
			if (type.isAnnotationPresent(BranchBase.class)) {
				return type.getAnnotation(BranchBase.class);
			} else {
				if (type.getSuperclass() != null) {
					BranchBase doc = getBranchBaseAnnotation(type.getSuperclass());
					if (doc != null) {
						return doc;
					}
				}

				for (Class<?> iface : type.getInterfaces()) {
					BranchBase doc = getBranchBaseAnnotation(iface);
					if (doc != null) {
						return doc;
					}
				}

				return null;
			}
		}

		private String getTestBasePath(BranchBase branchBaseAnnotation) {
			return Optional.ofNullable(branchBaseAnnotation)
					.map(a -> a.value())
					.orElse(Branch.MAIN_PATH);
		}

		private boolean isolateTests(BranchBase branchBaseAnnotation) {
			return Optional.ofNullable(branchBaseAnnotation)
					.map(a -> a.isolateTests())
					.orElse(true);
		}
	}

	protected IBranchPath branchPath;

	protected IEventBus getBus() {
		return ApplicationContext.getServiceForClass(IEventBus.class);
	}
	
	@Rule 
	public final TestWatcher watcher = new CustomTestWatcher();
	
	protected void updateBranchWithMetadata(IBranchPath branchPath, Map<?, ?> metadata) {
		whenUpdatingBranch(branchPath, metadata).then().assertThat().statusCode(204);
	}
	
	protected String createDefaultCodeSystem(String shortName) {
		Map<String, String> defaultCodeSystemRequestBody = newCodeSystemRequestBody(shortName, branchPath.getPath()).build();
		return createCustomizedCodeSystem(defaultCodeSystemRequestBody);
	}
	
	protected String createSnomedExtensionCodeSystem(String shortName, String branchPath, String language) {
		Map<String, String> newExtensionCodeSystemRequestBody = CodeSystemRestRequests.defaultCodeSystemBodyBuilder(BranchPathUtils.createPath(branchPath), shortName)
																						.put("extensionOf", "SNOMEDCT").build();
		return createCustomizedCodeSystem(newExtensionCodeSystemRequestBody);
	}

	protected String createCustomizedCodeSystem(final Map<String, String> newExtensionCodeSystemRequestBody) {
		return assertCodeSystemCreated(newExtensionCodeSystemRequestBody);
	}
	
	private static Response whenCreatingCodeSystem(final Map<?, ?> requestBody) {
		return givenAuthenticatedRequest("/admin")
				.with().contentType(ContentType.JSON)
				.and().body(requestBody)
				.when().post("/codesystems");
	}
	
	public static String assertCodeSystemCreated(final Map<?, ?> requestBody) {
		final String path = whenCreatingCodeSystem(requestBody).then().assertThat().statusCode(201)
			.and().header("Location", containsString(String.format("%s/%s", "codesystems", requestBody.get("shortName"))))
			.and().body(equalTo(""))
			.and().extract().response().getHeader("Location");
		
		return lastPathSegment(path);
	}
	
	public static Builder<String, String> newCodeSystemRequestBody(final String shortName, final String branchPath) {
		return ImmutableMap.<String, String>builder()
				.put("name", "CodeSystem")
				.put("branchPath", branchPath)
				.put("shortName", shortName)
				.put("citation", "citation")
				.put("iconPath", "icons/snomed.png")
				.put("repositoryUuid", "snomedStore")
				.put("terminologyId", "concept")
				.put("oid", shortName)
				.put("primaryLanguage", "ENG")
				.put("organizationLink", "link");
	}
	
	private static Response whenUpdatingBranch(final IBranchPath branchPath, final Map<?, ?> metadata) {
		final Map<?, ?> requestBody = ImmutableMap.<String, Object> builder()
				.put("metadata", metadata).build();

		return givenAuthenticatedRequest(SnomedApiTestConstants.SCT_API)
				.and().contentType(ContentType.JSON)
				.and().body(requestBody)
				.when().put("/branches/" + branchPath);
	}
}
