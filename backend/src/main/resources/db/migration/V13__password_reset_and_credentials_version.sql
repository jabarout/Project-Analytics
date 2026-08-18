-- =============================================================================
-- Phase 4: password reset tokens + credentials version for JWT invalidation
-- =============================================================================

ALTER TABLE users ADD COLUMN credentials_version INTEGER NOT NULL DEFAULT 0;

CREATE TABLE password_reset_token (
    id              UUID         NOT NULL,
    user_id         UUID         NOT NULL,
    token_hash      VARCHAR(128) NOT NULL,
    expires_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    used_at         TIMESTAMP WITH TIME ZONE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT password_reset_token_pk PRIMARY KEY (id),
    CONSTRAINT password_reset_token_hash_uq UNIQUE (token_hash),
    CONSTRAINT password_reset_token_user_fk FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_password_reset_token_user ON password_reset_token (user_id);
CREATE INDEX idx_password_reset_token_expires ON password_reset_token (expires_at);

COMMENT ON COLUMN users.credentials_version IS 'Incremented on password change; JWT claim must match or token is rejected.';
COMMENT ON TABLE password_reset_token IS 'Hashed single-use password reset tokens (never store raw token).';
