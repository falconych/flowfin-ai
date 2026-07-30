package com.flowfin.core.controller;

import com.flowfin.core.dto.DocumentRequest;
import com.flowfin.core.dto.DocumentResponse;
import com.flowfin.core.service.DocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/ingest")
    public ResponseEntity<DocumentResponse> ingestDocument(@Valid @RequestBody DocumentRequest request) {
        // Delegate full ingestion lifecycle (DB persistence + Kafka event publishing) to the service
        DocumentResponse response = documentService.processIngestion(request);
        return ResponseEntity.ok(response);
    }
}