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
import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.b2international.commons.BooleanUtils;
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
import com.b2international.snowowl.snomed.reasoner.classification.AbstractEquivalenceSet;
import com.b2international.snowowl.snomed.reasoner.classification.AbstractResponse.Type;
import com.b2international.snowowl.snomed.reasoner.classification.ClassificationSettings;
import com.b2international.snowowl.snomed.reasoner.classification.EquivalenceSet;
import com.b2international.snowowl.snomed.reasoner.classification.GetResultResponse;
import com.b2international.snowowl.snomed.reasoner.classification.GetResultResponseChanges;
import com.b2international.snowowl.snomed.reasoner.classification.SnomedExternalReasonerService;
import com.b2international.snowowl.snomed.reasoner.classification.entry.AbstractChangeEntry.Nature;
import com.b2international.snowowl.snomed.reasoner.classification.entry.ChangeConcept;
import com.b2international.snowowl.snomed.reasoner.classification.entry.RelationshipChangeEntry;
import com.b2international.snowowl.snomed.reasoner.server.request.SnomedReasonerRequests;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
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
	private static final Splitter TAB_SPLITTER = Splitter.on('\t');
	
	private boolean disposed = false;
	private SnomedExternalClassificationServiceClient client;
	private InternalFileRegistry fileRegistry;
	private long numberOfPollTries;
	private long timeBetweenPollTries;
	private final Cache<String, GetResultResponseChanges> classificationResultRegistry;
	
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
		
		classificationResultRegistry = CacheBuilder.newBuilder().maximumSize(classificationConfig.getMaxReasonerResults()).build();
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
		
		GetResultResponseChanges changes = classificationResultRegistry.getIfPresent(classificationId);
		
		if (null == changes) {
			return new GetResultResponse(GetResultResponse.Type.NOT_AVAILABLE);
		}

		return new GetResultResponse(Type.SUCCESS, changes); // FIXME?
	}

	@Override
	public void removeResult(String classificationId) {
		classificationResultRegistry.asMap().remove(classificationId);
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
        	
        	RequestBody previousReleaseRequestBody = RequestBody.create(MediaType.parse("text/plain"), previousRelease);
        	
        	RequestBody fileRequestBody = RequestBody.create(MediaType.parse("multipart/form-data"), rf2Delta);
        	MultipartBody.Part rf2DeltaBody = MultipartBody.Part.createFormData("rf2Delta", rf2Delta.getName(), fileRequestBody);
        	
        	RequestBody branchPathRequestBody = RequestBody.create(MediaType.parse("text/plain"), branchPath);
        	RequestBody reasonerIdRequestBody = RequestBody.create(MediaType.parse("text/plain"), reasonerId);
        	
        	LOGGER.info("Sending export results for external classification, branch path: {}, previous release: {}, reasoner: {}", branchPath, previousRelease, reasonerId);
        	
        	String location = client.sendResults(previousReleaseRequestBody, rf2DeltaBody, branchPathRequestBody, reasonerIdRequestBody)
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
	public Path getExternalResults(String externalClassificationId) {
		
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
		
		return checkNotNull(classificationResult);
	}

	@Override
	public void registerExternalResults(String internalClassificationId, Path results) {
		
		LOGGER.info("Processing external classification results for internal id {}", internalClassificationId);
		
		ImmutableList.Builder<RelationshipChangeEntry> relationshipBuilder = ImmutableList.builder();
		ImmutableList.Builder<AbstractEquivalenceSet> equivalentConceptsBuilder = ImmutableList.builder();
		
		try (FileSystem zipfs = FileSystems.newFileSystem(results, null)) {
			for (final Path path : zipfs.getRootDirectories()) {
				Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
					@Override 
					public FileVisitResult visitFile(final Path path, final BasicFileAttributes attrs) throws IOException {
						
						String fileName = path.getFileName().toString();
						if (fileName.startsWith("sct2_Relationship")) {
							processRelationships(path, relationshipBuilder);
						} else if (fileName.startsWith("der2_sRefset_EquivalentConceptSimpleMap")) {
							processEquivalentConcepts(path, equivalentConceptsBuilder);
						}
						
						return FileVisitResult.CONTINUE;
					}

				});
			}
	
		} catch (final Exception e) {
			throw new SnowowlRuntimeException("An exception happened while processing external classification results", e);
		}
		
		List<RelationshipChangeEntry> relationshipChanges = relationshipBuilder.build();
		List<AbstractEquivalenceSet> equivalentConcepts = equivalentConceptsBuilder.build();
		
		GetResultResponseChanges changes = new GetResultResponseChanges(0, equivalentConcepts, relationshipChanges, Collections.emptyList());
		classificationResultRegistry.put(internalClassificationId, changes);
		
		try {
			Files.deleteIfExists(results);
		} catch (IOException e) {
			// ignore
		}
		
	}
	
	protected void processEquivalentConcepts(Path path, Builder<AbstractEquivalenceSet> equivalentConceptsBuilder) throws IOException, IllegalStateException {
		
		ArrayListMultimap<String, String> equivalentConceptSets = ArrayListMultimap.create();
		
		Files.lines(path)
			.skip(1) // header
			.forEach( line -> {
				List<String> elements = TAB_SPLITTER.splitToList(line);
				
				String referencedComponent = elements.get(5); // equivalent concept
				String mapTarget = elements.get(6); // id of equivalent set
				
				equivalentConceptSets.put(mapTarget, referencedComponent);
			});
		
		for (Entry<String, Collection<String>> entry : equivalentConceptSets.asMap().entrySet()) {
			
			if (entry.getValue().size() > 1) {
				String suggestedConceptId = entry.getValue().stream().findFirst().get();
				List<String> equivalentIds = entry.getValue().stream().skip(1).collect(toList());
				
				equivalentConceptsBuilder.add(new EquivalenceSet(suggestedConceptId, equivalentIds));
			} else {
				throw new IllegalStateException("Equivalent concept sets must have at least two elements");
			}
			
		}
	}

	private void processRelationships(final Path path, Builder<RelationshipChangeEntry> builder) throws IOException {
		Files.lines(path)
			.skip(1) // header
			.forEach(line -> {
				List<String> elements = TAB_SPLITTER.splitToList(line);
				
				String id = elements.get(0);
				boolean active = BooleanUtils.valueOf(elements.get(2));
				long sourceId = Long.valueOf(elements.get(4));
				long destinationId = Long.valueOf(elements.get(5));
				int groupId = Integer.valueOf(elements.get(6));
				long typeId = Long.valueOf(elements.get(7));
				long modifierId = Long.valueOf(elements.get(9));
				
				RelationshipChangeEntry entry = new RelationshipChangeEntry(
						active ? Nature.INFERRED : Nature.REDUNDANT, 
						Strings.isNullOrEmpty(id) ? null : id,
						new ChangeConcept(sourceId, sourceId), 
						new ChangeConcept(typeId, typeId), 
						new ChangeConcept(destinationId, destinationId), 
						groupId, 
						0, 
						new ChangeConcept(modifierId, modifierId), 
						false);
				
				builder.add(entry);
			});
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
