package org.springframework.samples.petclinic.genai.dispute.agent;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.genai.dispute.dto.AgentResult;
import org.springframework.samples.petclinic.genai.dispute.dto.Dispute;
import org.springframework.samples.petclinic.genai.dispute.dto.DisputeStatus;
import org.springframework.samples.petclinic.genai.dispute.dto.DisputeType;
import org.springframework.stereotype.Component;

/**
 * Decision Agent - combines inputs from all previous agents to make a final decision:
 * Approve (issue refund), Reject, or Escalate to human agent.
 */
@Component
public class DecisionAgent {

	private static final Logger LOG = LoggerFactory.getLogger(DecisionAgent.class);

	public AgentResult process(Dispute dispute) {
		long start = System.currentTimeMillis();
		LOG.info("DecisionAgent processing dispute {}", dispute.getDisputeId());

		String decision;
		String reason;
		DisputeStatus finalStatus;

		double riskScore = dispute.getRiskScore();
		DisputeType type = dispute.getDisputeType();
		double amount = dispute.getSubmission().amount();

		if (riskScore >= 75 && amount > 1000) {
			decision = "ESCALATE";
			reason = "High risk score (" + String.format("%.0f", riskScore)
					+ ") combined with high transaction amount ($" + String.format("%.2f", amount)
					+ "). Requires human review.";
			finalStatus = DisputeStatus.ESCALATED;
		}
		else if (type == DisputeType.DUPLICATE_CHARGE) {
			decision = "APPROVE";
			reason = "Duplicate charge confirmed through transaction log analysis. Automatic refund eligible.";
			finalStatus = DisputeStatus.APPROVED;
		}
		else if (type == DisputeType.PRODUCT_NOT_RECEIVED && riskScore < 50) {
			decision = "APPROVE";
			reason = "Product not received claim validated. Low risk score (" + String.format("%.0f", riskScore)
					+ "). Refund approved.";
			finalStatus = DisputeStatus.APPROVED;
		}
		else if (riskScore >= 60) {
			decision = "ESCALATE";
			reason = "Medium-high risk score (" + String.format("%.0f", riskScore)
					+ ") with " + type.getDisplayName() + " classification. Manual review recommended.";
			finalStatus = DisputeStatus.ESCALATED;
		}
		else if (type == DisputeType.FRAUD || type == DisputeType.UNAUTHORIZED_TRANSACTION) {
			if (amount <= 500 && riskScore < 50) {
				decision = "APPROVE";
				reason = "Low-value " + type.getDisplayName()
						+ " claim with acceptable risk. Provisional credit issued.";
				finalStatus = DisputeStatus.APPROVED;
			}
			else {
				decision = "ESCALATE";
				reason = type.getDisplayName() + " with amount $" + String.format("%.2f", amount)
						+ " requires fraud investigation team review.";
				finalStatus = DisputeStatus.ESCALATED;
			}
		}
		else if (riskScore < 40 && amount <= 200) {
			decision = "APPROVE";
			reason = "Low risk, low amount dispute. Auto-approved per policy.";
			finalStatus = DisputeStatus.APPROVED;
		}
		else {
			decision = "ESCALATE";
			reason = "Dispute requires additional review. Type: " + type.getDisplayName() + ", Risk: "
					+ String.format("%.0f", riskScore);
			finalStatus = DisputeStatus.ESCALATED;
		}

		dispute.setDecision(decision);
		dispute.setDecisionReason(reason);
		dispute.setStatus(finalStatus);

		Map<String, Object> details = new HashMap<>();
		details.put("decision", decision);
		details.put("reason", reason);
		details.put("inputRiskScore", riskScore);
		details.put("inputDisputeType", type.getDisplayName());
		details.put("inputAmount", amount);
		details.put("finalStatus", finalStatus.getDisplayName());

		String summary = "Decision: " + decision + " - " + reason;

		long elapsed = System.currentTimeMillis() - start;
		return new AgentResult("Decision Agent", "SUCCESS", summary, details, elapsed);
	}

}
