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

import javax.validation.constraints.Min;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @since 5.10.10
 */
public class SnomedClassificationServiceConfiguration {

	@NotEmpty
	@JsonProperty(value = "url", required = false)
	private String url;
	
	@NotEmpty
	@JsonProperty(value = "userName", required = false)
	private String userName;
	
	@NotEmpty
	@JsonProperty(value = "password", required = false)
	private String password;

	@Min(1)
	@JsonProperty(value = "numberOfPollTries", required = false)
	private long numberOfPollTries = 10;
	
	@Min(1)
	@JsonProperty(value = "timeBetweenPollTries", required = false)
	private long timeBetweenPollTries = 1000;
	
	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * @param userName the userName to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the numberOfPollTries
	 */
	public long getNumberOfPollTries() {
		return numberOfPollTries;
	}

	/**
	 * @param numberOfPollTries the numberOfPollTries to set
	 */
	public void setNumberOfPollTries(long numberOfPollTries) {
		this.numberOfPollTries = numberOfPollTries;
	}

	/**
	 * @return the timeBetweenPollTries
	 */
	public long getTimeBetweenPollTries() {
		return timeBetweenPollTries;
	}

	/**
	 * @param timeBetweenPollTries the timeBetweenPollTries to set
	 */
	public void setTimeBetweenPollTries(long timeBetweenPollTries) {
		this.timeBetweenPollTries = timeBetweenPollTries;
	}
	
}
