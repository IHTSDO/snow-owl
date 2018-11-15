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
package com.b2international.snowowl.datastore.tasks;

/**
 * Task context based branch management policy representation.
 * <p>Clients may specify what repositories could be affected by the given {@link ITaskContext task context}. 
 *
 */
public interface ITaskContextBranchManagementPolicy {

	/**Returns with an iterable of effected repository identifiers.*/
	Iterable<String> getRepositoryUuids();
	
}