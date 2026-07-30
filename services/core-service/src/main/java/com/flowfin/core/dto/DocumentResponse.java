package com.flowfin.core.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;
import java.util.UUID;

public record DocumentResponse(
        UUID documentId,
        String title,
        String status,
        String message,
        
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "UTC")
        Instant createdAt
) {
    public DocumentResponse(UUID documentId, String title, String status, Instant createdAt) {
        this(documentId, title, status, "Document successfully queued for AI processing", createdAt);
    }
}