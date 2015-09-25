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
package com.b2international.snowowl.snomed.datastore.index;

import static com.b2international.snowowl.datastore.index.IndexUtils.getBooleanValue;
import static com.b2international.snowowl.snomed.datastore.browser.SnomedIndexBrowserConstants.COMPONENT_RELEASED;
import static com.b2international.snowowl.snomed.datastore.browser.SnomedIndexBrowserConstants.RELATIONSHIP_DESTINATION_NEGATED;
import static com.b2international.snowowl.snomed.datastore.browser.SnomedIndexBrowserConstants.RELATIONSHIP_GROUP;
import static com.b2international.snowowl.snomed.datastore.browser.SnomedIndexBrowserConstants.RELATIONSHIP_INFERRED;
import static com.b2international.snowowl.snomed.datastore.browser.SnomedIndexBrowserConstants.RELATIONSHIP_OBJECT_ID;
import static com.b2international.snowowl.snomed.datastore.browser.SnomedIndexBrowserConstants.RELATIONSHIP_UNION_GROUP;
import static com.b2international.snowowl.snomed.datastore.browser.SnomedIndexBrowserConstants.RELATIONSHIP_UNIVERSAL;
import static com.b2international.snowowl.snomed.datastore.browser.SnomedIndexBrowserConstants.RELATIONSHIP_VALUE_ID;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;

import com.b2international.commons.StringUtils;
import com.b2international.snowowl.core.api.IBranchPath;
import com.b2international.snowowl.core.date.EffectiveTimes;
import com.b2international.snowowl.datastore.index.IndexQueryBuilder;
import com.b2international.snowowl.datastore.index.IndexUtils;
import com.b2international.snowowl.datastore.index.QueryDslIndexQueryAdapter;
import com.b2international.snowowl.datastore.index.mapping.LongIndexField;
import com.b2international.snowowl.datastore.index.mapping.Mappings;
import com.b2international.snowowl.snomed.datastore.SnomedRelationshipIndexEntry;
import com.b2international.snowowl.snomed.datastore.browser.SnomedIndexBrowserConstants;
import com.b2international.snowowl.snomed.datastore.index.mapping.SnomedMappings;
import com.google.common.base.Optional;

/**
 * Query adapter for SNOMED CT relationships.
 * 
 */
public class SnomedRelationshipIndexQueryAdapter extends QueryDslIndexQueryAdapter<SnomedRelationshipIndexEntry> {

	private static final long serialVersionUID = -3776571469734573429L;

	public static final int SEARCH_SOURCE_ID = 1 << 0;
	public static final int SEARCH_DESTINATION_ID = 1 << 1;
	public static final int SEARCH_RELATIONSHIP_ID = 1 << 2;
	public static final int SEARCH_ACTIVE_RELATIONSHIPS_ONLY = 1 << 3;
	public static final int SEARCH_STORAGE_KEY = 1 << 4;
	
	public static SnomedRelationshipIndexQueryAdapter findBySourceId(final String conceptId) {
		return new SnomedRelationshipIndexQueryAdapter(conceptId, SEARCH_SOURCE_ID);
	}
	
	public static SnomedRelationshipIndexQueryAdapter findByDestinationId(final String conceptId) {
		return new SnomedRelationshipIndexQueryAdapter(conceptId, SEARCH_DESTINATION_ID);
	}
	
	public static SnomedRelationshipIndexQueryAdapter findByRelationshipId(final String relationshipId) {
		return new SnomedRelationshipIndexQueryAdapter(relationshipId, SEARCH_RELATIONSHIP_ID);
	}
	
	public static SnomedRelationshipIndexQueryAdapter findByStorageKey(final long storageKey) {
		return new SnomedRelationshipIndexQueryAdapter(String.valueOf(storageKey), SEARCH_STORAGE_KEY);
	}

	public SnomedRelationshipIndexQueryAdapter(final String queryString, final int searchFlags) {
		super(queryString, checkFlags(searchFlags, SEARCH_SOURCE_ID, SEARCH_DESTINATION_ID, SEARCH_RELATIONSHIP_ID, SEARCH_ACTIVE_RELATIONSHIPS_ONLY, SEARCH_STORAGE_KEY), null); // TODO: component id filtering
	}
	
	@Override
	public SnomedRelationshipIndexEntry buildSearchResult(final Document doc, final IBranchPath branchPath, final float score) {
		
		final String id = SnomedMappings.id().getValueAsString(doc);
		final String objectId = doc.get(RELATIONSHIP_OBJECT_ID);
		final String attributeId = SnomedMappings.relationshipType().getValueAsString(doc);
		final String valueId = doc.get(RELATIONSHIP_VALUE_ID);
		final String characteristicTypeId = SnomedMappings.relationshipCharacteristicType().getValueAsString(doc);
		byte group = (byte) doc.getField(RELATIONSHIP_GROUP).numericValue().intValue();
		byte unionGroup = (byte) doc.getField(RELATIONSHIP_UNION_GROUP).numericValue().intValue();
		byte flags = SnomedRelationshipIndexEntry.generateFlags(getBooleanValue(doc.getField(COMPONENT_RELEASED)), 
				SnomedMappings.active().getValue(doc) == 1, 
				getBooleanValue(doc.getField(RELATIONSHIP_INFERRED)),
				getBooleanValue(doc.getField(RELATIONSHIP_UNIVERSAL)),
				getBooleanValue(doc.getField(RELATIONSHIP_DESTINATION_NEGATED)));
		final String moduleId = SnomedMappings.module().getValueAsString(doc);
		final long storageKey = Mappings.storageKey().getValue(doc);
		// FIXME: remove null check
		final IndexableField effectiveTimeField = doc.getField(SnomedIndexBrowserConstants.RELATIONSHIP_EFFECTIVE_TIME);
		final long effectiveTime = (null == effectiveTimeField) ? EffectiveTimes.UNSET_EFFECTIVE_TIME : IndexUtils.getLongValue(effectiveTimeField);
		return new SnomedRelationshipIndexEntry(id, objectId, attributeId, valueId, characteristicTypeId, storageKey, moduleId, group, unionGroup, flags, effectiveTime);
	}
	
	@Override
	protected IndexQueryBuilder createIndexQueryBuilder() {
		Optional<Long> parsedSearchStringOptional = IndexUtils.parseLong(searchString);
		final IndexQueryBuilder activeRelationshipsQuery = new IndexQueryBuilder()
			.requireIf(anyFlagSet(SEARCH_ACTIVE_RELATIONSHIPS_ONLY), SnomedMappings.newQuery().active().matchAll())
			.requireIf(StringUtils.isEmpty(searchString), SnomedMappings.newQuery().relationship().matchAll());
		if (parsedSearchStringOptional.isPresent()) {
			final Long id = parsedSearchStringOptional.get();
			return activeRelationshipsQuery
			.finishIf(StringUtils.isEmpty(searchString))
			.requireExactTermIf(anyFlagSet(SEARCH_SOURCE_ID), RELATIONSHIP_OBJECT_ID, LongIndexField._toBytesRef(id))
			.requireExactTermIf(anyFlagSet(SEARCH_DESTINATION_ID), RELATIONSHIP_VALUE_ID, LongIndexField._toBytesRef(id))
			.requireIf(anyFlagSet(SEARCH_STORAGE_KEY), SnomedMappings.newQuery().storageKey(id).matchAll())
			.requireIf(anyFlagSet(SEARCH_RELATIONSHIP_ID), SnomedMappings.newQuery().id(id).matchAll());
		} else {
			// TODO: this query adapter only searches by IDs, what to return here?
			return activeRelationshipsQuery;
		}
	}
}
