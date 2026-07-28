package com.flowfin.core.dto;

import jakarta.validation.constraints.NotBlank;

public record DocumentRequest(
        @NotBlank(message = "Source is required")
        String source,

        @NotBlank(message = "Raw text content is required")
        String rawContent
) {}