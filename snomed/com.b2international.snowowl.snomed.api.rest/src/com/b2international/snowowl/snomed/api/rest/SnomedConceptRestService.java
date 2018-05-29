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

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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
import org.springframework.web.context.request.async.DeferredResult;

import com.b2international.commons.StringUtils;
import com.b2international.commons.http.AcceptHeader;
import com.b2international.commons.http.ExtendedLocale;
import com.b2international.snowowl.core.branch.Branch;
import com.b2international.snowowl.core.domain.PageableCollectionResource;
import com.b2international.snowowl.core.exceptions.BadRequestException;
import com.b2international.snowowl.datastore.request.RepositoryRequests;
import com.b2international.snowowl.datastore.request.SearchResourceRequest;
import com.b2international.snowowl.datastore.request.SearchResourceRequest.SortField;
import com.b2international.snowowl.snomed.api.domain.expression.ISnomedExpression;
import com.b2international.snowowl.snomed.api.impl.SnomedExpressionService;
import com.b2international.snowowl.snomed.api.rest.domain.ChangeRequest;
import com.b2international.snowowl.snomed.api.rest.domain.RestApiError;
import com.b2international.snowowl.snomed.api.rest.domain.SnomedConceptRestInput;
import com.b2international.snowowl.snomed.api.rest.domain.SnomedConceptRestSearch;
import com.b2international.snowowl.snomed.api.rest.domain.SnomedConceptRestUpdate;
import com.b2international.snowowl.snomed.api.rest.util.DeferredResults;
import com.b2international.snowowl.snomed.api.rest.util.Responses;
import com.b2international.snowowl.snomed.core.domain.SnomedConcept;
import com.b2international.snowowl.snomed.core.domain.SnomedConcepts;
import com.b2international.snowowl.snomed.datastore.request.SnomedRequests;
import com.google.common.base.Strings;
import com.google.common.net.HttpHeaders;
import com.mangofactory.swagger.annotations.ApiIgnore;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

/**
 * @since 1.0
 */
@Api("Concepts")
@Controller
@RequestMapping(produces={ AbstractRestService.SO_MEDIA_TYPE })
public class SnomedConceptRestService extends AbstractSnomedRestService {

	@Autowired
	private SnomedExpressionService expressionService;

