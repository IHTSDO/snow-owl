/*
 * Copyright 2011-2018 B2i Healthcare Pte Ltd, http://b2i.sg
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
package com.b2international.snowowl.snomed.api.rest.classification;

import static com.b2international.snowowl.snomed.api.rest.CodeSystemRestRequests.createCodeSystem;
import static com.b2international.snowowl.snomed.api.rest.CodeSystemVersionRestRequests.createVersion;
import static com.b2international.snowowl.snomed.api.rest.CodeSystemVersionRestRequests.getNextAvailableEffectiveDateAsString;
import static com.b2international.snowowl.snomed.api.rest.SnomedClassificationRestRequests.beginClassification;
import static com.b2international.snowowl.snomed.api.rest.SnomedClassificationRestRequests.beginClassificationSave;
import static com.b2international.snowowl.snomed.api.rest.SnomedClassificationRestRequests.getClassification;
import static com.b2international.snowowl.snomed.api.rest.SnomedClassificationRestRequests.getClassificationJobId;
import static com.b2international.snowowl.snomed.api.rest.SnomedClassificationRestRequests.getEquivalentConceptSets;
import static com.b2international.snowowl.snomed.api.rest.SnomedClassificationRestRequests.getRelationshipChanges;
import static com.b2international.snowowl.snomed.api.rest.SnomedClassificationRestRequests.waitForClassificationJob;
import static com.b2international.snowowl.snomed.api.rest.SnomedClassificationRestRequests.waitForClassificationSaveJob;
import static com.b2international.snowowl.snomed.api.rest.SnomedComponentRestRequests.createComponent;
import static com.b2international.snowowl.snomed.api.rest.SnomedComponentRestRequests.getComponent;
import static com.b2international.snowowl.snomed.api.rest.SnomedRestFixtures.changeToDefining;
import static com.b2international.snowowl.snomed.api.rest.SnomedRestFixtures.createNewConcept;
import static com.b2international.snowowl.snomed.api.rest.SnomedRestFixtures.createNewRelationship;
import static com.b2international.snowowl.snomed.api.rest.SnomedRestFixtures.createRefSetMemberRequestBody;
import static com.b2international.snowowl.snomed.api.rest.SnomedRestFixtures.createRelationshipRequestBody;
import static com.b2international.snowowl.snomed.api.rest.SnomedRestFixtures.inactivateRelationship;
import static com.b2international.snowowl.test.commons.rest.RestExtensions.givenAuthenticatedRequest;
import static com.b2international.snowowl.test.commons.rest.RestExtensions.lastPathSegment;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.BeforeClass;
import org.junit.Test;

import com.b2international.commons.http.ExtendedLocale;
import com.b2international.snowowl.core.ApplicationContext;
import com.b2international.snowowl.core.api.IBranchPath;
import com.b2international.snowowl.core.date.DateFormats;
import com.b2international.snowowl.core.date.Dates;
import com.b2international.snowowl.datastore.BranchPathUtils;
import com.b2international.snowowl.datastore.file.FileRegistry;
import com.b2international.snowowl.datastore.internal.file.InternalFileRegistry;
import com.b2international.snowowl.datastore.server.internal.JsonSupport;
import com.b2international.snowowl.eventbus.IEventBus;
import com.b2international.snowowl.identity.domain.User;
import com.b2international.snowowl.snomed.SnomedConstants.Concepts;
import com.b2international.snowowl.snomed.api.rest.AbstractSnomedApiTest;
import com.b2international.snowowl.snomed.api.rest.CodeSystemVersionRestRequests;
import com.b2international.snowowl.snomed.api.rest.SnomedApiTestConstants;
import com.b2international.snowowl.snomed.api.rest.SnomedBranchingRestRequests;
import com.b2international.snowowl.snomed.api.rest.SnomedComponentType;
import com.b2international.snowowl.snomed.api.rest.domain.SnomedRefSetMemberRestInput;
import com.b2international.snowowl.snomed.common.SnomedRf2Headers;
import com.b2international.snowowl.snomed.common.SnomedTerminologyComponentConstants;
import com.b2international.snowowl.snomed.core.domain.CharacteristicType;
import com.b2international.snowowl.snomed.core.domain.RelationshipModifier;
import com.b2international.snowowl.snomed.core.domain.Rf2ExportResult;
import com.b2international.snowowl.snomed.core.domain.Rf2RefSetExportLayout;
import com.b2international.snowowl.snomed.core.domain.Rf2ReleaseType;
import com.b2international.snowowl.snomed.core.domain.SnomedConcept;
import com.b2international.snowowl.snomed.core.domain.SnomedConcepts;
import com.b2international.snowowl.snomed.datastore.SnomedDatastoreActivator;
import com.b2international.snowowl.snomed.datastore.config.SnomedClassificationConfiguration;
import com.b2international.snowowl.snomed.datastore.config.SnomedCoreConfiguration;
import com.b2international.snowowl.snomed.datastore.id.SnomedIdentifiers;
import com.b2international.snowowl.snomed.datastore.request.SnomedRequests;
import com.b2international.snowowl.snomed.reasoner.domain.ChangeNature;
import com.b2international.snowowl.snomed.reasoner.domain.ClassificationStatus;
import com.b2international.snowowl.snomed.reasoner.domain.EquivalentConceptSets;
import com.b2international.snowowl.snomed.reasoner.domain.ReasonerRelationship;
import com.b2international.snowowl.snomed.reasoner.domain.RelationshipChange;
import com.b2international.snowowl.snomed.reasoner.domain.RelationshipChanges;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;

import io.restassured.response.ValidatableResponse;

/**
 * @since 4.6
 */
