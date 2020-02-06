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
import com.b2international.snowowl.snomed.Concept
import com.b2international.snowowl.snomed.Relationship
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

def offset = 1000
def branchesToFix = [ "MAIN" ]

def scriptLocation = "/opt/ms_upgrade/" // update the path if necessary

def relationshipIdsToDeletePath = scriptLocation + "relationship_ids_to_remove.txt"
def refsetMemberIdsToDeletePath = scriptLocation + "member_ids_to_remove.txt"
def relationshipsToUpdatePath = scriptLocation + "relationships_to_update.txt"

def rf2DeltaArchivePath = scriptLocation + "SnomedCT_RF2Release_INT_20200206T151054.zip"


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

def deleteRelationshipsWithId = { branchPath, sum, relationshipIds ->

	def relationshipBulk = BulkRequest.create()
	
	def existingRelationshipIds = getExistingRelationshipIds(branchPath, relationshipIds)
	
	if (!existingRelationshipIds.isEmpty()) {
		
		println("Removing ${existingRelationshipIds.size()} / ${sum} invalid relationships...")
		
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
			.setUserId(User.SYSTEM.getUsername())
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

def deleteMembersWithId = { branchPath, sum, memberIds ->

	def memberBulk = BulkRequest.create();
	
	def existingMemberIds = getExistingMemberIds(branchPath, memberIds)
	
	if (!existingMemberIds.isEmpty()) {
		
		println("Removing ${existingMemberIds.size()} / ${sum} invalid reference set members...")
		
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
			.setUserId(User.SYSTEM.getUsername())
			.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
			.execute(bus)
			.getSync()
			
	}
	
}

def deleteComponents = { branchPath ->
	
	def relationshipIds = getIdsFromFile(relationshipIdsToDeletePath)
	
	println("Deleting ${relationshipIds.size()} relationships in batches of ${offset} ...")
	
	def sum = 0
	
	relationshipIds.collate(offset).each { ids ->
		sum += ids.size() 
		deleteRelationshipsWithId(branchPath, sum, ids)
	}
	
	def existingRelationshipIds = getExistingRelationshipIds(branchPath, relationshipIds)
	
	if (!existingRelationshipIds.isEmpty()) {
		
		existingRelationshipIds.each { id ->
			println("Failed to delete relationship with ID '${id}'")
		}
		
	}
	
	def memberIds = getIdsFromFile(refsetMemberIdsToDeletePath)
	
	println("Deleting ${memberIds.size()} reference set members in batches of ${offset} ...")
	
	def sumMember = 0
	
	memberIds.collate(offset).each { ids ->
		sumMember += ids.size()
		deleteMembersWithId(branchPath, sumMember, ids)
	}
	
	def existingMemberIds = getExistingMemberIds(branchPath, memberIds)
	
	if (!existingMemberIds.isEmpty()) {
		
		existingMemberIds.each { id ->
			println("Failed to delete reference set member with ID '${id}'")
		}
		
	}
	
	return existingRelationshipIds.isEmpty() && existingMemberIds.isEmpty()
}

def getConcept = { String id, SnomedEditingContext context, Map<String, Concept> idToConceptCache ->
	
	if (idToConceptCache.containsKey(id)) {
		return idToConceptCache.get(id)
	}
	
	def concept = context.lookup(id, Concept)
	idToConceptCache.put(id, concept)
	
	return concept
	
}

def updateRelationships = { branchPath, sum, SnomedEditingContext editingContext, Map<String, Concept> idToConceptCache, lines ->
	
	println("Updating ${lines.size()} / ${sum} relationships to their expected form")
	
	lines.each { fields ->
		
		def id = fields[0]
		def effectiveTime = EffectiveTimes.parse(fields[1], DateFormats.SHORT)
		def status = BooleanUtils.valueOf(fields[2]).booleanValue()
		def module = fields[3]
		def source = fields[4]
		def destination = fields[5]
		def group = Integer.valueOf(fields[6])
		def type = fields[7]
		def charType = fields[8]
		def modifier = fields[9]
		
		def relationship = editingContext.lookup(id, Relationship)
		
		relationship.setEffectiveTime(effectiveTime)
		relationship.setActive(status)
		relationship.setModule(getConcept(module, editingContext, idToConceptCache))
		relationship.setSource(getConcept(source, editingContext, idToConceptCache))
		relationship.setType(getConcept(type, editingContext, idToConceptCache))
		relationship.setDestination(getConcept(destination, editingContext, idToConceptCache))
		relationship.setCharacteristicType(getConcept(charType, editingContext, idToConceptCache))
		relationship.setModifier(getConcept(modifier, editingContext, idToConceptCache))
		relationship.setGroup(group)
		
	}
		
	if (editingContext.isDirty()) {
		editingContext.commit("Update '${lines.size()}' relationships on branch: '${branchPath}'")
	}
	
}

def validateRelationships = { branchPath, lines ->
	
	def relationshipIdToRelationshipMap = [:]
	
	lines.each { fields ->
		
		def id = fields[0]
		def effectiveTime = EffectiveTimes.parse(fields[1], DateFormats.SHORT)
		def status = BooleanUtils.valueOf(fields[2]).booleanValue()
		def module = fields[3]
		def source = fields[4]
		def destination = fields[5]
		def group = Integer.valueOf(fields[6])
		def type = fields[7]
		def charType = CharacteristicType.getByConceptId(fields[8])
		def modifier = RelationshipModifier.getByConceptId(fields[9])
		
		def rship = new SnomedRelationship()
		
		rship.setId(id)
		rship.setEffectiveTime(effectiveTime)
		rship.setActive(status)
		rship.setModuleId(module)
		rship.setSourceId(source)
		rship.setTypeId(type)
		rship.setDestinationId(destination)
		rship.setGroup(group)
		rship.setCharacteristicTypeId(charType.getConceptId())
		rship.setModifierId(modifier.getConceptId())
		
		relationshipIdToRelationshipMap.put(id, rship)
	}
	
	def relationships = SnomedRequests.prepareSearchRelationship()
		.setLimit(relationshipIdToRelationshipMap.keySet().size())
		.filterByIds(relationshipIdToRelationshipMap.keySet())
		.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
		.execute(bus)
		.getSync()
		.getItems()
	
	def anyFailed = false
	
	relationships.each { SnomedRelationship relationship ->
		
		def failed = false
			
		def SnomedRelationship expectedRelationship = relationshipIdToRelationshipMap.get(relationship.getId())
		
		if (!relationship.getEffectiveTime().equals(expectedRelationship.getEffectiveTime())) {
			failed = true
		}
		
		if (relationship.isActive() ^ expectedRelationship.isActive()) {
			failed = true
		}
		
		if (!relationship.getModuleId().equals(expectedRelationship.getModuleId())) {
			failed = true
		}
		
		if (!relationship.getSourceId().equals(expectedRelationship.getSourceId())) {
			failed = true
		}
		
		if (!relationship.getTypeId().equals(expectedRelationship.getTypeId())) {
			failed = true
		}
		
		if (!relationship.getDestinationId().equals(expectedRelationship.getDestinationId())) {
			failed = true
		}
		
		if (!relationship.getGroup().equals(expectedRelationship.getGroup())) {
			failed = true
		}
		
		if (!relationship.getCharacteristicTypeId().equals(expectedRelationship.getCharacteristicTypeId())) {
			failed = true
		}
		
		if (!relationship.getModifierId().equals(expectedRelationship.getModifierId())) {
			failed = true
		}
		
		if (failed) {
			println("Failed to update relationship to its expected form: \nEXP:\t${expectedRelationship}\nCURR:\t${relationship}")
			anyFailed = true
		}
		
	}
	
	if (!anyFailed) {
		println("${lines.size()} relationships were successfully updated to their expected form")
	}
	
	relationshipIdToRelationshipMap.clear()
	
	return anyFailed
}

def updateComponents = { branchPath ->
	
	def List<String[]> relationshipsToUpdate = getRF2LinesFromFile(relationshipsToUpdatePath)
	
	println("Updating ${relationshipsToUpdate.size()} relationships to their expected form in batches of ${offset} ...")
	
	def failed = false
	
	if (!relationshipsToUpdate.isEmpty()) {
		
		def editingContext = new SnomedEditingContext(BranchPathUtils.createPath(branchPath))
		Map<String, Concept> idToConceptCache = [:]
		
		def sum = 0
		
		relationshipsToUpdate.collate(offset).each { lines ->
			sum += lines.size()
			updateRelationships(branchPath, sum, editingContext, idToConceptCache, lines)
			failed = validateRelationships(branchPath, lines)
		}
		
		idToConceptCache.clear()
		editingContext.close()
		
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
	
	println("Updating components on branch '${branchPath}'")
	
	def updateResult = updateComponents(branchPath)
	
	if (deletionResult && updateComponents) {
		
		def deltaFile = new File(rf2DeltaArchivePath)
		
		if (deltaFile.exists()) {
			
			println ("Importing RF2 delta on branch '${branchPath}'...")
			
			importDelta(branchPath, deltaFile)
			
		}
		
	}
	
}
