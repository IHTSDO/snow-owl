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
package com.b2international.snowowl.snomed.datastore.config;

import javax.validation.Valid;

import org.hibernate.validator.constraints.NotEmpty;

import com.b2international.snowowl.datastore.config.ConnectionPoolConfiguration;
import com.b2international.snowowl.snomed.SnomedConstants.Concepts;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * SNOMED CT related application level configuration parameters.
 * 
 * @since 3.4
 */
public class SnomedCoreConfiguration extends ConnectionPoolConfiguration {
	
	public static final String DEFAULT_LANGUAGE = "en-gb"; //$NON-NLS-1$
	public static final String DEFAULT_NAMESPACE = ""; //$NON-NLS-1$
	public static final String DEFAULT_MODULE = Concepts.MODULE_SCT_CORE;
	
	// Branch metadata property keys
	public static final String BRANCH_DEFAULT_NAMESPACE_KEY = "defaultNamespace";
	public static final String BRANCH_DEFAULT_REASONER_NAMESPACE_KEY = "defaultReasonerNamespace";
	public static final String BRANCH_DEFAULT_MODULE_ID_KEY = "defaultModuleId";
	public static final String BRANCH_ASSERTION_GROUP_NAMES_KEY = "assertionGroupNames";
	
	@NotEmpty
	private String language = DEFAULT_LANGUAGE;
	
	@NotEmpty
	private String concreteDomainTypeRefsetIdentifier = Concepts.REFSET_CONCRETE_DOMAIN_TYPE;
	
	@NotEmpty
	private String stringDatatypeRefsetIdentifier = Concepts.REFSET_STRING_DATATYPE;
	
	@NotEmpty
	private String booleanDatatypeRefsetIdentifier = Concepts.REFSET_BOOLEAN_DATATYPE;
	
	@NotEmpty
	private String floatDatatypeRefsetIdentifier = Concepts.REFSET_FLOAT_DATATYPE;
	
	@NotEmpty
	private String integerDatatypeRefsetIdentifier = Concepts.REFSET_INTEGER_DATATYPE;
	
	@NotEmpty
	private String datetimeDatatypeRefsetIdentifier = Concepts.REFSET_DATETIME_DATATYPE;
	
	@Valid
	private SnomedIdentifierConfiguration ids = new SnomedIdentifierConfiguration();
	
	@Valid
	@JsonProperty(value = "classification")
	private SnomedClassificationConfiguration classificationConfig = new SnomedClassificationConfiguration();
	
	private boolean collectSystemChanges = false;
	
	private boolean concreteDomainSupport = false;
	
	// enables the manual editing of inferred relationships and concrete data types
	private boolean inferredEditingEnabled = false;
		
	/**
	 * @return the language code currently used for SNOMED CT
	 */
	@JsonProperty
	public String getLanguage() {
		return language;
	}
	
	/**
	 * @param language the SNOMED CT language code to set
	 */
	@JsonProperty
	public void setLanguage(String language) {
		this.language = language;
	}
	
	@JsonProperty("concreteDomainSupport")
	public boolean isConcreteDomainSupported() {
		return concreteDomainSupport;
	}
	
	@JsonProperty("concreteDomainSupport")
	public void setConcreteDomainSupported(boolean concreteDomainSupport) {
		this.concreteDomainSupport = concreteDomainSupport;
	}

	@JsonProperty("inferredEditingEnabled")
	public boolean isInferredEditingEnabled() {
		return inferredEditingEnabled;
	}

	@JsonProperty("inferredEditingEnabled")
	public void setInferredEditingEnabled(boolean inferredEditingEnabled) {
		this.inferredEditingEnabled = inferredEditingEnabled;
	}
	
	@JsonProperty("collectSystemChanges")
	public boolean isCollectSystemChanges() {
		return collectSystemChanges;
	}
	
	@JsonProperty("collectSystemChanges")
	public void setCollectSystemChanges(boolean collectSystemChanges) {
		this.collectSystemChanges = collectSystemChanges;
	}
	
	/**
	 * Get all identifier service related configuration options
	 */
	public SnomedIdentifierConfiguration getIds() {
		return ids;
	}
	
	/**
	 * Sets the identifier service related configurations
	 */
	public void setIds(SnomedIdentifierConfiguration ids) {
		this.ids = ids;
	}

	/**
	 * Get all classification related configurations
	 */
	public SnomedClassificationConfiguration getClassificationConfig() {
		return classificationConfig;
	}
	
	/**
	 * Sets the classification related configurations
	 */
	public void setClassificationConfig(SnomedClassificationConfiguration classificationConfig) {
		this.classificationConfig = classificationConfig;
	}
	
