/*
 * Copyright 2011-2019 B2i Healthcare Pte Ltd, http://b2i.sg
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
package com.b2international.snowowl.snomed.api.rest;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

import java.net.URI;
import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.async.DeferredResult;

import com.b2international.commons.StringUtils;
import com.b2international.commons.http.ExtendedLocale;
import com.b2international.snowowl.core.domain.IComponent;
import com.b2international.snowowl.core.domain.PageableCollectionResource;
import com.b2international.snowowl.core.request.SearchResourceRequest;
import com.b2international.snowowl.core.request.SearchResourceRequest.Sort;
import com.b2international.snowowl.core.request.SearchResourceRequest.SortField;
import com.b2international.snowowl.datastore.request.SearchIndexResourceRequest;
import com.b2international.snowowl.snomed.api.ISnomedExpressionService;
import com.b2international.snowowl.snomed.api.domain.expression.ISnomedExpression;
import com.b2international.snowowl.snomed.api.rest.domain.ChangeRequest;
import com.b2international.snowowl.snomed.api.rest.domain.RestApiError;
import com.b2international.snowowl.snomed.api.rest.domain.SnomedConceptRestInput;
import com.b2international.snowowl.snomed.api.rest.domain.SnomedConceptRestSearch;
import com.b2international.snowowl.snomed.api.rest.domain.SnomedConceptRestUpdate;
import com.b2international.snowowl.snomed.api.rest.util.DeferredResults;
import com.b2international.snowowl.snomed.api.rest.util.Responses;
import com.b2international.snowowl.snomed.common.SnomedRf2Headers;
import com.b2international.snowowl.snomed.core.domain.SnomedConcept;
import com.b2international.snowowl.snomed.core.domain.SnomedConcepts;
import com.b2international.snowowl.snomed.datastore.request.SnomedDescriptionSearchRequestBuilder;
import com.b2international.snowowl.snomed.datastore.request.SnomedRequests;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import springfox.documentation.annotations.ApiIgnore;

/**
 * @since 1.0
 */
@Api(value = "Concepts", description = "Concepts", tags = { "concepts" })
@Controller
@RequestMapping(produces={ AbstractRestService.SO_MEDIA_TYPE })
public class SnomedConceptRestService extends AbstractRestService {

	@Autowired
	private ISnomedExpressionService expressionService;

	public SnomedConceptRestService() {
		super(ImmutableSet.<String>builder()
				.addAll(SnomedConcept.Fields.ALL)
				.add("term") // special term based sort for concepts
				.build());
	}
	
