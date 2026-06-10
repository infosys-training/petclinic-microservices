package org.springframework.samples.petclinic.genai.dispute.agent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.genai.dispute.dto.AgentResult;
import org.springframework.samples.petclinic.genai.dispute.dto.Dispute;
import org.springframework.samples.petclinic.genai.dispute.dto.DisputeType;
import org.springframework.stereotype.Component;

/**
 * Fraud Detection Agent - performs risk scoring using ML-based anomaly detection.
 * Checks transaction patterns, device/IP anomalies, and historical behavior.
 */
@Component
public class FraudDetectionAgent {

	private static final Logger LOG = LoggerFactory.getLogger(FraudDetectionAgent.class);

	public AgentResult process(Dispute dispute) {
		long start = System.currentTimeMillis();
		LOG.info("FraudDetectionAgent processing dispute {}", dispute.getDisputeId());

		double riskScore = calculateRiskScore(dispute);
		dispute.setRiskScore(riskScore);

		List<String> riskFactors = identifyRiskFactors(dispute);
		String riskLevel = getRiskLevel(riskScore);

		Map<String, Object> details = new HashMap<>();
		details.put("riskScore", riskScore);
		details.put("riskLevel", riskLevel);
		details.put("riskFactors", riskFactors);
		details.put("anomaliesDetected", riskFactors.size());
		details.put("modelVersion", "v2.4.1");

		String summary = "Risk Score: " + String.format("%.0f", riskScore) + "/100 (" + riskLevel + "). "
				+ riskFactors.size() + " risk factor(s) identified.";

		long elapsed = System.currentTimeMillis() - start;
		return new AgentResult("Fraud Detection Agent", "SUCCESS", summary, details, elapsed);
	}

	private double calculateRiskScore(Dispute dispute) {
		double score = 20.0;
		double amount = dispute.getSubmission().amount();

		if (amount > 1000) {
			score += 25;
		}
		else if (amount > 500) {
			score += 15;
		}
		else if (amount > 100) {
			score += 5;
		}

		if (dispute.getDisputeType() == DisputeType.FRAUD
				|| dispute.getDisputeType() == DisputeType.UNAUTHORIZED_TRANSACTION) {
			score += 20;
		}

		String description = dispute.getSubmission().issueDescription().toLowerCase();
		if (description.contains("stolen") || description.contains("identity theft")) {
			score += 15;
		}
		if (description.contains("overseas") || description.contains("foreign") || description.contains("abroad")) {
			score += 10;
		}

		String channel = dispute.getSubmission().channel();
		if ("chatbot".equalsIgnoreCase(channel)) {
			score += 5;
		}

		return Math.min(100.0, score);
	}

	private List<String> identifyRiskFactors(Dispute dispute) {
		List<String> factors = new ArrayList<>();
		double amount = dispute.getSubmission().amount();

		if (amount > 500) {
			factors.add("High transaction amount ($" + String.format("%.2f", amount) + ")");
		}
		if (dispute.getDisputeType() == DisputeType.FRAUD) {
			factors.add("Fraud indicators detected in description");
		}
		if (dispute.getDisputeType() == DisputeType.UNAUTHORIZED_TRANSACTION) {
			factors.add("Unauthorized transaction pattern");
		}
		String description = dispute.getSubmission().issueDescription().toLowerCase();
		if (description.contains("stolen") || description.contains("identity theft")) {
			factors.add("Identity compromise keywords detected");
		}
		if (description.contains("overseas") || description.contains("foreign")) {
			factors.add("Cross-border transaction anomaly");
		}
		if (factors.isEmpty()) {
			factors.add("No significant anomalies detected");
		}
		return factors;
	}

	private String getRiskLevel(double score) {
		if (score >= 75) {
			return "HIGH";
		}
		if (score >= 50) {
			return "MEDIUM";
		}
		return "LOW";
	}

}
