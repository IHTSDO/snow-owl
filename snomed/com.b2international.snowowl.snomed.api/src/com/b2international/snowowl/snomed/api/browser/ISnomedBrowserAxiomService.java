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
package com.b2international.snowowl.snomed.api.browser;

import java.util.Collection;
import java.util.List;

import org.snomed.otf.owltoolkit.conversion.ConversionException;
import org.snomed.otf.owltoolkit.domain.AxiomRepresentation;

import com.b2international.commons.http.ExtendedLocale;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserConcept;

public interface ISnomedBrowserAxiomService {

	public Collection<? extends ISnomedBrowserConcept> expandAxioms(Collection<? extends ISnomedBrowserConcept> concepts, String branchPath, List<ExtendedLocale> locales);

	public AxiomRepresentation convertAxiomToRelationships(String owlExpression, String branchPath) throws ConversionException;
	
	public String convertRelationshipsToAxiom(AxiomRepresentation axiomRepresentation, String branchPath);

}
