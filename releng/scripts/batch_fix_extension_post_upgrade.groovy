/*******************************************************************************
 * Copyright (c) 2019 B2i Healthcare. All rights reserved.
 *******************************************************************************/
package scripts

import java.util.stream.Collectors

import com.b2international.snowowl.core.ApplicationContext
import com.b2international.snowowl.core.branch.Branch
import com.b2international.snowowl.core.events.AsyncRequest
import com.b2international.snowowl.core.events.bulk.BulkRequest
import com.b2international.snowowl.core.request.SearchResourceRequestBuilder
import com.b2international.snowowl.core.request.SearchResourceRequestIterator
import com.b2international.snowowl.datastore.request.RepositoryRequests
import com.b2international.snowowl.eventbus.IEventBus
import com.b2international.snowowl.identity.domain.User
import com.b2international.snowowl.snomed.SnomedConstants.Concepts
import com.b2international.snowowl.snomed.common.SnomedRf2Headers
import com.b2international.snowowl.snomed.core.domain.BranchMetadataResolver
import com.b2international.snowowl.snomed.core.domain.CharacteristicType
import com.b2international.snowowl.snomed.core.domain.DescriptionInactivationIndicator
import com.b2international.snowowl.snomed.core.domain.RelationshipModifier
import com.b2international.snowowl.snomed.core.domain.SnomedConcept
import com.b2international.snowowl.snomed.core.domain.SnomedConcepts
import com.b2international.snowowl.snomed.core.domain.SnomedDescription
import com.b2international.snowowl.snomed.core.domain.SnomedDescriptions
import com.b2international.snowowl.snomed.core.domain.SnomedRelationship
import com.b2international.snowowl.snomed.core.domain.refset.SnomedReferenceSetMember
import com.b2international.snowowl.snomed.core.domain.refset.SnomedReferenceSetMembers
import com.b2international.snowowl.snomed.datastore.SnomedDatastoreActivator
import com.b2international.snowowl.snomed.datastore.config.SnomedCoreConfiguration
import com.b2international.snowowl.snomed.datastore.index.entry.SnomedConceptDocument
import com.b2international.snowowl.snomed.datastore.index.entry.SnomedRefSetMemberIndexEntry
import com.b2international.snowowl.snomed.datastore.request.SnomedConceptSearchRequestBuilder
import com.b2international.snowowl.snomed.datastore.request.SnomedDescriptionSearchRequestBuilder
import com.b2international.snowowl.snomed.datastore.request.SnomedRefSetMemberSearchRequestBuilder
import com.b2international.snowowl.snomed.datastore.request.SnomedRequests
import com.b2international.snowowl.snomed.snomedrefset.SnomedRefSetType
import com.google.common.base.Splitter

def branches = [ 
		"MAIN/2020-01-31/SNOMEDCT-US/USMAR20/USMAR20-10",
		"MAIN/2020-01-31/SNOMEDCT-DK/DKMAR20/DKMAR20-7"
	]

def dryRun = true

def useAutoReplacementForType = true
def useAutoReplacementForSource = true
def useAutoReplacementForDestination = true

def BATCH_SIZE = 20000
def SCROLL_KEEPALIVE = "1h"
def repo = SnomedDatastoreActivator.REPOSITORY_UUID
def bus = ApplicationContext.getServiceForClass(IEventBus.class)

def log = { message ->
	print "[${new Date().format("YYYY-MM-dd HH:mm:ss")}] "
	println message
}

def hasConceptNonCurrentIndicator = { SnomedDescription description ->
	
	return description.getMembers().any { SnomedReferenceSetMember member -> 
		
		if (member.isActive() && Concepts.REFSET_DESCRIPTION_INACTIVITY_INDICATOR.equals(member.getReferenceSetId())) {
			def valueId = member.getProperties().get(SnomedRf2Headers.FIELD_VALUE_ID)
			return DescriptionInactivationIndicator.CONCEPT_NON_CURRENT.getConceptId().equals(valueId)
		}
		
		return false
	}
	
}

