import groovy.io.FileType

import com.b2international.snowowl.core.ApplicationContext
import com.b2international.snowowl.core.Metadata
import com.b2international.snowowl.core.MetadataMixin
import com.b2international.snowowl.datastore.request.RepositoryRequests
import com.b2international.snowowl.eventbus.IEventBus
import com.b2international.snowowl.snomed.datastore.SnomedDatastoreActivator
import com.fasterxml.jackson.databind.ObjectMapper

def codeSystemsFolder = new File("/opt/codesystems")

def objectMapper = new ObjectMapper()

objectMapper.addMixIn(Metadata, MetadataMixin);

codeSystemsFolder.eachFileRecurse(FileType.FILES) { file -> 
	
	def lines = file.readLines()
	
	if (lines.size() > 0) {
		
		def json = lines.get(0)
		def branchData = objectMapper.readValue(json, Map)
		
		def metadataString = branchData.get("metadata") 
		def branchPath = branchData.get("path")

		def metadata = objectMapper.readValue(objectMapper.writeValueAsString(metadataString), Metadata)		

		if (!metadata.isEmpty()) {
			
			def result = RepositoryRequests.branching().prepareUpdate(branchPath)
				.setMetadata(metadata)
				.build(SnomedDatastoreActivator.REPOSITORY_UUID)
				.execute(ApplicationContext.getServiceForClass(IEventBus))
				.getSync()
				
			if (result) {
				println("Successfully updated branch metadata for '${branchPath}'")
			} else {
				println("Failed to update metadata for '${branchPath}'")
			}
			
		}
		
	} else {
		println(file.getAbsolutePath() + " is empty")
	}
	
}

