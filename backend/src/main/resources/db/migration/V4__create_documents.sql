CREATE TABLE documents (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL,
    storage_key VARCHAR(255) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(255) NOT NULL,
    size_bytes BIGINT NOT NULL,
    sha256_checksum VARCHAR(64) NOT NULL,
    integration_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_by_user_id BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_documents_company FOREIGN KEY (company_id) REFERENCES companies (id),
    CONSTRAINT fk_documents_user FOREIGN KEY (created_by_user_id) REFERENCES users (id),
    CONSTRAINT uk_documents_storage_key UNIQUE (storage_key)
);

CREATE INDEX idx_documents_company ON documents (company_id);
CREATE INDEX idx_documents_created_at ON documents (created_at);
