package org.springframework.samples.petclinic.genai.dispute.agent;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.genai.dispute.dto.AgentResult;
import org.springframework.samples.petclinic.genai.dispute.dto.Dispute;
import org.springframework.stereotype.Component;

/**
 * AI Intake Agent - extracts and validates transaction data from the dispute submission
 * using NLP analysis. Simulates LLM-based extraction when no API key is available.
 */
@Component
public class IntakeAgent {

	private static final Logger LOG = LoggerFactory.getLogger(IntakeAgent.class);

	public AgentResult process(Dispute dispute) {
		long start = System.currentTimeMillis();
		LOG.info("IntakeAgent processing dispute {}", dispute.getDisputeId());

		Map<String, Object> details = new HashMap<>();
		var submission = dispute.getSubmission();

		details.put("transactionId", submission.transactionId());
		details.put("amount", submission.amount());
		details.put("merchantName", submission.merchantName());
		details.put("customerName", submission.customerName());
		details.put("channel", submission.channel());
		details.put("transactionDate", submission.transactionDate());

		boolean hasCompleteData = submission.transactionId() != null && !submission.transactionId().isBlank()
				&& submission.amount() > 0 && submission.issueDescription() != null
				&& !submission.issueDescription().isBlank();

		details.put("dataComplete", hasCompleteData);
		details.put("extractedKeywords", extractKeywords(submission.issueDescription()));

		String summary = hasCompleteData
				? "Successfully extracted transaction data. Transaction ID: " + submission.transactionId()
						+ ", Amount: $" + String.format("%.2f", submission.amount()) + ", Merchant: "
						+ submission.merchantName()
				: "Incomplete data detected. Some fields may require customer follow-up.";

		long elapsed = System.currentTimeMillis() - start;
		return new AgentResult("AI Intake Agent", hasCompleteData ? "SUCCESS" : "WARNING", summary, details, elapsed);
	}

	private String extractKeywords(String description) {
		if (description == null || description.isBlank()) {
			return "";
		}
		String lower = description.toLowerCase();
		StringBuilder keywords = new StringBuilder();
		String[] keywordList = { "fraud", "unauthorized", "stolen", "duplicate", "charged twice", "wrong amount",
				"never received", "not delivered", "damaged", "refund", "scam", "suspicious", "card stolen",
				"identity theft" };
		for (String keyword : keywordList) {
			if (lower.contains(keyword)) {
				if (keywords.length() > 0) {
					keywords.append(", ");
				}
				keywords.append(keyword);
			}
		}
		return keywords.toString();
	}

}
