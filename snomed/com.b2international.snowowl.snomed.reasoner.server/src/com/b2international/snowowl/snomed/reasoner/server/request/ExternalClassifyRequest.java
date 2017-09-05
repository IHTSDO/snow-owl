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
package com.b2international.snowowl.snomed.reasoner.server.request;

import java.nio.file.Path;

import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.b2international.commons.TimedProgressMonitorWrapper;
import com.b2international.snowowl.core.ServiceProvider;
import com.b2international.snowowl.core.events.Request;
import com.b2international.snowowl.core.exceptions.ApiError;
import com.b2international.snowowl.datastore.remotejobs.RemoteJob;
import com.b2international.snowowl.snomed.reasoner.classification.ClassificationSettings;
import com.b2international.snowowl.snomed.reasoner.classification.SnomedExternalReasonerService;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @since 5.10.10
 */
@SuppressWarnings("serial")
public class ExternalClassifyRequest implements Request<ServiceProvider, ApiError> {

	private static final Logger LOG = LoggerFactory.getLogger(ExternalClassifyRequest.class);
	
	@JsonProperty
	private final ClassificationSettings settings;

	public ExternalClassifyRequest(ClassificationSettings settings) {
		this.settings = settings;
	}
	
	@JsonProperty
	public String getReasonerId() {
		return settings.getReasonerId();
	}
	
	@JsonProperty
	public String getBranch() {
		return settings.getSnomedBranchPath().getPath();
	}
	
	@Override
	public ApiError execute(ServiceProvider context) {
		
		RemoteJob job = context.service(RemoteJob.class);
		String internalClassificationId = job.getId();
		
		IProgressMonitor monitor = context.service(IProgressMonitor.class);

		TimedProgressMonitorWrapper wrapper = new TimedProgressMonitorWrapper(monitor);
		wrapper.beginTask(job.getName() + "...", IProgressMonitor.UNKNOWN);

		try {
			
			// TODO lock?
			
			SnomedExternalReasonerService reasonerService = context.service(SnomedExternalReasonerService.class);
			
			LOG.info("Initiating external classification request for {}", settings.getSnomedBranchPath().getPath());
			
			String externalClassificationId = reasonerService.sendExternalRequest(settings.getSnomedBranchPath().getPath(), settings.getReasonerId());
			Path results = reasonerService.getExternalResults(externalClassificationId);
			reasonerService.registerExternalResults(internalClassificationId, results);
			
			return new ApiError.Builder("OK").code(200).build();
		} catch (Exception e) {
			return createApiError(e);
		} finally {
			wrapper.done();
		}
	}

	private static ApiError createApiError(final Exception e) {
		LOG.error("Caught exception while waiting for external classification results.", e);
		return new ApiError.Builder("Caught exception while waiting for external classification results.").code(500).build();
	}
}
