package com.b2international.snowowl.snomed.api.impl.domain.browser;

import java.util.List;

import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserAxiom;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserRelationship;
import com.b2international.snowowl.snomed.core.domain.DefinitionStatus;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.collect.ImmutableList;

public class SnomedBrowserAxiom extends SnomedBrowserComponent implements ISnomedBrowserAxiom {

	private String axiomId;
	private DefinitionStatus definitionStatus;

	@JsonDeserialize(contentAs=SnomedBrowserRelationship.class)
	private List<ISnomedBrowserRelationship> relationships = ImmutableList.of();

	@Override
	public String getId() {
		return axiomId;
	}

	@Override
	public String getAxiomId() {
		return axiomId;
	}

	@Override
	public DefinitionStatus getDefinitionStatus() {
		return definitionStatus;
	}

	@Override
	public List<ISnomedBrowserRelationship> getRelationships() {
		return relationships;
	}
	
	public void setDefinitionStatus(final DefinitionStatus definitionStatus) {
		this.definitionStatus = definitionStatus;
	}

	public void setRelationships(final List<ISnomedBrowserRelationship> relationships) {
		this.relationships = relationships;
	}
	
	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("SnomedBrowserAxiom [axiomId=");
		builder.append(axiomId);
		builder.append(", definitionStatus=");
		builder.append(definitionStatus);
		builder.append(", relationships=");
		builder.append(relationships);
		builder.append(", getEffectiveTime()=");
		builder.append(getEffectiveTime());
		builder.append(", getModuleId()=");
		builder.append(getModuleId());
		builder.append(", isActive()=");
		builder.append(isActive());
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((axiomId == null) ? 0 : axiomId.hashCode());
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
		SnomedBrowserAxiom other = (SnomedBrowserAxiom) obj;
		if (axiomId == null) {
			if (other.axiomId != null)
				return false;
		} else if (!axiomId.equals(other.axiomId))
			return false;
		return true;
	}
	
}
