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
package com.b2international.snowowl.datastore.server.snomed;

import static com.b2international.snowowl.core.ApplicationContext.getServiceForClass;
import static com.b2international.snowowl.snomed.SnomedConstants.Concepts.FULLY_SPECIFIED_NAME;
import static com.b2international.snowowl.snomed.SnomedConstants.Concepts.IS_A;
import static com.b2international.snowowl.snomed.SnomedConstants.Concepts.MODULE_B2I_EXTENSION;
import static com.b2international.snowowl.snomed.SnomedConstants.Concepts.MODULE_ROOT;
import static com.b2international.snowowl.snomed.SnomedConstants.Concepts.MODULE_SCT_CORE;
import static com.b2international.snowowl.snomed.SnomedConstants.Concepts.REFSET_BOOLEAN_DATATYPE;
import static com.b2international.snowowl.snomed.SnomedConstants.Concepts.REFSET_CONCRETE_DOMAIN_TYPE;
import static com.b2international.snowowl.snomed.SnomedConstants.Concepts.REFSET_DATETIME_DATATYPE;
import static com.b2international.snowowl.snomed.SnomedConstants.Concepts.REFSET_DEFINING_TYPE;
import static com.b2international.snowowl.snomed.SnomedConstants.Concepts.REFSET_FLOAT_DATATYPE;
import static com.b2international.snowowl.snomed.SnomedConstants.Concepts.REFSET_INTEGER_DATATYPE;
import static com.b2international.snowowl.snomed.SnomedConstants.Concepts.REFSET_MEASUREMENT_TYPE;
import static com.b2international.snowowl.snomed.SnomedConstants.Concepts.REFSET_ROOT_CONCEPT;
import static com.b2international.snowowl.snomed.SnomedConstants.Concepts.REFSET_STRING_DATATYPE;
import static com.b2international.snowowl.snomed.SnomedConstants.Concepts.SYNONYM;

import java.util.Set;

import com.b2international.snowowl.core.SnowOwlApplication;
import com.b2international.snowowl.core.domain.TransactionContext;
import com.b2international.snowowl.core.events.bulk.BulkRequest;
import com.b2international.snowowl.core.events.bulk.BulkRequestBuilder;
import com.b2international.snowowl.datastore.oplock.impl.DatastoreLockContextDescriptions;
import com.b2international.snowowl.eventbus.IEventBus;
import com.b2international.snowowl.snomed.SnomedConstants;
import com.b2international.snowowl.snomed.core.domain.Acceptability;
import com.b2international.snowowl.snomed.core.domain.CaseSignificance;
import com.b2international.snowowl.snomed.core.domain.CharacteristicType;
import com.b2international.snowowl.snomed.core.domain.RelationshipModifier;
import com.b2international.snowowl.snomed.core.domain.SnomedConcepts;
import com.b2international.snowowl.snomed.datastore.ISnomedImportPostProcessor;
import com.b2international.snowowl.snomed.datastore.ISnomedPostProcessorContext;
import com.b2international.snowowl.snomed.datastore.SnomedDatastoreActivator;
import com.b2international.snowowl.snomed.datastore.config.SnomedCoreConfiguration;
import com.b2international.snowowl.snomed.datastore.request.SnomedConceptCreateRequestBuilder;
import com.b2international.snowowl.snomed.datastore.request.SnomedDescriptionCreateRequestBuilder;
import com.b2international.snowowl.snomed.datastore.request.SnomedRefSetCreateRequestBuilder;
import com.b2international.snowowl.snomed.datastore.request.SnomedRelationshipCreateRequestBuilder;
import com.b2international.snowowl.snomed.datastore.request.SnomedRequests;
import com.b2international.snowowl.snomed.snomedrefset.SnomedRefSetType;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * @since 4.6
 */
public class SnomedConcreteDomainImportPostProcessor implements ISnomedImportPostProcessor {

	private static final String B2I_NAMESPACE = "1000154";
	
	private static final String B2I_MODULE_PT = "SNOMED CT B2i extension module";
	private static final String B2I_MODULE_FSN = "SNOMED CT B2i extension module (core metadata concept)";
	
