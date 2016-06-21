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
package com.b2international.snowowl.snomed.datastore.index.entry;

import static com.b2international.index.query.Expressions.matchAny;
import static com.b2international.index.query.Expressions.matchAnyInt;
import static com.b2international.snowowl.snomed.common.SnomedTerminologyComponentConstants.CONCEPT_NUMBER;
import static com.b2international.snowowl.snomed.common.SnomedTerminologyComponentConstants.DESCRIPTION_NUMBER;
import static com.b2international.snowowl.snomed.common.SnomedTerminologyComponentConstants.RELATIONSHIP_NUMBER;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import com.b2international.commons.StringUtils;
import com.b2international.index.Doc;
import com.b2international.index.query.Expression;
import com.b2international.snowowl.core.CoreTerminologyBroker;
import com.b2international.snowowl.core.date.DateFormats;
import com.b2international.snowowl.core.date.EffectiveTimes;
import com.b2international.snowowl.snomed.common.SnomedRf2Headers;
import com.b2international.snowowl.snomed.common.SnomedTerminologyComponentConstants;
import com.b2international.snowowl.snomed.core.domain.Acceptability;
import com.b2international.snowowl.snomed.core.domain.InactivationIndicator;
import com.b2international.snowowl.snomed.core.domain.RelationshipRefinability;
import com.b2international.snowowl.snomed.core.domain.SnomedConcept;
import com.b2international.snowowl.snomed.core.domain.SnomedCoreComponent;
import com.b2international.snowowl.snomed.core.domain.SnomedDescription;
import com.b2international.snowowl.snomed.core.domain.SnomedRelationship;
import com.b2international.snowowl.snomed.core.domain.refset.SnomedReferenceSetMember;
import com.b2international.snowowl.snomed.datastore.SnomedRefSetUtil;
import com.b2international.snowowl.snomed.snomedrefset.DataType;
import com.b2international.snowowl.snomed.snomedrefset.SnomedAssociationRefSetMember;
import com.b2international.snowowl.snomed.snomedrefset.SnomedAttributeValueRefSetMember;
import com.b2international.snowowl.snomed.snomedrefset.SnomedComplexMapRefSetMember;
import com.b2international.snowowl.snomed.snomedrefset.SnomedConcreteDataTypeRefSetMember;
import com.b2international.snowowl.snomed.snomedrefset.SnomedDescriptionTypeRefSetMember;
import com.b2international.snowowl.snomed.snomedrefset.SnomedLanguageRefSetMember;
import com.b2international.snowowl.snomed.snomedrefset.SnomedModuleDependencyRefSetMember;
import com.b2international.snowowl.snomed.snomedrefset.SnomedQueryRefSetMember;
import com.b2international.snowowl.snomed.snomedrefset.SnomedRefSetMember;
import com.b2international.snowowl.snomed.snomedrefset.SnomedRefSetType;
import com.b2international.snowowl.snomed.snomedrefset.SnomedSimpleMapRefSetMember;
import com.b2international.snowowl.snomed.snomedrefset.util.SnomedRefSetSwitch;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;

/**
 * Lightweight representation of a SNOMED CT reference set member.
 */
@Doc
@JsonDeserialize(builder = SnomedRefSetMemberIndexEntry.Builder.class)
public final class SnomedRefSetMemberIndexEntry extends SnomedDocument {

	private static final long serialVersionUID = 5198766293865046258L;

