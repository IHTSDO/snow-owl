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
package com.b2international.snowowl.snomed.datastore.config;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @since 5.10.10
 */
public class SnomedClassificationConfiguration {

	public static final String ELK_REASONER_ID = "org.semanticweb.elk.elk.reasoner.factory"; //$NON-NLS-1$
	public static final String DEFAULT_REASONER = ELK_REASONER_ID;
	
	public static final int DEFAULT_MAXIMUM_REASONER_COUNT = 2;
	public static final int DEFAULT_MAXIMUM_REASONER_RESULTS = 10;
	public static final int DEFAULT_MAXIMUM_REASONER_RUNS = 1000;
	
	@NotEmpty
	@JsonProperty(value = "defaultReasoner", required = false)
	private String defaultReasoner = DEFAULT_REASONER;
	
	@JsonProperty(value = "showReasonerUsageWarning", required = false)
	private boolean showReasonerUsageWarning = true;
	
	@Min(1)
	@Max(3)
	@JsonProperty(value = "maxReasonerCount", required = false)
	private int maxReasonerCount = DEFAULT_MAXIMUM_REASONER_COUNT;
	
	@Min(1)
	@Max(100)
	@JsonProperty(value = "maxReasonerResults", required = false)
	private int maxReasonerResults = DEFAULT_MAXIMUM_REASONER_RESULTS;
	
	@Min(1)
	@Max(1_000_000)
	@JsonProperty(value = "maxReasonerRuns", required = false)
	private int maxReasonerRuns = DEFAULT_MAXIMUM_REASONER_RUNS;

	@Valid
	@JsonProperty(value = "externalService", required = false)
	private SnomedClassificationServiceConfiguration externalService;

	/**
	 * @return the currently set default reasoner ID 
	 */
	public String getDefaultReasoner() {
		return defaultReasoner;
	}

	/**
	 * @param defaultReasoner - the reasoner to set as default
	 */
	public void setDefaultReasoner(String defaultReasoner) {
		this.defaultReasoner = defaultReasoner;
	}

	public boolean isShowReasonerUsageWarning() {
		return showReasonerUsageWarning;
	}

	public void setShowReasonerUsageWarning(boolean showReasonerUsageWarning) {
		this.showReasonerUsageWarning = showReasonerUsageWarning;
	}

	/**
	 * @return the number of reasoners that are permitted to run simultaneously
	 */
	public int getMaxReasonerCount() {
		return maxReasonerCount;
	}

	/**
	 * @param maxReasonerCount sets the number of reasoners that are permitted to run simultaneously
	 */
	public void setMaxReasonerCount(int maxReasonerCount) {
		this.maxReasonerCount = maxReasonerCount;
	}

	/**
	 * @return the number of inferred taxonomies that should be kept in memory. The user can only choose to save
	 *         the results of the classification run if the corresponding inferred taxonomy is still present.
	 */
	public int getMaxReasonerResults() {
		return maxReasonerResults;
	}

	/**
	 * @param maxReasonerResults the maxReasonerResults to set
	 */
	public void setMaxReasonerResults(int maxReasonerResults) {
		this.maxReasonerResults = maxReasonerResults;
	}

	/**
	 * @return the number of classification run details to preserve. Details include inferred and redundant 
	 *         relationships, the list of equivalent concepts found during classification, and job metadata
	 *         (creation, start and end times, final state, requesting user). 
	 */
	public int getMaxReasonerRuns() {
		return maxReasonerRuns;
	}

	/**
	 * @param maxReasonerRuns the maxReasonerRuns to set
	 */
	public void setMaxReasonerRuns(int maxReasonerRuns) {
		this.maxReasonerRuns = maxReasonerRuns;
	}

	/**
	 * @return the externalService
	 */
	public SnomedClassificationServiceConfiguration getExternalService() {
		return externalService;
	}

	/**
	 * @param externalService the externalService to set
	 */
	public void setExternalService(SnomedClassificationServiceConfiguration externalService) {
		this.externalService = externalService;
	}
	
}