public class SnomedClassificationApiTest extends AbstractSnomedApiTest {

	private static final ObjectMapper MAPPER = JsonSupport.getDefaultObjectMapper();

	private static final String ACCESS = "260507000";
	private static final String BLADDER_FILLING_TECHNIQUE = "246502009";
	
	private static final String ASSOCIATED_FINDING = "246090004";
	private static final String POST_PROCESSING = "260931002";
	private static final String PROVOCATION_TECHNIQUE = "246506007";

	private static final boolean USE_EXTERNAL_SERVICE = false;
	private static final String PATH_TO_EXTERNAL_SERVICE_RELEASE_FOLDER = "<path_to_external_service_release_folder>";
	
	private static String REASONER_ID = SnomedClassificationConfiguration.ELK_REASONER_ID;
	
	private static int getPersistedInferredRelationshipCount(IBranchPath conceptPath, String conceptId) {
		List<Map<String, Object>> relationships = getComponent(conceptPath, SnomedComponentType.CONCEPT, conceptId, 
				"relationships(\"active\":true,\"characteristicType\":\"" + CharacteristicType.INFERRED_RELATIONSHIP.getConceptId() + "\")")
				.statusCode(200)
				.extract()
				.jsonPath()
				.getList("relationships.items");

		return relationships.size();
	}
	
	@BeforeClass
	public static void before() throws IOException {
		if (USE_EXTERNAL_SERVICE) {
			
			REASONER_ID = "org.semanticweb.elk.owlapi.ElkReasonerFactory"; // IHTSDO default 
			
			Rf2ExportResult exportResult = SnomedRequests.rf2().prepareExport()
					.setReleaseType(Rf2ReleaseType.SNAPSHOT)
					.setIncludePreReleaseContent(false)
					.setConceptsAndRelationshipsOnly(true)
					.setUserId(User.SYSTEM.getUsername())
					.setReferenceBranch(BranchPathUtils.createMainPath().getPath())
					.setRefSetExportLayout(Rf2RefSetExportLayout.COMBINED)
					.setCountryNamespaceElement(SnomedIdentifiers.INT_NAMESPACE)
					.setLocales(ImmutableList.of(ExtendedLocale.valueOf("en-gb"), ExtendedLocale.valueOf("en-us")))
					.build(SnomedDatastoreActivator.REPOSITORY_UUID)
					.execute(ApplicationContext.getServiceForClass(IEventBus.class))
					.getSync();
			
			UUID fileId = exportResult.getRegistryId();
			
			InternalFileRegistry fileRegistry = (InternalFileRegistry) ApplicationContext.getServiceForClass(FileRegistry.class);
			File rf2Delta = fileRegistry.getFile(fileId);
			
			Path previousPackageFilePath = Paths.get(PATH_TO_EXTERNAL_SERVICE_RELEASE_FOLDER,
					"prevPackage_" + Dates.formatByGmt(new Date(), DateFormats.SHORT) + ".zip");
			
			if (Files.exists(previousPackageFilePath)) {
				Files.delete(previousPackageFilePath);
			}
			
			Files.copy(rf2Delta.toPath(), previousPackageFilePath, StandardCopyOption.REPLACE_EXISTING);
			
			fileRegistry.delete(fileId);
			
			Date latestEffectiveDate = CodeSystemVersionRestRequests.getEffectiveDates(SnomedTerminologyComponentConstants.SNOMED_SHORT_NAME).last();
			
			SnomedBranchingRestRequests.updateBranch(BranchPathUtils.createMainPath(), ImmutableMap.<String, Object>builder()
					.put("previousPackage", previousPackageFilePath.getFileName().toString())
					.put("previousRelease", Dates.formatByGmt(latestEffectiveDate, DateFormats.SHORT))
					.build());
			
		}
	}

	@Test
	public void testClassificationTaskInitialState() {
		
		createNewConcept(branchPath);
		
		String firstJobId = getClassificationJobId(beginClassification(branchPath));
		
		getClassification(branchPath, firstJobId)
			.statusCode(200)
			.body("status", anyOf(equalTo(ClassificationStatus.SCHEDULED.name()), equalTo(ClassificationStatus.RUNNING.name())));
		
		String secondJobId = getClassificationJobId(beginClassification(branchPath));
		
		getClassification(branchPath, secondJobId)
			.statusCode(200)
			.body("status", anyOf(equalTo(ClassificationStatus.SCHEDULED.name()), equalTo(ClassificationStatus.RUNNING.name())));
	
		waitForClassificationJob(branchPath, firstJobId)
			.statusCode(200)
			.body("status", equalTo(ClassificationStatus.COMPLETED.name()));
		
		waitForClassificationJob(branchPath, secondJobId)
			.statusCode(200)
			.body("status", equalTo(ClassificationStatus.COMPLETED.name()));
		
	}
	
	@Test
	public void testClassificationTaskBecomesStale() {
		
		createNewConcept(branchPath);
		
		String firstJobId = getClassificationJobId(beginClassification(branchPath));
		
		waitForClassificationJob(branchPath, firstJobId)
			.statusCode(200)
			.body("status", equalTo(ClassificationStatus.COMPLETED.name()));
		
		createNewConcept(branchPath);
		
		getClassification(branchPath, firstJobId)
			.statusCode(200)
			.body("status", equalTo(ClassificationStatus.STALE.name()));
		
	}
	
