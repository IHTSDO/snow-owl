package com.b2international.snowowl.snomed.api.impl;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.b2international.commons.http.ExtendedLocale;
import com.b2international.snowowl.core.ApplicationContext;
import com.b2international.snowowl.core.branch.Branch;
import com.b2international.snowowl.core.exceptions.ComponentNotFoundException;
import com.b2international.snowowl.datastore.request.SearchIndexResourceRequest;
import com.b2international.snowowl.eventbus.IEventBus;
import com.b2international.snowowl.snomed.SnomedConstants.Concepts;
import com.b2international.snowowl.snomed.api.impl.domain.Predicate;
import com.b2international.snowowl.snomed.core.domain.SnomedConcept;
import com.b2international.snowowl.snomed.core.domain.SnomedConcepts;
import com.b2international.snowowl.snomed.core.domain.constraint.SnomedConstraint;
import com.b2international.snowowl.snomed.core.domain.constraint.SnomedConstraints;
import com.b2international.snowowl.snomed.core.domain.constraint.SnomedRelationshipPredicate;
import com.b2international.snowowl.snomed.datastore.SnomedDatastoreActivator;
import com.b2international.snowowl.snomed.datastore.index.constraint.SnomedConstraintPredicateType;
import com.b2international.snowowl.snomed.datastore.request.SnomedRequests;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

public class SnomedMrcmService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SnomedMrcmService.class);
	
	@Resource
	private IEventBus bus;

	public List<Predicate> getPredicates(String conceptId) {
		
		final String branchPath = Branch.MAIN_PATH; // XXX is it expected that this method evaluates always against MAIN?
		
		Collection<SnomedConstraint> constraints = SnomedRequests.prepareGetConcept(conceptId)
			.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
			.execute(ApplicationContext.getServiceForClass(IEventBus.class))
			.thenWith( concept -> {
				return SnomedRequests.prepareGetApplicablePredicates(
						branchPath,
						singleton(concept.getId()),
						SnomedConcept.GET_ANCESTORS.apply(concept), // includes stated and inferred ancestors
						emptySet(),
						emptySet());
			}).getSync();
		
		return constraints.stream()
			.map(SnomedConstraint::getPredicate)
			.filter(SnomedRelationshipPredicate.class::isInstance)
			.map(SnomedRelationshipPredicate.class::cast)
			.map( constraintPredicate -> {
				Predicate predicate = new Predicate();
				predicate.setRelationshipTypeExpression(constraintPredicate.getAttributeExpression());
				predicate.setRelationshipValueExpression(constraintPredicate.getRangeExpression());
				return predicate;
			})
			.collect(toList());
		
	}

	public SnomedConcepts getDomainAttributes(String branchPath, List<String> parentIds, 
			int offset, int limit, final List<ExtendedLocale> locales, final String expand) {

		String ecl;
		
		if (!parentIds.isEmpty()) {
			
			Collection<SnomedConstraint> constraints = SnomedRequests.prepareGetApplicablePredicates(
					branchPath,
					emptySet(),
					ImmutableSet.copyOf(parentIds),
					emptySet(),
					emptySet())
				.getSync();
			
			Set<String> typeExpressions = constraints.stream()
				.map(SnomedConstraint::getPredicate)
				.filter(SnomedRelationshipPredicate.class::isInstance)
				.map(SnomedRelationshipPredicate.class::cast)
				.map( SnomedRelationshipPredicate::getAttributeExpression)
				.collect(toSet());
			
			if (typeExpressions.isEmpty()) {
				return new SnomedConcepts(limit, 0);
			} else {
				ecl = Joiner.on(" OR ").join(typeExpressions);
			}
			
		} else {
			ecl = Concepts.IS_A;
		}
		
		return SnomedRequests
				.prepareSearchConcept()
				.setLimit(limit)
				.filterByEcl(ecl)
				.filterByActive(true)
				.setExpand(expand)
				.setLocales(locales)
				.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
				.execute(bus)
				.getSync();
	}

	public SnomedConcepts getAttributeValues(String branchPath, String attributeId, String termPrefix, 
			int offset, int limit, List<ExtendedLocale> locales, String expand) {
		
		final Collection<String> ancestorIds = SnomedRequests.prepareGetConcept(attributeId)
				.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
				.execute(bus)
				.then(concept -> {
					Set<String> ancestors = Sets.newHashSet();
					for (long parentId : concept.getParentIds()) {
						ancestors.add(Long.toString(parentId));
					}
					for (long ancestorId : concept.getAncestorIds()) {
						ancestors.add(Long.toString(ancestorId));
					}
					return ancestors;
				})
				.getSync();
		
		String relationshipValueExpression = null;
		String relationshipTypeExpression = null;
		
		SnomedConstraints constraints = SnomedRequests.prepareSearchConstraint()
				.all()
				.filterByType(SnomedConstraintPredicateType.RELATIONSHIP)
				.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
				.execute(bus)
				.getSync();
		
		for (SnomedConstraint constraint : constraints) {
			
			if (constraint.getPredicate() instanceof SnomedRelationshipPredicate) {
				
				SnomedRelationshipPredicate relationshipPredicate = (SnomedRelationshipPredicate) constraint.getPredicate();
				
				relationshipTypeExpression = relationshipPredicate.getAttributeExpression();
				
				if (relationshipTypeExpression.startsWith("<")) {
					
					String relationshipTypeId = relationshipTypeExpression.replace("<", "");
					
					if ((relationshipTypeExpression.startsWith("<<")
							&& (relationshipTypeId.equals(attributeId) || ancestorIds.contains(relationshipTypeId)))
							|| ancestorIds.contains(relationshipTypeId)) {
						relationshipValueExpression = relationshipPredicate.getRangeExpression();
						break;
					}
					
				} else if (relationshipTypeExpression.equals(attributeId)) {
					relationshipValueExpression = relationshipPredicate.getRangeExpression();
					break;
				}
				
			}
			
		}
		
		if (relationshipValueExpression == null) {
			LOGGER.error("No MRCM predicate found for attribute {}", attributeId);
			throw new ComponentNotFoundException("MRCM predicate for attribute", attributeId);
		}
		
		LOGGER.info("Matched attribute predicate for attribute {}, type expression '{}', value expression '{}'", attributeId, relationshipTypeExpression, relationshipValueExpression);
		
		return SnomedRequests
				.prepareSearchConcept()
				.setLimit(limit)
				.filterByEcl(relationshipValueExpression)
				.filterByTerm(termPrefix)
				.filterByActive(true)
				.setExpand(expand)
				.setLocales(locales)
				.sortBy(SearchIndexResourceRequest.SCORE)
				.build(SnomedDatastoreActivator.REPOSITORY_UUID, branchPath)
				.execute(bus)
				.getSync();
	}
}