def hasInactivationIndicator = { SnomedDescription description ->
	
	return description.getMembers().any { SnomedReferenceSetMember member ->
		member.isActive() && Concepts.REFSET_DESCRIPTION_INACTIVITY_INDICATOR.equals(member.getReferenceSetId())
	}
	
}

def createReport = { Collection<String> ids, String fileName, Branch branch ->
	def branchId = Splitter.on(Branch.SEPARATOR).splitToList(branch.path()).last()
	def destinationFile = new File(System.getProperty("user.home") + "/" + fileName + "_" + branchId + "_" + new Date().format("yyyyMMdd_HHmmss") + ".txt")
	def writer = new FileWriter(destinationFile)
	ids.each { writer.append(it + '\r\n') }
	writer.flush()
	writer.close()
	log("Report file is available @ '$destinationFile.absolutePath'.")
}

def createScrollingIterator(SearchResourceRequestBuilder requestBuilder, String branchPath) {
	return new SearchResourceRequestIterator(requestBuilder, { scrolledBuilder ->
		AsyncRequest scrolledRequest = scrolledBuilder.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
		return scrolledRequest
			.execute(ApplicationContext.getServiceForClass(IEventBus.class))
			.getSync()
	})
}

def validateDescriptionsAndLanguageRefsetMembers = { Branch branch, String moduleId, Set<String> inactiveConceptIds ->
	
	def activeDescriptionIds = [] as Set
	def inactiveDescriptionIds = [] as Set
	def activeDescriptionIdsOfInactiveConcepts = [] as Set
	def inactiveDescriptionIdsWithoutIndicator = [] as Set
	
	def SnomedDescriptionSearchRequestBuilder descriptionSearchRequest = SnomedRequests.prepareSearchDescription()
		.filterByModule(moduleId)
		.setExpand("members(active:true)")
		.setLimit(BATCH_SIZE)
		.setScroll(SCROLL_KEEPALIVE)
	
	def dcounter = 0
	
	log("Collecting all active or inactive description IDs in module '${moduleId}'...")
	
	createScrollingIterator(descriptionSearchRequest, branch.path()).each { SnomedDescriptions descriptionBatch ->
		
		dcounter+=descriptionBatch.items.size()
		
		log("Processing batch ${descriptionBatch.items.size()} / ${dcounter} of ${descriptionBatch.getTotal()} descriptions")
		
		descriptionBatch.each { SnomedDescription description ->
			if (description.isActive()) {
				activeDescriptionIds.add(description.getId())
				if (inactiveConceptIds.contains(description.getConceptId()) && !hasConceptNonCurrentIndicator(description)) {
					activeDescriptionIdsOfInactiveConcepts.add(description.getId())
				}
			} else {
				inactiveDescriptionIds.add(description.getId())
				if (!hasInactivationIndicator(description)) {
					inactiveDescriptionIdsWithoutIndicator.add(description.getId())
				}
			}
		}
		
	}
	
	log("Found ${activeDescriptionIds.size()} active descriptions in module '${moduleId}'")
	log("Found ${inactiveDescriptionIds.size()} inactive descriptions in module '${moduleId}'")
	log("Found ${activeDescriptionIdsOfInactiveConcepts.size()} active descriptions where the referenced concept is inactive and has no 'Concept non-current' inactivation indicator")
	log("Found ${inactiveDescriptionIdsWithoutIndicator.size()} inactive descriptions where there is no inactivation indicator reference set member")
	
	def SnomedRefSetMemberSearchRequestBuilder languageMemberReq = SnomedRequests.prepareSearchMember()
			.filterByRefSetType([ SnomedRefSetType.LANGUAGE ])
			.filterByModule(moduleId)
			.filterByReferencedComponent(activeDescriptionIds + inactiveDescriptionIds)
			.filterByActive(true)
			.setFields(SnomedRefSetMemberIndexEntry.Fields.ID, SnomedRefSetMemberIndexEntry.Fields.REFERENCED_COMPONENT_ID, SnomedRefSetMemberIndexEntry.Fields.REFERENCED_COMPONENT_TYPE)
			.setLimit(BATCH_SIZE)
			.setScroll(SCROLL_KEEPALIVE)
	
	def activeMembersWithInactiveDescription = [] as Set
	
	def mcounter = 0
	
	createScrollingIterator(languageMemberReq, branch.path()).each { SnomedReferenceSetMembers languageMemberBatch ->
		
		mcounter+=languageMemberBatch.items.size()
		
		log("Processing batch ${languageMemberBatch.items.size()} / ${mcounter} of ${languageMemberBatch.getTotal()} language reference set members")
		
		languageMemberBatch.each { SnomedReferenceSetMember member ->
			if (inactiveDescriptionIds.contains(member.getReferencedComponent().getId())) {
				activeMembersWithInactiveDescription.add(member.getId())
			} else {
				activeDescriptionIds.remove(member.getReferencedComponent().getId())
			}
		}
		
	}
	
	log("Found ${activeMembersWithInactiveDescription.size()} active language reference set member referencing inactive descriptions in module '${moduleId}'")
	log("Found ${activeDescriptionIds.size()} active descriptions without any language reference set member in module '${moduleId}'")
	
	if (!activeDescriptionIdsOfInactiveConcepts.isEmpty() && !dryRun) {
		
		log("Add missing concept non-current indicators to active descriptions with inactive concept (${activeDescriptionIdsOfInactiveConcepts.size()})")
		
		def bulk = BulkRequest.create()
		
		activeDescriptionIdsOfInactiveConcepts.each { id ->
			bulk.add(SnomedRequests.prepareNewMember()
				.setActive(true)
				.setModuleId(moduleId)
				.setReferencedComponentId(id)
				.setReferenceSetId(Concepts.REFSET_DESCRIPTION_INACTIVITY_INDICATOR)
				.setProperties([(SnomedRf2Headers.FIELD_VALUE_ID) : (DescriptionInactivationIndicator.CONCEPT_NON_CURRENT.getConceptId())])
				.build())
		}
		
		SnomedRequests.prepareCommit()
			.setBody(bulk)
			.setUserId(User.SYSTEM.username)
			.setCommitComment("Add ${activeDescriptionIdsOfInactiveConcepts.size()} missing concept non-current indicators to extension descriptions")
			.build(repo, branch.path())
			.get()
			
	}
	
	if (!activeDescriptionIds.isEmpty()) {
		createReport(activeDescriptionIds, "active_descriptions_wo_lang_member", branch)
	}
	
	if (!inactiveDescriptionIdsWithoutIndicator.isEmpty()) {
		createReport(inactiveDescriptionIdsWithoutIndicator, "inactive_descriptions_wo_indicator", branch)
	}
	
	if (!activeMembersWithInactiveDescription.isEmpty()) {
		createReport(activeMembersWithInactiveDescription, "active_members_w_inactive_description", branch)
	}
	
	if (!activeDescriptionIdsOfInactiveConcepts.isEmpty()) {
		createReport(activeDescriptionIdsOfInactiveConcepts, "active_descriptions_w_missing_indicator", branch)
	}
	
}

