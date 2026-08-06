package com.flowfin.core.dto;

import java.util.UUID;

public record DocumentChunk(
        UUID documentId,
        int chunkIndex,
        String content,
        int charCount
) {}
