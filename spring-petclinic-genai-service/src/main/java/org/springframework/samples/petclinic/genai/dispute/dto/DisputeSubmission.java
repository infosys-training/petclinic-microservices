package org.springframework.samples.petclinic.genai.dispute.dto;

public record DisputeSubmission(
	String customerName,
	String customerEmail,
	String transactionId,
	double amount,
	String merchantName,
	String transactionDate,
	String cardLast4,
	String channel,
	String issueDescription
) {
}
