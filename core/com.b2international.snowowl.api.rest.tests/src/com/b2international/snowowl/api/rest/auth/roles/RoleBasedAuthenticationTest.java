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
package com.b2international.snowowl.api.rest.auth.roles;

import static com.b2international.snowowl.api.rest.CodeSystemApiAssert.newCodeSystemRequestBody;
import static com.b2international.snowowl.test.commons.rest.RestExtensions.givenAuthenticatedRequest;
import static com.b2international.snowowl.test.commons.rest.RestExtensions.givenAuthenticatedRequestWithRole;
import static com.b2international.snowowl.test.commons.rest.RestExtensions.givenAuthenticatedRequestWithRoleAndUser;
import static com.b2international.snowowl.test.commons.rest.RestExtensions.givenInvalidPasswordRequest;
import static com.b2international.snowowl.test.commons.rest.RestExtensions.givenUnauthenticatedRequest;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.List;
import java.util.Set;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import com.b2international.commons.platform.PlatformUtil;
import com.b2international.snowowl.core.ApplicationContext;
import com.b2international.snowowl.core.config.SnowOwlConfiguration;
import com.b2international.snowowl.datastore.commitinfo.CommitInfo;
import com.b2international.snowowl.datastore.request.RepositoryRequests;
import com.b2international.snowowl.identity.IdentityConfiguration;
import com.b2international.snowowl.test.commons.BundleStartRule;
import com.b2international.snowowl.test.commons.SnowOwlAppRule;
import com.b2international.snowowl.test.commons.rest.RestExtensions;

import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

/**
 * @since 6.16.4
 */
public class RoleBasedAuthenticationTest {

	@ClassRule
	public static final RuleChain appRule = RuleChain
		.outerRule(SnowOwlAppRule.snowOwl().clearResources(true).config(PlatformUtil.toAbsolutePath(RoleBasedAuthenticationTest.class, "rest-auth-configuration.yml")))
		.around(new BundleStartRule("org.eclipse.jetty.osgi.boot"))
		.around(new BundleStartRule("com.b2international.snowowl.api.rest"));

	@Test
	public void denyUnauthenticated() {
		assertResponseStatus(givenUnauthenticatedRequest("/admin"), 401);
	}

	@Test
	public void denyIncorrectCredentials() {
		assertResponseStatus(givenInvalidPasswordRequest("/admin"), 401);
	}

	@Test
	public void denyAccessWithoutRoles() {
		assertResponseStatus(givenAuthenticatedRequest("/admin"), 403);
	}

	@Test
	public void denyAccessWithInvalidRoles() {
		assertResponseStatus(givenAuthenticatedRequestWithRole("/admin", "invalid-role"), 403);
	}

	@Test
	public void allowAccessWithRoles() {

		Set<String> roles = getConfiguredRoles();
		assertEquals(2, roles.size());

		for (String role : roles) {
			assertResponseStatus(givenAuthenticatedRequestWithRole("/admin", role), 200);
		}

	}

	@Test
	public void allowAccessToStaticPathWithoutRoles() {
		givenAuthenticatedRequest("/admin").when().get("/api-docs").then().assertThat().statusCode(200);
	}

	@Test
	public void allowAccessToStaticPathWithRoles() {
		givenAuthenticatedRequestWithRole("/admin", getConfiguredRoles().stream().findFirst().get()).when().get("/api-docs").then().assertThat().statusCode(200);
	}

	@Test
	public void testDecoratedUserNameInCommit() {

		String role = getConfiguredRoles().stream().findFirst().get();
		String custom_username = "custom_username";

		assertNotEquals(RestExtensions.USER, custom_username);

		givenAuthenticatedRequestWithRoleAndUser("/admin", role, custom_username)
			.with().contentType(ContentType.JSON)
			.and().body(newCodeSystemRequestBody("test_codesystem"))
			.when().post("/codesystems")
			.then().assertThat().statusCode(201);

		List<CommitInfo> commits = RepositoryRequests.commitInfos().prepareSearchCommitInfo()
				.filterByUserId(custom_username)
				.filterByComment(String.format("Created new Code System %s", "test_codesystem"))
				.build("snomedStore")
				.get()
				.getItems();

		assertEquals(1, commits.size());
		assertEquals(custom_username, commits.stream().findFirst().get().getUserId());

	}

	private void assertResponseStatus(final RequestSpecification request, final int statusCode) {
		request.when().get("/repositories").then().assertThat().statusCode(statusCode);
	}

	private Set<String> getConfiguredRoles() {
		return ApplicationContext.getServiceForClass(SnowOwlConfiguration.class).getModuleConfig(IdentityConfiguration.class).getRoles();
	}

}
