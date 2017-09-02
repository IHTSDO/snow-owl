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
import java.util.UUID;

import com.b2international.snowowl.core.ApplicationContext;
import com.b2international.snowowl.core.IDisposableService;
import com.b2international.snowowl.datastore.file.FileRegistry;
import com.b2international.snowowl.datastore.internal.file.InternalFileRegistry;
import com.b2international.snowowl.eventbus.IEventBus;
import com.b2international.snowowl.snomed.core.domain.Rf2ReleaseType;
import com.b2international.snowowl.snomed.datastore.SnomedDatastoreActivator;
import com.b2international.snowowl.snomed.datastore.config.SnomedClassificationConfiguration;
import com.b2international.snowowl.snomed.datastore.request.SnomedRequests;
import com.b2international.snowowl.snomed.reasoner.classification.ClassificationSettings;
import com.b2international.snowowl.snomed.reasoner.classification.GetResultResponse;
import com.b2international.snowowl.snomed.reasoner.classification.SnomedExternalReasonerService;
import com.b2international.snowowl.snomed.reasoner.server.request.SnomedReasonerRequests;

/**
 * @since 5.10.13
 */
public class SnomedExternalReasonerServiceImpl implements SnomedExternalReasonerService, IDisposableService {

	private boolean disposed = false;
	private SnomedExternalClassificationServiceClient client;
	private InternalFileRegistry fileRegistry;
	
	public SnomedExternalReasonerServiceImpl(SnomedClassificationConfiguration classificationConfig) {
		client = new SnomedExternalClassificationServiceClient(classificationConfig);
		fileRegistry = (InternalFileRegistry) ApplicationContext.getServiceForClass(FileRegistry.class);
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
	public String sendExternalRequest(String branchPath, String reasonerId) {
		
		UUID fileId = SnomedRequests.rf2().prepareExport()
			.setReleaseType(Rf2ReleaseType.DELTA)
			.setIncludeUnpublished(true)
			.setConceptsAndRelationshipOnly(true)
			.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
			.execute(getEventBus())
			.getSync();
		
		File rf2Delta = fileRegistry.getFile(fileId);
		
		// TODO get previous release from branch metadata
		String externalClassificationId = client.sendExternalClassifyRequest(branchPath, "2017-01-31", reasonerId, rf2Delta);
		
		return externalClassificationId;
	}

	@Override
	public File getExternalResults(String externalClassificationId) {
		return client.getResult(externalClassificationId);
	}

	@Override
	public void registerExternalResults(String internalClassificationId, File results) {
		// TODO
	}

	private static IEventBus getEventBus() {
		return ApplicationContext.getServiceForClass(IEventBus.class);
	}

	@Override
	public void dispose() {
		if (client != null) {
			client.close();
			client = null;
		}
		
		disposed = true;
	}

	@Override
	public boolean isDisposed() {
		return disposed;
	}
}
