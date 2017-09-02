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
package com.b2international.snowowl.snomed.reasoner.server.classification.external;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;

import com.b2international.snowowl.core.ApplicationContext;
import com.b2international.snowowl.eventbus.IEventBus;
import com.b2international.snowowl.snomed.datastore.config.SnomedClassificationConfiguration;
import com.b2international.snowowl.snomed.reasoner.classification.ClassificationSettings;
import com.b2international.snowowl.snomed.reasoner.classification.GetResultResponse;
import com.b2international.snowowl.snomed.reasoner.classification.SnomedExternalReasonerService;
import com.b2international.snowowl.snomed.reasoner.server.request.SnomedReasonerRequests;

/**
 * @since 5.10.13
 */
public class SnomedExternalReasonerServiceImpl implements SnomedExternalReasonerService {

	public SnomedExternalReasonerServiceImpl(SnomedClassificationConfiguration classificationConfig) {
		
	}

	@Override
	public void beginClassification(ClassificationSettings settings) {
		checkNotNull(settings, "Classification settings may not be null.");
		checkArgument(settings.isUseExternalService(), "Use external service flag must be set to true");
		
		SnomedReasonerRequests.prepareClassify()
			.setSettings(settings)
			.buildAsync()
			.execute(getEventBus())
			.getSync();
	}

	@Override
	public GetResultResponse getResult(String classificationId) {
		return null;
	}

	@Override
	public void removeResult(String classificationId) {
	}

	@Override
	public String sendExternalRequest(String path, String reasonerId) {
		return null;
	}

	@Override
	public File getExternalResults(String externalClassificationId) {
		return null;
	}

	@Override
	public void registerExternalResults(String internalClassificationId, File results) {
	}

	private static IEventBus getEventBus() {
		return ApplicationContext.getServiceForClass(IEventBus.class);
	}
}
