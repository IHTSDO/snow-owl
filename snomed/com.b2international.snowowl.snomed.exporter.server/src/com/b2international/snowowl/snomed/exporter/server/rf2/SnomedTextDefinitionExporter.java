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
package com.b2international.snowowl.snomed.exporter.server.rf2;

import com.b2international.index.query.Expressions.ExpressionBuilder;
import com.b2international.index.revision.RevisionSearcher;
import com.b2international.snowowl.snomed.SnomedConstants.Concepts;
import com.b2international.snowowl.snomed.datastore.index.entry.SnomedDescriptionIndexEntry;
import com.b2international.snowowl.snomed.exporter.server.ComponentExportType;
import com.b2international.snowowl.snomed.exporter.server.SnomedExportContext;

/**
 *
 */
public class SnomedTextDefinitionExporter extends SnomedRf2DescriptionExporter {

	public SnomedTextDefinitionExporter(SnomedExportContext configuration, RevisionSearcher revisionSearcher, boolean unpublished) {
		super(configuration, revisionSearcher, unpublished);
	}
	
	@Override
	public ComponentExportType getType() {
		return ComponentExportType.TEXT_DEFINITION;
	}
	
	@Override
	protected void appendExpressionConstraint(ExpressionBuilder builder) {
		builder.must(SnomedDescriptionIndexEntry.Expressions.type(Concepts.TEXT_DEFINITION));
	}

}
