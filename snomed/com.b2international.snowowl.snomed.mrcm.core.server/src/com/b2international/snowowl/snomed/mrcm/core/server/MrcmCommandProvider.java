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
package com.b2international.snowowl.snomed.mrcm.core.server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;

import com.b2international.commons.StringUtils;
import com.b2international.snowowl.core.date.Dates;
import com.b2international.snowowl.core.exceptions.BadRequestException;
import com.b2international.snowowl.core.users.SpecialUserStore;
import com.b2international.snowowl.server.console.CommandLineAuthenticator;
import com.b2international.snowowl.snomed.mrcm.core.io.MrcmExportFormat;
import com.google.common.base.Optional;
import com.google.common.collect.Sets;

/**
 * OSGi command provider for MRCM import and export.
 */
public class MrcmCommandProvider implements CommandProvider {

	private String exportFormatsString = StringUtils.toString(Sets.newHashSet(MrcmExportFormat.values()));
	
	public void _mrcm(final CommandInterpreter interpreter) {
		
		try {
			final String nextArgument = interpreter.nextArgument();
			
			if ("import".equals(nextArgument)) {
				_import(interpreter);
				return;
			} else if ("export".equals(nextArgument)) {
				_export(interpreter);
				return;
			} else {
				interpreter.println(getHelp());
			}
			
		} catch (final Throwable t) {
			interpreter.println(getHelp());
		}
	}
	
	public synchronized void _import(final CommandInterpreter interpreter) {
		
		final String filePath = interpreter.nextArgument();
		if (StringUtils.isEmpty(filePath)) {
			interpreter.println("MRCM import file path should be specified.");
			return;
		}
		
		final String targetPath = Optional.fromNullable(interpreter.nextArgument()).or("MAIN");
		
		final CommandLineAuthenticator authenticator = new CommandLineAuthenticator();
		if (!authenticator.authenticate(interpreter)) {
			interpreter.println("Authentication failed.");
			return;
		}
		
		final Path file = Paths.get(filePath);
		try (final InputStream content = Files.newInputStream(file, StandardOpenOption.READ)) {
			new XMIMrcmImporter().doImport(targetPath, authenticator.getUsername(), content);
		} catch (IOException e) {
			interpreter.printStackTrace(e);
		}
		
	}

	public synchronized void _export(final CommandInterpreter interpreter) {

		final String format = interpreter.nextArgument();
		if (StringUtils.isEmpty(format)) {
			interpreter.println("Format needs to be specified.");
			return;
		}
		
		
		MrcmExportFormat selectedFormat = null;
		try {
			selectedFormat = MrcmExportFormat.valueOf(format);
		} catch(IllegalArgumentException iae) {
			interpreter.println("Incorrect format: " + format + " Supported formats: " + exportFormatsString + ".");
			return;
		}
		
		final String destinationFolder = interpreter.nextArgument();
		
		if (StringUtils.isEmpty(destinationFolder)) {
			interpreter.println("Export destination folder needs to be specified.");
			return;
		}
		
		final String sourcePath = Optional.fromNullable(interpreter.nextArgument()).or("MAIN");
		final CommandLineAuthenticator authenticator = new CommandLineAuthenticator();
		if (!authenticator.authenticate(interpreter)) {
			interpreter.println("Authentication failed.");
			return;
		}
		
		// final String userId = authenticator.getUsername();
		final String user = SpecialUserStore.SYSTEM_USER_NAME;

		interpreter.println("Exporting MRCM rules (" + selectedFormat.name() + ")...");
		
		final Path outputFolder = Paths.get(destinationFolder);
		checkOutputFolder(outputFolder);
		final Path exportPath = outputFolder.resolve("mrcm_" + Dates.now() + "." + selectedFormat.name().toLowerCase());
		
		try (final OutputStream stream = Files.newOutputStream(exportPath, StandardOpenOption.CREATE)) {
			if (selectedFormat == MrcmExportFormat.XMI) {
				new XMIMrcmExporter().doExport(sourcePath, user, stream);
			} else if (selectedFormat == MrcmExportFormat.CSV) {
				new CsvMrcmExporter().doExport(sourcePath, user, stream);
			}
			interpreter.println("Exported MRCM rules to " + exportPath + " in " 
			+ selectedFormat.name() + " format.");
		} catch (IOException e) {
			interpreter.printStackTrace(e);
		}
	}
	
	private void checkOutputFolder(Path outputFolder) {
		final File folder = outputFolder.toFile();
		if (!folder.exists() || !folder.isDirectory()) {
			throw new BadRequestException("Export destination folder cannot be found.");
		}
		if (!folder.canRead()) {
			throw new BadRequestException("Cannot read destination folder.");
		}		
	}

	@Override
	public String getHelp() {
		return new StringBuilder("--- MRCM commands ---\n")
		.append("\tmrcm import [importFileAbsolutePath] [targetBranch] - Imports the MRCM rules from the given XMI source file.\n")
		.append("\tmrcm export ").append(exportFormatsString).append(" [destinationDirectoryPath] [sourceBranch] - Exports the MRCM rules in the given format to the destination folder.\n")
		.toString();
	}

}