package org.springframework.samples.petclinic.genai.dispute;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.genai.dispute.agent.ClassifierAgent;
import org.springframework.samples.petclinic.genai.dispute.agent.CommunicationAgent;
import org.springframework.samples.petclinic.genai.dispute.agent.DecisionAgent;
import org.springframework.samples.petclinic.genai.dispute.agent.EvidenceCollectorAgent;
import org.springframework.samples.petclinic.genai.dispute.agent.FraudDetectionAgent;
import org.springframework.samples.petclinic.genai.dispute.agent.IntakeAgent;
import org.springframework.samples.petclinic.genai.dispute.dto.AgentResult;
import org.springframework.samples.petclinic.genai.dispute.dto.Dispute;
import org.springframework.samples.petclinic.genai.dispute.dto.DisputeStatus;
import org.springframework.samples.petclinic.genai.dispute.dto.DisputeSubmission;
import org.springframework.stereotype.Service;

/**
 * Dispute Orchestrator - the core brain that manages the dispute resolution workflow.
 * Coordinates different AI agents in sequence, updating dispute status at each stage.
 */
@Service
public class DisputeOrchestrator {

	private static final Logger LOG = LoggerFactory.getLogger(DisputeOrchestrator.class);

	private final DisputeRepository repository;

	private final IntakeAgent intakeAgent;

	private final ClassifierAgent classifierAgent;

	private final FraudDetectionAgent fraudDetectionAgent;

	private final EvidenceCollectorAgent evidenceCollectorAgent;

	private final DecisionAgent decisionAgent;

	private final CommunicationAgent communicationAgent;

	public DisputeOrchestrator(DisputeRepository repository, IntakeAgent intakeAgent,
			ClassifierAgent classifierAgent, FraudDetectionAgent fraudDetectionAgent,
			EvidenceCollectorAgent evidenceCollectorAgent, DecisionAgent decisionAgent,
			CommunicationAgent communicationAgent) {
		this.repository = repository;
		this.intakeAgent = intakeAgent;
		this.classifierAgent = classifierAgent;
		this.fraudDetectionAgent = fraudDetectionAgent;
		this.evidenceCollectorAgent = evidenceCollectorAgent;
		this.decisionAgent = decisionAgent;
		this.communicationAgent = communicationAgent;
	}

	public Dispute processDispute(DisputeSubmission submission) {
		String disputeId = repository.generateId();
		Dispute dispute = new Dispute(disputeId, submission);
		repository.save(dispute);

		LOG.info("Starting dispute processing pipeline for {}", disputeId);

		// Stage 1: AI Intake
		dispute.setStatus(DisputeStatus.INTAKE_PROCESSING);
		AgentResult intakeResult = intakeAgent.process(dispute);
		dispute.addAgentResult(intakeResult);
		repository.save(dispute);

		// Stage 2: Classification
		dispute.setStatus(DisputeStatus.CLASSIFYING);
		AgentResult classifierResult = classifierAgent.process(dispute);
		dispute.addAgentResult(classifierResult);
		repository.save(dispute);

		// Stage 3: Fraud Detection
		dispute.setStatus(DisputeStatus.FRAUD_ANALYSIS);
		AgentResult fraudResult = fraudDetectionAgent.process(dispute);
		dispute.addAgentResult(fraudResult);
		repository.save(dispute);

		// Stage 4: Evidence Collection
		dispute.setStatus(DisputeStatus.EVIDENCE_COLLECTION);
		AgentResult evidenceResult = evidenceCollectorAgent.process(dispute);
		dispute.addAgentResult(evidenceResult);
		repository.save(dispute);

		// Stage 5: Decision
		dispute.setStatus(DisputeStatus.DECISION_PENDING);
		AgentResult decisionResult = decisionAgent.process(dispute);
		dispute.addAgentResult(decisionResult);
		repository.save(dispute);

		// Stage 6: Communication
		AgentResult communicationResult = communicationAgent.process(dispute);
		dispute.addAgentResult(communicationResult);
		repository.save(dispute);

		LOG.info("Dispute {} processing complete. Decision: {}", disputeId, dispute.getDecision());
		return dispute;
	}

}
