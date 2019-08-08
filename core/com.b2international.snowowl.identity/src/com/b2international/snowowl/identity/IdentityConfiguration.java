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
package com.b2international.snowowl.identity;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;

/**
 * Identity module configuration. Use to configure the underlying identity services (authentication, authorization, tokens, etc.).
 * 
 * @since 5.11
 */
public class IdentityConfiguration {

	public static final String REQUEST_HEADER_USERNAME = "X-AUTH-username";
    public static final String REQUEST_HEADER_ROLES = "X-AUTH-roles";
    public static final String DEFAULT_ROLE = "ROLE_USER";
	
	private boolean adminParty = false;
	
	@NotNull
	private Set<String> roles = ImmutableSet.of(DEFAULT_ROLE);
	
	@NotEmpty
	private List<IdentityProviderConfig> providerConfigurations = Collections.emptyList();
	
	public boolean isAdminParty() {
		return adminParty;
	}

	public void setAdminParty(boolean adminParty) {
		this.adminParty = adminParty;
	}
	
	@JsonProperty("providers")
	public List<IdentityProviderConfig> getProviderConfigurations() {
		return providerConfigurations;
	}
	
	public void setProviderConfigurations(List<IdentityProviderConfig> providerConfigurations) {
		this.providerConfigurations = providerConfigurations;
	}
	
	@JsonProperty
	public Set<String> getRoles() {
		return roles;
	}
	
	@JsonProperty
	public void setRoles(Set<String> roles) {
		this.roles = roles;
	}
	
}