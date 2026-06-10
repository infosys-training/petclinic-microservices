package org.springframework.samples.petclinic.genai.dispute.agent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.genai.dispute.dto.AgentResult;
import org.springframework.samples.petclinic.genai.dispute.dto.Dispute;
import org.springframework.stereotype.Component;

/**
 * Evidence Collector Agent - gathers transaction logs, merchant info, device/IP data
 * from core banking and payment systems.
 */
@Component
public class EvidenceCollectorAgent {

	private static final Logger LOG = LoggerFactory.getLogger(EvidenceCollectorAgent.class);

	public AgentResult process(Dispute dispute) {
		long start = System.currentTimeMillis();
		LOG.info("EvidenceCollectorAgent processing dispute {}", dispute.getDisputeId());

		var submission = dispute.getSubmission();
		List<Map<String, String>> evidenceItems = new ArrayList<>();

		evidenceItems.add(Map.of("type", "Transaction Log", "source", "Core Banking System", "status", "Retrieved",
				"detail", "Transaction " + submission.transactionId() + " for $"
						+ String.format("%.2f", submission.amount()) + " at " + submission.merchantName()));

		evidenceItems.add(Map.of("type", "Merchant Information", "source", "Merchant Database", "status", "Retrieved",
				"detail", "Merchant: " + submission.merchantName() + " - Active merchant with valid MID"));

		evidenceItems.add(Map.of("type", "Device/IP Data", "source", "Security Platform", "status", "Retrieved",
				"detail", "Channel: " + submission.channel() + " - Device fingerprint collected"));

		evidenceItems.add(Map.of("type", "Transaction History", "source", "Core Banking System", "status", "Retrieved",
				"detail", "Customer transaction history for last 90 days analyzed"));

		evidenceItems
			.add(Map.of("type", "Card Network Data", "source", "Visa/Mastercard", "status", "Pending",
					"detail", "Chargeback eligibility check initiated for card ending " + submission.cardLast4()));

		Map<String, Object> details = new HashMap<>();
		details.put("evidenceItems", evidenceItems);
		details.put("totalEvidence", evidenceItems.size());
		details.put("pendingItems", 1);
		details.put("completedItems", 4);

		String summary = "Collected " + evidenceItems.size() + " evidence items from banking systems. "
				+ "4 retrieved, 1 pending from card network.";

		long elapsed = System.currentTimeMillis() - start;
		return new AgentResult("Evidence Collector Agent", "SUCCESS", summary, details, elapsed);
	}

}
