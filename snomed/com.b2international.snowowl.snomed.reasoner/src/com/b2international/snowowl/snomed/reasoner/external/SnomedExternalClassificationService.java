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
package com.b2international.snowowl.snomed.reasoner.external;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.newHashMap;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.b2international.commons.http.ExtendedLocale;
import com.b2international.commons.time.TimeUtil;
import com.b2international.snowowl.core.ApplicationContext;
import com.b2international.snowowl.core.IDisposableService;
import com.b2international.snowowl.core.api.SnowowlRuntimeException;
import com.b2international.snowowl.core.branch.Branch;
import com.b2international.snowowl.datastore.file.FileRegistry;
import com.b2international.snowowl.datastore.internal.file.InternalFileRegistry;
import com.b2international.snowowl.eventbus.IEventBus;
import com.b2international.snowowl.retrofit.AuthenticationInterceptor;
import com.b2international.snowowl.retrofit.PromiseCallAdapterFactory;
import com.b2international.snowowl.snomed.core.domain.BranchMetadataResolver;
import com.b2international.snowowl.snomed.core.domain.Rf2ExportResult;
import com.b2international.snowowl.snomed.core.domain.Rf2RefSetExportLayout;
import com.b2international.snowowl.snomed.core.domain.Rf2ReleaseType;
import com.b2international.snowowl.snomed.datastore.SnomedDatastoreActivator;
import com.b2international.snowowl.snomed.datastore.config.SnomedClassificationConfiguration;
import com.b2international.snowowl.snomed.datastore.config.SnomedCoreConfiguration;
import com.b2international.snowowl.snomed.datastore.id.SnomedIdentifiers;
import com.b2international.snowowl.snomed.datastore.request.SnomedRequests;
import com.b2international.snowowl.snomed.reasoner.domain.ClassificationStatus;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 *
 * @since 6.12.2
 */
public class SnomedExternalClassificationService implements IDisposableService {

	public static final String EQUIVALENT_CONCEPTS_FILENAME_PATTERN = "der2_sRefset_EquivalentConceptSimpleMap";
	public static final String RELATIONSHIP_FILENAME_PATTERN = "sct2_Relationship";
	
	private static final String PREVIOUS_PACKAGE_METADATA_KEY = "previousPackage";
	private static final String DEPENDENCY_PACKAGE_METADATA_KEY = "dependencyPackage";
	
