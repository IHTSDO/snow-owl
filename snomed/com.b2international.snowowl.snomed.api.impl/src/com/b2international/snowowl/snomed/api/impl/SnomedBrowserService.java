/*
 * Copyright 2011-2017 B2i Healthcare Pte Ltd, http://b2i.sg
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.b2international.snowowl.snomed.api.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.b2international.commons.CompareUtils;
import com.b2international.commons.http.ExtendedLocale;
import com.b2international.commons.options.Options;
import com.b2international.snowowl.core.ApplicationContext;
import com.b2international.snowowl.core.branch.Branch;
import com.b2international.snowowl.core.domain.IComponent;
import com.b2international.snowowl.core.domain.TransactionContext;
import com.b2international.snowowl.core.events.bulk.BulkRequest;
import com.b2international.snowowl.core.events.bulk.BulkRequestBuilder;
import com.b2international.snowowl.core.events.bulk.BulkResponse;
import com.b2international.snowowl.core.exceptions.ComponentNotFoundException;
import com.b2international.snowowl.core.terminology.ComponentCategory;
import com.b2international.snowowl.datastore.request.RepositoryCommitRequestBuilder;
import com.b2international.snowowl.datastore.request.RepositoryRequests;
import com.b2international.snowowl.datastore.request.SearchIndexResourceRequest;
import com.b2international.snowowl.eventbus.IEventBus;
import com.b2international.snowowl.snomed.SnomedConstants.Concepts;
import com.b2international.snowowl.snomed.api.browser.ISnomedBrowserAxiomService;
import com.b2international.snowowl.snomed.api.browser.ISnomedBrowserService;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserAxiom;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserBulkChangeRun;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserChildConcept;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserConcept;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserConstant;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserDescription;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserDescriptionResult;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserParentConcept;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserRelationship;
import com.b2international.snowowl.snomed.api.domain.browser.SnomedBrowserBulkChangeStatus;
import com.b2international.snowowl.snomed.api.domain.browser.SnomedBrowserDescriptionResults;
import com.b2international.snowowl.snomed.api.domain.browser.SnomedBrowserDescriptionType;
import com.b2international.snowowl.snomed.api.impl.domain.InputFactory;
import com.b2international.snowowl.snomed.api.impl.domain.browser.SnomedBrowserAxiom;
import com.b2international.snowowl.snomed.api.impl.domain.browser.SnomedBrowserBulkChangeRun;
import com.b2international.snowowl.snomed.api.impl.domain.browser.SnomedBrowserChildConcept;
import com.b2international.snowowl.snomed.api.impl.domain.browser.SnomedBrowserConcept;
import com.b2international.snowowl.snomed.api.impl.domain.browser.SnomedBrowserConstant;
import com.b2international.snowowl.snomed.api.impl.domain.browser.SnomedBrowserDescription;
import com.b2international.snowowl.snomed.api.impl.domain.browser.SnomedBrowserDescriptionResult;
import com.b2international.snowowl.snomed.api.impl.domain.browser.SnomedBrowserDescriptionResultDetails;
import com.b2international.snowowl.snomed.api.impl.domain.browser.SnomedBrowserParentConcept;
import com.b2international.snowowl.snomed.api.impl.domain.browser.SnomedBrowserRelationship;
import com.b2international.snowowl.snomed.api.impl.domain.browser.SnomedBrowserRelationshipTarget;
import com.b2international.snowowl.snomed.api.impl.domain.browser.SnomedBrowserRelationshipType;
import com.b2international.snowowl.snomed.api.impl.domain.browser.SnomedBrowserTerm;
import com.b2international.snowowl.snomed.core.domain.CaseSignificance;
import com.b2international.snowowl.snomed.core.domain.CharacteristicType;
import com.b2international.snowowl.snomed.core.domain.ConceptEnum;
import com.b2international.snowowl.snomed.core.domain.DefinitionStatus;
import com.b2international.snowowl.snomed.core.domain.RelationshipModifier;
import com.b2international.snowowl.snomed.core.domain.SnomedComponent;
import com.b2international.snowowl.snomed.core.domain.SnomedConcept;
import com.b2international.snowowl.snomed.core.domain.SnomedConcepts;
import com.b2international.snowowl.snomed.core.domain.SnomedDescription;
import com.b2international.snowowl.snomed.core.domain.SnomedDescriptions;
import com.b2international.snowowl.snomed.core.domain.SnomedRelationship;
import com.b2international.snowowl.snomed.datastore.SnomedDatastoreActivator;
import com.b2international.snowowl.snomed.datastore.id.SnomedIdentifiers;
import com.b2international.snowowl.snomed.datastore.request.SnomedConceptCreateRequest;
import com.b2international.snowowl.snomed.datastore.request.SnomedConceptUpdateRequest;
import com.b2international.snowowl.snomed.datastore.request.SnomedDescriptionCreateRequest;
import com.b2international.snowowl.snomed.datastore.request.SnomedDescriptionUpdateRequest;
import com.b2international.snowowl.snomed.datastore.request.SnomedRefSetMemberCreateRequest;
import com.b2international.snowowl.snomed.datastore.request.SnomedRefSetMemberUpdateRequest;
import com.b2international.snowowl.snomed.datastore.request.SnomedRelationshipCreateRequest;
import com.b2international.snowowl.snomed.datastore.request.SnomedRelationshipUpdateRequest;
import com.b2international.snowowl.snomed.datastore.request.SnomedRequests;
import com.google.common.base.Optional;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Ints;

public class SnomedBrowserService implements ISnomedBrowserService {

	private static final Logger LOGGER = LoggerFactory.getLogger(SnomedBrowserService.class);
	
	private static final List<ConceptEnum> CONCEPT_ENUMS = ImmutableList.<ConceptEnum>builder()
				.add(DefinitionStatus.values())
				.add(CharacteristicType.values())
				.add(CaseSignificance.values())
				.add(SnomedBrowserDescriptionType.values())
				.add(RelationshipModifier.values())
				.build();
	
	private static final String CONCEPT_ID_PLACEHOLDER = "$";

	private final Cache<String, SnomedBrowserBulkChangeRun> bulkChangeRuns = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.DAYS).build();
	
	@Override
	public ISnomedBrowserConcept getConceptDetails(final String branchPath, final String conceptId, final List<ExtendedLocale> extendedLocales) {
		return getConceptDetailsInBulk(branchPath, Collections.singleton(conceptId), extendedLocales).iterator().next();
	}
	
	@Override
	public Set<ISnomedBrowserConcept> getConceptDetailsInBulk(final String branchPath, final Set<String> conceptIds, final List<ExtendedLocale> extendedLocales) {
		
		if (conceptIds.isEmpty()) {
			return Collections.emptySet();
		}
		
		SnomedConcepts concepts = SnomedRequests.prepareSearchConcept()
			.setLimit(conceptIds.size())
			.filterByIds(conceptIds)
			.setLocales(extendedLocales)
			.setExpand("fsn(),pt(),inactivationProperties(),descriptions(limit:"+Integer.MAX_VALUE+",expand(inactivationProperties())),relationships(limit:"+Integer.MAX_VALUE+",expand(type(expand(fsn(),pt())),destination(expand(fsn(),pt()))))")
			.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
			.execute(getBus())
			.getSync();

		if (conceptIds.size() == 1) {
			
			Set<String> hitIds = concepts.getItems().stream().map(SnomedComponent::getId).collect(toSet());
			java.util.Optional<String> notFound = conceptIds.stream().filter(id -> !hitIds.contains(id)).findFirst();
			
			if (notFound.isPresent()) {
				throw new ComponentNotFoundException(ComponentCategory.CONCEPT, notFound.get());
			}
			
		}
		
		Set<ISnomedBrowserConcept> browserConcepts = concepts.getItems().stream()
			.map(concept -> {
				
				SnomedBrowserConcept browserConcept = new SnomedBrowserConcept();
				
				browserConcept.setConceptId(concept.getId());
				browserConcept.setActive(concept.isActive());
				browserConcept.setReleased(concept.isReleased());
				browserConcept.setEffectiveTime(concept.getEffectiveTime());
				browserConcept.setModuleId(concept.getModuleId());
				
				browserConcept.setDefinitionStatus(concept.getDefinitionStatus());
				
				browserConcept.setInactivationIndicator(concept.getInactivationIndicator());
				browserConcept.setAssociationTargets(concept.getAssociationTargets());
				
				browserConcept.setFsn(concept.getFsn() != null ? new SnomedBrowserTerm(concept.getFsn()) : new SnomedBrowserTerm(concept.getId()));
				browserConcept.setPreferredSynonym(concept.getPt() != null ? new SnomedBrowserTerm(concept.getPt().getTerm()) : new SnomedBrowserTerm(concept.getId()));
				
				browserConcept.setDescriptions(convertDescriptions(concept.getDescriptions()));
				browserConcept.setRelationships(convertRelationships(concept.getRelationships()));
				
				return browserConcept;
				
			})
			.collect(toSet());
		
		return getAxiomService().expandAxioms(browserConcepts, branchPath, extendedLocales).stream().collect(toSet());
	}
	
	@Override
	public ISnomedBrowserConcept createConcept(String branchPath, ISnomedBrowserConcept newConcept, String userId, List<ExtendedLocale> locales) {
		InputFactory inputFactory = new InputFactory(getBranch(branchPath));
		final SnomedConceptCreateRequest req = inputFactory.createComponentInput(newConcept, SnomedConceptCreateRequest.class);
		final String commitComment = getCommitComment(userId, newConcept, "creating");
		
		final String createdConceptId = SnomedRequests
				.prepareCommit()
				.setCommitComment(commitComment)
				.setBody(req)
				.setUserId(userId)
				.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
				.execute(getBus())
				.getSync()
				.getResultAs(String.class);
		
		return getConceptDetails(branchPath, createdConceptId, locales);
	}

	@Override
	public void updateConcept(String branch, List<? extends ISnomedBrowserConcept> updatedConcepts, String userId, List<ExtendedLocale> locales) {
		final String commitComment = userId + " Bulk update.";
		createBulkCommit(branch, updatedConcepts, false, userId, locales, commitComment)
			.build(SnomedDatastoreActivator.REPOSITORY_UUID, branch)
			.execute(getBus())
			.getSync();
		
		LOGGER.info("Committed bulk concept changes on {}", branch);
	}
	
	@Override
	public SnomedBrowserBulkChangeRun beginBulkChange(final String branch, final List<? extends ISnomedBrowserConcept> concepts, Boolean allowCreate, final String userId, final List<ExtendedLocale> locales) {
		
		final SnomedBrowserBulkChangeRun run = new SnomedBrowserBulkChangeRun();
		run.start();
	
		List<String> conceptIds = concepts.stream()
				.map(concept -> Strings.isNullOrEmpty(concept.getId()) ? CONCEPT_ID_PLACEHOLDER : concept.getId())
				.collect(toList());
		
		final String commitComment = userId + " Bulk update.";
		createBulkCommit(branch, concepts, allowCreate, userId, locales, commitComment)
			.build(SnomedDatastoreActivator.REPOSITORY_UUID, branch)
			.execute(getBus())
			.then(commitResult -> onSuccess(run, conceptIds, branch, commitResult.getResultAs(BulkResponse.class)))
			.fail(throwable -> onFailure(run, branch, throwable));
		
		bulkChangeRuns.put(run.getId(), run);
		
		return run;
	}

	@Override
	public ISnomedBrowserBulkChangeRun getBulkChange(String branch, String bulkId, List<ExtendedLocale> locales, Options expand) {
	
		SnomedBrowserBulkChangeRun run = bulkChangeRuns.getIfPresent(bulkId);
	
		if (run != null && run.getConceptIds() != null && expand.containsKey("concepts")) {
			
			if (CompareUtils.isEmpty(run.getConceptIds())) {
				run.setConcepts(Collections.emptyList());
			} else {
				
				LOGGER.info(">>> Collecting bulk concept create / update results on {}", branch);
				
				Map<String, ISnomedBrowserConcept> allConcepts = newHashMap();
				
				for (List<String> conceptIdPartitions : Lists.partition(run.getConceptIds(), 1000)) {
					Set<ISnomedBrowserConcept> concepts = getConceptDetailsInBulk(branch, ImmutableSet.copyOf(conceptIdPartitions), locales);
					allConcepts.putAll(concepts.stream().collect(toMap(ISnomedBrowserConcept::getId, c -> c)));
				}
				
				run.setConcepts(run.getConceptIds().stream().map(allConcepts::get).collect(toList())); // keep the order
				
				LOGGER.info("<<< Bulk concept create / update results are ready on {}", branch);
			}
			
		}
		
		return run;
	}

	@Override
	public List<ISnomedBrowserParentConcept> getConceptParents(final String branchPath, final String conceptId, final List<ExtendedLocale> locales, boolean isStatedForm, SnomedBrowserDescriptionType preferredDescriptionType) {
		final DescriptionService descriptionService = new DescriptionService(getBus(), branchPath);
	
		return new FsnJoinerOperation<ISnomedBrowserParentConcept>(conceptId, locales, descriptionService, preferredDescriptionType) {
			
			@Override
			protected Iterable<SnomedConcept> getConceptEntries(String conceptId) {
				SnomedConcept concept = SnomedRequests.prepareGetConcept(conceptId)
						.setExpand(isStatedForm ? "statedAncestors(direct:true)" : "ancestors(direct:true)")
						.setLocales(locales)
						.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
						.execute(getBus())
						.getSync();
				return isStatedForm ? concept.getStatedAncestors() : concept.getAncestors();
			}
	
			@Override
			protected ISnomedBrowserParentConcept convertConceptEntry(SnomedConcept conceptEntry, Optional<SnomedDescription> descriptionOptional) {
				final String childConceptId = conceptEntry.getId();
				final SnomedBrowserParentConcept convertedConcept = new SnomedBrowserParentConcept(); 
	
				convertedConcept.setConceptId(childConceptId);
				convertedConcept.setDefinitionStatus(conceptEntry.getDefinitionStatus());
				
				SnomedBrowserTerm term = descriptionOptional.transform(description -> new SnomedBrowserTerm(description)).or(new SnomedBrowserTerm(conceptEntry.getId()));
				if (preferredDescriptionType == SnomedBrowserDescriptionType.FSN) {
					convertedConcept.setFsn(term);
				} else if (preferredDescriptionType == SnomedBrowserDescriptionType.SYNONYM) {
					convertedConcept.setPreferredSynonym(term);
				}
				
				return convertedConcept;
			}
			
		}.run();
	}

	@Override
	public List<ISnomedBrowserChildConcept> getConceptChildren(
			String branchPath,
			String conceptId,
			List<ExtendedLocale> locales,
			boolean isStatedForm,
			SnomedBrowserDescriptionType preferredDescriptionType,
			int limit) {
		
		final DescriptionService descriptionService = new DescriptionService(getBus(), branchPath);
	
		return new FsnJoinerOperation<ISnomedBrowserChildConcept>(conceptId, locales, descriptionService, preferredDescriptionType) {
			
			@Override
			protected Iterable<SnomedConcept> getConceptEntries(String conceptId) {
				return SnomedRequests.prepareSearchConcept()
						.setLimit(limit)
						.setExpand("statedDescendants(direct:true,limit:0),descendants(direct:true,limit:0)")
						.filterByActive(true)
						.filterByParent(isStatedForm ? null : conceptId)
						.filterByStatedParent(isStatedForm ? conceptId : null)
						.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
						.execute(getBus())
						.getSync();
			}
	
			@Override
			protected ISnomedBrowserChildConcept convertConceptEntry(SnomedConcept conceptEntry, Optional<SnomedDescription> descriptionOptional) {
				
				final String childConceptId = conceptEntry.getId();
				final SnomedBrowserChildConcept convertedConcept = new SnomedBrowserChildConcept(); 
	
				convertedConcept.setConceptId(childConceptId);
				convertedConcept.setActive(conceptEntry.isActive());
				convertedConcept.setDefinitionStatus(conceptEntry.getDefinitionStatus());
				convertedConcept.setModuleId(conceptEntry.getModuleId());
				
				
				SnomedBrowserTerm term = descriptionOptional.transform(description -> new SnomedBrowserTerm(description)).or(new SnomedBrowserTerm(conceptEntry.getId()));
				if (preferredDescriptionType == SnomedBrowserDescriptionType.FSN) {
					convertedConcept.setFsn(term);
				} else if (preferredDescriptionType == SnomedBrowserDescriptionType.SYNONYM) {
					convertedConcept.setPreferredSynonym(term);
				}
				
				convertedConcept.setIsLeafStated(conceptEntry.getStatedDescendants().getTotal() == 0);
				convertedConcept.setIsLeafInferred(conceptEntry.getDescendants().getTotal() == 0);
	
				return convertedConcept;
			}
			
		}.run();
	}

	@Override
	public SnomedBrowserDescriptionResults getDescriptions(
			String branchPath,
			String query, List<ExtendedLocale> locales,
			SnomedBrowserDescriptionType preferredDescriptionType,
			String searchAfter,
			int limit) {
		
		checkNotNull(branchPath, "BranchPath may not be null.");
		checkNotNull(query, "Query may not be null.");
		checkArgument(query.length() >= 3, "Query must be at least 3 characters long.");
	
		final DescriptionService descriptionService = new DescriptionService(getBus(), branchPath);
		
		SnomedDescriptions snomedDescriptions = SnomedRequests.prepareSearchDescription()
			.setSearchAfter(searchAfter)
			.setLimit(limit)
			.filterByTerm(query)
			.filterByType(ImmutableSet.of(Concepts.FULLY_SPECIFIED_NAME, Concepts.SYNONYM))
			.sortBy(SearchIndexResourceRequest.SCORE)
			.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
			.execute(getBus())
			.getSync();
		
		final Collection<SnomedDescription> descriptions = snomedDescriptions.getItems();
		
		final List<SnomedDescription> sortedDescriptions = FluentIterable.from(descriptions)
			.toSortedList((d1, d2) -> {
				int result = d2.getScore().compareTo(d1.getScore());
				return result == 0 ? Ints.compare(d1.getTerm().length(), d2.getTerm().length()) : result;
			});
	
		final Set<String> conceptIds = FluentIterable.from(sortedDescriptions)
			.transform(description -> description.getConceptId())
			.toSet();
		
		final Iterable<SnomedConcept> concepts = getConcepts(branchPath, conceptIds);
		final Map<String, SnomedConcept> conceptMap = Maps.uniqueIndex(concepts, IComponent.ID_FUNCTION);
		
		final Map<String, SnomedDescription> descriptionByConceptIdMap; 
		switch (preferredDescriptionType) {
		case FSN:
			descriptionByConceptIdMap = descriptionService.getFullySpecifiedNames(conceptIds, locales);
			break;
		default:
			descriptionByConceptIdMap = descriptionService.getPreferredTerms(conceptIds, locales);
			break;
		}
		
		final Cache<String, SnomedBrowserDescriptionResultDetails> detailCache = CacheBuilder.newBuilder().build();
		final ImmutableList.Builder<ISnomedBrowserDescriptionResult> resultBuilder = ImmutableList.builder();
		
		for (final SnomedDescription description : sortedDescriptions) {
			
			final SnomedBrowserDescriptionResult descriptionResult = new SnomedBrowserDescriptionResult();
			descriptionResult.setActive(description.isActive());
			descriptionResult.setTerm(description.getTerm());
			
			try {
				final SnomedBrowserDescriptionResultDetails details = detailCache.get(description.getConceptId(), 
						new Callable<SnomedBrowserDescriptionResultDetails>() {
					
					@Override
					public SnomedBrowserDescriptionResultDetails call() throws Exception {
						final String conceptId = description.getConceptId();
						final SnomedConcept concept = conceptMap.get(conceptId);
						final SnomedBrowserDescriptionResultDetails details = new SnomedBrowserDescriptionResultDetails();
						
						if (concept != null) {
							details.setActive(concept.isActive());
							details.setConceptId(concept.getId());
							details.setDefinitionStatus(concept.getDefinitionStatus());
							details.setModuleId(concept.getModuleId());
							
							if (preferredDescriptionType == SnomedBrowserDescriptionType.FSN) {
								if (descriptionByConceptIdMap.containsKey(conceptId)) {
									details.setFsn(new SnomedBrowserTerm(descriptionByConceptIdMap.get(conceptId)));
								} else {
									details.setFsn(new SnomedBrowserTerm(conceptId));
								}
							} else {
								if (descriptionByConceptIdMap.containsKey(conceptId)) {
									details.setPreferredSynonym(new SnomedBrowserTerm(descriptionByConceptIdMap.get(conceptId)));
								} else {
									details.setPreferredSynonym(new SnomedBrowserTerm(conceptId));
								}
							}
							
						} else {
							LOGGER.warn("Concept {} not found in map, properties will not be set.", conceptId);
						}
						
						return details;
					}
				});
				
				descriptionResult.setConcept(details);
			} catch (ExecutionException e) {
				LOGGER.error("Exception thrown during computing details for concept {}, properties will not be set.", description.getConceptId(), e);
			}
			
			resultBuilder.add(descriptionResult);
		}
	
		return SnomedBrowserDescriptionResults.of(resultBuilder.build(), snomedDescriptions);
	}

	@Override
	public Map<String, ISnomedBrowserConstant> getConstants(final String branch, final List<ExtendedLocale> locales) {
		final ImmutableMap.Builder<String, ISnomedBrowserConstant> resultBuilder = ImmutableMap.builder();
		final DescriptionService descriptionService = new DescriptionService(getBus(), branch);
		
		for (final ConceptEnum conceptEnum : CONCEPT_ENUMS) {
			try {
				final String conceptId = conceptEnum.getConceptId();
	
				// Check if the corresponding concept exists
				SnomedRequests.prepareGetConcept(conceptId)
						.build(SnomedDatastoreActivator.REPOSITORY_UUID, branch)
						.execute(getBus())
						.getSync();
				
				final SnomedBrowserConstant constant = new SnomedBrowserConstant();
				constant.setConceptId(conceptId);
				
				final SnomedDescription fullySpecifiedName = descriptionService.getFullySpecifiedName(conceptId, locales);
				if (fullySpecifiedName != null) {
					constant.setFsn(new SnomedBrowserTerm(fullySpecifiedName));
				} else {
					constant.setFsn(new SnomedBrowserTerm(conceptId));
				}
				
				resultBuilder.put(conceptEnum.name(), constant);
			} catch (ComponentNotFoundException e) {
				// ignore
			}
		}
		
		return resultBuilder.build();
	}

	private RepositoryCommitRequestBuilder createBulkCommit(String branchPath, List<? extends ISnomedBrowserConcept> concepts, Boolean allowCreate, String userId, List<ExtendedLocale> locales, final String commitComment) {
		
		final Stopwatch watch = Stopwatch.createStarted();
		
		final BulkRequestBuilder<TransactionContext> bulkRequest = BulkRequest.create();

		InputFactory inputFactory = new InputFactory(getBranch(branchPath));

		// Process concepts in batches of 1000
		for (List<? extends ISnomedBrowserConcept> updatedConceptsBatch : Lists.partition(concepts, 1000)) {
			
			// Load existing versions in bulk
			Set<String> conceptIds = updatedConceptsBatch.stream()
					.map(ISnomedBrowserConcept::getConceptId)
					.filter(Objects::nonNull)
					.collect(Collectors.toSet());
			
			Set<ISnomedBrowserConcept> existingConcepts = getConceptDetailsInBulk(branchPath, conceptIds, locales);
			
			Map<String, ISnomedBrowserConcept> existingConceptsMap = existingConcepts.stream()
					.collect(Collectors.toMap(ISnomedBrowserConcept::getConceptId, concept -> concept));
			
			// For each concept add component updates to the bulk request
			for (ISnomedBrowserConcept concept : updatedConceptsBatch) {
				
				ISnomedBrowserConcept existingConcept = existingConceptsMap.get(concept.getConceptId());
				
				if (existingConcept == null) {
					
					if (allowCreate) {
						
						final SnomedConceptCreateRequest req = inputFactory.createComponentInput(concept, SnomedConceptCreateRequest.class);
						bulkRequest.add(req);
						
					} else {
						// If one existing concept is not found fail the whole commit 
						throw new ComponentNotFoundException("Snomed Concept", concept.getConceptId());
					}
					
				} else {
					update(concept, existingConcept, userId, locales, bulkRequest, inputFactory);
				}
				
			}
		}
		
		// Commit everything at once
		final RepositoryCommitRequestBuilder commit = SnomedRequests
			.prepareCommit()
			.setUserId(userId)
			.setCommitComment(commitComment)
			.setPreparationTime(watch.elapsed(TimeUnit.MILLISECONDS))
			.setBody(bulkRequest);

		return commit;
	}
	
	private Void onSuccess(SnomedBrowserBulkChangeRun run, List<String> conceptIds, String branch, BulkResponse response) {
		
		int currentIndex = 0;
		
		for (Object item : response.getItems()) {
			if (item instanceof String) { // ignore updates and deletions, catch create requests
				String id = (String) item;
				if (ComponentCategory.CONCEPT == SnomedIdentifiers.getComponentCategory(id)) {
					for (int i = currentIndex; i < conceptIds.size(); i++) {
						String originalId = conceptIds.get(i);
						if (CONCEPT_ID_PLACEHOLDER.equals(originalId)) {
							conceptIds.set(i, id);
							currentIndex = i + 1;
							break;
						}
					}
				}
			}
		}
		
		if (conceptIds.stream().anyMatch(id -> CONCEPT_ID_PLACEHOLDER.equals(id))) {
			throw new IllegalStateException("Unknown item in bulk response: " + response);
		}
		
		run.setConceptIds(conceptIds);
		run.end(SnomedBrowserBulkChangeStatus.COMPLETED);
		
		LOGGER.info("Committed bulk concept changes on {}", branch);
		
		return null;
	}
	
	private Void onFailure(SnomedBrowserBulkChangeRun run, String branch, final Throwable throwable) {
		run.end(SnomedBrowserBulkChangeStatus.FAILED);
		LOGGER.error("Bulk concept changes failed during commit on {}", branch, throwable);
		return null;
	}
	
	private void update(ISnomedBrowserConcept newVersionConcept, ISnomedBrowserConcept existingVersionConcept, String userId, List<ExtendedLocale> locales, 
			BulkRequestBuilder<TransactionContext> bulkRequest, InputFactory inputFactory) {
		
		LOGGER.info("Update concept start {}", newVersionConcept.getFsn());

		// Concept update
		final SnomedConceptUpdateRequest conceptUpdate = inputFactory.createComponentUpdate(existingVersionConcept, newVersionConcept, SnomedConceptUpdateRequest.class);
		
		final List<ISnomedBrowserDescription> existingVersionDescriptions = existingVersionConcept.getDescriptions();
		final List<ISnomedBrowserDescription> newVersionDescriptions = newVersionConcept.getDescriptions();
		
		// description delete
		Set<String> descriptionDeletionIds = inputFactory.getComponentDeletions(existingVersionDescriptions, newVersionDescriptions);
		// description update
		Map<String, SnomedDescriptionUpdateRequest> descriptionUpdates = inputFactory.createComponentUpdates(existingVersionDescriptions, newVersionDescriptions, SnomedDescriptionUpdateRequest.class);
		// description create
		List<SnomedDescriptionCreateRequest> descriptionInputs = inputFactory.createComponentInputs(newVersionDescriptions, SnomedDescriptionCreateRequest.class);
		descriptionInputs.forEach( requests -> requests.setConceptId(existingVersionConcept.getId()));
		
		LOGGER.info("Got description changes +{} -{} m{}, {}", descriptionInputs.size(), descriptionDeletionIds.size(), descriptionUpdates.size(), newVersionConcept.getFsn());

		final List<ISnomedBrowserRelationship> existingVersionRelationships = existingVersionConcept.getRelationships();
		final List<ISnomedBrowserRelationship> newVersionRelationships = newVersionConcept.getRelationships();
		
		// relationship delete
		Set<String> relationshipDeletionIds = inputFactory.getComponentDeletions(existingVersionRelationships, newVersionRelationships);
		// relationship update
		Map<String, SnomedRelationshipUpdateRequest> relationshipUpdates = inputFactory.createComponentUpdates(existingVersionRelationships, newVersionRelationships, SnomedRelationshipUpdateRequest.class);
		// relationship create
		List<SnomedRelationshipCreateRequest> relationshipInputs = inputFactory.createComponentInputs(newVersionRelationships, SnomedRelationshipCreateRequest.class);
		
		LOGGER.info("Got relationship changes +{} -{} m{}, {}", relationshipInputs.size(), relationshipDeletionIds.size(), relationshipUpdates.size(), newVersionConcept.getFsn());

		// additional axioms
		if (!existingVersionConcept.getClassAxioms().isEmpty() || !newVersionConcept.getClassAxioms().isEmpty()) {
			
			List<ISnomedBrowserAxiom> newAdditionalAxioms = newVersionConcept.getClassAxioms();
			List<ISnomedBrowserAxiom> existingAdditionalAxioms = existingVersionConcept.getClassAxioms();
			
			// additional axiom delete
			Set<String> additionalAxiomIdsToDelete = inputFactory.getComponentDeletions(existingAdditionalAxioms, newAdditionalAxioms);
			
			setOptionalProperties(newAdditionalAxioms, true, newVersionConcept.getModuleId(), existingVersionConcept.getConceptId());
			reuseInactiveIds(existingAdditionalAxioms, newAdditionalAxioms);
			
			// additional axiom create
			List<SnomedRefSetMemberCreateRequest> newAdditionalAxiomRequests = inputFactory.createComponentInputs(newAdditionalAxioms, SnomedRefSetMemberCreateRequest.class);
			
			// additional axiom update
			Map<String, SnomedRefSetMemberUpdateRequest> updateAdditionalAxiomRequests = inputFactory.createComponentUpdates(existingAdditionalAxioms, newAdditionalAxioms, SnomedRefSetMemberUpdateRequest.class);
			
			LOGGER.info("Got additional axiom changes +{} -{} m{}, {}", newAdditionalAxiomRequests.size(), additionalAxiomIdsToDelete.size(), updateAdditionalAxiomRequests.size(), newVersionConcept.getFsn());
			
			additionalAxiomIdsToDelete.stream().map(SnomedRequests::prepareDeleteMember).forEach(bulkRequest::add);
			newAdditionalAxiomRequests.forEach(bulkRequest::add);
			updateAdditionalAxiomRequests.keySet().forEach(id -> bulkRequest.add(updateAdditionalAxiomRequests.get(id)));
			
		}
		
		// gci axioms
		if (!existingVersionConcept.getGciAxioms().isEmpty() || !newVersionConcept.getGciAxioms().isEmpty()) {
			
			List<ISnomedBrowserAxiom> newGciAxioms = newVersionConcept.getGciAxioms();
			List<ISnomedBrowserAxiom> existingGciAxioms = existingVersionConcept.getGciAxioms();
			
			// gci axiom delete
			Set<String> gciAxiomIdsToDelete = inputFactory.getComponentDeletions(existingGciAxioms, newGciAxioms);
			
			setOptionalProperties(newGciAxioms, false, newVersionConcept.getModuleId(), existingVersionConcept.getConceptId());
			reuseInactiveIds(existingGciAxioms, newGciAxioms);
			
			// gci axiom create
			List<SnomedRefSetMemberCreateRequest> newGciAxiomRequests = inputFactory.createComponentInputs(newGciAxioms, SnomedRefSetMemberCreateRequest.class);
			
			// gci axiom update
			Map<String, SnomedRefSetMemberUpdateRequest> updateGciAxiomRequests = inputFactory.createComponentUpdates(existingGciAxioms, newGciAxioms, SnomedRefSetMemberUpdateRequest.class);
			
			LOGGER.info("Got GCI axiom changes +{} -{} m{}, {}", newGciAxiomRequests.size(), gciAxiomIdsToDelete.size(), updateGciAxiomRequests.size(), newVersionConcept.getFsn());
			
			gciAxiomIdsToDelete.stream().map(SnomedRequests::prepareDeleteMember).forEach(bulkRequest::add);
			newGciAxiomRequests.forEach(bulkRequest::add);
			updateGciAxiomRequests.keySet().forEach(id -> bulkRequest.add(updateGciAxiomRequests.get(id)));
			
		}
		
		// In the case of inactivation, other updates seem to go more smoothly if this is done later
		boolean conceptInactivation = existingVersionConcept.isActive() && !newVersionConcept.isActive();
		
		if (conceptUpdate != null && !conceptInactivation) {
			bulkRequest.add(conceptUpdate);
		}
		
		// descriptions
		descriptionUpdates.keySet().forEach(id -> bulkRequest.add(descriptionUpdates.get(id)));
		descriptionInputs.stream().forEach(bulkRequest::add);
		descriptionDeletionIds.stream().map(SnomedRequests::prepareDeleteDescription).forEach(bulkRequest::add);

		// relationships
		relationshipUpdates.keySet().forEach(id -> bulkRequest.add(relationshipUpdates.get(id)));
		relationshipInputs.forEach(bulkRequest::add);
		relationshipDeletionIds.stream().map(SnomedRequests::prepareDeleteRelationship).forEach(bulkRequest::add);
		
		// Inactivate concept last
		if (conceptUpdate != null && conceptInactivation) {
			bulkRequest.add(conceptUpdate);
		}
		
	}

	private void setOptionalProperties(List<ISnomedBrowserAxiom> axioms, boolean isNamedConceptOnLeft, String moduleId, String conceptId) {
		axioms.stream()
			.filter(SnomedBrowserAxiom.class::isInstance)
			.map(SnomedBrowserAxiom.class::cast)
			.forEach(axiom -> {
				axiom.setNamedConceptOnLeft(isNamedConceptOnLeft);
				axiom.setModuleId(moduleId);
				axiom.setReferencedComponentId(conceptId);
			});
	}

	private void reuseInactiveIds(List<ISnomedBrowserAxiom> existingAxioms, List<ISnomedBrowserAxiom> newAxioms) {
		Set<String> existingIds = existingAxioms.stream().map(ISnomedBrowserAxiom::getAxiomId).collect(toSet());
		Iterator<String> inactiveIds = existingAxioms.stream().filter(axiom -> !axiom.isActive()).map(ISnomedBrowserAxiom::getAxiomId).collect(toSet()).iterator();
		
		newAxioms.stream()
			.filter(axiom -> axiom.getAxiomId() != null && !existingIds.contains(axiom.getAxiomId())) 
			.forEach(axiom -> {
				SnomedBrowserAxiom browserAxiom = (SnomedBrowserAxiom) axiom;
				browserAxiom.setAxiomId(null);
				if (inactiveIds.hasNext()) {
					browserAxiom.setAxiomId(inactiveIds.next());
				}
			});
	}

	private String getCommitComment(String userId, ISnomedBrowserConcept snomedConceptInput, String action) {
		return String.format("%s %s concept %s", userId, action, getFsn(snomedConceptInput));
	}

	private String getFsn(ISnomedBrowserConcept concept) {
		if (concept.getFsn() != null) {
			return concept.getFsn().getTerm();
		}
		
		List<ISnomedBrowserDescription> descriptions = concept.getDescriptions();
		if (descriptions == null) {
			return concept.getConceptId();
		}

		for (ISnomedBrowserDescription description : descriptions) {
			if (!description.isActive()) { continue; }
			if (!Concepts.FULLY_SPECIFIED_NAME.equals(description.getType().getConceptId())) { continue; }
			// Found an active FSN, good to go
			return description.getTerm();	
		}
		
		return concept.getConceptId();
	}
	
	private List<ISnomedBrowserDescription> convertDescriptions(final Iterable<SnomedDescription> descriptions) {
		final ImmutableList.Builder<ISnomedBrowserDescription> convertedDescriptionBuilder = ImmutableList.builder();
		for (final SnomedDescription description : descriptions) {
			final SnomedBrowserDescription convertedDescription = new SnomedBrowserDescription();
	
			final SnomedBrowserDescriptionType descriptionType = SnomedBrowserDescriptionType.getByConceptId(description.getTypeId());
			if (null == descriptionType) {
				LOGGER.warn("Unsupported description type ID {} on description {}, ignoring.", description.getTypeId(), description.getId());
				continue;
			}
			
			convertedDescription.setActive(description.isActive());
			convertedDescription.setReleased(description.isReleased());
			convertedDescription.setCaseSignificance(description.getCaseSignificance());
			convertedDescription.setConceptId(description.getConceptId());
			convertedDescription.setDescriptionId(description.getId());
			convertedDescription.setEffectiveTime(description.getEffectiveTime());
			convertedDescription.setLang(description.getLanguageCode());
			convertedDescription.setModuleId(description.getModuleId());
			convertedDescription.setTerm(description.getTerm());
			convertedDescription.setType(descriptionType);
			convertedDescription.setAcceptabilityMap(description.getAcceptabilityMap());
			
			convertedDescription.setInactivationIndicator(description.getInactivationIndicator());
			convertedDescription.setAssociationTargets(description.getAssociationTargets());
			
			convertedDescriptionBuilder.add(convertedDescription);
		}
	
		return convertedDescriptionBuilder.build();
	}
	
	private List<ISnomedBrowserRelationship> convertRelationships(final Iterable<SnomedRelationship> relationships) {
		
		final LoadingCache<SnomedConcept, SnomedBrowserRelationshipType> types = CacheBuilder.newBuilder().build(new CacheLoader<SnomedConcept, SnomedBrowserRelationshipType>() {
			@Override
			public SnomedBrowserRelationshipType load(SnomedConcept key) throws Exception {
				return convertBrowserRelationshipType(key);
			}
		});
		
		final LoadingCache<SnomedConcept, SnomedBrowserRelationshipTarget> targets = CacheBuilder.newBuilder().build(new CacheLoader<SnomedConcept, SnomedBrowserRelationshipTarget>() {
			@Override
			public SnomedBrowserRelationshipTarget load(SnomedConcept key) throws Exception {
				return convertBrowserRelationshipTarget(key);
			}
		});
		
		final ImmutableList.Builder<ISnomedBrowserRelationship> convertedRelationshipBuilder = ImmutableList.builder();
		
		for (final SnomedRelationship relationship : relationships) {
			final SnomedBrowserRelationship convertedRelationship = new SnomedBrowserRelationship(relationship.getId());
			
			convertedRelationship.setActive(relationship.isActive());
			convertedRelationship.setCharacteristicType(relationship.getCharacteristicType());
			convertedRelationship.setEffectiveTime(relationship.getEffectiveTime());
			convertedRelationship.setGroupId(relationship.getGroup());
			convertedRelationship.setModifier(relationship.getModifier());
			convertedRelationship.setModuleId(relationship.getModuleId());
			convertedRelationship.setReleased(relationship.isReleased());
			convertedRelationship.setSourceId(relationship.getSourceId());
			
			convertedRelationship.setTarget(targets.getUnchecked(relationship.getDestination()));
			convertedRelationship.setType(types.getUnchecked(relationship.getType()));
			
			convertedRelationshipBuilder.add(convertedRelationship);
		}
		
		return convertedRelationshipBuilder.build();
	}
	
	@Override
	public SnomedBrowserRelationshipType convertBrowserRelationshipType(SnomedConcept concept) {
		final SnomedBrowserRelationshipType result = new SnomedBrowserRelationshipType(concept.getId());
		
		if (concept.getFsn() != null) {
			result.setFsn(new SnomedBrowserTerm(concept.getFsn()));
		} else {
			result.setFsn(new SnomedBrowserTerm(concept.getId()));
		}
	
		if (concept.getPt() != null) {
			result.setPt(new SnomedBrowserTerm(concept.getPt()));
		} else {
			result.setPt(new SnomedBrowserTerm(concept.getId()));
		}
		
		return result;
	}

	@Override
	public SnomedBrowserRelationshipTarget convertBrowserRelationshipTarget(SnomedConcept concept) {
		final SnomedBrowserRelationshipTarget result = new SnomedBrowserRelationshipTarget(concept.getId());
		
		result.setActive(concept.isActive());
		result.setDefinitionStatus(concept.getDefinitionStatus());
		result.setEffectiveTime(concept.getEffectiveTime());
		result.setFsn(concept.getFsn() != null ? new SnomedBrowserTerm(concept.getFsn()) : new SnomedBrowserTerm(concept.getId()));
		result.setPt(concept.getPt() != null ? new SnomedBrowserTerm(concept.getPt()) : new SnomedBrowserTerm(concept.getId()));
		result.setModuleId(concept.getModuleId());
		result.setReleased(concept.isReleased());
		
		return result;
	}

	private SnomedConcepts getConcepts(final String branchPath, final Set<String> conceptIds) {
		if (conceptIds.isEmpty()) {
			return new SnomedConcepts(0, 0);
		}
		return SnomedRequests.prepareSearchConcept()
				.setLimit(conceptIds.size())
				.filterByIds(conceptIds)
				.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
				.execute(getBus())
				.getSync();
	}

	@Override
	public SnomedBrowserRelationshipTarget getSnomedBrowserRelationshipTarget(SnomedConcept destinationConcept, String branch, List<ExtendedLocale> locales) {
		final DescriptionService descriptionService = new DescriptionService(getBus(), branch);
		final SnomedBrowserRelationshipTarget target = new SnomedBrowserRelationshipTarget(destinationConcept.getId());

		target.setActive(destinationConcept.isActive());
		target.setDefinitionStatus(destinationConcept.getDefinitionStatus());
		target.setEffectiveTime(destinationConcept.getEffectiveTime());
		target.setModuleId(destinationConcept.getModuleId());

		SnomedDescription fullySpecifiedName = descriptionService.getFullySpecifiedName(destinationConcept.getId(), locales);
		if (fullySpecifiedName != null) {
			target.setFsn(new SnomedBrowserTerm(fullySpecifiedName));
		} else {
			target.setFsn(new SnomedBrowserTerm(destinationConcept.getId()));
		}
		
		SnomedDescription pt = descriptionService.getPreferredTerm(destinationConcept.getId(), locales);
		if (pt != null) {
			target.setPt(new SnomedBrowserTerm(pt));
		} else {
			target.setPt(new SnomedBrowserTerm(destinationConcept.getId()));
		}
		
		return target;
	}

	private IEventBus getBus() {
		return ApplicationContext.getInstance().getServiceChecked(IEventBus.class);
	}

	private Branch getBranch(String branchPath) {
		return RepositoryRequests.branching().prepareGet(branchPath)
				.build(SnomedDatastoreActivator.REPOSITORY_UUID)
				.execute(getBus())
				.getSync();
	}

	private ISnomedBrowserAxiomService getAxiomService() {
		return ApplicationContext.getServiceForClass(ISnomedBrowserAxiomService.class);
	}
}
