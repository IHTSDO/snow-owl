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
package com.b2international.snowowl.snomed.datastore.internal.boot;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.xtext.parser.IParser;
import org.eclipse.xtext.serializer.ISerializer;
import org.eclipse.xtext.validation.IResourceValidator;

import com.b2international.snowowl.core.config.SnowOwlConfiguration;
import com.b2international.snowowl.core.setup.DefaultBootstrapFragment;
import com.b2international.snowowl.core.setup.Environment;
import com.b2international.snowowl.core.setup.ModuleConfig;
import com.b2international.snowowl.core.terminology.ComponentCategory;
import com.b2international.snowowl.snomed.core.ecl.DefaultEclParser;
import com.b2international.snowowl.snomed.core.ecl.DefaultEclSerializer;
import com.b2international.snowowl.snomed.core.ecl.EclParser;
import com.b2international.snowowl.snomed.core.ecl.EclSerializer;
import com.b2international.snowowl.snomed.core.lang.LanguageSetting;
import com.b2international.snowowl.snomed.core.lang.StaticLanguageSetting;
import com.b2international.snowowl.snomed.datastore.config.SnomedCoreConfiguration;
import com.b2international.snowowl.snomed.datastore.id.SnomedIdentifiers;
import com.b2international.snowowl.snomed.datastore.id.reservations.ISnomedIdentiferReservationService;
import com.b2international.snowowl.snomed.datastore.id.reservations.Reservation;
import com.b2international.snowowl.snomed.datastore.id.reservations.Reservations;
import com.b2international.snowowl.snomed.ecl.EclStandaloneSetup;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Injector;

/**
 * @since 3.4
 */
@ModuleConfig(fieldName = "snomed", type = SnomedCoreConfiguration.class)
public class SnomedCoreBootstrap extends DefaultBootstrapFragment {

	@Override
	public void init(SnowOwlConfiguration configuration, Environment env) throws Exception {
		final SnomedCoreConfiguration coreConfig = configuration.getModuleConfig(SnomedCoreConfiguration.class);
		env.services().registerService(SnomedCoreConfiguration.class, coreConfig);
		env.services().registerService(LanguageSetting.class, new StaticLanguageSetting(coreConfig.getLanguage(), SnomedCoreConfiguration.DEFAULT_LANGUAGE));
		final Injector injector = new EclStandaloneSetup().createInjectorAndDoEMFRegistration();
		env.services().registerService(EclParser.class, new DefaultEclParser(injector.getInstance(IParser.class), injector.getInstance(IResourceValidator.class)));
		env.services().registerService(EclSerializer.class, new DefaultEclSerializer(injector.getInstance(ISerializer.class)));
	}

	@Override
	public void run(SnowOwlConfiguration configuration, Environment env, IProgressMonitor monitor) throws Exception {
		final Reservation intMetadataReservation = Reservations.range(
				SnomedIdentifiers.MIN_INT_METADATA_ITEMID, // 900000000000000
				SnomedIdentifiers.MAX_INT_ITEMID, // 999999999999999
				null, // INT namespace 
				ImmutableSet.of(ComponentCategory.CONCEPT, ComponentCategory.DESCRIPTION, ComponentCategory.RELATIONSHIP));
		
		final ISnomedIdentiferReservationService reservationService = env.service(ISnomedIdentiferReservationService.class);
		reservationService.create("int_metadata", intMetadataReservation);
	}

}
