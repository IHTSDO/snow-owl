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
package com.b2international.snowowl.snomed.mrcm.core.server.setup;

import java.io.OutputStream;

import com.b2international.snowowl.snomed.mrcm.core.io.MrcmExportFormat;
import com.b2international.snowowl.snomed.mrcm.core.io.MrcmExporter;
import com.b2international.snowowl.snomed.mrcm.core.server.CsvMrcmExporter;
import com.b2international.snowowl.snomed.mrcm.core.server.XMIMrcmExporter;

/**
 * MRCM Exporter to delegate the export based on the export format.
 * 
 * @author bbanfai
 * @since 4.4
 */
public class MrcmExporterImpl implements MrcmExporter {

	@Override
	public void doExport(String user, OutputStream content, MrcmExportFormat exportFormat) {
		if (exportFormat == MrcmExportFormat.XMI) {
			new XMIMrcmExporter().doExport(user, content);
		} else if (exportFormat == MrcmExportFormat.CSV) {
			new CsvMrcmExporter().doExport(user, content);
		} else {
			throw new UnsupportedOperationException("No exporter is registered for " + exportFormat);
		}
	}

}
