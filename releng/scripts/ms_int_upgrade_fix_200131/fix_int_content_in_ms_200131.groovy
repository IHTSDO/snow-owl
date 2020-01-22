package scripts

import org.eclipse.core.runtime.Platform

import com.b2international.commons.BooleanUtils
import com.b2international.commons.ConsoleProgressMonitor
import com.b2international.snowowl.core.ApplicationContext
import com.b2international.snowowl.core.date.DateFormats
import com.b2international.snowowl.core.date.EffectiveTimes
import com.b2international.snowowl.core.events.bulk.BulkRequest
import com.b2international.snowowl.datastore.BranchPathUtils
import com.b2international.snowowl.eventbus.IEventBus
import com.b2international.snowowl.identity.domain.User
import com.b2international.snowowl.snomed.Relationship
import com.b2international.snowowl.snomed.SnomedConstants.Concepts
import com.b2international.snowowl.snomed.common.ContentSubType
import com.b2international.snowowl.snomed.common.SnomedTerminologyComponentConstants
import com.b2international.snowowl.snomed.core.domain.CharacteristicType
import com.b2international.snowowl.snomed.core.domain.RelationshipModifier
import com.b2international.snowowl.snomed.core.domain.SnomedRelationship
import com.b2international.snowowl.snomed.datastore.SnomedDatastoreActivator
import com.b2international.snowowl.snomed.datastore.SnomedEditingContext
import com.b2international.snowowl.snomed.datastore.index.entry.SnomedRefSetMemberIndexEntry
import com.b2international.snowowl.snomed.datastore.index.entry.SnomedRelationshipIndexEntry
import com.b2international.snowowl.snomed.datastore.request.SnomedRequests

// Configurable parameters

def offset = 3000
def branchesToFix = [ "MAIN" ]

def scriptLocation = "/opt/ms_upgrade/" // update the path if necessary

def relationshipIdsToDeletePath = scriptLocation + "relationship_ids_to_remove.txt"
def refsetMemberIdsToDeletePath = scriptLocation + "member_ids_to_remove.txt"
def relationshipsToUpdatePath = scriptLocation + "relationships_to_update.txt"

def rf2DeltaArchivePath = scriptLocation + "SnomedCT_RF2Release_INT_20200106_fixed_owl_filename.zip"



def bus = ApplicationContext.getServiceForClass(IEventBus)
def snomedImporterBundle = Platform.getBundle("com.b2international.snowowl.snomed.importer.rf2")
def importUtil = snomedImporterBundle.loadClass("com.b2international.snowowl.snomed.importer.rf2.util.ImportUtil").newInstance()

def getIdsFromFile = { filePath ->
	def ids = [] as Set
	new File(filePath).eachLine { line ->
		ids.add(line)
	}
	return ids.toList()
}

def getRF2LinesFromFile = { filePath ->
	def lines = []
	new File(filePath).splitEachLine("\t") { fields ->
		lines.add(fields)
	}
	return lines
}

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
		
		println("Removing ${existingRelationshipIds.size()} invalid relationships...")
		
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
			.setCommitComment("Deleting invalid relationships from branch: '${branchPath}'")
			.setUserId("info@b2international.com")
			.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
			.execute(bus)
			.getSync()
			
	}
		
}

def getExistingMemberIds = { branchPath, memberIds ->
	
	return SnomedRequests.prepareSearchMember()
			.all()
			.filterByIds(memberIds)
			.setFields(SnomedRefSetMemberIndexEntry.Fields.ID)
			.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
			.execute(bus)
			.getSync()
			.getItems()
			.collect { member -> member.getId() }
			.toSet()
	
}

