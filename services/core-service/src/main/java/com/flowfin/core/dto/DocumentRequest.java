package com.flowfin.core.dto;

import jakarta.validation.constraints.NotBlank;

public record DocumentRequest(
    @NotBlank(message = "Title is required")
    String title,

    @NotBlank(message = "Content body cannot be empty")
    String content,

    @NotBlank(message = "Source type is required")
    String source
) {}