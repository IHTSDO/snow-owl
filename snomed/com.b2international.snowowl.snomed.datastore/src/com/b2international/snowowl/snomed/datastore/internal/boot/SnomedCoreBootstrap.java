/*
 * Copyright 2011-2019 B2i Healthcare Pte Ltd, http://b2i.sg
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.b2international.snowowl.core.config.SnowOwlConfiguration;
import com.b2international.snowowl.core.setup.DefaultBootstrapFragment;
import com.b2international.snowowl.core.setup.Environment;
import com.b2international.snowowl.core.setup.ModuleConfig;
import com.b2international.snowowl.core.validation.eval.ValidationRuleEvaluator;
import com.b2international.snowowl.datastore.cdo.ICDORepository;
import com.b2international.snowowl.datastore.cdo.ICDORepositoryManager;
import com.b2international.snowowl.snomed.core.ecl.DefaultEclParser;
import com.b2international.snowowl.snomed.core.ecl.DefaultEclSerializer;
import com.b2international.snowowl.snomed.core.ecl.EclParser;
import com.b2international.snowowl.snomed.core.ecl.EclSerializer;
import com.b2international.snowowl.snomed.core.ql.DefaultSnomedQueryParser;
import com.b2international.snowowl.snomed.core.ql.DefaultSnomedQuerySerializer;
import com.b2international.snowowl.snomed.core.ql.SnomedQueryParser;
import com.b2international.snowowl.snomed.core.ql.SnomedQuerySerializer;
import com.b2international.snowowl.snomed.datastore.SnomedDatastoreActivator;
import com.b2international.snowowl.snomed.datastore.config.SnomedClassificationServiceConfiguration;
import com.b2international.snowowl.snomed.datastore.config.SnomedCoreConfiguration;
import com.b2international.snowowl.snomed.datastore.config.SnomedDroolsConfiguration;
import com.b2international.snowowl.snomed.datastore.id.assigner.SnomedNamespaceAndModuleAssignerProvider;
import com.b2international.snowowl.snomed.ecl.EclStandaloneSetup;
import com.b2international.snowowl.snomed.ql.QLStandaloneSetup;
import com.b2international.snowowl.snomed.validation.SnomedQueryValidationRuleEvaluator;
import com.google.common.base.Strings;
import com.google.inject.Injector;

/**
 * @since 3.4
 */
@ModuleConfig(fieldName = "snomed", type = SnomedCoreConfiguration.class)
public class SnomedCoreBootstrap extends DefaultBootstrapFragment {
	
	private static final Logger LOGGER = LoggerFactory.getLogger("bootstrap");

	@Override
	public void init(SnowOwlConfiguration configuration, Environment env) throws Exception {
		final SnomedCoreConfiguration coreConfig = configuration.getModuleConfig(SnomedCoreConfiguration.class);
		env.services().registerService(SnomedCoreConfiguration.class, coreConfig);
		
		if (coreConfig.getClassificationConfig() != null && coreConfig.getClassificationConfig().getExternalService() != null) {
			SnomedClassificationServiceConfiguration externalService = coreConfig.getClassificationConfig().getExternalService();
			if (!Strings.isNullOrEmpty(externalService.getUrl())) {
				LOGGER.info("External classification service url is set to {}", externalService.getUrl());
			}
		}
		
		if (coreConfig.getDroolsConfig() != null) {
			final SnomedDroolsConfiguration droolsConfig = coreConfig.getDroolsConfig();
			if (!Strings.isNullOrEmpty(droolsConfig.getRulesDirectory())) {
				LOGGER.info("Drools rules folder is set to {}", droolsConfig.getRulesDirectory());
			}
			if (!Strings.isNullOrEmpty(droolsConfig.getAwsKey()) && !Strings.isNullOrEmpty(droolsConfig.getAwsPrivateKey())
					&& !Strings.isNullOrEmpty(droolsConfig.getResourcesBucket()) && !Strings.isNullOrEmpty(droolsConfig.getResourcesPath())) {
				LOGGER.info("Additional drools engine resources are configured to be accessed via S3 bucket: '{}'", droolsConfig.getResourcesBucket());
			} else if (!Strings.isNullOrEmpty(droolsConfig.getTermValidationResourcesPath())) {
				LOGGER.info("Additional drools engine resources are configured to be accessed via local folder: '{}'", droolsConfig.getTermValidationResourcesPath());
			}
		}
		
		final Injector eclInjector = new EclStandaloneSetup().createInjectorAndDoEMFRegistration();
		env.services().registerService(EclParser.class, new DefaultEclParser(eclInjector.getInstance(IParser.class), eclInjector.getInstance(IResourceValidator.class)));
		env.services().registerService(EclSerializer.class, new DefaultEclSerializer(eclInjector.getInstance(ISerializer.class)));
		
		final Injector qlInjector = new QLStandaloneSetup().createInjectorAndDoEMFRegistration();
		env.services().registerService(SnomedQueryParser.class, new DefaultSnomedQueryParser(qlInjector.getInstance(IParser.class), qlInjector.getInstance(IResourceValidator.class)));
		env.services().registerService(SnomedQuerySerializer.class, new DefaultSnomedQuerySerializer(qlInjector.getInstance(ISerializer.class)));
		
		// register SNOMED CT Query based validation rule evaluator
		ValidationRuleEvaluator.Registry.register(new SnomedQueryValidationRuleEvaluator());

		env.services().registerService(SnomedNamespaceAndModuleAssignerProvider.class, SnomedNamespaceAndModuleAssignerProvider.INSTANCE);
	}

	@Override
	public void run(SnowOwlConfiguration configuration, Environment env, IProgressMonitor monitor) throws Exception {
		if (env.isServer() || env.isEmbedded()) {
			final SnomedCoreConfiguration snomedConfig = configuration.getModuleConfig(SnomedCoreConfiguration.class);
			final ICDORepository repository = env.service(ICDORepositoryManager.class).getByUuid(SnomedDatastoreActivator.REPOSITORY_UUID);
			repository.setReaderPoolCapacity(snomedConfig.getReaderPoolCapacity());
			repository.setWriterPoolCapacity(snomedConfig.getWriterPoolCapacity());
		}
	}
}
