package com.super_spring.product_service.event;

import java.time.Instant;

public record ImportExportCompletionEvent(
        String operation,
        String status,
        Instant completedAt,
        String traceId) {
}
