-- =============================================================================
-- Milestone 2 — Authentication: users and preferences
-- =============================================================================
-- Domain entity "User" is stored in table "users" because USER is a reserved
-- SQL keyword in PostgreSQL/H2. Semantics match docs/06_Database_Design.md.
-- =============================================================================

CREATE TABLE users (
    id              UUID         NOT NULL,
    username        VARCHAR(100) NOT NULL,
    email           VARCHAR(255) NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    role            VARCHAR(50)  NOT NULL,
    enabled         BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT users_pk PRIMARY KEY (id),
    CONSTRAINT users_username_uq UNIQUE (username),
    CONSTRAINT users_email_uq UNIQUE (email)
);

CREATE TABLE user_preference (
    id                        UUID         NOT NULL,
    user_id                   UUID         NOT NULL,
    theme                     VARCHAR(50)  NOT NULL DEFAULT 'light',
    language                  VARCHAR(20)  NOT NULL DEFAULT 'en',
    dashboard_configuration   TEXT,
    created_at                TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT user_preference_pk PRIMARY KEY (id),
    CONSTRAINT user_preference_user_id_uq UNIQUE (user_id),
    CONSTRAINT user_preference_user_fk FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX idx_users_role ON users (role);
CREATE INDEX idx_users_enabled ON users (enabled);

-- Development seed administrator (password: Admin123!).
-- BCrypt hash only — plaintext password is never stored.
INSERT INTO users (id, username, email, password_hash, role, enabled, created_at, updated_at)
VALUES (
    '11111111-1111-1111-1111-111111111111',
    'admin',
    'admin@projectanalytics.local',
    '$2b$10$wXQMyRuBJQdpYMpzON6wceyfDghPKvwA6M5uIVIj5RTxLg9tJEez6',
    'ADMINISTRATOR',
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

INSERT INTO user_preference (id, user_id, theme, language, dashboard_configuration, created_at, updated_at)
VALUES (
    '22222222-2222-2222-2222-222222222222',
    '11111111-1111-1111-1111-111111111111',
    'light',
    'en',
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

COMMENT ON TABLE users IS 'Platform users (authentication and RBAC).';
COMMENT ON TABLE user_preference IS 'Per-user UI preferences (theme, language, layout).';
