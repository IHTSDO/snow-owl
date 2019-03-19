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
import com.b2international.snowowl.snomed.snomedrefset.SnomedDescriptionTypeRefSetMember;
import com.b2international.snowowl.snomed.snomedrefset.SnomedRefSetMember;

/**
 * @since 5.0
 */
final class SnomedDescriptionTypeMemberUpdateDelegate extends SnomedRefSetMemberUpdateDelegate {

	SnomedDescriptionTypeMemberUpdateDelegate(SnomedRefSetMemberUpdateRequest request) {
		super(request);
	}

	@Override
	boolean execute(SnomedRefSetMember member, TransactionContext context) {
		final SnomedDescriptionTypeRefSetMember descriptionTypeMember = (SnomedDescriptionTypeRefSetMember) member;
		final String newDescriptionFormat = getComponentId(SnomedRf2Headers.FIELD_DESCRIPTION_FORMAT);
		final Integer newDescriptionLength = getProperty(SnomedRf2Headers.FIELD_DESCRIPTION_LENGTH, Integer.class);

		boolean changed = false;

		if (newDescriptionFormat != null && !newDescriptionFormat.equals(descriptionTypeMember.getDescriptionFormat())) {
			descriptionTypeMember.setDescriptionFormat(newDescriptionFormat);
			changed |= true;
		}

		if (newDescriptionLength != null && !newDescriptionLength.equals(descriptionTypeMember.getDescriptionLength())) {
			descriptionTypeMember.setDescriptionLength(newDescriptionLength);
			changed |= true;
		}

		return changed;
	}

	@Override
	boolean hasMutablePropertieschanged(SnomedRefSetMember currentMember, SnomedReferenceSetMember releasedMember) {
		final SnomedDescriptionTypeRefSetMember currentDescriptionTypeMember = (SnomedDescriptionTypeRefSetMember) currentMember;
		
		final String releasedDescriptionFormat = (String) releasedMember.getProperties().get(SnomedRf2Headers.FIELD_DESCRIPTION_FORMAT);
		final Integer releasedDescriptionLength = (Integer) releasedMember.getProperties().get(SnomedRf2Headers.FIELD_DESCRIPTION_LENGTH);
		
		
		if (releasedDescriptionFormat != null && !releasedDescriptionFormat.equals(currentDescriptionTypeMember.getDescriptionFormat())) {
			return true;
		}
		
		if (releasedDescriptionLength != null && releasedDescriptionLength.intValue() != currentDescriptionTypeMember.getDescriptionLength()) {
			return true;
		}
		
		return false;
	}

}
