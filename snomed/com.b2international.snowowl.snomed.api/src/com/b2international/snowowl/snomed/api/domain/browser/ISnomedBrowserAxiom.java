package com.b2international.snowowl.snomed.api.domain.browser;

import java.util.List;

import com.b2international.snowowl.snomed.core.domain.DefinitionStatusProvider;

public interface ISnomedBrowserAxiom extends ISnomedBrowserComponentWithId, DefinitionStatusProvider {

	String getAxiomId();
	
	List<ISnomedBrowserRelationship> getRelationships();
	
}