def fixInactiveRelationshipTypes = { Branch branch, String moduleId, String namespace, Set<String> inactiveConceptIds ->
	
	def typeIds = [] as Set
	def relationshipIdsToInactivate = [] as Set
	def relationshipIdsToReplace = ["oldId\tcharacteristicType\tsourceId\tsourceFsn\toldTypeId\toldTypeFsn\tnewTypeId\tnewTypeFsn\tdesinationId\tdestinationFsn"]
	
	def activeRelationshipsWithInactiveType = SnomedRequests.prepareSearchRelationship()
		.all()
		.filterByType(inactiveConceptIds)
		.filterByModule(moduleId)
		.filterByActive(true)
		.setExpand("source(expand(fsn())),type(expand(fsn())),destination(expand(fsn()))")
		.build(repo, branch.path())
		.execute(bus)
		.getSync()
		.stream()
		.peek { SnomedRelationship r -> 
			typeIds.add(r.getTypeId())
			relationshipIdsToInactivate.add(r.getId())
		}
		.collect(Collectors.toList())
	
	log("Found ${activeRelationshipsWithInactiveType.size()} active relationships where the type concept is inactive on branch '${branch.path()}'")
	
	if (!typeIds.isEmpty()) {
		
		def typeToReplacementMap = [:]
		
		SnomedRequests.prepareSearchMember()
			.all()
			.filterByRefSet([Concepts.REFSET_REPLACED_BY_ASSOCIATION, Concepts.REFSET_SAME_AS_ASSOCIATION])
			.filterByActive(true)
			.filterByReferencedComponent(typeIds)
			.build(repo, branch.path())
			.execute(bus)
			.getSync()
			.stream()
			.each{ SnomedReferenceSetMember m ->
				typeToReplacementMap.put(m.getReferencedComponent().getId(), m.getProperties().get(SnomedRf2Headers.FIELD_TARGET_COMPONENT).getId())
			}
		
		def conceptIdsToFetch = typeToReplacementMap.keySet() + typeToReplacementMap.values()
		def idToFsnMap = [:]
		
		SnomedRequests.prepareSearchConcept()
			.filterByIds(conceptIdsToFetch)
			.setLimit(conceptIdsToFetch.size())
			.setExpand("fsn()")
			.build(repo, branch.path())
			.execute(bus)
			.getSync()
			.each { SnomedConcept c -> idToFsnMap.put(c.getId(), c.getFsn().getTerm()) }
		
		typeToReplacementMap.each{ k,v ->
			log("Relationship type '${k} | ${idToFsnMap.get(k)}' has a replacement '${v} | ${idToFsnMap.get(v)}'")
		}
		
		if (!dryRun) {
			
			def bulk = BulkRequest.create()
			
			activeRelationshipsWithInactiveType.each{ SnomedRelationship r ->
				
				bulk.add(SnomedRequests.prepareUpdateRelationship(r.getId()).setActive(false))
					
				if (useAutoReplacementForType && typeToReplacementMap.containsKey(r.getTypeId())) {
					
					def newTypeId = typeToReplacementMap.get(r.getTypeId())
					def characteristicType = CharacteristicType.getByConceptId(r.getCharacteristicTypeId())
					
					bulk.add(SnomedRequests.prepareNewRelationship()
						.setActive(r.isActive())
						.setCharacteristicType(characteristicType)
						.setDestinationId(r.getDestinationId())
						.setDestinationNegated(r.isDestinationNegated())
						.setGroup(r.getGroup())
						.setIdFromNamespace(namespace)
						.setModifier(RelationshipModifier.getByConceptId(r.getModifierId()))
						.setModuleId(r.getModuleId())
						.setSourceId(r.getSourceId())
						.setTypeId(newTypeId)
						.setUnionGroup(r.getUnionGroup()))
					
					relationshipIdsToReplace.add(
						"${r.getId()}\t${characteristicType}\t" + 
						"${r.getSourceId()}\t${r.getSource().getFsn().getTerm()}\t" +
						"${r.getTypeId()}\t${r.getType().getFsn().getTerm()}\t" +
						"${newTypeId}\t${idToFsnMap.get(newTypeId)}\t" +
						"${r.getDestinationId()}\t${r.getDestination().getFsn().getTerm()}"
					)
					
				}
				
			}
			
			String replace = useAutoReplacementForType ? " / replaced" : ""
			
			SnomedRequests.prepareCommit()
				.setBody(bulk)
				.setCommitComment("Inactivated${replace} ${relationshipIdsToInactivate.size()} relationships with inactive type concept")
				.setUserId(User.SYSTEM.username)
				.build(repo, branch.path())
				.get()
				
		}
		
	}
	
	if (!relationshipIdsToInactivate.isEmpty()) {
		createReport(relationshipIdsToInactivate, "active_relationships_w_inactive_type", branch)
	}
	
	if (relationshipIdsToReplace.size > 1) {
		createReport(relationshipIdsToReplace, "active_relationships_w_inactive_type_replaced", branch)
	}
	
}

