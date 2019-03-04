/*
 * Copyright 2017 B2i Healthcare Pte Ltd, http://b2i.sg
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
package com.b2international.snowowl.snomed.core.tests.util;

import java.math.BigDecimal;

import com.b2international.collections.PrimitiveSets;
import com.b2international.snowowl.core.date.EffectiveTimes;
import com.b2international.snowowl.core.domain.IComponent;
import com.b2international.snowowl.core.terminology.ComponentCategory;
import com.b2international.snowowl.snomed.SnomedConstants.Concepts;
import com.b2international.snowowl.snomed.common.SnomedRf2Headers;
import com.b2international.snowowl.snomed.common.SnomedTerminologyComponentConstants;
import com.b2international.snowowl.snomed.core.domain.CaseSignificance;
import com.b2international.snowowl.snomed.core.tree.Trees;
import com.b2international.snowowl.snomed.datastore.id.RandomSnomedIdentiferGenerator;
import com.b2international.snowowl.snomed.datastore.id.SnomedIdentifiers;
import com.b2international.snowowl.snomed.datastore.index.entry.SnomedConceptDocument;
import com.b2international.snowowl.snomed.datastore.index.entry.SnomedDescriptionIndexEntry;
import com.b2international.snowowl.snomed.datastore.index.entry.SnomedRefSetMemberIndexEntry;
import com.b2international.snowowl.snomed.datastore.index.entry.SnomedRefSetMemberIndexEntry.Fields;
import com.b2international.snowowl.snomed.datastore.index.entry.SnomedRelationshipIndexEntry;
import com.b2international.snowowl.snomed.snomedrefset.DataType;
import com.b2international.snowowl.snomed.snomedrefset.SnomedRefSetType;

/**
 * @since 6.0
 */
public abstract class DocumentBuilders {

	private DocumentBuilders() {}
	
	public static SnomedConceptDocument.Builder concept(final String id) {
		return SnomedConceptDocument.builder()
				.id(id)
				.iconId(Concepts.ROOT_CONCEPT)
				.active(true)
				.released(true)
				.exhaustive(false)
				.moduleId(Concepts.MODULE_SCT_CORE)
				.effectiveTime(EffectiveTimes.UNSET_EFFECTIVE_TIME)
				.primitive(true)
				.parents(PrimitiveSets.newLongOpenHashSet(IComponent.ROOT_IDL))
				.ancestors(PrimitiveSets.newLongOpenHashSet(IComponent.ROOT_IDL))
				.statedParents(PrimitiveSets.newLongOpenHashSet(IComponent.ROOT_IDL))
				.statedAncestors(PrimitiveSets.newLongOpenHashSet(IComponent.ROOT_IDL));
	}
	
	public static SnomedDescriptionIndexEntry.Builder description(final String id, final String type, final String term) {
		return SnomedDescriptionIndexEntry.builder()
				.id(id)
				.active(true)
				.moduleId(Concepts.MODULE_SCT_CORE)
				.typeId(type)
				.languageCode("en")
				.term(term)
				.caseSignificanceId(CaseSignificance.INITIAL_CHARACTER_CASE_INSENSITIVE.getConceptId())
				.released(false);
	}
	
	public static SnomedRelationshipIndexEntry.Builder relationship(final String source, final String type, final String destination, final String form) {
		final String characteristicTypeId = Trees.INFERRED_FORM.equals(form) ? Concepts.INFERRED_RELATIONSHIP : Concepts.STATED_RELATIONSHIP;
		return SnomedRelationshipIndexEntry.builder()
				.id(RandomSnomedIdentiferGenerator.generateRelationshipId())
				.active(true)
				.moduleId(Concepts.MODULE_SCT_CORE)
				.sourceId(source)
				.typeId(type)
				.destinationId(destination)
				.characteristicTypeId(characteristicTypeId)
				.modifierId(Concepts.EXISTENTIAL_RESTRICTION_MODIFIER);
	}
	
	public static SnomedRefSetMemberIndexEntry.Builder decimalMember(final String referencedComponentId, final String attributeName, final BigDecimal value, final String form) {
		return concreteDomain(referencedComponentId, attributeName, value, DataType.DECIMAL, form);
	}
	
	public static SnomedRefSetMemberIndexEntry.Builder integerMember(final String referencedComponentId, final String attributeName, final int value, final String form) {
		return concreteDomain(referencedComponentId, attributeName, value, DataType.INTEGER, form);
	}
	
	public static SnomedRefSetMemberIndexEntry.Builder stringMember(final String referencedComponentId, final String attributeName, final String value, final String form) {
		return concreteDomain(referencedComponentId, attributeName, value, DataType.STRING, form);
	}

	public static SnomedRefSetMemberIndexEntry.Builder concreteDomain(final String referencedComponentId, final String attributeName, final Object value, final DataType type, final String form) {
		final short referencedComponentType = 
				SnomedIdentifiers.getComponentCategory(referencedComponentId) == ComponentCategory.CONCEPT 
					? SnomedTerminologyComponentConstants.CONCEPT_NUMBER 
					: SnomedTerminologyComponentConstants.RELATIONSHIP_NUMBER;
		final String characteristicTypeId = Trees.INFERRED_FORM.equals(form) ? Concepts.INFERRED_RELATIONSHIP : Concepts.STATED_RELATIONSHIP;
		return SnomedRefSetMemberIndexEntry.builder()
				.id(RandomSnomedIdentiferGenerator.generateRelationshipId())
				.active(true)
				.moduleId(Concepts.MODULE_SCT_CORE)
				.referencedComponentId(referencedComponentId)
				.referencedComponentType(referencedComponentType)
				.referenceSetId(RandomSnomedIdentiferGenerator.generateConceptId())
				.referenceSetType(SnomedRefSetType.CONCRETE_DATA_TYPE)
				.field(SnomedRf2Headers.FIELD_CHARACTERISTIC_TYPE_ID, characteristicTypeId)
				.field(SnomedRf2Headers.FIELD_ATTRIBUTE_NAME, attributeName)
				.field(Fields.DATA_TYPE, type)
				.field(SnomedRf2Headers.FIELD_VALUE, value);
	}
	
}