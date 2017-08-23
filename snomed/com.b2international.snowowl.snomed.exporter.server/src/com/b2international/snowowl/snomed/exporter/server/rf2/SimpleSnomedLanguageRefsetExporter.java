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
package com.b2international.snowowl.snomed.exporter.server.rf2;

import java.io.IOException;

import com.b2international.index.Hits;
import com.b2international.index.revision.RevisionSearcher;
import com.b2international.snowowl.snomed.datastore.index.entry.SnomedRefSetMemberIndexEntry;
import com.b2international.snowowl.snomed.exporter.server.SnomedExportContext;

/**
 * @since 5.10.12 
 */
public class SimpleSnomedLanguageRefsetExporter extends SnomedLanguageRefSetExporter {

	public SimpleSnomedLanguageRefsetExporter(SnomedExportContext exportContext, RevisionSearcher revisionSearcher, String languageCode) {
		super(exportContext, revisionSearcher, languageCode);
	}

	@Override
	protected Hits<SnomedRefSetMemberIndexEntry> filter(Hits<SnomedRefSetMemberIndexEntry> allResults) throws IOException {
		return allResults; // no filtering needed, assuming only one language code exists
	}
}
