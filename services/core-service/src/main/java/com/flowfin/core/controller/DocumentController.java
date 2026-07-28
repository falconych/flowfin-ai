package com.flowfin.core.controller;

import com.flowfin.core.dto.DocumentRequest;
import com.flowfin.core.service.DocumentProducer;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentProducer documentProducer;

    @PostMapping("/ingest")
    public ResponseEntity<Map<String, String>> ingestDocument(@Valid @RequestBody DocumentRequest request) {
        String documentId = documentProducer.sendRawDocument(request);
        return ResponseEntity.ok(Map.of(
                "status", "ACCEPTED",
                "documentId", documentId,
                "message", "Document successfully queued for AI processing"
        ));
    }
}