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
package com.b2international.snowowl.snomed.api.impl.domain.browser;

import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserParentConcept;
import com.b2international.snowowl.snomed.core.domain.DefinitionStatus;

public class SnomedBrowserParentConcept implements ISnomedBrowserParentConcept {

	private String conceptId;
	private SnomedBrowserTerm fsn;
	private SnomedBrowserTerm preferredSynonym;
	private DefinitionStatus definitionStatus;
	private boolean active;
	private String moduleId;

	@Override
	public String getConceptId() {
		return conceptId;
	}

	@Override
	public SnomedBrowserTerm getFsn() {
		return fsn;
	}
	
	@Override
	public SnomedBrowserTerm getPreferredSynonym() {
		return preferredSynonym;
	}

	@Override
	public DefinitionStatus getDefinitionStatus() {
		return definitionStatus;
	}

	@Override
	public boolean isActive() {
		return active;
	}
	
	@Override
	public String getModuleId() {
		return moduleId;
	}
	
	public void setConceptId(final String conceptId) {
		this.conceptId = conceptId;
	}

	public void setFsn(final SnomedBrowserTerm fsn) {
		this.fsn = fsn;
	}

	public void setPreferredSynonym(SnomedBrowserTerm preferredSynonym) {
		this.preferredSynonym = preferredSynonym;
	}

	public void setDefinitionStatus(final DefinitionStatus definitionStatus) {
		this.definitionStatus = definitionStatus;
	}
	
	public void setActive(boolean acitve) {
		this.active = acitve;
	}
	
	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("SnomedBrowserParentConcept [conceptId=");
		builder.append(conceptId);
		builder.append(", fsn=");
		builder.append(fsn);
		builder.append(", preferredSynonym=");
		builder.append(preferredSynonym);
		builder.append(", definitionStatus=");
		builder.append(definitionStatus);
		builder.append(", active=");
		builder.append(active);
		builder.append(", moduleId=");
		builder.append(moduleId);
		builder.append("]");
		return builder.toString();
	}
}
