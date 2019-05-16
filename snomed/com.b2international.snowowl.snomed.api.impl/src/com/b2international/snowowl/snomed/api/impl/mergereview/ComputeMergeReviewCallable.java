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
package com.b2international.snowowl.snomed.api.impl.mergereview;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.b2international.commons.http.ExtendedLocale;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserConcept;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserDescription;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserRelationship;
import com.b2international.snowowl.snomed.api.impl.domain.browser.SnomedBrowserConcept;
import com.b2international.snowowl.snomed.api.mergereview.ISnomedBrowserMergeReviewDetail;

/**
 * @since 4.5
 */
public class ComputeMergeReviewCallable extends MergeReviewCallable<ISnomedBrowserMergeReviewDetail> {

	protected ComputeMergeReviewCallable(final String conceptId, final MergeReviewParameters parameters) {
		super(conceptId, parameters);
	}

	@Override
	protected ISnomedBrowserMergeReviewDetail onSuccess() throws IOException {
		final ISnomedBrowserConcept sourceConcept = getBrowserService().getConceptDetails(parameters.getSourcePath(), conceptId, parameters.getExtendedLocales());
		final ISnomedBrowserConcept targetConcept = getBrowserService().getConceptDetails(parameters.getTargetPath(), conceptId, parameters.getExtendedLocales());

		final ISnomedBrowserConcept autoMergedConcept = mergeConcepts(sourceConcept, targetConcept, parameters.getExtendedLocales());

		ISnomedBrowserConcept manuallyMergedConcept = null;

		if (getManualConceptMergeService().exists(parameters.getTargetPath(), parameters.getMergeReviewId(), conceptId)) {
			manuallyMergedConcept = getManualConceptMergeService().retrieve(parameters.getTargetPath(), parameters.getMergeReviewId(), conceptId);
		}

		return new SnomedBrowserMergeReviewDetail(sourceConcept, targetConcept, autoMergedConcept, manuallyMergedConcept);
	}

	private SnomedBrowserConcept mergeConcepts(
			final ISnomedBrowserConcept sourceConcept,
			final ISnomedBrowserConcept targetConcept,
			final List<ExtendedLocale> locales) {

		final SnomedBrowserConcept mergedConcept = new SnomedBrowserConcept();
		// If one of the concepts is unpublished, then it's values are newer.  If both are unpublished, source would win
		ISnomedBrowserConcept winner = sourceConcept;
		if (targetConcept.getEffectiveTime() == null && sourceConcept.getEffectiveTime() != null) {
			winner = targetConcept;
		}
		// Set directly owned values
		mergedConcept.setConceptId(winner.getConceptId());
		mergedConcept.setActive(winner.isActive());
		mergedConcept.setDefinitionStatus(winner.getDefinitionStatus());
		mergedConcept.setEffectiveTime(winner.getEffectiveTime());
		mergedConcept.setModuleId(winner.getModuleId());
		mergedConcept.setIsLeafInferred(winner.getIsLeafInferred());
		mergedConcept.setIsLeafStated(winner.getIsLeafStated());

		mergedConcept.setInactivationIndicator(winner.getInactivationIndicator());
		mergedConcept.setAssociationTargets(winner.getAssociationTargets());

		// Merge Descriptions - take all the descriptions from source, and add in from target
		// if they're unpublished, which will cause an overwrite in the Set if the Description Id matches
		// TODO UNLESS the source description is also unpublished (Change to use map?)
		final Set<ISnomedBrowserDescription> mergedDescriptions = new HashSet<ISnomedBrowserDescription>(sourceConcept.getDescriptions());
		for (final ISnomedBrowserDescription thisDescription : targetConcept.getDescriptions()) {
			if (thisDescription.getEffectiveTime() == null) {
				mergedDescriptions.add(thisDescription);
			}
		}
		mergedConcept.setDescriptions(new ArrayList<ISnomedBrowserDescription>(mergedDescriptions));

		// Merge Relationships  - same process using Set to remove duplicated
		final Set<ISnomedBrowserRelationship> mergedRelationships = new HashSet<ISnomedBrowserRelationship>(sourceConcept.getRelationships());
		for (final ISnomedBrowserRelationship thisRelationship : targetConcept.getRelationships()) {
			if (thisRelationship.getEffectiveTime() == null) {
				mergedRelationships.add(thisRelationship);
			}
		}
		mergedConcept.setRelationships(new ArrayList<ISnomedBrowserRelationship>(mergedRelationships));

		return mergedConcept;
	}

	@Override
	protected ISnomedBrowserMergeReviewDetail onSkip() {
		return ISnomedBrowserMergeReviewDetail.SKIP_DETAIL;
	}

}
