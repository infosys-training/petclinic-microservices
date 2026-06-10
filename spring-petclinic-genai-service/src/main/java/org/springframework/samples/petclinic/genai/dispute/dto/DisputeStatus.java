package org.springframework.samples.petclinic.genai.dispute.dto;

public enum DisputeStatus {

	SUBMITTED("Submitted"),
	INTAKE_PROCESSING("AI Intake Processing"),
	CLASSIFYING("Classifying Dispute"),
	FRAUD_ANALYSIS("Fraud Analysis"),
	EVIDENCE_COLLECTION("Collecting Evidence"),
	DECISION_PENDING("Decision Pending"),
	APPROVED("Approved - Refund Initiated"),
	REJECTED("Rejected"),
	ESCALATED("Escalated to Human Agent");

	private final String displayName;

	DisputeStatus(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}

}
