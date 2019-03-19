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
import com.b2international.snowowl.snomed.snomedrefset.SnomedLanguageRefSetMember;
import com.b2international.snowowl.snomed.snomedrefset.SnomedRefSetMember;

/**
 * @since 5.0
 */
final class SnomedLanguageMemberUpdateDelegate extends SnomedRefSetMemberUpdateDelegate {

	SnomedLanguageMemberUpdateDelegate(SnomedRefSetMemberUpdateRequest request) {
		super(request);
	}

	@Override
	boolean execute(SnomedRefSetMember member, TransactionContext context) {
		final SnomedLanguageRefSetMember languageMember = (SnomedLanguageRefSetMember) member;
		final String newAcceptabilityId = getComponentId(SnomedRf2Headers.FIELD_ACCEPTABILITY_ID);

		if (newAcceptabilityId != null && !newAcceptabilityId.equals(languageMember.getAcceptabilityId())) {
			languageMember.setAcceptabilityId(newAcceptabilityId);
			return true;
		} else {
			return false;
		}
	}

	@Override
	boolean hasMutablePropertieschanged(SnomedRefSetMember currentMember, SnomedReferenceSetMember releasedMember) {
		final SnomedLanguageRefSetMember currentLanguageMember = (SnomedLanguageRefSetMember) currentMember;
		
		final String releasedAcceptabilityId = (String) releasedMember.getProperties().get(SnomedRf2Headers.FIELD_ACCEPTABILITY_ID);
		
		return releasedAcceptabilityId != null && !releasedAcceptabilityId.equals(currentLanguageMember.getAcceptabilityId());
	}

}
