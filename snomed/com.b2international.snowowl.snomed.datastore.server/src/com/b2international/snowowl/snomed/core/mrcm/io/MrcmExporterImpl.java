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
package com.b2international.snowowl.snomed.core.mrcm.io;

import java.io.OutputStream;

/**
 * MRCM Exporter to delegate the export based on the export format.
 * 
 * @author bbanfai
 * @since 4.4
 */
public class MrcmExporterImpl implements MrcmExporter {

	@Override
	public void doExport(String sourcePath, String user, OutputStream content, MrcmExportFormat exportFormat) {
		if (exportFormat == MrcmExportFormat.XMI) {
			new XMIMrcmExporter().doExport(sourcePath, user, content);
		} else if (exportFormat == MrcmExportFormat.CSV) {
			new CsvMrcmExporter().doExport(sourcePath, user, content);
		} else {
			throw new UnsupportedOperationException("No exporter is registered for " + exportFormat);
		}
	}

}
