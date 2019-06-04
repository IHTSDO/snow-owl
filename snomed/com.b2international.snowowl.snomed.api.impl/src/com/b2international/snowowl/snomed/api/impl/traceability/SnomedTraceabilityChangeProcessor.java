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
package com.b2international.snowowl.snomed.api.impl.traceability;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Sets.newHashSet;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.cdo.CDOObject;
import org.eclipse.emf.cdo.common.revision.delta.CDOFeatureDelta;
import org.eclipse.emf.cdo.common.revision.delta.CDORevisionDelta;
import org.eclipse.emf.cdo.common.revision.delta.CDOSetFeatureDelta;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.b2international.commons.http.ExtendedLocale;
import com.b2international.index.revision.RevisionIndex;
import com.b2international.index.revision.RevisionIndexRead;
import com.b2international.index.revision.RevisionSearcher;
import com.b2international.snowowl.core.ApplicationContext;
import com.b2international.snowowl.core.RepositoryManager;
import com.b2international.snowowl.core.api.IBranchPath;
import com.b2international.snowowl.core.api.SnowowlRuntimeException;
import com.b2international.snowowl.core.api.SnowowlServiceException;
import com.b2international.snowowl.core.events.metrics.MetricsThreadLocal;
import com.b2international.snowowl.core.events.metrics.Timer;
import com.b2international.snowowl.core.ft.FeatureToggles;
import com.b2international.snowowl.core.ft.Features;
import com.b2international.snowowl.datastore.ICDOChangeProcessor;
import com.b2international.snowowl.datastore.ICDOCommitChangeSet;
import com.b2international.snowowl.datastore.cdo.CDOIDUtils;
import com.b2international.snowowl.datastore.index.ImmutableIndexCommitChangeSet;
import com.b2international.snowowl.datastore.index.IndexCommitChangeSet;
import com.b2international.snowowl.eventbus.IEventBus;
import com.b2international.snowowl.identity.domain.User;
import com.b2international.snowowl.snomed.Component;
import com.b2international.snowowl.snomed.Concept;
import com.b2international.snowowl.snomed.Description;
import com.b2international.snowowl.snomed.Relationship;
import com.b2international.snowowl.snomed.SnomedConstants.Concepts;
import com.b2international.snowowl.snomed.SnomedPackage;
import com.b2international.snowowl.snomed.api.browser.ISnomedBrowserAxiomService;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserConcept;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserDescription;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserRelationship;
import com.b2international.snowowl.snomed.api.domain.browser.SnomedBrowserDescriptionType;
import com.b2international.snowowl.snomed.api.impl.domain.browser.SnomedBrowserConcept;
import com.b2international.snowowl.snomed.api.impl.domain.browser.SnomedBrowserDescription;
import com.b2international.snowowl.snomed.api.impl.domain.browser.SnomedBrowserRelationship;
import com.b2international.snowowl.snomed.api.impl.domain.browser.SnomedBrowserRelationshipTarget;
import com.b2international.snowowl.snomed.api.impl.domain.browser.SnomedBrowserRelationshipType;
import com.b2international.snowowl.snomed.api.impl.domain.browser.SnomedBrowserTerm;
import com.b2international.snowowl.snomed.common.ContentSubType;
import com.b2international.snowowl.snomed.common.SnomedTerminologyComponentConstants;
import com.b2international.snowowl.snomed.core.domain.SnomedConcept;
import com.b2international.snowowl.snomed.core.domain.SnomedConcepts;
import com.b2international.snowowl.snomed.core.domain.SnomedDescription;
import com.b2international.snowowl.snomed.core.domain.SnomedDescriptions;
import com.b2international.snowowl.snomed.core.domain.SnomedRelationship;
import com.b2international.snowowl.snomed.core.domain.SnomedRelationships;
import com.b2international.snowowl.snomed.datastore.SnomedDatastoreActivator;
import com.b2international.snowowl.snomed.datastore.SnomedDescriptionLookupService;
import com.b2international.snowowl.snomed.datastore.config.SnomedCoreConfiguration;
import com.b2international.snowowl.snomed.datastore.id.SnomedIdentifiers;
import com.b2international.snowowl.snomed.datastore.index.entry.SnomedConceptDocument;
import com.b2international.snowowl.snomed.datastore.index.entry.SnomedDescriptionIndexEntry;
import com.b2international.snowowl.snomed.datastore.index.entry.SnomedRefSetMemberIndexEntry;
import com.b2international.snowowl.snomed.datastore.index.entry.SnomedRelationshipIndexEntry;
import com.b2international.snowowl.snomed.datastore.request.SnomedRequests;
import com.b2international.snowowl.snomed.snomedrefset.SnomedOWLExpressionRefSetMember;
import com.b2international.snowowl.snomed.snomedrefset.SnomedRefSetMember;
import com.b2international.snowowl.snomed.snomedrefset.SnomedRefSetPackage;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * Change processor implementation that produces a log entry for committed transactions.
 */
