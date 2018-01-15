/*
 * Copyright 2011-2016 B2i Healthcare Pte Ltd, http://b2i.sg
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.b2international.snowowl.snomed.api.rest.browser;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.b2international.commons.http.AcceptHeader;
import com.b2international.commons.http.ExtendedLocale;
import com.b2international.snowowl.core.domain.IComponentRef;
import com.b2international.snowowl.core.exceptions.BadRequestException;
import com.b2international.snowowl.datastore.server.domain.StorageRef;
import com.b2international.snowowl.snomed.api.browser.ISnomedBrowserService;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserBulkChangeRun;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserChildConcept;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserConcept;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserConstant;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserDescriptionResult;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserParentConcept;
import com.b2international.snowowl.snomed.api.domain.browser.SnomedBrowserDescriptionType;
import com.b2international.snowowl.snomed.api.impl.domain.browser.SnomedBrowserConcept;
import com.b2international.snowowl.snomed.api.rest.AbstractSnomedRestService;
import com.b2international.snowowl.snomed.api.rest.domain.RestApiError;
import com.b2international.snowowl.snomed.api.rest.util.Responses;
import com.b2international.snowowl.snomed.api.validation.ISnomedBrowserValidationService;
import com.b2international.snowowl.snomed.api.validation.ISnomedInvalidContent;
import com.google.common.collect.ImmutableList;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

/**
 * @since 1.0
 */
@Api("IHTSDO SNOMED CT Browser")
@Controller
@RequestMapping(
		value="/browser",
		produces={ SnomedBrowserRestService.IHTSDO_V1_MEDIA_TYPE, MediaType.APPLICATION_JSON_VALUE })
public class SnomedBrowserRestService extends AbstractSnomedRestService {

	/**
	 * The currently supported versioned media type of the IHTSDO SNOMED CT Browser RESTful API.
	 */
	public static final String IHTSDO_V1_MEDIA_TYPE = "application/vnd.org.ihtsdo.browser+json";

	@Autowired
	protected ISnomedBrowserService browserService;
	
	@Autowired
	private ISnomedBrowserValidationService validationService;

	@ApiOperation(
			value="Retrieve single concept properties",
			notes="Retrieves a single concept and related information on a branch.")
	@ApiResponses({
		@ApiResponse(code = 200, message = "OK", response = Void.class),
		@ApiResponse(code = 404, message = "Code system version or concept not found", response = RestApiError.class)
	})
	@RequestMapping(value="/{path:**}/concepts/{conceptId}", method=RequestMethod.GET)
	public @ResponseBody ISnomedBrowserConcept getConceptDetails(
			@ApiParam(value="The branch path")
			@PathVariable(value="path")
			final String branchPath,

			@ApiParam(value="The concept identifier")
			@PathVariable(value="conceptId")
			final String conceptId,

			@ApiParam(value="Language codes and reference sets, in order of preference")
			@RequestHeader(value="Accept-Language", defaultValue="en-US;q=0.8,en-GB;q=0.6", required=false) 
			final String acceptLanguage) {

		final List<ExtendedLocale> extendedLocales = getExtendedLocales(acceptLanguage);
		return browserService.getConceptDetails(branchPath, conceptId, extendedLocales);
	}

	@ApiOperation(
			value="Retrieve concept properties in bulk",
			notes="Retrieves many concept and related information on a branch.")
	@ApiResponses({
		@ApiResponse(code = 200, message = "OK", response = Void.class),
		@ApiResponse(code = 404, message = "Code system version or concept not found", response = RestApiError.class)
	})
	@RequestMapping(value="/{path:**}/concepts/bulk-load", method=RequestMethod.POST)
	public @ResponseBody Set<ISnomedBrowserConcept> bulkGetConceptDetails(
			@ApiParam(value="The branch path")
			@PathVariable(value="path")
			final String branchPath,

			@ApiParam(value="A set of concept identifiers")
			@RequestParam(value="conceptIds", required=false) 
			final Set<String> conceptIds,

			@ApiParam(value="Language codes and reference sets, in order of preference")
			@RequestHeader(value="Accept-Language", defaultValue="en-US;q=0.8,en-GB;q=0.6", required=false) 
			final String acceptLanguage) {

		final List<ExtendedLocale> extendedLocales = getExtendedLocales(acceptLanguage);
		return browserService.getConceptDetailsInBulk(branchPath, conceptIds, extendedLocales);
	}

