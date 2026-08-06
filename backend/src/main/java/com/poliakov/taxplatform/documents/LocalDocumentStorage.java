package com.poliakov.taxplatform.documents;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Component
public class LocalDocumentStorage implements DocumentStorage {

    private static final Logger log = LoggerFactory.getLogger(LocalDocumentStorage.class);
    private final Path rootLocation;

    public LocalDocumentStorage(@Value("${app.storage.root:.local-storage/documents}") String root) {
        this.rootLocation = Paths.get(root);
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage location", e);
        }
    }

    @Override
    public void store(String key, InputStream inputStream) {
        validateKey(key);
        try {
            Path destinationFile = this.rootLocation.resolve(Paths.get(key)).normalize().toAbsolutePath();
            if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
                // This is a security check for path traversal
                throw new RuntimeException("Cannot store file outside current directory.");
            }
            Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file.", e);
        }
    }

    @Override
    public void delete(String key) {
        validateKey(key);
        try {
            Path file = this.rootLocation.resolve(Paths.get(key)).normalize().toAbsolutePath();
            if (file.getParent().equals(this.rootLocation.toAbsolutePath())) {
                Files.deleteIfExists(file);
            } else {
                log.warn("Attempted to delete file outside storage root: {}", key);
            }
        } catch (IOException e) {
            log.error("Failed to delete file: {}", key, e);
        }
    }

    private void validateKey(String key) {
        if (key == null || key.isEmpty() || key.contains("..") || key.contains("/") || key.contains("\\")) {
            throw new IllegalArgumentException("Invalid storage key: " + key);
        }
    }
}
