/*
 * Copyright 2011-2015 B2i Healthcare Pte Ltd, http://b2i.sg
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
package com.b2international.snowowl.snomed.exporter.server.sandbox;

import static com.b2international.snowowl.snomed.common.SnomedTerminologyComponentConstants.DESCRIPTION_NUMBER;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Set;

import org.apache.lucene.document.Document;

import com.b2international.snowowl.snomed.SnomedConstants;
import com.b2international.snowowl.snomed.common.SnomedRf2Headers;
import com.b2international.snowowl.snomed.datastore.index.mapping.SnomedMappings;
import com.b2international.snowowl.snomed.exporter.server.ComponentExportType;
import com.b2international.snowowl.snomed.exporter.server.SnomedRfFileNameBuilder;

/**
 * RF2 export implementation for SNOMED&nbsp;CT description.
 *
 */
public class SnomedDescriptionExporter extends SnomedCoreExporter implements SnomedFileSwitchingExporter {

	public SnomedDescriptionExporter(final SnomedExportConfiguration configuration) {
		super(checkNotNull(configuration, "configuration"));
	}

	@Override
	public Set<String> getFieldsToLoad() {
		return SnomedMappings.fieldsToLoad()
				.id()
				.effectiveTime()
				.active()
				.module()
				.descriptionConcept()
				.descriptionLanguageCode()
				.descriptionType()
				.descriptionTerm()
				.descriptionCaseSignificance()
				.build();
	}

	@Override
	public String transform(final Document doc) {
		final StringBuilder sb = new StringBuilder();
		sb.append(SnomedMappings.id().getValueAsString(doc));
		sb.append(HT);
		sb.append(formatEffectiveTime(SnomedMappings.effectiveTime().getValue(doc)));
		sb.append(HT);
		sb.append(SnomedMappings.active().getValue(doc));
		sb.append(HT);
		sb.append(SnomedMappings.module().getValueAsString(doc));
		sb.append(HT);
		sb.append(SnomedMappings.descriptionConcept().getValueAsString(doc));
		sb.append(HT);
		sb.append(SnomedMappings.descriptionLanguageCode().getValueAsString(doc));
		sb.append(HT);
		sb.append(SnomedMappings.descriptionType().getValueAsString(doc));
		sb.append(HT);
		sb.append(SnomedMappings.descriptionTerm().getValue(doc));
		sb.append(HT);
		sb.append(SnomedMappings.descriptionCaseSignificance().getValueAsString(doc));
		return sb.toString();
	}
	
	@Override
	public String getFileName(String[] rows) {
		SnomedExportConfiguration configuration = getConfiguration();
		String type = SnomedConstants.Concepts.TEXT_DEFINITION.equals(rows[6]) ? "TextDefinition" : "Description";
		return new StringBuilder("sct2_")
		.append(type)
		.append("_")
		.append(String.valueOf(configuration.getContentSubType()))
		.append("-")
		.append(rows[5])// languageCode
		.append("_")
		.append(configuration.getClientNamespace())
		.append("_")
		.append(SnomedRfFileNameBuilder.getReleaseDate(configuration))
		.append(".txt")
		.toString();
	}

	@Override
	public ComponentExportType getType() {
		return ComponentExportType.DESCRIPTION;
	}

	@Override
	public String[] getColumnHeaders() {
		return SnomedRf2Headers.DESCRIPTION_HEADER;
	}
	
	@Override
	protected int getTerminologyComponentType() {
		return DESCRIPTION_NUMBER;
	}
}