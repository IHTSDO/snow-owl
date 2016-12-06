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

import com.b2international.snowowl.core.events.util.Promise;
import com.b2international.snowowl.eventbus.IEventBus;
import com.b2international.snowowl.eventbus.IHandler;
import com.b2international.snowowl.eventbus.IMessage;

/**
 * @since 5.0
 */
public final class AsyncRequest<R> {

	private final String address;
	private final Request<?, R> request;

	public AsyncRequest(String address, Request<?, R> request) {
		this.address = address;
		this.request = request;
	}
	
	public Promise<R> execute(IEventBus bus) {
		final Promise<R> promise = new Promise<>();
		final Class<R> responseType = ((BaseRequest<?, R>) request).getReturnType(); 
		bus.send(address, request, new IHandler<IMessage>() {
			@Override
			public void handle(IMessage message) {
				try {
					if (message.isSucceeded()) {
						promise.resolve(message.body(responseType));
					} else {
						promise.reject(message.body(Throwable.class, AsyncRequest.class.getClassLoader()));
					}
				} catch (Throwable e) {
					promise.reject(e);
				}
			}
		});
		return promise;
	}

}