package org.springframework.samples.petclinic.genai.dispute.agent;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.genai.dispute.dto.AgentResult;
import org.springframework.samples.petclinic.genai.dispute.dto.Dispute;
import org.springframework.stereotype.Component;

/**
 * Communication Agent - generates customer-facing messages (email, chatbot responses)
 * and merchant evidence requests based on the dispute decision.
 */
@Component
public class CommunicationAgent {

	private static final Logger LOG = LoggerFactory.getLogger(CommunicationAgent.class);

	public AgentResult process(Dispute dispute) {
		long start = System.currentTimeMillis();
		LOG.info("CommunicationAgent processing dispute {}", dispute.getDisputeId());

		String customerName = dispute.getSubmission().customerName();
		String decision = dispute.getDecision();
		String communication = generateCommunication(dispute, customerName, decision);

		dispute.setCommunicationSent(communication);

		Map<String, Object> details = new HashMap<>();
		details.put("recipientEmail", dispute.getSubmission().customerEmail());
		details.put("channel", dispute.getSubmission().channel());
		details.put("messageType", getMessageType(decision));
		details.put("communicationText", communication);

		String summary = "Communication sent to " + customerName + " via " + dispute.getSubmission().channel()
				+ ". Decision notification: " + decision;

		long elapsed = System.currentTimeMillis() - start;
		return new AgentResult("Communication Agent", "SUCCESS", summary, details, elapsed);
	}

	private String generateCommunication(Dispute dispute, String customerName, String decision) {
		String transactionId = dispute.getSubmission().transactionId();
		double amount = dispute.getSubmission().amount();

		return switch (decision) {
			case "APPROVE" -> "Dear " + customerName + ",\n\n"
					+ "Your dispute (ID: " + dispute.getDisputeId() + ") regarding transaction "
					+ transactionId + " for $" + String.format("%.2f", amount) + " has been APPROVED.\n\n"
					+ "A refund of $" + String.format("%.2f", amount) + " will be credited to your account "
					+ "ending in " + dispute.getSubmission().cardLast4() + " within 3-5 business days.\n\n"
					+ "Reason: " + dispute.getDecisionReason() + "\n\n"
					+ "If you have any questions, please contact our support team.\n\n"
					+ "Best regards,\nDispute Resolution Team";
			case "REJECT" -> "Dear " + customerName + ",\n\n"
					+ "After careful review, your dispute (ID: " + dispute.getDisputeId() + ") regarding transaction "
					+ transactionId + " for $" + String.format("%.2f", amount) + " has been DECLINED.\n\n"
					+ "Reason: " + dispute.getDecisionReason() + "\n\n"
					+ "You may appeal this decision within 30 days by contacting our disputes department.\n\n"
					+ "Best regards,\nDispute Resolution Team";
			case "ESCALATE" -> "Dear " + customerName + ",\n\n"
					+ "Your dispute (ID: " + dispute.getDisputeId() + ") regarding transaction "
					+ transactionId + " for $" + String.format("%.2f", amount) + " is being reviewed "
					+ "by our specialized team.\n\n"
					+ "A dedicated agent will contact you within 24-48 hours with an update.\n\n"
					+ "Reference: " + dispute.getDisputeId() + "\n\n"
					+ "Best regards,\nDispute Resolution Team";
			default -> "Dear " + customerName + ",\n\n"
					+ "We have received your dispute and are processing it. "
					+ "You will receive an update shortly.\n\n"
					+ "Best regards,\nDispute Resolution Team";
		};
	}

	private String getMessageType(String decision) {
		return switch (decision) {
			case "APPROVE" -> "Refund Approval Notification";
			case "REJECT" -> "Dispute Rejection Notification";
			case "ESCALATE" -> "Escalation Acknowledgment";
			default -> "Status Update";
		};
	}

}
