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
package com.b2international.snowowl.snomed.api.domain.browser;

import java.util.List;

import com.b2international.snowowl.snomed.core.domain.AssociationType;
import com.b2international.snowowl.snomed.core.domain.DefinitionStatusProvider;
import com.b2international.snowowl.snomed.core.domain.InactivationIndicator;
import com.google.common.collect.Multimap;

/**
 * Represents a specific SNOMED CT concept, carrying information for use in the IHTSDO SNOMED CT Browser.
 */
public interface ISnomedBrowserConcept extends ISnomedBrowserComponentWithId, IConceptIdWithFsnProvider, DefinitionStatusProvider, TaxonomyNode {

	/** @return the Preferred Term of this concept for the requested language code and dialect */
	String getPreferredSynonym();
	
	/** @return the list of descriptions associated with this concept */
	List<ISnomedBrowserDescription> getDescriptions();
	
	/** @return the list of relationships associated with this concept */
	List<ISnomedBrowserRelationship> getRelationships();
	
	List<ISnomedBrowserAxiom> getClassAxioms();
	
	List<ISnomedBrowserAxiom> getGciAxioms();
	
	/** @return the inactivation indicator value; not set if the concept is still active, or no reason was given */
	InactivationIndicator getInactivationIndicator();

	/** @return related association targets; not set if the concept is still active, or no targets were specified */
	Multimap<AssociationType, String> getAssociationTargets();	
}