	@ApiOperation(
			value="Retrieve Concepts from a branch", 
			notes="Returns a list with all/filtered Concepts from a branch."
					+ "<p>The following properties can be expanded:"
					+ "<p>"
					+ "&bull; pt() &ndash; the description representing the concept's preferred term in the given locale<br>"
					+ "&bull; fsn() &ndash; the description representing the concept's fully specified name in the given locale<br>"
					+ "&bull; descriptions() &ndash; the list of descriptions for the concept<br>")
	@ApiResponses({
		@ApiResponse(code = 200, message = "OK", response = PageableCollectionResource.class),
		@ApiResponse(code = 400, message = "Invalid search config", response = RestApiError.class),
		@ApiResponse(code = 404, message = "Branch not found", response = RestApiError.class)
	})
	@GetMapping(value="/{path:**}/concepts", produces={ AbstractRestService.SO_MEDIA_TYPE, AbstractRestService.TEXT_CSV_MEDIA_TYPE })
	public @ResponseBody DeferredResult<SnomedConcepts> searchByGet(
			
			@ApiParam(value="The branch path", required = true)
			@PathVariable(value="path")
			final String branch,

			@ApiParam(value="The Concept identifier(s) to match")
			@RequestParam(value="conceptIds", required=false) 
			final Set<String> conceptIds,
			
			@ApiParam(value="The effective time to match (yyyyMMdd, exact matches only)")
			@RequestParam(value="effectiveTime", required=false) 
			final String effectiveTimeFilter,
			
			
			@ApiParam(value="The concept status to match")
			@RequestParam(value="active", required=false) 
			final Boolean activeFilter,
			
			@ApiParam(value="The concept module identifier to match")
			@RequestParam(value="module", required=false) 
			final String moduleFilter,
			
			@ApiParam(value="The definition status to match")
			@RequestParam(value="definitionStatus", required=false) 
			final String definitionStatusFilter,
			
			@ApiParam(value="The namespace to match")
			@RequestParam(value="namespace", required=false) 
			final String namespaceFilter,
						
			
			@ApiParam(value="The ECL expression to match on the inferred form")
			@RequestParam(value="ecl", required=false) 
			final String eclFilter,
			
			@ApiParam(value="The ECL expression to match on the stated form")
			@RequestParam(value="statedEcl", required=false) 
			final String statedEclFilter,
			
			@ApiParam(value="The SNOMED CT Query expression to match (inferred form only)")
			@RequestParam(value="query", required=false) 
			final String queryFilter,
			
			
			@ApiParam(value="Description semantic tag(s) to match")
			@RequestParam(value="semanticTag", required=false)
			final String[] semanticTags,
			
			@ApiParam(value="The description term to match")
			@RequestParam(value="term", required=false) 
			final String termFilter,
			
			@ApiParam(value="Description type ECL expression to match")
			@RequestParam(value="descriptionType", required=false) 
			final String descriptionTypeFilter,
			
			@ApiParam(value="The description active state to match")
			@RequestParam(value="termActive", required=false) 
			final Boolean descriptionActiveFilter,

			
			@ApiParam(value = "The inferred parent(s) to match")
			@RequestParam(value="parent", required=false)
			final String[] parent,
			
			@ApiParam(value = "The inferred ancestor(s) to match")
			@RequestParam(value="ancestor", required=false)
			final String[] ancestor,
			
			@ApiParam(value = "The stated parent(s) to match")
			@RequestParam(value="statedParent", required=false)
			final String[] statedParent,
			
			@ApiParam(value = "The stated ancestor(s) to match")
			@RequestParam(value="statedAncestor", required=false)
			final String[] statedAncestor,
			
			
			@ApiParam(value="Expansion parameters")
			@RequestParam(value="expand", required=false)
			final String expand,
			
			@ApiParam(value="The scrollKeepAlive to start a scroll using this query")
			@RequestParam(value="scrollKeepAlive", required=false) 
			final String scrollKeepAlive,
			
			@ApiParam(value="A scrollId to continue scrolling a previous query")
			@RequestParam(value="scrollId", required=false) 
			final String scrollId,
			
			@ApiParam(value="The search key to use for retrieving the next page of results")
			@RequestParam(value="searchAfter", required=false) 
			final String searchAfter,

			@ApiParam(value = "Sort keys")
			@RequestParam(value="sort", required=false)
			final List<String> sort,
			
			@ApiParam(value="The maximum number of items to return")
			@RequestParam(value="limit", defaultValue="50", required=false) 
			final int limit,

			
			@ApiParam(value="Accepted language tags, in order of preference")
			@RequestHeader(value=HttpHeaders.ACCEPT_LANGUAGE, defaultValue="en-US;q=0.8,en-GB;q=0.6", required=false) 
			final String languageSetting,
			
			@ApiIgnore
			@RequestHeader(value=HttpHeaders.ACCEPT, required=false)
			final String contentType
			
		) {
		
		return doSearch(branch,
				conceptIds,
				effectiveTimeFilter,
				activeFilter,
				moduleFilter,
				definitionStatusFilter,
				namespaceFilter,
				eclFilter,
				statedEclFilter,
				queryFilter,
				semanticTags,
				termFilter,
				descriptionTypeFilter,
				descriptionActiveFilter,
				parent,
				ancestor,
				statedParent,
				statedAncestor,
				expand,
				scrollKeepAlive,
				scrollId,
				searchAfter,
				sort,
				limit,
				languageSetting,
				contentType);
	}
	
