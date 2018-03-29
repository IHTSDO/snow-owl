package com.b2international.snowowl.snomed.api.rest.util;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
import com.b2international.snowowl.snomed.api.rest.domain.SnomedCsvConcept;
import com.b2international.snowowl.snomed.core.domain.SnomedConcept;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.datatype.guava.GuavaModule;

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
			
			items = convert(items, clazz);
			
			ObjectWriter writer = mapper.writer(schema);
			writer.writeValue(output.getBody(), items);
		}
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
