package org.springframework.samples.petclinic.genai.dispute.dto;

import java.util.Map;

public record AgentResult(
	String agentName,
	String status,
	String summary,
	Map<String, Object> details,
	long processingTimeMs
) {
}
