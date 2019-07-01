package com.b2international.snowowl.snomed.api.impl.domain;

import static com.google.common.collect.Maps.newHashMap;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import com.b2international.snowowl.core.branch.Branch;
import com.b2international.snowowl.core.domain.TransactionContext;
import com.b2international.snowowl.core.events.Request;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserComponentWithId;
import com.b2international.snowowl.snomed.api.domain.browser.ISnomedBrowserConcept;
import com.b2international.snowowl.snomed.datastore.request.SnomedComponentCreateRequest;
import com.b2international.snowowl.snomed.datastore.request.SnomedComponentRequest;
import com.b2international.snowowl.snomed.datastore.request.SnomedComponentUpdateRequest;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

public class InputFactory {

	private final List<ComponentInputCreator<?, ?, ?>> creators;

	public InputFactory(final Branch branch) {
		creators = ImmutableList.of(
				new ConceptInputCreator(), 
				new DescriptionInputCreator(), 
				new RelationshipInputCreator(),
				new AxiomInputCreator(branch.path()));
	}

	public <I extends SnomedComponentCreateRequest> I createComponentInput(ISnomedBrowserComponentWithId component, Class<I> inputType) {
		return getInputDelegate(inputType).createInput(component, this);
	}

	public <I extends SnomedComponentCreateRequest> List<I> createComponentInputs(
			List<? extends ISnomedBrowserComponentWithId> newVersionComponents,
			Class<I> inputType) {
		
		return newVersionComponents.stream()
			.filter(component -> component.getId() == null)
			.map(component -> createComponentInput(component, inputType))
			.filter(Objects::nonNull)
			.collect(toList());
	}

	@SuppressWarnings("unchecked")
	public <U extends SnomedComponentUpdateRequest> U createComponentUpdate(ISnomedBrowserConcept existingVersion, ISnomedBrowserConcept newVersion, Class<U> updateType) {
		return updateType.cast(getUpdateDelegate(updateType).createUpdate(existingVersion, newVersion));
	}

	@SuppressWarnings("unchecked")
	public <U extends Request<TransactionContext, Boolean>> Map<String, U> createComponentUpdates(
			List<? extends ISnomedBrowserComponentWithId> existingVersions,
			List<? extends ISnomedBrowserComponentWithId> newVersions,
			Class<U> updateType) {

		Map<String, U> updateMap = newHashMap();
		
		Map<String, ? extends ISnomedBrowserComponentWithId> existingComponents = existingVersions.stream()
				.collect(toMap(ISnomedBrowserComponentWithId::getId, Function.identity()));
		
		Map<String, ? extends ISnomedBrowserComponentWithId> newComponents = newVersions.stream()
				.filter(component -> component.getId() != null)
				.collect(toMap(ISnomedBrowserComponentWithId::getId, Function.identity()));

		for (String id : existingComponents.keySet()) {
			
			if (newComponents.containsKey(id)) {
				ISnomedBrowserComponentWithId existingComponent = existingComponents.get(id);
				ISnomedBrowserComponentWithId newComponent = newComponents.get(id);
				final U update = (U) getUpdateDelegate(updateType).createUpdate(existingComponent, newComponent);
				if (update != null) {
					updateMap.put(id, update);
				}
			}
			
		}
		
		return updateMap;
	}

	public Set<String> getComponentDeletions(List<? extends ISnomedBrowserComponentWithId> existingVersion, List<? extends ISnomedBrowserComponentWithId> newVersion) {
		return Sets.difference(toIdSet(existingVersion), toIdSet(newVersion));
	}

	private Set<String> toIdSet(final List<? extends ISnomedBrowserComponentWithId> components) {
		return components.stream()
				.filter(component -> component.getId() != null)
				.map(ISnomedBrowserComponentWithId::getId).collect(toSet());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <I extends SnomedComponentCreateRequest, U extends SnomedComponentRequest<Boolean>> ComponentInputCreator<I, U, ISnomedBrowserComponentWithId> getInputDelegate(Class<I> inputType) {
		for (ComponentInputCreator creator : creators) {
			if (creator.canCreateInput(inputType)) {
				return creator;
			}
		}
		throw new RuntimeException("No ComponentInputCreator found for input type " + inputType);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <U extends Request<TransactionContext, Boolean>> ComponentInputCreator getUpdateDelegate(Class<U> updateType) {
		for (ComponentInputCreator creator : creators) {
			if (creator.canCreateUpdate(updateType)) {
				return creator;
			}
		}
		throw new RuntimeException("No ComponentInputCreator found for update type " + updateType);
	}
	
}
