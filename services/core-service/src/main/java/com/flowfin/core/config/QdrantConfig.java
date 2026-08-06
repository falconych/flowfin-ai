package com.flowfin.core.config;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class QdrantConfig {

    @Value("${app.qdrant.host:localhost}")
    private String host;

    @Value("${app.qdrant.port:6334}")
    private int port;

    @Bean
    public QdrantClient qdrantClient() {
        log.info("Initializing Qdrant gRPC client connection to {}:{}", host, port);
        
        return new QdrantClient(
                QdrantGrpcClient.newBuilder(host, port, false)
                        .build()
        );
    }
}