	public static class Fields {
		// known RF2 fields
		public static final String REFERENCE_SET_ID = "referenceSetId"; // XXX different than the RF2 header field name
		public static final String REFERENCED_COMPONENT_ID = SnomedRf2Headers.FIELD_REFERENCED_COMPONENT_ID;
		public static final String ACCEPTABILITY_ID = SnomedRf2Headers.FIELD_ACCEPTABILITY_ID;
		public static final String VALUE_ID = SnomedRf2Headers.FIELD_VALUE_ID;
		public static final String TARGET_COMPONENT = SnomedRf2Headers.FIELD_TARGET_COMPONENT;
		public static final String MAP_TARGET = SnomedRf2Headers.FIELD_MAP_TARGET;
		public static final String MAP_TARGET_DESCRIPTION = SnomedRf2Headers.FIELD_MAP_TARGET_DESCRIPTION;
		public static final String MAP_GROUP = SnomedRf2Headers.FIELD_MAP_GROUP;
		public static final String MAP_PRIORITY = SnomedRf2Headers.FIELD_MAP_PRIORITY;
		public static final String MAP_RULE = SnomedRf2Headers.FIELD_MAP_RULE;
		public static final String MAP_ADVICE = SnomedRf2Headers.FIELD_MAP_ADVICE;
		public static final String MAP_CATEGORY_ID = SnomedRf2Headers.FIELD_MAP_CATEGORY_ID;
		public static final String CORRELATION_ID = SnomedRf2Headers.FIELD_CORRELATION_ID;
		public static final String DESCRIPTION_FORMAT = SnomedRf2Headers.FIELD_DESCRIPTION_FORMAT;
		public static final String DESCRIPTION_LENGTH = SnomedRf2Headers.FIELD_DESCRIPTION_LENGTH;
		public static final String OPERATOR_ID = SnomedRf2Headers.FIELD_OPERATOR_ID;
		public static final String UNIT_ID = SnomedRf2Headers.FIELD_UNIT_ID;
		public static final String QUERY = SnomedRf2Headers.FIELD_QUERY;
		public static final String CHARACTERISTIC_TYPE_ID = SnomedRf2Headers.FIELD_CHARACTERISTIC_TYPE_ID;
		public static final String SOURCE_EFFECTIVE_TIME = SnomedRf2Headers.FIELD_SOURCE_EFFECTIVE_TIME;
		public static final String TARGET_EFFECTIVE_TIME = SnomedRf2Headers.FIELD_TARGET_EFFECTIVE_TIME;
		public static final String DATA_VALUE = SnomedRf2Headers.FIELD_VALUE;
		public static final String ATTRIBUTE_NAME = SnomedRf2Headers.FIELD_ATTRIBUTE_NAME;
		// extra index fields to store datatype and map target type
		public static final String DATA_TYPE = "dataType";
		public static final String MAP_TARGET_TYPE = "mapTargetType";
		public static final String REFSET_TYPE = "referenceSetType";
		public static final String REFERENCED_COMPONENT_TYPE = "referencedComponentType";
	}
	
		public static Builder builder() {
		return new Builder();
	}
	
	public static Builder builder(final SnomedRefSetMemberIndexEntry source) {
		return builder()
				.active(source.isActive())
				.effectiveTime(source.getEffectiveTime())
				.id(source.getId())
				.moduleId(source.getModuleId())
				.referencedComponentId(source.getReferencedComponentId())
				.referencedComponentType(source.getReferencedComponentType())
				.referenceSetId(source.getReferenceSetId())
				.referenceSetType(source.getReferenceSetType())
				.released(source.isReleased())
				.mapTargetComponentType(source.getMapTargetComponentType())
				.fields(source.getAdditionalFields());
	}
	
	public static final Builder builder(final SnomedReferenceSetMember input) {
		final Object mapTargetComponentType = input.getProperties().get(Fields.MAP_TARGET_TYPE);
		
		final Builder builder = builder()
				.active(input.isActive())
				.effectiveTime(EffectiveTimes.getEffectiveTime(input.getEffectiveTime()))
				.id(input.getId())
				.moduleId(input.getModuleId())
				.referencedComponentId(input.getReferencedComponent().getId())
				.referencedComponentType(input.getReferencedComponent())
				.referenceSetId(input.getReferenceSetId())
				.referenceSetType(input.type())
				.released(input.isReleased())
				.mapTargetComponentType(mapTargetComponentType == null ? -1 : (short) mapTargetComponentType);
		
		for (Entry<String, Object> entry : input.getProperties().entrySet()) {
			final Object value = entry.getValue();
			final String fieldName = entry.getKey();
			// certain RF2 fields can be expanded into full blown representation class, get the ID in this case
			if (value instanceof SnomedCoreComponent) {
				builder.field(fieldName, ((SnomedCoreComponent) value).getId());
			} else {
				builder.field(fieldName, convertValue(entry.getKey(), value));
			}
		}
		
		return builder;
	}
	
