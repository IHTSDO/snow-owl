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

import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserDescriptionResult;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserDescriptionResultDetails;

public class SnomedBrowserDescriptionResult implements ISnomedBrowserDescriptionResult {

	private String term;
	private boolean active;
	private ISnomedBrowserDescriptionResultDetails concept;

	@Override
	public String getTerm() {
		return term;
	}

	@Override
	public boolean isActive() {
		return active;
	}

	@Override
	public ISnomedBrowserDescriptionResultDetails getConcept() {
		return concept;
	}

	public void setTerm(final String term) {
		this.term = term;
	}

	public void setActive(final boolean active) {
		this.active = active;
	}

	public void setConcept(final ISnomedBrowserDescriptionResultDetails concept) {
		this.concept = concept;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("SnomedBrowserDescriptionResult [term=");
		builder.append(term);
		builder.append(", active=");
		builder.append(active);
		builder.append(", concept=");
		builder.append(concept);
		builder.append("]");
		return builder.toString();
	}
}
