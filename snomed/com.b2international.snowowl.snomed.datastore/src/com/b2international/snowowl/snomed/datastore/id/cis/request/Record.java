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
package com.b2international.snowowl.snomed.datastore.id.cis.request;

import java.util.UUID;

/**
 * @since 4.5
 */
public class Record {

	private String sctid;
	private String systemId;

	public Record(final String sctid) {
		this(sctid, UUID.randomUUID().toString());
	}
	
	public Record(final String sctid, final String systemId) {
		this.sctid = sctid;
		this.systemId = systemId;
	}

	public String getSctid() {
		return sctid;
	}

	public void setSctid(String sctid) {
		this.sctid = sctid;
	}

	public String getSystemId() {
		return systemId;
	}

	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}

}