	public static Builder builder(SnomedRefSetMember refSetMember) {
		final Builder builder = SnomedRefSetMemberIndexEntry.builder()
				.id(refSetMember.getUuid()) 
				.moduleId(refSetMember.getModuleId())
				.active(refSetMember.isActive())
				.released(refSetMember.isReleased())
				.effectiveTime(refSetMember.isSetEffectiveTime() ? refSetMember.getEffectiveTime().getTime() : EffectiveTimes.UNSET_EFFECTIVE_TIME)
				.referenceSetId(refSetMember.getRefSetIdentifierId())
				.referenceSetType(refSetMember.getRefSet().getType())
				.referencedComponentType(refSetMember.getReferencedComponentType())
				.referencedComponentId(refSetMember.getReferencedComponentId());

		return new SnomedRefSetSwitch<Builder>() {

			@Override
			public Builder caseSnomedAssociationRefSetMember(final SnomedAssociationRefSetMember associationMember) {
				return builder.targetComponent(associationMember.getTargetComponentId());
			}

			@Override
			public Builder caseSnomedAttributeValueRefSetMember(final SnomedAttributeValueRefSetMember attributeValueMember) {
				return builder.field(Fields.VALUE_ID, attributeValueMember.getValueId());
			}

			@Override
			public Builder caseSnomedConcreteDataTypeRefSetMember(final SnomedConcreteDataTypeRefSetMember concreteDataTypeMember) {
				return builder.field(Fields.ATTRIBUTE_NAME, concreteDataTypeMember.getLabel())
						.field(Fields.DATA_TYPE, concreteDataTypeMember.getDataType())
						.field(Fields.DATA_VALUE, concreteDataTypeMember.getSerializedValue())
						.field(Fields.CHARACTERISTIC_TYPE_ID, concreteDataTypeMember.getCharacteristicTypeId())
						.field(Fields.OPERATOR_ID, concreteDataTypeMember.getOperatorComponentId())
						.field(Fields.UNIT_ID, concreteDataTypeMember.getUomComponentId());
			}

			@Override
			public Builder caseSnomedDescriptionTypeRefSetMember(final SnomedDescriptionTypeRefSetMember descriptionTypeMember) {
				return builder
						.field(Fields.DESCRIPTION_FORMAT, descriptionTypeMember.getDescriptionFormat())
						.field(Fields.DESCRIPTION_LENGTH, descriptionTypeMember.getDescriptionLength());
			}

			@Override
			public Builder caseSnomedLanguageRefSetMember(final SnomedLanguageRefSetMember languageMember) {
				return builder.field(Fields.ACCEPTABILITY_ID, languageMember.getAcceptabilityId());
			}

			@Override
			public Builder caseSnomedModuleDependencyRefSetMember(final SnomedModuleDependencyRefSetMember moduleDependencyMember) {
				return builder
						.field(Fields.SOURCE_EFFECTIVE_TIME, EffectiveTimes.getEffectiveTime(moduleDependencyMember.getSourceEffectiveTime()))
						.field(Fields.TARGET_EFFECTIVE_TIME, EffectiveTimes.getEffectiveTime(moduleDependencyMember.getTargetEffectiveTime()));
			}

			@Override
			public Builder caseSnomedQueryRefSetMember(final SnomedQueryRefSetMember queryMember) {
				return builder.field(Fields.QUERY, queryMember.getQuery());
			}

			@Override
			public Builder caseSnomedSimpleMapRefSetMember(final SnomedSimpleMapRefSetMember mapRefSetMember) {
				return builder
						.mapTargetComponentType(mapRefSetMember.getMapTargetComponentType())
						.field(Fields.MAP_TARGET, mapRefSetMember.getMapTargetComponentId())
						.field(Fields.MAP_TARGET_DESCRIPTION, mapRefSetMember.getMapTargetComponentDescription());
			}
			
			@Override
			public Builder caseSnomedComplexMapRefSetMember(final SnomedComplexMapRefSetMember mapRefSetMember) {
				return builder
						.mapTargetComponentType(mapRefSetMember.getMapTargetComponentType())
						.field(Fields.MAP_TARGET, mapRefSetMember.getMapTargetComponentId())
						.field(Fields.CORRELATION_ID, mapRefSetMember.getCorrelationId())
						.field(Fields.MAP_GROUP, Integer.valueOf(mapRefSetMember.getMapGroup()))
						.field(Fields.MAP_ADVICE, Strings.nullToEmpty(mapRefSetMember.getMapAdvice()))
						.field(Fields.MAP_PRIORITY, Integer.valueOf(mapRefSetMember.getMapPriority()))
						.field(Fields.MAP_RULE, Strings.nullToEmpty(mapRefSetMember.getMapRule()))
						// extended refset
						.field(Fields.MAP_CATEGORY_ID, mapRefSetMember.getMapCategoryId());
			}
			
			@Override
			public Builder caseSnomedRefSetMember(SnomedRefSetMember object) {
				return builder;
			};

		}.doSwitch(refSetMember);
	}
	
