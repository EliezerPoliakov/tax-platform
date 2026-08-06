package com.poliakov.taxplatform.processing;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Map;

@Entity
@Table(name = "processing_jobs")
public class ProcessingJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "document_id", nullable = false)
    private Long documentId;

    @Column(name = "parser_type", nullable = false)
    private String parserType;

    @Column(name = "parser_version", nullable = false)
    private String parserVersion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "correlation_id")
    private String correlationId;

    @Column(name = "failure_code")
    private String failureCode;

    // We can use a Map for the summary, but since it's JSONB in Postgres,
    // and we are using standard JPA, we might need a converter or just store as string/map.
    // For simplicity in this synthetic task, let's keep it basic.
    // Given the requirement "Persist a result summary containing: canonical schema version; record count; totals grouped by currency."
    @Column(name = "result_summary", columnDefinition = "text")
    private String resultSummary;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ProcessingJob() {}

    public ProcessingJob(Long companyId, Long documentId, String parserType, String parserVersion, String correlationId) {
        this.companyId = companyId;
        this.documentId = documentId;
        this.parserType = parserType;
        this.parserVersion = parserVersion;
        this.correlationId = correlationId;
        this.status = JobStatus.CREATED;
        this.attemptCount = 0;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public Long getCompanyId() { return companyId; }
    public Long getDocumentId() { return documentId; }
    public String getParserType() { return parserType; }
    public String getParserVersion() { return parserVersion; }
    public JobStatus getStatus() { return status; }
    public void setStatus(JobStatus status) { this.status = status; this.updatedAt = Instant.now(); }
    public int getAttemptCount() { return attemptCount; }
    public void setAttemptCount(int attemptCount) { this.attemptCount = attemptCount; this.updatedAt = Instant.now(); }
    public String getCorrelationId() { return correlationId; }
    public String getFailureCode() { return failureCode; }
    public void setFailureCode(String failureCode) { this.failureCode = failureCode; this.updatedAt = Instant.now(); }
    public String getResultSummary() { return resultSummary; }
    public void setResultSummary(String resultSummary) { this.resultSummary = resultSummary; this.updatedAt = Instant.now(); }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
