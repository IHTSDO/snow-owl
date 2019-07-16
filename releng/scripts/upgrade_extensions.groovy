/*
 * Copyright 2019 B2i Healthcare Pte Ltd, http://b2i.sg
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
package com.b2international.snowowl.test.commons

import com.b2international.commons.time.TimeUtil
import com.b2international.snowowl.core.ApplicationContext
import com.b2international.snowowl.core.Metadata
import com.b2international.snowowl.core.branch.Branch
import com.b2international.snowowl.core.branch.Branch.BranchState
import com.b2international.snowowl.core.merge.Merge
import com.b2international.snowowl.datastore.BranchPathUtils
import com.b2international.snowowl.datastore.CodeSystemEntry
import com.b2international.snowowl.datastore.request.RepositoryRequests
import com.b2international.snowowl.eventbus.IEventBus
import com.b2international.snowowl.identity.domain.User
import com.b2international.snowowl.snomed.datastore.SnomedDatastoreActivator
import com.b2international.snowowl.terminologyregistry.core.request.CodeSystemRequests
import com.google.common.base.Stopwatch
import com.google.common.collect.ImmutableListMultimap

def bus = ApplicationContext.getServiceForClass(IEventBus)
def repositoryId = SnomedDatastoreActivator.REPOSITORY_UUID

// specify the path of the target version
def targetVersionPath = "MAIN/2019-07-31"
// specify the short name of the target version (used for metadata)
def targetVersionShortName = "20190731"
// specify the name of the SNAPSHOT RF2 archive that contains the content of the new INT version branch
def dependencyPackage = "uat-ms_2019-07-31_20190716_094756.zip"

// specify the short name of the extension and the name of the related project branches here that need to be handled during the upgrade
def extensionsAndProjects = ImmutableListMultimap.builder()
	.put("SNOMEDCT-NO", "NOOCT19")
	.put("SNOMEDCT-CH", "CHPRE19")
	.put("SNOMEDCT-EE", "EEPRE19")
//	.put("SNOMEDCT-US", "USSEP19")
//	.put("SNOMEDCT-IE", "IEOCT19")
//	.put("SNOMEDCT-SE", "SENOV19")
//	.put("SNOMEDCT-DK", "DKSEP19")
//	.put("SNOMEDCT-BE", "BESEP19")
//	.put("SNOMEDCT-LOINC", "LOINC2019")
	.build()

def extensionsToCurrentBranchMap = [:]
def extensionsToMetadataMap = [:]

extensionsAndProjects.keySet().each { shortName ->
	
	def branchPath = CodeSystemRequests.prepareGetCodeSystem(shortName)
		.build(repositoryId)
		.execute(bus)
		.getSync()
		.getBranchPath()
		
	extensionsToCurrentBranchMap.put(shortName, branchPath)
	
	println("Current branch of " + shortName + " is " + branchPath)
	
	Branch branch = RepositoryRequests.branching().prepareGet(branchPath)
		.build(repositoryId)
		.execute(bus)
		.getSync()
		
	extensionsToMetadataMap.put(shortName, branch.metadata())
	
}

def mergeBranches = { source, target ->
	
	Merge merge = RepositoryRequests.merging().prepareCreate()
		.setSource(source)
		.setTarget(target)
		.setUserId(User.SYSTEM.getUsername())
		.build(repositoryId)
		.execute(bus)
		.getSync()

	def status = merge.getStatus()
	
	while (status == Merge.Status.CANCEL_REQUESTED || status == Merge.Status.IN_PROGRESS || status == Merge.Status.SCHEDULED) {
		
		Thread.sleep(10000)
		
		merge = RepositoryRequests.merging().prepareGet(merge.getId())
			.build(repositoryId)
			.execute(bus)
			.getSync()
			
		status = merge.getStatus()
		
	}

	def duration
	
	if (merge.getStartDate() != null && merge.getEndDate() != null) {
		duration = TimeUtil.milliToReadableString(merge.getEndDate().getTime() - merge.getStartDate().getTime())
	} else {
		duration = "Unknown"
	}
	
	println("Merging " + source + " to " + target + " finished with status: " + status + ". Merge id: " + merge.getId() + " Duration: " + duration)
	
	if (status == Merge.Status.CONFLICTS) {
		
		println("Conflicts detected:")
		
		merge.getConflicts().collect { conflict -> conflict.getMessage() }.each { message ->
			println("\t" + message)
		}
		
	}
	
	return status
}

def upgrade = { shortName ->
	
	Stopwatch stopwatch = Stopwatch.createStarted()
	
	println("Checking project branch states for " + shortName)
	
	extensionsAndProjects.get(shortName).each { project ->
		
		def hits = RepositoryRequests.branching().prepareSearch()
			.filterByParent(extensionsToCurrentBranchMap.get(shortName))
			.filterByName(project)
			.build(repositoryId)
			.execute(bus)
			.getSync()
			.getItems()
			
		if (!hits.isEmpty()) {
			
			Branch projectBranch = hits.first()
			
			if (projectBranch.state() == BranchState.STALE) {
				println("Project branch with path: " + projectBranch.path() + " is STALE. Skipping.")
			} else if (projectBranch.state() == BranchState.FORWARD) {
				println("Project branch with path: " + projectBranch.path() + " is FORWARD. Merging to " + projectBranch.parentPath())
				mergeBranches(projectBranch.path(), projectBranch.parentPath())
			} else if (projectBranch.state() == BranchState.BEHIND)  {
				println("Project branch with path: " + projectBranch.path() + " is BEHIND . Nothing to do")
			} else if (projectBranch.state() == BranchState.DIVERGED) {
				println("Project branch with path: " + projectBranch.path() + " is DIVERGED . Rebasing and then merging to " + projectBranch.parentPath())
				mergeBranches(projectBranch.parentPath(), projectBranch.path())
				mergeBranches(projectBranch.path(), projectBranch.parentPath())
			} else if (projectBranch.state() == BranchState.UP_TO_DATE) {
				println("Project branch with path: " + projectBranch.path() + " is UP_TO_DATE. Nothing to do.")
			}
			
		} else {
			println("Project with name: " + project + " was not found")
		}
		
	}
	
	def readyToUpgrade = extensionsAndProjects.get(shortName).every { project ->
		
		def hits = RepositoryRequests.branching().prepareSearch()
			.filterByParent(extensionsToCurrentBranchMap.get(shortName))
			.filterByName(project)
			.build(repositoryId)
			.execute(bus)
			.getSync()
			.getItems()
			
		if (!hits.isEmpty()) {
			Branch projectBranch = hits.first()
			return projectBranch.state() == BranchState.UP_TO_DATE || projectBranch.state() == BranchState.BEHIND
		} else {
			return false
		}
		
	}
	
	if (readyToUpgrade) {
		
		def targetExtensionBranchPath = targetVersionPath + "/" + shortName
				
		if (!BranchPathUtils.exists(repositoryId, targetExtensionBranchPath)) {
			
			println("Creating new extension branch: " + targetExtensionBranchPath)
			
			RepositoryRequests.branching()
				.prepareCreate()
				.setParent(targetVersionPath)
				.setName(shortName)
				.build(repositoryId)
				.execute(bus)
				.getSync()
			
		}

		println("Upgrading extension " + shortName + "...")
		
		def status = mergeBranches(extensionsToCurrentBranchMap.get(shortName), targetExtensionBranchPath)
		
		if (status == Merge.Status.COMPLETED) {
			
			println("Updating new extension branch with previous metadata value")
			
			Metadata metadata = extensionsToMetadataMap.get(shortName)
			metadata.put("dependencyRelease", targetVersionShortName)
			metadata.put("dependencyPackage", dependencyPackage)
			
			def success = RepositoryRequests.branching()
				.prepareUpdate(targetExtensionBranchPath)
				.setMetadata(metadata)
				.build(repositoryId)
				.execute(bus)
				.getSync()
				
			if (success) {
				
				Branch branch = RepositoryRequests.branching().prepareGet(targetExtensionBranchPath)
					.build(repositoryId)
					.execute(bus)
					.getSync()
				
				println("Metadata was successfully updated for " + targetExtensionBranchPath + ". Dependency release is set to: " + branch.metadata().get("dependencyRelease"))
				
			} else {
				println("Metadata update failed for " + targetExtensionBranchPath)
			}
			
			println("Updating current branch path for extension " + shortName)
			
			CodeSystemRequests.prepareUpdateCodeSystem(shortName)
				.setBranchPath(targetExtensionBranchPath)
				.build(repositoryId, Branch.MAIN_PATH, User.SYSTEM.getUsername(), "Updated branch path of " + shortName)
				.execute(bus)
				.getSync()
			
			CodeSystemEntry entry = CodeSystemRequests.prepareGetCodeSystem(shortName)
				.build(repositoryId)
				.execute(bus)
				.getSync()
				
			println("Current branch path of extension " + shortName + " is " + entry.getBranchPath())
			
			extensionsAndProjects.get(shortName).each { project ->
				
				def newProjectBranchPath = targetExtensionBranchPath + "/" + project
				
				if (!BranchPathUtils.exists(repositoryId, newProjectBranchPath)) {
			
					println("Creating new project branch: " + newProjectBranchPath)
					
					RepositoryRequests.branching()
						.prepareCreate()
						.setParent(targetExtensionBranchPath)
						.setName(project)
						.build(repositoryId)
						.execute(bus)
						.getSync()
					
				}
				
			}
			
		}
		
	} else {
		println("Unable to upgrade extension with shortname: " + shortName + " due to unresolved project branches")
	}
	
	println("Upgrade attempt of " + shortName + " took " + TimeUtil.toString(stopwatch))
}

extensionsAndProjects.keySet().each { shortName ->
	println("Attempting to upgrade " + shortName)
	upgrade(shortName)
}