	@ApiOperation(
			value="Retrieve Concepts from a branch", 
			notes="Returns a list with all/filtered Concepts from a branch."
					+ "<p>The following properties can be expanded:"
					+ "<p>"
					+ "&bull; pt() &ndash; the description representing the concept's preferred term in the given locale<br>"
					+ "&bull; fsn() &ndash; the description representing the concept's fully specified name in the given locale<br>"
					+ "&bull; descriptions() &ndash; the list of descriptions for the concept<br>")
	@ApiResponses({
		@ApiResponse(code = 200, message = "OK", response = PageableCollectionResource.class),
		@ApiResponse(code = 400, message = "Invalid filter config", response = RestApiError.class),
		@ApiResponse(code = 404, message = "Branch not found", response = RestApiError.class)
	})
	@RequestMapping(value="/{path:**}/concepts", method=RequestMethod.GET, produces={ AbstractRestService.SO_MEDIA_TYPE, AbstractRestService.TEXT_CSV_MEDIA_TYPE })
	public @ResponseBody DeferredResult<SnomedConcepts> search(
			@ApiParam(value="The branch path")
			@PathVariable(value="path")
			final String branch,

			@ApiParam(value="The concept status to match")
			@RequestParam(value="active", required=false) 
			final Boolean activeFilter,
			
			@ApiParam(value="The concept module identifier to match")
			@RequestParam(value="module", required=false) 
			final String moduleFilter,
			
			@ApiParam(value="The namespace to match")
			@RequestParam(value="namespace", required=false) 
			final String namespaceFilter,
			
			@ApiParam(value="The effective time to match (yyyyMMdd, exact matches only)")
			@RequestParam(value="effectiveTime", required=false) 
			final String effectiveTimeFilter,
			
			@ApiParam(value="The definition status to match")
			@RequestParam(value="definitionStatus", required=false) 
			final String definitionStatusFilter,
			
			@ApiParam(value="The description term to match")
			@RequestParam(value="term", required=false) 
			final String termFilter,
			
			@ApiParam(value="The description active state to match")
			@RequestParam(value="termActive", required=false) 
			final Boolean descriptionActiveFilter,

			@ApiParam(value="Deprecated! The ESCG expression to match")
			@RequestParam(value="escg", required=false) 
			final String escgFilter,
			
			@ApiParam(value="The ECL expression to match in the inferred tree")
			@RequestParam(value="ecl", required=false) 
			final String eclFilter,
			
			@ApiParam(value="The ECL expression to match in the stated tree")
			@RequestParam(value="statedEcl", required=false)
			final String statedEclFilter,
			
			@ApiParam(value="A set of concept identifiers")
			@RequestParam(value="conceptIds", required=false) 
			final Set<String> conceptIds,
			
			@ApiParam(value="The starting offset in the list")
			@RequestParam(value="offset", defaultValue="0", required=false) 
			final int offset,

			@ApiParam(value="The maximum number of items to return")
			@RequestParam(value="limit", defaultValue="50", required=false) 
			final int limit,
			@ApiParam(value="Expansion parameters")
			@RequestParam(value="expand", required=false)
			final String expand,

			@ApiParam(value="Accepted language tags, in order of preference")
			@RequestHeader(value=HttpHeaders.ACCEPT_LANGUAGE, defaultValue="en-US;q=0.8,en-GB;q=0.6", required=false) 
			final String acceptLanguage,
			
			@ApiIgnore
			@RequestHeader(value=HttpHeaders.ACCEPT, required=false)
			final String contentType) {
		
		return doSearch(branch,
				activeFilter,
				moduleFilter,
				namespaceFilter,
				effectiveTimeFilter,
				definitionStatusFilter,
				termFilter,
				descriptionActiveFilter,
				escgFilter,
				eclFilter,
				statedEclFilter,
				conceptIds,
				offset,
				limit,
				expand,
				acceptLanguage,
				contentType);
	}

	@ApiOperation(
			value="Alternate search endpoint. Retrieve Concepts from a branch", 
			notes="This is an alternative to the standard {path}/concepts search endpoint.<br/>"
					+ "Returns a list with all/filtered Concepts from a branch."
					+ "<p>The following properties can be expanded:"
					+ "<p>"
					+ "&bull; pt() &ndash; the description representing the concept's preferred term in the given locale<br>"
					+ "&bull; fsn() &ndash; the description representing the concept's fully specified name in the given locale<br>"
					+ "&bull; descriptions() &ndash; the list of descriptions for the concept<br>")
	@ApiResponses({
		@ApiResponse(code = 200, message = "OK", response = PageableCollectionResource.class),
		@ApiResponse(code = 400, message = "Invalid filter config", response = RestApiError.class),
		@ApiResponse(code = 404, message = "Branch not found", response = RestApiError.class)
	})
	@RequestMapping(value="/{path:**}/concepts/search", method = {RequestMethod.POST},  produces={ AbstractRestService.SO_MEDIA_TYPE, AbstractRestService.TEXT_CSV_MEDIA_TYPE })
	public @ResponseBody DeferredResult<SnomedConcepts> searchViaPost(
			@ApiParam(value="The branch path")
			@PathVariable(value="path")
			final String branch,

			@RequestBody
			final SnomedConceptRestSearch body,
			
			@ApiParam(value="Accepted language tags, in order of preference")
			@RequestHeader(value=HttpHeaders.ACCEPT_LANGUAGE, defaultValue="en-US;q=0.8,en-GB;q=0.6", required=false) 
			final String acceptLanguage,
		
			@ApiIgnore
			@RequestHeader(value=HttpHeaders.ACCEPT, defaultValue=AbstractRestService.SO_MEDIA_TYPE, required=false)
			final String contentType){
		
		return doSearch(branch,
				body.getActiveFilter(),
				body.getModuleFilter(),
				null, // TODO
				null, // TODO
				body.getDefinitionStatusFilter(),
				body.getTermFilter(),
				body.getDescriptionActiveFilter(),
				body.getEscgFilter(),
				body.getEclFilter(),
				body.getStatedEclFilter(),
				body.getConceptIds(),
				body.getOffset(),
				body.getLimit(),
				body.getExpand(),
				acceptLanguage,
				contentType);
	}

