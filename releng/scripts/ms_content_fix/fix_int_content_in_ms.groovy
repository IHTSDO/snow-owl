import com.b2international.commons.BooleanUtils
import com.b2international.snowowl.core.ApplicationContext
import com.b2international.snowowl.core.date.DateFormats
import com.b2international.snowowl.core.date.EffectiveTimes
import com.b2international.snowowl.core.events.bulk.BulkRequest
import com.b2international.snowowl.datastore.BranchPathUtils
import com.b2international.snowowl.eventbus.IEventBus
import com.b2international.snowowl.snomed.Relationship
import com.b2international.snowowl.snomed.SnomedConstants.Concepts
import com.b2international.snowowl.snomed.core.domain.refset.SnomedReferenceSetMember
import com.b2international.snowowl.snomed.datastore.SnomedDatastoreActivator
import com.b2international.snowowl.snomed.datastore.SnomedEditingContext
import com.b2international.snowowl.snomed.datastore.request.SnomedRequests

def bus = ApplicationContext.getServiceForClass(IEventBus)

def branchesToFix = [ // specify extension branches here 
	"MAIN",
	"MAIN/2018-01-31/SNOMEDCT-US"
]

def scriptLocation = "/opt/termserver/ms_upgrade/" // update the path if necessary

def descriptionIdsToDelete = scriptLocation + "description_ids_to_delete.txt"
def relationshipIdsToDelete = scriptLocation + "relationship_ids_to_delete.txt"
def languageRefsetMembersToDelete = scriptLocation + "language_refset_member_ids_to_delete.txt"
def relationshipsToUpdate = scriptLocation + "relationships_to_update.txt"

def intModuleIds = ["900000000000012004", "900000000000207008", "449080006"] as Set
def mrcmRefsetIds = [
	Concepts.REFSET_MRCM_DOMAIN_INTERNATIONAL,
	Concepts.REFSET_MRCM_ATTRIBUTE_DOMAIN_INTERNATIONAL,
	Concepts.REFSET_MRCM_ATTRIBUTE_RANGE_INTERNATIONAL,
	Concepts.REFSET_MRCM_MODULE_SCOPE ]

def getIds = { filePath ->
	def ids = [] as Set
	new File(filePath).eachLine { line ->
		ids.add(line)
	}
	return ids
}

