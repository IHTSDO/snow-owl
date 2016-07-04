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
package com.b2international.snowowl.snomed.datastore.server.request;

import static com.google.common.collect.Maps.newHashMap;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.queries.BooleanFilter;
import org.apache.lucene.queries.CustomScoreQuery;
import org.apache.lucene.queries.function.FunctionQuery;
import org.apache.lucene.queries.function.FunctionValues;
import org.apache.lucene.queries.function.valuesource.LongFieldSource;
import org.apache.lucene.queries.function.valuesource.SimpleFloatFunction;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;

import com.b2international.commons.functions.StringToLongFunction;
import com.b2international.commons.options.Options;
import com.b2international.commons.pcj.LongSets;
import com.b2international.snowowl.core.domain.BranchContext;
import com.b2international.snowowl.core.exceptions.IllegalQueryParameterException;
import com.b2international.snowowl.core.terminology.ComponentCategory;
import com.b2international.snowowl.snomed.core.domain.ISnomedDescription;
import com.b2international.snowowl.snomed.core.domain.SnomedConcepts;
import com.b2international.snowowl.snomed.datastore.escg.EscgParseFailedException;
import com.b2international.snowowl.snomed.datastore.escg.EscgRewriter;
import com.b2international.snowowl.snomed.datastore.escg.IEscgQueryEvaluatorService;
import com.b2international.snowowl.snomed.datastore.escg.IndexQueryQueryEvaluator;
import com.b2international.snowowl.snomed.datastore.id.SnomedIdentifiers;
import com.b2international.snowowl.snomed.datastore.index.entry.SnomedConceptIndexEntry;
import com.b2international.snowowl.snomed.datastore.index.mapping.SnomedMappings;
import com.b2international.snowowl.snomed.datastore.index.mapping.SnomedQueryBuilder;
import com.b2international.snowowl.snomed.datastore.server.converter.SnomedConverters;
import com.b2international.snowowl.snomed.dsl.query.SyntaxErrorException;
import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Ints;

import bak.pcj.LongCollection;

/**
 * @since 4.5
 */
final class SnomedConceptSearchRequest extends SnomedSearchRequest<SnomedConcepts> {

	enum OptionKey {

		/**
		 * Description term to (smart) match
		 */
		TERM,

		/**
		 * ESCG expression to match
		 */
		ESCG,

		/**
		 * Namespace part of concept ID to match (?)
		 */
		NAMESPACE,
		
		/**
		 * Parent concept ID
		 */
		PARENT,
		
		/**
		 * Ancestor concept ID (includes direct parents)
		 */
		ANCESTOR
	}
	
	SnomedConceptSearchRequest() {}

