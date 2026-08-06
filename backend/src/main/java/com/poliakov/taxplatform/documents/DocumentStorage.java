package com.poliakov.taxplatform.documents;

import java.io.InputStream;

public interface DocumentStorage {
    void store(String key, InputStream inputStream);
    void delete(String key);
}
