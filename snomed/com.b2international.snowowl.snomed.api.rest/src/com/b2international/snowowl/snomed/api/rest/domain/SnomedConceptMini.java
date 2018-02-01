package com.b2international.snowowl.snomed.api.rest.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

public class SnomedConceptMini {
	@JsonUnwrapped
	@JsonProperty("id")
	private String id;
	@JsonUnwrapped()
	@JsonProperty("fsn")
	private String fsn;
	
	public SnomedConceptMini(String id) {
		this(id, id);
	}
	public SnomedConceptMini(String id, String fsn) {
		this.id = id;
		this.fsn = fsn;
	}

	public void setFsn(String fsn) {
		this.fsn = fsn;
	}
	
	public String getId() {
		return id;
	}
	
	public String getFsn() {
		return fsn;
	}
	
}
