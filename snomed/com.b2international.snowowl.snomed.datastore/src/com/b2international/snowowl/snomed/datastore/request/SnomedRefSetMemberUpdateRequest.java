/*
 * Copyright 2011-2018 B2i Healthcare Pte Ltd, http://b2i.sg
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
package com.b2international.snowowl.snomed.datastore.request;

import java.util.Date;
import java.util.Map;
import java.util.Objects;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import com.b2international.commons.ClassUtils;
import com.b2international.snowowl.core.date.DateFormats;
import com.b2international.snowowl.core.date.EffectiveTimes;
import com.b2international.snowowl.core.domain.TransactionContext;
import com.b2international.snowowl.core.events.Request;
import com.b2international.snowowl.core.exceptions.ComponentNotFoundException;
import com.b2international.snowowl.core.terminology.ComponentCategory;
import com.b2international.snowowl.eventbus.IEventBus;
import com.b2international.snowowl.snomed.common.SnomedRf2Headers;
import com.b2international.snowowl.snomed.core.domain.refset.SnomedReferenceSetMember;
import com.b2international.snowowl.snomed.datastore.SnomedDatastoreActivator;
import com.b2international.snowowl.snomed.snomedrefset.SnomedAssociationRefSetMember;
import com.b2international.snowowl.snomed.snomedrefset.SnomedRefSetMember;
import com.b2international.snowowl.snomed.snomedrefset.SnomedRefSetType;
import com.google.common.base.Strings;

/**
 * @since 4.5
 */
public final class SnomedRefSetMemberUpdateRequest implements Request<TransactionContext, Boolean> {
	
	private static final String[] EMPTY_ARRAY = new String[] {};

	@NotEmpty
	private final String memberId;
	
	@NotEmpty
	private final Map<String, Object> properties;

	@NotNull
	private final Boolean force;
	
	SnomedRefSetMemberUpdateRequest(String memberId, Map<String, Object> properties, Boolean force) {
		this.memberId = memberId;
		this.properties = properties;
		this.force = force;
	}
	
	String getMemberId() {
		return memberId;
	}
	
	boolean hasProperty(String key) {
		return properties.containsKey(key);
	}
	
	String getComponentId(String key) {
		Object value = properties.get(key);
		if (value == null) {
			return null;
		} else if (value instanceof Map) {
			return ClassUtils.checkAndCast(((Map<?, ?>) value).get(SnomedRf2Headers.FIELD_ID), String.class);
		} else {
			return ClassUtils.checkAndCast(value, String.class);
		}
	}
	
	String getProperty(String key) {
		return getProperty(key, String.class);
	}
	
	<T> T getProperty(String key, Class<T> valueType) {
		Object value = properties.get(key);
		if (value == null) {
			return null;
		} else {
			return ClassUtils.checkAndCast(value, valueType);
		}
	}

	@Override
	public Boolean execute(TransactionContext context) {
		SnomedRefSetMember member = context.lookup(memberId, SnomedRefSetMember.class);

		/* 
		 * TODO: Generalize the logic below: any attempts of retrieving a missing component during component update
		 * (with the exception of the component that is being updated) should return a 400 response instead of a 404. 
		 */
		try {

			SnomedRefSetType type = member.getRefSet().getType();

			boolean changed = false;

			changed |= updateStatus(member);
			changed |= updateModule(member);
			changed |= updateEffectiveTime(member);

			SnomedRefSetMemberUpdateDelegate delegate = getDelegate(type);
			changed |= delegate.execute(member, context);

			if (changed) {
				if (!isEffectiveTimeUpdate() && member.isSetEffectiveTime()) {
					member.unsetEffectiveTime();
				} else {
					if (member.isReleased()) {
						final String branchPath = SnomedComponentUpdateRequest.getLatestReleaseBranch(context);
						if (!Strings.isNullOrEmpty(branchPath)) {
							final SnomedReferenceSetMember releasedMember = SnomedRequests.prepareGetMember(getMemberId())
								.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
								.execute(context.service(IEventBus.class))
								.getSync();
							if (releasedMember == null) {
								throw new ComponentNotFoundException(ComponentCategory.SET_MEMBER, getMemberId());
							} else if (!isDifferentToPreviousRelease(member, releasedMember, type)) {
								member.setEffectiveTime(releasedMember.getEffectiveTime());
							}
						}
					}
				}
			}

			return changed;

		} catch (ComponentNotFoundException e) {
			throw e.toBadRequestException();
		}
	}
	
