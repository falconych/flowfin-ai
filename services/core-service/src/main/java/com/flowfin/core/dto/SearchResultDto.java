package com.flowfin.core.dto;

public record SearchResultDto(
        String documentId,
        int chunkIndex,
        String content,
        float score
) {}