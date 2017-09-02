/*
 * Copyright 2011-2017 B2i Healthcare Pte Ltd, http://b2i.sg
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
package com.b2international.snowowl.snomed.reasoner.classification;

/**
 * Represents the RPC interface for the SNOMED CT reasoner service.
 * <p>
 * Additionally supported operations are:
 * <ul>
 * <li>{@link #getEquivalentConcepts(String) returning equivalence sets}
 * <li>{@link #persistChanges(String, String) persisting changes from a classification run}
 * <li>{@link #canStartImmediately() checking reasoner availability}
 * </ul>
 * 
 * @since 5.10.13
 */
public interface SnomedInternalReasonerService extends SnomedReasonerService {

	/**
	 * Returns equivalent sets computed from the classification. This method blocks until a result becomes available.
	 * 
	 * @param classificationId the unique identifier of the classification run
	 * @return a {@link GetEquivalentConceptsResponse} instance
	 */
	GetEquivalentConceptsResponse getEquivalentConcepts(String classificationId);
	
	/**
	 * Instructs the reasoner to persist the results of the classification with the given identifier.
	 * 
	 * @param classificationId the unique identifier of the classification run
	 * @param userId the requesting user's identifier
	 * @return a {@link PersistChangesResponse} instance
	 */
	PersistChangesResponse persistChanges(String classificationId, String userId);

	/**
	 * Checks if any free reasoner instances are available.
	 * 
	 * @return {@code true} if a request for classification is likely to start in a short time, {@code false} otherwise
	 */
	boolean canStartImmediately();
	
}
