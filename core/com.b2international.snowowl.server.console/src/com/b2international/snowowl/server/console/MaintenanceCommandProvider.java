/*
 * Copyright 2011-2015 B2i Healthcare Pte Ltd, http://b2i.sg
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
package com.b2international.snowowl.server.console;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;

import com.b2international.commons.StringUtils;
import com.b2international.index.revision.Purge;
import com.b2international.snowowl.core.ApplicationContext;
import com.b2international.snowowl.core.ApplicationContext.ServiceRegistryEntry;
import com.b2international.snowowl.core.branch.Branch;
import com.b2international.snowowl.datastore.cdo.ICDORepositoryManager;
import com.b2international.snowowl.datastore.request.RepositoryRequests;
import com.b2international.snowowl.datastore.server.ServerDbUtils;
import com.b2international.snowowl.datastore.server.reindex.OptimizeRequest;
import com.b2international.snowowl.datastore.server.reindex.PurgeRequest;
import com.b2international.snowowl.datastore.server.reindex.ReindexRequest;
import com.b2international.snowowl.datastore.server.reindex.ReindexRequestBuilder;
import com.b2international.snowowl.datastore.server.reindex.ReindexResult;
import com.b2international.snowowl.eventbus.IEventBus;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;

/**
 * OSGI command contribution with Snow Owl commands.
 *
 */
public class MaintenanceCommandProvider implements CommandProvider {

	class Artefact {
		public long storageKey;
		public long snomedId;
		public String branchPath;

		public Artefact(String branchPath, long snomedId, long storageKey) {
			this.branchPath = branchPath;
			this.snomedId = snomedId;
			this.storageKey = storageKey;
		}

		@Override
		public String toString() {
			return "Artefact [branchPath=" + branchPath + ", storageKey=" + storageKey + ", snomedId=" + snomedId + "]";
		}
	}

