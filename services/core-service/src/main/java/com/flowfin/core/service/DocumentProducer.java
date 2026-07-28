package com.flowfin.core.service;

import com.flowfin.core.dto.DocumentRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.raw-documents}")
    private String rawDocumentsTopic;

    public String sendRawDocument(DocumentRequest request) {
        String documentId = UUID.randomUUID().toString();
        log.info("Publishing raw document event to Kafka topic [{}]: id={}", rawDocumentsTopic, documentId);

        kafkaTemplate.send(rawDocumentsTopic, documentId, request);
        return documentId;
    }
}