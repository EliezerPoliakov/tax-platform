package com.poliakov.taxplatform.documents;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalDocumentStorageTest {

    @TempDir
    Path tempDir;

    private LocalDocumentStorage storage;

    @BeforeEach
    void setUp() {
        storage = new LocalDocumentStorage(tempDir.toString());
    }

    @Test
    void storeAndRetrieveFile() throws IOException {
        String key = "test-file-123";
        String content = "hello world";
        storage.store(key, new ByteArrayInputStream(content.getBytes()));

        Path storedFile = tempDir.resolve(key);
        assertThat(storedFile).exists();
        assertThat(Files.readString(storedFile)).isEqualTo(content);
    }

    @Test
    void deleteFile() throws IOException {
        String key = "delete-me";
        Files.writeString(tempDir.resolve(key), "content");

        storage.delete(key);

        assertThat(tempDir.resolve(key)).doesNotExist();
    }

    @Test
    void rejectPathTraversalOnStore() {
        String key = "../outside.txt";
        assertThatThrownBy(() -> storage.store(key, new ByteArrayInputStream("content".getBytes())))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectPathTraversalWithSubdirectoryOnStore() {
        // Even if we tried to use subdirectories in the key, our implementation forbids '/' and '\\'
        assertThatThrownBy(() -> storage.store("sub/file.txt", new ByteArrayInputStream("content".getBytes())))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectEmptyKey() {
        assertThatThrownBy(() -> storage.store("", new ByteArrayInputStream("content".getBytes())))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deleteNonExistentFileDoesNotThrow() {
        storage.delete("non-existent");
    }
}