	/**
	 * The ID of the concrete domain type reference set identifier concept
	 * 
	 * @return the concreteDomainTypeRefsetIdentifier
	 */
	@JsonProperty("concreteDomainTypeRefsetIdentifier")
	public String getConcreteDomainTypeRefsetIdentifier() {
		return concreteDomainTypeRefsetIdentifier;
	}

	/**
	 * Sets the ID of the concrete domain type reference set identifier concept
	 * 
	 * @param concreteDomainTypeRefsetIdentifier the concreteDomainTypeRefsetIdentifier to set
	 */
	@JsonProperty("concreteDomainTypeRefsetIdentifier")
	public void setConcreteDomainTypeRefsetIdentifier(String concreteDomainTypeRefsetIdentifier) {
		this.concreteDomainTypeRefsetIdentifier = concreteDomainTypeRefsetIdentifier;
	}

	/**
	 * The ID of the string datatype reference set identifier concept
	 * 
	 * @return the stringDatatypeRefsetIdentifier
	 */
	@JsonProperty("stringDataTypeRefsetIdentifier")
	public String getStringDatatypeRefsetIdentifier() {
		return stringDatatypeRefsetIdentifier;
	}

	/**
	 * Sets the ID of the string datatype reference set identifier concept
	 * 
	 * @param stringDatatypeRefsetIdentifier the stringDatatypeRefsetIdentifier to set
	 */
	@JsonProperty("stringDataTypeRefsetIdentifier")
	public void setStringDatatypeRefsetIdentifier(String stringDatatypeRefsetIdentifier) {
		this.stringDatatypeRefsetIdentifier = stringDatatypeRefsetIdentifier;
	}

	/**
	 * The ID of the boolean datatype reference set identifier concept
	 * 
	 * @return the booleanDatatypeRefsetIdentifier
	 */
	@JsonProperty("booleanDataTypeRefsetIdentifier")
	public String getBooleanDatatypeRefsetIdentifier() {
		return booleanDatatypeRefsetIdentifier;
	}

	/**
	 * Sets the ID of the boolean datatype reference set identifier concept
	 * 
	 * @param booleanDatatypeRefsetIdentifier the booleanDatatypeRefsetIdentifier to set
	 */
	@JsonProperty("booleanDataTypeRefsetIdentifier")
	public void setBooleanDatatypeRefsetIdentifier(String booleanDatatypeRefsetIdentifier) {
		this.booleanDatatypeRefsetIdentifier = booleanDatatypeRefsetIdentifier;
	}

	/**
	 * The ID of the float datatype reference set identifier concept
	 * 
	 * @return the floatDatatypeRefsetIdentifier
	 */
	@JsonProperty("floatDataTypeRefsetIdentifier")
	public String getFloatDatatypeRefsetIdentifier() {
		return floatDatatypeRefsetIdentifier;
	}

	/**
	 * Sets the ID of the float datatype reference set identifier concept
	 * 
	 * @param floatDatatypeRefsetIdentifier the floatDatatypeRefsetIdentifier to set
	 */
	@JsonProperty("floatDataTypeRefsetIdentifier")
	public void setFloatDatatypeRefsetIdentifier(String floatDatatypeRefsetIdentifier) {
		this.floatDatatypeRefsetIdentifier = floatDatatypeRefsetIdentifier;
	}

	/**
	 * The ID of the integer datatype reference set identifier concept
	 * 
	 * @return the integerDatatypeRefsetIdentifier
	 */
	@JsonProperty("integerDataTypeRefsetIdentifier")
	public String getIntegerDatatypeRefsetIdentifier() {
		return integerDatatypeRefsetIdentifier;
	}

	/**
	 * Sets the ID of the integer datatype reference set identifier concept
	 * 
	 * @param integerDatatypeRefsetIdentifier the integerDatatypeRefsetIdentifier to set
	 */
	@JsonProperty("integerDataTypeRefsetIdentifier")
	public void setIntegerDatatypeRefsetIdentifier(String integerDatatypeRefsetIdentifier) {
		this.integerDatatypeRefsetIdentifier = integerDatatypeRefsetIdentifier;
	}

	/**
	 * The ID of the datetime datatype reference set identifier concept
	 * 
	 * @return the datetimeDatatypeRefsetIdentifier
	 */
	@JsonProperty("datetimeDataTypeRefsetIdentifier")
	public String getDatetimeDatatypeRefsetIdentifier() {
		return datetimeDatatypeRefsetIdentifier;
	}

	/**
	 * Sets the ID of the datetime datatype reference set identifier concept
	 * 
	 * @param datetimeDatatypeRefsetIdentifier the datetimeDatatypeRefsetIdentifier to set
	 */
	@JsonProperty("datetimeDataTypeRefsetIdentifier")
	public void setDatetimeDatatypeRefsetIdentifier(String datetimeDatatypeRefsetIdentifier) {
		this.datetimeDatatypeRefsetIdentifier = datetimeDatatypeRefsetIdentifier;
	}
	
}