	@Test
	public void testInferredNotStatedFlag() throws Exception {
		
		if (USE_EXTERNAL_SERVICE) {
			
			final String parentConceptId = createNewConcept(branchPath);
			final String targetConceptId = createNewConcept(branchPath);
			final String childConceptId = createNewConcept(branchPath, parentConceptId);
			
			createNewRelationship(branchPath, parentConceptId, Concepts.MORPHOLOGY, targetConceptId);
			
			String classificationId = getClassificationJobId(beginClassification(branchPath, REASONER_ID, USE_EXTERNAL_SERVICE));
			waitForClassificationJob(branchPath, classificationId)
			.statusCode(200)
			.body("status", equalTo(ClassificationStatus.COMPLETED.name()));
			
			Collection<RelationshipChange> changes = MAPPER.readValue(getRelationshipChanges(branchPath, classificationId).statusCode(200)
					.extract()
					.asInputStream(), RelationshipChanges.class)
					.getItems();
			
			Set<RelationshipChange> relationshipChanges = changes.stream()
					.filter(change -> change.getRelationship().getSourceId().equals(childConceptId) &&
							change.getRelationship().getTypeId().equals(Concepts.MORPHOLOGY) &&
							change.getRelationship().getDestinationId().equals(targetConceptId) &&
							ChangeNature.INFERRED == change.getChangeNature())
					.collect(toSet());
			
			assertEquals(1, relationshipChanges.size());
			RelationshipChange relationshipChange = relationshipChanges.stream().findFirst().get();
			assertTrue(relationshipChange.isInferredNotStated());
			
		}
		
	}
	
	
	@Test
	public void persistInferredRelationship() throws Exception {
		String parentConceptId = createNewConcept(branchPath);
		String targetConceptId = createNewConcept(branchPath);
		String childConceptId = createNewConcept(branchPath, parentConceptId);

		createNewRelationship(branchPath, parentConceptId, Concepts.MORPHOLOGY, targetConceptId);

		String classificationId = getClassificationJobId(beginClassification(branchPath, REASONER_ID, USE_EXTERNAL_SERVICE));
		waitForClassificationJob(branchPath, classificationId)
		.statusCode(200)
		.body("status", equalTo(ClassificationStatus.COMPLETED.name()));

		Collection<RelationshipChange> changes = MAPPER.readValue(getRelationshipChanges(branchPath, classificationId).statusCode(200)
				.extract()
				.asInputStream(), RelationshipChanges.class)
				.getItems();

		Multimap<String, RelationshipChange> changesBySource = Multimaps.index(changes, c -> c.getRelationship().getSourceId());
		Collection<RelationshipChange> parentRelationshipChanges = changesBySource.get(parentConceptId);
		Collection<RelationshipChange> childRelationshipChanges = changesBySource.get(childConceptId);

		// parent concept should have two inferred relationships, one ISA and one MORPHOLOGY, both inferred
		assertEquals(2, parentRelationshipChanges.size());
		// child concept should have two inferred relationships, one ISA and one MORPHOLOGY from parent, both inferred
		assertEquals(2, childRelationshipChanges.size());

		for (RelationshipChange change : parentRelationshipChanges) {
			assertEquals(ChangeNature.INFERRED, change.getChangeNature());
			switch (change.getRelationship().getTypeId()) {
			case Concepts.IS_A:
				assertEquals(Concepts.ROOT_CONCEPT, change.getRelationship().getDestinationId());
				break;
			case Concepts.MORPHOLOGY:
				assertEquals(targetConceptId, change.getRelationship().getDestinationId());
				break;
			}
		}

		for (RelationshipChange change : childRelationshipChanges) {
			assertEquals(ChangeNature.INFERRED, change.getChangeNature());
			switch (change.getRelationship().getTypeId()) {
			case Concepts.IS_A:
				assertEquals(parentConceptId, change.getRelationship().getDestinationId());
				break;
			case Concepts.MORPHOLOGY:
				assertEquals(targetConceptId, change.getRelationship().getDestinationId());
				break;
			}
		}

		beginClassificationSave(branchPath, classificationId);
		waitForClassificationSaveJob(branchPath, classificationId)
		.statusCode(200)
		.body("status", equalTo(ClassificationStatus.SAVED.name()));

		assertEquals(2, getPersistedInferredRelationshipCount(branchPath, parentConceptId));
		assertEquals(2, getPersistedInferredRelationshipCount(branchPath, childConceptId));
		
	}