	@ApiOperation(
			value="Retrieve Concepts from a branch", 
			notes="Returns a list with all/filtered Concepts from a branch."
					+ "<p>The following properties can be expanded:"
					+ "<p>"
					+ "&bull; pt() &ndash; the description representing the concept's preferred term in the given locale<br>"
					+ "&bull; fsn() &ndash; the description representing the concept's fully specified name in the given locale<br>"
					+ "&bull; descriptions() &ndash; the list of descriptions for the concept<br>")
	@ApiResponses({
		@ApiResponse(code = 200, message = "OK", response = PageableCollectionResource.class),
		@ApiResponse(code = 400, message = "Invalid search config", response = RestApiError.class),
		@ApiResponse(code = 404, message = "Branch not found", response = RestApiError.class)
	})
	@PostMapping(value="/{path:**}/concepts/search", produces={ AbstractRestService.SO_MEDIA_TYPE, AbstractRestService.TEXT_CSV_MEDIA_TYPE })
	public @ResponseBody DeferredResult<SnomedConcepts> searchByPost(
			
			@ApiParam(value="The branch path", required = true)
			@PathVariable(value="path")
			final String branch,

			@RequestBody(required = false)
			final SnomedConceptRestSearch body,
			
			@ApiParam(value="Accepted language tags, in order of preference")
			@RequestHeader(value=HttpHeaders.ACCEPT_LANGUAGE, defaultValue="en-US;q=0.8,en-GB;q=0.6", required=false) 
			final String languageSetting,
			
			@ApiIgnore
			@RequestHeader(value=HttpHeaders.ACCEPT, defaultValue=AbstractRestService.SO_MEDIA_TYPE, required=false)
			final String contentType) {
		
		return doSearch(branch,
				body.getConceptIds(),
				body.getEffectiveTimeFilter(),
				body.getActiveFilter(),
				body.getModuleFilter(),
				body.getDefinitionStatusFilter(),
				body.getNamespaceFilter(),
				body.getEclFilter(),
				body.getStatedEclFilter(),
				body.getQueryFilter(),
				body.getSemanticTags(),
				body.getTermFilter(),
				body.getDescriptionTypeFilter(),
				body.getDescriptionActiveFilter(),
				body.getParent(),
				body.getAncestor(),
				body.getStatedParent(),
				body.getStatedAncestor(),
				body.getExpand(),
				body.getScrollKeepAlive(),
				body.getScrollId(),
				body.getSearchAfter(),
				body.getSort(),
				body.getLimit(),
				languageSetting,
				contentType);
	}

