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
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.b2international.commons.time.TimeUtil;
import com.b2international.snowowl.core.ApplicationContext;
import com.b2international.snowowl.core.IDisposableService;
import com.b2international.snowowl.core.api.SnowowlRuntimeException;
import com.b2international.snowowl.core.branch.Branch;
import com.b2international.snowowl.datastore.file.FileRegistry;
import com.b2international.snowowl.datastore.internal.file.InternalFileRegistry;
import com.b2international.snowowl.datastore.request.RepositoryRequests;
import com.b2international.snowowl.eventbus.IEventBus;
import com.b2international.snowowl.retrofit.PromiseCallAdapterFactory;
import com.b2international.snowowl.snomed.api.domain.classification.ClassificationStatus;
import com.b2international.snowowl.snomed.core.domain.BranchMetadataResolver;
import com.b2international.snowowl.snomed.core.domain.Rf2ReleaseType;
import com.b2international.snowowl.snomed.datastore.SnomedDatastoreActivator;
import com.b2international.snowowl.snomed.datastore.config.SnomedClassificationConfiguration;
import com.b2international.snowowl.snomed.datastore.request.SnomedRequests;
import com.b2international.snowowl.snomed.reasoner.classification.ClassificationSettings;
import com.b2international.snowowl.snomed.reasoner.classification.GetResultResponse;
import com.b2international.snowowl.snomed.reasoner.classification.SnomedExternalReasonerService;
import com.b2international.snowowl.snomed.reasoner.server.request.SnomedReasonerRequests;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * @since 5.10.13
 */
public class SnomedExternalReasonerServiceImpl implements SnomedExternalReasonerService, IDisposableService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SnomedExternalReasonerService.class);
	
	private boolean disposed = false;
	private SnomedExternalClassificationServiceClient client;
	private InternalFileRegistry fileRegistry;
	private long numberOfPollTries;
	private long timeBetweenPollTries;
	
	public SnomedExternalReasonerServiceImpl(SnomedClassificationConfiguration classificationConfig) {
		
		final ObjectMapper mapper = new ObjectMapper()
				.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
				.setSerializationInclusion(Include.NON_NULL);
		
		client = new Retrofit.Builder()
				.baseUrl(classificationConfig.getExternalService().getUrl())
				.addCallAdapterFactory(new PromiseCallAdapterFactory(mapper, ExternalClassificationServiceError.class))
				.addConverterFactory(JacksonConverterFactory.create())
				.build()
				.create(SnomedExternalClassificationServiceClient.class);
		
		// TODO authentication
		
		fileRegistry = (InternalFileRegistry) ApplicationContext.getServiceForClass(FileRegistry.class);
		
		numberOfPollTries = classificationConfig.getExternalService().getNumberOfPollTries();
		timeBetweenPollTries = classificationConfig.getExternalService().getTimeBetweenPollTries();
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
        
		try {
        	
        	UUID fileId = SnomedRequests.rf2().prepareExport()
        			.setReleaseType(Rf2ReleaseType.DELTA)
        			.setIncludeUnpublished(true)
        			.setConceptsAndRelationshipOnly(true)
        			.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
        			.execute(getEventBus())
        			.getSync();
        	
        	File rf2Delta = fileRegistry.getFile(fileId);
        	
        	Branch branch = RepositoryRequests.branching()
        			.prepareGet(branchPath)
        			.build(SnomedDatastoreActivator.REPOSITORY_UUID)
        			.execute(getEventBus())
        			.getSync();
        	
        	String previousRelease = BranchMetadataResolver.getEffectiveBranchMetadataValue(branch, "previousRelease");
        	
        	RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), rf2Delta);
        	MultipartBody.Part rf2DeltaBody = MultipartBody.Part.createFormData("rf2Delta", rf2Delta.getName(), requestFile);
        	
        	LOGGER.info("Sending export results for external classification, branch path: {}, previous release: {}, reasoner: {}", branchPath, previousRelease, reasonerId);
        	
        	String location = client.sendResults(previousRelease, rf2DeltaBody, branchPath, reasonerId)
        			.fail(fail -> {
        				throw Throwables.propagate(fail);
        			})
        			.getSync();
        	
        	fileRegistry.delete(fileId);
			
        	return Iterables.getLast(Splitter.on('/').splitToList(location));
        	
		} catch (Exception e) {
			throw new SnowowlRuntimeException("Exception while preparing data for external classification", e);
		}
	}

	@Override
	public File getExternalResults(String externalClassificationId) {
		
		ClassificationStatus externalClassificationStatus = ClassificationStatus.SCHEDULED;
		
		try {
			
			for (long pollTry = 1; pollTry <= numberOfPollTries; pollTry++) {
				
				LOGGER.info("Polling external classification results with external id: {} ({})", externalClassificationId, pollTry);
				
				ExternalClassificationStatus classificationStatus = client.getClassification(externalClassificationId)
						.fail(fail -> {
							throw Throwables.propagate(fail);
						})
						.getSync();
				externalClassificationStatus = classificationStatus.getStatus();
				
				if (externalClassificationStatus == ClassificationStatus.COMPLETED) {
					LOGGER.info("External classification request completed with external id: {}", externalClassificationId);
					break;
				} else if (externalClassificationStatus == ClassificationStatus.FAILED) {
					throw new ExternalClassificationServiceException(
							"External classification request (external id: %s) returned with FAILED status. Reason: %s", externalClassificationId,
							classificationStatus.getErrorMessage());
				}
			
				Thread.sleep(timeBetweenPollTries);
				
			}
			
		} catch (Exception e) {
			throw new SnowowlRuntimeException("Exception while polling external classification result", e);
		}
		
		if (externalClassificationStatus != ClassificationStatus.COMPLETED) {
			throw new SnowowlRuntimeException(
					String.format("External classification request did not finish with expected status in the allocated time frame (%s)",
							TimeUtil.milliToReadableString(numberOfPollTries * timeBetweenPollTries)));
		}
		
		Path classificationResult = null;
		
		try {
			
			LOGGER.info("Downloading results for external classification request with external id: {}", externalClassificationId);
			
			InputStream inputStream = client.getResult(externalClassificationId).getSync().byteStream();
			
			classificationResult = Files.createTempFile("", "");
			Files.copy(inputStream, classificationResult, StandardCopyOption.REPLACE_EXISTING);
			
		} catch (Exception e) {
			try {
				Files.deleteIfExists(classificationResult);
			} catch (IOException ignore) {
				// ignore
			}
			throw new SnowowlRuntimeException("Exception while processing external classification results", e);
		}
		
		return classificationResult.toFile();
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
		disposed = true;
	}

	@Override
	public boolean isDisposed() {
		return disposed;
	}
}
