/*
 * Copyright 2011-2016 B2i Healthcare Pte Ltd, http://b2i.sg
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

import java.io.Serializable;

import com.b2international.snowowl.core.ServiceProvider;
import com.b2international.snowowl.core.events.util.Promise;
import com.b2international.snowowl.eventbus.IEventBus;

/**
 * A {@link Request} represents an executable form of user intent. They can be executed in a specific context usually within a {@link ServiceProvider}
 * . Executing a {@link Request} will result in either a success or failure. Success usually returns the requested object or success message while
 * failure usually delivers an error object or {@link Throwable} to the caller.
 * <p>
 * If you have a reference to the context where an instance of {@link Request} can be executed, then you can immediately execute it via the
 * {@link #execute(ServiceProvider)} method, this is basically the same as invoking a function. If you don't have the required context, then you can
 * dispatch it via the following methods using a dispatcher (currently {@link IEventBus} is supported), which will deliver the message to the owner of
 * the context and execute your {@link Request}:
 * <ul>
 * <li>{@link #execute(IEventBus)} - async execution, will return with a {@link Promise}</li>
 * <li>{@link #execute(C)} - sync execution, will return with the response or throws exception if fails</li>
 * </p>
 *
 * @since 4.5
 * @param <C>
 *            - the type of context where this {@link Request} can be executed
 * @param <R>
 *            - the type of the resource aka the response
 */
public interface Request<C extends ServiceProvider, R> extends Serializable {

	/**
	 * Executes this action on the given {@link ExecutionContext} directly without dispatching it.
	 *
	 * @param context
	 *            - the context within this {@link Request} can be executed
	 * @return - the result of the {@link Request}, never <code>null</code>.
	 */
	R execute(C context);
	
}
