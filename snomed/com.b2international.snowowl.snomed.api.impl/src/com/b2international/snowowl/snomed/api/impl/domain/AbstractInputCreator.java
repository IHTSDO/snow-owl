package com.b2international.snowowl.snomed.api.impl.domain;

import java.util.Map;

import com.b2international.snowowl.snomed.SnomedConstants;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserComponent;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

public abstract class AbstractInputCreator {

	protected String getModuleOrDefault(ISnomedBrowserComponent component) {
		final String moduleId = component.getModuleId();
		return moduleId != null ? moduleId : SnomedConstants.Concepts.MODULE_SCT_CORE;
	}
	
	protected <K, V> Map<K, V> nullToEmptyMap(final Map<K, V> map) {
		return map != null ? map : ImmutableMap.<K, V>of();
	}
	
	protected <K, V> Multimap<K, V> nullToEmptyMultimap(final Multimap<K, V> multimap) {
		return multimap != null ? multimap : ImmutableMultimap.<K, V>of();
	}

}
