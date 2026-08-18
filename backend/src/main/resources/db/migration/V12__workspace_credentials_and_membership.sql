-- =============================================================================
-- M14 / M15 Hybrid access: per-workspace credentials + PA workspace membership
-- =============================================================================
-- Credentials are server-side only (never returned in API DTOs).
-- Membership gates analytics access; connector becomes workspace admin on eligible connect.
-- =============================================================================

CREATE TABLE workspace_credential (
    id                      UUID         NOT NULL,
    workspace_id            UUID         NOT NULL,
    auth_scheme             VARCHAR(30)  NOT NULL,
    -- AES-GCM ciphertext (Base64) of API key or OAuth access token
    secret_ciphertext       TEXT         NOT NULL,
    -- Optional refresh token ciphertext (OAuth)
    refresh_ciphertext      TEXT,
    openproject_user_id     BIGINT,
    openproject_login       VARCHAR(200),
    openproject_email       VARCHAR(255),
    openproject_admin       BOOLEAN,
    eligibility_snapshot    VARCHAR(500),
    token_expires_at        TIMESTAMP WITH TIME ZONE,
    created_by_user_id      UUID         NOT NULL,
    created_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT workspace_credential_pk PRIMARY KEY (id),
    CONSTRAINT workspace_credential_workspace_uq UNIQUE (workspace_id),
    CONSTRAINT workspace_credential_workspace_fk FOREIGN KEY (workspace_id) REFERENCES workspace (id) ON DELETE CASCADE,
    CONSTRAINT workspace_credential_user_fk FOREIGN KEY (created_by_user_id) REFERENCES users (id)
);

CREATE TABLE workspace_membership (
    id                      UUID         NOT NULL,
    workspace_id            UUID         NOT NULL,
    user_id                 UUID         NOT NULL,
    workspace_admin         BOOLEAN      NOT NULL DEFAULT FALSE,
    analytics_access        BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT workspace_membership_pk PRIMARY KEY (id),
    CONSTRAINT workspace_membership_uq UNIQUE (workspace_id, user_id),
    CONSTRAINT workspace_membership_workspace_fk FOREIGN KEY (workspace_id) REFERENCES workspace (id) ON DELETE CASCADE,
    CONSTRAINT workspace_membership_user_fk FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_workspace_membership_user ON workspace_membership (user_id);
CREATE INDEX idx_workspace_membership_workspace ON workspace_membership (workspace_id);

COMMENT ON TABLE workspace_credential IS 'Server-side OpenProject credentials per workspace (API key or OAuth). Never expose to frontend.';
COMMENT ON TABLE workspace_membership IS 'PA user access to a workspace: workspace_admin and/or analytics_access.';
-- Legacy seed-admin membership backfill is done in WorkspaceMembershipBootstrap (H2 + Postgres safe).
