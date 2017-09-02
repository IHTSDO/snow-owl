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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.b2international.snowowl.core.exceptions.BadRequestException;
import com.b2international.snowowl.snomed.api.domain.classification.ClassificationStatus;
import com.b2international.snowowl.snomed.datastore.config.SnomedClassificationConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

/**
 * @since 5.10.13
 */
public class SnomedExternalClassificationServiceClient {

	private static final String CONTEXT_ROOT = "classification-service";
	
	private HttpClient client;
	private String baseUrl;
	private int numberOfPollTries;
	private int timeBetweenPollTries;

	public SnomedExternalClassificationServiceClient(SnomedClassificationConfiguration config) {
		baseUrl = config.getExternalService().getUrl();
		client = HttpClients.createDefault();
		numberOfPollTries = 10; // TODO add it to config
		timeBetweenPollTries = 1000; // TODO add it to config
	}
	
	public String sendExternalClassifyRequest(String branchPath, String previousRelease, String reasonerId, File rf2Delta) {

		HttpEntity entity = MultipartEntityBuilder.create()
				.addTextBody("previousRelease", previousRelease)
				.addTextBody("branch", branchPath)
				.addTextBody("reasonerId", reasonerId)
				.addBinaryBody("rf2Delta", rf2Delta)
				.build();
				
		HttpPost httpPost = new HttpPost(String.format("%s/%s", getServiceUrl(), "classifications"));
		httpPost.setEntity(entity);

		String externalClassificationId;
		
		try {
			
			HttpResponse response = client.execute(httpPost);
			
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
				Header header = response.getFirstHeader("Location");
				externalClassificationId = Iterables.getLast(Splitter.on("/").splitToList(header.getValue()));
			} else {
				throw new BadRequestException(String.format("Bad request: %s", response.getStatusLine().getStatusCode()));
			}
			
		} catch (IOException e) {
			throw new BadRequestException("External classify request expception", e);
		}
		
		return externalClassificationId;
	}
	
	public File getResult(String externalClassificationId) {

		ClassificationStatus status = ClassificationStatus.SCHEDULED;
		
		for (int pollTry = numberOfPollTries; pollTry > 0; pollTry--) {
			
			try {
				
				HttpGet httpGet = new HttpGet(String.format("%s/%s/%s", getServiceUrl(), "classifications", externalClassificationId));
				HttpResponse response = client.execute(httpGet);
				
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					String string = EntityUtils.toString(response.getEntity());
					ObjectMapper mapper = new ObjectMapper();
					ExternalClassificationStatus classificationStatus = mapper.readValue(string, ExternalClassificationStatus.class);
					status = classificationStatus.getStatus();
					
					if (status == ClassificationStatus.COMPLETED) {
						break;
					}
					
					if (status == ClassificationStatus.FAILED) {
						throw new BadRequestException("External classification failed");
					}
					
					Thread.sleep(timeBetweenPollTries);
					
				} else {
					throw new BadRequestException(String.format("Bad request: %s", response.getStatusLine().getStatusCode()));
				}
				
			} catch (IOException | InterruptedException e) {
				throw new BadRequestException("External classify request expception", e);
			}
			
		}
		
		if (status != ClassificationStatus.COMPLETED) {
			throw new BadRequestException("External classification failed");
		}
		
		return getResultFile(externalClassificationId);
	}

	private File getResultFile(String externalClassificationId) {
		
		Path resultPath = null;
		
		try {
			
			HttpGet httpGet = new HttpGet(String.format("%s/%s/%s/results/rf2", getServiceUrl(), "classifications", externalClassificationId));
			HttpResponse response = client.execute(httpGet);
			
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				InputStream inputStream = response.getEntity().getContent();
				
				resultPath = Paths.get("C:\\tmp\\class_result.zip"); // FIXME
				Files.copy(inputStream, resultPath, StandardCopyOption.REPLACE_EXISTING);
				
			} else {
				throw new BadRequestException(String.format("Bad request: %s", response.getStatusLine().getStatusCode()));
			}
			
		} catch (IOException e) {
			throw new BadRequestException("External classify request expception", e);
		}
		
		return resultPath.toFile();
	}

	private String getServiceUrl() {
		return String.format("%s/%s", baseUrl, CONTEXT_ROOT);
	}

	public void close() {
		if (null != client) {
			client.getConnectionManager().shutdown();
		}
	}

	
}