	private DeferredResult<SnomedConcepts> doSearch(
			final String branch,
			final Boolean activeFilter,
			final String moduleFilter,
			final String namespaceFilter,
			final String effectiveTimeFilter,
			final String definitionStatusFilter,
			final String termFilter,
			final Boolean descriptionActiveFilter,
			final String escgFilter,
			final String eclFilter,
			final String statedEclFilter,
			final Set<String> conceptIds, 
			final int offset,
			final int limit,
			final String expandParams,
			final String acceptLanguage,
			final String contentType) {
		
		final List<ExtendedLocale> extendedLocales;
		
		String expand = expandParams;
		
		if (AbstractRestService.TEXT_CSV_MEDIA_TYPE.equals(contentType)) {
			if (!Strings.isNullOrEmpty(expand) && expand.contains("pt") && !expand.contains("descriptions()")) {
				expand = String.format("%s,descriptions()", expand);
			}
		}
		try {
			extendedLocales = AcceptHeader.parseExtendedLocales(new StringReader(acceptLanguage));
		} catch (IOException e) {
			throw new BadRequestException(e.getMessage());
		} catch (IllegalArgumentException e) {
			throw new BadRequestException(e.getMessage());
		}
		
		final SortField sortField = StringUtils.isEmpty(termFilter) 
				? SearchResourceRequest.DOC_ID 
				: SearchResourceRequest.SCORE;
		
		return DeferredResults.wrap(
				SnomedRequests
					.prepareSearchConcept()
					.setLimit(limit)
					.setOffset(offset)
					.filterByIds(conceptIds == null ? Collections.emptySet() : conceptIds)
					.filterByActive(activeFilter)
					.filterByModule(moduleFilter)
					.filterByEffectiveTime(effectiveTimeFilter)
					.filterByDefinitionStatus(definitionStatusFilter)
					.filterByNamespace(namespaceFilter)
					.filterByTerm(termFilter)
					.filterByDescriptionActive(descriptionActiveFilter)
					.filterByEscg(escgFilter)
					.filterByEcl(eclFilter)
					.filterByStatedEcl(statedEclFilter)
					.filterByExtendedLocales(extendedLocales)
					.setExpand(expand)
					.setLocales(extendedLocales)
					.sortBy(sortField)
					.build(repositoryId, branch)
					.execute(bus));
	}

