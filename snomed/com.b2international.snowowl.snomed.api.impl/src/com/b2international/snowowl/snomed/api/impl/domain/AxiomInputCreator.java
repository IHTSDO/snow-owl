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
package com.b2international.snowowl.snomed.api.impl.domain;

import org.snomed.otf.owltoolkit.conversion.AxiomRelationshipConversionService;
import org.snomed.otf.owltoolkit.domain.AxiomRepresentation;
import org.snomed.otf.owltoolkit.domain.Relationship;

import com.b2international.snowowl.core.ApplicationContext;
import com.b2international.snowowl.core.branch.Branch;
import com.b2international.snowowl.core.domain.TransactionContext;
import com.b2international.snowowl.core.events.Request;
import com.b2international.snowowl.snomed.SnomedConstants.Concepts;
import com.b2international.snowowl.snomed.api.browser.ISnomedBrowserAxiomService;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserAxiom;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserRelationship;
import com.b2international.snowowl.snomed.api.impl.domain.browser.SnomedBrowserAxiom;
import com.b2international.snowowl.snomed.common.SnomedRf2Headers;
import com.b2international.snowowl.snomed.datastore.request.SnomedRefSetMemberCreateRequest;
import com.b2international.snowowl.snomed.datastore.request.SnomedRefSetMemberUpdateRequest;
import com.b2international.snowowl.snomed.datastore.request.SnomedRequests;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;

public class AxiomInputCreator extends AbstractInputCreator implements ComponentInputCreator<SnomedRefSetMemberCreateRequest, SnomedRefSetMemberUpdateRequest, ISnomedBrowserAxiom> {

	private AxiomRelationshipConversionService conversionService;
	private Branch branch;

	public AxiomInputCreator(final Branch branch) {
		this.branch = branch;
	}

	@Override
	public SnomedRefSetMemberCreateRequest createInput(final ISnomedBrowserAxiom newAxiom, final InputFactory inputFactory) {
		final SnomedBrowserAxiom browserAxiom = (SnomedBrowserAxiom) newAxiom;
		return (SnomedRefSetMemberCreateRequest) SnomedRequests
			.prepareNewMember()
			.setModuleId(browserAxiom.getModuleId())
			.setReferenceSetId(Concepts.REFSET_OWL_AXIOM)
			.setReferencedComponentId(browserAxiom.getReferencedComponentId())
			.setProperties(ImmutableMap.<String, Object>of(SnomedRf2Headers.FIELD_OWL_EXPRESSION, toOwlExpression(browserAxiom)))
			.build();
	}

	@Override
	public SnomedRefSetMemberUpdateRequest createUpdate(final ISnomedBrowserAxiom existingAxiom, final ISnomedBrowserAxiom updatedAxiom) {
		if (updatedAxiom.getAxiomId() != null) {

			final SnomedBrowserAxiom updatedBrowserAxiom = (SnomedBrowserAxiom) updatedAxiom;

			return (SnomedRefSetMemberUpdateRequest) SnomedRequests.prepareUpdateMember()
				.setMemberId(updatedBrowserAxiom.getAxiomId())
				.setSource(ImmutableMap.<String, Object>builder()
						.put(SnomedRf2Headers.FIELD_ACTIVE, updatedBrowserAxiom.isActive())
						.put(SnomedRf2Headers.FIELD_MODULE_ID, updatedBrowserAxiom.getModuleId())
						.put(SnomedRf2Headers.FIELD_OWL_EXPRESSION, toOwlExpression(updatedBrowserAxiom))
						.build())
				.build();

		}

		return null;
	}

	@Override
	public boolean canCreateInput(final Class<? extends Request<TransactionContext, String>> inputType) {
		return SnomedRefSetMemberCreateRequest.class.isAssignableFrom(inputType);
	}

	@Override
	public boolean canCreateUpdate(final Class<? extends Request<TransactionContext, Boolean>> updateType) {
		return SnomedRefSetMemberUpdateRequest.class.isAssignableFrom(updateType);
	}

	private String toOwlExpression(final SnomedBrowserAxiom browserAxiom) {

		final ListMultimap<Integer, Relationship> relationships = ArrayListMultimap.create();
		for (final ISnomedBrowserRelationship relationship : browserAxiom.getRelationships()) {
			final long typeId = Long.valueOf(relationship.getType().getConceptId());
			final long targetId = Long.valueOf(relationship.getTarget().getConceptId());
			relationships.put(relationship.getGroupId(), new Relationship(relationship.getGroupId(), typeId, targetId));
		}

		final AxiomRepresentation axiomRepresentation = new AxiomRepresentation();
		axiomRepresentation.setPrimitive(browserAxiom.getDefinitionStatus().isPrimitive());

		final Long conceptId = Long.valueOf(browserAxiom.getReferencedComponentId());

		if (browserAxiom.isNamedConceptOnLeft()) {
			axiomRepresentation.setLeftHandSideNamedConcept(conceptId);
			axiomRepresentation.setRightHandSideRelationships(Multimaps.asMap(relationships));
		} else {
			axiomRepresentation.setRightHandSideNamedConcept(conceptId);
			axiomRepresentation.setLeftHandSideRelationships(Multimaps.asMap(relationships));
		}

		return getConversionService().convertRelationshipsToAxiom(axiomRepresentation);
	}

	public AxiomRelationshipConversionService getConversionService() {
		if (conversionService == null) {
			conversionService = ApplicationContext.getServiceForClass(ISnomedBrowserAxiomService.class).getConversionService(branch.path());
		}
		return conversionService;
	}

}
