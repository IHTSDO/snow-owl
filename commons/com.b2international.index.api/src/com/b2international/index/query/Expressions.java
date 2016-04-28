/*
 * Copyright 2011-2016 B2i Healthcare Pte Ltd, http://b2i.sg
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

import java.util.List;

import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

/**
 * @since 4.7
 */
public class Expressions {
	
	public interface PredicateBuilder {
		BinaryOperatorBuilder exactMatch(String field, String value);
		BinaryOperatorBuilder not(BinaryOperatorBuilder expressionBuilder);
	}

	public interface BinaryOperatorBuilder extends Buildable<Expression> {
		BinaryOperatorBuilder and(BinaryOperatorBuilder expressionBuilder);
		BinaryOperatorBuilder or(BinaryOperatorBuilder expressionBuilder);
	}
	
	public interface Builder extends BinaryOperatorBuilder, PredicateBuilder {}
	
	private static final class BuilderImpl implements Builder {

		private Optional<Expression> previous = Optional.absent();
		
		@Override
		public BinaryOperatorBuilder exactMatch(String field, String value) {
			previous = Optional.<Expression>of(Expressions.exactMatch(field, value));
			return this;
		}

		@Override
		public BinaryOperatorBuilder not(BinaryOperatorBuilder expressionBuilder) {
			previous = Optional.<Expression>of(Expressions.not(expressionBuilder.build()));
			return this;
		}

		@Override
		public BinaryOperatorBuilder and(BinaryOperatorBuilder expressionBuilder) {
			Expression previousExpression = previous.get();
			And or = new And(previousExpression, expressionBuilder.build());
			previous = Optional.<Expression>of(or);
			return this;
		}

		@Override
		public BinaryOperatorBuilder or(BinaryOperatorBuilder expressionBuilder) {
			Expression previousExpression = previous.get();
			Or or = new Or(previousExpression, expressionBuilder.build());
			previous = Optional.<Expression>of(or);
			return this;
		}

		@Override
		public Expression build() {
			return previous.get();
		}
		
	}
	
	public static Expression nestedMatch(final String field, Expression expression) {
		final List<String> pathSegments = Lists.reverse(Splitter.on(".").splitToList(field));
		Expression previous = expression;
		for (String segment : pathSegments) {
			previous = new NestedPredicate(segment, previous);
		}
		return previous;
	}
	
	public static Expression prefixMatch(final String field, final String prefix) {
		return new PrefixPredicate(field, prefix);
	}
	
	public static Expression not(Expression inner) {
		return new Not(inner);
	}

	public static Expression exactMatch(String field, String value) {
		return new StringPredicate(field, value);
	}
	
	public static Expression exactMatch(String field, Long value) {
		return new LongPredicate(field, value);
	}

	public static PredicateBuilder builder() {
		return new BuilderImpl();
	}
}