	private static final String DEFINING_TYPE_REFSET_FSN = "Defining type reference set (foundation metadata concept)";
	private static final String DEFINING_TYPE_REFSET_PT = "Defining type reference set";
	private static final String CONCRETE_DOMAIN_TYPE_REFSET_FSN = "Concrete domain type reference set (foundation metadata concept)";
	private static final String CONCRETE_DOMAIN_TYPE_REFSET_PT = "Concrete domain type reference set";
	private static final String MEASUREMENT_TYPE_REFSET_FSN = "Measurement type reference set (foundation metadata concept)";
	private static final String MEASUREMENT_TYPE_REFSET_PT = "Measurement type reference set";

	private static final String BOOLEAN_DATATYPE_REFSET_FSN = "Boolean datatype reference set (foundation metadata concept)";
	private static final String BOOLEAN_DATATYPE_REFSET_PT = "Boolean datatype reference set";
	private static final String DATETIME_DATATYPE_REFSET_FSN = "Datetime datatype reference set (foundation metadata concept)";
	private static final String DATETIME_DATATYPE_REFSET_PT = "Datetime datatype reference set";
	private static final String STRING_DATATYPE_REFSET_FSN = "String datatype reference set (foundation metadata concept)";
	private static final String STRING_DATATYPE_REFSET_PT = "String datatype reference set";
	private static final String FLOAT_DATATYPE_REFSET_FSN = "Float datatype reference set (foundation metadata concept)";
	private static final String FLOAT_DATATYPE_REFSET_PT = "Float datatype reference set";
	private static final String INTEGER_DATATYPE_REFSET_FSN = "Integer datatype reference set (foundation metadata concept)";
	private static final String INTEGER_DATATYPE_REFSET_PT = "Integer datatype reference set";
	
