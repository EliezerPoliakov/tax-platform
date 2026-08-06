package com.poliakov.taxplatform.processing;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "canonical_records")
public class CanonicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_id", nullable = false)
    private Long jobId;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "document_number", nullable = false)
    private String documentNumber;

    @Column(name = "document_date", nullable = false)
    private LocalDate documentDate;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected CanonicalRecord() {}

    public CanonicalRecord(Long jobId, Long companyId, String documentNumber, LocalDate documentDate, String currency, BigDecimal amount) {
        this.jobId = jobId;
        this.companyId = companyId;
        this.documentNumber = documentNumber;
        this.documentDate = documentDate;
        this.currency = currency;
        this.amount = amount;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public Long getJobId() { return jobId; }
    public Long getCompanyId() { return companyId; }
    public String getDocumentNumber() { return documentNumber; }
    public LocalDate getDocumentDate() { return documentDate; }
    public String getCurrency() { return currency; }
    public BigDecimal getAmount() { return amount; }
    public Instant getCreatedAt() { return createdAt; }
}
