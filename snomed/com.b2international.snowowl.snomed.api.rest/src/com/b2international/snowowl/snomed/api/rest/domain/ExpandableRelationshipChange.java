package com.b2international.snowowl.snomed.api.rest.domain;

import com.b2international.snowowl.snomed.api.domain.classification.ChangeNature;
import com.b2international.snowowl.snomed.api.domain.classification.IRelationshipChange;
import com.b2international.snowowl.snomed.core.domain.RelationshipModifier;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


@JsonPropertyOrder({ "changeNature", "sourceId", "sourceFsn", "typeId", "typeFsn", "destinationId",
		"destinationFsn", "destinationNegated", "characteristicTypeId", "group","id", "unionGroup", "modifier" })
public class ExpandableRelationshipChange implements IRelationshipChange {

	private IRelationshipChange change;
	private SnomedConceptMini source;
	private SnomedConceptMini type;
	private SnomedConceptMini destination;

	public ExpandableRelationshipChange(IRelationshipChange wrappedChange) {
		this.change = wrappedChange;
	}
	
	@JsonIgnore
	public String getMiniSourceId() {
		return this.source.getId();
	}
	
	@JsonProperty("sourceFsn")
	public String getSourceFsn() {
		if(getSourceId().equals(source.getId())) {
			return this.source.getFsn();
		} else {
			return "";
		}
	}
	
	@JsonIgnore
	public String getMiniTypeId() {
		return this.type.getId();
	}
	
	@JsonProperty("typeFsn")
	public String getTypeFsn() {
		if(getTypeId().equals(type.getId())) {
			return this.type.getFsn();
		} else {
			return "";
		}
	}
	
	@JsonIgnore
	public String getMiniDestinationId() {
		return this.destination.getId();
	}
	
	@JsonProperty("destinationFsn")
	public String getDestinationFsn() {
		return this.destination.getFsn();
	}
	
	public void setSource(SnomedConceptMini source) {
		this.source = source;
	}

	public void setType(SnomedConceptMini type) {
		this.type = type;
	}

	public void setDestination(SnomedConceptMini destination) {
		this.destination = destination;
	}

	@Override
	public ChangeNature getChangeNature() {
		return change.getChangeNature();
	}
	
	@Override
	public String getId() {
		return change.getId();
	}

	@Override
	public String getSourceId() {
		return change.getSourceId();
	}

	@Override
	public String getTypeId() {
		return change.getTypeId();
	}

	@Override
	public String getDestinationId() {
		return change.getDestinationId();
	}

	@Override
	public boolean isDestinationNegated() {
		return change.isDestinationNegated();
	}

	@Override
	public String getCharacteristicTypeId() {
		return change.getCharacteristicTypeId();
	}

	@Override
	public int getGroup() {
		return change.getGroup();
	}

	@Override
	public int getUnionGroup() {
		return change.getUnionGroup();
	}

	@Override
	public RelationshipModifier getModifier() {
		return change.getModifier();
	}

}