	@ApiOperation(
			value="Create a concept",
			notes="Creates a new Concept on a branch.")
	@ApiResponses({
			@ApiResponse(code = 200, message = "OK", response = Void.class),
			@ApiResponse(code = 404, message = "Code system version or concept not found", response = RestApiError.class)
	})
	@RequestMapping(value="/{path:**}/concepts", 
			method=RequestMethod.POST, 
			consumes={ IHTSDO_V1_MEDIA_TYPE, MediaType.APPLICATION_JSON_VALUE })
	public @ResponseBody ISnomedBrowserConcept createConcept(
			@ApiParam(value="The branch path")
			@PathVariable(value="path")
			final String branchPath,

			@ApiParam(value="Language codes and reference sets, in order of preference")
			@RequestHeader(value="Accept-Language", defaultValue="en-US;q=0.8,en-GB;q=0.6", required=false) 
			final String languageSetting,

			@RequestBody
			final SnomedBrowserConcept concept,

			final Principal principal) {

		final String userId = principal.getName();
		final List<ExtendedLocale> extendedLocales;
		
		try {
			extendedLocales = AcceptHeader.parseExtendedLocales(new StringReader(languageSetting));
		} catch (IOException e) {
			throw new BadRequestException(e.getMessage());
		} catch (IllegalArgumentException e) {
			throw new BadRequestException(e.getMessage());
		}
		
		return browserService.create(branchPath, concept, userId, extendedLocales);
	}

	@ApiOperation(
			value="Validate a concept",
			notes="Validates a concept in the context of a branch, without persisting your changes.")
	@ApiResponses({
			@ApiResponse(code = 200, message = "OK", response = Void.class),
			@ApiResponse(code = 404, message = "Code system version or concept not found")
	})
	@RequestMapping(value="/{path:**}/validate/concept", method=RequestMethod.POST)
	public @ResponseBody List<ISnomedInvalidContent> validateNewConcept(
			@ApiParam(value="The branch path")
			@PathVariable(value="path")
			final String branchPath,

			@ApiParam(value="Language codes and reference sets, in order of preference")
			@RequestHeader(value="Accept-Language", defaultValue="en-US;q=0.8,en-GB;q=0.6", required=false) 
			final String acceptLanguage,

			@RequestBody
			final SnomedBrowserConcept concept) {

		return validationService.validateConcept(branchPath, concept, getExtendedLocales(acceptLanguage));
	}

	@ApiOperation(
			value="Update a concept",
			notes="Updates a new Concept on a branch.")
	@ApiResponses({
			@ApiResponse(code = 200, message = "OK", response = Void.class),
			@ApiResponse(code = 404, message = "Code system version or concept not found", response = RestApiError.class)
	})
	@RequestMapping(value="/{path:**}/concepts/{conceptId}", 
			method=RequestMethod.PUT,
			consumes={ IHTSDO_V1_MEDIA_TYPE, MediaType.APPLICATION_JSON_VALUE })
	public @ResponseBody ISnomedBrowserConcept updateConcept(
			@ApiParam(value="The branch path")
			@PathVariable(value="path")
			final String branchPath,

			@ApiParam(value="The SCTID of the concept being updated")
			@PathVariable(value="conceptId")
			final String conceptId,

			@ApiParam(value="Language codes and reference sets, in order of preference")
			@RequestHeader(value="Accept-Language", defaultValue="en-US;q=0.8,en-GB;q=0.6", required=false) 
			final String languageSetting,

			@RequestBody
			final SnomedBrowserConcept concept,

			final Principal principal) {

		final String userId = principal.getName();
		final List<ExtendedLocale> extendedLocales;
		
		try {
			extendedLocales = AcceptHeader.parseExtendedLocales(new StringReader(languageSetting));
		} catch (IOException e) {
			throw new BadRequestException(e.getMessage());
		} catch (IllegalArgumentException e) {
			throw new BadRequestException(e.getMessage());
		}
		
		if (!conceptId.equals(concept.getConceptId())) {
			throw new BadRequestException("The concept ID in the request body does not match the ID in the URL.");
		}
		
		
		browserService.update(branchPath, ImmutableList.of(concept), userId, extendedLocales);
		return browserService.getConceptDetails(branchPath, concept.getConceptId(), extendedLocales);
	}