	private static Object convertValue(String rf2Field, Object value) {
		switch (rf2Field) {
		case SnomedRf2Headers.FIELD_SOURCE_EFFECTIVE_TIME:
		case SnomedRf2Headers.FIELD_TARGET_EFFECTIVE_TIME:
			if (value instanceof String && !StringUtils.isEmpty((String) value)) {
				return Long.valueOf((String) value);
			}
		default: return value;
		}
	}

	public static Collection<SnomedRefSetMemberIndexEntry> from(final Iterable<SnomedReferenceSetMember> refSetMembers) {
		return FluentIterable.from(refSetMembers).transform(new Function<SnomedReferenceSetMember, SnomedRefSetMemberIndexEntry>() {
			@Override
			public SnomedRefSetMemberIndexEntry apply(final SnomedReferenceSetMember refSetMember) {
				return builder(refSetMember).build();
			}
		}).toList();
	}

	public static final class Expressions extends SnomedDocument.Expressions {

		public static Expression referenceSetId(Collection<String> referenceSetIds) {
			return matchAny(Fields.REFERENCE_SET_ID, referenceSetIds);
		}

		public static Expression referencedComponentIds(Collection<String> referencedComponentIds) {
			return matchAny(Fields.REFERENCED_COMPONENT_ID, referencedComponentIds);
		}
		
		public static Expression targetComponents(Collection<String> targetComponentIds) {
			return matchAny(Fields.TARGET_COMPONENT, targetComponentIds);
		}

		public static Expression refSetTypes(Collection<SnomedRefSetType> refSetTypes) {
			return matchAnyInt(Fields.REFSET_TYPE, FluentIterable.from(refSetTypes).transform(new Function<SnomedRefSetType, Integer>() {
				@Override
				public Integer apply(SnomedRefSetType input) {
					return input.ordinal();
				}
			}).toSet());
		}
		
	}
	
	public static final class Builder extends SnomedDocumentBuilder<Builder> {

		private String referencedComponentId;

		private String referenceSetId;
		private SnomedRefSetType referenceSetType;
		private short referencedComponentType;
		private short mapTargetComponentType = CoreTerminologyBroker.UNSPECIFIED_NUMBER_SHORT;

