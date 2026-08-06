package com.flowfin.core.service;

import com.flowfin.core.dto.DocumentChunk;
import com.flowfin.core.dto.SearchResultDto;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Collections.Distance;
import io.qdrant.client.grpc.Collections.VectorParams;
import io.qdrant.client.grpc.Points.PointId;
import io.qdrant.client.grpc.Points.PointStruct;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static io.qdrant.client.PointIdFactory.id;
import static io.qdrant.client.ValueFactory.value;
import static io.qdrant.client.VectorsFactory.vectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QdrantService {

    private final QdrantClient qdrantClient;
    private final MockEmbeddingService embeddingService;

    @Value("${app.qdrant.collection-name:document_chunks}")
    private String collectionName;

    @Value("${app.qdrant.vector-size:384}")
    private int vectorSize;

    /**
     * Initializes the Qdrant vector collection on startup if it does not exist.
     */
    @PostConstruct
    public void initCollection() {
        try {
            Boolean exists = qdrantClient.collectionExistsAsync(collectionName).get();
            if (Boolean.FALSE.equals(exists)) {
                log.info("Collection '{}' does not exist in Qdrant. Creating new collection...", collectionName);

                VectorParams params = VectorParams.newBuilder()
                        .setSize(vectorSize)
                        .setDistance(Distance.Cosine)
                        .build();

                qdrantClient.createCollectionAsync(collectionName, params).get();

                log.info("Successfully created Qdrant collection: '{}'", collectionName);
            } else {
                log.info("Qdrant collection '{}' already exists. Skipping initialization.", collectionName);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Thread interrupted during collection initialization", e);
        } catch (ExecutionException e) {
            log.error("Failed to initialize Qdrant collection: '{}'", collectionName, e.getCause());
        }
    }

    /**
     * Converts text chunks into embedding vectors and persists them to Qdrant collection.
     *
     * @param chunks List of processed text chunks from a document
     */
    public void upsertChunks(List<DocumentChunk> chunks) {
        if (chunks == null || chunks.isEmpty()) {
            return;
        }

        List<PointStruct> points = new ArrayList<>();

        for (DocumentChunk chunk : chunks) {
            List<Float> embedding = embeddingService.generateEmbedding(chunk.content());
            PointId pointId = id(UUID.randomUUID());

            PointStruct point = PointStruct.newBuilder()
                    .setId(pointId)
                    .setVectors(vectors(embedding))
                    .putAllPayload(Map.of(
                            "documentId", value(chunk.documentId().toString()),
                            "chunkIndex", value(chunk.chunkIndex()),
                            "content", value(chunk.content())
                    ))
                    .build();

            points.add(point);
        }

        try {
            qdrantClient.upsertAsync(collectionName, points).get();
            log.info("Successfully persisted {} vector points to Qdrant collection '{}'",
                    points.size(), collectionName);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Thread interrupted while persisting vectors to Qdrant", e);
            throw new RuntimeException("Interrupted during Qdrant upsert", e);
        } catch (ExecutionException e) {
            log.error("Execution error occurred during Qdrant upsert operation", e.getCause());
            throw new RuntimeException("Qdrant upsert execution failed", e.getCause());
        }
    }

    /**
     * Searches for top-k similar chunks in Qdrant based on vector similarity.
     *
     * @param queryText Search query text from user
     * @param limit     Top-K matches limit
     * @return List of matching chunks sorted by relevance score
     */
    public List<SearchResultDto> searchSimilarChunks(String queryText, int limit) {
        List<Float> queryVector = embeddingService.generateEmbedding(queryText);

        try {
            List<io.qdrant.client.grpc.Points.ScoredPoint> searchPoints = qdrantClient.searchAsync(
                                io.qdrant.client.grpc.Points.SearchPoints.newBuilder()
                                        .setCollectionName(collectionName)
                                        .addAllVector(queryVector)
                                        .setLimit(limit)
                                        .setWithPayload(io.qdrant.client.WithPayloadSelectorFactory.enable(true))
                                        .build()
                        ).get();

            List<SearchResultDto> results = new ArrayList<>();
            for (io.qdrant.client.grpc.Points.ScoredPoint point : searchPoints) {
                var payload = point.getPayloadMap();
                results.add(new SearchResultDto(
                        payload.getOrDefault("documentId", value("")).getStringValue(),
                        (int) payload.getOrDefault("chunkIndex", value(0)).getIntegerValue(),
                        payload.getOrDefault("content", value("")).getStringValue(),
                        point.getScore()
                ));
            }

            log.info("Found {} matching points for search query: '{}'", results.size(), queryText);
            return results;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Search thread interrupted", e);
            throw new RuntimeException("Search operation interrupted", e);
        } catch (ExecutionException e) {
            log.error("Execution error during Qdrant search", e.getCause());
            throw new RuntimeException("Search operation failed", e.getCause());
        }
    }
}