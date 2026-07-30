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
}