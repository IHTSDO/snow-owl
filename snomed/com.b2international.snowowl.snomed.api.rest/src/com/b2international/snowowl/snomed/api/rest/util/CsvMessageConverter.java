package com.b2international.snowowl.snomed.api.rest.util;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import com.b2international.snowowl.core.date.DateFormats;
import com.b2international.snowowl.core.date.EffectiveTimes;
import com.b2international.snowowl.core.domain.CollectionResource;
import com.b2international.snowowl.core.exceptions.NotImplementedException;
import com.b2international.snowowl.snomed.SnomedConstants.Concepts;
import com.b2international.snowowl.snomed.common.SnomedRf2Headers;
import com.b2international.snowowl.snomed.core.domain.Acceptability;
import com.b2international.snowowl.snomed.core.domain.SnomedConcept;
import com.b2international.snowowl.snomed.core.domain.SnomedDescription;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.csv.CsvSchema.Builder;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

@SuppressWarnings("rawtypes")
public class CsvMessageConverter extends AbstractHttpMessageConverter<CollectionResource> {
	private static final String DEFINITION_STATUS_FIELD = "definitionStatus";
	private static final String FSN_FIELD = "fsn";
	private static final MediaType MEDIA_TYPE = new MediaType("text", "csv", Charset.forName("utf-8"));
	private static final String CONTENT_DISPOSITION = "Content-Disposition";
	private static final String ATTACHMENT = "attachment";

	private CsvMapper mapper;

	public CsvMessageConverter() {
		super(MEDIA_TYPE);
		this.mapper = initCsvMapper();
	}

	@Override
	protected boolean supports(Class<?> clazz) {
		return CollectionResource.class.isAssignableFrom(clazz);
	}

	private CsvMapper initCsvMapper() {
		final CsvMapper csvMapper = new CsvMapper();
		csvMapper.registerModule(new GuavaModule());
		csvMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
		return csvMapper;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void writeInternal(CollectionResource response, HttpOutputMessage output) throws IOException, HttpMessageNotWritableException {
		Collection<Object> items = response.getItems();
		if (!items.isEmpty()) {
			final Class<?> clazz = items.iterator().next().getClass();
			final Collection<Object> results;
			final CsvSchema schema;

			output.getHeaders().setContentType(MEDIA_TYPE);
			output.getHeaders().set(CONTENT_DISPOSITION, ATTACHMENT);
			
			if (clazz.isAssignableFrom(SnomedConcept.class)) {
				final Set<SnomedConcept> conceptItems = items
						.stream()
						.map(SnomedConcept.class::cast)
						.collect(Collectors.toSet());
				
				final Set<String> headers = Sets.newLinkedHashSet(
						ImmutableList.of(
								SnomedRf2Headers.FIELD_ID, FSN_FIELD,
								SnomedRf2Headers.FIELD_EFFECTIVE_TIME,
								SnomedRf2Headers.FIELD_ACTIVE,
								SnomedRf2Headers.FIELD_MODULE_ID,
								DEFINITION_STATUS_FIELD));

				addPtHeaders(conceptItems, headers);
				
				final Builder schemaBuilder = CsvSchema.builder();
				
				headers.forEach(header ->  schemaBuilder.addColumn(header));
				
				results = mapConceptsToNodes(conceptItems);
				schema = schemaBuilder.build();
			} else {
				results = items;
				schema = mapper.schemaFor(clazz);
			}
			
			ObjectWriter writer = mapper.writer(schema.withHeader().withColumnSeparator('\t'));
			writer.writeValue(output.getBody(), results);
		}
	}

	private void addPtHeaders(final Set<SnomedConcept> conceptItems, final Set<String> headers) {
		for (SnomedConcept concept : conceptItems) {
			if (concept.getDescriptions() == null) {
				break;
			}
			for (SnomedDescription description : concept.getDescriptions()) { 
				for (Entry<String, Acceptability> entry : description.getAcceptabilityMap().entrySet()) {
					if (Concepts.SYNONYM.equals(description.getTypeId()) && Acceptability.PREFERRED.equals(entry.getValue())) {
						headers.add(String.format("pt_%s", entry.getKey()));
					}
				}
			}
		}
	}

	private Collection<Object> mapConceptsToNodes(final Set<SnomedConcept> conceptItems) {
		return conceptItems.stream().map(concept -> {
			final ObjectNode node = mapper.createObjectNode();
			
			node.put(SnomedRf2Headers.FIELD_ID, concept.getId());
			node.put(FSN_FIELD, concept.getFsn() == null ? "" : concept.getFsn().getTerm());
			node.put(SnomedRf2Headers.FIELD_EFFECTIVE_TIME, EffectiveTimes.format(concept.getEffectiveTime(), DateFormats.SHORT));
			node.put(SnomedRf2Headers.FIELD_ACTIVE, Boolean.toString(concept.isActive()));
			node.put(SnomedRf2Headers.FIELD_MODULE_ID, concept.getModuleId());
			node.put(DEFINITION_STATUS_FIELD, concept.getDefinitionStatus().toString());
			
			if (concept.getDescriptions() != null) {
				concept.getDescriptions().forEach(description -> {
					for (Entry<String, Acceptability> entry : description.getAcceptabilityMap().entrySet()) {
						if (Concepts.SYNONYM.equals(description.getTypeId())
								&& Acceptability.PREFERRED.equals(entry.getValue())) {
							node.put(String.format("pt_%s", entry.getKey()), description.getTerm());
						}
					}
				});
			}
			return node;
		}).collect(Collectors.toList());
	}
	
	@Override
	protected CollectionResource readInternal(Class<? extends CollectionResource> arg0, HttpInputMessage arg1) throws IOException, HttpMessageNotReadableException {
		throw new NotImplementedException();
	}

}
