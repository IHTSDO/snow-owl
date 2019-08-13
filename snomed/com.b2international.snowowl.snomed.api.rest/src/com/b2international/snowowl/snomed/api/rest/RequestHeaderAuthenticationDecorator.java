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
package com.b2international.snowowl.snomed.api.rest;

import static com.b2international.snowowl.identity.IdentityConfiguration.REQUEST_HEADER_ROLES;
import static com.b2international.snowowl.identity.IdentityConfiguration.REQUEST_HEADER_USERNAME;

import java.io.IOException;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * @since 4.6
 */
public class RequestHeaderAuthenticationDecorator extends OncePerRequestFilter {

	@Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain)
            throws ServletException, IOException {
    	
    	final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    	
		// Pass through recorded credentials and details object
		final Object currentCredentials = authentication.getCredentials();
		final Object currentDetails = authentication.getDetails();
		
		// Change username to value retrieved from header
		final String decoratedUsername = request.getHeader(REQUEST_HEADER_USERNAME);
		
		// Merge authorities granted via existing authentication with values in header
		final List<GrantedAuthority> decoratedRoles = AuthorityUtils.commaSeparatedStringToAuthorityList(request.getHeader(REQUEST_HEADER_ROLES));
		
		final PreAuthenticatedAuthenticationToken decoratedAuthentication = new PreAuthenticatedAuthenticationToken(decoratedUsername, currentCredentials, decoratedRoles);
		decoratedAuthentication.setDetails(currentDetails);
		SecurityContextHolder.getContext().setAuthentication(decoratedAuthentication);            
        
        filterChain.doFilter(request, response);
    }
    
    @Override
    protected boolean shouldNotFilter(final HttpServletRequest request) throws ServletException {
    	Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    	return authentication == null || !authentication.isAuthenticated() || request.getHeader(REQUEST_HEADER_USERNAME) == null || request.getHeader(REQUEST_HEADER_ROLES) == null;
    }
}
