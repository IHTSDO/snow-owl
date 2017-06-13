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
package com.b2international.snowowl.snomed.datastore.request;

import static com.b2international.snowowl.datastore.index.RevisionDocument.Expressions.id;
import static com.b2international.snowowl.snomed.datastore.index.entry.SnomedDescriptionIndexEntry.Expressions.acceptableIn;
import static com.b2international.snowowl.snomed.datastore.index.entry.SnomedDescriptionIndexEntry.Expressions.allTermPrefixesPresent;
import static com.b2international.snowowl.snomed.datastore.index.entry.SnomedDescriptionIndexEntry.Expressions.allTermsPresent;
import static com.b2international.snowowl.snomed.datastore.index.entry.SnomedDescriptionIndexEntry.Expressions.exactTerm;
import static com.b2international.snowowl.snomed.datastore.index.entry.SnomedDescriptionIndexEntry.Expressions.fuzzy;
import static com.b2international.snowowl.snomed.datastore.index.entry.SnomedDescriptionIndexEntry.Expressions.languageCodes;
import static com.b2international.snowowl.snomed.datastore.index.entry.SnomedDescriptionIndexEntry.Expressions.parsedTerm;
import static com.b2international.snowowl.snomed.datastore.index.entry.SnomedDescriptionIndexEntry.Expressions.preferredIn;
import static com.google.common.collect.Lists.newArrayList;

import java.io.IOException;
import java.util.List;

import com.b2international.index.Hits;
import com.b2international.index.query.Expression;
import com.b2international.index.query.Expressions;
import com.b2international.index.query.Expressions.ExpressionBuilder;
import com.b2international.index.revision.RevisionSearcher;
import com.b2international.snowowl.core.domain.BranchContext;
import com.b2international.snowowl.core.exceptions.BadRequestException;
import com.b2international.snowowl.core.terminology.ComponentCategory;
import com.b2international.snowowl.datastore.index.RevisionDocument;
import com.b2international.snowowl.snomed.core.domain.Acceptability;
import com.b2international.snowowl.snomed.core.domain.SnomedDescriptions;
import com.b2international.snowowl.snomed.datastore.converter.SnomedConverters;
import com.b2international.snowowl.snomed.datastore.id.SnomedIdentifiers;
import com.b2international.snowowl.snomed.datastore.index.entry.SnomedDescriptionIndexEntry;

/**
 * @since 4.5
 */
final class SnomedDescriptionSearchRequest extends SnomedComponentSearchRequest<SnomedDescriptions> {

	enum OptionKey {
		EXACT_TERM,
		TERM,
		CONCEPT,
		TYPE,
		ACCEPTABILITY,
		LANGUAGE,
		USE_FUZZY,
		PARSED_TERM;
	}
	
	SnomedDescriptionSearchRequest() {}

	@Override
	protected SnomedDescriptions doExecute(BranchContext context) throws IOException {
		final RevisionSearcher searcher = context.service(RevisionSearcher.class);
		if (containsKey(OptionKey.TERM) && getString(OptionKey.TERM).length() < 2) {
			throw new BadRequestException("Description term must be at least 2 characters long.");
		}

		final ExpressionBuilder queryBuilder = Expressions.builder();
		// Add (presumably) most selective filters first
		addActiveClause(queryBuilder);
		addIdFilter(queryBuilder, RevisionDocument.Expressions::ids);
		addLocaleFilter(context, queryBuilder);
		addModuleClause(queryBuilder);
		addNamespaceFilter(queryBuilder);
		addEffectiveTimeClause(queryBuilder);
		addActiveMemberOfClause(queryBuilder);
		addLanguageFilter(queryBuilder);
		addActiveMemberOfClause(queryBuilder);
		addEclFilter(context, queryBuilder, OptionKey.CONCEPT, SnomedDescriptionIndexEntry.Expressions::concepts);
		addEclFilter(context, queryBuilder, OptionKey.TYPE, SnomedDescriptionIndexEntry.Expressions::types);
		
		if (containsKey(OptionKey.TERM)) {
			final String searchTerm = getString(OptionKey.TERM);
			if (!containsKey(OptionKey.USE_FUZZY)) {
				queryBuilder.must(toDescriptionTermQuery(searchTerm));
			} else {
				queryBuilder.must(fuzzy(searchTerm));
			}
		}
		
		if (containsKey(OptionKey.EXACT_TERM)) {
			queryBuilder.must(exactTerm(getString(OptionKey.EXACT_TERM)));
		}

		final Hits<SnomedDescriptionIndexEntry> hits = searcher.search(select(SnomedDescriptionIndexEntry.class)
				.where(queryBuilder.build())
				.offset(offset())
				.limit(limit())
				.sortBy(sortBy())
				.withScores(containsKey(OptionKey.TERM))
				.build());
		if (limit() < 1 || hits.getTotal() < 1) {
			return new SnomedDescriptions(offset(), limit(), hits.getTotal());
		} else {
			return SnomedConverters.newDescriptionConverter(context, expand(), locales()).convert(hits.getHits(), offset(), limit(), hits.getTotal());
		}
	}
	
	@Override
	protected SnomedDescriptions createEmptyResult(int offset, int limit) {
		return new SnomedDescriptions(offset, limit, 0);
	}
	
	private Expression toDescriptionTermQuery(final String searchTerm) {
		final ExpressionBuilder qb = Expressions.builder();
		qb.should(createTermDisjunctionQuery(searchTerm));
		
		if (containsKey(OptionKey.PARSED_TERM)) {
			qb.should(parsedTerm(searchTerm));
		}
		
		if (isComponentId(searchTerm, ComponentCategory.DESCRIPTION)) {
			qb.should(Expressions.boost(id(searchTerm), 1000f));
		}
		
		return qb.build();
	}
	
	private boolean isComponentId(String value, ComponentCategory type) {
		try {
			return SnomedIdentifiers.getComponentCategory(value) == type;
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	private Expression createTermDisjunctionQuery(final String searchTerm) {
		final List<Expression> disjuncts = newArrayList();
		disjuncts.add(exactTerm(searchTerm));
		disjuncts.add(allTermsPresent(searchTerm));
		disjuncts.add(allTermPrefixesPresent(searchTerm));
		return Expressions.dismax(disjuncts);
	}

	private void addLanguageFilter(ExpressionBuilder queryBuilder) {
		if (containsKey(OptionKey.LANGUAGE)) {
			queryBuilder.filter(languageCodes(getCollection(OptionKey.LANGUAGE, String.class)));
		}
	}

	private void addLocaleFilter(BranchContext context, ExpressionBuilder queryBuilder) {
		if (languageRefSetIds().isEmpty()) {
			return;
		}
		
		ExpressionBuilder languageRefSetExpression = Expressions.builder();
		
		for (String languageRefSetId : languageRefSetIds()) {
			if (containsKey(OptionKey.ACCEPTABILITY)) {
				final Acceptability acceptability = get(OptionKey.ACCEPTABILITY, Acceptability.class);
				final Expression acceptabilityExpression = Acceptability.PREFERRED.equals(acceptability) 
						? preferredIn(languageRefSetId) 
						: acceptableIn(languageRefSetId);

				languageRefSetExpression.should(acceptabilityExpression);
			} else {
				languageRefSetExpression.should(preferredIn(languageRefSetId));
				languageRefSetExpression.should(acceptableIn(languageRefSetId));
			}
		}
		
		queryBuilder.filter(languageRefSetExpression.build());
	}
}
