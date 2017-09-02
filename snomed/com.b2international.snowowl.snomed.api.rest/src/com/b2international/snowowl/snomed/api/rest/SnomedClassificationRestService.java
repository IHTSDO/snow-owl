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

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

import java.net.URI;
import java.security.Principal;
import java.util.List;

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

import com.b2international.commons.http.ExtendedLocale;
import com.b2international.snowowl.core.ApplicationContext;
import com.b2international.snowowl.core.domain.CollectionResource;
import com.b2international.snowowl.core.domain.PageableCollectionResource;
import com.b2international.snowowl.core.exceptions.ApiValidation;
import com.b2international.snowowl.core.exceptions.BadRequestException;
import com.b2international.snowowl.snomed.api.ISnomedClassificationService;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserConcept;
import com.b2international.snowowl.snomed.api.domain.classification.ClassificationStatus;
import com.b2international.snowowl.snomed.api.domain.classification.IClassificationRun;
import com.b2international.snowowl.snomed.api.domain.classification.IEquivalentConceptSet;
import com.b2international.snowowl.snomed.api.domain.classification.IRelationshipChange;
import com.b2international.snowowl.snomed.api.domain.classification.IRelationshipChangeList;
import com.b2international.snowowl.snomed.api.rest.domain.ClassificationRestInput;
import com.b2international.snowowl.snomed.api.rest.domain.ClassificationRunRestUpdate;
import com.b2international.snowowl.snomed.api.rest.domain.RestApiError;
import com.b2international.snowowl.snomed.api.rest.util.Responses;
import com.b2international.snowowl.snomed.datastore.config.SnomedCoreConfiguration;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

/**
 * @since 1.0
 */
@Api("Classifications")
@Controller
@RequestMapping(produces={ AbstractRestService.SO_MEDIA_TYPE, MediaType.APPLICATION_JSON_VALUE })
public class SnomedClassificationRestService extends AbstractSnomedRestService {

	@Autowired
	protected ISnomedClassificationService delegate;
	
	@Autowired
	protected SnomedResourceExpander resourceExpander;

	@ApiOperation(
			value="Retrieve classification runs from branch", 
			notes="Returns a list of classification runs for a branch.")
	@ApiResponses({
		@ApiResponse(code = 200, message = "OK"),
		@ApiResponse(code = 404, message = "Branch not found", response=RestApiError.class)
	})
	@RequestMapping(value="/{path:**}/classifications", method=RequestMethod.GET)
	public @ResponseBody CollectionResource<IClassificationRun> getAllClassificationRuns(
			@ApiParam(value="The branch path")
			@PathVariable(value="path") 
			final String branchPath) {

		return CollectionResource.of(delegate.getAllClassificationRuns(branchPath));
	}

	@ApiOperation(
			value="Start a classification on a branch",
			notes = "Classification runs are async jobs. The call to this method immediately returns with a unique URL "
					+ "pointing to the classification run.<p>The URL can be used to fetch the state of the classification "
					+ "to determine whether it's completed or not.")
	@ApiResponses({
		@ApiResponse(code = 201, message = "Created"),
		@ApiResponse(code = 404, message = "Branch not found", response=RestApiError.class)
	})
	@RequestMapping(
			value="/{path:**}/classifications", 
			method=RequestMethod.POST,
			consumes={ AbstractRestService.SO_MEDIA_TYPE, MediaType.APPLICATION_JSON_VALUE })
	@ResponseStatus(value=HttpStatus.CREATED)
	public ResponseEntity<Void> beginClassification(
			@ApiParam(value="The branch path")
			@PathVariable(value="path") 
			final String branchPath,

			@ApiParam(value="Classification parameters")
			@RequestBody 
			final ClassificationRestInput request,

			final Principal principal) {
		ApiValidation.checkInput(request);
		
		if (request.isUseExternalService() && !isExternalServiceConfigPresent()) {
			throw new BadRequestException("External classification service is not configured properly");
		}
		
		final IClassificationRun classificationRun = delegate.beginClassification(branchPath, request.getReasonerId(), request.isUseExternalService(), principal.getName());
		return Responses.created(getClassificationUri(branchPath, classificationRun)).build();
	}
	