def fixInactiveRelationshipSources = {  Branch branch, String moduleId, String namespace, Set<String> inactiveConceptIds ->
	
	def sourceIds = [] as Set
	def relationshipIdsToInactivate = [] as Set
	def relationshipIdsToReplace = ["oldId\tcharacteristicType\toldSourceId\tOldSourceFsn\tnewSourceId\tnewSourceFsn\ttypeId\ttypeFsn\tdesinationId\tdestinationFsn"]
	
	def activeRelationshipsWithInactiveSource = SnomedRequests.prepareSearchRelationship()
		.all()
		.filterBySource(inactiveConceptIds)
		.filterByActive(true)
		.filterByModule(moduleId)
		.setExpand("source(expand(fsn())),type(expand(fsn())),destination(expand(fsn()))")
		.build(repo, branch.path())
		.execute(bus)
		.getSync()
		.stream()
		.peek { SnomedRelationship r -> 
			sourceIds.add(r.getSourceId())
			relationshipIdsToInactivate.add(r.getId())
		}
		.collect(Collectors.toList())
	
	log("Found ${activeRelationshipsWithInactiveSource.size()} active relationships where the source concept is inactive on branch '${branch.path()}'")
	
	if (!sourceIds.isEmpty()) {
		
		def sourceToReplacementMap = [:]
		
		SnomedRequests.prepareSearchMember()
			.all()
			.filterByRefSet([Concepts.REFSET_REPLACED_BY_ASSOCIATION, Concepts.REFSET_SAME_AS_ASSOCIATION])
			.filterByActive(true)
			.filterByReferencedComponent(sourceIds)
			.build(repo, branch.path())
			.execute(bus)
			.getSync()
			.stream()
			.each{ SnomedReferenceSetMember m ->
				sourceToReplacementMap.put(m.getReferencedComponent().getId(), m.getProperties().get(SnomedRf2Headers.FIELD_TARGET_COMPONENT).getId())
			}
		
		def conceptIdsToFetch = sourceToReplacementMap.keySet() + sourceToReplacementMap.values()
		def idToFsnMap = [:]
		
		SnomedRequests.prepareSearchConcept()
			.filterByIds(conceptIdsToFetch)
			.setLimit(conceptIdsToFetch.size())
			.setExpand("fsn()")
			.build(repo, branch.path())
			.execute(bus)
			.getSync()
			.each { SnomedConcept c -> idToFsnMap.put(c.getId(), c.getFsn().getTerm()) }
		
		sourceToReplacementMap.each{ k,v ->
			log("Relationship source '${k} | ${idToFsnMap.get(k)}' has a replacement '${v} | ${idToFsnMap.get(v)}'")
		}
		
		if (!dryRun) {
			
			def bulk = BulkRequest.create()
			
			activeRelationshipsWithInactiveSource.each{ SnomedRelationship r ->
				
				bulk.add(SnomedRequests.prepareUpdateRelationship(r.getId()).setActive(false))
				
				if (useAutoReplacementForSource && sourceToReplacementMap.containsKey(r.getSourceId())) {
					
					def newSourceId = sourceToReplacementMap.get(r.getSourceId())
					def characteristicType = CharacteristicType.getByConceptId(r.getCharacteristicTypeId())
					
					bulk.add(SnomedRequests.prepareNewRelationship()
						.setActive(r.isActive())
						.setCharacteristicType(characteristicType)
						.setDestinationId(r.getDestinationId())
						.setDestinationNegated(r.isDestinationNegated())
						.setGroup(r.getGroup())
						.setIdFromNamespace(namespace)
						.setModifier(RelationshipModifier.getByConceptId(r.getModifierId()))
						.setModuleId(r.getModuleId())
						.setSourceId(newSourceId)
						.setTypeId(r.getTypeId())
						.setUnionGroup(r.getUnionGroup()))
					
					relationshipIdsToReplace.add(
						"${r.getId()}\t${characteristicType}\t" +
						"${r.getSourceId()}\t${r.getSource().getFsn().getTerm()}\t" +
						"${newSourceId}\t${idToFsnMap.get(newSourceId)}\t" +
						"${r.getTypeId()}\t${r.getType().getFsn().getTerm()}\t" +
						"${r.getDestinationId()}\t${r.getDestination().getFsn().getTerm()}"
					)
					
				}
				
			}
	
			String replace = useAutoReplacementForSource ? " / replaced" : ""
			
			SnomedRequests.prepareCommit()
				.setBody(bulk)
				.setCommitComment("Inactivated${replace} ${relationshipIdsToInactivate.size()} relationships with inactive source concept")
				.setUserId(User.SYSTEM.username)
				.build(repo, branch.path())
				.get()
			
		}
	
	}
	
	if (!relationshipIdsToInactivate.isEmpty()) {
		createReport(relationshipIdsToInactivate, "active_relationships_w_inactive_source", branch)
	}
	
	if (relationshipIdsToReplace.size > 1) {
		createReport(relationshipIdsToReplace, "active_relationships_w_inactive_source_replaced", branch)
	}
	
}

