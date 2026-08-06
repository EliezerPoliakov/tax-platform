CREATE TABLE companies (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE company_members (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    company_id BIGINT NOT NULL,
    role VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_company_members_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_company_members_company FOREIGN KEY (company_id) REFERENCES companies (id),
    CONSTRAINT uk_company_members_user_company UNIQUE (user_id, company_id),
    CONSTRAINT chk_company_members_role CHECK (role IN ('OWNER', 'MEMBER'))
);

CREATE INDEX idx_company_members_user ON company_members (user_id);
CREATE INDEX idx_company_members_company ON company_members (company_id);