		// Member specific fields, they can be null or emptyish values
		// ASSOCIATION reference set members
		private String targetComponent;
		// ATTRIBUTE VALUE
		private String valueId;
		// CONCRETE DOMAIN reference set members
		private DataType dataType;
		private String attributeName;
		private String value;
		private String operatorId;
		private String characteristicTypeId;
		private String unitId;
		// DESCRIPTION
		private Integer descriptionLength;
		private String descriptionFormat;
		// LANGUAGE
		private String acceptabilityId;
		// MODULE
		private Long sourceEffectiveTime;
		private Long targetEffectiveTime;
		// SIMPLE MAP reference set members
		private String mapTarget;
		private String mapTargetDescription;
		// COMPLEX MAP
		private String mapCategoryId;
		private String correlationId;
		private String mapAdvice;
		private String mapRule;
		private Integer mapGroup;
		private Integer mapPriority;
		// QUERY
		private String query;

		@JsonCreator
		private Builder() {
			// Disallow instantiation outside static method
		}

		Builder fields(Map<String, Object> fields) {
			for (Entry<String, Object> entry : fields.entrySet()) {
				field(entry.getKey(), entry.getValue());
			}
			return this;
		}
		
		Builder field(String fieldName, Object value) {
			switch (fieldName) {
			case Fields.ACCEPTABILITY_ID: this.acceptabilityId = (String) value; break;
			case Fields.ATTRIBUTE_NAME: this.attributeName = (String) value; break;
			case Fields.CHARACTERISTIC_TYPE_ID: this.characteristicTypeId = (String) value; break;
			case Fields.CORRELATION_ID: this.correlationId = (String) value; break;
			case Fields.DATA_TYPE: this.dataType = (DataType) value; break;
			case Fields.DATA_VALUE: this.value = (String) value; break;
			case Fields.DESCRIPTION_FORMAT: this.descriptionFormat = (String) value; break;
			case Fields.DESCRIPTION_LENGTH: this.descriptionLength = (Integer) value; break;
			case Fields.MAP_ADVICE: this.mapAdvice = (String) value; break;
			case Fields.MAP_CATEGORY_ID: this.mapCategoryId = (String) value; break;
			case Fields.MAP_GROUP: this.mapGroup = (Integer) value; break;
			case Fields.MAP_PRIORITY: this.mapPriority = (Integer) value; break;
			case Fields.MAP_RULE: this.mapRule = (String) value; break;
			case Fields.MAP_TARGET: this.mapTarget = (String) value; break;
			case Fields.MAP_TARGET_DESCRIPTION: this.mapTargetDescription = (String) value; break;
			case Fields.OPERATOR_ID: this.operatorId = (String) value; break;
			case Fields.QUERY: this.query = (String) value; break;
			case Fields.SOURCE_EFFECTIVE_TIME: this.sourceEffectiveTime = (Long) value; break;
			case Fields.TARGET_COMPONENT: this.targetComponent = (String) value; break;
			case Fields.TARGET_EFFECTIVE_TIME: this.targetEffectiveTime = (Long) value; break;
			case Fields.UNIT_ID: this.unitId = (String) value; break;
			case Fields.VALUE_ID: this.valueId = (String) value; break;
			default: throw new UnsupportedOperationException("Unknown RF2 member field: " + fieldName);
			}
			return this;
		}

		@Override
		protected Builder getSelf() {
			return this;
		}

		public Builder referencedComponentId(final String referencedComponentId) {
			this.referencedComponentId = referencedComponentId;
			return this;
		}

		public Builder referenceSetId(final String referenceSetId) {
			this.referenceSetId = referenceSetId;
			return this;
		}

		public Builder referenceSetType(final SnomedRefSetType referenceSetType) {
			this.referenceSetType = referenceSetType;
			return this;
		}

		public Builder referencedComponentType(final String referencedComponentType) {
			this.referencedComponentType = SnomedTerminologyComponentConstants.getValue(referencedComponentType);
			return this;
		}
		
		public Builder referencedComponentType(final short referencedComponentType) {
			this.referencedComponentType = referencedComponentType;
			return this;
		}
		
