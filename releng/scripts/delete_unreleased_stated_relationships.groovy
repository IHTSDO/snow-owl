package scripts

import com.b2international.snowowl.core.ApplicationContext
import com.b2international.snowowl.core.events.bulk.BulkRequest
import com.b2international.snowowl.eventbus.IEventBus
import com.b2international.snowowl.snomed.SnomedConstants.Concepts
import com.b2international.snowowl.snomed.datastore.SnomedDatastoreActivator
import com.b2international.snowowl.snomed.datastore.index.entry.SnomedRelationshipIndexEntry
import com.b2international.snowowl.snomed.datastore.request.SnomedRequests

// Configurable parameters

def offset = 100

def branchesToFix = [ 
	"MAIN/2019-07-31/SNOMEDCT-EE/EEPRE19UAT/EEPRE19UAT-41",
	"MAIN/2019-07-31/SNOMEDCT-NO/NOOCT19UAT/NOOCT19UAT-2",
	"MAIN/2019-07-31/SNOMEDCT-CH/CHPRE19UAT/CHPRE19UAT-10" 
]

def bus = ApplicationContext.getServiceForClass(IEventBus)

def getExistingRelationshipIds = { branchPath, relationshipIds -> 
	
	return SnomedRequests.prepareSearchRelationship()
			.all()
			.filterByIds(relationshipIds)
			.setFields(SnomedRelationshipIndexEntry.Fields.ID)
			.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
			.execute(bus)
			.getSync()
			.getItems()
			.collect { relationship -> relationship.getId() }
			.toSet()
	
}

def deleteRelationshipsWithId = { branchPath, relationshipIds ->

	def relationshipBulk = BulkRequest.create()
	
	def existingRelationshipIds = getExistingRelationshipIds(branchPath, relationshipIds)
	
	if (!existingRelationshipIds.isEmpty()) {
		
		println("Removing ${existingRelationshipIds.size()} unreleased stated relationships...")
		
		existingRelationshipIds.each { id ->
			relationshipBulk.add(SnomedRequests.prepareDeleteRelationship(id).force(true).build())
		}
		
	} else {
		println("None of the specified relationship ids are present on branch: '${branchPath}'")
	}
	
	def relationshipBulkRequest = relationshipBulk.build()
	
	if (!relationshipBulkRequest.getRequests().isEmpty()) {
		
		SnomedRequests.prepareCommit()
			.setBody(relationshipBulkRequest)
			.setCommitComment("Deleting unreleased stated relationships from branch: '${branchPath}'")
			.setUserId("info@b2international.com")
			.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
			.execute(bus)
			.getSync()
			
	}
		
}

def getUnpublishedStatedRelationshipIds = { branchPath -> 
	
	SnomedRequests.prepareSearchRelationship()
		.all()
		.filterByReleased(false)
		.filterByCharacteristicType(Concepts.STATED_RELATIONSHIP)
		.setFields(SnomedRelationshipIndexEntry.Fields.ID)
		.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
		.execute(bus)
		.getSync()
		.getItems()
		.collect { relationship -> relationship.getId() }
		.toSet()
	
}

def deleteComponents = { branchPath ->
	
	def relationshipIds = getUnpublishedStatedRelationshipIds(branchPath)
	
	if (relationshipIds.isEmpty()) {
		println("There are no unreleased stated relationships on branch '${branchPath}'")
		return true
	}
	
	println("Deleting ${relationshipIds.size()} relationships in batches of ${offset} ...")
	
	relationshipIds.collate(offset).each { ids -> 
		deleteRelationshipsWithId(branchPath, ids)
	}
	
	def existingRelationshipIds = getExistingRelationshipIds(branchPath, relationshipIds)
	
	if (!existingRelationshipIds.isEmpty()) {
		
		existingRelationshipIds.each { id ->
			println("Failed to delete relationship with ID '${id}'")
		}
		
	}
	
	return existingRelationshipIds.isEmpty()
}

branchesToFix.each { branchPath ->
	
	println("Deleting unreleased stated relationships on branch '${branchPath}'")
	
	def deletionResult = deleteComponents(branchPath)
	
	if (deletionResult) {
		println("Unreleased stated relationship deletion finished successfully on branch '${branchPath}'")
	} else {
		println("Unreleased stated relationship deletion failures are present on branch '${branchPath}'")
	}
	
}