	@ApiOperation(
			value="Retrieve Concept properties",
			notes="Returns all properties of the specified Concept, including a summary of inactivation indicator and association members."
					+ "<p>The following properties can be expanded:"
					+ "<p>"
					+ "&bull; pt() &ndash; the description representing the concept's preferred term in the given locale<br>"
					+ "&bull; fsn() &ndash; the description representing the concept's fully specified name in the given locale<br>"
					+ "&bull; descriptions() &ndash; the list of descriptions for the concept<br>"
					+ "&bull; ancestors(offset:0,limit:50,direct:true,expand(pt(),...)) &ndash; the list of concept ancestors (parameter 'direct' is required)<br>"
					+ "&bull; descendants(offset:0,limit:50,direct:true,expand(pt(),...)) &ndash; the list of concept descendants (parameter 'direct' is required)<br>")
	@ApiResponses({
		@ApiResponse(code = 200, message = "OK", response = Void.class),
		@ApiResponse(code = 404, message = "Branch or Concept not found", response = RestApiError.class)
	})
	@RequestMapping(value="/{path:**}/concepts/{conceptId}", method=RequestMethod.GET)
	public @ResponseBody DeferredResult<SnomedConcept> read(
			@ApiParam(value="The branch path")
			@PathVariable(value="path")
			final String branchPath,

			@ApiParam(value="The Concept identifier")
			@PathVariable(value="conceptId")
			final String conceptId,
			
			@ApiParam(value="Expansion parameters")
			@RequestParam(value="expand", required=false)
			final String expand,
			
			@ApiParam(value="Accepted language tags, in order of preference")
			@RequestHeader(value=HttpHeaders.ACCEPT_LANGUAGE, defaultValue="en-US;q=0.8,en-GB;q=0.6", required=false) 
			final String acceptLanguage) {

		final List<ExtendedLocale> extendedLocales;
		
		try {
			extendedLocales = AcceptHeader.parseExtendedLocales(new StringReader(acceptLanguage));
		} catch (IOException e) {
			throw new BadRequestException(e.getMessage());
		} catch (IllegalArgumentException e) {
			throw new BadRequestException(e.getMessage());
		}

		return DeferredResults.wrap(
				SnomedRequests
					.prepareGetConcept(conceptId)
					.setExpand(expand)
					.setLocales(extendedLocales)
					.build(repositoryId, branchPath)
					.execute(bus));
	}

	@ApiOperation(
			value="Retrieve authoring form of a concept",
			notes="Retrieve authoring form of a concept which includes proximal primitive super-types and all inferred relationships "
					+ "which are not of type 'Is a'.")
	@ApiResponses({
		@ApiResponse(code = 200, message = "OK", response = Void.class),
		@ApiResponse(code = 404, message = "Branch or Concept not found", response = RestApiError.class)
	})
	@RequestMapping(value="/{path:**}/concepts/{conceptId}/authoring-form", method=RequestMethod.GET)
	public @ResponseBody ISnomedExpression readShortNormalForm(
			@ApiParam(value="The branch path")
			@PathVariable(value="path")
			final String branchPath,

			@ApiParam(value="The Concept identifier")
			@PathVariable(value="conceptId")
			final String conceptId,
			
			@ApiParam(value="Normalise attribute values")
			@RequestParam(value="normaliseAttributeValues", defaultValue="true")
			final boolean normaliseAttributeValues,
			
			@ApiParam(value="Accepted language tags, in order of preference")
			@RequestHeader(value=HttpHeaders.ACCEPT_LANGUAGE, defaultValue="en-US;q=0.8,en-GB;q=0.6", required=false) 
			final String acceptLanguage) {

		final List<ExtendedLocale> extendedLocales;
		
		try {
			extendedLocales = AcceptHeader.parseExtendedLocales(new StringReader(acceptLanguage));
		} catch (IOException e) {
			throw new BadRequestException(e.getMessage());
		} catch (IllegalArgumentException e) {
			throw new BadRequestException(e.getMessage());
		}

		return expressionService.getConceptAuthoringForm(conceptId, branchPath, extendedLocales);
	}

	@ApiOperation(
			value="Create Concept", 
			notes="Creates a new Concept directly on a branch.")
	@ApiResponses({
		@ApiResponse(code = 201, message = "Concept created on task"),
		@ApiResponse(code = 404, message = "Branch not found", response = RestApiError.class)
	})
	@RequestMapping(
			value="/{path:**}/concepts", 
			method=RequestMethod.POST, 
			consumes={ AbstractRestService.SO_MEDIA_TYPE, MediaType.APPLICATION_JSON_VALUE })
	@ResponseStatus(HttpStatus.CREATED)
	public ResponseEntity<Void> create(
			@ApiParam(value="The branch path")
			@PathVariable(value="path")
			final String branchPath,

			@ApiParam(value="Concept parameters")
			@RequestBody 
			final ChangeRequest<SnomedConceptRestInput> body,

			final Principal principal) {
		
		final String userId = principal.getName();
		
		final SnomedConceptRestInput change = body.getChange();
		
		final String commitComment = body.getCommitComment();
		
		Branch branch = RepositoryRequests.branching()
				.prepareGet(branchPath)
				.build(repositoryId)
				.execute(bus).getSync();
		
		final String createdConceptId = change
			.toRequestBuilder(branch)
			.build(repositoryId, branchPath, userId, commitComment)
			.execute(bus)
			.getSync(COMMIT_TIMEOUT, TimeUnit.MILLISECONDS)
			.getResultAs(String.class);
		
		
		return Responses.created(getConceptLocationURI(branchPath, createdConceptId)).build();
	}

