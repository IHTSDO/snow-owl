package com.b2international.snowowl.snomed.api.impl.validation.domain;

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import org.ihtsdo.drools.domain.Description;
import org.ihtsdo.drools.domain.OntologyAxiom;
import org.ihtsdo.drools.domain.Relationship;

import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserConcept;

public class ValidationConcept implements org.ihtsdo.drools.domain.Concept {

	private ISnomedBrowserConcept browserConcept;
	private List<Description> descriptions;
	private List<Relationship> relationships;

	public ValidationConcept(ISnomedBrowserConcept browserConcept) {
		this.browserConcept = browserConcept;
		String conceptId = browserConcept.getConceptId();
		
		descriptions.addAll(browserConcept.getDescriptions().stream()
				.map(desc -> new ValidationDescription(desc, conceptId)).collect(toList()));
		
		relationships.addAll(browserConcept.getRelationships().stream()
				.map(relationship -> new ValidationRelationship(relationship, conceptId)).collect(toList()));
		
		browserConcept.getClassAxioms()
			.forEach(axiom -> {
				axiom.getRelationships().forEach(relationship -> {
					relationships.add(new ValidationAxiomRelationship(axiom, relationship, conceptId));
				});
			});
		
		browserConcept.getGciAxioms()
			.forEach(axiom -> {
				axiom.getRelationships().forEach(relationship -> {
					relationships.add(new ValidationAxiomRelationship(axiom, relationship, conceptId));
				});
			});
		
	}
	
	public java.util.Collection<? extends OntologyAxiom> getOntologyAxioms() {
		return Stream.concat(
					browserConcept.getClassAxioms().stream().map(axiom -> new ValidationOntologyAxiom(axiom)),
					browserConcept.getGciAxioms().stream().map(axiom -> new ValidationOntologyAxiom(axiom)))
				.collect(toList());
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
