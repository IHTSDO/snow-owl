/*
 * Copyright 2017-2018 B2i Healthcare Pte Ltd, http://b2i.sg
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
package com.b2international.snowowl.datastore.request.version;

import static com.b2international.snowowl.datastore.oplock.impl.DatastoreLockContextDescriptions.CREATE_VERSION;

import java.time.Instant;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.hibernate.validator.constraints.NotEmpty;

import com.b2international.snowowl.core.CoreTerminologyBroker;
import com.b2international.snowowl.core.Repository;
import com.b2international.snowowl.core.RepositoryManager;
import com.b2international.snowowl.core.ServiceProvider;
import com.b2international.snowowl.core.api.SnowowlRuntimeException;
import com.b2international.snowowl.core.api.SnowowlServiceException;
import com.b2international.snowowl.core.branch.Branch;
import com.b2international.snowowl.core.domain.exceptions.CodeSystemNotFoundException;
import com.b2international.snowowl.core.events.Request;
import com.b2international.snowowl.core.exceptions.BadRequestException;
import com.b2international.snowowl.core.exceptions.ConflictException;
import com.b2international.snowowl.core.exceptions.NotFoundException;
import com.b2international.snowowl.datastore.BranchPathUtils;
import com.b2international.snowowl.datastore.CodeSystemEntry;
import com.b2international.snowowl.datastore.CodeSystemVersionEntry;
import com.b2international.snowowl.datastore.oplock.IOperationLockManager;
import com.b2international.snowowl.datastore.oplock.OperationLockException;
import com.b2international.snowowl.datastore.oplock.impl.DatastoreLockContext;
import com.b2international.snowowl.datastore.oplock.impl.DatastoreOperationLockException;
import com.b2international.snowowl.datastore.oplock.impl.IDatastoreOperationLockManager;
import com.b2international.snowowl.datastore.oplock.impl.SingleRepositoryAndBranchLockTarget;
import com.b2international.snowowl.datastore.remotejobs.RemoteJob;
import com.b2international.snowowl.datastore.request.RepositoryRequests;
import com.b2international.snowowl.datastore.version.IVersioningManager;
import com.b2international.snowowl.datastore.version.PublishOperationConfiguration;
import com.b2international.snowowl.datastore.version.VersioningManagerBroker;
import com.b2international.snowowl.eventbus.IEventBus;
import com.b2international.snowowl.terminologyregistry.core.request.CodeSystemRequests;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @since 5.7
 */
final class CodeSystemVersionCreateRequest implements Request<ServiceProvider, Boolean> {

	private static final long serialVersionUID = 1L;
	private static final int TASK_WORK_STEP = 4;
	
	@NotEmpty
	@JsonProperty
	String versionId;
	
	@JsonProperty
	String description;
	
	@NotNull
	@JsonProperty
	Date effectiveTime;
	
	@NotEmpty
	@JsonProperty
	String codeSystemShortName;
	
	// lock props
	private transient DatastoreLockContext lockContext;
	private transient SingleRepositoryAndBranchLockTarget lockTarget;
	
	@Override
	public Boolean execute(ServiceProvider context) {
		final RemoteJob job = context.service(RemoteJob.class);
		final String user = job.getUser();
		
		// get code system
		CodeSystemEntry codeSystem = null; 
		Set<String> repositoryIds = context.service(RepositoryManager.class).repositories().stream().map(Repository::id).collect(Collectors.toSet());
		for (String repositoryId : repositoryIds) {
			try {
				codeSystem = CodeSystemRequests.prepareGetCodeSystem(codeSystemShortName)
						.build(repositoryId)
						.execute(context.service(IEventBus.class))
						.getSync();
			} catch (NotFoundException e) {
				// ignore
			}
		}
		
		if (codeSystem == null) {
			throw new CodeSystemNotFoundException(codeSystemShortName);
		}

		// check that the new versionId does not conflict with any other currently available branch
		final String newVersionPath = String.format("%s%s%s", codeSystem.getBranchPath(), Branch.SEPARATOR, versionId);
		
		// validate new path
		Branch.BranchNameValidator.DEFAULT.checkName(versionId);
		
		try {
			RepositoryRequests.branching()
				.prepareGet(newVersionPath)
				.build(codeSystem.getRepositoryUuid())
				.execute(context.service(IEventBus.class))
				.getSync();
			throw new ConflictException("An existing branch with path '%s' conflicts with the specified version identifier.", newVersionPath);
		} catch (NotFoundException e) {
			// ignore
		}
		
		// check that the specified effective time
		validateEffectiveTime(context, codeSystem);
		
		acquireLocks(context, user, codeSystem);
		
		final IProgressMonitor monitor = SubMonitor.convert(context.service(IProgressMonitor.class), TASK_WORK_STEP);
		try {
			IVersioningManager versioningManager = VersioningManagerBroker.INSTANCE.createVersioningManager(codeSystem.getTerminologyComponentId());
			monitor.worked(1);
			// execute publication process via the tooling specific VersioningManager 
			versioningManager.publish(new PublishOperationConfiguration(user, codeSystemShortName, codeSystem.getBranchPath(), versionId, description, effectiveTime), monitor);
			monitor.worked(1);
			// tag the repository
			doTag(context, codeSystem, monitor);
			// execute any post commit steps defined by the versioning manager
			postCommit(versioningManager, monitor);
		} catch (SnowowlServiceException e) {
			throw new SnowowlRuntimeException("Error occurred during versioning.", e);
		} finally {
			releaseLocks(context);
			if (null != monitor) {
				monitor.done();
			}
		}
		
		return Boolean.TRUE;
	}
	
