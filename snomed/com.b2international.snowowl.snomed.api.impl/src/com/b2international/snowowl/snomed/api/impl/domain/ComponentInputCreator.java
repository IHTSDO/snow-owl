package com.b2international.snowowl.snomed.api.impl.domain;

import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserComponentWithId;
import com.b2international.snowowl.snomed.datastore.request.SnomedComponentUpdateRequest;
import com.b2international.snowowl.snomed.datastore.request.SnomedCoreComponentCreateRequest;

public interface ComponentInputCreator<I extends SnomedCoreComponentCreateRequest, U extends SnomedComponentUpdateRequest, T extends ISnomedBrowserComponentWithId> {

	I createInput(T newComponent, InputFactory inputFactory);

	U createUpdate(T existingComponent, T updatedComponent);
	
	boolean canCreateInput(Class<? extends SnomedCoreComponentCreateRequest> inputType);

	boolean canCreateUpdate(Class<? extends SnomedComponentUpdateRequest> updateType);
}
