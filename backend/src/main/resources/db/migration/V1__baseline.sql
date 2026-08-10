-- =============================================================================
-- Milestone 1 — Foundation baseline
-- =============================================================================
-- Establishes Flyway versioning for Project Analytics.
-- Domain tables (workspace, portfolio, project, analytics, ...) are introduced
-- in later milestones per docs/06_Database_Design.md.
--
-- PostgreSQL is the system of record. Redis is cache-only and has no schema here.
-- =============================================================================

CREATE TABLE IF NOT EXISTS schema_foundation (
    id              SMALLINT NOT NULL,
    application     VARCHAR(100) NOT NULL,
    schema_version  VARCHAR(50)  NOT NULL,
    applied_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT schema_foundation_pk PRIMARY KEY (id),
    CONSTRAINT schema_foundation_singleton CHECK (id = 1)
);

INSERT INTO schema_foundation (id, application, schema_version)
SELECT 1, 'project-analytics', 'M1-foundation'
WHERE NOT EXISTS (SELECT 1 FROM schema_foundation WHERE id = 1);

COMMENT ON TABLE schema_foundation IS
    'Foundation marker table confirming Flyway baseline for Milestone 1.';