	private static final List<ExtendedLocale> LOCALES = ImmutableList.of(ExtendedLocale.valueOf("en-gb"), ExtendedLocale.valueOf("en-us"));
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SnomedExternalClassificationService.class);
	private static final Splitter TAB_SPLITTER = Splitter.on('\t');
	
	private boolean disposed = false;
	private SnomedExternalClassificationServiceClient client;
	private InternalFileRegistry fileRegistry;
	private long numberOfPollTries;
	private long timeBetweenPollTries;
	
	public SnomedExternalClassificationService(SnomedClassificationConfiguration classificationConfig) {
		
		final ObjectMapper mapper = new ObjectMapper()
				.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
				.setSerializationInclusion(Include.NON_NULL);
		
		String extServiceUser = classificationConfig.getExternalService().getUserName();
		String extServicePassword = classificationConfig.getExternalService().getPassword();
		
		client = new Retrofit.Builder()
				.baseUrl(classificationConfig.getExternalService().getUrl())
				.client(new OkHttpClient.Builder()
						.addInterceptor(new AuthenticationInterceptor(Credentials.basic(extServiceUser, extServicePassword, Charsets.UTF_8)))
						.build())
				.addCallAdapterFactory(new PromiseCallAdapterFactory(mapper, ExternalClassificationServiceError.class))
				.addConverterFactory(JacksonConverterFactory.create())
				.build()
				.create(SnomedExternalClassificationServiceClient.class);
		
		fileRegistry = (InternalFileRegistry) ApplicationContext.getServiceForClass(FileRegistry.class);
		
		numberOfPollTries = classificationConfig.getExternalService().getNumberOfPollTries();
		timeBetweenPollTries = classificationConfig.getExternalService().getTimeBetweenPollTries();
		
	}

	public String sendExternalRequest(Branch branch, String reasonerId, String userId) {
        
		try {
        	
			String previousPackage = BranchMetadataResolver.getEffectiveBranchMetadataValue(branch, PREVIOUS_PACKAGE_METADATA_KEY);
			String dependencyPackage = BranchMetadataResolver.getEffectiveBranchMetadataValue(branch, DEPENDENCY_PACKAGE_METADATA_KEY);
			
			if (Strings.isNullOrEmpty(previousPackage)) {
				throw new SnowowlRuntimeException("Exception while preparing request for external classification. No previousPackage metadata set.");
			}

			String shortName = BranchMetadataResolver.getEffectiveBranchMetadataValue(branch, SnomedCoreConfiguration.BRANCH_EXTENSION_SHORTNAME_KEY);
			String defaultNamespace = BranchMetadataResolver.getEffectiveBranchMetadataValue(branch, SnomedCoreConfiguration.DEFAULT_NAMESPACE);
			
			String countryAndNamespaceElement = getCountryAndNamespaceElement(shortName, defaultNamespace);
			
			Rf2ExportResult exportResult = SnomedRequests.rf2().prepareExport()
        			.setReleaseType(Rf2ReleaseType.DELTA)
        			.setIncludePreReleaseContent(true)
        			.setConceptsAndRelationshipsOnly(true)
        			.setUserId(userId)
        			.setReferenceBranch(branch.path())
        			.setRefSetExportLayout(Rf2RefSetExportLayout.COMBINED)
        			.setCountryNamespaceElement(countryAndNamespaceElement)
        			.setLocales(LOCALES)
        			.build(SnomedDatastoreActivator.REPOSITORY_UUID)
        			.execute(getEventBus())
        			.getSync();
        	
        	UUID fileId = exportResult.getRegistryId();
			File rf2Delta = fileRegistry.getFile(fileId);
        	
        	RequestBody previousPackageRequestBody = previousPackage != null ? RequestBody.create(MediaType.parse("text/plain"), previousPackage) : null;
        	RequestBody dependencyPackageRequestBody = dependencyPackage != null ? RequestBody.create(MediaType.parse("text/plain"), dependencyPackage) : null;
        	
        	RequestBody fileRequestBody = RequestBody.create(MediaType.parse("multipart/form-data"), rf2Delta);
        	MultipartBody.Part rf2DeltaBody = MultipartBody.Part.createFormData("rf2Delta", rf2Delta.getName(), fileRequestBody);
        	
        	RequestBody branchPathRequestBody = RequestBody.create(MediaType.parse("text/plain"), branch.path());
        	RequestBody reasonerIdRequestBody = RequestBody.create(MediaType.parse("text/plain"), reasonerId);
        	
        	LOGGER.info("Sending export results for external classification, branch path: {}, previous package: {}, {}reasoner: {}", branch.path(), previousPackage, 
        			Strings.isNullOrEmpty(dependencyPackage) ? "" : String.format("dependency package: %s, ", dependencyPackage), reasonerId);
        	
        	String location = client.sendResults(previousPackageRequestBody, dependencyPackageRequestBody, rf2DeltaBody, branchPathRequestBody, reasonerIdRequestBody)
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

	private String getCountryAndNamespaceElement(String shortName, String defaultNamespace) {
		if (!Strings.isNullOrEmpty(shortName) && !Strings.isNullOrEmpty(defaultNamespace)) {
			return shortName.toUpperCase() + defaultNamespace;
		}
		return SnomedIdentifiers.INT_NAMESPACE;
	}

	public Path getExternalResults(String externalClassificationRequestId) {
		
		ClassificationStatus externalClassificationStatus = ClassificationStatus.SCHEDULED;
		
		try {
			
			for (long pollTry = 1; pollTry <= numberOfPollTries; pollTry++) {
				
				LOGGER.info("Polling external classification results with external id: {} ({})", externalClassificationRequestId, pollTry);
				
				ExternalClassificationStatus classificationStatus = client.getClassification(externalClassificationRequestId)
						.fail(fail -> {
							throw Throwables.propagate(fail);
						})
						.getSync();
				externalClassificationStatus = classificationStatus.getStatus();
				
				if (externalClassificationStatus == ClassificationStatus.COMPLETED) {
					LOGGER.info("External classification request completed with external id: {}", externalClassificationRequestId);
					break;
				} else if (externalClassificationStatus == ClassificationStatus.FAILED) {
					throw new ExternalClassificationServiceException(
							"External classification request (external id: %s) returned with FAILED status. Reason: %s", externalClassificationRequestId,
							classificationStatus.getStatusMessage());
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
			
			LOGGER.info("Downloading results for external classification request with external id: {}", externalClassificationRequestId);
			
			InputStream inputStream = client.getResult(externalClassificationRequestId).getSync().byteStream();
			
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

	public Map<String, Path> getRequiredFilePaths(Path results, String internalClassificationId) {
		
		LOGGER.info("Processing external classification results for internal id {}", internalClassificationId);
		
		Map<String, Path> filePathMap = newHashMap();
		
		try (FileSystem zipfs = FileSystems.newFileSystem(results, null)) {
			for (final Path path : zipfs.getRootDirectories()) {
				Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
					@Override 
					public FileVisitResult visitFile(final Path path, final BasicFileAttributes attrs) throws IOException {
						
						String fileName = path.getFileName().toString();
						if (fileName.startsWith(RELATIONSHIP_FILENAME_PATTERN)) {
							filePathMap.put(RELATIONSHIP_FILENAME_PATTERN, path);
						} else if (fileName.startsWith(EQUIVALENT_CONCEPTS_FILENAME_PATTERN)) {
							filePathMap.put(EQUIVALENT_CONCEPTS_FILENAME_PATTERN, path);
						}
						
						return FileVisitResult.CONTINUE;
					}

				});
			}
	
		} catch (final Exception e) {
			throw new SnowowlRuntimeException("An exception happened while processing external classification results", e);
		}
		
		return filePathMap;
	}

	public Collection<List<String>> getLinesFromFile(Path archivePath, Path filePath) throws IOException {
		try (FileSystem zipfs = FileSystems.newFileSystem(archivePath, null)) {
			return Files.lines(zipfs.getPath(filePath.toString()))
					.skip(1) // header
					.map( line -> TAB_SPLITTER.splitToList(line))
					.collect(toList());
		}
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
