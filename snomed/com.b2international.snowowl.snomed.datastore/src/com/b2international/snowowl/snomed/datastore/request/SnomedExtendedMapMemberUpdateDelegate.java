/*
 * Copyright 2017-2019 B2i Healthcare Pte Ltd, http://b2i.sg
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

import com.b2international.snowowl.core.domain.TransactionContext;
import com.b2international.snowowl.snomed.common.SnomedRf2Headers;
import com.b2international.snowowl.snomed.core.domain.refset.SnomedReferenceSetMember;
import com.b2international.snowowl.snomed.snomedrefset.SnomedComplexMapRefSetMember;
import com.b2international.snowowl.snomed.snomedrefset.SnomedRefSetMember;

/**
 * @since 5.0
 */
final class SnomedExtendedMapMemberUpdateDelegate extends SnomedRefSetMemberUpdateDelegate {

	SnomedExtendedMapMemberUpdateDelegate(SnomedRefSetMemberUpdateRequest request) {
		super(request);
	}

	@Override
	boolean execute(SnomedRefSetMember member, TransactionContext context) {
		final SnomedComplexMapRefSetMember extendedMapMember = (SnomedComplexMapRefSetMember) member;
		final String newMapTargetId = getComponentId(SnomedRf2Headers.FIELD_MAP_TARGET);
		final Integer newGroup = getProperty(SnomedRf2Headers.FIELD_MAP_GROUP, Integer.class);
		final Integer newPriority = getProperty(SnomedRf2Headers.FIELD_MAP_PRIORITY, Integer.class);
		final String newMapRule = getProperty(SnomedRf2Headers.FIELD_MAP_RULE);
		final String newMapAdvice = getProperty(SnomedRf2Headers.FIELD_MAP_ADVICE);
		final String newCorrelationId = getComponentId(SnomedRf2Headers.FIELD_CORRELATION_ID);
		final String newMapCategoryId = getComponentId(SnomedRf2Headers.FIELD_MAP_CATEGORY_ID);

		boolean changed = false;

		if (newMapTargetId != null && !newMapTargetId.equals(extendedMapMember.getMapTargetComponentId())) {
			extendedMapMember.setMapTargetComponentId(newMapTargetId);
			changed |= true;
		}

		if (newGroup != null && newGroup.intValue() != extendedMapMember.getMapGroup()) {
			extendedMapMember.setMapGroup(newGroup);
			changed |= true;
		}

		if (newPriority != null && newPriority.intValue() != extendedMapMember.getMapPriority()) {
			extendedMapMember.setMapPriority(newPriority);
			changed |= true;
		}

		if (newMapRule != null && !newMapRule.equals(extendedMapMember.getMapRule())) {
			extendedMapMember.setMapRule(newMapRule);
			changed |= true;
		}

		if (newMapAdvice != null && !newMapAdvice.equals(extendedMapMember.getMapAdvice())) {
			extendedMapMember.setMapAdvice(newMapAdvice);
			changed |= true;
		}

		if (newCorrelationId != null && !newCorrelationId.equals(extendedMapMember.getCorrelationId())) {
			extendedMapMember.setCorrelationId(newCorrelationId);
			changed |= true;
		}

		if (newMapCategoryId != null && !newMapCategoryId.equals(extendedMapMember.getMapCategoryId())) {
			extendedMapMember.setMapCategoryId(newMapCategoryId);
			changed |= true;
		}

		return changed;
	}

	@Override
	boolean hasMutablePropertieschanged(SnomedRefSetMember currentMember, SnomedReferenceSetMember releasedMember) {
		final SnomedComplexMapRefSetMember currentExtendedMapMember = (SnomedComplexMapRefSetMember) currentMember;

		final String releasedMapTargetId = (String) releasedMember.getProperties().get(SnomedRf2Headers.FIELD_MAP_TARGET);
		final Integer releasedMapGroup = (Integer) releasedMember.getProperties().get(SnomedRf2Headers.FIELD_MAP_GROUP);
		final Integer releasedMapPriority = (Integer) releasedMember.getProperties().get(SnomedRf2Headers.FIELD_MAP_PRIORITY);
		final String releasedMapRule = (String) releasedMember.getProperties().get(SnomedRf2Headers.FIELD_MAP_RULE);
		final String releasedMapAdvice = (String) releasedMember.getProperties().get(SnomedRf2Headers.FIELD_MAP_ADVICE);
		final String releasedCorrelationId = (String) releasedMember.getProperties().get(SnomedRf2Headers.FIELD_CORRELATION_ID);
		final String releasedMapCategoryId = (String) releasedMember.getProperties().get(SnomedRf2Headers.FIELD_MAP_CATEGORY_ID);
		
		if (releasedMapTargetId != null && !releasedMapTargetId.equals(currentExtendedMapMember.getMapTargetComponentId())) {
			return true;
		}
		
		if (releasedMapGroup != null && releasedMapGroup.intValue() != currentExtendedMapMember.getMapGroup()) {
			return true;
		}
		
		if (releasedMapPriority != null && releasedMapPriority.intValue() != currentExtendedMapMember.getMapPriority()) {
			return true;
		}
		
		if (releasedMapRule != null && !releasedMapRule.equals(currentExtendedMapMember.getMapRule())) {
			return true;
		}
		
		if (releasedMapAdvice != null && !releasedMapAdvice.equals(currentExtendedMapMember.getMapAdvice())) {
			return true;
		}
		
		if (releasedCorrelationId != null && !releasedCorrelationId.equals(currentExtendedMapMember.getCorrelationId())) {
			return true;
		}
		
		if (releasedMapCategoryId != null && !releasedMapCategoryId.equals(currentExtendedMapMember.getMapCategoryId())) {
			return true;
		}
		
		return false;
	}

}
