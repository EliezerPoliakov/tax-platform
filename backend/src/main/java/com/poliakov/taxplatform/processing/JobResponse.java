package com.poliakov.taxplatform.processing;

import java.time.Instant;

public record JobResponse(
        Long id,
        Long companyId,
        Long documentId,
        String parserType,
        String parserVersion,
        JobStatus status,
        int attemptCount,
        String correlationId,
        String failureCode,
        String resultSummary,
        Instant createdAt,
        Instant updatedAt
) {
    public static JobResponse fromEntity(ProcessingJob job) {
        return new JobResponse(
                job.getId(),
                job.getCompanyId(),
                job.getDocumentId(),
                job.getParserType(),
                job.getParserVersion(),
                job.getStatus(),
                job.getAttemptCount(),
                job.getCorrelationId(),
                job.getFailureCode(),
                job.getResultSummary(),
                job.getCreatedAt(),
                job.getUpdatedAt()
        );
    }
}
