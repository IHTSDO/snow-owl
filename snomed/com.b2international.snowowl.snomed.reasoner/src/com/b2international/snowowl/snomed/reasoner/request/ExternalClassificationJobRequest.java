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
package com.b2international.snowowl.snomed.reasoner.request;

import static java.util.Collections.emptyList;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.hibernate.validator.constraints.NotEmpty;

import com.b2international.snowowl.core.branch.Branch;
import com.b2international.snowowl.core.domain.BranchContext;
import com.b2international.snowowl.core.events.Request;
import com.b2international.snowowl.datastore.remotejobs.RemoteJob;
import com.b2international.snowowl.snomed.datastore.index.taxonomy.IReasonerTaxonomy;
import com.b2international.snowowl.snomed.reasoner.classification.ClassificationTracker;
import com.b2international.snowowl.snomed.reasoner.exceptions.ReasonerApiException;
import com.b2international.snowowl.snomed.reasoner.external.ExternalClassificationNormalFormGenerator;
import com.b2international.snowowl.snomed.reasoner.external.ExternalReasonerTaxonomy;
import com.b2international.snowowl.snomed.reasoner.external.SnomedExternalClassificationService;

/**
 *
 * @since 6.12.2 
 */
public class ExternalClassificationJobRequest implements Request<BranchContext, Boolean> {

	@NotEmpty
	private String reasonerId;
	
	ExternalClassificationJobRequest() {}
	
	void setReasonerId(final String reasonerId) {
		this.reasonerId = reasonerId;
	}
	
	@Override
	public Boolean execute(BranchContext context) {
		final RemoteJob job = context.service(RemoteJob.class);
		final String classificationId = job.getId();
		final String userId = job.getUser();

		final Branch branch = context.branch();
		final long headTimestamp = branch.headTimestamp();
		final ClassificationTracker tracker = context.service(ClassificationTracker.class);

		tracker.classificationRunning(classificationId, headTimestamp);

		try {
			
			SnomedExternalClassificationService classificationService = context.service(SnomedExternalClassificationService.class);
			
			// send external request
			String externalClassificationRequestId = classificationService.sendExternalRequest(branch, reasonerId, userId);
			
			Path result = classificationService.getExternalResults(externalClassificationRequestId);
			Map<String, Path> filePaths = classificationService.getRequiredFilePaths(result, classificationId);
			
			IReasonerTaxonomy inferredTaxonomy;
			
			if (filePaths.containsKey(SnomedExternalClassificationService.EQUIVALENT_CONCEPTS_FILENAME_PATTERN)) {
				
				Collection<List<String>> equivalentConcepts = classificationService.getLinesFromFile(result,
						filePaths.get(SnomedExternalClassificationService.EQUIVALENT_CONCEPTS_FILENAME_PATTERN));
				
				if (!equivalentConcepts.isEmpty()) {
					inferredTaxonomy = new ExternalReasonerTaxonomy(equivalentConcepts);
				} else {
					inferredTaxonomy = IReasonerTaxonomy.EMPTY;
				}
				
			} else {
				inferredTaxonomy = IReasonerTaxonomy.EMPTY;
			}
			
			Collection<List<String>> relationships;
			
			if (filePaths.containsKey(SnomedExternalClassificationService.RELATIONSHIP_FILENAME_PATTERN)) {
				relationships = classificationService.getLinesFromFile(result,
						filePaths.get(SnomedExternalClassificationService.RELATIONSHIP_FILENAME_PATTERN));
			} else {
				relationships = emptyList();
			}
			
			ExternalClassificationNormalFormGenerator normalFormGenerator = new ExternalClassificationNormalFormGenerator(context, relationships);
			tracker.classificationCompleted(classificationId, inferredTaxonomy, normalFormGenerator);
			
		} catch (final Exception e) {
			tracker.classificationFailed(classificationId);
			throw new ReasonerApiException("Exception caught while running external classification.", e);
		}

		return Boolean.TRUE;
	}

}
