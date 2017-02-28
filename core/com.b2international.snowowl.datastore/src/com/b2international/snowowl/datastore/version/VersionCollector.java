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
package com.b2international.snowowl.datastore.version;

import static com.b2international.snowowl.core.ApplicationContext.getServiceForClass;
import static com.b2international.snowowl.datastore.CodeSystemUtils.getRepositoryUuid;
import static com.b2international.snowowl.datastore.ICodeSystemVersion.VERSION_IMPORT_DATE_COMPARATOR;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.reverseOrder;
import static java.util.Collections.sort;

import java.util.Collection;
import java.util.List;

import com.b2international.snowowl.core.events.Request;
import com.b2international.snowowl.datastore.CodeSystemService;
import com.b2international.snowowl.datastore.ICodeSystemVersion;
import com.google.common.base.Preconditions;

/**
 * Version collector implementation.
 * 
 * @deprecated - use {@link Request} API instead.
 */
public class VersionCollector implements IVersionCollector {

	private final String toolingId;

	/**Creates a existing version collector service for the given tooling.*/
	public VersionCollector(final String toolingId) {
		this.toolingId = Preconditions.checkNotNull(toolingId);
	}
	
	@Override
	public Collection<ICodeSystemVersion> getVersions() {
		final String repositoryUuid = getRepositoryUuid(toolingId);
		final Collection<ICodeSystemVersion> versions = getServiceForClass(CodeSystemService.class).getAllTagsDecorateWithPatched(repositoryUuid);
		final List<ICodeSystemVersion> $ = newArrayList(versions);
		sort($, reverseOrder(VERSION_IMPORT_DATE_COMPARATOR));
		return $;
		
	}
	
}