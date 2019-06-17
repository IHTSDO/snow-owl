/*
 * Copyright 2011-2015 B2i Healthcare Pte Ltd, http://b2i.sg
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.b2international.snowowl.snomed.api.impl.domain.browser;

import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserRelationshipTarget;
import com.b2international.snowowl.snomed.core.domain.DefinitionStatus;

public class SnomedBrowserRelationshipTarget extends SnomedBrowserComponent implements ISnomedBrowserRelationshipTarget {

	private String conceptId;
	private SnomedBrowserTerm fsn;
	private SnomedBrowserTerm pt;
	private DefinitionStatus definitionStatus;

	public SnomedBrowserRelationshipTarget() {
	}
	
	public SnomedBrowserRelationshipTarget(String conceptId) {
		this.conceptId = conceptId;
	}
	
	@Override
	public String getConceptId() {
		return conceptId;
	}

	@Override
	public SnomedBrowserTerm getFsn() {
		return fsn;
	}

	@Override
	public SnomedBrowserTerm getPt() {
		return pt;
	}
	
	@Override
	public DefinitionStatus getDefinitionStatus() {
		return definitionStatus;
	}

	public void setConceptId(final String conceptId) {
		this.conceptId = conceptId;
	}

	public void setFsn(final SnomedBrowserTerm fsn) {
		this.fsn = fsn;
	}

	public void setPt(SnomedBrowserTerm pt) {
		this.pt = pt;
	}
	
	public void setDefinitionStatus(final DefinitionStatus definitionStatus) {
		this.definitionStatus = definitionStatus;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((conceptId == null) ? 0 : conceptId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SnomedBrowserRelationshipTarget other = (SnomedBrowserRelationshipTarget) obj;
		if (conceptId == null) {
			if (other.conceptId != null)
				return false;
		} else if (!conceptId.equals(other.conceptId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("SnomedBrowserRelationshipTarget [conceptId=");
		builder.append(conceptId);
		builder.append(", fsn=");
		builder.append(fsn);
		builder.append(", pt=");
		builder.append(pt);
		builder.append(", definitionStatus=");
		builder.append(definitionStatus);
		builder.append(", getEffectiveTime()=");
		builder.append(getEffectiveTime());
		builder.append(", getModuleId()=");
		builder.append(getModuleId());
		builder.append(", isActive()=");
		builder.append(isActive());
		builder.append("]");
		return builder.toString();
	}
}