	@Override
	public String getHelp() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("---Snow Owl commands---\n");
		buffer.append("\tsnowowl checkservices - Checks the core services presence\n");
		buffer.append("\tsnowowl dbcreateindex [nsUri] - creates the CDO_CREATED index on the proper DB tables for all classes contained by a package identified by its unique namspace URI\n");
		buffer.append("\tsnowowl reindex [repositoryId] [failedCommitTimestamp]- reindexes the content for the given repository ID from the given failed commit timestamp (optional, default timestamp is 1 which means no failed commit).\n");
		buffer.append("\tsnowowl optimize [repositoryId] [maxSegments] - optimizes the underlying index for the repository to have the supplied maximum number of segments (default number is 1)\n");
		buffer.append("\tsnowowl purge [repositoryId] [branchPath] [ALL|LATEST|HISTORY] - optimizes the underlying index by deleting unnecessary documents from the given branch using the given purge strategy (default strategy is LATEST)\n");
		return buffer.toString();
	}

	/**
	 * Reflective template method declaratively registered. Needs to start with
	 * "_".
	 * 
	 * @param interpreter
	 */
	public void _snowowl(CommandInterpreter interpreter) {
		try {
			String cmd = interpreter.nextArgument();

			if ("checkservices".equals(cmd)) {
				checkServices(interpreter);
				return;
			}

			if ("dbcreateindex".equals(cmd)) {
				executeCreateDbIndex(interpreter);
				return;
			}

			if ("listrepositories".equals(cmd)) {
				listRepositories(interpreter);
				return;
			}

			if ("listbranches".equals(cmd)) {
				listBranches(interpreter);
				return;
			}

			if ("reindex".equals(cmd)) {
				reindex(interpreter);
				return;
			}
			
			if ("optimize".equals(cmd)) {
				optimize(interpreter);
				return; 
			}
			
			if ("purge".equals(cmd)) {
				purge(interpreter);
				return;
			}
			
			interpreter.println(getHelp());
		} catch (Exception ex) {
			if (Strings.isNullOrEmpty(ex.getMessage())) {
				interpreter.println("Something went wrong during the processing of your request.");
				ex.printStackTrace();
			} else {
				interpreter.println(ex.getMessage());
			}
		}
	}

	private void purge(CommandInterpreter interpreter) {
		final String repositoryId = interpreter.nextArgument();
		
		if (Strings.isNullOrEmpty(repositoryId)) {
			interpreter.println("RepositoryId parameter is required");
			return;
		}
		
		final String branchPath = interpreter.nextArgument();
		
		if (Strings.isNullOrEmpty(branchPath)) {
			interpreter.print("BranchPath parameter is required");
			return;
		}
		
		
		final String purgeArg = interpreter.nextArgument();
		final Purge purge = Strings.isNullOrEmpty(purgeArg) ? Purge.LATEST : Purge.valueOf(purgeArg);
		if (purge == null) {
			interpreter.print("Invalid purge parameter. Select one of " + Joiner.on(",").join(Purge.values()));
			return;
		}
		
		PurgeRequest.builder(repositoryId)
			.setBranchPath(branchPath)
			.setPurge(purge)
			.create()
			.execute(getBus())
			.getSync();
	}

	private void reindex(CommandInterpreter interpreter) {
		final String repositoryId = interpreter.nextArgument();
		
		if (Strings.isNullOrEmpty(repositoryId)) {
			interpreter.println("RepositoryId parameter is required");
			return;
		}
		
		final ReindexRequestBuilder req = ReindexRequest.builder(repositoryId);
		
		final String failedCommitTimestamp = interpreter.nextArgument();
		if (!StringUtils.isEmpty(failedCommitTimestamp)) {
			req.setFailedCommitTimestamp(Long.parseLong(failedCommitTimestamp));
		}
		
		final ReindexResult result = req
				.create()
				.execute(getBus())
				.getSync();
		
		interpreter.println(result.getMessage());
	}

	private static IEventBus getBus() {
		return ApplicationContext.getServiceForClass(IEventBus.class);
	}
	
	private void optimize(CommandInterpreter interpreter) {
		final String repositoryId = interpreter.nextArgument();
		if (Strings.isNullOrEmpty(repositoryId)) {
			interpreter.println("RepositoryId parameter is required.");
			return;
		}
		
		// default max segments is 1
		int maxSegments = 1;
		final String maxSegmentsArg = interpreter.nextArgument();
		if (!Strings.isNullOrEmpty(maxSegmentsArg)) {
			maxSegments = Integer.parseInt(maxSegmentsArg);
		}

		// TODO convert this to a request
		interpreter.println("Optimizing index to max. " + maxSegments + " number of segments...");
		OptimizeRequest.builder(repositoryId)
			.setMaxSegments(maxSegments)
			.create()
			.execute(getBus())
			.getSync();
		interpreter.println("Index optimization completed.");
	}

	public synchronized void executeCreateDbIndex(CommandInterpreter interpreter) {

		String nsUri = interpreter.nextArgument();
		if (null != nsUri) {
			ServerDbUtils.createCdoCreatedIndexOnTables(nsUri);
		} else {
			interpreter.println("Namespace URI should be specified.");
		}
	}

	public synchronized void listRepositories(CommandInterpreter interpreter) {
		ICDORepositoryManager repositoryManager = ApplicationContext.getServiceForClass(ICDORepositoryManager.class);
		Set<String> uuidKeySet = repositoryManager.uuidKeySet();
		if (!uuidKeySet.isEmpty()) {
			interpreter.println("Repositories:");
			for (String repositoryName : uuidKeySet) {
				interpreter.println("  " + repositoryName);
			}
		}
	}

	public synchronized void listBranches(CommandInterpreter interpreter)
			throws InterruptedException, ExecutionException {

		String repositoryName = interpreter.nextArgument();
		if (isValidRepositoryName(repositoryName, interpreter)) {
			IEventBus eventBus = ApplicationContext.getInstance().getService(IEventBus.class);
			interpreter.println("Repository " + repositoryName + " branches:");
			Branch branch = RepositoryRequests.branching(repositoryName).prepareGet("MAIN").executeSync(eventBus, 1000);
			processBranch(branch, 0, interpreter);
		}
	}

	// Depth-first traversal
	private void processBranch(Branch childBranch, int indent, CommandInterpreter interpreter) {

		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < indent; i++) {
			sb.append(' ');

		}
		sb.append(childBranch.name());
		interpreter.println(sb.toString());
		indent++;
		Collection<? extends Branch> children = childBranch.children();
		for (Branch branch : children) {
			processBranch(branch, indent, interpreter);
		}
		indent--;
	}

	public synchronized void checkServices(CommandInterpreter ci) {
		
		ci.println("Checking core services...");
		try {
			Collection<ServiceRegistryEntry<?>> services = ApplicationContext.getInstance().checkServices();
			for (ServiceRegistryEntry<?> entry : services) {
				ci.println("Interface: " + entry.getServiceInterface() + " : " + entry.getImplementation());
			}
			ci.println("Core services are registered properly and available for use.");
		} catch (final Throwable t) {
			ci.print("Error: " + t.getMessage());
		}
	}

	private boolean isValidRepositoryName(String repositoryName, CommandInterpreter interpreter) {
		if (repositoryName == null) {
			interpreter.println(
					"Repository name should be specified. Execute 'listrepositories' to see the available repositories.");
			return false;
		}

		ICDORepositoryManager repositoryManager = ApplicationContext.getServiceForClass(ICDORepositoryManager.class);
		Set<String> uuidKeySet = repositoryManager.uuidKeySet();
		if (!uuidKeySet.contains(repositoryName)) {
			interpreter.println("Could not find repository called: " + repositoryName);
			interpreter.println("Available repository names are: " + uuidKeySet);
			return false;
		}
		return true;

	}
}