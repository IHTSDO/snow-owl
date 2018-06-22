import com.b2international.index.Hits
import com.b2international.index.query.Expression
import com.b2international.index.query.Expressions
import com.b2international.index.query.Query
import com.b2international.index.revision.RevisionSearcher
import com.b2international.snowowl.core.ComponentIdentifier
import com.b2international.snowowl.core.validation.issue.IssueDetail
import com.b2international.snowowl.snomed.common.SnomedRf2Headers
import com.b2international.snowowl.snomed.common.SnomedTerminologyComponentConstants
import com.b2international.snowowl.snomed.datastore.index.entry.SnomedConceptDocument
import com.b2international.snowowl.snomed.datastore.index.entry.SnomedRelationshipIndexEntry
import com.google.common.collect.ImmutableMap
import com.google.common.collect.Lists

RevisionSearcher searcher = ctx.service(RevisionSearcher.class)

Iterable<Hits<String>> inactiveConceptBatches = searcher.scroll(Query.select(String.class)
		.from(SnomedConceptDocument.class)
		.fields(SnomedConceptDocument.Fields.ID)
		.where(SnomedConceptDocument.Expressions.inactive())
		.limit(10_000)
		.build())

List<IssueDetail> issueDetails = Lists.newArrayList()

inactiveConceptBatches.each({ conceptBatch ->
	List<String> inactiveConceptIds = conceptBatch.getHits()

	Expression invalidRelationshipExpression = Expressions.builder()
			.filter(SnomedRelationshipIndexEntry.Expressions.active())
			.should(SnomedRelationshipIndexEntry.Expressions.sourceIds(inactiveConceptIds))
			.should(SnomedRelationshipIndexEntry.Expressions.typeIds(inactiveConceptIds))
			.should(SnomedRelationshipIndexEntry.Expressions.destinationIds(inactiveConceptIds))
			.build()

	Iterable<Hits<String[]>> invalidRelationshipBatches = searcher.scroll(Query.select(String[].class)
			.from(SnomedRelationshipIndexEntry.class)
			.fields(SnomedRelationshipIndexEntry.Fields.ID, SnomedRelationshipIndexEntry.Fields.MODULE_ID)
			.where(invalidRelationshipExpression)
			.limit(10_000)
			.build())

	invalidRelationshipBatches.each({ relationshipBatch ->
		relationshipBatch.each({ relationship ->
			issueDetails.add(
					new IssueDetail(
						ComponentIdentifier.of(SnomedTerminologyComponentConstants.RELATIONSHIP_NUMBER, relationship[0]),
						ImmutableMap.of(
							SnomedRf2Headers.FIELD_ACTIVE, true,
							SnomedRf2Headers.FIELD_MODULE_ID, relationship[1])))
		})
	})
})

return issueDetails
