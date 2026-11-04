package com.super_spring.product_service.messaging;

import java.time.Instant;

public record ImportExportCompletionEvent(
        String eventId,
        String operationId, // Unique identifier for the import/export operation
        String userId,
        String operationType, // "IMPORT" or "EXPORT"
        String status, // "SUCCESS" or "FAILURE"
        String fileName,
        Instant completedAt,
        String traceId) {
}