-- ============================================================
-- AUTH SERVICE - V2
-- Create refresh token table
-- ============================================================

CREATE TABLE refresh_tokens (
    id VARCHAR(36) NOT NULL,
    version BIGINT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,

    token VARCHAR(512) NOT NULL,
    expiry_date TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,

    user_id VARCHAR(36),

    CONSTRAINT pk_refresh_tokens PRIMARY KEY (id),

    CONSTRAINT uk_refresh_tokens_token UNIQUE (token),

    CONSTRAINT fk_refresh_tokens_user
        FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE CASCADE
);

-- ============================================================
-- INDEXES
-- ============================================================

CREATE INDEX idx_refresh_tokens_user_id
    ON refresh_tokens (user_id);

CREATE INDEX idx_refresh_tokens_expiry_date
    ON refresh_tokens (expiry_date);