		public Builder referencedComponentType(final SnomedCoreComponent component) {
			if (component instanceof SnomedConcept) {
				this.referencedComponentType = CONCEPT_NUMBER;
			} else if (component instanceof SnomedDescription) {
				this.referencedComponentType = DESCRIPTION_NUMBER;
			} else if (component instanceof SnomedRelationship) {
				this.referencedComponentType = RELATIONSHIP_NUMBER;
			} else {
				this.referencedComponentType = -1;
			}
			
			return this;
		}

		public Builder mapTargetComponentType(final short mapTargetComponentType) {
			this.mapTargetComponentType = mapTargetComponentType;
			return this;
		}
		
		public Builder targetComponent(String targetComponent) {
			this.targetComponent = targetComponent;
			return this;
		}
		
		public SnomedRefSetMemberIndexEntry build() {
			final SnomedRefSetMemberIndexEntry doc = new SnomedRefSetMemberIndexEntry(id,
					label,
					moduleId, 
					released, 
					active, 
					effectiveTime, 
					referencedComponentId, 
					referenceSetId,
					referenceSetType,
					referencedComponentType,
					mapTargetComponentType);
			// association members
			doc.targetComponent = targetComponent;
			// attribute value
			doc.valueId = valueId;
			// concrete domain members
			doc.dataType = dataType;
			doc.attributeName = attributeName;
			doc.value = value;
			doc.characteristicTypeId = characteristicTypeId;
			doc.operatorId = operatorId;
			doc.unitId = unitId;
			// description
			doc.descriptionFormat = descriptionFormat;
			doc.descriptionLength = descriptionLength;
			// language reference set
			doc.acceptabilityId = acceptabilityId;
			// module
			doc.sourceEffectiveTime = sourceEffectiveTime;
			doc.targetEffectiveTime = targetEffectiveTime;
			// simple map
			doc.mapTarget = mapTarget;
			doc.mapTargetDescription = mapTargetDescription;
			// complex map
			doc.mapCategoryId = mapCategoryId;
			doc.mapAdvice = mapAdvice;
			doc.correlationId = correlationId;
			doc.mapGroup = mapGroup;
			doc.mapPriority = mapPriority;
			doc.mapRule = mapRule;
			// query
			doc.query = query;
			
			// metadata
			doc.setBranchPath(branchPath);
			doc.setCommitTimestamp(commitTimestamp);
			doc.setStorageKey(storageKey);
			doc.setReplacedIns(replacedIns);
			return doc;
		}
	}

	private final String referencedComponentId;
	private final String referenceSetId;
	private final SnomedRefSetType referenceSetType;
	private final short referencedComponentType;
	private final short mapTargetComponentType;
	
	// Member specific fields, they can be null or emptyish values
	// ASSOCIATION reference set members
	private String targetComponent;
	// ATTRIBUTE VALUE
	private String valueId;
	// CONCRETE DOMAIN reference set members
	private DataType dataType;
	private String attributeName;
	private String value;
	private String operatorId;
	private String characteristicTypeId;
	private String unitId;
	// DESCRIPTION
	private Integer descriptionLength;
	private String descriptionFormat;
	// LANGUAGE
	private String acceptabilityId;
	// MODULE
	private Long sourceEffectiveTime;
	private Long targetEffectiveTime;
	// SIMPLE MAP reference set members
	private String mapTarget;
	private String mapTargetDescription;
	// COMPLEX MAP
	private String mapCategoryId;
	private String correlationId;
	private String mapAdvice;
	private String mapRule;
	private Integer mapGroup;
	private Integer mapPriority;
	// QUERY
	private String query;
	

