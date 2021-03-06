/*
 * Copyright 2011-2018 B2i Healthcare Pte Ltd, http://b2i.sg
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
package com.b2international.snowowl.datastore.version;

import org.eclipse.core.runtime.IProgressMonitor;

import com.b2international.snowowl.core.api.SnowowlServiceException;

/**
 * Service interface for managing the publication process for a concrete terminology.
 */
public interface IPublishManager {
	
	/**
	 * Performs the publication based on the configuration argument.
	 * 
	 * @param configuration the configuration for the operation.
	 * @param monitor monitor for the publication process.
	 * @throws SnowowlServiceException if the publication failed.
	 */
	void publish(final PublishOperationConfiguration configuration, final IProgressMonitor monitor) throws SnowowlServiceException;
	
	/**
	 * Performs terminology specific actions after the successful commit.
	 */
	default void postCommit() {
	}
	
}