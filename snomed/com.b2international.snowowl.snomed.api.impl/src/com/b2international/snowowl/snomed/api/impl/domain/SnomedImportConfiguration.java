/*
 * Copyright 2011-2016 B2i Healthcare Pte Ltd, http://b2i.sg
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
package com.b2international.snowowl.snomed.api.impl.domain;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Date;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import com.b2international.snowowl.core.date.EffectiveTimes;
import com.b2international.snowowl.snomed.core.domain.ISnomedImportConfiguration;
import com.b2international.snowowl.snomed.core.domain.Rf2ReleaseType;

/**
 * Implementation of a {@link ISnomedImportConfiguration SNOMED&nbsp;CT import configuration}.
 */
public class SnomedImportConfiguration implements ISnomedImportConfiguration {

	@NotNull
	private final Rf2ReleaseType rf2ReleaseType;
	
	@NotEmpty
	private final String branchPath;
	
	@NotNull
	private final boolean createVersion;
	
	private boolean releasePatch;
	
	private Date patchReleaseVersion;
	
	private ImportStatus importStatus = ImportStatus.WAITING_FOR_FILE;
	private Date startDate;
	private Date completionDate;
	
	@NotEmpty
	private final String codeSystemShortName;
	
	/**
	 * Creates a new import configuration instance.
	 * @param rf2ReleaseType the RF2 release type.
	 * @param branchPath the branch path where the import has to be performed.
	 * @param languageRefSetId the language reference set identifier concept ID for the preferred language. 
	 * @param createVersion boolean indicating whether a new version has to be created for each individual 
	 * effective times. Has no effect if the RF2 release type in *NOT* full.
	 */
	public SnomedImportConfiguration(final Rf2ReleaseType rf2ReleaseType, final String branchPath, final boolean createVersion,
			final String codeSystemShortName) {
		this.rf2ReleaseType = rf2ReleaseType;
		this.branchPath = branchPath;
		this.createVersion = createVersion;
		this.codeSystemShortName = codeSystemShortName;
	}

	public static ISnomedImportConfiguration newReleasePatchConfiguration(final String branchPath, String patchReleaseVersion, String shortName) {
		final SnomedImportConfiguration configuration = new SnomedImportConfiguration(Rf2ReleaseType.DELTA, branchPath, false, shortName);
		configuration.releasePatch = true;
		checkNotNull(patchReleaseVersion, "patchReleaseVersion");
		configuration.patchReleaseVersion = EffectiveTimes.parse(patchReleaseVersion);
		return configuration;
	}
	
	@Override
	public Rf2ReleaseType getRf2ReleaseType() {
		return rf2ReleaseType;
	}

	@Override
	public String getBranchPath() {
		return branchPath;
	}
	
	@Override
	public boolean shouldCreateVersion() {
		return createVersion;
	}

	@Override
	public ImportStatus getStatus() {
		return importStatus;
	}
	
	@Override
	public Date getStartDate() {
		return startDate;
	}
	
	@Override
	public Date getCompletionDate() {
		return completionDate;
	}
	
	@Override
	public boolean isReleasePatch() {
		return releasePatch;
	}
	
	@Override
	public Date getPatchReleaseVersion() {
		return patchReleaseVersion;
	}
	
	@Override
	public String getCodeSystemShortName() {
		return codeSystemShortName;
	}
	
	/**
	 * Sets the status to the desired value.
	 * @param importStatus the import status to set.
	 */
	public void setStatus(final ImportStatus importStatus) {
		if (ImportStatus.RUNNING == importStatus) {
			this.startDate = new Date();
		} else if (ImportStatus.COMPLETED == importStatus || ImportStatus.FAILED == importStatus) {
			this.completionDate = new Date();
		}
		this.importStatus = checkNotNull(importStatus, "importStatus");
	}

}
