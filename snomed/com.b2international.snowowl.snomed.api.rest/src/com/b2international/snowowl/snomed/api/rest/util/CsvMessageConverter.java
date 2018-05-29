package com.b2international.snowowl.snomed.api.rest.util;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
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
import com.b2international.snowowl.snomed.api.rest.domain.SnomedCsvConcept;
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
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@SuppressWarnings("rawtypes")
public class CsvMessageConverter extends AbstractHttpMessageConverter<CollectionResource> {
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
			
			output.getHeaders().setContentType(MEDIA_TYPE);
			output.getHeaders().set(CONTENT_DISPOSITION, ATTACHMENT);
			
			final Class<?> clazz = items.iterator().next().getClass();
			CsvSchema schema = mapper.schemaFor(getClassForConversion(clazz)).withHeader().withColumnSeparator('\t');
			
			final Optional<SnomedConcept> isConcept = items
					.stream()
					.findFirst()
					.filter(SnomedConcept.class::isInstance)
					.map(SnomedConcept.class::cast);
			
			if (isConcept.isPresent() && isConcept.get().getDescriptions() != null) {
				final Set<SnomedConcept> conceptItems = items
						.stream()
						.map(SnomedConcept.class::cast)
						.collect(Collectors.toSet());
				
				final Set<String> headers = Sets.newLinkedHashSet(ImmutableList.of("id", "fsn", "effectiveTime", "active", "moduleId", "definitionStatus"));

				addPtHeaders(conceptItems, headers);
				
				final Builder schemaBuilder = CsvSchema.builder();
				final List<String> headerList = Lists.newArrayList(headers);
				for (int i = 0; i < headerList.size(); i++) {
					schemaBuilder.addColumn(headerList.get(i));
				}
				final List<ObjectNode> nodes = Lists.newArrayList();
				schema = schemaBuilder.build().withHeader().withColumnSeparator('\t');
				createNodes(conceptItems, nodes);
				ObjectWriter writer = mapper.writer(schema);
				writer.writeValue(output.getBody(), nodes);
				return;
			}
		
			items = convert(items, clazz);
			
			ObjectWriter writer = mapper.writer(schema);
			writer.writeValue(output.getBody(), items);
		}
	}

	private void addPtHeaders(final Set<SnomedConcept> conceptItems, final Set<String> headers) {
		for (SnomedConcept concept : conceptItems) {
			for (SnomedDescription description : concept.getDescriptions()) { 
				for (Entry<String, Acceptability> entry : description.getAcceptabilityMap().entrySet()) {
					if (description.getTypeId().equals(Concepts.SYNONYM) && entry.getValue().equals(Acceptability.PREFERRED)) {
						headers.add(String.format("pt_%s", entry.getKey()));
					}
				}
			}
		}
	}

	private void createNodes(final Set<SnomedConcept> conceptItems, final List<ObjectNode> nodes) {
		conceptItems.forEach(concept -> {
			final ObjectNode node = mapper.createObjectNode();
			
			node.put("id", concept.getId());
			node.put("fsn", concept.getFsn() == null ? "" : concept.getFsn().getTerm());
			node.put("effectiveTime", EffectiveTimes.format(concept.getEffectiveTime(), DateFormats.SHORT));
			node.put("active", Boolean.toString(concept.isActive()));
			node.put("moduleId", concept.getModuleId());
			node.put("definitionStatus", concept.getDefinitionStatus().toString());
			
			concept.getDescriptions().forEach(description -> {
				for (Entry<String, Acceptability> entry : description.getAcceptabilityMap().entrySet()) {
					if (description.getTypeId().equals(Concepts.SYNONYM) && entry.getValue().equals(Acceptability.PREFERRED)) {
						node.put(String.format("pt_%s", entry.getKey()), description.getTerm());
					}
				}
					
			});
			nodes.add(node);
		});
	}

	private Class<?> getClassForConversion(Class<?> clazz) {
		if (clazz.isAssignableFrom(SnomedConcept.class)) {
			return SnomedCsvConcept.class;
		}
		return clazz;
	}

	private Collection<Object> convert(Collection<Object> items, Class<?> clazz) { 
		final List<Object> convertedItems = new ArrayList<>();

		for (Object item : items) {
			if (item instanceof SnomedConcept) {
				SnomedConcept concept = (SnomedConcept) item;
				convertedItems.add(convertConcept(concept));
			} else {
				convertedItems.add(item);
			}
		}
		
		return convertedItems;
	}
	
	private SnomedCsvConcept convertConcept(SnomedConcept concept) {
		
		return new SnomedCsvConcept(
				concept.getId(),
				EffectiveTimes.format(concept.getEffectiveTime(), DateFormats.SHORT),
				concept.isActive(),
				concept.getModuleId(),
				concept.getDefinitionStatus().name(),
				concept.getFsn() != null ? concept.getFsn().getTerm() : "");
	}
	
	@Override
	protected CollectionResource readInternal(Class<? extends CollectionResource> arg0, HttpInputMessage arg1)
			throws IOException, HttpMessageNotReadableException {
		throw new NotImplementedException();
	}

}
