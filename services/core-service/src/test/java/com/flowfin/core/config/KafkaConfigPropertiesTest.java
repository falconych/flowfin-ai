package com.flowfin.core.config;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class KafkaConfigPropertiesTest {

    @Test
    void kafkaDefaultValueTypeShouldReferenceTheDocumentIngestedEvent() throws IOException {
        Path applicationYaml = Path.of("src/main/resources/application.yml");
        String content = Files.readString(applicationYaml);

        assertThat(content)
                .contains("spring.json.value.default.type: \"com.flowfin.core.event.DocumentIngestedEvent\"");
    }
}
