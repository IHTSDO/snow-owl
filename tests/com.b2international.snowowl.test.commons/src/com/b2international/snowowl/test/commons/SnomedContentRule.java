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
package com.b2international.snowowl.test.commons;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;

import org.junit.rules.ExternalResource;

import com.b2international.commons.ConsoleProgressMonitor;
import com.b2international.commons.platform.PlatformUtil;
import com.b2international.snowowl.api.impl.codesystem.domain.CodeSystem;
import com.b2international.snowowl.core.ApplicationContext;
import com.b2international.snowowl.core.api.IBranchPath;
import com.b2international.snowowl.datastore.BranchPathUtils;
import com.b2international.snowowl.eventbus.IEventBus;
import com.b2international.snowowl.snomed.common.ContentSubType;
import com.b2international.snowowl.snomed.datastore.SnomedDatastoreActivator;
import com.b2international.snowowl.snomed.datastore.request.SnomedRequests;
import com.b2international.snowowl.snomed.importer.rf2.util.ImportUtil;

/**
 * JUnit test rule to import SNOMED CT content during automated tests.
 * 
 * @since 3.6
 */
public class SnomedContentRule extends ExternalResource {

	private final String branchPath;
	private final CodeSystem codeSystem;
	private final File importArchive;
	private final ContentSubType contentType;

	public SnomedContentRule(final CodeSystem codeSystem, final String importArchivePath, final ContentSubType contentType) {
		this(IBranchPath.MAIN_BRANCH, codeSystem, importArchivePath, contentType);
	}
	
	public SnomedContentRule(final String branchPath, final CodeSystem codeSystem, final String importArchivePath, final ContentSubType contentType) {
		this.branchPath = checkNotNull(branchPath, "branchPath");
		this.codeSystem = checkNotNull(codeSystem, "codeSystem");
		this.contentType = checkNotNull(contentType, "contentType");
		this.importArchive = new File(PlatformUtil.toAbsolutePathBundleEntry(SnomedContentRule.class, importArchivePath));
	}

	@Override
	protected void before() throws Throwable {
		checkBranch();
		new ImportUtil().doImport(codeSystem, "info@b2international.com", contentType, branchPath, importArchive, true,
				new ConsoleProgressMonitor());
	}

	private void checkBranch() {
		if (!IBranchPath.MAIN_BRANCH.equals(branchPath) && !BranchPathUtils.exists(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)) {
			final IBranchPath parentBranchPath = BranchPathUtils.createPath(branchPath.substring(0, branchPath.lastIndexOf('/')));
			final IEventBus eventBus = ApplicationContext.getServiceForClass(IEventBus.class);
			
			SnomedRequests.branching().prepareCreate()
				.setParent(parentBranchPath.getPath())
				.setName(codeSystem.getShortName())
				.build(SnomedDatastoreActivator.REPOSITORY_UUID)
				.execute(eventBus)
				.getSync();
		}
	}

}