	private SnomedRefSetMemberIndexEntry(final String id,
			final String label,
			final String moduleId, 
			final boolean released,
			final boolean active, 
			final long effectiveTimeLong, 
			final String referencedComponentId, 
			final String referenceSetId,
			final SnomedRefSetType referenceSetType,
			final short referencedComponentType,
			final short mapTargetComponentType) {

		super(id, 
				label,
				referencedComponentId, // XXX: iconId is the referenced component identifier
				moduleId, 
				released, 
				active, 
				effectiveTimeLong);

		checkArgument(referencedComponentType >= CoreTerminologyBroker.UNSPECIFIED_NUMBER_SHORT, "Referenced component type '%s' is invalid.", referencedComponentType);
		checkArgument(mapTargetComponentType >= CoreTerminologyBroker.UNSPECIFIED_NUMBER_SHORT, "Map target component type '%s' is invalid.", referencedComponentType);

		this.referencedComponentId = checkNotNull(referencedComponentId, "Reference component identifier may not be null.");
		this.referenceSetId = checkNotNull(referenceSetId, "Reference set identifier may not be null.");
		this.referenceSetType = checkNotNull(referenceSetType, "Reference set type may not be null.");
		this.referencedComponentType = referencedComponentType;
		this.mapTargetComponentType = mapTargetComponentType;
	}


	/**
	 * @return the referenced component identifier
	 */
	public String getReferencedComponentId() {
		return referencedComponentId;
	}

	/**
	 * @return the identifier of the member's reference set
	 */
	public String getReferenceSetId() {
		return referenceSetId;
	}

	/**
	 * @return the type of the member's reference set
	 */
	public SnomedRefSetType getReferenceSetType() {
		return referenceSetType;
	}

	/**
	 * @return the {@code String} terminology component identifier of the map target in this member, or
	 *         {@link CoreTerminologyBroker#UNSPECIFIED_NUMBER_SHORT} if not known (or the reference set is not a map)
	 */
	public short getMapTargetComponentType() {
		return mapTargetComponentType;
	}

	@Override
	public String toString() {
		// XXX refset type specific toString???
		return toStringHelper()
				.add("referencedComponentId", referencedComponentId)
				.add("referenceSetType", referenceSetType)
				.add("referencedComponentType", referencedComponentType)
				.add("mapTargetComponentType", mapTargetComponentType)
				.toString();
	}
	
	@JsonIgnore
	@SuppressWarnings("unchecked")
	public <T> T getValueAs() {
		final DataType dataType = getDataType();
		return (T) (dataType == null ? null : SnomedRefSetUtil.deserializeValue(dataType, getValue()));
	}
	
	public String getValue() {
		return value;
	}

	public DataType getDataType() {
		return dataType;
	}

	public String getUnitId() {
		return unitId;
	}

	public String getAttributeName() {
		return attributeName;
	}

	public String getOperatorId() {
		return operatorId;
	}

	public String getCharacteristicTypeId() {
		return characteristicTypeId;
	}	

	public String getAcceptabilityId() {
		return acceptabilityId;
	}

	public Integer getDescriptionLength() {
		return descriptionLength;
	}
	
	public String getDescriptionFormat() {
		return descriptionFormat;
	}

	public String getMapTarget() {
		return mapTarget;
	}

	public Integer getMapGroup() {
		return mapGroup;
	}

	public Integer getMapPriority() {
		return mapPriority;
	}

	public String getMapRule() {
		return mapRule;
	}

	public String getMapAdvice() {
		return mapAdvice;
	}
	
	public String getMapCategoryId() {
		return mapCategoryId;
	}
	
	public String getCorrelationId() {
		return correlationId;
	}

	public String getMapTargetDescription() {
		return mapTargetDescription;
	}
	
	public String getQuery() {
		return query;
	}
	
	public String getTargetComponent() {
		return targetComponent;
	}
	
	public String getValueId() {
		return valueId;
	}
	
	public Long getSourceEffectiveTime() {
		return sourceEffectiveTime;
	}
	
	public Long getTargetEffectiveTime() {
		return targetEffectiveTime;
	}
	
	public short getReferencedComponentType() {
		return referencedComponentType;
	}
	
	// model helper methods
	
	@JsonIgnore
	public Acceptability getAcceptability() {
		return Acceptability.getByConceptId(getAcceptabilityId());
	}
	
	@JsonIgnore
	public RelationshipRefinability getRefinability() {
		return RelationshipRefinability.getByConceptId(getValueId());
	}
	
