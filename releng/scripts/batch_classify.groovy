package scripts

import java.util.concurrent.TimeUnit

import com.b2international.snowowl.core.ApplicationContext
import com.b2international.snowowl.eventbus.IEventBus
import com.b2international.snowowl.identity.domain.User
import com.b2international.snowowl.snomed.datastore.SnomedDatastoreActivator
import com.b2international.snowowl.snomed.reasoner.domain.ClassificationStatus
import com.b2international.snowowl.snomed.reasoner.domain.ClassificationTask
import com.b2international.snowowl.snomed.reasoner.domain.RelationshipChanges
import com.b2international.snowowl.snomed.reasoner.request.ClassificationRequests

def reasonerId = "org.semanticweb.elk.owlapi.ElkReasonerFactory"

def pollInterval = TimeUnit.SECONDS.toMillis(2L)
def pollTimeout = TimeUnit.MINUTES.toMillis(15L)

def exitStates = [ClassificationStatus.COMPLETED, ClassificationStatus.FAILED, ClassificationStatus.STALE, ClassificationStatus.CANCELED]

def branchesToClassify = [
	"MAIN/2019-07-31"
]

def bus = ApplicationContext.getServiceForClass(IEventBus)

def classify = { branchPath ->
	
	def classificationId = ClassificationRequests.prepareCreateClassification()
		.setReasonerId(reasonerId)
		.setUserId(User.SYSTEM.getUsername())
		.setUseExternalService(true)
		.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
		.execute(bus)
		.getSync()
		
	def status = ClassificationStatus.SCHEDULED
	def endTime = System.currentTimeMillis() + pollTimeout
	def currentTime = 0L
	def ClassificationTask classificationTask
	
	while (!exitStates.contains(status) && currentTime < endTime) {
		
		Thread.sleep(pollInterval)
		
		classificationTask = ClassificationRequests.prepareGetClassification(classificationId)
			.build(SnomedDatastoreActivator.REPOSITORY_UUID)
			.execute(bus)
			.getSync()
		
		status = classificationTask.getStatus()
		currentTime = System.currentTimeMillis()
		
	}
	
	println("Classification on '${branchPath}' with ID '${classificationId}' finished with state '${status}'")

	if (status == ClassificationStatus.COMPLETED) {
		
		println("Inferred relationship changes found -> ${classificationTask.getInferredRelationshipChangesFound()}")
		println("Redundant stated relationship changes found -> ${classificationTask.getRedundantStatedRelationshipsFound()}")
		println("Equivalent concepts found -> ${classificationTask.getEquivalentConceptsFound()}")
		
		def RelationshipChanges changes = ClassificationRequests.prepareSearchRelationshipChange()
			.filterByClassificationId(classificationId)
			.setExpand("relationship()")
			.setLimit(0)
			.build(SnomedDatastoreActivator.REPOSITORY_UUID)
			.execute(bus)
			.getSync()
			
		println("Total number of relationship changes -> ${changes.getTotal()}")
		
	}	
	
}

branchesToClassify.each { branchPath ->
	
	println("Classifying branch '${branchPath}'")
	
	classify(branchPath)
	
}
