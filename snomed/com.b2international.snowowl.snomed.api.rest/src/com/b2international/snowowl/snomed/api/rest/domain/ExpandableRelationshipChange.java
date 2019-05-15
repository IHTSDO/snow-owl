package com.b2international.snowowl.snomed.api.rest.domain;

import com.b2international.snowowl.snomed.core.domain.RelationshipModifier;
import com.b2international.snowowl.snomed.reasoner.domain.ReasonerRelationship;
import com.b2international.snowowl.snomed.reasoner.domain.RelationshipChange;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.base.Strings;


@JsonPropertyOrder({ "changeNature", "sourceId", "sourceFsn", "typeId", "typeFsn", "destinationId",
		"destinationFsn", "destinationNegated", "characteristicTypeId", "group","id", "unionGroup", "modifier" })
public class ExpandableRelationshipChange extends RelationshipChange {

	private String id;
	private String sourceId;
	private String typeId;
	private String destinationId;
	private boolean destinationNegated;
	private String characteristicTypeId;
	private int group;
	private int unionGroup;
	private RelationshipModifier modifier;
	
	public ExpandableRelationshipChange(RelationshipChange change) {
		setId(Strings.isNullOrEmpty(change.getRelationship().getOriginId()) ? "" : change.getRelationship().getOriginId());
		setChangeNature(change.getChangeNature());
		setSourceId(change.getRelationship().getSourceId());
		setTypeId(change.getRelationship().getTypeId());
		setDestinationId(change.getRelationship().getDestinationId());
		setDestinationNegated(change.getRelationship().isDestinationNegated());
		setCharacteristicTypeId(change.getRelationship().getCharacteristicType().getConceptId());
		setGroup(change.getRelationship().getGroup());
		setUnionGroup(change.getRelationship().getUnionGroup());
		setModifier(change.getRelationship().getModifier());
		setRelationship(change.getRelationship());
	}
	
	@JsonIgnore
	@Override
	public ReasonerRelationship getRelationship() {
		return super.getRelationship();
	}

	public String getId() {
		return id;
	}

	public String getSourceId() {
		return sourceId;
	}

	public String getTypeId() {
		return typeId;
	}

	public String getDestinationId() {
		return destinationId;
	}

	public boolean isDestinationNegated() {
		return destinationNegated;
	}

	public String getCharacteristicTypeId() {
		return characteristicTypeId;
	}

	public int getGroup() {
		return group;
	}

	public int getUnionGroup() {
		return unionGroup;
	}

	public RelationshipModifier getModifier() {
		return modifier;
	}

	@JsonProperty("sourceFsn")
	public String getSourceFsn() {
		if (getRelationship().getSource() != null && getRelationship().getSource().getFsn() != null) {
			return Strings.nullToEmpty(getRelationship().getSource().getFsn().getTerm());
		}
		return "";
	}
	
	@JsonProperty("typeFsn")
	public String getTypeFsn() {
		if (getRelationship().getType() != null && getRelationship().getType().getFsn() != null) {
			return Strings.nullToEmpty(getRelationship().getType().getFsn().getTerm());
		}
		return "";
	}
	
	@JsonProperty("destinationFsn")
	public String getDestinationFsn() {
		if (getRelationship().getDestination() != null && getRelationship().getDestination().getFsn() != null) {
			return Strings.nullToEmpty(getRelationship().getDestination().getFsn().getTerm());
		}
		return "";
	}
	
	public void setId(String id) {
		this.id = id;
	}

	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}

	public void setTypeId(String typeId) {
		this.typeId = typeId;
	}

	public void setDestinationId(String destinationId) {
		this.destinationId = destinationId;
	}

	public void setDestinationNegated(boolean destinationNegated) {
		this.destinationNegated = destinationNegated;
	}

	public void setCharacteristicTypeId(String characteristicTypeId) {
		this.characteristicTypeId = characteristicTypeId;
	}

	public void setGroup(int group) {
		this.group = group;
	}

	public void setUnionGroup(int unionGroup) {
		this.unionGroup = unionGroup;
	}

	public void setModifier(RelationshipModifier modifier) {
		this.modifier = modifier;
	}

}