@SuppressWarnings("deprecation")
public class SnomedTraceabilityChangeProcessor implements ICDOChangeProcessor {

	private static final Logger TRACE_LOGGER = LoggerFactory.getLogger("traceability");
	private static final Logger SYS_LOGGER = LoggerFactory.getLogger(SnomedTraceabilityChangeProcessor.class);
	private static final List<ExtendedLocale> LOCALES = ImmutableList.of(ExtendedLocale.valueOf("en-gb"));

	private static final String OWL_AXIOM = "OwlAxiom";
	private static final String OWL_ONTOLOGY = "OwlOntology";

	private static final Set<EClass> MRCM_REFSET_MEMBER_TYPES = ImmutableSet.<EClass>of(
			SnomedRefSetPackage.Literals.SNOMED_MRCM_DOMAIN_REF_SET_MEMBER,
			SnomedRefSetPackage.Literals.SNOMED_MRCM_ATTRIBUTE_DOMAIN_REF_SET_MEMBER,
			SnomedRefSetPackage.Literals.SNOMED_MRCM_ATTRIBUTE_RANGE_REF_SET_MEMBER,
			SnomedRefSetPackage.Literals.SNOMED_MRCM_MODULE_SCOPE_REF_SET_MEMBER);
	
	private static final Set<EClass> DETACHED_REFSET_MEMBER_TYPES = ImmutableSet.<EClass>of(
			SnomedRefSetPackage.Literals.SNOMED_OWL_EXPRESSION_REF_SET_MEMBER,
			SnomedRefSetPackage.Literals.SNOMED_LANGUAGE_REF_SET_MEMBER,
			SnomedRefSetPackage.Literals.SNOMED_ASSOCIATION_REF_SET_MEMBER,
			SnomedRefSetPackage.Literals.SNOMED_ATTRIBUTE_VALUE_REF_SET_MEMBER,
			SnomedRefSetPackage.Literals.SNOMED_MRCM_DOMAIN_REF_SET_MEMBER,
			SnomedRefSetPackage.Literals.SNOMED_MRCM_ATTRIBUTE_DOMAIN_REF_SET_MEMBER,
			SnomedRefSetPackage.Literals.SNOMED_MRCM_ATTRIBUTE_RANGE_REF_SET_MEMBER,
			SnomedRefSetPackage.Literals.SNOMED_MRCM_MODULE_SCOPE_REF_SET_MEMBER);
	
	private static final Set<EStructuralFeature> IGNORED_FEATURES = ImmutableSet.<EStructuralFeature>of(
			SnomedPackage.Literals.CONCEPT__DESCRIPTIONS,
			SnomedPackage.Literals.CONCEPT__OUTBOUND_RELATIONSHIPS,
			SnomedPackage.Literals.CONCEPT__FULLY_SPECIFIED_NAME,
			SnomedPackage.Literals.RELATIONSHIP__REFINABILITY_REF_SET_MEMBERS);
	
	private TraceabilityEntry entry;

	private final boolean collectSystemChanges;
	private final RevisionIndex index;
	private final IBranchPath branchPath;
	private ISnomedBrowserAxiomService axiomService;

