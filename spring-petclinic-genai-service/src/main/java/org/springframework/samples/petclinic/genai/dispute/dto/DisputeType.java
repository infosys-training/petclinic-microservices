package org.springframework.samples.petclinic.genai.dispute.dto;

public enum DisputeType {

	FRAUD("Fraud"),
	DUPLICATE_CHARGE("Duplicate Charge"),
	SERVICE_ISSUE("Service Issue"),
	UNAUTHORIZED_TRANSACTION("Unauthorized Transaction"),
	PRODUCT_NOT_RECEIVED("Product Not Received"),
	OTHER("Other");

	private final String displayName;

	DisputeType(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}

}
