package com.b2international.snowowl.snomed.api.impl.domain;

public class Predicate {

	private String relationshipTypeExpression;
	private String relationshipValueExpression;

	public void setRelationshipTypeExpression(String relationshipTypeExpression) {
		this.relationshipTypeExpression = relationshipTypeExpression;
	}
	
	public String getRelationshipTypeExpression() {
		return relationshipTypeExpression;
	}
	
	public void setRelationshipValueExpression(
			String relationshipValueExpression) {
		this.relationshipValueExpression = relationshipValueExpression;
	}
	
	public String getRelationshipValueExpression() {
		return relationshipValueExpression;
	}

}