	private boolean isExternalServiceConfigPresent() {
		SnomedCoreConfiguration snomedConfig = ApplicationContext.getServiceForClass(SnomedCoreConfiguration.class);
		return snomedConfig.getClassificationConfig() != null && snomedConfig.getClassificationConfig().isExternalClassificationServiceConfigured();
	}

	@ApiOperation(
			value="Retrieve the state of a classification run from branch")
	@ApiResponses({
		@ApiResponse(code = 200, message = "OK"),
		@ApiResponse(code = 404, message = "Branch or classification not found", response=RestApiError.class)
	})
	@RequestMapping(value="/{path:**}/classifications/{classificationId}", method=RequestMethod.GET)
	public @ResponseBody IClassificationRun getClassificationRun(
			@ApiParam(value="The branch path")
			@PathVariable(value="path") 
			final String branchPath,
			
			@ApiParam(value="The classification identifier")
			@PathVariable(value="classificationId") 
			final String classificationId) {

		return delegate.getClassificationRun(branchPath, classificationId);
	}

	@ApiOperation(
			value="Retrieve equivalent concepts from a classification run on a branch",
			notes="Returns a list of equivalent concept sets if the classification completed successfully; an empty JSON array "
					+ " is returned if the classification hasn't finished yet, or no equivalent concepts could be found.")
	@ApiResponses({
		@ApiResponse(code = 200, message = "OK"),
		@ApiResponse(code = 404, message = "Branch or classification not found", response=RestApiError.class)
	})
	@RequestMapping(value="/{path:**}/classifications/{classificationId}/equivalent-concepts", method=RequestMethod.GET)
	public @ResponseBody CollectionResource<IEquivalentConceptSet> getEquivalentConceptSets(
			@ApiParam(value="The branch path")
			@PathVariable(value="path") 
			final String branchPath,

			@ApiParam(value="The classification identifier")
			@PathVariable(value="classificationId") 
			final String classificationId,
			
			@ApiParam(value="Accepted language tags, in order of preference")
			@RequestHeader(value="Accept-Language", defaultValue="en-US;q=0.8,en-GB;q=0.6", required=false) 
			final String acceptLanguage) {

		final List<ExtendedLocale> extendedLocales = getExtendedLocales(acceptLanguage);
		return CollectionResource.of(delegate.getEquivalentConceptSets(branchPath, classificationId, extendedLocales));
	}

	@ApiOperation(
			value="Retrieve relationship changes made by a classification run on a branch",
			notes="Returns a list of relationship changes if the classification completed successfully; an empty JSON array "
					+ " is returned if the classification hasn't finished yet, or no changed relationships could be found.")
	@ApiResponses({
		@ApiResponse(code = 200, message = "OK"),
		@ApiResponse(code = 404, message = "Branch or classification not found", response=RestApiError.class)
	})
	@RequestMapping(value="/{path:**}/classifications/{classificationId}/relationship-changes", method=RequestMethod.GET, produces={"application/json", "text/csv"})
	public @ResponseBody PageableCollectionResource<IRelationshipChange> getRelationshipChanges(
			@ApiParam(value="The branch path")
			@PathVariable(value="path") 
			final String branchPath,

			@ApiParam(value="The classification identifier")
			@PathVariable(value="classificationId") 
			final String classificationId,
			
			@ApiParam(value="What parts of the response information to expand.", allowableValues="source.fsn, type.fsn, destination.fsn", allowMultiple=true)
			@RequestParam(value="expand", defaultValue="", required=false)
			final List<String> expand,

			@ApiParam(value="The starting offset in the list")
			@RequestParam(value="offset", defaultValue="0", required=false) 
			final int offset,

			@ApiParam(value="The maximum number of items to return")
			@RequestParam(value="limit", defaultValue="50", required=false) 
			final int limit,

			@ApiParam(value="Accepted language tags, in order of preference")
			@RequestHeader(value="Accept-Language", defaultValue="en-US;q=0.8,en-GB;q=0.6", required=false) 
			final String acceptLanguage) {

		final IRelationshipChangeList relationshipChangeList = delegate.getRelationshipChanges(branchPath, classificationId, offset, limit);
		List<IRelationshipChange> changes = relationshipChangeList.getChanges();
		if (!changes.isEmpty()) {
			changes = resourceExpander.expandRelationshipChanges(branchPath, changes, getExtendedLocales(acceptLanguage), expand);
		}
		return PageableCollectionResource.of(changes, offset, limit, relationshipChangeList.getTotal());
	}

