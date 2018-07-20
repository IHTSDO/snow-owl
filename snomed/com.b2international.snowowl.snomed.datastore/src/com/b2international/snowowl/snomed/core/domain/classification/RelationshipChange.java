/*
 * Copyright 2018 B2i Healthcare Pte Ltd, http://b2i.sg
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
package com.b2international.snowowl.snomed.core.domain.classification;

import com.b2international.snowowl.snomed.core.domain.RelationshipModifier;
import com.google.common.base.MoreObjects;

/**
 *
 * @since 6.6.1
 */
public class RelationshipChange {

	private String id;
	private ChangeNature changeNature;
	private String sourceId;
	private String typeId;
	private String destinationId;
	private boolean destinationNegated;
	private String characteristicTypeId;
	private int group;
	private int unionGroup;
	private RelationshipModifier modifier;

	public String getId() {
		return id;
	}

	public ChangeNature getChangeNature() {
		return changeNature;
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

	public void setId(String id) {
		this.id = id;
	}

	public void setChangeNature(ChangeNature changeNature) {
		this.changeNature = changeNature;
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
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
					.add("id", id)
					.add("changeNature", changeNature)
					.add("sourceId", sourceId)
					.add("typeId", typeId)
					.add("destinationId", destinationId)
					.add("destinationNegated", destinationNegated)
					.add("characteristicTypeId", characteristicTypeId)
					.add("group", group)
					.add("unionGroup", unionGroup)
					.add("modifier", modifier)
					.toString();
	}

}
