-- =============================================================================
-- M14 Phase 7: OAuth authorization-code + PKCE pending state
-- Short-lived rows binding a browser redirect to the initiating PA user.
-- =============================================================================

CREATE TABLE oauth_connect_pending (
    id                      UUID         NOT NULL,
    state_token             VARCHAR(64)  NOT NULL,
    code_verifier           VARCHAR(128) NOT NULL,
    user_id                 UUID         NOT NULL,
    base_url                VARCHAR(500) NOT NULL,
    workspace_name          VARCHAR(200),
    expires_at              TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT oauth_connect_pending_pk PRIMARY KEY (id),
    CONSTRAINT oauth_connect_pending_state_uq UNIQUE (state_token),
    CONSTRAINT oauth_connect_pending_user_fk FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_oauth_connect_pending_expires ON oauth_connect_pending (expires_at);

COMMENT ON TABLE oauth_connect_pending IS 'One-time OAuth connect state (CSRF + PKCE) until callback completes.';
