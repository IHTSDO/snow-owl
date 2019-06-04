package com.b2international.snowowl.snomed.api.domain.browser;

/**
 * Holder for an FSN or PT term with langauge code.
 */
public interface ISnomedBrowserTerm {

	/** @return the FSN or PT's term */
	String getTerm();

	/** @return the FSN or PT's language code */
	String getLang();
	
}
