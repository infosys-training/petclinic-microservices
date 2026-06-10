package org.springframework.samples.petclinic.genai.dispute;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.samples.petclinic.genai.dispute.dto.Dispute;
import org.springframework.stereotype.Repository;

/**
 * In-memory repository for dispute management. Stores disputes in a thread-safe map.
 */
@Repository
public class DisputeRepository {

	private final Map<String, Dispute> disputes = new ConcurrentHashMap<>();

	private final AtomicInteger counter = new AtomicInteger(1000);

	public String generateId() {
		return "DSP-" + counter.incrementAndGet();
	}

	public Dispute save(Dispute dispute) {
		disputes.put(dispute.getDisputeId(), dispute);
		return dispute;
	}

	public Optional<Dispute> findById(String disputeId) {
		return Optional.ofNullable(disputes.get(disputeId));
	}

	public List<Dispute> findAll() {
		return new ArrayList<>(disputes.values());
	}

}