	@Test
	public void persistRedundantRelationship() throws Exception {
		String parentConceptId = createNewConcept(branchPath);
		String childConceptId = createNewConcept(branchPath, parentConceptId);

		// Add "regular" inferences before running the classification
		createNewRelationship(branchPath, parentConceptId, Concepts.IS_A, Concepts.ROOT_CONCEPT, CharacteristicType.INFERRED_RELATIONSHIP);
		createNewRelationship(branchPath, childConceptId, Concepts.IS_A, parentConceptId, CharacteristicType.INFERRED_RELATIONSHIP);

		// Add redundant information that should be removed
		createNewRelationship(branchPath, childConceptId, Concepts.IS_A, Concepts.ROOT_CONCEPT, CharacteristicType.INFERRED_RELATIONSHIP);

		String classificationId = getClassificationJobId(beginClassification(branchPath, REASONER_ID, USE_EXTERNAL_SERVICE));
		waitForClassificationJob(branchPath, classificationId)
			.statusCode(200)
			.body("status", equalTo(ClassificationStatus.COMPLETED.name()));

		RelationshipChanges changes = MAPPER.readValue(getRelationshipChanges(branchPath, classificationId).statusCode(200)
				.extract()
				.asInputStream(), RelationshipChanges.class);

		assertEquals(1, changes.getTotal());
		RelationshipChange relationshipChange = Iterables.getOnlyElement(changes.getItems());
		assertEquals(ChangeNature.REDUNDANT, relationshipChange.getChangeNature());
		assertEquals(childConceptId, relationshipChange.getRelationship().getSourceId());
		assertEquals(Concepts.IS_A, relationshipChange.getRelationship().getTypeId());
		assertEquals(Concepts.ROOT_CONCEPT, relationshipChange.getRelationship().getDestinationId());

		beginClassificationSave(branchPath, classificationId);
		waitForClassificationSaveJob(branchPath, classificationId)
			.statusCode(200)
			.body("status", equalTo(ClassificationStatus.SAVED.name()));

		assertEquals(1, getPersistedInferredRelationshipCount(branchPath, parentConceptId));
		assertEquals(1, getPersistedInferredRelationshipCount(branchPath, childConceptId));
	}
	
	@Test
	public void inactivateRedundantReleasedRelationshipWithoutModuleChange() throws Exception {
		testRedundantRelationshipInactivation("");
	}
	
	@Test
	public void inactivateRedundantReleasedRelationshipWithModuleChange() throws Exception {
		testRedundantRelationshipInactivation(Concepts.MODULE_SCT_MODEL_COMPONENT);
	}

	private void testRedundantRelationshipInactivation(final String defaultModuleId) throws Exception {
		
		final String codeSystemShortName = "SNOMEDCT-CLASSIFY-RSHIPS" + defaultModuleId;
		createCodeSystem(branchPath, codeSystemShortName).statusCode(201);
		
		String parentConceptId = createNewConcept(branchPath);
		String childConceptId = createNewConcept(branchPath, parentConceptId);
		
		// Add "regular" inferences before running the classification
		createNewRelationship(branchPath, parentConceptId, Concepts.IS_A, Concepts.ROOT_CONCEPT, CharacteristicType.INFERRED_RELATIONSHIP);
		createNewRelationship(branchPath, childConceptId, Concepts.IS_A, parentConceptId, CharacteristicType.INFERRED_RELATIONSHIP);

		// Add redundant information that should be removed
		String redundantRelationshipId = createNewRelationship(branchPath, childConceptId, Concepts.IS_A, Concepts.ROOT_CONCEPT, CharacteristicType.INFERRED_RELATIONSHIP);
		
		// Create version
		String effectiveDate = getNextAvailableEffectiveDateAsString(codeSystemShortName);
		createVersion(codeSystemShortName, "v1", effectiveDate).statusCode(201);
		
		if (USE_EXTERNAL_SERVICE) {
			
			Rf2ExportResult exportResult = SnomedRequests.rf2().prepareExport()
					.setReleaseType(Rf2ReleaseType.SNAPSHOT)
					.setIncludePreReleaseContent(false)
					.setConceptsAndRelationshipsOnly(true)
					.setUserId(User.SYSTEM.getUsername())
					.setReferenceBranch(branchPath.getPath())
					.setRefSetExportLayout(Rf2RefSetExportLayout.COMBINED)
					.setCountryNamespaceElement(SnomedIdentifiers.INT_NAMESPACE)
					.setLocales(ImmutableList.of(ExtendedLocale.valueOf("en-gb"), ExtendedLocale.valueOf("en-us")))
					.build(SnomedDatastoreActivator.REPOSITORY_UUID)
					.execute(ApplicationContext.getServiceForClass(IEventBus.class))
					.getSync();
			
			UUID fileId = exportResult.getRegistryId();
			
			InternalFileRegistry fileRegistry = (InternalFileRegistry) ApplicationContext.getServiceForClass(FileRegistry.class);
			File rf2Delta = fileRegistry.getFile(fileId);
			
			Path depPackageFilePath = Paths.get(PATH_TO_EXTERNAL_SERVICE_RELEASE_FOLDER, "depPackage_" + codeSystemShortName + ".zip");
			
			Files.copy(rf2Delta.toPath(), depPackageFilePath, StandardCopyOption.REPLACE_EXISTING);
			
			fileRegistry.delete(fileId);
			
			SnomedBranchingRestRequests.updateBranch(branchPath, ImmutableMap.<String, Object>builder()
					.put("dependencyPackage", depPackageFilePath.getFileName().toString())
					.build());
			
		}
		
		String classificationId = getClassificationJobId(beginClassification(branchPath, REASONER_ID, USE_EXTERNAL_SERVICE));
		waitForClassificationJob(branchPath, classificationId)
			.statusCode(200)
			.body("status", equalTo(ClassificationStatus.COMPLETED.name()));

		RelationshipChanges changes = MAPPER.readValue(getRelationshipChanges(branchPath, classificationId).statusCode(200)
				.extract()
				.asInputStream(), RelationshipChanges.class);

		assertEquals(1, changes.getTotal());
		RelationshipChange relationshipChange = Iterables.getOnlyElement(changes);
		assertEquals(ChangeNature.REDUNDANT, relationshipChange.getChangeNature());
		assertEquals(childConceptId, relationshipChange.getRelationship().getSourceId());
		assertEquals(Concepts.IS_A, relationshipChange.getRelationship().getTypeId());
		assertEquals(Concepts.ROOT_CONCEPT, relationshipChange.getRelationship().getDestinationId());
		assertEquals(redundantRelationshipId, relationshipChange.getRelationship().getOriginId());
		
		getComponent(branchPath, SnomedComponentType.RELATIONSHIP, redundantRelationshipId)
			.statusCode(200)
			.body("active", equalTo(true))
			.body("moduleId", equalTo(Concepts.MODULE_SCT_CORE));
		
		if (!Strings.isNullOrEmpty(defaultModuleId)) {
			final Map<?, ?> metadataUpdated = ImmutableMap.of(SnomedCoreConfiguration.BRANCH_DEFAULT_MODULE_ID_KEY, defaultModuleId);
			SnomedBranchingRestRequests.updateBranch(branchPath, metadataUpdated);
		}
		
		beginClassificationSave(branchPath, classificationId);
		waitForClassificationSaveJob(branchPath, classificationId)
			.statusCode(200)
			.body("status", equalTo(ClassificationStatus.SAVED.name()));

		getComponent(branchPath, SnomedComponentType.RELATIONSHIP, redundantRelationshipId)
			.statusCode(200)
			.body("active", equalTo(false))
			.body("moduleId", equalTo(Strings.isNullOrEmpty(defaultModuleId) ? Concepts.MODULE_SCT_CORE : defaultModuleId));
		
		assertEquals(1, getPersistedInferredRelationshipCount(branchPath, parentConceptId));
		assertEquals(1, getPersistedInferredRelationshipCount(branchPath, childConceptId));
	}

