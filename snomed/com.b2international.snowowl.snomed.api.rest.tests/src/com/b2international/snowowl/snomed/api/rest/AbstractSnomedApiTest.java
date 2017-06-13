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

import java.util.Optional;

import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import com.b2international.snowowl.core.api.IBranchPath;
import com.b2international.snowowl.core.branch.Branch;
import com.b2international.snowowl.datastore.BranchPathUtils;

/**
 * @since 2.0
 */
@BranchBase(Branch.MAIN_PATH)
public abstract class AbstractSnomedApiTest {

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

				branchPath = BranchPathUtils.createPath(SnomedApiTestConstants.PATH_JOINER.join(testBasePath, testClassName, testMethodName));
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

	@Rule 
	public final TestWatcher watcher = new CustomTestWatcher();
	
	protected void updateBranchWithMetadata(Map<?, ?> metadata) {
		whenUpdatingBranchWithPathAndMetadata(testBranchPath, metadata);
	}
	
	protected void updateBranchWithMetadata(IBranchPath branchPath, Map<?, ?> metadata) {
		whenUpdatingBranchWithPathAndMetadata(branchPath, metadata);
	}
	
	protected String createDefaultCodeSystem(String shortName) {
		Map<String, String> defaultCodeSystemRequestBody = newCodeSystemRequestBody(shortName);
		return createCustomizedCodeSystem(defaultCodeSystemRequestBody);
	}
	
	protected String createSnomedExtensionCodeSystem(String shortName, String branchPath, String language) {
		Map<String, String> newExtensionCodeSystemRequestBody = newCodeSystemRequestDefaultBodyBuilder(shortName, branchPath, language).put("extensionOf", "SNOMEDCT").build();
		return createCustomizedCodeSystem(newExtensionCodeSystemRequestBody);
	}

	protected String createCustomizedCodeSystem(final Map<String, String> newExtensionCodeSystemRequestBody) {
		return assertCodeSystemCreated(newExtensionCodeSystemRequestBody);
	}
}