	private DeferredResult<SnomedConcepts> doSearch(
			final String branch,
			final Set<String> conceptIds,
			final String effectiveTimeFilter,
			final Boolean activeFilter,
			final String moduleFilter,
			final String definitionStatusFilter,
			final String namespaceFilter,
			final String eclFilter,
			final String statedEclFilter,
			final String queryFilter,
			final String[] semanticTags,
			final String termFilter,
			final String descriptionTypeFilter,
			final Boolean descriptionActiveFilter,
			final String[] parent,
			final String[] ancestor,
			final String[] statedParent,
			final String[] statedAncestor,
			final String expandParams,
			final String scrollKeepAlive,
			final String scrollId,
			final String searchAfter,
			final List<String> sort,
			final int limit,
			final String languageSetting,
			final String contentType) {
		
		final List<ExtendedLocale> extendedLocales = getExtendedLocales(languageSetting);

		String expand = expandParams;
		
		if (AbstractRestService.TEXT_CSV_MEDIA_TYPE.equals(contentType)) {
			
			if (!Strings.isNullOrEmpty(expand) && expand.contains("pt") && !expand.contains("descriptions()")) {
				expand = String.format("%s,descriptions()", expand);
			}
			
		}
		
		List<Sort> sorts = extractSortFields(sort, branch, extendedLocales);
		
		if (sorts.isEmpty()) {
			final SortField sortField = StringUtils.isEmpty(termFilter) 
					? SearchIndexResourceRequest.DOC_ID 
					: SearchIndexResourceRequest.SCORE;
			sorts = Collections.singletonList(sortField);
		}
		
		return DeferredResults.wrap(
				SnomedRequests
				.prepareSearchConcept()
				.setLimit(limit)
				.setScroll(scrollKeepAlive)
				.setScrollId(scrollId)
				.setSearchAfter(searchAfter)
				.filterByIds(conceptIds)
				.filterByEffectiveTime(effectiveTimeFilter)
				.filterByActive(activeFilter)
				.filterByModule(moduleFilter)
				.filterByDefinitionStatus(definitionStatusFilter)
				.filterByNamespace(namespaceFilter)
				.filterByParents(parent == null ? null : ImmutableSet.copyOf(parent))
				.filterByAncestors(ancestor == null ? null : ImmutableSet.copyOf(ancestor))
				.filterByStatedParents(statedParent == null ? null : ImmutableSet.copyOf(statedParent))
				.filterByStatedAncestors(statedAncestor == null ? null : ImmutableSet.copyOf(statedAncestor))
				.filterByEcl(eclFilter)
				.filterByStatedEcl(statedEclFilter)
				.filterByQuery(queryFilter)
				.filterByTerm(Strings.emptyToNull(termFilter))
				.filterByDescriptionLanguageRefSet(extendedLocales)
				.filterByDescriptionType(descriptionTypeFilter)
				.filterByDescriptionSemanticTags(semanticTags == null ? null : ImmutableSet.copyOf(semanticTags))
				.filterByDescriptionActive(descriptionActiveFilter)
				.setExpand(expand)
				.setLocales(extendedLocales)
				.sortBy(sorts)
				.build(repositoryId, branch)
				.execute(bus));
	}
	
	@ApiOperation(
			value="Retrieve Concept properties",
			notes="Returns all properties of the specified Concept, including a summary of inactivation indicator and association members."
					+ "<p>The following properties can be expanded:"
					+ "<p>"
					+ "&bull; pt() &ndash; the description representing the concept's preferred term in the given locale<br>"
					+ "&bull; fsn() &ndash; the description representing the concept's fully specified name in the given locale<br>"
					+ "&bull; descriptions() &ndash; the list of descriptions for the concept<br>"
					+ "&bull; ancestors(limit:50,direct:true,expand(pt(),...)) &ndash; the list of concept ancestors (parameter 'direct' is required)<br>"
					+ "&bull; descendants(limit:50,direct:true,expand(pt(),...)) &ndash; the list of concept descendants (parameter 'direct' is required)<br>")
	@ApiResponses({
		@ApiResponse(code = 200, message = "OK", response = Void.class),
		@ApiResponse(code = 404, message = "Branch or Concept not found", response = RestApiError.class)
	})
	@RequestMapping(value="/{path:**}/concepts/{conceptId}", method=RequestMethod.GET)
	public @ResponseBody DeferredResult<SnomedConcept> read(
			@ApiParam(value="The branch path")
			@PathVariable(value="path")
			final String branchPath,

			@ApiParam(value="The Concept identifier")
			@PathVariable(value="conceptId")
			final String conceptId,
			
			@ApiParam(value="Expansion parameters")
			@RequestParam(value="expand", required=false)
			final String expand,
			
			@ApiParam(value="Accepted language tags, in order of preference")
			@RequestHeader(value=HttpHeaders.ACCEPT_LANGUAGE, defaultValue="en-US;q=0.8,en-GB;q=0.6", required=false) 
			final String acceptLanguage) {

		final List<ExtendedLocale> extendedLocales = getExtendedLocales(acceptLanguage);
		
		return DeferredResults.wrap(
				SnomedRequests
					.prepareGetConcept(conceptId)
					.setExpand(expand)
					.setLocales(extendedLocales)
					.build(repositoryId, branchPath)
					.execute(bus));
	}

