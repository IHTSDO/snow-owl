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
import com.b2international.snowowl.core.exceptions.ApiException;
import com.fasterxml.jackson.databind.ObjectMapper;

import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Callback;

/**
 * @since 5.10.13
 * @param <R>
 */
public final class PromiseCallAdapter<R> implements CallAdapter<R, Promise<R>> {

	/**
	 * @since 5.10.13
	 */
	public static interface Error {

		ApiException toApiException(int status);
		
	}
	
	private final Type responseType;
	private final ObjectMapper mapper;
	private final Class<? extends PromiseCallAdapter.Error> errorType;

	public PromiseCallAdapter(Type responseType, final ObjectMapper mapper, Class<? extends PromiseCallAdapter.Error> errorType) {
		this.responseType = responseType;
		this.mapper = mapper;
		this.errorType = errorType;
	}

	@Override
	public Type responseType() {
		return responseType;
	}

	@Override
	public Promise<R> adapt(Call<R> call) {
		final Promise<R> promise = new Promise<>();
		call.enqueue(new Callback<R>() {
			@Override
			public void onResponse(Call<R> call, retrofit2.Response<R> response) {
				try {
					int status = response.code();
					if (response.isSuccessful()) {
						promise.resolve(response.body());
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