	@ApiOperation(
			value="Start a bulk concept change on a branch",
			notes = "Bulk concept changes are async jobs. The call to this method immediately returns with a unique URL "
					+ "pointing to the bulk run.<p>The URL can be used to fetch the state of the bulk run "
					+ "to determine whether it's completed or not.")
	@ApiResponses({
		@ApiResponse(code = 201, message = "Created"),
		@ApiResponse(code = 404, message = "Branch not found", response=RestApiError.class)
	})
	@RequestMapping(value="/{path:**}/concepts/bulk", 
			method=RequestMethod.POST,
			consumes={ IHTSDO_V1_MEDIA_TYPE, MediaType.APPLICATION_JSON_VALUE })
	@ResponseStatus(HttpStatus.CREATED)
	public ResponseEntity<Void> beginBulkConceptChange(
			@ApiParam(value="The branch path")
			@PathVariable(value="path")
			final String branchPath,

			@ApiParam(value="Language codes and reference sets, in order of preference")
			@RequestHeader(value="Accept-Language", defaultValue="en-US;q=0.8,en-GB;q=0.6", required=false) 
			final String languageSetting,

			@RequestBody
			final List<SnomedBrowserConcept> concepts,

			final Principal principal) throws URISyntaxException {

		final String userId = principal.getName();
		final List<ExtendedLocale> extendedLocales;
		
		try {
			extendedLocales = AcceptHeader.parseExtendedLocales(new StringReader(languageSetting));
		} catch (IOException e) {
			throw new BadRequestException(e.getMessage());
		} catch (IllegalArgumentException e) {
			throw new BadRequestException(e.getMessage());
		}
		
		final ISnomedBrowserBulkChangeRun bulkChangeRun = browserService.beginBulkChange(branchPath, concepts, userId, extendedLocales);
		return Responses.created(getBulkChangeRunUri(branchPath, bulkChangeRun)).build();
	}
	
	@ApiOperation(
			value="Retrieve the state of a bulk concepts change from branch")
	@ApiResponses({
		@ApiResponse(code = 200, message = "OK"),
		@ApiResponse(code = 404, message = "Branch or classification not found", response=RestApiError.class)
	})
	@RequestMapping(value="/{path:**}/concepts/bulk/{bulkChangeId}", method=RequestMethod.GET)
	public @ResponseBody ISnomedBrowserBulkChangeRun getBulkRun(
			@ApiParam(value="The branch path")
			@PathVariable(value="path")
			final String branchPath,

			@ApiParam(value="The branch path")
			@PathVariable(value="bulkChangeId")
			final String bulkChangeId
			) {
		return browserService.getBulkChange(bulkChangeId);
	}

	@ApiOperation(
			value = "Retrieve parents of a concept",
			notes = "Returns a list of parent concepts of the specified concept on a branch.",
			response=Void.class)
	@ApiResponses({
		@ApiResponse(code = 200, message = "OK"),
		@ApiResponse(code = 404, message = "Code system version or concept not found", response = RestApiError.class)
	})
	@RequestMapping(
			value="/{path:**}/concepts/{conceptId}/parents",
			method = RequestMethod.GET)
	public @ResponseBody List<ISnomedBrowserParentConcept> getConceptParents(
			@ApiParam(value="The branch path")
			@PathVariable(value="path")
			final String branchPath,
			
			@ApiParam(value="The concept identifier")
			@PathVariable(value="conceptId")
			final String conceptId,
			
			@ApiParam(value="Language codes and reference sets, in order of preference")
			@RequestHeader(value="Accept-Language", defaultValue="en-US;q=0.8,en-GB;q=0.6", required=false) 
			final String languageSetting,
			
			@ApiParam(value="The type of the description to expand", allowableValues="FSN, SYNONYM")
			@RequestParam(value="preferredDescriptionType", defaultValue="FSN")
			final SnomedBrowserDescriptionType preferredDescriptionType) {
		
		final List<ExtendedLocale> extendedLocales;
		
		try {
			extendedLocales = AcceptHeader.parseExtendedLocales(new StringReader(languageSetting));
		} catch (IOException e) {
			throw new BadRequestException(e.getMessage());
		} catch (IllegalArgumentException e) {
			throw new BadRequestException(e.getMessage());
		}
		
		final IComponentRef ref = createComponentRef(branchPath, conceptId);
		return browserService.getConceptParents(ref, extendedLocales, preferredDescriptionType);
	}
	