	@ApiOperation(
			value="Retrieve authoring form of a concept",
			notes="Retrieve authoring form of a concept which includes proximal primitive super-types and all inferred relationships "
					+ "which are not of type 'Is a'.")
	@ApiResponses({
		@ApiResponse(code = 200, message = "OK", response = Void.class),
		@ApiResponse(code = 404, message = "Branch or Concept not found", response = RestApiError.class)
	})
	@RequestMapping(value="/{path:**}/concepts/{conceptId}/authoring-form", method=RequestMethod.GET)
	public @ResponseBody ISnomedExpression readShortNormalForm(
			@ApiParam(value="The branch path")
			@PathVariable(value="path")
			final String branchPath,

			@ApiParam(value="The Concept identifier")
			@PathVariable(value="conceptId")
			final String conceptId,
			
			@ApiParam(value="Accepted language tags, in order of preference")
			@RequestHeader(value=HttpHeaders.ACCEPT_LANGUAGE, defaultValue="en-US;q=0.8,en-GB;q=0.6", required=false) 
			final String acceptLanguage) {

		final List<ExtendedLocale> extendedLocales = getExtendedLocales(acceptLanguage);
		
		return expressionService.getConceptAuthoringForm(conceptId, branchPath, extendedLocales);
	}

	@ApiOperation(
			value="Create Concept", 
			notes="Creates a new Concept directly on a branch.")
	@ApiResponses({
		@ApiResponse(code = 201, message = "Concept created on task"),
		@ApiResponse(code = 404, message = "Branch not found", response = RestApiError.class)
	})
	@RequestMapping(
			value="/{path:**}/concepts", 
			method=RequestMethod.POST, 
			consumes={ AbstractRestService.SO_MEDIA_TYPE, MediaType.APPLICATION_JSON_VALUE })
	@ResponseStatus(HttpStatus.CREATED)
	public ResponseEntity<Void> create(
			@ApiParam(value="The branch path")
			@PathVariable(value="path")
			final String branchPath,

			@ApiParam(value="Concept parameters")
			@RequestBody 
			final ChangeRequest<SnomedConceptRestInput> body,

			final Principal principal) {
		
		final String userId = principal.getName();
		
		final SnomedConceptRestInput change = body.getChange();
		
		final String commitComment = body.getCommitComment();
		final String defaultModuleId = body.getDefaultModuleId();
		
		final String createdConceptId = change.toRequestBuilder()
			.build(repositoryId, branchPath, userId, commitComment, defaultModuleId)
			.execute(bus)
			.getSync(COMMIT_TIMEOUT, TimeUnit.MILLISECONDS)
			.getResultAs(String.class);
		
		
		return Responses.created(getConceptLocationURI(branchPath, createdConceptId)).build();
	}

