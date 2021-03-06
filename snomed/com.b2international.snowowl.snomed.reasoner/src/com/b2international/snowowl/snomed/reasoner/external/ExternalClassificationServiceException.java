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
package com.b2international.snowowl.snomed.reasoner.external;

import com.b2international.snowowl.core.exceptions.ApiException;

/**
 * @since 5.10.13
 */
public class ExternalClassificationServiceException extends ApiException {

	private static final long serialVersionUID = 3659761907306808816L;
	private static final int DEFAULT_STATUS_CODE = 500;
	
	private int statusCode;

	public ExternalClassificationServiceException(final String template, final Object...args) {
		this(DEFAULT_STATUS_CODE, template, args);
	}
	
	public ExternalClassificationServiceException(final int statusCode, final String template, final Object...args) {
		super(template, args);
		this.statusCode = statusCode;
	}
	
	@Override
	protected Integer getStatus() {
		return statusCode;
	}
	
}