	@Test
	public void testRedundantRelationshipModuleChange() throws Exception {
		
		final String codeSystemShortName = "SNOMEDCT-CLASSIFY-RSHIPS-MOD";
		createCodeSystem(branchPath, codeSystemShortName).statusCode(201);
		
		String parentConceptId = createNewConcept(branchPath);
		String childConceptId = createNewConcept(branchPath, parentConceptId);
		
		// Add "regular" inferences before running the classification
		createNewRelationship(branchPath, parentConceptId, Concepts.IS_A, Concepts.ROOT_CONCEPT, CharacteristicType.INFERRED_RELATIONSHIP);
		createNewRelationship(branchPath, childConceptId, Concepts.IS_A, parentConceptId, CharacteristicType.INFERRED_RELATIONSHIP);

		// Add redundant information that should be removed
		Map<?, ?> relationshipRequestBody = createRelationshipRequestBody(
				childConceptId, Concepts.IS_A, Concepts.ROOT_CONCEPT,
				Concepts.MODULE_SCT_MODEL_COMPONENT, CharacteristicType.INFERRED_RELATIONSHIP, 0)
				.put("commitComment", "Created new relationship")
				.build();

		String redundantRelationshipId = lastPathSegment(createComponent(branchPath, SnomedComponentType.RELATIONSHIP, relationshipRequestBody)
				.statusCode(201)
				.body(equalTo(""))
				.extract().header("Location"));
		
		getComponent(branchPath, SnomedComponentType.RELATIONSHIP, redundantRelationshipId)
			.statusCode(200)
			.body("active", equalTo(true))
			.body("moduleId", equalTo(Concepts.MODULE_SCT_MODEL_COMPONENT));
		
		// Create version
		String effectiveDate = getNextAvailableEffectiveDateAsString(codeSystemShortName);
		createVersion(codeSystemShortName, "v1", effectiveDate).statusCode(201);
		
		String classificationId = getClassificationJobId(beginClassification(branchPath));
		waitForClassificationJob(branchPath, classificationId)
			.statusCode(200)
			.body("status", equalTo(ClassificationStatus.COMPLETED.name()));

		RelationshipChanges changes = MAPPER.readValue(getRelationshipChanges(branchPath, classificationId).statusCode(200)
				.extract()
				.asInputStream(), RelationshipChanges.class);

		assertEquals(1, changes.getTotal());
		RelationshipChange relationshipChange = Iterables.getOnlyElement(changes);
		assertEquals(ChangeNature.REDUNDANT, relationshipChange.getChangeNature());
		assertEquals(childConceptId, relationshipChange.getRelationship().getSourceId());
		assertEquals(Concepts.IS_A, relationshipChange.getRelationship().getTypeId());
		assertEquals(Concepts.ROOT_CONCEPT, relationshipChange.getRelationship().getDestinationId());
		assertEquals(redundantRelationshipId, relationshipChange.getRelationship().getOriginId());
		
		beginClassificationSave(branchPath, classificationId);
		waitForClassificationSaveJob(branchPath, classificationId)
			.statusCode(200)
			.body("status", equalTo(ClassificationStatus.SAVED.name()));

		getComponent(branchPath, SnomedComponentType.RELATIONSHIP, redundantRelationshipId)
			.statusCode(200)
			.body("active", equalTo(false))
			.body("moduleId", equalTo(Concepts.MODULE_SCT_CORE));
		
		assertEquals(1, getPersistedInferredRelationshipCount(branchPath, parentConceptId));
		assertEquals(1, getPersistedInferredRelationshipCount(branchPath, childConceptId));
	}
	
