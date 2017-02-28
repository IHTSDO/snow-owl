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
package com.b2international.snowowl.snomed.datastore.request;

import com.b2international.snowowl.snomed.datastore.id.request.SnomedIdentifierDeprecateRequestBuilder;
import com.b2international.snowowl.snomed.datastore.id.request.SnomedIdentifierGenerateRequestBuilder;
import com.b2international.snowowl.snomed.datastore.id.request.SnomedIdentifierGetRequestBuilder;
import com.b2international.snowowl.snomed.datastore.id.request.SnomedIdentifierPublishRequestBuilder;
import com.b2international.snowowl.snomed.datastore.id.request.SnomedIdentifierRegisterRequestBuilder;
import com.b2international.snowowl.snomed.datastore.id.request.SnomedIdentifierReleaseRequestBuilder;
import com.b2international.snowowl.snomed.datastore.id.request.SnomedIdentifierReserveRequestBuilder;

/**
 * @since 5.5
 */
public final class Identifiers {

	Identifiers() { }
	
	public SnomedIdentifierDeprecateRequestBuilder prepareDeprecate() {
		return new SnomedIdentifierDeprecateRequestBuilder();
	}
	
	public SnomedIdentifierGenerateRequestBuilder prepareGenerate() {
		return new SnomedIdentifierGenerateRequestBuilder();
	}
	
	public SnomedIdentifierGetRequestBuilder prepareGet() {
		return new SnomedIdentifierGetRequestBuilder();
	}
	
	public SnomedIdentifierPublishRequestBuilder preparePublish() {
		return new SnomedIdentifierPublishRequestBuilder();
	}
	
	public SnomedIdentifierRegisterRequestBuilder prepareRegister() {
		return new SnomedIdentifierRegisterRequestBuilder();
	}
	
	public SnomedIdentifierReleaseRequestBuilder prepareRelease() {
		return new SnomedIdentifierReleaseRequestBuilder();
	}
	
	public SnomedIdentifierReserveRequestBuilder prepareReserve() {
		return new SnomedIdentifierReserveRequestBuilder();
	}
	
}
