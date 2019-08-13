/*
 * Copyright 2019 B2i Healthcare Pte Ltd, http://b2i.sg
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
package com.b2international.snowowl.snomed.datastore.id.assigner;

import java.util.Set;

import com.b2international.snowowl.core.domain.BranchContext;
import com.b2international.snowowl.snomed.core.domain.BranchMetadataResolver;
import com.b2international.snowowl.snomed.datastore.config.SnomedCoreConfiguration;
import com.b2international.snowowl.snomed.datastore.request.SnomedRequests;
import com.google.common.base.Strings;

/**
 *
 * @since 6.12.2
 */
public class BranchMetadataNamespaceAndModuleAssigner implements SnomedNamespaceAndModuleAssigner {

	private String defaultNamespace = null;
	private String defaultModuleId = null;
	
	@Override
	public String getRelationshipNamespace(String sourceConceptId) {
		return defaultNamespace;
	}

	@Override
	public String getRelationshipModuleId(String sourceConceptId) {
		return defaultModuleId;
	}

	@Override
	public String getConcreteDomainModuleId(String referencedConceptId) {
		return defaultModuleId;
	}

	@Override
	public void collectRelationshipNamespacesAndModules(Set<String> conceptIds, BranchContext context) {
		initializeDefaultModule(context);
		initializeDefaultNamespace(context);
	}

	@Override
	public void collectConcreteDomainModules(Set<String> conceptIds, BranchContext context) {
		initializeDefaultModule(context);
	}

	@Override
	public void clear() {
		defaultModuleId = null;
		defaultNamespace = null;
	}

	private void initializeDefaultModule(BranchContext context) {
		
		String moduleId = BranchMetadataResolver.getEffectiveBranchMetadataValue(context.branch(), SnomedCoreConfiguration.BRANCH_DEFAULT_MODULE_ID_KEY);
		if (!Strings.isNullOrEmpty(moduleId)) {
			defaultModuleId = moduleId;
		} else {
			defaultModuleId = context.service(SnomedCoreConfiguration.class).getDefaultModule();
		}
		
		// verify module existence
		SnomedRequests.prepareGetConcept(defaultModuleId).build().execute(context);
	}
	
	private void initializeDefaultNamespace(BranchContext context) {
		
		String namespace = BranchMetadataResolver.getEffectiveBranchMetadataValue(context.branch(), SnomedCoreConfiguration.BRANCH_DEFAULT_NAMESPACE_KEY);
		
		if (!Strings.isNullOrEmpty(namespace)) {
			defaultNamespace = namespace;
		} else {
			defaultNamespace = context.service(SnomedCoreConfiguration.class).getDefaultNamespace();
		}
		
	}

	@Override
	public String getName() {
		return "branch-metadata";
	}
}
