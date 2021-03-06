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
package com.b2international.snowowl.core.merge;

import static com.google.common.collect.Lists.newArrayList;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;

import com.b2international.snowowl.core.exceptions.ApiError;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * @since 4.6
 */
@JsonDeserialize(builder=Merge.Builder.class)
public interface Merge extends Serializable {

	enum Status {
		SCHEDULED,
		IN_PROGRESS,
		COMPLETED,
		CONFLICTS,
		FAILED, 
		CANCEL_REQUESTED;
	}

	/** 
	 * Address template for the global event bus to which final Merge statuses will be sent.
	 * <p>
	 * The first template parameter is the repository ID ("snomedStore"), while the second is the
	 * merge UUID.
	 */
	public static final String ADDRESS_TEMPLATE = "/merges/%s/%s";
	
	UUID getId();
	
	String getSource();

	String getTarget();

	Status getStatus();
	
	Date getScheduledDate();
	
	Date getStartDate();
	
	Date getEndDate();
	
	ApiError getApiError();
	
	Collection<MergeConflict> getConflicts();

	Merge start();

	Merge completed();

	Merge failed(ApiError newApiError);

	Merge failedWithConflicts(Collection<MergeConflict> newConflicts, ApiError newApiError);

	Merge cancelRequested();

	@JsonPOJOBuilder(buildMethodName="build", withPrefix = "")
	static class Builder {

		private final String source;
		private final String target;
		
		private UUID id = UUID.randomUUID();
		private Status status = Status.SCHEDULED;
		private Date scheduledDate = new Date();
		private Date startDate;
		private Date endDate;
		private ApiError apiError;
		private Collection<MergeConflict> conflicts = newArrayList();

		@JsonCreator
		public Builder(@JsonProperty("source") String source, @JsonProperty("target") String target) { 
			this.source = source;
			this.target = target;
		}

		public Builder id(UUID id) {
			this.id = id;
			return this;
		}

		public Builder status(Status status) {
			this.status = status;
			return this;
		}

		public Builder scheduledDate(Date scheduledDate) {
			this.scheduledDate = scheduledDate;
			return this;
		}

		public Builder startDate(Date startDate) {
			this.startDate = startDate;
			return this;
		}

		public Builder endDate(Date endDate) {
			this.endDate = endDate;
			return this;
		}

		public Builder apiError(ApiError apiError) {
			this.apiError = apiError;
			return this;
		}

		public Builder conflicts(Collection<MergeConflict> conflicts) {
			this.conflicts.addAll(conflicts);
			return this;
		}

		public Merge build() {
			return new MergeImpl(id, source, target, status, scheduledDate, startDate, endDate, apiError, conflicts);
		}
	}
}