	private ICDOCommitChangeSet commitChangeSet;
	private ObjectWriter objectWriter;
	private SnomedDescriptionLookupService descriptionLookupService;

	public SnomedTraceabilityChangeProcessor(final IBranchPath branchPath, ObjectWriter objectWriter) {
		this.branchPath = checkNotNull(branchPath);
		this.objectWriter = checkNotNull(objectWriter);
		this.index = ApplicationContext.getServiceForClass(RepositoryManager.class).get(SnomedDatastoreActivator.REPOSITORY_UUID).service(RevisionIndex.class);
		this.collectSystemChanges = collectSystemChanges(branchPath.getPath());
		this.descriptionLookupService = new SnomedDescriptionLookupService();
	}

	@Override
	public void process(ICDOCommitChangeSet commitChangeSet) throws SnowowlServiceException {
		
		final Timer traceabilityTimer = MetricsThreadLocal.get().timer("traceability");
		
		try {
			
			traceabilityTimer.start();
			
			this.entry = new TraceabilityEntry(commitChangeSet);
			this.commitChangeSet = commitChangeSet;
			
			if (isSystemCommit(commitChangeSet.getUserId()) && !collectSystemChanges) {
				return;
			}
			
			processNewComponents(commitChangeSet.getNewComponents());
			processComponentUpdates(commitChangeSet.getDirtyComponents());
			processComponentDeletions(commitChangeSet);
			
		} finally {
			traceabilityTimer.stop();
		}
	}

	@Override
	public IndexCommitChangeSet commit() throws SnowowlServiceException {
		SYS_LOGGER.info("Collected {} traceability entries on branch {}", entry.getChanges().size(), branchPath.getPath());
		return ImmutableIndexCommitChangeSet.builder().build();
	}

