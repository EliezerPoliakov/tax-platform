package com.poliakov.taxplatform.documents;

import java.time.Instant;

public record DocumentResponse(
        Long id,
        Long companyId,
        String originalFilename,
        String contentType,
        Long sizeBytes,
        String sha256Checksum,
        IntegrationType integrationType,
        DocumentStatus status,
        Long createdByUserId,
        Instant createdAt
) {
    public static DocumentResponse fromEntity(Document document) {
        return new DocumentResponse(
                document.getId(),
                document.getCompanyId(),
                document.getOriginalFilename(),
                document.getContentType(),
                document.getSizeBytes(),
                document.getSha256Checksum(),
                document.getIntegrationType(),
                document.getStatus(),
                document.getCreatedByUserId(),
                document.getCreatedAt()
        );
    }
}
