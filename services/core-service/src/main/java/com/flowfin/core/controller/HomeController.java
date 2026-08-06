package com.flowfin.core.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> getIndex() {
        Map<String, Object> response = Map.of(
            "service", "flowFin-ai Core Service",
            "description", "Financial AI document processing pipeline for automated ingestion, vectorization, and analysis.",
            "status", "UP",
            "timestamp", Instant.now().toString(),
            "endpoints", Map.of(
                "ingestDocument", "POST /api/v1/documents/ingest"
            )
        );

        return ResponseEntity.ok(response);
    }
}