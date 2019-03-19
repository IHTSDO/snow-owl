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
final class SnomedComplexMapMemberUpdateDelegate extends SnomedRefSetMemberUpdateDelegate {

	SnomedComplexMapMemberUpdateDelegate(SnomedRefSetMemberUpdateRequest request) {
		super(request);
	}

	@Override
	boolean execute(SnomedRefSetMember member, TransactionContext context) {
		final SnomedComplexMapRefSetMember complexMapMember = (SnomedComplexMapRefSetMember) member;
		final String newMapTargetId = getComponentId(SnomedRf2Headers.FIELD_MAP_TARGET);
		final Integer newGroup = getProperty(SnomedRf2Headers.FIELD_MAP_GROUP, Integer.class);
		final Integer newPriority = getProperty(SnomedRf2Headers.FIELD_MAP_PRIORITY, Integer.class);
		final String newMapRule = getProperty(SnomedRf2Headers.FIELD_MAP_RULE);
		final String newMapAdvice = getProperty(SnomedRf2Headers.FIELD_MAP_ADVICE);
		final String newCorrelationId = getComponentId(SnomedRf2Headers.FIELD_CORRELATION_ID);

		boolean changed = false;

		if (newMapTargetId != null && !newMapTargetId.equals(complexMapMember.getMapTargetComponentId())) {
			complexMapMember.setMapTargetComponentId(newMapTargetId);
			changed |= true;
		}

		if (newGroup != null && newGroup.intValue() != complexMapMember.getMapGroup()) {
			complexMapMember.setMapGroup(newGroup);
			changed |= true;
		}

		if (newPriority != null && newPriority.intValue() != complexMapMember.getMapPriority()) {
			complexMapMember.setMapPriority(newPriority);
			changed |= true;
		}

		if (newMapRule != null && !newMapRule.equals(complexMapMember.getMapRule())) {
			complexMapMember.setMapRule(newMapRule);
			changed |= true;
		}

		if (newMapAdvice != null && !newMapAdvice.equals(complexMapMember.getMapAdvice())) {
			complexMapMember.setMapAdvice(newMapAdvice);
			changed |= true;
		}

		if (newCorrelationId != null && !newCorrelationId.equals(complexMapMember.getCorrelationId())) {
			complexMapMember.setCorrelationId(newCorrelationId);
			changed |= true;
		}

		return changed;
	}

	@Override
	boolean hasMutablePropertieschanged(SnomedRefSetMember currentMember, SnomedReferenceSetMember releasedMember) {
		final SnomedComplexMapRefSetMember currentComplexMapMember = (SnomedComplexMapRefSetMember) currentMember;
		
		final String releasedMapTargetId = (String) releasedMember.getProperties().get(SnomedRf2Headers.FIELD_MAP_TARGET);
		final Integer releasedMapGroup = (Integer) releasedMember.getProperties().get(SnomedRf2Headers.FIELD_MAP_GROUP);
		final Integer releasedMapPriority = (Integer) releasedMember.getProperties().get(SnomedRf2Headers.FIELD_MAP_PRIORITY);
		final String releasedMapRule = (String) releasedMember.getProperties().get(SnomedRf2Headers.FIELD_MAP_RULE);
		final String releasedMapAdvice = (String) releasedMember.getProperties().get(SnomedRf2Headers.FIELD_MAP_ADVICE);
		final String releasedCorrelationId = (String) releasedMember.getProperties().get(SnomedRf2Headers.FIELD_CORRELATION_ID);
		
		if (releasedMapTargetId != null && !releasedMapTargetId.equals(currentComplexMapMember.getMapTargetComponentId())) {
			return true;
		}
		
		if (releasedMapGroup != null && releasedMapGroup.intValue() != currentComplexMapMember.getMapGroup()) {
			return true;
		}
		
		if (releasedMapPriority != null && releasedMapPriority.intValue() != currentComplexMapMember.getMapPriority()) {
			return true;
		}
		
		if (releasedMapRule != null && !releasedMapRule.equals(currentComplexMapMember.getMapRule())) {
			return true;
		}
		
		if (releasedMapAdvice != null && !releasedMapAdvice.equals(currentComplexMapMember.getMapAdvice())) {
			return true;
		}
		
		if (releasedCorrelationId != null && !releasedCorrelationId.equals(currentComplexMapMember.getCorrelationId())) {
			return true;
		}
		
		return false;
	}

}
