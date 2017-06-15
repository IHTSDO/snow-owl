package com.b2international.snowowl.snomed.api.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.b2international.commons.http.ExtendedLocale;
import com.b2international.snowowl.core.domain.IComponentRef;
import com.b2international.snowowl.datastore.server.domain.ComponentRef;
import com.b2international.snowowl.snomed.api.impl.domain.ISnomedConceptMin;
import com.b2international.snowowl.snomed.core.domain.SnomedDescription;
import com.b2international.snowowl.snomed.datastore.SnomedDatastoreActivator;

public class SnomedServiceHelper {

	public static IComponentRef createComponentRef(final String branchPath, final String componentId) {
		final ComponentRef conceptRef = new ComponentRef(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath, componentId);
		conceptRef.checkStorageExists();
		return conceptRef;
	}
	
	protected static void populateConceptTerms(Collection<ISnomedConceptMin> concepts, List<ExtendedLocale> extendedLocales, DescriptionService descriptionService) {
		Set<String> allConceptIds = new HashSet<>();
		for (ISnomedConceptMin conceptMin : concepts) {
			allConceptIds.add(conceptMin.getId());
		}
		final Map<String, SnomedDescription> fsns = descriptionService.getFullySpecifiedNames(allConceptIds, extendedLocales);
		for (ISnomedConceptMin conceptMin : concepts) {
			final SnomedDescription iSnomedDescription = fsns.get(conceptMin.getId());
			if (iSnomedDescription != null && iSnomedDescription.getTerm() != null) {
				conceptMin.setTerm(iSnomedDescription.getTerm());
			} else {
				conceptMin.setTerm(conceptMin.getId());
			}
		}
	}

}
