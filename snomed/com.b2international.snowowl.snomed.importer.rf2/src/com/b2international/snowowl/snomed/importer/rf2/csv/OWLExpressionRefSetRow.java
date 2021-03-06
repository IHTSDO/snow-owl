/*
 * Copyright 2018 B2i Healthcare Pte Ltd, http://b2i.sg
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
package com.b2international.snowowl.snomed.importer.rf2.csv;

/**
 * @since 6.5
 */
public class OWLExpressionRefSetRow extends RefSetRow {

	private String owlExpression;

	public String getOwlExpression() {
		return owlExpression;
	}

	public void setOwlExpression(final String owlExpression) {
		this.owlExpression = owlExpression;
	}

	@Override
	public String toString() {
		return String.format(
				"OWLExpressionRefSetRow [uuid=%s, effectiveTime=%s, active=%s, moduleId=%s, refsetId=%s, referencedComponentId=%s, owlExpression=%s]",
				getUuid(), getEffectiveTime(), isActive(), getModuleId(), getRefSetId(), getReferencedComponentId(), getOwlExpression());
	}

}
