-- =============================================================================
-- Email confirmation on signup (must verify before login)
-- Existing users backfilled as verified=true.
-- =============================================================================

ALTER TABLE users
    ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT TRUE;

COMMENT ON COLUMN users.email_verified IS 'False until signup confirmation link is used; existing rows default true.';

CREATE TABLE email_confirmation_token (
    id              UUID         NOT NULL,
    user_id         UUID         NOT NULL,
    token_hash      VARCHAR(128) NOT NULL,
    expires_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    used_at         TIMESTAMP WITH TIME ZONE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT email_confirmation_token_pk PRIMARY KEY (id),
    CONSTRAINT email_confirmation_token_hash_uq UNIQUE (token_hash),
    CONSTRAINT email_confirmation_token_user_fk FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_email_confirmation_token_user ON email_confirmation_token (user_id);
CREATE INDEX idx_email_confirmation_token_expires ON email_confirmation_token (expires_at);

COMMENT ON TABLE email_confirmation_token IS 'Hashed single-use email confirmation tokens (never store raw token).';
