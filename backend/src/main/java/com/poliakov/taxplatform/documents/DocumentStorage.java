package com.poliakov.taxplatform.documents;

import java.io.InputStream;

public interface DocumentStorage {
    void store(String key, InputStream inputStream);
    InputStream load(String key);
    void delete(String key);
}