	@Test
	public void issue_SO_2152_testGroupRenumbering() throws Exception {
		String conceptId = createNewConcept(branchPath);

		// Add "regular" inferences before running the classification
		createNewRelationship(branchPath, conceptId, Concepts.IS_A, Concepts.ROOT_CONCEPT, CharacteristicType.INFERRED_RELATIONSHIP);
		// Add new relationship to the root as stated
		createNewRelationship(branchPath);
		// Add the same relationship with a different group to the new concept as inferred
		createNewRelationship(branchPath, conceptId, Concepts.PART_OF, Concepts.NAMESPACE_ROOT, CharacteristicType.INFERRED_RELATIONSHIP, 5);

		String classificationId = getClassificationJobId(beginClassification(branchPath, REASONER_ID, USE_EXTERNAL_SERVICE));
		waitForClassificationJob(branchPath, classificationId)
			.statusCode(200)
			.body("status", equalTo(ClassificationStatus.COMPLETED.name()));

		/* 
		 * Expecting lots of changes; all concepts receive the "Part of" relationship because it was added to the root concept, however, the original inferred relationship 
		 * with group 5 should be redundant.
		 */
		RelationshipChanges changes = MAPPER.readValue(getRelationshipChanges(branchPath, classificationId).statusCode(200)
				.extract()
				.asInputStream(), RelationshipChanges.class);

		boolean redundantFound = false;

		for (RelationshipChange relationshipChange : changes.getItems()) {
			ReasonerRelationship relationship = relationshipChange.getRelationship();
			assertEquals(Concepts.PART_OF, relationship.getTypeId());
			assertEquals(Concepts.NAMESPACE_ROOT, relationship.getDestinationId());

			if (ChangeNature.REDUNDANT.equals(relationshipChange.getChangeNature())) {
				assertFalse("Two redundant relationships found in response.", redundantFound);
				assertEquals(5, (int) relationship.getGroup());
				assertEquals(conceptId, relationship.getSourceId());
				redundantFound = true;
			} else {
				assertEquals(0, (int) relationship.getGroup());
			}
		}

		if (USE_EXTERNAL_SERVICE) { // apparently the external classification service considers the above scenario to be non-redundant
			assertFalse("There are redundant relationships found in response.", redundantFound);
		} else {
			assertTrue("No redundant relationships found in response.", redundantFound);
		}
		
	}
	
	@Test
	public void issue_APDS_327_testNoStatedRedundantRelationshipChanges() throws Exception {
		
		String conceptId = createNewConcept(branchPath);
		
		createNewRelationship(branchPath, conceptId, Concepts.IS_A, Concepts.ROOT_CONCEPT, CharacteristicType.INFERRED_RELATIONSHIP);
		
		// group 1
		createNewRelationship(branchPath, conceptId, ACCESS, BLADDER_FILLING_TECHNIQUE, CharacteristicType.STATED_RELATIONSHIP, 1);
		createNewRelationship(branchPath, conceptId, ASSOCIATED_FINDING, POST_PROCESSING, CharacteristicType.STATED_RELATIONSHIP, 1);
		
		createNewRelationship(branchPath, conceptId, ACCESS, BLADDER_FILLING_TECHNIQUE, CharacteristicType.INFERRED_RELATIONSHIP, 1);
		createNewRelationship(branchPath, conceptId, ASSOCIATED_FINDING, PROVOCATION_TECHNIQUE, CharacteristicType.INFERRED_RELATIONSHIP, 1);
		
		// group 2
		String relationshipId = createNewRelationship(branchPath, conceptId, ACCESS, BLADDER_FILLING_TECHNIQUE, CharacteristicType.STATED_RELATIONSHIP, 2);
		createNewRelationship(branchPath, conceptId, ASSOCIATED_FINDING, PROVOCATION_TECHNIQUE, CharacteristicType.STATED_RELATIONSHIP, 2);
		
		createNewRelationship(branchPath, conceptId, ACCESS, BLADDER_FILLING_TECHNIQUE, CharacteristicType.INFERRED_RELATIONSHIP, 2);
		createNewRelationship(branchPath, conceptId, ASSOCIATED_FINDING, POST_PROCESSING, CharacteristicType.INFERRED_RELATIONSHIP, 2);
		
		String classificationId = getClassificationJobId(beginClassification(branchPath, REASONER_ID, USE_EXTERNAL_SERVICE));
		waitForClassificationJob(branchPath, classificationId)
			.statusCode(200)
			.body("status", equalTo(ClassificationStatus.COMPLETED.name()));

		RelationshipChanges changes = MAPPER.readValue(getRelationshipChanges(branchPath, classificationId)
				.statusCode(200)
				.extract()
				.asInputStream(), RelationshipChanges.class);

		assertTrue(changes.isEmpty());
		
		inactivateRelationship(branchPath, relationshipId);
		getComponent(branchPath, SnomedComponentType.RELATIONSHIP, relationshipId)
			.statusCode(200)
			.body("active", equalTo(false));
		
		String secondClassificationId = getClassificationJobId(beginClassification(branchPath, REASONER_ID, USE_EXTERNAL_SERVICE));
		waitForClassificationJob(branchPath, secondClassificationId)
			.statusCode(200)
			.body("status", equalTo(ClassificationStatus.COMPLETED.name()));

		RelationshipChanges secondChanges = MAPPER.readValue(getRelationshipChanges(branchPath, secondClassificationId)
				.statusCode(200)
				.extract()
				.asInputStream(), RelationshipChanges.class);
		
		for (RelationshipChange relationshipChange : secondChanges) {
			if (ChangeNature.REDUNDANT.equals(relationshipChange.getChangeNature())) {
				getComponent(branchPath, SnomedComponentType.RELATIONSHIP, relationshipChange.getRelationship().getOriginId())
					.statusCode(200)
					.body("characteristicType", equalTo(CharacteristicType.INFERRED_RELATIONSHIP.name()));
			}
		}
	}

