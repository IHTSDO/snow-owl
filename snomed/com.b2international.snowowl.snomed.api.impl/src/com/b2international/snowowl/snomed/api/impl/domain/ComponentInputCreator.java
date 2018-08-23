package com.b2international.snowowl.snomed.api.impl.domain;

import com.b2international.snowowl.core.domain.TransactionContext;
import com.b2international.snowowl.core.events.Request;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserComponentWithId;

public interface ComponentInputCreator<I extends Request<TransactionContext, String>, U extends Request<TransactionContext, Boolean>, T extends ISnomedBrowserComponentWithId> {

	I createInput(T newComponent, InputFactory inputFactory);

	U createUpdate(T existingComponent, T updatedComponent);
	
	boolean canCreateInput(Class<? extends Request<TransactionContext, String>> inputType);

	boolean canCreateUpdate(Class<? extends Request<TransactionContext, Boolean>> updateType);
	
}
