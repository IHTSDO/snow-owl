package com.b2international.snowowl.snomed.api.domain.browser;

import java.util.List;

public interface ISnomedBrowserBulkChangeRun {

	String getId();

	SnomedBrowserBulkChangeStatus getStatus();

	/**
	 * Returns all the concept ids affected by the bulk change (either create or update requests)
	 * 
	 * @return a list of concept ids
	 */
	List<String> getConceptIds();

	/**
	 * If concepts() expand parameter is used, all affected concepts are expanded and returned through this method as {@link ISnomedBrowserConcept}s
	 * 
	 * @return
	 */
	List<ISnomedBrowserConcept> getConcepts();

}
