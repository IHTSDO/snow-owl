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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.b2international.snowowl.core.exceptions.ApiError;
import com.b2international.snowowl.core.exceptions.ApiErrorException;
import com.b2international.snowowl.core.exceptions.BadRequestException;
import com.b2international.snowowl.core.exceptions.ConflictException;
import com.b2international.snowowl.core.exceptions.MergeConflictException;
import com.b2international.snowowl.core.exceptions.NotFoundException;
import com.b2international.snowowl.core.exceptions.NotImplementedException;
import com.b2international.snowowl.core.exceptions.RequestTimeoutException;
import com.b2international.snowowl.snomed.api.rest.domain.RestApiError;
import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * @since 4.1
 */
@ControllerAdvice
public class ControllerExceptionMapper {

	private static final Logger LOG = LoggerFactory.getLogger(ControllerExceptionMapper.class);
	private static final String GENERIC_USER_MESSAGE = "Something went wrong during the processing of your request.";
	
	/**
	 * Generic <b>Internal Server Error</b> exception handler, serving as a fallback for RESTful client calls.
	 * 
	 * @param ex
	 * @return {@link RestApiError} instance with detailed messages
	 */
	@ExceptionHandler
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public @ResponseBody RestApiError handle(final Exception ex) {
		LOG.error("Exception during request processing", ex);
		return RestApiError.of(ApiError.Builder.of(GENERIC_USER_MESSAGE).build()).build(HttpStatus.INTERNAL_SERVER_ERROR.value());
	}
	
	@ExceptionHandler
	@ResponseStatus(HttpStatus.REQUEST_TIMEOUT)
	public @ResponseBody RestApiError handle(RequestTimeoutException ex) {
		LOG.error("Timeout during request processing", ex);
		return RestApiError.of(ApiError.Builder.of(GENERIC_USER_MESSAGE).build()).build(HttpStatus.REQUEST_TIMEOUT.value());
	}
	
	/**
	 * Exception handler converting any {@link JsonMappingException} to an <em>HTTP 400</em>.
	 * 
	 * @param ex
	 * @return {@link RestApiError} instance with detailed messages
	 */
	@ExceptionHandler
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public @ResponseBody RestApiError handle(HttpMessageNotReadableException ex) {
		LOG.error("Exception during processing of a JSON document", ex);
		return RestApiError.of(ApiError.Builder.of("Invalid JSON representation").developerMessage(ex.getMessage()).build()).build(HttpStatus.BAD_REQUEST.value());
	}
	
	@ExceptionHandler
	public ResponseEntity<RestApiError> handle(final ApiErrorException ex) {
		final ApiError error = ex.toApiError();
		return new ResponseEntity<>(RestApiError.of(error).build(error.getStatus()), HttpStatus.valueOf(error.getStatus()));
	}

	/**
	 * <b>Not Found</b> exception handler. All {@link NotFoundException not found exception}s are mapped to {@link HttpStatus#NOT_FOUND
	 * <em>404 Not Found</em>} in case of the absence of an instance resource.
	 * 
	 * @param ex
	 * @return {@link RestApiError} instance with detailed messages
	 */
	@ExceptionHandler
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public @ResponseBody RestApiError handle(final NotFoundException ex) {
		return RestApiError.of(ex.toApiError()).build(HttpStatus.NOT_FOUND.value());
	}

	/**
	 * Exception handler to return <b>Not Implemented</b> when an {@link UnsupportedOperationException} is thrown from the underlying system.
	 * 
	 * @param ex
	 * @return {@link RestApiError} instance with detailed messages
	 */
	@ExceptionHandler
	@ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
	public @ResponseBody RestApiError handle(NotImplementedException ex) {
		return RestApiError.of(ex.toApiError()).build(HttpStatus.NOT_IMPLEMENTED.value());
	}

	/**
	 * Exception handler to return <b>Bad Request</b> when an {@link BadRequestException} is thrown from the underlying system.
	 * 
	 * @param ex
	 * @return {@link RestApiError} instance with detailed messages
	 */
	@ExceptionHandler
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public @ResponseBody RestApiError handle(final BadRequestException ex) {
		return RestApiError.of(ex.toApiError()).build(HttpStatus.BAD_REQUEST.value());
	}
	
	@ExceptionHandler
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public @ResponseBody RestApiError handle(final IllegalArgumentException ex) {
		ex.printStackTrace();
		return RestApiError.of(ApiError.Builder.of(ex.getMessage()).build()).build(HttpStatus.BAD_REQUEST.value());
	}
	
	@ExceptionHandler
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public @ResponseBody RestApiError handle(final TypeMismatchException ex) {
		return RestApiError.of(ApiError.Builder.of(ex.getMessage()).build()).build(HttpStatus.BAD_REQUEST.value());
	}
	
	/**
	 * Exception handler to return <b>Bad Request</b> when an {@link BadRequestException} is thrown from the underlying system.
	 * 
	 * @param ex
	 * @return {@link RestApiError} instance with detailed messages
	 */
	@ExceptionHandler
	@ResponseStatus(HttpStatus.CONFLICT)
	public @ResponseBody RestApiError handle(final ConflictException ex) {
		if (ex.getCause() != null) {
			LOG.info("Conflict with cause", ex);
		}
		
		if (ex instanceof MergeConflictException) {
			LOG.info("Conflict details: {}", ex.toApiError().getAdditionalInfo());
		}
		
		return RestApiError.of(ex.toApiError()).build(HttpStatus.CONFLICT.value());
	}
}
