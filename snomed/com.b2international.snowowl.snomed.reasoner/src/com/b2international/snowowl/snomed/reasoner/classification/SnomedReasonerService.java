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
 * Supported operations are:
 * <ul>
 * <li>{@link #beginClassification(ClassificationSettings) starting classification of a branch}
 * <li>{@link #getResult(String) retrieving a classification result for review}
 * <li>{@link #removeResult(String) removing a classification result}
 * </ul>
 */
public interface SnomedReasonerService {

	/**
	 * Performs a classification run for the specified branch of SNOMED CT. Current ontology content is extended with
	 * the given concept definitions; if a definition describes a concept which already exists, the incoming definition
	 * takes precedence.
	 * 
	 * @param settings the object describing parameters for the classification run
	 */
	void beginClassification(ClassificationSettings settings);

	/**
	 * Returns the results of the classification for review. This method blocks until a result becomes available.
	 * 
	 * @param classificationId the unique identifier of the classification run
	 * @return a {@link GetResultResponse} instance
	 */
	GetResultResponse getResult(String classificationId);

	/**
	 * Removes the result of the classification with the given identifier from memory, it it exists.
	 * 
	 * @param classificationId the unique identifier of the classification run
	 */
	void removeResult(String classificationId);

}