	@Test
	public void issue_SO_1830_testInferredEquivalentConceptParents() throws Exception {
		
		String parentConceptId = createNewConcept(branchPath);
		String childConceptId = createNewConcept(branchPath, parentConceptId);
		String equivalentConceptId = createNewConcept(branchPath, parentConceptId);

		changeToDefining(branchPath, equivalentConceptId);
		
		String classificationId = getClassificationJobId(beginClassification(branchPath, REASONER_ID, USE_EXTERNAL_SERVICE));
		waitForClassificationJob(branchPath, classificationId)
			.statusCode(200)
			.body("status", equalTo(ClassificationStatus.COMPLETED.name()));

		/* 
		 * Expecting that childConceptId will get two inferred IS A-s pointing to parentConceptId and equivalentConceptId, respectively, 
		 * while parentConceptId and equivalentConceptId each will get a single inferred IS A pointing to the root concept.
		 */
		RelationshipChanges changes = MAPPER.readValue(getRelationshipChanges(branchPath, classificationId).statusCode(200)
				.extract()
				.asInputStream(), RelationshipChanges.class);

		FluentIterable<RelationshipChange> changesIterable = FluentIterable.from(changes.getItems());
		
		// inference of IS A relationships when equivalent concepts are present differs between internal and external service
		if (USE_EXTERNAL_SERVICE) {
			
			assertEquals(3, changes.getTotal());
			assertTrue("All changes should be inferred.", changesIterable.allMatch(relationshipChange -> ChangeNature.INFERRED.equals(relationshipChange.getChangeNature())));
			
			assertInferredIsAExists(changesIterable, childConceptId, equivalentConceptId);
			assertInferredIsAExists(changesIterable, parentConceptId, equivalentConceptId);
			assertInferredIsAExists(changesIterable, equivalentConceptId, Concepts.ROOT_CONCEPT);
			
		} else {
			
			assertEquals(4, changes.getTotal());
			assertTrue("All changes should be inferred.", changesIterable.allMatch(relationshipChange -> ChangeNature.INFERRED.equals(relationshipChange.getChangeNature())));
			
			assertInferredIsAExists(changesIterable, childConceptId, parentConceptId);
			assertInferredIsAExists(changesIterable, childConceptId, equivalentConceptId);
			assertInferredIsAExists(changesIterable, parentConceptId, Concepts.ROOT_CONCEPT);
			assertInferredIsAExists(changesIterable, equivalentConceptId, Concepts.ROOT_CONCEPT);
			
		}
		
		EquivalentConceptSets equivalentConceptSets = MAPPER.readValue(getEquivalentConceptSets(branchPath, classificationId).statusCode(200)
				.extract()
				.asInputStream(), EquivalentConceptSets.class);
		
		assertEquals(1, equivalentConceptSets.getItems().size());
		
		SnomedConcepts equivalentConceptsInFirstSet = equivalentConceptSets.first().get().getEquivalentConcepts();
		FluentIterable<SnomedConcept> equivalentConceptsIterable = FluentIterable.from(equivalentConceptsInFirstSet);
		
		assertEquals(2, equivalentConceptsInFirstSet.getTotal());
		assertEquivalentConceptPresent(equivalentConceptsIterable, parentConceptId);
		assertEquivalentConceptPresent(equivalentConceptsIterable, equivalentConceptId);
	}
	