def fixInactiveRelationshipDestinations = {  Branch branch, String moduleId, String namespace, Set<String> inactiveConceptIds ->
	
	def destinationIds = [] as Set
	def relationshipIdsToInactivate = [] as Set
	def relationshipIdsToReplace = ["oldId\tcharacteristicType\tsourceId\tsourceFsn\ttypeId\ttypeFsn\toldDesinationId\toldDestinationFsn\tnewDestinationId\tnewDestinationFsn"]
	
	def activeRelationshipsWithInactiveDestination = SnomedRequests.prepareSearchRelationship()
		.all()
		.filterByDestination(inactiveConceptIds)
		.filterByActive(true)
		.filterByModule(moduleId)
		.setExpand("source(expand(fsn())),type(expand(fsn())),destination(expand(fsn()))")
		.build(repo, branch.path())
		.execute(bus)
		.getSync()
		.stream()
		.peek { SnomedRelationship r -> 
			destinationIds.add(r.getDestinationId())
			relationshipIdsToInactivate.add(r.getId())
		}
		.collect(Collectors.toList())
	
	log("Found ${activeRelationshipsWithInactiveDestination.size()} active relationships where the destination concept is inactive on branch '${branch.path()}'")
	
	if (!destinationIds.isEmpty()) {
		
		def destinationToReplacementMap = [:]
		
		SnomedRequests.prepareSearchMember()
			.all()
			.filterByRefSet([Concepts.REFSET_REPLACED_BY_ASSOCIATION, Concepts.REFSET_SAME_AS_ASSOCIATION])
			.filterByActive(true)
			.filterByReferencedComponent(destinationIds)
			.build(repo, branch.path())
			.execute(bus)
			.getSync()
			.stream()
			.each{ SnomedReferenceSetMember m ->
				destinationToReplacementMap.put(m.getReferencedComponent().getId(), m.getProperties().get(SnomedRf2Headers.FIELD_TARGET_COMPONENT).getId())
			}
		
		def conceptIdsToFetch = destinationToReplacementMap.keySet() + destinationToReplacementMap.values()
		def idToFsnMap = [:]
		
		SnomedRequests.prepareSearchConcept()
			.filterByIds(conceptIdsToFetch)
			.setLimit(conceptIdsToFetch.size())
			.setExpand("fsn()")
			.build(repo, branch.path())
			.execute(bus)
			.getSync()
			.each { SnomedConcept c -> idToFsnMap.put(c.getId(), c.getFsn().getTerm()) }
		
		destinationToReplacementMap.each{ k,v ->
			log("Relationship destination '${k} | ${idToFsnMap.get(k)}' has a replacement '${v} | ${idToFsnMap.get(v)}'")
		}
		
		if (!dryRun) {
			
			def bulk = BulkRequest.create()
					
			activeRelationshipsWithInactiveDestination.each{ SnomedRelationship r ->
			
				bulk.add(SnomedRequests.prepareUpdateRelationship(r.getId()).setActive(false))
				
				if (useAutoReplacementForDestination && destinationToReplacementMap.containsKey(r.getDestinationId())) {
					
					def newDestinationId = destinationToReplacementMap.get(r.getDestinationId())
					def characteristicType = CharacteristicType.getByConceptId(r.getCharacteristicTypeId())
					
					bulk.add(SnomedRequests.prepareNewRelationship()
							.setActive(r.isActive())
							.setCharacteristicType(characteristicType)
							.setDestinationId(newDestinationId)
							.setDestinationNegated(r.isDestinationNegated())
							.setGroup(r.getGroup())
							.setIdFromNamespace(namespace)
							.setModifier(RelationshipModifier.getByConceptId(r.getModifierId()))
							.setModuleId(r.getModuleId())
							.setSourceId(r.getSourceId())
							.setTypeId(r.getTypeId())
							.setUnionGroup(r.getUnionGroup()))
							
					relationshipIdsToReplace.add(
						"${r.getId()}\t${characteristicType}\t" +
						"${r.getSourceId()}\t${r.getSource().getFsn().getTerm()}\t" +
						"${r.getTypeId()}\t${r.getType().getFsn().getTerm()}\t" +
						"${r.getDestinationId()}\t${r.getDestination().getFsn().getTerm()}\t" +
						"${newDestinationId}\t${idToFsnMap.get(newDestinationId)}"
					)
							
				}
					
			}
			
			String replace = useAutoReplacementForDestination ? " / replaced" : ""
			
			SnomedRequests.prepareCommit()
				.setBody(bulk)
				.setCommitComment("Inactivated${replace} ${relationshipIdsToInactivate.size()} relationships with inactive destination concept")
				.setUserId(User.SYSTEM.username)
				.build(repo, branch.path())
				.get()
			
		}
	
	}
	
	if (!relationshipIdsToInactivate.isEmpty()) {
		createReport(relationshipIdsToInactivate, "active_relationships_w_inactive_destination", branch)
	}
	
	if (relationshipIdsToReplace.size > 1) {
		createReport(relationshipIdsToReplace, "active_relationships_w_inactive_destination_replaced", branch)
	}
	
}

