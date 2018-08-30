/*
 * Copyright 2018 B2i Healthcare Pte Ltd, http://b2i.sg
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

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Drools related application level configuration parameters.
 * 
 * since 5.14
 */
public class SnomedDroolsConfiguration {
	
	@NotEmpty
	private String rulesDirectory = "/opt/termserver/snomed-drools-rules";
	
	@JsonProperty(required = false)
	private String awsKey;
	
	@JsonProperty(required = false)
	private String awsPrivateKey;
	
	@JsonProperty(required = false)
	private String resourcesBucket;
	
	@JsonProperty(required = false)
	private String resourcesPath;
	
	/**
	 * @return the rules directory
	 */
	public String getRulesDirectory() {
		return rulesDirectory;
	}

	/**
	 * @param rulesDirectory the rulesDirectory to set
	 */
	public void setRulesDirectory(String rulesDirectory) {
		this.rulesDirectory = rulesDirectory;
	}

	/**
	 * @return the aws host key
	 */
	public String getAwsKey() {
		return awsKey;
	}

	/**
	 * @param awsKey the awsKey to set
	 */
	public void setAwsKey(String awsKey) {
		this.awsKey = awsKey;
	}

	/**
	 * @return the awsPrivateKey
	 */
	public String getAwsPrivateKey() {
		return awsPrivateKey;
	}
	
	/**
	 * @param awsPrivateKey the awsPrivateKey to set
	 */
	public void setAwsPrivateKey(String awsPrivateKey) {
		this.awsPrivateKey = awsPrivateKey;
	}

	/**
	 * @return the resourcesBucket
	 */
	public String getResourcesBucket() {
		return resourcesBucket;
	}
	
	/**
	 * @param resourcesBucket the resourcesBucket to set
	 */
	public void setResourcesBucket(String resourcesBucket) {
		this.resourcesBucket = resourcesBucket;
	}

	/**
	 * @return the resourcesPath
	 */
	public String getResourcesPath() {
		return resourcesPath;
	}
	
	/**
	 * @param resourcesPath the resourcesPath to set
	 */
	public void setResourcesPath(String resourcesPath) {
		this.resourcesPath = resourcesPath;
	}

}
