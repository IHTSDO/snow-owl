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
package com.b2international.snowowl.snomed.core.domain;

import java.util.Date;

import com.b2international.snowowl.core.domain.BaseComponent;

/**
 * @since 4.0
 */
public abstract class BaseSnomedComponent extends BaseComponent implements SnomedComponent {

	private Boolean active;
	private Date effectiveTime;
	private String moduleId;
	private String iconId;
	private Float score;

	@Override
	public Boolean isActive() {
		return active;
	}

	@Override
	public Date getEffectiveTime() {
		return effectiveTime;
	}

	@Override
	public String getModuleId() {
		return moduleId;
	}
	
	@Override
	public String getIconId() {
		return iconId;
	}
	
	@Override
	public Float getScore() {
		return score;
	}

	public void setActive(final Boolean active) {
		this.active = active;
	}

	public void setEffectiveTime(final Date effectiveTime) {
		this.effectiveTime = effectiveTime;
	}

	public void setModuleId(final String moduleId) {
		this.moduleId = moduleId;
	}
	
	public void setIconId(String iconId) {
		this.iconId = iconId;
	}
	
	public void setScore(Float score) {
		this.score = score;
	}
	
}