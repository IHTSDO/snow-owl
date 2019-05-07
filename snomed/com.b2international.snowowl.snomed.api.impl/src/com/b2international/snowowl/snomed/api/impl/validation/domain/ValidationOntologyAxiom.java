/*
 * Copyright 2011-2019 B2i Healthcare Pte Ltd, http://b2i.sg
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
package com.b2international.snowowl.snomed.api.impl.validation.domain;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;

import org.ihtsdo.drools.domain.OntologyAxiom;

import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserAxiom;

/**
 * @since 6.14.3 
 */
public class ValidationOntologyAxiom implements OntologyAxiom {

	private ISnomedBrowserAxiom browserAxiom;

	public ValidationOntologyAxiom(ISnomedBrowserAxiom browserAxiom) {
		this.browserAxiom = checkNotNull(browserAxiom);
	}
	
	@Override
	public String getId() {
		return browserAxiom.getAxiomId();
	}

	@Override
	public String getModuleId() {
		return browserAxiom.getModuleId();
	}

	@Override
	public boolean isActive() {
		return browserAxiom.isActive();
	}

	@Override
	public boolean isPublished() {
		return browserAxiom.getEffectiveTime() != null;
	}

	@Override
	public boolean isReleased() {
		return browserAxiom.isReleased();
	}

	@Override
	public String getOwlExpression() {
		return browserAxiom.getOwlExpression();
	}

	@Override
	public Collection<String> getOwlExpressionNamedConcepts() {
		return browserAxiom.getNamedConceptIds();
	}

	@Override
	public String getReferencedComponentId() {
		return browserAxiom.getReferencedComponentId();
	}

}