	@ApiOperation(
			value = "Retrieve children of a concept",
			notes = "Returns a list of child concepts of the specified concept on a branch.",
			response=Void.class)
	@ApiResponses({
		@ApiResponse(code = 200, message = "OK"),
		@ApiResponse(code = 404, message = "Code system version or concept not found", response = RestApiError.class)
	})
	@RequestMapping(
			value="/{path:**}/concepts/{conceptId}/children",
			method = RequestMethod.GET)
	public @ResponseBody List<ISnomedBrowserChildConcept> getConceptChildren(
			@ApiParam(value="The branch path")
			@PathVariable(value="path")
			final String branchPath,

			@ApiParam(value="The concept identifier")
			@PathVariable(value="conceptId")
			final String conceptId,

			@ApiParam(value="Language codes and reference sets, in order of preference")
			@RequestHeader(value="Accept-Language", defaultValue="en-US;q=0.8,en-GB;q=0.6", required=false) 
			final String languageSetting,
			
			@ApiParam(value="Stated or inferred form", allowableValues="stated, inferred")
			@RequestParam(value="form", defaultValue="inferred")
			final String form,
			
			@ApiParam(value="The type of the description to expand", allowableValues="FSN, SYNONYM")
			@RequestParam(value="preferredDescriptionType", defaultValue="FSN")
			final SnomedBrowserDescriptionType preferredDescriptionType) {

		final List<ExtendedLocale> extendedLocales;
		
		try {
			extendedLocales = AcceptHeader.parseExtendedLocales(new StringReader(languageSetting));
		} catch (IOException e) {
			throw new BadRequestException(e.getMessage());
		} catch (IllegalArgumentException e) {
			throw new BadRequestException(e.getMessage());
		}
		
		if ("stated".equals(form) || "inferred".equals(form)) {
			final IComponentRef ref = createComponentRef(branchPath, conceptId);
			return browserService.getConceptChildren(ref, extendedLocales, "stated".equals(form), preferredDescriptionType);
		}
		
		throw new BadRequestException("Form parameter should be either 'stated' or 'inferred'");
	}

	@ApiOperation(
			value = "Retrieve descriptions matching a query.",
			notes = "Returns a list of descriptions which have a term matching the specified query string on a version.",
			response=Void.class)
	@ApiResponses({
			@ApiResponse(code = 200, message = "OK"),
			@ApiResponse(code = 404, message = "Code system version or concept not found", response = RestApiError.class)
	})
	@RequestMapping(
			value="/{path:**}/descriptions",
			method = RequestMethod.GET)
	public @ResponseBody List<ISnomedBrowserDescriptionResult> searchDescriptions(
			@ApiParam(value="The branch path")
			@PathVariable(value="path")
			final String branchPath,

			@ApiParam(value="The query string")
			@RequestParam(value="query")
			final String query,

			@ApiParam(value="The starting offset in the list")
			@RequestParam(value="offset", defaultValue="0", required=false)
			final int offset,

			@ApiParam(value="The maximum number of items to return")
			@RequestParam(value="limit", defaultValue="50", required=false)
			final int limit,

			@ApiParam(value="Language codes and reference sets, in order of preference")
			@RequestHeader(value="Accept-Language", defaultValue="en-US;q=0.8,en-GB;q=0.6", required=false)
			final String languageSetting,
			
			@ApiParam(value="The type of the description to expand", allowableValues="FSN, SYNONYM")
			@RequestParam(value="preferredDescriptionType", defaultValue="FSN")
			final SnomedBrowserDescriptionType preferredDescriptionType) {

		final List<ExtendedLocale> extendedLocales;
		
		try {
			extendedLocales = AcceptHeader.parseExtendedLocales(new StringReader(languageSetting));
		} catch (IOException e) {
			throw new BadRequestException(e.getMessage());
		} catch (IllegalArgumentException e) {
			throw new BadRequestException(e.getMessage());
		}
		
		final StorageRef ref = new StorageRef(repositoryId, branchPath);
		return browserService.getDescriptions(ref, query, extendedLocales, preferredDescriptionType, offset, limit);
	}

	@ApiOperation(
			value="Retrieve constants and properties",
			notes="Retrieves referenced constants and related concept properties from a version branch.")
	@ApiResponses({
		@ApiResponse(code = 200, message = "OK", response = Void.class)
	})
	@RequestMapping(value="/{path:**}/constants", method=RequestMethod.GET)
	public @ResponseBody Map<String, ISnomedBrowserConstant> getConstants(

			@ApiParam(value="The branch path")
			@PathVariable(value="path")
			final String branchPath,

			@ApiParam(value="Language codes and reference sets, in order of preference")
			@RequestHeader(value="Accept-Language", defaultValue="en-US;q=0.8,en-GB;q=0.6", required=false) 
			final String languageSetting) {
		
		final List<ExtendedLocale> extendedLocales;
		
		try {
			extendedLocales = AcceptHeader.parseExtendedLocales(new StringReader(languageSetting));
		} catch (IOException e) {
			throw new BadRequestException(e.getMessage());
		} catch (IllegalArgumentException e) {
			throw new BadRequestException(e.getMessage());
		}
		
		return browserService.getConstants(branchPath, extendedLocales);
	}

	private URI getBulkChangeRunUri(final String branchPath, final ISnomedBrowserBulkChangeRun bulkChangeRun) throws URISyntaxException {
		return linkTo(getClass()).slash(branchPath).slash("concepts").slash("bulk").slash(bulkChangeRun.getId()).toUri();
	}
	
}
