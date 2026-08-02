package com.flowfin.core.service;

import com.flowfin.core.dto.DocumentChunk;
import com.flowfin.core.event.DocumentIngestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentConsumer {

    private final DocumentService documentService;
    private final TextChunkerService textChunkerService;

    // Timeout duration in seconds (defaults to 10s if not specified in configuration)
    @Value("${app.processing.timeout-seconds:10}")
    private long processingTimeoutSeconds;

    @KafkaListener(
            topics = "${app.kafka.topics.raw-documents}",
            groupId = "${spring.kafka.consumer.group-id:core-service-group}"
    )
    public void consumeDocumentIngestedEvent(DocumentIngestedEvent event) {
        log.info("Received raw document event from Kafka: documentId='{}', title='{}'",
                event.documentId(), event.title());

        try {
            // 1. Set initial processing status
            documentService.markAsProcessing(event.documentId());

            // 2. Execute processing pipeline asynchronously with strict timeout constraint
            CompletableFuture.runAsync(() -> processDocumentPipeline(event))
                    .orTimeout(processingTimeoutSeconds, TimeUnit.SECONDS)
                    .join(); // Await completion or catch TimeoutException / CompletionException

            // 3. Update status to COMPLETED upon successful pipeline execution
            documentService.markAsCompleted(event.documentId());

        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;

            if (cause instanceof TimeoutException) {
                log.error("Processing timed out after {} seconds for documentId: '{}'",
                        processingTimeoutSeconds, event.documentId());
            } else {
                log.error("Failed to process ingested document event for documentId: '{}'",
                        event.documentId(), cause);
            }

            // Mark document status as FAILED in database
            documentService.markAsFailed(event.documentId());

            // Rethrow exception to trigger standard Kafka error handling / retry policy
            throw new RuntimeException("Document processing failed for id: " + event.documentId(), cause);
        }
    }

    /**
     * Core ingestion pipeline processing steps (Chunking -> Embedding -> Vector DB insertion).
     */
    private void processDocumentPipeline(DocumentIngestedEvent event) {
        // Perform text chunking
        List<DocumentChunk> chunks = textChunkerService.chunkText(event.documentId(), event.content());

        chunks.forEach(chunk -> log.info("Generated Chunk [index={}]: '{}' (len={})",
                chunk.chunkIndex(), chunk.content(), chunk.charCount()));

        // TODO (Phase 3): Generate embeddings and persist vectors to Qdrant
    }
}