	@Override
	public void postProcess(final ISnomedPostProcessorContext postProcessorContext) {
		
		final SnomedCoreConfiguration snomedCoreConfiguration = SnowOwlApplication.INSTANCE
				.getConfiguration()
				.getModuleConfig(SnomedCoreConfiguration.class);
		
		final String branch = postProcessorContext.branch();
		
		if (!snomedCoreConfiguration.isConcreteDomainSupported()) {
			return;
		}
		
		final BulkRequestBuilder<TransactionContext> commitRequest = BulkRequest.create();
		
		if (!refsetIdentifierConceptsExist(snomedCoreConfiguration, branch)) {

			if (isDefaultConcreteDomainConfiguration(snomedCoreConfiguration)) {
				
				if (!conceptExists(MODULE_B2I_EXTENSION, branch)) {
					
					commitRequest.add(createConcept(MODULE_B2I_EXTENSION, B2I_MODULE_FSN, B2I_MODULE_PT, MODULE_ROOT, branch));
					
					commitRequest.add(createConcept(REFSET_DEFINING_TYPE, DEFINING_TYPE_REFSET_FSN, DEFINING_TYPE_REFSET_PT, REFSET_ROOT_CONCEPT, branch));
					commitRequest.add(createConcept(REFSET_CONCRETE_DOMAIN_TYPE, CONCRETE_DOMAIN_TYPE_REFSET_FSN, CONCRETE_DOMAIN_TYPE_REFSET_PT, REFSET_DEFINING_TYPE, branch));
					
					commitRequest.add(createConcept(REFSET_BOOLEAN_DATATYPE, BOOLEAN_DATATYPE_REFSET_FSN, BOOLEAN_DATATYPE_REFSET_PT, REFSET_CONCRETE_DOMAIN_TYPE, branch));
					commitRequest.add(createConcept(REFSET_DATETIME_DATATYPE, DATETIME_DATATYPE_REFSET_FSN, DATETIME_DATATYPE_REFSET_PT, REFSET_CONCRETE_DOMAIN_TYPE, branch));
					commitRequest.add(createConcept(REFSET_STRING_DATATYPE, STRING_DATATYPE_REFSET_FSN, STRING_DATATYPE_REFSET_PT, REFSET_CONCRETE_DOMAIN_TYPE, branch));
					
					commitRequest.add(createConcept(REFSET_MEASUREMENT_TYPE, MEASUREMENT_TYPE_REFSET_FSN, MEASUREMENT_TYPE_REFSET_PT, REFSET_CONCRETE_DOMAIN_TYPE, branch));
					commitRequest.add(createConcept(REFSET_FLOAT_DATATYPE, FLOAT_DATATYPE_REFSET_FSN, FLOAT_DATATYPE_REFSET_PT, REFSET_MEASUREMENT_TYPE, branch));
					commitRequest.add(createConcept(REFSET_INTEGER_DATATYPE, INTEGER_DATATYPE_REFSET_FSN, INTEGER_DATATYPE_REFSET_PT, REFSET_MEASUREMENT_TYPE, branch));
				}
				
			} else {
				postProcessorContext.getLogger().error("Concrete domain refset identifier concepts are missing from the dataset");
				return;
			}
		}
			
		if (!refsetExists(snomedCoreConfiguration.getBooleanDatatypeRefsetIdentifier(), branch)) {
			commitRequest.add(createRefSet(snomedCoreConfiguration.getBooleanDatatypeRefsetIdentifier(), branch));
		}
		
		if (!refsetExists(snomedCoreConfiguration.getDatetimeDatatypeRefsetIdentifier(), branch)) {
			commitRequest.add(createRefSet(snomedCoreConfiguration.getDatetimeDatatypeRefsetIdentifier(), branch));
		}
		
		if (!refsetExists(snomedCoreConfiguration.getFloatDatatypeRefsetIdentifier(), branch)) {
			commitRequest.add(createRefSet(snomedCoreConfiguration.getFloatDatatypeRefsetIdentifier(), branch));
		}
		
		if (!refsetExists(snomedCoreConfiguration.getIntegerDatatypeRefsetIdentifier(), branch)) {
			commitRequest.add(createRefSet(snomedCoreConfiguration.getIntegerDatatypeRefsetIdentifier(), branch));
		}
		
		if (!refsetExists(snomedCoreConfiguration.getStringDatatypeRefsetIdentifier(), branch)) {
			commitRequest.add(createRefSet(snomedCoreConfiguration.getStringDatatypeRefsetIdentifier(), branch));
		}
		
		try {
			SnomedRequests.prepareCommit()
					.setBody(commitRequest)
					.setUserId(postProcessorContext.getUserId())
					.setCommitComment("Import post processor created concrete domain reference sets.")
					.setParentContextDescription(DatastoreLockContextDescriptions.IMPORT)
					.build(SnomedDatastoreActivator.REPOSITORY_UUID, branch)
					.execute(getServiceForClass(IEventBus.class))
					.getSync();
		} catch (final Exception e) {
			postProcessorContext.getLogger().error("Caught exception while creating concrete domain reference sets in {}", getClass().getSimpleName(), e);
		}
	}

	private boolean conceptExists(final String conceptId, final String branch) {
		return SnomedRequests.prepareSearchConcept()
				.setComponentIds(ImmutableSet.of(conceptId))
				.setLimit(0)
				.build(SnomedDatastoreActivator.REPOSITORY_UUID, branch)
				.execute(getServiceForClass(IEventBus.class))
				.getSync()
				.getTotal() > 0;
	}

	private boolean refsetExists(final String refsetId, final String branch) {
		return SnomedRequests.prepareSearchRefSet()
				.setComponentIds(ImmutableSet.of(refsetId))
				.setLimit(0)
				.build(SnomedDatastoreActivator.REPOSITORY_UUID, branch)
				.execute(getServiceForClass(IEventBus.class))
				.getSync()
				.getTotal() > 0;
	}

	private SnomedRefSetCreateRequestBuilder createRefSet(final String identifierId, final String branch) {
		return SnomedRequests.prepareNewRefSet()
			.setIdentifierId(identifierId)
			.setType(SnomedRefSetType.CONCRETE_DATA_TYPE);
	}

