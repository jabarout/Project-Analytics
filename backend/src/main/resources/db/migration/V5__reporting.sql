-- =============================================================================
-- Milestone 7 — Reporting: generated report history and file metadata
-- =============================================================================
-- Reports are produced from local analytics/dashboard outputs only.
-- OpenProject is never called from the reporting module.
-- Documented core columns (id, generated_by, report_type, file_path, generated_at)
-- plus format/status/scope needed for PDF/Excel workflow and download.
-- =============================================================================

CREATE TABLE report (
    id                UUID           NOT NULL,
    title             VARCHAR(255)   NOT NULL,
    report_type       VARCHAR(50)    NOT NULL,
    format            VARCHAR(20)    NOT NULL,
    status            VARCHAR(30)    NOT NULL,
    scope_type        VARCHAR(50),
    scope_id          UUID,
    generated_by      UUID           NOT NULL,
    file_path         VARCHAR(1000),
    file_name         VARCHAR(255),
    content_type      VARCHAR(100),
    file_size_bytes   BIGINT,
    error_message     TEXT,
    generated_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT report_pk PRIMARY KEY (id),
    CONSTRAINT report_generated_by_fk FOREIGN KEY (generated_by) REFERENCES users (id)
);

CREATE INDEX idx_report_generated_at ON report (generated_at DESC);
CREATE INDEX idx_report_generated_by ON report (generated_by);
CREATE INDEX idx_report_type ON report (report_type);
CREATE INDEX idx_report_status ON report (status);

COMMENT ON TABLE report IS 'Generated PDF/Excel reports and download metadata (local analytics only).';
