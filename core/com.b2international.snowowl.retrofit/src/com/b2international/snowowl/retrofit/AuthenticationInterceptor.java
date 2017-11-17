/*
 * Copyright 2017 B2i Healthcare Pte Ltd, http://b2i.sg
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

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * @since 5.10.18
 */
public class AuthenticationInterceptor implements Interceptor {

	private final String authToken;

	public AuthenticationInterceptor(final String token) {
		this.authToken = token;
	}

	@Override
	public Response intercept(final Chain chain) throws IOException {
		return chain.proceed(chain.request().newBuilder().header("Authorization", authToken).build());
	}

}
