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
package com.b2international.snowowl.snomed.reasoner.server.diff;

import static com.b2international.snowowl.core.ApplicationContext.getServiceForClass;

import com.b2international.snowowl.core.ApplicationContext;
import com.b2international.snowowl.core.branch.Branch;
import com.b2international.snowowl.core.exceptions.NotFoundException;
import com.b2international.snowowl.core.terminology.ComponentCategory;
import com.b2international.snowowl.datastore.request.RepositoryRequests;
import com.b2international.snowowl.eventbus.IEventBus;
import com.b2international.snowowl.snomed.core.domain.BranchMetadataResolver;
import com.b2international.snowowl.snomed.datastore.SnomedDatastoreActivator;
import com.b2international.snowowl.snomed.datastore.SnomedEditingContext;
import com.b2international.snowowl.snomed.datastore.config.SnomedCoreConfiguration;
import com.b2international.snowowl.snomed.datastore.id.ISnomedIdentifierService;
import com.google.common.base.Strings;
import com.google.common.collect.Multiset;

/**
 *
 * @since 6.6.1
 */
public class BranchMetadataNamespaceAndModuleAssigner extends DefaultNamespaceAndModuleAssigner {

	@Override
	public void allocateRelationshipIdsAndModules(Multiset<String> conceptIds, final SnomedEditingContext editingContext) {
		
		if (conceptIds.isEmpty()) {
			return;
		}
		
		try {
			
			Branch branch = RepositoryRequests.branching()
					.prepareGet(editingContext.getBranch())
					.build(SnomedDatastoreActivator.REPOSITORY_UUID)
					.execute(ApplicationContext.getServiceForClass(IEventBus.class))
					.getSync();

			String defaultModuleId = BranchMetadataResolver.getEffectiveBranchMetadataValue(branch,
					SnomedCoreConfiguration.BRANCH_DEFAULT_MODULE_ID_KEY);
			String defaultNamespaceId = BranchMetadataResolver.getEffectiveBranchMetadataValue(branch,
					SnomedCoreConfiguration.BRANCH_DEFAULT_NAMESPACE_KEY);

			ISnomedIdentifierService identifierService = getServiceForClass(ISnomedIdentifierService.class);
			
			String defaultNamespace = Strings.isNullOrEmpty(defaultNamespaceId)
					? editingContext.getDefaultNamespace()
					: defaultNamespaceId;

			reservedIds = identifierService.reserve(defaultNamespace, ComponentCategory.RELATIONSHIP, conceptIds.size());
			relationshipIds = reservedIds.iterator();

			defaultRelationshipModuleConcept = Strings.isNullOrEmpty(defaultModuleId)
					? editingContext.getDefaultModuleConcept()
					: editingContext.getConcept(defaultModuleId);
							
		} catch (NotFoundException e) {
			super.allocateRelationshipIdsAndModules(conceptIds, editingContext);
		}
		
	}

}