def deleteMembersWithId = { branchPath, memberIds ->

	def memberBulk = BulkRequest.create();
	
	def existingMemberIds = getExistingMemberIds(branchPath, memberIds)
	
	if (!existingMemberIds.isEmpty()) {
		
		println("Removing ${existingMemberIds.size()} invalid reference set members...")
		
		existingMemberIds.each { id ->
			memberBulk.add(SnomedRequests.prepareDeleteMember(id).force(true).build())
		}
		
	} else {
		println("None of the specified reference set member ids are present on branch: '${branchPath}'")
	}
	
	def memberBulkRequest = memberBulk.build()
	
	if (!memberBulkRequest.getRequests().isEmpty()) {
		
		SnomedRequests.prepareCommit()
			.setBody(memberBulkRequest)
			.setCommitComment("Deleting invalid reference set members from branch: '${branchPath}'")
			.setUserId("info@b2international.com")
			.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
			.execute(bus)
			.getSync()
			
	}
	
}

def deleteComponents = { branchPath ->
	
	def relationshipIds = getIdsFromFile(relationshipIdsToDeletePath)
	
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
	
	def memberIds = getIdsFromFile(refsetMemberIdsToDeletePath)
	
	println("Deleting ${memberIds.size()} reference set members in batches of ${offset} ...")
	
	memberIds.collate(offset).each { ids ->
		deleteMembersWithId(branchPath, ids)
	}
	
	def existingMemberIds = getExistingMemberIds(branchPath, memberIds)
	
	if (!existingMemberIds.isEmpty()) {
		
		existingMemberIds.each { id ->
			println("Failed to delete reference set member with ID '${id}'")
		}
		
	}
	
	return existingRelationshipIds.isEmpty() && existingMemberIds.isEmpty()
}

def changeRelationshipsToUnpublished = { branchPath, lines ->
	
	def bulk = BulkRequest.create()
	
	lines.each { fields ->
		
		def id = fields[0]
		
		bulk.add(SnomedRequests.prepareUpdateRelationship(id)
			.setModuleId(Concepts.MODULE_ROOT)
			.build())
		
	}
	
	def bulkRequest = bulk.build()
	
	if (!bulkRequest.getRequests().isEmpty()) {
		
		println("Unset effective time of '${bulkRequest.getRequests().size()}' relationships that need to be reverted")
		
		SnomedRequests.prepareCommit()
			.setBody(bulkRequest)
			.setCommitComment("Unset effective time of relationships on branch: '${branchPath}'")
			.setUserId("info@b2international.com")
			.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
			.execute(bus)
			.getSync()
		
	}
	
}

def revertRelationships = { branchPath, lines ->
	
	def bulk = BulkRequest.create()
	def relationshipIdToEffectiveTimeMap = [:]
	
	lines.each { fields ->
		
		def id = fields[0]
		def effectiveTime = EffectiveTimes.parse(fields[1], DateFormats.SHORT)
		
		relationshipIdToEffectiveTimeMap.put(id, effectiveTime)
		
		def status = BooleanUtils.valueOf(fields[2]).booleanValue()
		def module = fields[3]
		def source = fields[4]
		def destination = fields[5]
		def group = Integer.valueOf(fields[6])
		def type = fields[7]
		def charType = CharacteristicType.getByConceptId(fields[8])
		def modifier = RelationshipModifier.getByConceptId(fields[9])
		
		bulk.add(SnomedRequests.prepareUpdateRelationship(id)
			.setActive(status)
			.setModuleId(module)
			.setDestinationId(destination)
			.setGroup(group)
			.setTypeId(type)
			.setCharacteristicType(charType)
			.setModifier(modifier)
			.build())
		
	}
	
	def bulkRequest = bulk.build()
	
	if (!bulkRequest.getRequests().isEmpty()) {
		
		println("Revert '${bulkRequest.getRequests().size()}' relationships to their previously released form")
		
		SnomedRequests.prepareCommit()
			.setBody(bulkRequest)
			.setCommitComment("Revert relationships on branch: '${branchPath}'")
			.setUserId("info@b2international.com")
			.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
			.execute(bus)
			.getSync()
		
		def relationships = SnomedRequests.prepareSearchRelationship()
			.all()
			.filterByIds(relationshipIdToEffectiveTimeMap.keySet())
			.setFields(SnomedRelationshipIndexEntry.Fields.ID, SnomedRelationshipIndexEntry.Fields.EFFECTIVE_TIME)
			.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
			.execute(bus)
			.getSync()
			.getItems()
		
		def editingContext = new SnomedEditingContext(BranchPathUtils.createPath(branchPath))
		
		relationships.each { SnomedRelationship relationship ->
			
			if (EffectiveTimes.format(relationship.getEffectiveTime()).equals(EffectiveTimes.UNSET_EFFECTIVE_TIME_LABEL)) {
				
				def expectedEffectiveTime = relationshipIdToEffectiveTimeMap.get(relationship.getId())
				
				def rship = editingContext.lookup(relationship.getId(), Relationship)
				rship.setEffectiveTime(expectedEffectiveTime)
				
			}
			
		}
	
		if (editingContext.isDirty()) {
			editingContext.commit("Force update relationship effective times")
		}
			
		editingContext.close()
	
	}
	
}

