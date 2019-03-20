/*
 * Copyright 2018-2019 B2i Healthcare Pte Ltd, http://b2i.sg
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
import com.b2international.snowowl.snomed.snomedrefset.SnomedRefSetMember;
import com.b2international.snowowl.snomed.snomedrefset.SnomedSimpleMapRefSetMember;

/**
 * @since 6.5
 */
final class SnomedSimpleMapMemberWithDescriptionUpdateDelegate extends SnomedRefSetMemberUpdateDelegate {

	SnomedSimpleMapMemberWithDescriptionUpdateDelegate(SnomedRefSetMemberUpdateRequest request) {
		super(request);
	}

	@Override
	boolean execute(SnomedRefSetMember member, TransactionContext context) {
		final SnomedSimpleMapRefSetMember mapMember = (SnomedSimpleMapRefSetMember) member;
		final String newMapTargetId = getComponentId(SnomedRf2Headers.FIELD_MAP_TARGET);
		final String newMapTargetDescription = getProperty(SnomedRf2Headers.FIELD_MAP_TARGET_DESCRIPTION);

		boolean changed = false;

		if (newMapTargetId != null && !newMapTargetId.equals(mapMember.getMapTargetComponentId())) {
			mapMember.setMapTargetComponentId(newMapTargetId);
			changed |= true;
		}

		if (newMapTargetDescription != null && !newMapTargetDescription.equals(mapMember.getMapTargetComponentDescription())) {
			mapMember.setMapTargetComponentDescription(newMapTargetDescription);
			changed |= true;
		}

		return changed;
	}

	@Override
	boolean hasMutablePropertyChange(SnomedRefSetMember currentMember, SnomedReferenceSetMember releasedMember) {
		final SnomedSimpleMapRefSetMember currentMapMember = (SnomedSimpleMapRefSetMember) currentMember;
		final String releasedMapTargetId = (String) releasedMember.getProperties().get(SnomedRf2Headers.FIELD_MAP_TARGET);
		final String releasedMapTargetDescription = (String) releasedMember.getProperties().get(SnomedRf2Headers.FIELD_MAP_TARGET_DESCRIPTION);
		
		if (releasedMapTargetId != null && !releasedMapTargetId.equals(currentMapMember.getMapTargetComponentId())) {
			return true;
		}
		
		if (releasedMapTargetDescription != null && !releasedMapTargetDescription.equals(currentMapMember.getMapTargetComponentDescription())) {
			return true;
		}
		
		return false;
	}

}
