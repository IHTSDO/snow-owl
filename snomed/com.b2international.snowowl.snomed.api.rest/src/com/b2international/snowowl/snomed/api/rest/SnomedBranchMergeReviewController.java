/*
 * Copyright 2011-2015 B2i Healthcare Pte Ltd, http://b2i.sg
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
import java.util.Collections;
import java.util.concurrent.TimeoutException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import com.b2international.commons.collections.Procedure;
import com.b2international.snowowl.core.exceptions.ApiValidation;
import com.b2international.snowowl.datastore.server.events.ConceptChangesReply;
import com.b2international.snowowl.datastore.server.events.MergeReviewDetailsReply;
import com.b2international.snowowl.datastore.server.events.MergeReviewReply;
import com.b2international.snowowl.datastore.server.events.ReadConceptChangesEvent;
import com.b2international.snowowl.datastore.server.events.ReadMergeReviewDetailsEvent;
import com.b2international.snowowl.datastore.server.events.ReadMergeReviewEvent;
import com.b2international.snowowl.datastore.server.review.ConceptChanges;
import com.b2international.snowowl.datastore.server.review.MergeReview;
import com.b2international.snowowl.datastore.server.review.MergeReviewIntersection;
import com.b2international.snowowl.eventbus.IEventBus;
import com.b2international.snowowl.snomed.api.browser.ISnomedBrowserService;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserMergeReviewDetails;
import com.b2international.snowowl.snomed.api.impl.domain.browser.SnomedBrowserMergeReviewDetails;
import com.b2international.snowowl.snomed.api.rest.domain.CreateMergeReviewRequest;
import com.b2international.snowowl.snomed.api.rest.domain.RestApiError;
import com.b2international.snowowl.snomed.api.rest.util.Responses;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

/**
 * Provides REST endpoints for computing Reviewerences between branches.
 * 
 * @since 4.2
 */
@Api("Branches")
@RestController
@RequestMapping(value="/merge-reviews", produces={AbstractRestService.SO_MEDIA_TYPE, MediaType.APPLICATION_JSON_VALUE})
public class SnomedBranchMergeReviewController extends AbstractRestService {

	@Autowired
	private IEventBus bus;
	
	@Autowired
	@Value("${codeSystemShortName}")
	protected String codeSystemShortName;
	
	@Autowired
	protected ISnomedBrowserService browserService;

	@ApiOperation(
			value = "Create new merge review", 
			notes = "Creates a new terminology merge review for the SNOMED CT repository.")
	@ApiResponses({
		@ApiResponse(code = 201, message = "Created"),
		@ApiResponse(code = 400, message = "Bad Request", response=RestApiError.class)
	})
	@RequestMapping(method=RequestMethod.POST)
	@ResponseStatus(HttpStatus.CREATED)
	public DeferredResult<ResponseEntity<Void>> createMergeReview(@RequestBody final CreateMergeReviewRequest request) {
		ApiValidation.checkInput(request);
		final DeferredResult<ResponseEntity<Void>> result = new DeferredResult<>();
		final ControllerLinkBuilder linkTo = linkTo(SnomedBranchMergeReviewController.class);
		request.toEvent(repositoryId)
			.send(bus, MergeReviewReply.class)
			.then(new Procedure<MergeReviewReply>() { @Override protected void doApply(final MergeReviewReply reply) {
				result.setResult(Responses.created(getLocationHeader(linkTo, reply)).build());
			}})
			.fail(new Procedure<Throwable>() { @Override protected void doApply(final Throwable t) {
				result.setErrorResult(t);
			}});
		return result;
	}
	
	@ApiOperation(
			value = "Retrieve single merge review", 
			notes = "Retrieves an existing terminology merge review with the specified identifier, if it exists.")
	@ApiResponses({
		@ApiResponse(code = 200, message = "OK"),
		@ApiResponse(code = 404, message = "Merge Review not found", response=RestApiError.class),
	})
	@RequestMapping(value="/{id}", method=RequestMethod.GET)
	public DeferredResult<MergeReview> getMergeReview(@PathVariable("id") final String mergeReviewId) {
		final DeferredResult<MergeReview> result = new DeferredResult<>();
		new ReadMergeReviewEvent(repositoryId, mergeReviewId)
			.send(bus, MergeReviewReply.class)
			.then(new Procedure<MergeReviewReply>() { @Override protected void doApply(final MergeReviewReply reply) {
				result.setResult(reply.getMergeReview());
			}})
			.fail(new Procedure<Throwable>() { @Override protected void doApply(final Throwable t) {
				result.setErrorResult(t);
			}});
		return result;
	}
	
	@ApiOperation(
			value = "Retrieve the details of a merge review", 
			notes = "Retrieves the set of modified concepts for a merge review with the specified identifier, if it exists.")
	@ApiResponses({
		@ApiResponse(code = 200, message = "OK"),
		@ApiResponse(code = 404, message = "Merge Review not found or changes are not yet available", response=RestApiError.class),
	})
	@RequestMapping(value="/{id}/details", method=RequestMethod.GET)
	public ISnomedBrowserMergeReviewDetails getMergeReviewDetails(@PathVariable("id") final String mergeReviewId, final HttpServletRequest request) throws Exception {
		final DeferredResult<MergeReviewIntersection> deferredIntersection = new DeferredResult<>();
		new ReadMergeReviewDetailsEvent(repositoryId, mergeReviewId)
			.send(bus, MergeReviewDetailsReply.class)
			.then(new Procedure<MergeReviewDetailsReply>() { @Override protected void doApply(final MergeReviewDetailsReply reply) {
				deferredIntersection.setResult(reply.getMergeReviewDetails());
			}})
			.fail(new Procedure<Throwable>() { @Override protected void doApply(final Throwable t) {
				deferredIntersection.setErrorResult(t);
			}});

		MergeReviewIntersection intersection = (MergeReviewIntersection) getResult(deferredIntersection);
		ISnomedBrowserMergeReviewDetails details = browserService.getConceptDetails(intersection.id(),
																					intersection.getIntersectingConcepts(), 
																					intersection.getSourceBranch(), 
																					intersection.getTargetBranch(),
																					codeSystemShortName,
																					Collections.list(request.getLocales()));
		return details;
	}
	
	private Object getResult(
			DeferredResult<?> deferred) throws Exception {
		
		int timePassed = 0;
		while (!deferred.hasResult()) {
			Thread.sleep(50);
			timePassed++;
			if (timePassed > 500) {
				throw new TimeoutException("Failed to recover response in reasonable time");
			}
		}
		
		//We need to pass that information back down the BrowserService as it can't be called directly.
		Object deferredResult = deferred.getResult();
		if (deferredResult instanceof Exception) {
			throw ((Exception)deferredResult);
		}
		return deferredResult;
	}

	private URI getLocationHeader(ControllerLinkBuilder linkBuilder, final MergeReviewReply reply) {
		return linkBuilder.slash(reply.getMergeReview().id()).toUri();
	}
}
