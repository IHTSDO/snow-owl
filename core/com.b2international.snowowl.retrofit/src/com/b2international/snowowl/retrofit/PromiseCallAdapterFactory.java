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

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Optional;

import com.b2international.snowowl.core.events.util.Promise;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.ResponseBody;
import retrofit2.CallAdapter;
import retrofit2.Retrofit;

/**
 * @since 5.10.13
 */
public final class PromiseCallAdapterFactory extends CallAdapter.Factory {

	private final ObjectMapper mapper;
	private final Class<? extends Error> errorType;

	public PromiseCallAdapterFactory(final ObjectMapper mapper, final Class<? extends Error> errorType) {
		this.mapper = mapper;
		this.errorType = errorType;
	}
	
	@Override
	public CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {
		if (getRawType(returnType) != Promise.class) {
			return null;
		}
		
		if (!(returnType instanceof ParameterizedType)) {
			throw new IllegalStateException("Promise must have generic type (e.g., Promise<ResponseBody>)");
		}
		
		Optional<Annotation> headerAnnotation = Arrays.asList(annotations).stream().filter(annotation -> annotation instanceof ExtractHeaderProperty)
				.findFirst();

		if (headerAnnotation.isPresent()) {
			String property = ((ExtractHeaderProperty) headerAnnotation.get()).value();
			return new PromiseCallAdapter<ResponseBody, String>(ResponseBody.class, mapper, errorType, property);
		}
		
		Type responseType = getParameterUpperBound(0, (ParameterizedType) returnType);
		return new PromiseCallAdapter<>(responseType, mapper, errorType);
	}

}