package com.b2international.snowowl.snomed.api.rest.util;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Map;
import java.util.TimeZone;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import com.b2international.snowowl.core.domain.CollectionResource;
import com.b2international.snowowl.core.exceptions.NotImplementedException;
import com.b2international.snowowl.snomed.api.rest.domain.CollectionResourceMixin;
import com.b2international.snowowl.snomed.api.rest.domain.ISnomedComponentMixin;
import com.b2international.snowowl.snomed.common.SnomedRf2Headers;
import com.b2international.snowowl.snomed.core.domain.SnomedComponent;
import com.b2international.snowowl.snomed.core.domain.SnomedConcept;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator.Feature;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.collect.Maps;

@SuppressWarnings("rawtypes")
public class CsvMessageConverter extends AbstractHttpMessageConverter<CollectionResource> {
	private static final String DEFINITION_STATUS = "definitionStatus";
	private static final String ATTACHMENT = "attachment";
	private static final String CONTENT_DISPOSITION = "Content-Disposition";
	private static final MediaType MEDIA_TYPE = new MediaType("text", "csv", Charset.forName("utf-8"));

	private CsvMapper mapper;
	private Map<Class<?>, CsvSchema> predefinedSchemas;

	public CsvMessageConverter() {
		super(MEDIA_TYPE);
		this.mapper = initCsvMapper();
		this.predefinedSchemas = initSchemas();
	}

	@Override
	protected boolean supports(Class<?> clazz) {
		return CollectionResource.class.isAssignableFrom(clazz);
	}

	private CsvMapper initCsvMapper() {
		final CsvMapper csvMapper = new CsvMapper();
		csvMapper.registerModule(new GuavaModule());
		csvMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
		csvMapper.configure(Feature.IGNORE_UNKNOWN, true);
		final ISO8601DateFormat df = new ISO8601DateFormat();
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
		csvMapper.setDateFormat(df);
		csvMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		csvMapper.addMixIn(CollectionResource.class, CollectionResourceMixin.class);
		csvMapper.addMixIn(SnomedComponent.class, ISnomedComponentMixin.class);
		return csvMapper;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void writeInternal(CollectionResource response, HttpOutputMessage output) throws IOException, HttpMessageNotWritableException {
		final Collection<Object> items = response.getItems();
		if (!items.isEmpty()) {
			output.getHeaders().setContentType(MEDIA_TYPE);
			output.getHeaders().set(CONTENT_DISPOSITION, ATTACHMENT);

			final Class<?> clazz = items.iterator().next().getClass();
			CsvSchema schema = predefinedSchemas.get(clazz);

			if (schema == null) {
				schema = mapper.schemaFor(clazz).withHeader().withColumnSeparator('\t');
			}
			ObjectWriter writer = mapper.writer(schema);
			writer.writeValue(output.getBody(), items);
		}
	}

	@Override
	protected CollectionResource readInternal(Class<? extends CollectionResource> arg0, HttpInputMessage arg1)
			throws IOException, HttpMessageNotReadableException {
		throw new NotImplementedException();
	}

	private Map<Class<?>, CsvSchema> initSchemas() {
		final Map<Class<?>, CsvSchema> predefinedSchemas = Maps.newHashMap();
		final CsvSchema snomedConceptCsvSchema = createSchemaForSnomedConcept();
		predefinedSchemas.put(SnomedConcept.class, snomedConceptCsvSchema);
		return predefinedSchemas;
	}

	private CsvSchema createSchemaForSnomedConcept() {
		final CsvSchema csvSnomedConceptSchema = CsvSchema.builder()
				.addColumn(SnomedRf2Headers.FIELD_ID)
				.addColumn(SnomedRf2Headers.FIELD_EFFECTIVE_TIME)
				.addBooleanColumn(SnomedRf2Headers.FIELD_ACTIVE)
				.addColumn(SnomedRf2Headers.FIELD_MODULE_ID)
				.addColumn(DEFINITION_STATUS)
				.build()
				.withHeader()
				.withColumnSeparator('\t');
		
		return csvSnomedConceptSchema;
	}

}
