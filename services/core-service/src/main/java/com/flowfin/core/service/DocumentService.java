package com.flowfin.core.service;

import com.flowfin.core.dto.DocumentRequest;
import com.flowfin.core.dto.DocumentResponse;
import com.flowfin.core.entity.DocumentEntity;
import com.flowfin.core.entity.DocumentStatus;
import com.flowfin.core.event.DocumentIngestedEvent;
import com.flowfin.core.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentProducer documentProducer;

    @Transactional
    public DocumentResponse processIngestion(DocumentRequest request) {
        log.info("Processing ingestion request for title: '{}'", request.title());

        DocumentEntity entity = DocumentEntity.builder()
                .title(request.title())
                .content(request.content())
                .status(DocumentStatus.PENDING)
                .createdAt(Instant.now())
                .build();
 
        DocumentEntity savedEntity = documentRepository.saveAndFlush(entity);
        log.info("Document successfully persisted to DB with ID: {}", savedEntity.getId());
 
        try {
            DocumentIngestedEvent event = new DocumentIngestedEvent(
                    savedEntity.getId(),
                    savedEntity.getTitle(),
                    savedEntity.getContent(),
                    savedEntity.getCreatedAt()
            );
            documentProducer.sendDocumentIngestedEvent(event);
        } catch (Exception e) {
            log.error("Failed to publish Kafka event for document ID: {}", savedEntity.getId(), e);
        }

        return new DocumentResponse(
                savedEntity.getId(),
                savedEntity.getTitle(),
                savedEntity.getStatus().name(),
                savedEntity.getCreatedAt()
        );
    }
@Transactional
    public void markAsProcessing(UUID documentId) {
        updateStatus(documentId, DocumentStatus.PROCESSING);
    }

    @Transactional
    public void markAsCompleted(UUID documentId) {
        updateStatus(documentId, DocumentStatus.COMPLETED);
    }

    @Transactional
    public void markAsFailed(UUID documentId) {
        updateStatus(documentId, DocumentStatus.FAILED);
    }

    private void updateStatus(UUID documentId, DocumentStatus newStatus) {
        if (documentId == null) {
            log.warn("Cannot update status: documentId is null");
            return;
        }

        documentRepository.findById(documentId).ifPresentOrElse(
                entity -> {
                    entity.setStatus(newStatus);
                    documentRepository.save(entity);
                    log.info("Document status updated to {} for documentId: {}", newStatus, documentId);
                },
                () -> log.error("Document not found in database for documentId: {}", documentId)
        );
    }
}