	private boolean isDifferentToPreviousRelease(SnomedRefSetMember member, SnomedReferenceSetMember releasedMember, SnomedRefSetType type) {
		if (releasedMember.isActive() != member.isActive()) return true;
		if (!releasedMember.getModuleId().equals(member.getModuleId())) return true;
		final String[] mutableHeadersByType = getMutableHeadersByType(type);
		boolean hasPropertyChange = false;
		for (String header : mutableHeadersByType) {
			if (member instanceof SnomedAssociationRefSetMember) {
				final SnomedAssociationRefSetMember associationRefSetMember = (SnomedAssociationRefSetMember) member;
				final Object releasedProperty = releasedMember.getProperties().get(header);
			}
			
		}
		return hasPropertyChange;
	}
	
	private SnomedRefSetMemberUpdateDelegate getDelegate(SnomedRefSetType referenceSetType) {
		switch (referenceSetType) {
			case ASSOCIATION:
				return new SnomedAssociationMemberUpdateDelegate(this);
			case ATTRIBUTE_VALUE:
				return new SnomedAttributeValueMemberUpdateDelegate(this);
			case COMPLEX_MAP:
				return new SnomedComplexMapMemberUpdateDelegate(this);
			case CONCRETE_DATA_TYPE:
				return new SnomedConcreteDomainMemberUpdateDelegate(this);
			case DESCRIPTION_TYPE:
				return new SnomedDescriptionTypeMemberUpdateDelegate(this);
			case EXTENDED_MAP:
				return new SnomedExtendedMapMemberUpdateDelegate(this);
			case LANGUAGE:
				return new SnomedLanguageMemberUpdateDelegate(this);
			case MODULE_DEPENDENCY:
				return new SnomedModuleDependencyMemberUpdateDelegate(this);
			case QUERY:
				return new SnomedQueryMemberUpdateDelegate(this);
			case SIMPLE: 
				return new SnomedSimpleMemberUpdateDelegate(this);
			case SIMPLE_MAP:
				return new SnomedSimpleMapMemberUpdateDelegate(this);
			case SIMPLE_MAP_WITH_DESCRIPTION:
				return new SnomedSimpleMapMemberWithDescriptionUpdateDelegate(this);
			case OWL_AXIOM: //$FALL-THROUGH$
			case OWL_ONTOLOGY:
				return new SnomedOWLExpressionMemberUpdateDelegate(this);
			case MRCM_DOMAIN:
				return new SnomedMRCMDomainMemberUpdateDelegate(this);
			case MRCM_ATTRIBUTE_DOMAIN:
				return new SnomedMRCMAttributeDomainMemberUpdateDelegate(this);
			case MRCM_ATTRIBUTE_RANGE:
				return new SnomedMRCMAttributeRangeMemberUpdateDelegate(this);
			case MRCM_MODULE_SCOPE:
				return new SnomedMRCMModuleScopeMemberUpdateDelegate(this);
			default: 
				throw new IllegalStateException("Unexpected reference set type '" + referenceSetType + "'.");
		}
	}
	
