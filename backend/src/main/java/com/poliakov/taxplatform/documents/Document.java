package com.poliakov.taxplatform.documents;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "storage_key", nullable = false, unique = true)
    private String storageKey;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;

    @Column(name = "sha256_checksum", nullable = false, length = 64)
    private String sha256Checksum;

    @Enumerated(EnumType.STRING)
    @Column(name = "integration_type", nullable = false)
    private IntegrationType integrationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DocumentStatus status;

    @Column(name = "created_by_user_id", nullable = false)
    private Long createdByUserId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Document() {
    }

    public Document(Long companyId, String storageKey, String originalFilename, String contentType,
                    Long sizeBytes, String sha256Checksum, IntegrationType integrationType,
                    DocumentStatus status, Long createdByUserId, Instant createdAt) {
        this.companyId = companyId;
        this.storageKey = storageKey;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
        this.sha256Checksum = sha256Checksum;
        this.integrationType = integrationType;
        this.status = status;
        this.createdByUserId = createdByUserId;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public String getContentType() {
        return contentType;
    }

    public Long getSizeBytes() {
        return sizeBytes;
    }

    public String getSha256Checksum() {
        return sha256Checksum;
    }

    public IntegrationType getIntegrationType() {
        return integrationType;
    }

    public DocumentStatus getStatus() {
        return status;
    }

    public Long getCreatedByUserId() {
        return createdByUserId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