def deleteInvalidContent = { branchPath ->
	
	def bulk = BulkRequest.create()
	def shouldCommit = false
	
	def descriptionIds = getIds(descriptionIdsToDelete)
	
	def existingDescriptionIds = SnomedRequests.prepareSearchDescription()
		.all()
		.filterByIds(descriptionIds)
		.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
		.execute(bus)
		.getSync()
		.getItems()
		.collect { description -> description.getId() }
		.toSet()
		
	if (!existingDescriptionIds.isEmpty()) {
		
		println("Removing invalid descriptions...")
		
		descriptionIds.each { id ->
			bulk.add(SnomedRequests.prepareDeleteDescription(id).force(true).build())
		}
		
		shouldCommit = true
		
	} else {
		println("There are no invalid descriptions present on branch: " + branchPath)
	}
	
	def relationshipIds = getIds(relationshipIdsToDelete)
	 
	def existingRelationshipIds = SnomedRequests.prepareSearchRelationship()
		.all()
		.filterByIds(relationshipIds)
		.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
		.execute(bus)
		.getSync()
		.getItems()
		.collect { relationship -> relationship.getId() }
		.toSet()
	
	if (!existingRelationshipIds.isEmpty()) {
		
		println("Removing invalid relationships...")
		
		relationshipIds.each { id ->
			bulk.add(SnomedRequests.prepareDeleteRelationship(id).force(true).build())
		}
		
		shouldCommit = true
		
	} else {
		println("There are no invalid relationships present on branch: " + branchPath)
	}
	
	def memberIds = getIds(languageRefsetMembersToDelete) 
	
	def existingMemberIds = SnomedRequests.prepareSearchMember()
		.all()
		.filterByIds(memberIds)
		.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
		.execute(bus)
		.getSync()
		.getItems()
		.collect { member -> member.getId() }
		.toSet()
	
	if (!existingMemberIds.isEmpty()) {
		
		println("Removing invalid members...")
		
		memberIds.each { id ->
			bulk.add(SnomedRequests.prepareDeleteMember(id).force(true).build())
		}
		
		shouldCommit = true
	} else {
		println("There are no invalid members present on branch: " + branchPath)
	}
	
	def intModuleDependencyMembers = SnomedRequests.prepareSearchMember()
				.all()
				.filterByRefSet(Concepts.REFSET_MODULE_DEPENDENCY_TYPE)
				.filterByReferencedComponent(intModuleIds)
				.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
				.execute(bus)
				.getSync()
				.getItems()
				.findAll { member -> intModuleIds.contains(member.getModuleId()) }
	
	intModuleDependencyMembers.each { SnomedReferenceSetMember member ->
		println("Removing module dependency member referencing only international modules: " + member.getId() + ", moduleId: " + member.getModuleId() + ", referencedComponentId: " + member.getReferencedComponentId())
	}
				
	def intModuleDependencyMemberIds = intModuleDependencyMembers.collect { member -> member.getId() }.toSet()
	
	if (!intModuleDependencyMemberIds.isEmpty()) {
		
		intModuleDependencyMemberIds.each { id -> 
			bulk.add(SnomedRequests.prepareDeleteMember(id).force(true).build())
		}
	
		shouldCommit = true
	}
	
	def existingRefsetIds = SnomedRequests.prepareSearchRefSet()
		.all()
		.filterByIds(mrcmRefsetIds)
		.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
		.execute(bus)
		.getSync()
		.getItems()
		.collect { refset -> refset.getId() }
		.toSet()

	if (!existingRefsetIds.isEmpty()) {
		
		existingRefsetIds.each { id ->
			bulk.add(SnomedRequests.prepareDeleteReferenceSet(id).force(true).build())
			println("Removing MRCM refset with id: " + id)
		}
		
		shouldCommit = true
	}
			
	
	if (shouldCommit) {
		
		SnomedRequests.prepareCommit()
			.setBody(bulk)
			.setCommitComment("Deleting invalid pre-release content from branch: " + branchPath)
			.setUserId("System")
			.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
			.execute(bus)
			.getSync()
			
	}
	
}

def updateInvalidRelationships = { branchPath ->

	def editingContext = new SnomedEditingContext(BranchPathUtils.createPath(branchPath))
		
	new File(relationshipsToUpdate).splitEachLine(";") { fields ->
		
		def relationshipId = fields[0]
		def active = BooleanUtils.valueOf(fields[1]).booleanValue()
		def effectiveTime = EffectiveTimes.parse(fields[2], DateFormats.SHORT)
		
		try {
			
			def relationship = editingContext.lookup(relationshipId, Relationship)
			
			def oldEffectiveTime = EffectiveTimes.format(relationship.getEffectiveTime(), DateFormats.SHORT)
			def oldStatus = BooleanUtils.toString(relationship.isActive())
			
			println("Updating relationship (" + relationshipId + ") properties from: effectiveTime - " + oldEffectiveTime + ", status - " + oldStatus + " to: effectiveTime - " + fields[2] + ", status - " + fields[1])
			
			relationship.setEffectiveTime(effectiveTime)
			relationship.setActive(active)
			
		} catch (NotFoundException) {
			println("Relationship with id:" + relationshipId + " was not present on branch: " + branchPath)
		}
		
	}
	
	if (editingContext.isDirty()) {
		editingContext.commit("Updating invalid pre-release relationships on branch: " + branchPath)
	}
	
	editingContext.close()
	
}

branchesToFix.each { branchPath ->
	
	println("Fixing invalid content on " + branchPath)
	
	deleteInvalidContent(branchPath)
	updateInvalidRelationships(branchPath)
	
}
