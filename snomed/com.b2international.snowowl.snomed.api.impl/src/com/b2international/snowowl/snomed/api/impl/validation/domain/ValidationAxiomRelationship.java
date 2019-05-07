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

import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserAxiom;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserRelationship;

/**
 * @since 6.14.3 
 */
public class ValidationAxiomRelationship extends ValidationRelationship {

	private ISnomedBrowserAxiom axiom;

	public ValidationAxiomRelationship(ISnomedBrowserAxiom axiom, ISnomedBrowserRelationship relationship, String conceptId) {
		super(relationship, conceptId);
		this.axiom = axiom;
	}
	
	@Override
	public String getId() {
		return String.format("%s_%s_%s_%s", getAxiomId(), getRelationshipGroup(), getTypeId(), getDestinationId());
	}
	
	@Override
	public String getAxiomId() {
		return axiom.getAxiomId();
	}
	
	@Override
	public boolean isPublished() {
		return axiom.getEffectiveTime() != null;
	}
	
	@Override
	public boolean isReleased() {
		return axiom.isReleased();
	}
	
	@Override
	public String getModuleId() {
		return axiom.getModuleId();
	}
	
	
}
