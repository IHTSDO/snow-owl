package com.b2international.snowowl.snomed.api.rest.domain;

import com.b2international.snowowl.snomed.core.domain.classification.RelationshipChange;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


@JsonPropertyOrder({ "changeNature", "sourceId", "sourceFsn", "typeId", "typeFsn", "destinationId",
		"destinationFsn", "destinationNegated", "characteristicTypeId", "group","id", "unionGroup", "modifier" })
public class ExpandableRelationshipChange extends RelationshipChange {

	private SnomedConceptMini source;
	private SnomedConceptMini type;
	private SnomedConceptMini destination;

	public ExpandableRelationshipChange(RelationshipChange change) {
		setId(change.getId());
		setChangeNature(change.getChangeNature());
		setSourceId(change.getSourceId());
		setTypeId(change.getTypeId());
		setDestinationId(change.getDestinationId());
		setDestinationNegated(change.isDestinationNegated());
		setCharacteristicTypeId(change.getCharacteristicTypeId());
		setGroup(change.getGroup());
		setUnionGroup(change.getUnionGroup());
		setModifier(change.getModifier());
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

}
