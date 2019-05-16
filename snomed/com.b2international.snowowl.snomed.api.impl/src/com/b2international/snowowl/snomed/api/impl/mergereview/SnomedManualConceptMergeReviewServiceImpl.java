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
package com.b2international.snowowl.snomed.api.impl.mergereview;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.annotation.Resource;

import com.b2international.commons.FileUtils;
import com.b2international.snowowl.core.SnowOwlApplication;
import com.b2international.snowowl.core.api.SnowowlRuntimeException;
import com.b2international.snowowl.core.exceptions.NotFoundException;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserConcept;
import com.b2international.snowowl.snomed.api.impl.domain.browser.SnomedBrowserConcept;
import com.b2international.snowowl.snomed.api.mergereview.ISnomedManualConceptMergeReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @since 4.5
 */
public class SnomedManualConceptMergeReviewServiceImpl implements ISnomedManualConceptMergeReviewService {

	@Resource
	private ObjectMapper objectMapper;

	private static final String FILE_TYPE = ".json";

	@Override
	public boolean exists(final String branchPath, final String mergeReviewId, final String conceptId) {
		return Files.exists(getConceptStorePath(branchPath, mergeReviewId, conceptId));
	}

	@Override
	public void storeChanges(final String branchPath, final String mergeReviewId, final ISnomedBrowserConcept conceptUpdate) {
		try {

			final Path conceptFile = getConceptStorePath(branchPath, mergeReviewId, conceptUpdate.getConceptId());

			if (!Files.exists(conceptFile)) {
				Files.createDirectories(conceptFile.getParent());
			}

			getObjectMapper().writeValue(conceptFile.toFile(), conceptUpdate);

		} catch (final IOException e) {
			throw new SnowowlRuntimeException("Failed to persist manual concept merge.", e);
		}
	}

	@Override
	public ISnomedBrowserConcept retrieve(final String branchPath, final String mergeReviewId, final String conceptId) {

		try {

			final Path conceptFile = getConceptStorePath(branchPath, mergeReviewId, conceptId);

			if (!Files.exists(conceptFile)) {
				throw new NotFoundException("Manual concept merge review", String.format("%s (concept ID: %s)", mergeReviewId, conceptId));
			}

			return getObjectMapper().readValue(conceptFile.toFile(), SnomedBrowserConcept.class);

		} catch (final IOException e) {
			throw new SnowowlRuntimeException(e);
		}
	}

	@Override
	public boolean deleteAll(final String branchPath, final String mergeReviewId) {
		return FileUtils.deleteDirectory(getConceptStorePath(branchPath, mergeReviewId, "1").getParent().toFile());
	}

	private Path getConceptStorePath(final String branchPath, final String mergeReviewId, final String conceptId) {
		return Paths.get(getMergeReviewStorePath(), branchPath, mergeReviewId, conceptId + FILE_TYPE);
	}

	private String getMergeReviewStorePath() {
		return SnowOwlApplication.INSTANCE.getEnviroment().getMergeReviewStoreDirectory().getAbsolutePath();
	}

	private ObjectMapper getObjectMapper() {
		return objectMapper;
	}

}
