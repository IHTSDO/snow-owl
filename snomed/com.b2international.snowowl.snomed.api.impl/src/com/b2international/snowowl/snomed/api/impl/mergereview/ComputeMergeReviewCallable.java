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

import static com.google.common.collect.Lists.newArrayList;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.b2international.commons.http.ExtendedLocale;
import com.b2international.snowowl.core.exceptions.NotFoundException;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserComponentWithId;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserConcept;
import com.b2international.snowowl.snomed.api.impl.domain.browser.SnomedBrowserConcept;
import com.b2international.snowowl.snomed.api.mergereview.ISnomedBrowserMergeReviewDetail;
import com.google.common.collect.Maps;

/**
 * @since 4.5
 */
public class ComputeMergeReviewCallable extends MergeReviewCallable<ISnomedBrowserMergeReviewDetail> {

	protected ComputeMergeReviewCallable(final String conceptId, final MergeReviewParameters parameters) {
		super(conceptId, parameters);
	}

	@Override
	protected ISnomedBrowserMergeReviewDetail onSuccess() throws IOException {
		
		final ISnomedBrowserConcept sourceConcept = getBrowserConcept(parameters.getSourcePath(), conceptId, parameters.getExtendedLocales());
		final ISnomedBrowserConcept targetConcept = getBrowserConcept(parameters.getTargetPath(), conceptId, parameters.getExtendedLocales());

		ISnomedBrowserConcept autoMergedConcept = null;
		
		if (sourceConcept != null && targetConcept != null) {
			autoMergedConcept = mergeConcepts(sourceConcept, targetConcept, parameters.getExtendedLocales());
		} else if (sourceConcept != null) {
			autoMergedConcept = sourceConcept;
		} else if (targetConcept != null) {
			autoMergedConcept = targetConcept;
		}

		ISnomedBrowserConcept manuallyMergedConcept = null;

		if (getManualConceptMergeService().exists(parameters.getTargetPath(), parameters.getMergeReviewId(), conceptId)) {
			manuallyMergedConcept = getManualConceptMergeService().retrieve(parameters.getTargetPath(), parameters.getMergeReviewId(), conceptId);
		}

		return new SnomedBrowserMergeReviewDetail(sourceConcept, targetConcept, autoMergedConcept, manuallyMergedConcept);
	}

	private ISnomedBrowserConcept getBrowserConcept(String branchPath, String conceptId, List<ExtendedLocale> locales) {
		try {
			return getBrowserService().getConceptDetails(branchPath, conceptId, locales);
		} catch (NotFoundException e) {
			return null;
		}
	}

	private SnomedBrowserConcept mergeConcepts(
			final ISnomedBrowserConcept sourceConcept,
			final ISnomedBrowserConcept targetConcept,
			final List<ExtendedLocale> locales) {

		final SnomedBrowserConcept mergedConcept = new SnomedBrowserConcept();

		// If one of the concepts is unpublished, then it's values are newer. If both are unpublished, source would win
		ISnomedBrowserConcept winner = sourceConcept;
		ISnomedBrowserConcept looser = targetConcept; 
		
		if (sourceConcept.getEffectiveTime() != null && targetConcept.getEffectiveTime() == null) {
			winner = targetConcept;
			looser = sourceConcept;
		}
		
		// Set directly owned values
		mergedConcept.setConceptId(winner.getConceptId());
		
		mergedConcept.setActive(winner.isActive());
		mergedConcept.setReleased(winner.isReleased());
		mergedConcept.setEffectiveTime(winner.getEffectiveTime());
		mergedConcept.setModuleId(winner.getModuleId());
		
		mergedConcept.setDefinitionStatus(winner.getDefinitionStatus());
		mergedConcept.setIsLeafInferred(winner.getIsLeafInferred());
		mergedConcept.setIsLeafStated(winner.getIsLeafStated());

		mergedConcept.setInactivationIndicator(winner.getInactivationIndicator());
		mergedConcept.setAssociationTargets(winner.getAssociationTargets());
		
		mergedConcept.setDescriptions(mergeBrowserCollections(winner.getDescriptions(), looser.getDescriptions()));
		mergedConcept.setRelationships(mergeBrowserCollections(winner.getRelationships(), looser.getRelationships()));
		mergedConcept.setClassAxioms(mergeBrowserCollections(winner.getClassAxioms(), looser.getClassAxioms()));
		mergedConcept.setGciAxioms(mergeBrowserCollections(winner.getGciAxioms(), looser.getGciAxioms()));

		return mergedConcept;
		
	}

	@Override
	protected ISnomedBrowserMergeReviewDetail onSkip() {
		return ISnomedBrowserMergeReviewDetail.SKIP_DETAIL;
	}
	
	private <T extends ISnomedBrowserComponentWithId> List<T> mergeBrowserCollections(List<T> sourceComponents, List<T> targetComponents) {
		
		List<T> mergedComponents = newArrayList();
		
		Map<String, T> sourceComponentsMap = Maps.uniqueIndex(sourceComponents, ISnomedBrowserComponentWithId::getId);
		Map<String, T> targetComponentsMap = Maps.uniqueIndex(targetComponents, ISnomedBrowserComponentWithId::getId);
		
		sourceComponentsMap.forEach( (id, sourceComponent) -> {
			
			if (sourceComponent.getEffectiveTime() != null && targetComponentsMap.containsKey(id) && targetComponentsMap.get(id).getEffectiveTime() == null) {
				mergedComponents.add(targetComponentsMap.get(id));
			} else {
				mergedComponents.add(sourceComponent);
			}
			
		});
		
		return mergedComponents;
		
	}

}
