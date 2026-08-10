-- =============================================================================
-- Milestone 8 — Recommendations: rule-based decision intelligence outputs
-- =============================================================================
-- Recommendations are derived from stored analytics only (no OpenProject I/O,
-- no new scoring formulas). Core columns match docs/06_Database_Design.md
-- (analytics_id, title, description, severity, explanation, generated_at)
-- plus rule_code, priority, supporting metrics, and project_id for query scope.
-- =============================================================================

CREATE TABLE recommendation (
    id                   UUID         NOT NULL,
    analytics_id         UUID         NOT NULL,
    project_id           UUID         NOT NULL,
    rule_code            VARCHAR(80)  NOT NULL,
    title                VARCHAR(255) NOT NULL,
    description          TEXT         NOT NULL,
    severity             VARCHAR(30)  NOT NULL,
    explanation          TEXT         NOT NULL,
    suggested_action     TEXT,
    priority_rank        INTEGER      NOT NULL,
    supporting_metrics   TEXT,
    generated_at         TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT recommendation_pk PRIMARY KEY (id),
    CONSTRAINT recommendation_analytics_fk FOREIGN KEY (analytics_id) REFERENCES analytics (id),
    CONSTRAINT recommendation_project_fk FOREIGN KEY (project_id) REFERENCES project (id)
);

CREATE INDEX idx_recommendation_project_id ON recommendation (project_id);
CREATE INDEX idx_recommendation_analytics_id ON recommendation (analytics_id);
CREATE INDEX idx_recommendation_severity ON recommendation (severity);
CREATE INDEX idx_recommendation_generated_at ON recommendation (generated_at DESC);
CREATE INDEX idx_recommendation_priority ON recommendation (priority_rank ASC);

COMMENT ON TABLE recommendation IS 'Deterministic recommendations derived from analytics scores/KPIs/trends.';
