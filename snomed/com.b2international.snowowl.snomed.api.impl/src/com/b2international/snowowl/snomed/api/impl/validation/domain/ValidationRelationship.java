package com.b2international.snowowl.snomed.api.impl.validation.domain;

import org.ihtsdo.drools.domain.Relationship;

import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserRelationship;

public class ValidationRelationship implements Relationship {

	private ISnomedBrowserRelationship browserRelationship;
	private String conceptId;

	public ValidationRelationship(ISnomedBrowserRelationship browserRelationship, String conceptId) {
		this.browserRelationship = browserRelationship;
		this.conceptId = conceptId;
	}

	@Override
	public String getId() {
		return browserRelationship.getId();
	}

	@Override
	public boolean isActive() {
		return browserRelationship.isActive();
	}

	@Override
	public boolean isPublished() {
		return browserRelationship.getEffectiveTime() != null;
	}
	
	@Override
	public boolean isReleased() {
		return browserRelationship.isReleased();
	}
	
	@Override
	public String getModuleId() {
		return browserRelationship.getModuleId();
	}
	
	@Override
	public String getAxiomId() {
		return null;
	}

	@Override
	public boolean isAxiomGCI() {
		return false;
	}
	
	@Override
	public String getDestinationId() {
		return browserRelationship.getTarget().getConceptId();
	}

	@Override
	public int getRelationshipGroup() {
		return browserRelationship.getGroupId();
	}

	@Override
	public String getSourceId() {
		return conceptId;
	}

	@Override
	public String getTypeId() {
		return browserRelationship.getType().getConceptId();
	}

	@Override
	public String getCharacteristicTypeId() {
		return browserRelationship.getCharacteristicType().getConceptId();
	}

}
