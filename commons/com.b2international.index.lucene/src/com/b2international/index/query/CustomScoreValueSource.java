/*
 * Copyright 2017 B2i Healthcare Pte Ltd, http://b2i.sg
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
package com.b2international.index.query;

import static com.google.common.collect.Maps.newHashMap;

import java.io.IOException;
import java.util.Map;

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.queries.function.FunctionValues;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.queries.function.docvalues.FloatDocValues;
import org.apache.lucene.queries.function.valuesource.VectorValueSource;

import com.b2international.index.ScriptEngine;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import groovy.lang.Binding;
import groovy.lang.Script;

/**
 * @since 5.10
 */
public class CustomScoreValueSource extends VectorValueSource {

	private final String script;
	private final Map<String, ValueSource> sources;
	private final Map<String, ? extends Object> scriptParams;
	private final ScriptEngine scriptEngine;

	public CustomScoreValueSource(String script, final Map<String, ? extends Object> scriptParams, Map<String, ValueSource> sources, ScriptEngine scriptEngine) {
		super(ImmutableList.copyOf(sources.values()));
		this.script = script;
		this.scriptParams = scriptParams;
		this.sources = sources;
		this.scriptEngine = scriptEngine;
	}
	
	@Override
	public FunctionValues getValues(Map context, LeafReaderContext readerContext) throws IOException {
		final Map<String, FunctionValues> fieldValues = newHashMap();
		for (String field : sources.keySet()) {
			fieldValues.put(field, sources.get(field).getValues(context, readerContext));
		}
		return new FloatDocValues(this) {
			@Override
			public float floatVal(int doc) {
				final ImmutableMap.Builder<String, Object> ctx = ImmutableMap.builder();
				final Map<String, Object> _source = newHashMap();
				
				for (String field : sources.keySet()) {
					final FunctionValues value = fieldValues.get(field);
					if ("_score".equals(field)) {
						ctx.put(field, value.objectVal(doc));
					} else {
						_source.put(field, ImmutableMap.of("value", value.objectVal(doc)));
					}
				}
				ctx.put("doc", _source);
				ctx.put("params", scriptParams);
				
				final Binding binding = new Binding(newHashMap(ctx.build()));
				final Script compiledScript = scriptEngine.compile(script);
				compiledScript.setBinding(binding);
				return ((Number) compiledScript.run()).floatValue();
			}
		};
	}

}
