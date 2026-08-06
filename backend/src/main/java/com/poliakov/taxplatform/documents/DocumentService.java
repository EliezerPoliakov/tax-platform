package com.poliakov.taxplatform.documents;

import com.poliakov.taxplatform.identity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class DocumentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);

    private final DocumentRepository documentRepository;
    private final DocumentStorage documentStorage;
    private final long maxFileSize;

    public DocumentService(
            DocumentRepository documentRepository,
            DocumentStorage documentStorage,
            @Value("${app.documents.max-size:5242880}") long maxFileSize
    ) {
        this.documentRepository = documentRepository;
        this.documentStorage = documentStorage;
        this.maxFileSize = maxFileSize;
    }

    @Transactional(readOnly = true)
    public List<Document> listDocuments(Long companyId) {
        return documentRepository.findAllByCompanyId(companyId);
    }

    @Transactional(readOnly = true)
    public Optional<Document> getDocument(Long documentId, Long companyId) {
        return documentRepository.findByIdAndCompanyId(documentId, companyId);
    }

    @Transactional
    public Document uploadDocument(Long companyId, User user, MultipartFile file) {
        validateFile(file);

        String storageKey = UUID.randomUUID().toString();
        String checksum;
        long size;

        // Use a temporary file to compute checksum and size if necessary,
        // or do it while streaming to storage.
        // For SHA-256 and size, we can do it during the store operation or before.
        // Given we need the checksum for the metadata, and we want to be memory efficient.

        Path tempFile = null;
        try {
            tempFile = Files.createTempFile("upload-", ".tmp");
            try (InputStream is = file.getInputStream()) {
                size = copyAndComputeHash(is, tempFile);
            }

            if (size > maxFileSize) {
                throw new DocumentException("FILE_TOO_LARGE", "File size exceeds maximum allowed limit");
            }

            checksum = computeHash(tempFile);

            // Store to permanent storage
            try (InputStream is = Files.newInputStream(tempFile)) {
                documentStorage.store(storageKey, is);
            }

            Document document = new Document(
                    companyId,
                    storageKey,
                    file.getOriginalFilename(),
                    file.getContentType(),
                    size,
                    checksum,
                    IntegrationType.MANUAL_UPLOAD,
                    DocumentStatus.UPLOADED,
                    user.getId(),
                    Instant.now()
            );

            try {
                return documentRepository.save(document);
            } catch (Exception e) {
                log.error("Failed to persist document metadata, cleaning up storage: {}", storageKey, e);
                try {
                    documentStorage.delete(storageKey);
                } catch (Exception deleteEx) {
                    log.error("Failed to cleanup storage after metadata persistence failure: {}", storageKey, deleteEx);
                    // Do not silently ignore cleanup failures as per requirements
                    throw new DocumentException("STORAGE_CLEANUP_FAILED", "Failed to persist metadata and storage cleanup failed");
                }
                throw e;
            }

        } catch (IOException | NoSuchAlgorithmException e) {
            log.error("Error during document upload processing", e);
            throw new DocumentException("UPLOAD_ERROR", "An error occurred during file upload processing");
        } finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException e) {
                    log.warn("Failed to delete temporary upload file: {}", tempFile, e);
                }
            }
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new DocumentException("EMPTY_FILE", "Uploaded file is empty");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
            throw new DocumentException("INVALID_FILE_TYPE", "Only CSV files are accepted");
        }
    }

    private long copyAndComputeHash(InputStream is, Path target) throws IOException {
        return Files.copy(is, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }

    private String computeHash(Path path) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream is = Files.newInputStream(path)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
        }
        return HexFormat.of().formatHex(digest.digest());
    }
}
