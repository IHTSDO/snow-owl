/*
 * Copyright 2011-2017 B2i Healthcare Pte Ltd, http://b2i.sg
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
import com.b2international.snowowl.snomed.SnomedConstants.Concepts;
import com.b2international.snowowl.snomed.common.SnomedRf2Headers;
import com.b2international.snowowl.snomed.core.store.SnomedComponents;
import com.b2international.snowowl.snomed.snomedrefset.SnomedAnnotationRefSetMember;
import com.b2international.snowowl.snomed.snomedrefset.SnomedRefSet;
import com.b2international.snowowl.snomed.snomedrefset.SnomedRefSetType;

/**
 *
 */
public class SnomedOWLAxiomMemberCreateDelegate extends SnomedRefSetMemberCreateDelegate {

	SnomedOWLAxiomMemberCreateDelegate(final SnomedRefSetMemberCreateRequest request) {
		super(request);
	}

	@Override
	String execute(final SnomedRefSet refSet, final TransactionContext context) {
		checkRefSetType(refSet, SnomedRefSetType.OWL_AXIOM);
		checkReferencedComponent(refSet);
		checkNonEmptyProperty(refSet, SnomedRf2Headers.FIELD_OWL_EXPRESSION);

		checkComponentExists(refSet, context, SnomedRf2Headers.FIELD_MODULE_ID, getModuleId());
		checkComponentExists(refSet, context, SnomedRf2Headers.FIELD_REFERENCED_COMPONENT_ID, getReferencedComponentId());

		// The OWL Expression will have been generated using the temporary concept SCTID.
		// Now we have the real concept SCTID let's use it to replace the temporary one.
		String owlExpression = getProperty(SnomedRf2Headers.FIELD_OWL_EXPRESSION);
		// Use a regular expression to make sure we only match the identifier when surrounded by non decimal characters.
		owlExpression = owlExpression.replaceAll("([^\\d])" + Concepts.TEMPORARY_AXIOM_CONCEPT_PLACEHOLDER+ "([^\\d])", "$1" + getReferencedComponentId() + "$2");
		
		final SnomedAnnotationRefSetMember member = SnomedComponents.newOWLAxiomReferenceSetMember()
				.withId(getId())
				.withActive(isActive())
				.withReferencedComponent(getReferencedComponentId())
				.withModule(getModuleId())
				.withRefSet(getReferenceSetId())
				.withOWLExpression(owlExpression)
				.addTo(context);

		return member.getUuid();
	}

}