	@Override
	public void afterCommit() {
		
		final Timer traceabilityTimer = MetricsThreadLocal.get().timer("traceability");
		
		try {
			
			traceabilityTimer.start();
			
			if (commitChangeSet != null) {
				
				boolean isLightWeight = false;
				
				if (!entry.getChanges().isEmpty()) {
					
					if (!isDeltaImportInProgress(branchPath.getPath()) && !isClassificationInProgress(branchPath.getPath())) {
						
						final ImmutableSet<String> conceptIds = ImmutableSet.copyOf(entry.getChanges().keySet());
						
						final SnomedConcepts concepts = SnomedRequests.prepareSearchConcept()
								.setLimit(conceptIds.size())
								.filterByIds(conceptIds)
								.setExpand("descriptions(expand(inactivationProperties())),relationships(expand(destination()))")
								.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath.getPath())
								.execute(getBus())
								.getSync();
						
						final Set<SnomedBrowserConcept> convertedConcepts = newHashSet();
						
						for (SnomedConcept concept : concepts) {
							
							SnomedBrowserConcept convertedConcept = new SnomedBrowserConcept();
							
							convertedConcept.setActive(concept.isActive());
							convertedConcept.setConceptId(concept.getId());
							convertedConcept.setDefinitionStatus(concept.getDefinitionStatus());
							convertedConcept.setDescriptions(convertDescriptions(concept.getDescriptions()));
							convertedConcept.setEffectiveTime(concept.getEffectiveTime());
							convertedConcept.setModuleId(concept.getModuleId());
							convertedConcept.setRelationships(convertRelationships(concept.getRelationships()));
							convertedConcept.setFsn(new SnomedBrowserTerm(concept.getId()));
							convertedConcept.setInactivationIndicator(concept.getInactivationIndicator());
							convertedConcept.setAssociationTargets(concept.getAssociationTargets());
							convertedConcepts.add(convertedConcept);
								
						}
						
						Collection<? extends ISnomedBrowserConcept> resultsConcepts;
						
						if (!convertedConcepts.isEmpty()) { // Lookup and expand axioms
							resultsConcepts = getAxiomService().expandAxioms(convertedConcepts, branchPath.getPath(), LOCALES);
						} else {
							resultsConcepts = convertedConcepts;
						}
						
						for (ISnomedBrowserConcept result : resultsConcepts) {
							// PT and SYN labels are not populated
							entry.setConcept(result.getId(), (SnomedBrowserConcept) result);
						}
						
					} else {
						isLightWeight = true;
					}
					
				}
				
				TRACE_LOGGER.info(objectWriter.writeValueAsString(entry));
				SYS_LOGGER.info("Logged {} {}traceability entries on branch {}", entry.getChanges().size(), isLightWeight ? "lightweight " : "", branchPath.getPath());
			}
			
		} catch (IOException e) {
			throw SnowowlRuntimeException.wrap(e);
		} finally {
			traceabilityTimer.stop();
		}
	}

	@Override
	public String getName() {
		return "SNOMED CT Traceability";
	}

	@Override
	public void rollback() throws SnowowlServiceException {
		this.entry = null;
		this.commitChangeSet = null;
	}

	private void processNewComponents(Collection<CDOObject> newComponents) {
		
		for (CDOObject newComponent : newComponents) {
			
			final EClass eClass = newComponent.eClass();
			
			if (SnomedPackage.Literals.CONCEPT.equals(eClass)) {
				final Concept newConcept = (Concept) newComponent;
				entry.registerChange(newConcept.getId(), new TraceabilityChange(eClass, newConcept.getId(), ChangeType.CREATE));
			} else if (SnomedPackage.Literals.DESCRIPTION.equals(eClass)) {
				final Description newDescription = (Description) newComponent;
				entry.registerChange(newDescription.getConcept().getId(), new TraceabilityChange(eClass, newDescription.getId(), ChangeType.CREATE));
			} else if (SnomedPackage.Literals.RELATIONSHIP.equals(eClass)) {
				final Relationship newRelationship = (Relationship) newComponent;
				entry.registerChange(newRelationship.getSource().getId(), new TraceabilityChange(eClass, newRelationship.getId(), ChangeType.CREATE));
			} else if (SnomedRefSetPackage.Literals.SNOMED_OWL_EXPRESSION_REF_SET_MEMBER.equals(eClass)) {
				SnomedOWLExpressionRefSetMember member = (SnomedOWLExpressionRefSetMember) newComponent;
				if (SnomedIdentifiers.isConceptIdentifier(member.getReferencedComponentId())) {
					if (member.getRefSetIdentifierId().equals(Concepts.REFSET_OWL_AXIOM)) {
						entry.registerChange(member.getReferencedComponentId(), new TraceabilityChange(OWL_AXIOM, member.getUuid(), ChangeType.CREATE));
					} else if (member.getRefSetIdentifierId().equals(Concepts.REFSET_OWL_ONTOLOGY)) {
						entry.registerChange(member.getReferencedComponentId(), new TraceabilityChange(OWL_ONTOLOGY, member.getUuid(), ChangeType.CREATE));
					}
				}
			} else if (MRCM_REFSET_MEMBER_TYPES.contains(eClass)) {
				SnomedRefSetMember member = (SnomedRefSetMember) newComponent;
				if (SnomedIdentifiers.isConceptIdentifier(member.getReferencedComponentId())) {
					entry.registerChange(member.getReferencedComponentId(), new TraceabilityChange(eClass, member.getUuid(), ChangeType.CREATE));
				}
			} else {
				// language refset members are tracked by description update
				// association and attribute value refset members are tracked by container update (concept or description)
			}
			
		}
		
	}

	private void processComponentUpdates(Collection<CDOObject> dirtyComponents) {
		
		dirtyComponents.stream()
			.filter(Component.class::isInstance)
			.forEach(component -> processComponentUpdate(component));
		
		dirtyComponents.stream()
			.filter(SnomedRefSetMember.class::isInstance)
			.map(SnomedRefSetMember.class::cast)
			.forEach( member -> {
				
				EClass eClass = member.eClass();
				
				if (SnomedTerminologyComponentConstants.DESCRIPTION_NUMBER == member.getReferencedComponentType()) {
					
					if (SnomedRefSetPackage.Literals.SNOMED_LANGUAGE_REF_SET_MEMBER.equals(eClass) 
							|| SnomedRefSetPackage.Literals.SNOMED_ASSOCIATION_REF_SET_MEMBER.equals(eClass) 
							|| SnomedRefSetPackage.Literals.SNOMED_ATTRIBUTE_VALUE_REF_SET_MEMBER.equals(eClass)) {
						
						String conceptId = descriptionLookupService.getComponent(member.getReferencedComponentId(), commitChangeSet.getView()).getConcept().getId();
						entry.registerChange(conceptId, new TraceabilityChange(SnomedPackage.Literals.DESCRIPTION, member.getReferencedComponentId(), ChangeType.UPDATE));
						
					}
					
				} else if (SnomedTerminologyComponentConstants.CONCEPT_NUMBER == member.getReferencedComponentType()) {
					
					if (SnomedRefSetPackage.Literals.SNOMED_ASSOCIATION_REF_SET_MEMBER.equals(eClass) || SnomedRefSetPackage.Literals.SNOMED_ATTRIBUTE_VALUE_REF_SET_MEMBER.equals(eClass)) {
						entry.registerChange(member.getReferencedComponentId(), new TraceabilityChange(SnomedPackage.Literals.CONCEPT, member.getReferencedComponentId(), ChangeType.UPDATE));
					} else if (SnomedRefSetPackage.Literals.SNOMED_OWL_EXPRESSION_REF_SET_MEMBER.equals(eClass)) {
						if (Concepts.REFSET_OWL_AXIOM.equals(member.getRefSetIdentifierId())) {
							entry.registerChange(member.getReferencedComponentId(), new TraceabilityChange(OWL_AXIOM, member.getUuid(), ChangeType.UPDATE));
						} else if (Concepts.REFSET_OWL_ONTOLOGY.equals(member.getRefSetIdentifierId())) {
							entry.registerChange(member.getReferencedComponentId(), new TraceabilityChange(OWL_ONTOLOGY, member.getUuid(), ChangeType.UPDATE));
						}
					} else if (MRCM_REFSET_MEMBER_TYPES.contains(eClass)) {
						entry.registerChange(member.getReferencedComponentId(), new TraceabilityChange(eClass, member.getUuid(), ChangeType.UPDATE));
					}
					
				}
				
			});
		
	}

	private void processComponentUpdate(CDOObject dirtyComponent) {
		
		final EClass eClass = dirtyComponent.eClass();
		final CDORevisionDelta revisionDelta = commitChangeSet.getRevisionDeltas().get(dirtyComponent.cdoID());
	
		if (SnomedPackage.Literals.CONCEPT.equals(eClass)) {
			final Concept dirtyConcept = (Concept) dirtyComponent;
			registerInactivationOrUpdate(eClass, dirtyConcept.getId(), dirtyConcept.getId(), revisionDelta);
		} else if (SnomedPackage.Literals.DESCRIPTION.equals(eClass)) {
			final Description dirtyDescription = (Description) dirtyComponent;
			registerInactivationOrUpdate(eClass, dirtyDescription.getConcept().getId(), dirtyDescription.getId(), revisionDelta);
		} else if (SnomedPackage.Literals.RELATIONSHIP.equals(eClass)) {
			final Relationship dirtyRelationship = (Relationship) dirtyComponent;
			registerInactivationOrUpdate(eClass, dirtyRelationship.getSource().getId(), dirtyRelationship.getId(), revisionDelta);
		}
		
	}

	private void registerInactivationOrUpdate(final EClass eClass, final String conceptId, final String componentId, CDORevisionDelta revisionDelta) {
		CDOSetFeatureDelta activeDelta = (CDOSetFeatureDelta) revisionDelta.getFeatureDelta(SnomedPackage.Literals.COMPONENT__ACTIVE);
		if (activeDelta != null && Boolean.FALSE.equals(activeDelta.getValue())) {
			entry.registerChange(conceptId, new TraceabilityChange(eClass, componentId, ChangeType.INACTIVATE));
		} else {
			for (CDOFeatureDelta featureDelta : revisionDelta.getFeatureDeltas()) {
				if (!IGNORED_FEATURES.contains(featureDelta.getFeature())) {
					entry.registerChange(conceptId, new TraceabilityChange(eClass, componentId, ChangeType.UPDATE));
				}
			}
		}
	}

	private void processComponentDeletions(ICDOCommitChangeSet commitChangeSet) {
		
		index.read(branchPath.getPath(), new RevisionIndexRead<Void>() {
			@Override
			public Void execute(RevisionSearcher searcher) throws IOException {
				
				final Set<Long> detachedConceptStorageKeys = newHashSet(CDOIDUtils.createCdoIdToLong(commitChangeSet.getDetachedComponents(SnomedPackage.Literals.CONCEPT)));
				for (SnomedConceptDocument detachedConcept : searcher.get(SnomedConceptDocument.class, detachedConceptStorageKeys)) {
					entry.registerChange(detachedConcept.getId(), new TraceabilityChange(SnomedPackage.Literals.CONCEPT, detachedConcept.getId(), ChangeType.DELETE));
				}
				
				final Set<Long> detachedDescriptionStorageKeys = newHashSet(CDOIDUtils.createCdoIdToLong(commitChangeSet.getDetachedComponents(SnomedPackage.Literals.DESCRIPTION)));
				for (SnomedDescriptionIndexEntry detachedDescription : searcher.get(SnomedDescriptionIndexEntry.class, detachedDescriptionStorageKeys)) {
					entry.registerChange(detachedDescription.getConceptId(), new TraceabilityChange(SnomedPackage.Literals.DESCRIPTION, detachedDescription.getId(), ChangeType.DELETE));
				}
				
				final Set<Long> detachedRelationshipStorageKeys = newHashSet(CDOIDUtils.createCdoIdToLong(commitChangeSet.getDetachedComponents(SnomedPackage.Literals.RELATIONSHIP)));
				for (SnomedRelationshipIndexEntry detachedRelationship : searcher.get(SnomedRelationshipIndexEntry.class, detachedRelationshipStorageKeys)) {
					entry.registerChange(detachedRelationship.getSourceId(), new TraceabilityChange(SnomedPackage.Literals.RELATIONSHIP, detachedRelationship.getId(), ChangeType.DELETE));
				}
				
				for (EClass eClass : DETACHED_REFSET_MEMBER_TYPES) {
					Set<Long> detachedMemberStorageKeys = newHashSet(CDOIDUtils.createCdoIdToLong(commitChangeSet.getDetachedComponents(eClass)));
					for (SnomedRefSetMemberIndexEntry detachedRefsetMember : searcher.get(SnomedRefSetMemberIndexEntry.class, detachedMemberStorageKeys)) {					
						if (Concepts.REFSET_OWL_AXIOM.equals(detachedRefsetMember.getReferenceSetId())) {
							entry.registerChange(detachedRefsetMember.getReferencedComponentId(), new TraceabilityChange(OWL_AXIOM, detachedRefsetMember.getId(), ChangeType.DELETE));
						} else if (Concepts.REFSET_OWL_ONTOLOGY.equals(detachedRefsetMember.getReferenceSetId())) {
							entry.registerChange(detachedRefsetMember.getReferencedComponentId(), new TraceabilityChange(OWL_ONTOLOGY, detachedRefsetMember.getId(), ChangeType.DELETE));
						} else {
							entry.registerChange(detachedRefsetMember.getReferencedComponentId(), new TraceabilityChange(eClass, detachedRefsetMember.getId(), ChangeType.DELETE));
						}
					}
				}
				
				return null;
			}
		});
	}

	private List<ISnomedBrowserDescription> convertDescriptions(SnomedDescriptions descriptions) {
		return FluentIterable.from(descriptions).transform(new Function<SnomedDescription, ISnomedBrowserDescription>() {
			@Override
			public ISnomedBrowserDescription apply(SnomedDescription input) {
				final SnomedBrowserDescription convertedDescription = new SnomedBrowserDescription();
				
				convertedDescription.setAcceptabilityMap(input.getAcceptabilityMap());
				convertedDescription.setActive(input.isActive());
				convertedDescription.setCaseSignificance(input.getCaseSignificance());
				convertedDescription.setConceptId(input.getConceptId());
				convertedDescription.setDescriptionId(input.getId());
				convertedDescription.setEffectiveTime(input.getEffectiveTime());
				convertedDescription.setLang(input.getLanguageCode());
				convertedDescription.setModuleId(input.getModuleId());
				convertedDescription.setTerm(input.getTerm());
				convertedDescription.setType(SnomedBrowserDescriptionType.getByConceptId(input.getTypeId()));
				convertedDescription.setInactivationIndicator(input.getInactivationIndicator());
				convertedDescription.setAssociationTargets(input.getAssociationTargets());
				
				return convertedDescription;
			}
		}).toList();
	}

	private List<ISnomedBrowserRelationship> convertRelationships(SnomedRelationships relationships) {
		return FluentIterable.from(relationships).transform(new Function<SnomedRelationship, ISnomedBrowserRelationship>() {
			@Override
			public ISnomedBrowserRelationship apply(SnomedRelationship input) {
				final SnomedBrowserRelationship convertedRelationship = new SnomedBrowserRelationship();
				
				convertedRelationship.setActive(input.isActive());
				convertedRelationship.setCharacteristicType(input.getCharacteristicType());
				convertedRelationship.setEffectiveTime(input.getEffectiveTime());
				convertedRelationship.setGroupId(input.getGroup());
				convertedRelationship.setModifier(input.getModifier());
				convertedRelationship.setModuleId(input.getModuleId());
				convertedRelationship.setRelationshipId(input.getId());
				convertedRelationship.setSourceId(input.getSourceId());
				
				final SnomedBrowserRelationshipType type = new SnomedBrowserRelationshipType(input.getTypeId());
				type.setFsn(new SnomedBrowserTerm(input.getTypeId()));
				convertedRelationship.setType(type);
				
				final SnomedBrowserRelationshipTarget target = new SnomedBrowserRelationshipTarget(input.getDestinationId());
				target.setActive(input.getDestination().isActive());
				target.setDefinitionStatus(input.getDestination().getDefinitionStatus());
				target.setEffectiveTime(input.getDestination().getEffectiveTime());
				target.setFsn(new SnomedBrowserTerm(input.getDestinationId()));
				target.setModuleId(input.getDestination().getModuleId());
				convertedRelationship.setTarget(target);
				
				return convertedRelationship;
			}
		}).toList();
	}

	private ISnomedBrowserAxiomService getAxiomService() {
		if (axiomService == null) {
			axiomService = ApplicationContext.getServiceForClass(ISnomedBrowserAxiomService.class);
		}
		return axiomService;
	}

	private boolean isSystemCommit(String userId) {
		return User.isSystem(userId);
	}

	private boolean collectSystemChanges(String branch) {
		
		if (isDeltaImportInProgress(branch) || isClassificationInProgress(branch)) {
			return true;
		}
		
		return ApplicationContext.getServiceForClass(SnomedCoreConfiguration.class).isCollectSystemChanges();
	}

	private FeatureToggles getFeatureToggles() {
		return ApplicationContext.getServiceForClass(FeatureToggles.class);
	}

	private boolean isDeltaImportInProgress(final String branch) {
		return getFeatureToggles().isEnabled(Features.getImportFeatureToggle(SnomedDatastoreActivator.REPOSITORY_UUID, branch, ContentSubType.DELTA.getLowerCaseName()));
	}

	private boolean isClassificationInProgress(final String branch) {
		return getFeatureToggles().isEnabled(Features.getClassifyFeatureToggle(SnomedDatastoreActivator.REPOSITORY_UUID, branch));
	}

	private IEventBus getBus() {
		return ApplicationContext.getServiceForClass(IEventBus.class);
	}

}