	private String[] getMutableHeadersByType(SnomedRefSetType referenceSetType) {
		switch (referenceSetType) {
		case ASSOCIATION:
			return new String[] { SnomedRf2Headers.FIELD_TARGET_COMPONENT_ID };
		case ATTRIBUTE_VALUE:
			return new String[] { SnomedRf2Headers.FIELD_VALUE_ID };
		case COMPLEX_MAP:
			return new String[] { 
					SnomedRf2Headers.FIELD_MAP_GROUP, SnomedRf2Headers.FIELD_MAP_PRIORITY,
					SnomedRf2Headers.FIELD_MAP_RULE, SnomedRf2Headers.FIELD_MAP_ADVICE,
					SnomedRf2Headers.FIELD_MAP_TARGET, SnomedRf2Headers.FIELD_CORRELATION_ID 
					};
		case EXTENDED_MAP:
			return new String[] { 
					SnomedRf2Headers.FIELD_MAP_GROUP, SnomedRf2Headers.FIELD_MAP_PRIORITY,
					SnomedRf2Headers.FIELD_MAP_RULE, SnomedRf2Headers.FIELD_MAP_ADVICE,
					SnomedRf2Headers.FIELD_MAP_TARGET, SnomedRf2Headers.FIELD_CORRELATION_ID,
					SnomedRf2Headers.FIELD_MAP_CATEGORY_ID
					};
		case CONCRETE_DATA_TYPE:
			return new String[] {
					SnomedRf2Headers.FIELD_UNIT_ID, 
					SnomedRf2Headers.FIELD_OPERATOR_ID, 
					SnomedRf2Headers.FIELD_VALUE
			};
		case DESCRIPTION_TYPE:
			return new String[] { SnomedRf2Headers.FIELD_DESCRIPTION_FORMAT, SnomedRf2Headers.FIELD_DESCRIPTION_LENGTH };
		case LANGUAGE:
			return new String[] { SnomedRf2Headers.FIELD_ACCEPTABILITY_ID };
		case MODULE_DEPENDENCY:
			return new String[] { SnomedRf2Headers.FIELD_SOURCE_EFFECTIVE_TIME, SnomedRf2Headers.FIELD_TARGET_EFFECTIVE_TIME };
		case QUERY:
			return new String[] { SnomedRf2Headers.FIELD_QUERY };
		case SIMPLE: 
			return EMPTY_ARRAY;
		case SIMPLE_MAP:
			return new String[] { SnomedRf2Headers.FIELD_MAP_TARGET };
		case SIMPLE_MAP_WITH_DESCRIPTION:
			return new String[] { SnomedRf2Headers.FIELD_MAP_TARGET, SnomedRf2Headers.FIELD_MAP_TARGET_DESCRIPTION };
		case OWL_AXIOM: //$FALL-THROUGH$
		case OWL_ONTOLOGY:
			return new String[] { SnomedRf2Headers.FIELD_OWL_EXPRESSION };
		case MRCM_DOMAIN:
			return new String[] { 
					SnomedRf2Headers.FIELD_MRCM_DOMAIN_CONSTRAINT, SnomedRf2Headers.FIELD_MRCM_PARENT_DOMAIN, 
					SnomedRf2Headers.FIELD_MRCM_PROXIMAL_PRIMITIVE_CONSTRAINT, SnomedRf2Headers.FIELD_MRCM_PROXIMAL_PRIMITIVE_REFINEMENT, 
					SnomedRf2Headers.FIELD_MRCM_DOMAIN_TEMPLATE_FOR_PRECOORDINATION, SnomedRf2Headers.FIELD_MRCM_DOMAIN_TEMPLATE_FOR_POSTCOORDINATION, 
					SnomedRf2Headers.FIELD_MRCM_EDITORIAL_GUIDE_REFERENCE};
		case MRCM_ATTRIBUTE_DOMAIN:
			return new String[] {
					SnomedRf2Headers.FIELD_MRCM_GROUPED, SnomedRf2Headers.FIELD_MRCM_ATTRIBUTE_CARDINALITY,
					SnomedRf2Headers.FIELD_MRCM_ATTRIBUTE_IN_GROUP_CARDINALITY, SnomedRf2Headers.FIELD_MRCM_RULE_STRENGTH_ID,
					SnomedRf2Headers.FIELD_MRCM_CONTENT_TYPE_ID
			};
		case MRCM_ATTRIBUTE_RANGE:
			return new String[] {
					SnomedRf2Headers.FIELD_MRCM_RANGE_CONSTRAINT, SnomedRf2Headers.FIELD_MRCM_ATTRIBUTE_RULE,
					SnomedRf2Headers.FIELD_MRCM_RULE_STRENGTH_ID, SnomedRf2Headers.FIELD_MRCM_CONTENT_TYPE_ID,
			};
		case MRCM_MODULE_SCOPE:
			return EMPTY_ARRAY;
			
		default: 
			throw new IllegalStateException("Unexpected reference set type '" + referenceSetType + "'.");
		}
	}
	
	private boolean updateStatus(SnomedRefSetMember member) {
		Boolean newStatus = getProperty(SnomedRf2Headers.FIELD_ACTIVE, Boolean.class);
		if (newStatus != null && !newStatus.equals(member.isActive())) {
			member.setActive(newStatus);
			return true;
		} else {
			return false;
		}
	}

	private boolean updateModule(SnomedRefSetMember member) {
		String newModuleId = getComponentId(SnomedRf2Headers.FIELD_MODULE_ID);
		if (newModuleId != null && !newModuleId.equals(member.getModuleId())) {
			member.setModuleId(newModuleId);
			return true;
		} else {
			return false;
		}
	}

	private boolean updateEffectiveTime(SnomedRefSetMember member) {
		if (!isEffectiveTimeUpdate()) {
			return false;
		}
		
		Date newEffectiveTime = EffectiveTimes.parse(getProperty(SnomedRf2Headers.FIELD_EFFECTIVE_TIME), DateFormats.SHORT);
		if (!Objects.equals(newEffectiveTime, member.getEffectiveTime())) {
			if (newEffectiveTime == null) {
				// if effective time is null, then unset the effective time but don't change the released flag
				member.unsetEffectiveTime();
			} else {
				// otherwise set the value and toggle the "relased" flag
				member.setEffectiveTime(newEffectiveTime);
				member.setReleased(true);
			}
			return true;
		} else {
			return false;
		}
	}

	private boolean isEffectiveTimeUpdate() {
		return force && hasProperty(SnomedRf2Headers.FIELD_EFFECTIVE_TIME);
	}

}
