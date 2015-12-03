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
package com.b2international.snowowl.snomed.mrcm.core.server.setup;

import com.b2international.snowowl.core.api.SnowowlServiceException;
import com.b2international.snowowl.datastore.serviceconfig.AbstractServerServiceConfigJob;
import com.b2international.snowowl.snomed.mrcm.core.io.MrcmImporter;
import com.b2international.snowowl.snomed.mrcm.core.server.XMIMrcmImporter;

/**
 * @since 4.4
 */
public class MrcmImporterConfigJob extends AbstractServerServiceConfigJob<MrcmImporter> {

	public MrcmImporterConfigJob() {
		super("Configuring MRCM Importer...", "mrcm");
	}

	@Override
	protected Class<MrcmImporter> getServiceClass() {
		return MrcmImporter.class;
	}

	@Override
	protected MrcmImporter createServiceImplementation() throws SnowowlServiceException {
		return new XMIMrcmImporter();
	}

}