	@Override
	protected SnomedConcepts doExecute(BranchContext context) throws IOException {
		final IndexSearcher searcher = context.service(IndexSearcher.class);

		final SnomedQueryBuilder queryBuilder = SnomedMappings.newQuery().concept();
		addActiveClause(queryBuilder);
		addModuleClause(queryBuilder);
		
		if (containsKey(OptionKey.PARENT)) {
			queryBuilder.parent(getString(OptionKey.PARENT));
		}
		
		if (containsKey(OptionKey.ANCESTOR)) {
			final String ancestorId = getString(OptionKey.ANCESTOR);
			queryBuilder.and(SnomedMappings.newQuery()
					.parent(ancestorId)
					.ancestor(ancestorId)
					.matchAny());
		}
		
		final BooleanFilter filter = new BooleanFilter();
		Sort sort;
		Query query;
		
		if (containsKey(OptionKey.ESCG)) {
			/* 
			 * XXX: Not using IEscgQueryEvaluatorService, as it would add the equivalent of 
			 * active() and concept() to escgQuery, which is not needed.
			 */
			final String escg = getString(OptionKey.ESCG);
			try {
				final IndexQueryQueryEvaluator queryEvaluator = new IndexQueryQueryEvaluator();
				final BooleanQuery escgQuery = queryEvaluator.evaluate(context.service(EscgRewriter.class).parseRewrite(escg));
				queryBuilder.and(escgQuery);
			} catch (final SyntaxErrorException e) {
				throw new IllegalQueryParameterException(e.getMessage());
			} catch (EscgParseFailedException e) {
				final LongCollection matchingConceptIds = context.service(IEscgQueryEvaluatorService.class).evaluateConceptIds(context.branch().branchPath(), escg);
				addFilterClause(filter, SnomedMappings.id().createTermsFilter(LongSets.toSet(matchingConceptIds)), Occur.MUST);
			}
		}
		
		
		if (!componentIds().isEmpty()) {
			addFilterClause(filter, createComponentIdFilter(), Occur.MUST);
		}
		
		if (containsKey(OptionKey.TERM)) {
			final String term = getString(OptionKey.TERM);
			final Map<String, Integer> conceptScoreMap = executeDescriptionSearch(context, term);
			
			try {
				final ComponentCategory category = SnomedIdentifiers.getComponentCategory(term);
				if (category == ComponentCategory.CONCEPT) {
					if (conceptScoreMap.isEmpty()) {
						conceptScoreMap.put(term, 1);
					} else {
						conceptScoreMap.put(term, Ints.max(Ints.toArray(conceptScoreMap.values())) + 1);
					}
				}
			} catch (IllegalArgumentException e) {
				// ignored
			}
			
			if (conceptScoreMap.isEmpty()) {
				return new SnomedConcepts(offset(), limit(), 0);
			}
			
			addFilterClause(filter, SnomedMappings.id().createTermsFilter(StringToLongFunction.copyOf(conceptScoreMap.keySet())), Occur.MUST); 
			final FunctionQuery functionQuery = new FunctionQuery(new SimpleFloatFunction(new LongFieldSource(SnomedMappings.id().fieldName())) {
				
				@Override
				protected String name() {
					return "ConceptScoreMap";
				}
				
				@Override
				protected float func(int doc, FunctionValues vals) {
					final String conceptId = Long.toString(vals.longVal(doc));
					if (conceptScoreMap.containsKey(conceptId)) {
						return conceptScoreMap.get(conceptId);
					} else {
						return 0.0f;
					}
				}
			});
			
			query = new CustomScoreQuery(createFilteredQuery(queryBuilder.matchAll(), filter), functionQuery);
			sort = Sort.RELEVANCE;
		} else {
			query = createConstantScoreQuery(createFilteredQuery(queryBuilder.matchAll(), filter));
			sort = Sort.INDEXORDER;
		}
		
		final int totalHits = getTotalHits(searcher, query);
		if (limit() < 1 || totalHits < 1) {
			return new SnomedConcepts(offset(), limit(), totalHits);
		}
		
		// TODO: control score tracking
		final TopDocs topDocs = searcher.search(query, null, numDocsToRetrieve(searcher, totalHits), sort, true, false);
		if (topDocs.scoreDocs.length < 1) {
			return new SnomedConcepts(offset(), limit(), topDocs.totalHits);
		}
		
		final ScoreDoc[] scoreDocs = topDocs.scoreDocs;
		final ImmutableList.Builder<SnomedConceptIndexEntry> conceptsBuilder = ImmutableList.builder();
		
		final Options expand = expand();
		for (int i = offset(); i < scoreDocs.length; i++) {
			Document doc = searcher.doc(scoreDocs[i].doc); // TODO: should expand & filter drive fieldsToLoad? Pass custom fieldValueLoader?
			SnomedConceptIndexEntry indexEntry = SnomedConceptIndexEntry.builder(doc).build();
			conceptsBuilder.add(indexEntry);
		}
		return SnomedConverters.newConceptConverter(context, expand, locales()).convert(conceptsBuilder.build(), offset(), limit(), topDocs.totalHits);
	}

	private Map<String, Integer> executeDescriptionSearch(BranchContext context, String term) {
		final Collection<ISnomedDescription> items = SnomedRequests.prepareSearchDescription()
			.all()
			.filterByActive(true)
			.filterByTerm(term)
			.filterByLanguageRefSetIds(languageRefSetIds())
			.filterByConceptId(StringToLongFunction.copyOf(componentIds()))
			.build()
			.execute(context)
			.getItems();

		final Map<String, Integer> conceptMap = newHashMap();
		int i = items.size();
		
		for (ISnomedDescription description : items) {
			if (!conceptMap.containsKey(description.getConceptId())) {
				conceptMap.put(description.getConceptId(), i);
			}
			
			i--;
		}

		return conceptMap;
	}

	@Override
	protected Class<SnomedConcepts> getReturnType() {
		return SnomedConcepts.class;
	}
}