def validateRevertedRelationships = { branchPath, lines ->
	
	def relationshipIdToEffectiveTimeMap = [:]
	
	lines.each { fields ->
		
		def id = fields[0]
		def effectiveTime = EffectiveTimes.parse(fields[1], DateFormats.SHORT)
		
		relationshipIdToEffectiveTimeMap.put(id, effectiveTime)
		
	}
	
	def relationships = SnomedRequests.prepareSearchRelationship()
		.all()
		.filterByIds(relationshipIdToEffectiveTimeMap.keySet())
		.setFields(SnomedRelationshipIndexEntry.Fields.ID, SnomedRelationshipIndexEntry.Fields.EFFECTIVE_TIME)
		.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
		.execute(bus)
		.getSync()
		.getItems()
	
	def failed = false	
	
	relationships.each { SnomedRelationship relationship ->
		
		def expectedEffectiveTime = relationshipIdToEffectiveTimeMap.get(relationship.getId())
		
		if (!expectedEffectiveTime.equals(relationship.getEffectiveTime())) {
			
			println("Relationship with ID '" + relationship.getId() + "' was not reverted to '"
				+ EffectiveTimes.format(expectedEffectiveTime) + "' -> '"
				+ EffectiveTimes.format(relationship.getEffectiveTime()) + "'")
			
			failed = true
			
		}
		
	}
	
	
	if (!failed) {
		
		println("${lines.size()} relationships were reverted successfully to their previously released form")
		
	}
	
	return failed
}

def revertComponents = { branchPath ->
	
	def List<String[]> relationshipsToUpdate = getRF2LinesFromFile(relationshipsToUpdatePath)
	
	println("Reverting ${relationshipsToUpdate.size()} relationships to their previously released form in batches of ${offset} ...")
	
	def failed = false
	
	relationshipsToUpdate.collate(offset).each { lines ->
		changeRelationshipsToUnpublished(branchPath, lines)
		revertRelationships(branchPath, lines)
		failed = validateRevertedRelationships(branchPath, lines)
	}
	
	return !failed
	
}

def importDelta = { branchPath, deltaFile -> 
	
	def importResult = importUtil.doImport(
		SnomedTerminologyComponentConstants.SNOMED_SHORT_NAME,
		User.SYSTEM.getUsername(),
		ContentSubType.DELTA, 
		branchPath,
		deltaFile,
		false,
		new ConsoleProgressMonitor())
	
	def hasDefects = importResult.getValidationDefects().any { defect -> defect.getDefectType().isCritical() }
	
	if (!importResult.getVisitedConcepts().isEmpty() && !hasDefects) {
		println("SNOMED CT import successfully finished on branch '${branchPath}'")
	}
	
}

branchesToFix.each { branchPath ->
	
	println("Deleting components on branch '${branchPath}'")
	
	def deletionResult = deleteComponents(branchPath)
	
	println("Reverting components on branch '${branchPath}'")
	
	def revertResult = revertComponents(branchPath)
	
	if (deletionResult && revertResult) {
		
		def deltaFile = new File(rf2DeltaArchivePath)
		
		if (deltaFile.exists()) {
			
			println ("Importing RF2 delta on branch '${branchPath}'...")
			
			importDelta(branchPath, deltaFile)
			
		}
		
	}
	
}
