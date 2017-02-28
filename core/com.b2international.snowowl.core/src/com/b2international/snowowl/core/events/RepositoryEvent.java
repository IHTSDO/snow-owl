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
package com.b2international.snowowl.core.events;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @since 4.5
 */
public abstract class RepositoryEvent extends BaseEvent {

	private static final long serialVersionUID = 1L;
	
	/**
	 * Notification address where subclasses of this {@link RepositoryEvent} will be sent. Use the unique repository identifier to complete this
	 * template.
	 */
	public static final String ADDRESS_TEMPLATE = "/%s/notifications";
	private final String repositoryId;

	protected RepositoryEvent(final String repositoryId) {
		this.repositoryId = checkNotNull(repositoryId, "repositoryId");
	}

	public final String getRepositoryId() {
		return repositoryId;
	}

	@Override
	protected final String getAddress() {
		return String.format(ADDRESS_TEMPLATE, repositoryId);
	}

}