	private void validateEffectiveTime(ServiceProvider context, CodeSystemEntry codeSystem) {
		if (!CoreTerminologyBroker.getInstance().isEffectiveTimeSupported(codeSystem.getTerminologyComponentId())) {
			return;
		}

		Instant mostRecentVersionEffectiveTime = getMostRecentVersionEffectiveDateTime(context, codeSystem);
		
		if (!effectiveTime.toInstant().isAfter(mostRecentVersionEffectiveTime)) {
			throw new BadRequestException("The specified '%s' effective time is invalid. Date should be after epoch.", effectiveTime, mostRecentVersionEffectiveTime);
		}
	}
	
	private Instant getMostRecentVersionEffectiveDateTime(ServiceProvider context, CodeSystemEntry codeSystem) {
		return CodeSystemRequests.prepareSearchCodeSystemVersion()
			.all()
			.filterByCodeSystemShortName(codeSystem.getShortName())
			.build(codeSystem.getRepositoryUuid())
			.execute(context.service(IEventBus.class))
			.getSync()
			.stream()
			.max(CodeSystemVersionEntry.VERSION_EFFECTIVE_DATE_COMPARATOR)
			.map(CodeSystemVersionEntry::getEffectiveDate)
			.map(Instant::ofEpochMilli)
			.orElse(Instant.EPOCH);
	}
	
	private void acquireLocks(ServiceProvider context, String user, CodeSystemEntry codeSystem) {
		try {
			this.lockContext = new DatastoreLockContext(user, CREATE_VERSION);
			this.lockTarget = new SingleRepositoryAndBranchLockTarget(
				codeSystem.getRepositoryUuid(), 
				BranchPathUtils.createPath(codeSystem.getBranchPath())
			);
			context.service(IDatastoreOperationLockManager.class).lock(lockContext, IOperationLockManager.IMMEDIATE, lockTarget);
		} catch (final OperationLockException e) {
			if (e instanceof DatastoreOperationLockException) {
				throw new DatastoreOperationLockException(String.format("Failed to acquire locks for versioning because %s.", e.getMessage())); 
			} else {
				throw new DatastoreOperationLockException("Error while trying to acquire lock on repository for versioning.");
			}
		} catch (final InterruptedException e) {
			throw new SnowowlRuntimeException(e);
		}
	}
	
	private void releaseLocks(ServiceProvider context) {
		context.service(IDatastoreOperationLockManager.class).unlock(lockContext, lockTarget);
	}
	
	private void doTag(ServiceProvider context, CodeSystemEntry codeSystem, IProgressMonitor monitor) {
		RepositoryRequests
			.branching()
			.prepareCreate()
			.setParent(codeSystem.getBranchPath())
			.setName(versionId)
			.build(codeSystem.getRepositoryUuid())
			.execute(context.service(IEventBus.class))
			.getSync();
		monitor.worked(1);
	}

	/*
	 * Performs actions after the successful commit. 
	 */
	private void postCommit(final IVersioningManager versioningManager, final IProgressMonitor monitor) throws SnowowlServiceException {
		versioningManager.postCommit();
		if (null != monitor) {
			monitor.worked(1);
		}
	}
	
}
