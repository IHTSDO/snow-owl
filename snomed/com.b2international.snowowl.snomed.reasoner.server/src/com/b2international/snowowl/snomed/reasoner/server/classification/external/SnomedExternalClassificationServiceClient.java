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
package com.b2international.snowowl.snomed.reasoner.server.classification.external;

import com.b2international.snowowl.core.events.util.Promise;
import com.b2international.snowowl.retrofit.ExtractHeaderProperty;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Streaming;

/**
 * @since 5.10.13 
 */
public interface SnomedExternalClassificationServiceClient {

	@Multipart
	@POST("classification-service/classifications")
	@ExtractHeaderProperty("location")
	Promise<String> sendResults(
			@Part("previousRelease") RequestBody previousRelease,
			@Part MultipartBody.Part rf2Delta,
			@Part("branch") RequestBody branch,
			@Part("reasonerId") RequestBody reasonerId);
	
	@GET("classification-service/classifications/{id}")
	Promise<ExternalClassificationStatus> getClassification(@Path("id") String id);
	
	@Streaming
	@GET("classification-service/classifications/{id}/results/rf2")
	Promise<ResponseBody> getResult(@Path("id") String id);
	
}
