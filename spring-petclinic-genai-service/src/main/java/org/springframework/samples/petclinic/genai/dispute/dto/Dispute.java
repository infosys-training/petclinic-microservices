package org.springframework.samples.petclinic.genai.dispute.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Dispute {

	private String disputeId;

	private DisputeSubmission submission;

	private DisputeStatus status;

	private DisputeType disputeType;

	private double riskScore;

	private String decision;

	private String decisionReason;

	private List<AgentResult> agentResults = new ArrayList<>();

	private String communicationSent;

	private LocalDateTime createdAt;

	private LocalDateTime updatedAt;

	public Dispute() {
	}

	public Dispute(String disputeId, DisputeSubmission submission) {
		this.disputeId = disputeId;
		this.submission = submission;
		this.status = DisputeStatus.SUBMITTED;
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
	}

	public String getDisputeId() {
		return disputeId;
	}

	public void setDisputeId(String disputeId) {
		this.disputeId = disputeId;
	}

	public DisputeSubmission getSubmission() {
		return submission;
	}

	public void setSubmission(DisputeSubmission submission) {
		this.submission = submission;
	}

	public DisputeStatus getStatus() {
		return status;
	}

	public void setStatus(DisputeStatus status) {
		this.status = status;
		this.updatedAt = LocalDateTime.now();
	}

	public DisputeType getDisputeType() {
		return disputeType;
	}

	public void setDisputeType(DisputeType disputeType) {
		this.disputeType = disputeType;
	}

	public double getRiskScore() {
		return riskScore;
	}

	public void setRiskScore(double riskScore) {
		this.riskScore = riskScore;
	}

	public String getDecision() {
		return decision;
	}

	public void setDecision(String decision) {
		this.decision = decision;
	}

	public String getDecisionReason() {
		return decisionReason;
	}

	public void setDecisionReason(String decisionReason) {
		this.decisionReason = decisionReason;
	}

	public List<AgentResult> getAgentResults() {
		return agentResults;
	}

	public void setAgentResults(List<AgentResult> agentResults) {
		this.agentResults = agentResults;
	}

	public void addAgentResult(AgentResult result) {
		this.agentResults.add(result);
	}

	public String getCommunicationSent() {
		return communicationSent;
	}

	public void setCommunicationSent(String communicationSent) {
		this.communicationSent = communicationSent;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

}