	@Test
	public void testRelationshipChangesGetResponse() throws Exception {
		
		String classificationId1 = getClassificationJobId(beginClassification(branchPath, REASONER_ID, USE_EXTERNAL_SERVICE));
		waitForClassificationJob(branchPath, classificationId1)
			.statusCode(200)
			.body("status", equalTo(ClassificationStatus.COMPLETED.name()));
		
		beginClassificationSave(branchPath, classificationId1);
		waitForClassificationSaveJob(branchPath, classificationId1)
			.statusCode(200)
			.body("status", equalTo(ClassificationStatus.SAVED.name()));
		
		String sourceConcept = createNewConcept(branchPath);
		
		String classificationId2 = getClassificationJobId(beginClassification(branchPath, REASONER_ID, USE_EXTERNAL_SERVICE));
		waitForClassificationJob(branchPath, classificationId2)
			.statusCode(200)
			.body("status", equalTo(ClassificationStatus.COMPLETED.name()));
		
		ValidatableResponse response = givenAuthenticatedRequest(SnomedApiTestConstants.SCT_API)
				.accept(CONTENT_TYPE_TXT_CSV)
				.when().get("/{path}/classifications/{classificationId}/relationship-changes?expand=source.fsn,type.fsn,destination.fsn", branchPath.getPath(), classificationId2)
				.then()
				.statusCode(200);
		String responseString = response.extract().asString();
		Set<String> responseRows = Sets.newHashSet(Splitter.on('\n').split(responseString)).stream().filter(row -> !Strings.isNullOrEmpty(row)).collect(Collectors.toSet());
		
		String inferredNotStated = USE_EXTERNAL_SERVICE ? "false" : "true";
		
		Set<String> expectedRows = Sets.newHashSet(
				String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s", "changeNature", "sourceId", "sourceFsn", "typeId", "typeFsn",
						"destinationId", "destinationFsn", "destinationNegated", "characteristicTypeId", "group", "id", "unionGroup", "modifier",
						"inferredNotStated"),
				String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s", "INFERRED", sourceConcept, "\"FSN of concept\"", "116680003",
						"\"Is a (attribute)\"", "138875005", "\"SNOMED CT Concept (SNOMED RT+CTV3)\"", "false", "900000000000011006", "0", "", "0",
						"EXISTENTIAL", inferredNotStated));

		assertEquals(expectedRows, responseRows);
		
	}
	
	@Test
	public void testInferredRelationshipGroupChange() throws Exception {
		
		if (USE_EXTERNAL_SERVICE) {
			
			String diseaseId = "64572001";

			getComponent(branchPath, SnomedComponentType.CONCEPT, diseaseId).statusCode(200);
			getComponent(branchPath, SnomedComponentType.CONCEPT, Concepts.CAUSATIVE_AGENT).statusCode(200);
			
			// create axiom instead of stated relationship
			String owlAxiom = "SubClassOf(:64572001 ObjectIntersectionOf(:404684003 ObjectSomeValuesFrom(:609096000 ObjectSomeValuesFrom(:246075003 :370136006))))";
			
			Map<?, ?> requestBody = createRefSetMemberRequestBody(Concepts.REFSET_OWL_AXIOM, diseaseId)
					.put(SnomedRefSetMemberRestInput.ADDITIONAL_FIELDS, ImmutableMap.<String, Object>builder()
						.put(SnomedRf2Headers.FIELD_OWL_EXPRESSION, owlAxiom)
						.build())
					.put("commitComment", "Created new OWL Axiom reference set member for Disease")
					.build();

			String memberId = lastPathSegment(createComponent(branchPath, SnomedComponentType.MEMBER, requestBody)
					.statusCode(201)
					.extract().header("Location"));

			getComponent(branchPath, SnomedComponentType.MEMBER, memberId).statusCode(200);
			
			// create inferred relationship in group 0
			String relationshipId = createNewRelationship(branchPath, diseaseId, Concepts.CAUSATIVE_AGENT,
					Concepts.NAMESPACE_ROOT, CharacteristicType.INFERRED_RELATIONSHIP);
			
			getComponent(branchPath, SnomedComponentType.RELATIONSHIP, relationshipId)
				.statusCode(200)
				.body("group", equalTo(0));
			
			// classify
			String classificationId = getClassificationJobId(beginClassification(branchPath, REASONER_ID, USE_EXTERNAL_SERVICE));
			
			waitForClassificationJob(branchPath, classificationId)
				.statusCode(200)
				.body("status", equalTo(ClassificationStatus.COMPLETED.name()));

			RelationshipChanges changes = MAPPER.readValue(getRelationshipChanges(branchPath, classificationId).statusCode(200)
					.extract()
					.asInputStream(), RelationshipChanges.class);
			
			assertEquals(1, changes.getTotal());
			RelationshipChange relationshipChange = changes.getItems().stream().findFirst().get();
			
			assertEquals(ChangeNature.INFERRED, relationshipChange.getChangeNature());
			assertEquals(relationshipId, relationshipChange.getRelationship().getOriginId());
			assertEquals(diseaseId, relationshipChange.getRelationship().getSourceId());
			assertEquals(Concepts.CAUSATIVE_AGENT, relationshipChange.getRelationship().getTypeId());
			assertEquals(Concepts.NAMESPACE_ROOT, relationshipChange.getRelationship().getDestinationId());
			assertEquals(1, relationshipChange.getRelationship().getGroup().intValue());
			
			beginClassificationSave(branchPath, classificationId);
			waitForClassificationSaveJob(branchPath, classificationId)
				.statusCode(200)
				.body("status", equalTo(ClassificationStatus.SAVED.name()));

			getComponent(branchPath, SnomedComponentType.RELATIONSHIP, relationshipId)
				.statusCode(200)
				.body("group", equalTo(1));
			
		}
		
	}

	private static void assertInferredIsAExists(FluentIterable<RelationshipChange> changesIterable, String childConceptId, String parentConceptId) {
		assertTrue("Inferred IS A between " + childConceptId + " and " + parentConceptId + " not found.", 
				changesIterable.anyMatch(relationshipChange -> {
					final ReasonerRelationship relationship = relationshipChange.getRelationship();
					return Concepts.IS_A.equals(relationship.getTypeId())
							&& childConceptId.equals(relationship.getSourceId())
							&& parentConceptId.equals(relationship.getDestinationId())
							&& relationship.getGroup() == 0
							&& relationship.getUnionGroup() == 0
							&& RelationshipModifier.EXISTENTIAL.equals(relationship.getModifier())
							&& CharacteristicType.INFERRED_RELATIONSHIP.equals(relationship.getCharacteristicType());
				}));
	}
	
	private static void assertEquivalentConceptPresent(FluentIterable<SnomedConcept> equivalentConceptsIterable, String conceptId) {
		assertTrue("Equivalent concept with ID " + conceptId + " not found in set.", 
				equivalentConceptsIterable.anyMatch(equivalentConcept -> conceptId.equals(equivalentConcept.getId())));
	}
}