def validateRelationshipReferences = { Branch branch, String moduleId, String namespace, Set<String> inactiveConceptIds -> 
	
	fixInactiveRelationshipTypes(branch, moduleId, namespace, inactiveConceptIds)
	fixInactiveRelationshipSources(branch, moduleId, namespace, inactiveConceptIds)
	fixInactiveRelationshipDestinations(branch, moduleId, namespace, inactiveConceptIds)
	
}

branches.each { branchPath ->
	
	log("Validating branch '${branchPath}'...")

	def branch = RepositoryRequests.branching()
		.prepareGet(branchPath)
		.build(repo)
		.execute(bus)
		.getSync()
		
	def moduleId = BranchMetadataResolver.getEffectiveBranchMetadataValue(branch, SnomedCoreConfiguration.BRANCH_DEFAULT_MODULE_ID_KEY)
	def namespace = BranchMetadataResolver.getEffectiveBranchMetadataValue(branch, SnomedCoreConfiguration.BRANCH_DEFAULT_NAMESPACE_KEY)
	
	log("Found default module ID for branch '${branchPath}' -> '${moduleId}'")
	log("Found default namespace for branch '${branchPath}' -> '${namespace}'")
	
	def inactiveConceptIds = [] as Set
	
	def SnomedConceptSearchRequestBuilder conceptSearchRequest = SnomedRequests.prepareSearchConcept()
		.filterByActive(false)
		.setFields(SnomedConceptDocument.Fields.ID)
		.setLimit(BATCH_SIZE)
		.setScroll(SCROLL_KEEPALIVE)
	
	def counter = 0
	
	log("Collecting all inactive concept IDs...")
	
	createScrollingIterator(conceptSearchRequest, branchPath).each { SnomedConcepts conceptBatch ->
		
		counter+=conceptBatch.items.size()
		
		log("Processing batch ${conceptBatch.items.size()} / ${counter} of ${conceptBatch.getTotal()} inactive concepts")
		
		conceptBatch.each { SnomedConcept concept ->
			inactiveConceptIds.add(concept.getId())
		}
		
	}
	
	validateDescriptionsAndLanguageRefsetMembers(branch, moduleId, inactiveConceptIds)
	validateRelationshipReferences(branch, moduleId, namespace, inactiveConceptIds)
	
}