	@ApiOperation(
			value="Retrieve a preview of a concept with classification changes applied",
			notes="Retrieves a preview of single concept and related information on a branch with classification changes applied.")
	@ApiResponses({
			@ApiResponse(code = 200, message = "OK", response = Void.class),
			@ApiResponse(code = 404, message = "Code system version or concept not found", response = RestApiError.class)
	})
	@RequestMapping(value="/{path:**}/classifications/{classificationId}/concept-preview/{conceptId}", method=RequestMethod.GET)
	public @ResponseBody ISnomedBrowserConcept getConceptDetails(
			@ApiParam(value="The branch path")
			@PathVariable(value="path")
			final String branchPath,

			@ApiParam(value="The classification identifier")
			@PathVariable(value="classificationId")
			final String classificationId,

			@ApiParam(value="The concept identifier")
			@PathVariable(value="conceptId")
			final String conceptId,

			@ApiParam(value="Language codes and reference sets, in order of preference")
			@RequestHeader(value="Accept-Language", defaultValue="en-US;q=0.8,en-GB;q=0.6", required=false)
			final String acceptLanguage) {

		return delegate.getConceptPreview(branchPath, classificationId, conceptId, getExtendedLocales(acceptLanguage));
	}

	@ApiOperation(
			value="Update a classification run on a branch",
			notes="Update the specified classification run by changing its state property. Saving the results is an async operation "
					+ "due to the possible high number of changes. It is advised to fetch the state of the classification run until "
					+ "the state changes to 'SAVED' or 'SAVE_FAILED'.<p>"
					+ "Currently only the state can be changed from 'COMPLETED' to 'SAVED'.")
	@ApiResponses({
		@ApiResponse(code = 204, message = "No content, update successful"),
		@ApiResponse(code = 404, message = "Branch or classification not found", response=RestApiError.class)
	})
	@RequestMapping(
			value="/{path:**}/classifications/{classificationId}", 
			method=RequestMethod.PUT,
			consumes={ AbstractRestService.SO_MEDIA_TYPE, MediaType.APPLICATION_JSON_VALUE })
	@ResponseStatus(value=HttpStatus.NO_CONTENT)
	public void updateClassificationRun(
			@ApiParam(value="The branch path")
			@PathVariable(value="path") 
			final String branchPath,

			@ApiParam(value="The classification identifier")
			@PathVariable(value="classificationId") 
			final String classificationId,

			@ApiParam(value="The updated classification parameters")
			@RequestBody 
			final ClassificationRunRestUpdate updatedRun,

			final Principal principal) {
		
		// TODO: compare all fields to find out what the client wants us to do, check for conflicts, etc.
		if (ClassificationStatus.SAVED.equals(updatedRun.getStatus())) {
			delegate.persistChanges(branchPath, classificationId, principal.getName());
		}
	}

	@ApiOperation(
			value="Removes a classification run from a branch",
			notes="Classification runs remain available until they are explicitly deleted by the client. Results of the classification cannot be retrieved after deletion.")
	@ApiResponses({
		@ApiResponse(code = 204, message = "No content, delete successful"),
		@ApiResponse(code = 404, message = "Branch or classification not found", response=RestApiError.class)
	})
	@RequestMapping(value="/{path:**}/classifications/{classificationId}", method=RequestMethod.DELETE)
	@ResponseStatus(value=HttpStatus.NO_CONTENT)
	public void deleteClassificationRun(
			@ApiParam(value="The branch path")
			@PathVariable(value="path") 
			final String branchPath,

			@ApiParam(value="The classification identifier")
			@PathVariable(value="classificationId") 
			final String classificationId) {
		
		delegate.removeClassificationRun(branchPath, classificationId);
	}

	private URI getClassificationUri(final String branchPath, final IClassificationRun classificationRun) {
		return linkTo(SnomedClassificationRestService.class).slash(branchPath).slash("classifications").slash(classificationRun.getId()).toUri();
	}

}
