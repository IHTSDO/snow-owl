/*******************************************************************************
 * Copyright (c) 2019 B2i Healthcare. All rights reserved.
 *******************************************************************************/
package scripts

import com.b2international.snowowl.core.ApplicationContext
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
import com.b2international.snowowl.snomed.core.domain.DescriptionInactivationIndicator
import com.b2international.snowowl.snomed.core.domain.SnomedConcept
import com.b2international.snowowl.snomed.core.domain.SnomedConcepts
import com.b2international.snowowl.snomed.core.domain.SnomedDescription
import com.b2international.snowowl.snomed.core.domain.SnomedDescriptions
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


def branchPath = "MAIN/2019-07-31/SNOMEDCT-BE/BESEP19/BESEP19-TEST"
def dryRun = true


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

def createReport = { Set<String> ids, String fileName ->
	def destinationFile = new File(System.getProperty("user.home") + "/" + fileName + "_" + new Date().format("yyyyMMdd_HHmmss") + ".txt")
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

def branch = RepositoryRequests.branching()
	.prepareGet(branchPath)
	.build(repo)
	.execute(bus)
	.getSync()

def moduleId = BranchMetadataResolver.getEffectiveBranchMetadataValue(branch, SnomedCoreConfiguration.BRANCH_DEFAULT_MODULE_ID_KEY)

log("Found default module ID for branch '${branchPath}' -> '${moduleId}'")

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

createScrollingIterator(descriptionSearchRequest, branchPath).each { SnomedDescriptions descriptionBatch ->
	
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

createScrollingIterator(languageMemberReq, branchPath).each { SnomedReferenceSetMembers languageMemberBatch ->
	
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
		.build(repo, branchPath)
		.get()
		
}

if (!activeDescriptionIds.isEmpty()) {
	createReport(activeDescriptionIds, "active_descriptions_wo_lang_member")
}

if (!inactiveDescriptionIdsWithoutIndicator.isEmpty()) {
	createReport(inactiveDescriptionIdsWithoutIndicator, "inactive_descriptions_wo_indicator")
}

if (!activeMembersWithInactiveDescription.isEmpty()) {
	createReport(activeMembersWithInactiveDescription, "active_members_w_inactive_description")
}

if (!activeDescriptionIdsOfInactiveConcepts.isEmpty()) {
	createReport(activeDescriptionIdsOfInactiveConcepts, "active_descriptions_w_missing_indicator")
}

