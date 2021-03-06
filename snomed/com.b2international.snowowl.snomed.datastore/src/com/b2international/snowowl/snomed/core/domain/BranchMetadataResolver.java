package com.b2international.snowowl.snomed.core.domain;

import com.b2international.snowowl.core.branch.Branch;

public class BranchMetadataResolver {

	public static String getEffectiveBranchMetadataValue(Branch branch, String metadataKey) {
		final String metadataValue = branch.metadata().getString(metadataKey);
		if (metadataValue != null) {
			return metadataValue;
		} else {
			final Branch parent = branch.parent();
			if (parent != null && branch != parent) {
				return getEffectiveBranchMetadataValue(parent, metadataKey);
			}
		}
		return null;
	}

}
