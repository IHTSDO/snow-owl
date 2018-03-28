/*
 * Copyright 2011-2018 B2i Healthcare Pte Ltd, http://b2i.sg
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

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Representation of a SnomedConcept for easier mapping to Csv format
 */
@JsonPropertyOrder({ "id", "fsn", "effectiveTime", "active", "moduleId", "definitionStatus"})
public class SnomedCsvConcept {

	private final String id;
	private final String effectiveTime;
	private final boolean active;
	private final String moduleId;
	private final String definitionStatus;
	private final String fsn;

	public SnomedCsvConcept(String id, String effectiveTime, boolean active, String moduleId, String definitionStatus, String fsn) {
		this.id = id;
		this.effectiveTime = effectiveTime;
		this.active = active;
		this.moduleId = moduleId;
		this.definitionStatus = definitionStatus;
		this.fsn = fsn;
	}

	public String getId() {
		return id;
	}

	public String getEffectiveTime() {
		return effectiveTime;
	}

	public boolean isActive() {
		return active;
	}

	public String getModuleId() {
		return moduleId;
	}

	public String getDefinitionStatus() {
		return definitionStatus;
	}

	public String getFsn() {
		return fsn;
	}

}
