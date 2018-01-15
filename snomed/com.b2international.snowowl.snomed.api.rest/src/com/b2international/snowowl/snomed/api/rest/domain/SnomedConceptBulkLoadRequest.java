package com.b2international.snowowl.snomed.api.rest.domain;

import java.util.Set;

public class SnomedConceptBulkLoadRequest {

	private Set<String> conceptIds;

	public Set<String> getConceptIds() {
		return conceptIds;
	}
	
	public void setConceptIds(Set<String> conceptIds) {
		this.conceptIds = conceptIds;
	}
}
