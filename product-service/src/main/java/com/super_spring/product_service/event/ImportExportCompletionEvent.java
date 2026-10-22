package com.super_spring.product_service.event;

import java.time.Instant;

public record ImportExportCompletionEvent(
        String eventId,
        String operationId,
        String userId,
        String operationType,
        String status,
        String fileName,
        Instant completedAt,
        String traceId) {
}