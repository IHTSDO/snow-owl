package com.b2international.snowowl.snomed.api.validation;

import java.util.List;

import com.b2international.commons.http.ExtendedLocale;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserConcept;

public interface ISnomedBrowserValidationService {

	List<ISnomedInvalidContent> validateConcepts(String branchPath, List<? extends ISnomedBrowserConcept> concepts, List<ExtendedLocale> locales);

	int reloadRules();

}
