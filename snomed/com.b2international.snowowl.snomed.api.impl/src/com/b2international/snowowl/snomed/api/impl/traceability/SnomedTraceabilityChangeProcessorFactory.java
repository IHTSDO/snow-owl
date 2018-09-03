/*
 * Copyright 2011-2018 B2i Healthcare Pte Ltd, http://b2i.sg
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
package com.b2international.snowowl.snomed.api.impl.traceability;

import java.util.TimeZone;

import com.b2international.snowowl.core.ApplicationContext;
import com.b2international.snowowl.core.api.IBranchPath;
import com.b2international.snowowl.core.api.SnowowlServiceException;
import com.b2international.snowowl.core.ft.FeatureToggles;
import com.b2international.snowowl.core.ft.Features;
import com.b2international.snowowl.datastore.ICDOChangeProcessor;
import com.b2international.snowowl.datastore.server.CDOChangeProcessorFactory;
import com.b2international.snowowl.snomed.common.ContentSubType;
import com.b2international.snowowl.snomed.datastore.SnomedDatastoreActivator;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;

/**
 * CDO change processor factory responsible to create {@link SnomedTraceabilityChangeProcessor traceability change processors} for the SNOMED CT terminology.
 */
public class SnomedTraceabilityChangeProcessorFactory implements CDOChangeProcessorFactory {

	private static final String FACTORY_NAME = "SNOMED CT traceability change processor factory";
	private ObjectWriter objectWriter;

	public SnomedTraceabilityChangeProcessorFactory() {
		this.objectWriter = createObjectWriter();
	}

	@Override
	public ICDOChangeProcessor createChangeProcessor(final IBranchPath branchPath) throws SnowowlServiceException {
		if (skipTraceabilityChangeProcessing(branchPath)) {
			return ICDOChangeProcessor.NULL_IMPL;
		} else {
			return new SnomedTraceabilityChangeProcessor(branchPath, objectWriter);
		}
	}

	@Override
	public String getFactoryName() {
		return FACTORY_NAME;
	}

	private boolean skipTraceabilityChangeProcessing(final IBranchPath branchPath) {
		return isSnapshotImportInProgress(branchPath) || isFullImportInProgress(branchPath) || isReindexInProgress();
	}

	private boolean isSnapshotImportInProgress(final IBranchPath branchPath) {
		return getFeatureToggles().isEnabled(Features.getImportFeatureToggle(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath.getPath(), ContentSubType.SNAPSHOT.getLowerCaseName()));
	}
	
	private boolean isFullImportInProgress(final IBranchPath branchPath) {
		return getFeatureToggles().isEnabled(Features.getImportFeatureToggle(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath.getPath(), ContentSubType.FULL.getLowerCaseName()));
	}

	private boolean isReindexInProgress() {
		return getFeatureToggles().isEnabled(Features.getReindexFeatureToggle(SnomedDatastoreActivator.REPOSITORY_UUID));
	}
	
	private FeatureToggles getFeatureToggles() {
		return ApplicationContext.getServiceForClass(FeatureToggles.class);
	}
	
	private ObjectWriter createObjectWriter() {
		
		final ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setSerializationInclusion(Include.NON_EMPTY);
	
		final ISO8601DateFormat df = new ISO8601DateFormat();
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
		objectMapper.setDateFormat(df);
		objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
	
		return objectMapper.writer();
	}
}