	@ApiOperation(
			value="Update Concept",
			notes="Updates properties of the specified Concept, also managing inactivation indicator and association reference set "
					+ "membership in case of inactivation."
					+ "<p>The following properties are allowed to change:"
					+ "<p>"
					+ "&bull; module identifier<br>"
					+ "&bull; subclass definition status<br>"
					+ "&bull; definition status<br>"
					+ "&bull; associated Concepts<br>"
					+ ""
					+ "<p>The following properties, when changed, will trigger inactivation:"
					+ "<p>"
					+ "&bull; inactivation indicator<br>")
	@ApiResponses({
		@ApiResponse(code = 204, message = "Update successful"),
		@ApiResponse(code = 404, message = "Branch or Concept not found", response = RestApiError.class)
	})
	@RequestMapping(
			value="/{path:**}/concepts/{conceptId}/updates", 
			method=RequestMethod.POST,
			consumes={ AbstractRestService.SO_MEDIA_TYPE, MediaType.APPLICATION_JSON_VALUE })
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void update(			
			@ApiParam(value="The branch path")
			@PathVariable(value="path")
			final String branchPath,

			@ApiParam(value="The Concept identifier")
			@PathVariable(value="conceptId")
			final String conceptId,
			
			@ApiParam(value="Updated Concept parameters")
			@RequestBody 
			final ChangeRequest<SnomedConceptRestUpdate> body,

			final Principal principal) {

		final String userId = principal.getName();
		final String commitComment = body.getCommitComment();

		body.getChange().toRequestBuilder(conceptId)
			.build(repositoryId, branchPath, userId, commitComment)
			.execute(bus)
			.getSync(COMMIT_TIMEOUT, TimeUnit.MILLISECONDS);
	}

	@ApiOperation(
			value="Delete Concept",
			notes="Permanently removes the specified unreleased Concept and related components.<p>If any participating "
					+ "component has already been released the Concept can not be removed and a <code>409</code> "
					+ "status will be returned."
					+ "<p>The force flag enables the deletion of a released Concept. "
					+ "Deleting published components is against the RF2 history policy so"
					+ " this should only be used to remove a new component from a release before the release is published.</p>")
	@ApiResponses({
		@ApiResponse(code = 204, message = "Deletion successful"),
		@ApiResponse(code = 404, message = "Branch or Concept not found", response = RestApiError.class),
		@ApiResponse(code = 409, message = "Cannot be deleted if released", response = RestApiError.class)
	})
	@RequestMapping(value="/{path:**}/concepts/{conceptId}", method=RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(			
			@ApiParam(value="The branch path")
			@PathVariable(value="path")
			final String branchPath,

			@ApiParam(value="The Concept identifier")
			@PathVariable(value="conceptId")
			final String conceptId,

			@ApiParam(value="Force deletion flag")
			@RequestParam(defaultValue="false", required=false)
			final Boolean force,

			final Principal principal) {
		SnomedRequests
			.prepareDeleteConcept(conceptId)
			.force(force)
			.build(repositoryId, branchPath, principal.getName(), String.format("Deleted Concept '%s' from store.", conceptId))
			.execute(bus)
			.getSync(COMMIT_TIMEOUT, TimeUnit.MILLISECONDS);
	}

	private URI getConceptLocationURI(String branchPath, String conceptId) {
		return linkTo(SnomedConceptRestService.class).slash(branchPath).slash("concepts").slash(conceptId).toUri();
	}
}
