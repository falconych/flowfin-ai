package com.flowfin.core.event;

import java.time.Instant;
import java.util.UUID;

public record DocumentIngestedEvent(
    UUID documentId,
    String title,
    String content,
    Instant timestamp
) {}