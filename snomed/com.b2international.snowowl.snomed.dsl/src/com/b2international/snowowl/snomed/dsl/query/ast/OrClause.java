/*
 * Copyright 2011-2015 B2i Healthcare Pte Ltd, http://b2i.sg
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
package com.b2international.snowowl.snomed.dsl.query.ast;

public class OrClause extends BinaryRValue<RValue, RValue> {

	public OrClause() {
		super();
	}

	public OrClause(RValue lValue, RValue rValue) {
		super(lValue, rValue);
	}

	@Override
	protected void appendLeftRightSeparatorForToString(StringBuilder buf) {
		buf.append(" OR ");
	}
}