	@ApiOperation(
			value="Update Concept",
			notes="Updates properties of the specified Concept, also managing inactivation indicator and association reference set "
					+ "membership in case of inactivation."
					+ "<p>The following properties are allowed to change:"
					+ "<p>"
					+ "&bull; module identifier<br>"
					+ "&bull; subclass definition status<br>"
					+ "&bull; definition status<br>"
					+ "&bull; associated Concepts<br>"
					+ ""
					+ "<p>The following properties, when changed, will trigger inactivation:"
					+ "<p>"
					+ "&bull; inactivation indicator<br>")
	@ApiResponses({
		@ApiResponse(code = 204, message = "Update successful"),
		@ApiResponse(code = 404, message = "Branch or Concept not found", response = RestApiError.class)
	})
	@RequestMapping(
			value="/{path:**}/concepts/{conceptId}/updates", 
			method=RequestMethod.POST,
			consumes={ AbstractRestService.SO_MEDIA_TYPE, MediaType.APPLICATION_JSON_VALUE })
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void update(			
			@ApiParam(value="The branch path")
			@PathVariable(value="path")
			final String branchPath,

			@ApiParam(value="The Concept identifier")
			@PathVariable(value="conceptId")
			final String conceptId,
			
			@ApiParam(value="Updated Concept parameters")
			@RequestBody 
			final ChangeRequest<SnomedConceptRestUpdate> body,

			final Principal principal) {

		final String userId = principal.getName();
		
		final String commitComment = body.getCommitComment();
		final String defaultModuleId = body.getDefaultModuleId();

		body.getChange().toRequestBuilder(conceptId)
			.build(repositoryId, branchPath, userId, commitComment, defaultModuleId)
			.execute(bus)
			.getSync(COMMIT_TIMEOUT, TimeUnit.MILLISECONDS);
	}

	@ApiOperation(
			value="Delete Concept",
			notes="Permanently removes the specified unreleased Concept and related components.<p>If any participating "
					+ "component has already been released the Concept can not be removed and a <code>409</code> "
					+ "status will be returned."
					+ "<p>The force flag enables the deletion of a released Concept. "
					+ "Deleting published components is against the RF2 history policy so"
					+ " this should only be used to remove a new component from a release before the release is published.</p>")
	@ApiResponses({
		@ApiResponse(code = 204, message = "Deletion successful"),
		@ApiResponse(code = 404, message = "Branch or Concept not found", response = RestApiError.class),
		@ApiResponse(code = 409, message = "Cannot be deleted if released", response = RestApiError.class)
	})
	@RequestMapping(value="/{path:**}/concepts/{conceptId}", method=RequestMethod.DELETE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(			
			@ApiParam(value="The branch path")
			@PathVariable(value="path")
			final String branchPath,

			@ApiParam(value="The Concept identifier")
			@PathVariable(value="conceptId")
			final String conceptId,

			@ApiParam(value="Force deletion flag")
			@RequestParam(defaultValue="false", required=false)
			final Boolean force,

			final Principal principal) {
		SnomedRequests
			.prepareDeleteConcept(conceptId)
			.force(force)
			.build(repositoryId, branchPath, principal.getName(), String.format("Deleted Concept '%s' from store.", conceptId))
			.execute(bus)
			.getSync(COMMIT_TIMEOUT, TimeUnit.MILLISECONDS);
	}
	
	private URI getConceptLocationURI(String branchPath, String conceptId) {
		return linkTo(SnomedConceptRestService.class).slash(branchPath).slash("concepts").slash(conceptId).toUri();
	}
	
	@Override
	protected Sort toSort(String field, boolean ascending, String branch, List<ExtendedLocale> extendedLocales) {
		switch (field) {
		case SnomedRf2Headers.FIELD_TERM:
			return toTermSort(field, ascending, branch, extendedLocales);
		}
		return super.toSort(field, ascending, branch, extendedLocales);
	}

	private Sort toTermSort(String field, boolean ascending, String branchPath, List<ExtendedLocale> extendedLocales) {
		final Set<String> synonymIds = SnomedRequests.prepareGetSynonyms()
			.setFields(SnomedConcept.Fields.ID)
			.build(repositoryId, branchPath)
			.execute(bus)
			.getSync()
			.getItems()
			.stream()
			.map(IComponent::getId)
			.collect(Collectors.toSet());
	
		final Map<String, Object> args = ImmutableMap.of("locales", SnomedDescriptionSearchRequestBuilder.getLanguageRefSetIds(extendedLocales), "synonymIds", synonymIds);
		return SearchResourceRequest.SortScript.of("termSort", args, ascending);
	}
	
}
