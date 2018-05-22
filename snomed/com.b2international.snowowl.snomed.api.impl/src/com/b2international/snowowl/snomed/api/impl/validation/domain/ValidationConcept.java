package com.b2international.snowowl.snomed.api.impl.validation.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.ihtsdo.drools.domain.Description;
import org.ihtsdo.drools.domain.Relationship;

import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserConcept;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserDescription;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserRelationship;

public class ValidationConcept implements org.ihtsdo.drools.domain.Concept {

	private ISnomedBrowserConcept browserConcept;
	private List<Description> descriptions;
	private List<Relationship> relationships;

	public ValidationConcept(ISnomedBrowserConcept browserConcept) {
		this.browserConcept = browserConcept;
		String conceptId = browserConcept.getConceptId();
		descriptions = new ArrayList<>();
		for (ISnomedBrowserDescription browserDescription : browserConcept.getDescriptions()) {
			descriptions.add(new ValidationDescription(browserDescription, conceptId));
		}
		relationships = new ArrayList<>();
		for (ISnomedBrowserRelationship browserRelationship : browserConcept.getRelationships()) {
			relationships.add(new ValidationRelationship(browserRelationship, conceptId));
		}
	}
	
	public java.util.Collection<? extends org.ihtsdo.drools.domain.OntologyAxiom> getOntologyAxioms() {
		return new HashSet<>();
	}

	@Override
	public String getId() {
		return browserConcept.getConceptId();
	}
	
	@Override
	public boolean isActive() {
		return browserConcept.isActive();
	}
	
	@Override
	public boolean isPublished() {
		return browserConcept.getEffectiveTime() != null;
	}
	
	@Override
	public boolean isReleased() {
		return browserConcept.isReleased();
	}
	
	@Override
	public String getModuleId() {
		return browserConcept.getModuleId();
	}

	@Override
	public String getDefinitionStatusId() {
		return browserConcept.getDefinitionStatus().getConceptId();
	}

	@Override
	public Collection<Description> getDescriptions() {
		return descriptions;
	}

	@Override
	public Collection<Relationship> getRelationships() {
		return relationships;
	}

}
