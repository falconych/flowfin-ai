package com.flowfin.core.service;

import com.flowfin.core.event.DocumentIngestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.raw-documents}")
    private String rawDocumentsTopic;

    public void sendDocumentIngestedEvent(DocumentIngestedEvent event) {
 
        String messageKey = event.documentId() != null ? event.documentId().toString() : null;

        log.info("Publishing raw document event to Kafka topic [{}]: documentId={}", rawDocumentsTopic, messageKey);

        kafkaTemplate.send(rawDocumentsTopic, messageKey, event);
    }
}