	private SnomedConceptCreateRequestBuilder createConcept(final String identifierConceptId, final String fsnTerm, final String ptTerm, final String parent, final String branch) {
		return SnomedRequests.prepareNewConcept()
				.setId(identifierConceptId)
				.setActive(true)
				.setModuleId(MODULE_B2I_EXTENSION.equals(identifierConceptId) ? MODULE_SCT_CORE : MODULE_B2I_EXTENSION) // workaround to be able to set the module for the B2i module concept
				.addDescription(createDescription(identifierConceptId, fsnTerm, FULLY_SPECIFIED_NAME, Acceptability.PREFERRED, branch))
				.addDescription(createDescription(identifierConceptId, ptTerm, SYNONYM, Acceptability.PREFERRED, branch))
				.addRelationship(createIsaRelationship(identifierConceptId, parent, CharacteristicType.STATED_RELATIONSHIP, branch))
				.addRelationship(createIsaRelationship(identifierConceptId, parent, CharacteristicType.INFERRED_RELATIONSHIP, branch));
	}
	
	private SnomedDescriptionCreateRequestBuilder createDescription(final String conceptId, final String term, final String type, final Acceptability acceptability, final String branch) {
		return SnomedRequests.prepareNewDescription()
				.setIdFromNamespace(B2I_NAMESPACE)
				.setActive(true)
				.setModuleId(MODULE_B2I_EXTENSION)
				.setConceptId(conceptId)
				.setLanguageCode("en")
				.setTypeId(type)
				.setTerm(term)
				.setCaseSignificance(CaseSignificance.CASE_INSENSITIVE)
				.setAcceptability(ImmutableMap.of(SnomedConstants.Concepts.REFSET_LANGUAGE_TYPE_US, acceptability));
	}
	
	private SnomedRelationshipCreateRequestBuilder createIsaRelationship(final String source, final String destination, final CharacteristicType characteristicType, final String branch) {
		return SnomedRequests.prepareNewRelationship() 
			.setIdFromNamespace(B2I_NAMESPACE)
			.setActive(true)
			.setModuleId(MODULE_B2I_EXTENSION)
			.setSourceId(source)
			.setDestinationId(destination)
			.setTypeId(IS_A)
			.setCharacteristicType(characteristicType)
			.setModifier(RelationshipModifier.EXISTENTIAL);
	}
	
	private boolean isDefaultConcreteDomainConfiguration(final SnomedCoreConfiguration config) {
		return REFSET_CONCRETE_DOMAIN_TYPE.equals(config.getConcreteDomainTypeRefsetIdentifier())
				&& REFSET_BOOLEAN_DATATYPE.equals(config.getBooleanDatatypeRefsetIdentifier())
				&& REFSET_DATETIME_DATATYPE.equals(config.getDatetimeDatatypeRefsetIdentifier())
				&& REFSET_FLOAT_DATATYPE.equals(config.getFloatDatatypeRefsetIdentifier())
				&& REFSET_INTEGER_DATATYPE.equals(config.getIntegerDatatypeRefsetIdentifier())
				&& REFSET_STRING_DATATYPE.equals(config.getStringDatatypeRefsetIdentifier());
	}

	private boolean refsetIdentifierConceptsExist(final SnomedCoreConfiguration configuration, final String branch) {
		final Set<String> refSetIds = ImmutableSet.<String>builder()
				.add(configuration.getBooleanDatatypeRefsetIdentifier())
				.add(configuration.getDatetimeDatatypeRefsetIdentifier())
				.add(configuration.getFloatDatatypeRefsetIdentifier())
				.add(configuration.getIntegerDatatypeRefsetIdentifier())
				.add(configuration.getStringDatatypeRefsetIdentifier())
				.build();
		
		final SnomedConcepts concepts = SnomedRequests.prepareSearchConcept()
				.setComponentIds(refSetIds)
				.setLimit(0)
				.build(SnomedDatastoreActivator.REPOSITORY_UUID, branch)
				.execute(getServiceForClass(IEventBus.class))
				.getSync();

		return refSetIds.size() == concepts.getTotal();
	}

}
