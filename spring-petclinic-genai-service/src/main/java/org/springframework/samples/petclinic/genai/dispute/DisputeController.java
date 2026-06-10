package org.springframework.samples.petclinic.genai.dispute;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.genai.dispute.dto.Dispute;
import org.springframework.samples.petclinic.genai.dispute.dto.DisputeSubmission;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for the Dispute Resolution AI Agent system. Provides endpoints to
 * submit disputes, list all disputes, and view individual dispute details including
 * the full AI agent processing pipeline results.
 */
@RestController
@RequestMapping("/disputes")
public class DisputeController {

	private static final Logger LOG = LoggerFactory.getLogger(DisputeController.class);

	private final DisputeOrchestrator orchestrator;

	private final DisputeRepository repository;

	public DisputeController(DisputeOrchestrator orchestrator, DisputeRepository repository) {
		this.orchestrator = orchestrator;
		this.repository = repository;
	}

	@PostMapping
	public ResponseEntity<Dispute> submitDispute(@RequestBody DisputeSubmission submission) {
		LOG.info("Received dispute submission from {} for transaction {}", submission.customerName(),
				submission.transactionId());
		Dispute dispute = orchestrator.processDispute(submission);
		return ResponseEntity.ok(dispute);
	}

	@GetMapping
	public ResponseEntity<List<Dispute>> listDisputes() {
		return ResponseEntity.ok(repository.findAll());
	}

	@GetMapping("/{disputeId}")
	public ResponseEntity<Dispute> getDispute(@PathVariable String disputeId) {
		return repository.findById(disputeId)
			.map(ResponseEntity::ok)
			.orElse(ResponseEntity.notFound().build());
	}

}
