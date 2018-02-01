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
package com.b2international.snowowl.snomed.api.rest.domain;

import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;

import com.b2international.snowowl.snomed.datastore.request.SnomedRefSetMemberCreateRequestBuilder;
import com.b2international.snowowl.snomed.datastore.request.SnomedRequests;

/**
 * @since 4.5
 */
public class SnomedRefSetMemberRestInput {

	public static final String ADDITIONAL_FIELDS = "additionalFields";
	
	private Boolean active = Boolean.TRUE;
	private String moduleId;
	private String referencedComponentId;
	private String refsetId;
	private Map<String, Object> additionalFields = newHashMap();

	public Map<String, Object> getAdditionalFields() {
		return additionalFields;
	}
	
	public void setAdditionalFields(Map<String, Object> additionalFields) {
		this.additionalFields = additionalFields;
	}
	
	public void setActive(Boolean active) {
		this.active = active;
	}
	
	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}
	
	public void setReferencedComponentId(String referencedComponentId) {
		this.referencedComponentId = referencedComponentId;
	}
	
	public void setReferenceSetId(String referenceSetId) {
		this.refsetId = referenceSetId;
	}
	
	public Boolean isActive() {
		return active;
	}
	
	public String getModuleId() {
		return moduleId;
	}
	
	public String getReferencedComponentId() {
		return referencedComponentId;
	}
	
	public String getReferenceSetId() {
		return refsetId;
	}

	public SnomedRefSetMemberCreateRequestBuilder toRequestBuilder() {
		final SnomedRefSetMemberCreateRequestBuilder req = SnomedRequests.prepareNewMember();
		req.setActive(isActive());
		req.setReferenceSetId(getReferenceSetId());
		req.setReferencedComponentId(getReferencedComponentId());
		req.setModuleId(getModuleId());
		req.setProperties(getAdditionalFields());
		return req;
	}
	
}