	@JsonIgnore
	public InactivationIndicator getInactivationIndicator() {
		return InactivationIndicator.getByConceptId(getValueId());
	}
	
	@JsonIgnore
	public String getSourceEffectiveTimeAsString() {
		return EffectiveTimes.format(getSourceEffectiveTime(), DateFormats.SHORT);
	}
	
	@JsonIgnore
	public String getTargetEffectiveTimeAsString() {
		return EffectiveTimes.format(getTargetEffectiveTime(), DateFormats.SHORT);
	}
	
	/**
	 * @return the {@code String} terminology component identifier of the component referenced in this member
	 */
	@JsonIgnore
	public String getReferencedComponentTypeAsString() {
		return CoreTerminologyBroker.getInstance().getTerminologyComponentId(referencedComponentType);
	}

	/**
	 * @return the {@code String} terminology component identifier of the map target in this member, or
	 *         {@link CoreTerminologyBroker#UNSPECIFIED} if not known (or the reference set is not a map)
	 */
	@JsonIgnore
	public String getMapTargetComponentTypeString() {
		return CoreTerminologyBroker.getInstance().getTerminologyComponentId(mapTargetComponentType);
	}
	
	/**
	 * Helper which converts all non-null/empty additional fields to a values {@link Map} keyed by their field name; 
	 * @return
	 */
	@JsonIgnore
	public Map<String, Object> getAdditionalFields() {
		final ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
		// ASSOCIATION refset members
		putIfPresent(builder, Fields.TARGET_COMPONENT, getTargetComponent());
		// ATTRIBUTE_VALUE refset members 
		putIfPresent(builder, Fields.VALUE_ID, getValueId());
		// CONCRETE DOMAIN reference set members
		putIfPresent(builder, Fields.DATA_TYPE, getDataType());
		putIfPresent(builder, Fields.ATTRIBUTE_NAME, getAttributeName());
		putIfPresent(builder, Fields.DATA_VALUE, getValue());
		putIfPresent(builder, Fields.OPERATOR_ID, getOperatorId());
		putIfPresent(builder, Fields.CHARACTERISTIC_TYPE_ID, getCharacteristicTypeId());
		putIfPresent(builder, Fields.UNIT_ID, getUnitId());
		// DESCRIPTION
		putIfPresent(builder, Fields.DESCRIPTION_LENGTH, getDescriptionLength());
		putIfPresent(builder, Fields.DESCRIPTION_FORMAT, getDescriptionFormat());
		// LANGUAGE
		putIfPresent(builder, Fields.ACCEPTABILITY_ID, getAcceptabilityId());
		// MODULE
		putIfPresent(builder, Fields.SOURCE_EFFECTIVE_TIME, getSourceEffectiveTime());
		putIfPresent(builder, Fields.TARGET_EFFECTIVE_TIME, getTargetEffectiveTime());
		// SIMPLE MAP reference set members
		putIfPresent(builder, Fields.MAP_TARGET, getMapTarget());
		putIfPresent(builder, Fields.MAP_TARGET_DESCRIPTION, getMapTargetDescription());
		// COMPLEX MAP
		putIfPresent(builder, Fields.MAP_CATEGORY_ID, getMapCategoryId());
		putIfPresent(builder, Fields.CORRELATION_ID, getCorrelationId());
		putIfPresent(builder, Fields.MAP_ADVICE, getMapAdvice());
		putIfPresent(builder, Fields.MAP_RULE, getMapRule());
		putIfPresent(builder, Fields.MAP_GROUP, getMapGroup());
		putIfPresent(builder, Fields.MAP_PRIORITY, getMapPriority());
		// QUERY
		putIfPresent(builder, Fields.QUERY, getQuery());
		return builder.build();
	}
	
	private static void putIfPresent(ImmutableMap.Builder<String, Object> builder, String key, Object value) {
		if (key != null && value != null) {
			builder.put(key, value);
		}
	}
	
}
