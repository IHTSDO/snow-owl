package com.b2international.snowowl.datastore.server.snomed;

import static java.util.stream.Collectors.toSet;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.otf.owltoolkit.conversion.AxiomRelationshipConversionService;
import org.snomed.otf.owltoolkit.conversion.ConversionException;
import org.snomed.otf.owltoolkit.domain.AxiomRepresentation;

import com.b2international.commons.options.Options;
import com.b2international.commons.time.TimeUtil;
import com.b2international.snowowl.core.ApplicationContext;
import com.b2international.snowowl.datastore.index.RevisionDocument.Fields;
import com.b2international.snowowl.eventbus.IEventBus;
import com.b2international.snowowl.snomed.SnomedConstants.Concepts;
import com.b2international.snowowl.snomed.core.domain.SnomedConcept;
import com.b2international.snowowl.snomed.core.domain.SnomedCoreComponent;
import com.b2international.snowowl.snomed.core.domain.refset.SnomedReferenceSetMember;
import com.b2international.snowowl.snomed.datastore.SnomedDatastoreActivator;
import com.b2international.snowowl.snomed.datastore.index.entry.SnomedRefSetMemberIndexEntry;
import com.b2international.snowowl.snomed.datastore.request.SnomedRequests;
import com.b2international.snowowl.snomed.snomedrefset.SnomedRefSetType;
import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;

/**
 * @since 6.16.2
 */
public class SnomedBrowserAxiomConverter {

	private static final Logger LOGGER = LoggerFactory.getLogger(SnomedBrowserAxiomConverter.class);

	public AxiomRepresentation convertAxiomToRelationships(final String owlExpression, final String branchPath) throws ConversionException {

		final Stopwatch watch = Stopwatch.createStarted();

		final Set<Long> ungroupedAttributeIds = SnomedRequests.prepareSearchMember()
				.all()
				.filterByActive(true)
				.filterByProps(Options.builder()
						.put(SnomedRefSetMemberIndexEntry.Fields.MRCM_GROUPED, false)
						.build())
				.filterByRefSetType(SnomedRefSetType.MRCM_ATTRIBUTE_DOMAIN)
				.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
				.execute(getBus())
				.getSync()
				.stream()
				.map(SnomedReferenceSetMember::getReferencedComponent)
				.map(SnomedCoreComponent::getId)
				.map(Long::valueOf)
				.collect(Collectors.toSet());

		final AxiomRelationshipConversionService conversionService = withTccl(() -> new AxiomRelationshipConversionService(ungroupedAttributeIds));
		LOGGER.info("SNOMED OWL Toolkit axiom conversion service initialization took {}", TimeUtil.toString(watch));

		return conversionService.convertAxiomToRelationships(owlExpression);
	}

	public String convertRelationshipsToAxiom(final AxiomRepresentation axiomRepresentation, final String branchPath) {

		final Stopwatch watch = Stopwatch.createStarted();

		final Set<Long> ungroupedAttributes = SnomedRequests.prepareSearchMember()
				.all()
				.filterByActive(true)
				.filterByProps(Options.builder()
						.put(SnomedRefSetMemberIndexEntry.Fields.MRCM_GROUPED, false)
						.build())
				.filterByRefSetType(SnomedRefSetType.MRCM_ATTRIBUTE_DOMAIN)
				.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
				.execute(getBus())
				.getSync()
				.stream()
				.map(SnomedReferenceSetMember::getReferencedComponent)
				.map(SnomedCoreComponent::getId)
				.map(Long::valueOf)
				.collect(Collectors.toSet());

		final Set<Long> objectAttributes = SnomedRequests.prepareSearchConcept()
				.all()
				.filterByActive(true)
				.filterByStatedAncestor(Concepts.CONCEPT_MODEL_OBJECT_ATTRIBUTE)
				.setFields(Fields.ID)
				.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
				.execute(getBus())
				.getSync()
				.stream()
				.map(SnomedConcept::getId)
				.map(Long::valueOf)
				.collect(toSet());

		final Set<Long> dataAttributes = SnomedRequests.prepareSearchConcept()
				.all()
				.filterByActive(true)
				.filterByStatedAncestor(Concepts.CONCEPT_MODEL_DATA_ATTRIBUTE)
				.setFields(Fields.ID)
				.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
				.execute(getBus())
				.getSync()
				.stream()
				.map(SnomedConcept::getId)
				.map(Long::valueOf)
				.collect(toSet());

		final AxiomRelationshipConversionService conversionService = withTccl(() -> new AxiomRelationshipConversionService(ungroupedAttributes, objectAttributes, dataAttributes));
		LOGGER.info("SNOMED OWL Toolkit complex axiom conversion service initialization took {}", TimeUtil.toString(watch));

		return conversionService.convertRelationshipsToAxiom(axiomRepresentation);
	}

	private IEventBus getBus() {
		return ApplicationContext.getServiceForClass(IEventBus.class);
	}

	private static <T> T withTccl(final Callable<T> callable) {

		final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

		try {

			Thread.currentThread().setContextClassLoader(SnomedBrowserAxiomConverter.class.getClassLoader());

			try {
				return callable.call();
			} catch (final Exception e) {
				Throwables.propagateIfPossible(e);
				throw new RuntimeException(e);
			}

		} finally {
			Thread.currentThread().setContextClassLoader(contextClassLoader);
		}

	}
}
