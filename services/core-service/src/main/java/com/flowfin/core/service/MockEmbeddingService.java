package com.flowfin.core.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
public class MockEmbeddingService {

    @Value("${app.qdrant.vector-size:384}")
    private int vectorSize;

    /**
     * Generates a deterministic mock embedding vector of configured dimension for testing.
     *
     * @param text Input string content
     * @return List of Float values representing a normalized dummy vector
     */
    public List<Float> generateEmbedding(String text) {
        if (text == null) {
            text = "";
        }

        Random random = new Random(text.hashCode());
        List<Float> vector = new ArrayList<>(vectorSize);

        float sumSquares = 0.0f;
        for (int i = 0; i < vectorSize; i++) {
            float val = (random.nextFloat() * 2.0f) - 1.0f;
            vector.add(val);
            sumSquares += val * val;
        }

        // L2 Normalization
        float norm = (float) Math.sqrt(sumSquares);
        for (int i = 0; i < vectorSize; i++) {
            vector.set(i, vector.get(i) / norm);
        }

        return vector;
    }
}
