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
package com.b2international.snowowl.snomed.datastore.request.rf2.importer;

import static com.b2international.snowowl.snomed.common.SnomedRf2Headers.FIELD_MRCM_DOMAIN_CONSTRAINT;
import static com.b2international.snowowl.snomed.common.SnomedRf2Headers.FIELD_MRCM_DOMAIN_TEMPLATE_FOR_POSTCOORDINATION;
import static com.b2international.snowowl.snomed.common.SnomedRf2Headers.FIELD_MRCM_DOMAIN_TEMPLATE_FOR_PRECOORDINATION;
import static com.b2international.snowowl.snomed.common.SnomedRf2Headers.FIELD_MRCM_EDITORIAL_GUIDE_REFERENCE;
import static com.b2international.snowowl.snomed.common.SnomedRf2Headers.FIELD_MRCM_PARENT_DOMAIN;
import static com.b2international.snowowl.snomed.common.SnomedRf2Headers.FIELD_MRCM_PROXIMAL_PRIMITIVE_CONSTRAINT;
import static com.b2international.snowowl.snomed.common.SnomedRf2Headers.FIELD_MRCM_PROXIMAL_PRIMITIVE_REFINEMENT;
import static com.b2international.snowowl.snomed.common.SnomedRf2Headers.MRCM_DOMAIN_HEADER;

import com.b2international.collections.PrimitiveSets;
import com.b2international.collections.longs.LongSet;
import com.b2international.snowowl.snomed.core.domain.SnomedConcept;
import com.b2international.snowowl.snomed.core.domain.refset.SnomedReferenceSetMember;
import com.b2international.snowowl.snomed.snomedrefset.SnomedRefSetType;
import com.google.common.collect.ImmutableMap;

/**
 * @since 6.5
 */
public class Rf2MRCMDomainRefSetContentType implements Rf2RefSetContentType {

	@Override
	public void resolve(SnomedReferenceSetMember component, String[] values) {
		component.setType(SnomedRefSetType.MRCM_DOMAIN);
		component.setReferenceSetId(values[4]);
		// XXX actual type is not relevant here
		component.setReferencedComponent(new SnomedConcept(values[5]));
		
		component.setProperties(
			ImmutableMap.<String, Object>builder()
				.put(FIELD_MRCM_DOMAIN_CONSTRAINT, values[6])
				.put(FIELD_MRCM_PARENT_DOMAIN, values[7]) 
				.put(FIELD_MRCM_PROXIMAL_PRIMITIVE_CONSTRAINT, values[8])
				.put(FIELD_MRCM_PROXIMAL_PRIMITIVE_REFINEMENT, values[9]) 
				.put(FIELD_MRCM_DOMAIN_TEMPLATE_FOR_PRECOORDINATION, values[10])
				.put(FIELD_MRCM_DOMAIN_TEMPLATE_FOR_POSTCOORDINATION, values[11])
				.put(FIELD_MRCM_EDITORIAL_GUIDE_REFERENCE, values[12])
				.build()
		);
	}
	
	@Override
	public LongSet getDependencies(String[] values) {
		return PrimitiveSets.newLongOpenHashSet(
				Long.parseLong(values[3]),	
				Long.parseLong(values[4])
			);
	}

	@Override
	public String getType() {
		return "mrcm-domain";
	}
	
	@Override
	public String[] getHeaderColumns() {
		return MRCM_DOMAIN_HEADER;
	}

}