package com.b2international.snowowl.snomed.api.impl.domain.browser;

import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserTerm;
import com.b2international.snowowl.snomed.core.domain.SnomedDescription;

public class SnomedBrowserTerm implements ISnomedBrowserTerm {

	private String term;
	private String lang;

	public SnomedBrowserTerm() {
	}

	public SnomedBrowserTerm(String term, String lang) {
		this.term = term;
		this.lang = lang;
	}

	public SnomedBrowserTerm(SnomedDescription desription) {
		this(desription.getTerm(), desription.getLanguageCode());
	}

	/**
	 * Fallback constructor for when no description is available
	 * @param conceptId
	 */
	public SnomedBrowserTerm(String conceptId) {
		this(conceptId, "en");
	}
	
	@Override
	public String getTerm() {
		return term;
	}
	
	@Override
	public String getLang() {
		return lang;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("SnomedBrowserTerm [term=");
		builder.append(term);
		builder.append(", lang=");
		builder.append(lang);
		builder.append("]");
		return builder.toString();
	}

}
