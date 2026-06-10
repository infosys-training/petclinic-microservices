package org.springframework.samples.petclinic.genai.dispute.agent;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.genai.dispute.dto.AgentResult;
import org.springframework.samples.petclinic.genai.dispute.dto.Dispute;
import org.springframework.samples.petclinic.genai.dispute.dto.DisputeType;
import org.springframework.stereotype.Component;

/**
 * Classifier Agent - categorizes disputes into types: Fraud, Duplicate Charge,
 * Service Issue, Unauthorized Transaction, Product Not Received, or Other.
 */
@Component
public class ClassifierAgent {

	private static final Logger LOG = LoggerFactory.getLogger(ClassifierAgent.class);

	public AgentResult process(Dispute dispute) {
		long start = System.currentTimeMillis();
		LOG.info("ClassifierAgent processing dispute {}", dispute.getDisputeId());

		String description = dispute.getSubmission().issueDescription().toLowerCase();
		DisputeType classifiedType = classify(description);
		double confidence = calculateConfidence(description, classifiedType);

		dispute.setDisputeType(classifiedType);

		Map<String, Object> details = new HashMap<>();
		details.put("classifiedType", classifiedType.name());
		details.put("displayName", classifiedType.getDisplayName());
		details.put("confidence", String.format("%.1f%%", confidence * 100));
		details.put("analysisMethod", "NLP Keyword + Pattern Analysis");

		String summary = "Classified as: " + classifiedType.getDisplayName() + " (Confidence: "
				+ String.format("%.0f%%", confidence * 100) + ")";

		long elapsed = System.currentTimeMillis() - start;
		return new AgentResult("Classifier Agent", "SUCCESS", summary, details, elapsed);
	}

	private DisputeType classify(String description) {
		if (containsAny(description, "fraud", "scam", "stolen card", "identity theft", "phishing")) {
			return DisputeType.FRAUD;
		}
		if (containsAny(description, "duplicate", "charged twice", "double charge", "billed twice")) {
			return DisputeType.DUPLICATE_CHARGE;
		}
		if (containsAny(description, "unauthorized", "didn't authorize", "not me", "card stolen", "stolen")) {
			return DisputeType.UNAUTHORIZED_TRANSACTION;
		}
		if (containsAny(description, "not received", "never received", "not delivered", "missing order",
				"never arrived")) {
			return DisputeType.PRODUCT_NOT_RECEIVED;
		}
		if (containsAny(description, "poor service", "bad quality", "damaged", "defective", "wrong item", "broken")) {
			return DisputeType.SERVICE_ISSUE;
		}
		return DisputeType.OTHER;
	}

	private double calculateConfidence(String description, DisputeType type) {
		int matchCount = 0;
		String[] keywords = getKeywordsForType(type);
		for (String keyword : keywords) {
			if (description.contains(keyword)) {
				matchCount++;
			}
		}
		return Math.min(0.95, 0.60 + (matchCount * 0.10));
	}

	private String[] getKeywordsForType(DisputeType type) {
		return switch (type) {
			case FRAUD -> new String[] { "fraud", "scam", "stolen", "identity theft", "phishing", "suspicious" };
			case DUPLICATE_CHARGE -> new String[] { "duplicate", "twice", "double", "repeated", "again" };
			case UNAUTHORIZED_TRANSACTION ->
				new String[] { "unauthorized", "not me", "stolen", "didn't make", "unknown" };
			case PRODUCT_NOT_RECEIVED ->
				new String[] { "not received", "never received", "missing", "not delivered", "lost" };
			case SERVICE_ISSUE -> new String[] { "service", "quality", "damaged", "defective", "wrong", "broken" };
			case OTHER -> new String[] {};
		};
	}

	private boolean containsAny(String text, String... keywords) {
		for (String keyword : keywords) {
			if (text.contains(keyword)) {
				return true;
			}
		}
		return false;
	}

}
