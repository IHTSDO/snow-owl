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
package com.b2international.snowowl.snomed.core.domain;

/**
 * Represents a SNOMEd&nbsp;CT relationship.
 * 
 */
public class SnomedRelationship extends BaseSnomedCoreComponent implements ISnomedRelationship {

	private boolean destinationNegated;
	private Integer group;
	private Integer unionGroup;
	private CharacteristicType characteristicType;
	private RelationshipRefinability refinability;
	private RelationshipModifier modifier;
	private ISnomedConcept source;
	private ISnomedConcept destination;
	private ISnomedConcept type;

	public SnomedRelationship() {
	}
	
	public SnomedRelationship(String id) {
		setId(id);
	}

	@Override
	public String getSourceId() {
		return getSourceConcept().getId();
	}
	
	@Override
	public ISnomedConcept getSourceConcept() {
		return source;
	}

	@Override
	public String getDestinationId() {
		return getDestinationConcept().getId();
	}
	
	@Override
	public ISnomedConcept getDestinationConcept() {
		return destination;
	}

	@Override
	public boolean isDestinationNegated() {
		return destinationNegated;
	}

	@Override
	public String getTypeId() {
		return getTypeConcept().getId();
	}
	
	@Override
	public ISnomedConcept getTypeConcept() {
		return type;
	}

	@Override
	public Integer getGroup() {
		return group;
	}

	@Override
	public Integer getUnionGroup() {
		return unionGroup;
	}

	@Override
	public CharacteristicType getCharacteristicType() {
		return characteristicType;
	}

	@Override
	public RelationshipRefinability getRefinability() {
		return refinability;
	}

	@Override
	public RelationshipModifier getModifier() {
		return modifier;
	}

	public void setSource(ISnomedConcept source) {
		this.source = source;
	}
	
	public void setDestination(ISnomedConcept destination) {
		this.destination = destination;
	}
	
	public void setType(ISnomedConcept type) {
		this.type = type;
	}
	
	public void setDestinationNegated(final boolean destinationNegated) {
		this.destinationNegated = destinationNegated;
	}

	public void setGroup(final Integer group) {
		this.group = group;
	}

	public void setUnionGroup(final Integer unionGroup) {
		this.unionGroup = unionGroup;
	}

	public void setCharacteristicType(final CharacteristicType characteristicType) {
		this.characteristicType = characteristicType;
	}

	public void setRefinability(final RelationshipRefinability refinability) {
		this.refinability = refinability;
	}

	public void setModifier(final RelationshipModifier modifier) {
		this.modifier = modifier;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("SnomedRelationship [getId()=");
		builder.append(getId());
		builder.append(", isReleased()=");
		builder.append(isReleased());
		builder.append(", isActive()=");
		builder.append(isActive());
		builder.append(", getEffectiveTime()=");
		builder.append(getEffectiveTime());
		builder.append(", getModuleId()=");
		builder.append(getModuleId());
		builder.append(", getSourceId()=");
		builder.append(getSourceId());
		builder.append(", getDestinationId()=");
		builder.append(getDestinationId());
		builder.append(", isDestinationNegated()=");
		builder.append(isDestinationNegated());
		builder.append(", getTypeId()=");
		builder.append(getTypeId());
		builder.append(", getGroup()=");
		builder.append(getGroup());
		builder.append(", getUnionGroup()=");
		builder.append(getUnionGroup());
		builder.append(", getCharacteristicType()=");
		builder.append(getCharacteristicType());
		builder.append(", getRefinability()=");
		builder.append(getRefinability());
		builder.append(", getModifier()=");
		builder.append(getModifier());
		builder.append("]");
		return builder.toString();
	}
}