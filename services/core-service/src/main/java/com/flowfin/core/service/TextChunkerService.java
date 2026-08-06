package com.flowfin.core.service;

import com.flowfin.core.dto.DocumentChunk;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class TextChunkerService {

    @Value("${app.chunking.max-chunk-size:500}")
    private int maxChunkSize;

    @Value("${app.chunking.overlap-size:50}")
    private int overlapSize;

    /**
     * Splits document content into smaller sequential text chunks with configured overlap.
     *
     * @param documentId Unique identifier of the target document
     * @param content Raw text payload of the document
     * @return List of processed document chunks
     */
    public List<DocumentChunk> chunkText(UUID documentId, String content) {
        if (content == null || content.isBlank()) {
            log.warn("Provided content for documentId: '{}' is empty. Skipping chunking.", documentId);
            return Collections.emptyList();
        }

        List<DocumentChunk> chunks = new ArrayList<>();
        int textLength = content.length();
        int step = maxChunkSize - overlapSize;

        // Ensure step size is strictly positive to prevent infinite loops
        if (step <= 0) {
            log.error("Invalid chunk configuration: maxChunkSize ({}) must be greater than overlapSize ({}). Falling back to step = maxChunkSize.",
                    maxChunkSize, overlapSize);
            step = maxChunkSize;
        }

        int chunkIndex = 0;
        for (int start = 0; start < textLength; start += step) {
            int end = Math.min(start + maxChunkSize, textLength);
            String chunkText = content.substring(start, end).trim();

            if (!chunkText.isEmpty()) {
                chunks.add(new DocumentChunk(
                        documentId,
                        chunkIndex++,
                        chunkText,
                        chunkText.length()
                ));
            }

            // Boundary check: if end reached text boundary, terminate chunking
            if (end == textLength) {
                break;
            }
        }

        log.info("Successfully produced {} chunks for documentId: '{}'", chunks.size(), documentId);
        return chunks;
    }
}
