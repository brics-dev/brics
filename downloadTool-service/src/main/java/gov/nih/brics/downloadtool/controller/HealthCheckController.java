package gov.nih.brics.downloadtool.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * A simple health check.
 * 
 * @author Ryan Powell
 */
@RestController
public class HealthCheckController {
	@GetMapping("/health")
	public ResponseEntity<Void> healthCheck() {
		return ResponseEntity.ok().build();
	}
}
