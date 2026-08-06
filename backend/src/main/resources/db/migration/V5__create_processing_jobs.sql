CREATE TABLE processing_jobs (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL,
    document_id BIGINT NOT NULL,
    parser_type VARCHAR(50) NOT NULL,
    parser_version VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    attempt_count INT NOT NULL DEFAULT 0,
    correlation_id VARCHAR(255),
    failure_code VARCHAR(50),
    result_summary TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_processing_jobs_company FOREIGN KEY (company_id) REFERENCES companies (id),
    CONSTRAINT fk_processing_jobs_document FOREIGN KEY (document_id) REFERENCES documents (id)
);

CREATE TABLE canonical_records (
    id BIGSERIAL PRIMARY KEY,
    job_id BIGINT NOT NULL,
    company_id BIGINT NOT NULL,
    document_number VARCHAR(255) NOT NULL,
    document_date DATE NOT NULL,
    currency VARCHAR(3) NOT NULL,
    amount NUMERIC(19, 4) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_canonical_records_job FOREIGN KEY (job_id) REFERENCES processing_jobs (id),
    CONSTRAINT fk_canonical_records_company FOREIGN KEY (company_id) REFERENCES companies (id)
);

CREATE INDEX idx_processing_jobs_company ON processing_jobs (company_id);
CREATE INDEX idx_processing_jobs_document ON processing_jobs (document_id);
CREATE INDEX idx_canonical_records_job ON canonical_records (job_id);
CREATE INDEX idx_canonical_records_company ON canonical_records (company_id);
