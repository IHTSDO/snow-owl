/*
 * Copyright 2011-2017 B2i Healthcare Pte Ltd, http://b2i.sg
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
package com.b2international.snowowl.retrofit;

import java.io.IOException;
import java.lang.reflect.Type;

import com.b2international.snowowl.core.events.util.Promise;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;

import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Callback;

/**
 * @since 5.10.13
 * @param <R> from
 * @param <T> to
 */
public final class PromiseCallAdapter<R, T> implements CallAdapter<R, Promise<T>> {

	private final Type responseType;
	private final ObjectMapper mapper;
	private final Class<? extends Error> errorType;
	private String headerProperty;

	public PromiseCallAdapter(Type responseType, final ObjectMapper mapper, Class<? extends Error> errorType) {
		this(responseType, mapper, errorType, null);
	}
	
	public PromiseCallAdapter(Type responseType, final ObjectMapper mapper, Class<? extends Error> errorType, String headerProperty) {
		this.responseType = responseType;
		this.mapper = mapper;
		this.errorType = errorType;
		this.headerProperty = headerProperty;
	}

	@Override
	public Type responseType() {
		return responseType;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Promise<T> adapt(Call<R> call) {
		final Promise<T> promise = new Promise<>();
		call.enqueue(new Callback<R>() {
			@Override
			public void onResponse(Call<R> call, retrofit2.Response<R> response) {
				try {
					int status = response.code();
					if (response.isSuccessful()) {
						if (!Strings.isNullOrEmpty(headerProperty)) {
							promise.resolve((T) response.headers().get(headerProperty));
						} else {
							promise.resolve((T) response.body());
						}
					} else {
						promise.reject(mapper.readValue(response.errorBody().string(), errorType).toApiException(status));
					}
				} catch (IOException e) {
					promise.reject(e);
				}
			}

			@Override
			public void onFailure(Call<R> call, Throwable t) {
				promise.reject(t);
			}
		});
		
		return promise;
	}
}