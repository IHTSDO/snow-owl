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

import java.util.Date;

import com.b2international.snowowl.snomed.api.domain.classification.ClassificationStatus;


/**
 * @since 5.10.13
 */
public class ExternalClassificationStatus {

	private String classificationId;
	private String branch;
	private String reasonerId;
	private Date created;
	private String previousRelease;
	private ClassificationStatus status;
	private String errorMessage;
	private String developerMessage;
	
	public String getClassificationId() {
		return classificationId;
	}
	
	public void setClassificationId(String classificationId) {
		this.classificationId = classificationId;
	}
	
	public String getBranch() {
		return branch;
	}
	
	public void setBranch(String branch) {
		this.branch = branch;
	}
	
	public String getReasonerId() {
		return reasonerId;
	}
	
	public void setReasonerId(String reasonerId) {
		this.reasonerId = reasonerId;
	}
	
	public Date getCreated() {
		return created;
	}
	
	public void setCreated(Date created) {
		this.created = created;
	}
	
	public String getPreviousRelease() {
		return previousRelease;
	}
	
	public void setPreviousRelease(String previousRelease) {
		this.previousRelease = previousRelease;
	}
	
	public ClassificationStatus getStatus() {
		return status;
	}
	
	public void setStatus(ClassificationStatus status) {
		this.status = status;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}
	
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	public String getDeveloperMessage() {
		return developerMessage;
	}
	
	public void setDeveloperMessage(String developerMessage) {
		this.developerMessage = developerMessage;
	}
	
}
