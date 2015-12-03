/*
 * Copyright 2011-2015 B2i Healthcare Pte Ltd, http://b2i.sg
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
package com.b2international.snowowl.core.events;

import com.b2international.snowowl.core.ServiceProvider;
import com.b2international.snowowl.core.exceptions.ApiValidation;

/**
 * Base class of {@link RequestBuilder} implementations. Validates the resulting {@link Request} via {@link ApiValidation#checkInput(Object)}.
 * 
 * @since 4.5
 * @param <B>
 *            - the subclass type of this class
 * @param <C>
 *            - the required context
 * @param <R>
 *            - the response type
 */
public abstract class BaseRequestBuilder<B extends BaseRequestBuilder<B, C, R>, C extends ServiceProvider, R> implements RequestBuilder<C, R> {

	@Override
	public final Request<C, R> build() {
		return ApiValidation.checkInput(doBuild());
	}

	protected abstract Request<C, R> doBuild();

	protected final B getSelf() {
		return (B) this;
	}

}
