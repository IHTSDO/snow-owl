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
package com.b2international.snowowl.snomed.reasoner.server.classification.external;

import com.b2international.snowowl.core.exceptions.ApiException;
import com.b2international.snowowl.retrofit.Error;

/**
 * @since 5.10.13
 */
public class ExternalClassificationServiceError implements Error {

	@Override
	public ApiException toApiException(int status) {
		return new ExternalClassificationServiceException(String.format("External classification service returned code: %s", getMessage(status)));
	}

	private String getMessage(int status) {
		
		if (status == 401) {
			return String.format("%s - %s", status, "Unauthorized");
		} else if (status == 404) {
			return String.format("%s - %s", status, "Not found");
		} else if (status == 403) {
			return String.format("%s - %s", status, "Forbidden");
		}
		
		return String.valueOf(status);
	}

}
