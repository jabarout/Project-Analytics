-- =============================================================================
-- Milestone 5 — Analytics Engine persistence
-- =============================================================================
-- Scores are computed from local synchronized data only. Explainable text is
-- stored with each calculation. Snapshots support trend endpoints.
-- =============================================================================

CREATE TABLE analytics (
    id                       UUID           NOT NULL,
    project_id               UUID           NOT NULL,
    health_score             NUMERIC(5, 2)  NOT NULL,
    risk_score               NUMERIC(5, 2)  NOT NULL,
    attention_score          NUMERIC(5, 2)  NOT NULL,
    completion_percentage    NUMERIC(5, 2),
    budget_variance          NUMERIC(12, 4),
    schedule_variance        NUMERIC(12, 4),
    health_status            VARCHAR(50),
    health_explanation       TEXT,
    risk_level               VARCHAR(50),
    risk_explanation         TEXT,
    attention_explanation    TEXT,
    calculated_at            TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at               TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at               TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT analytics_pk PRIMARY KEY (id),
    CONSTRAINT analytics_project_fk FOREIGN KEY (project_id) REFERENCES project (id),
    CONSTRAINT analytics_project_id_uq UNIQUE (project_id)
);

CREATE TABLE analytics_snapshot (
    id                       UUID           NOT NULL,
    project_id               UUID           NOT NULL,
    health_score             NUMERIC(5, 2)  NOT NULL,
    risk_score               NUMERIC(5, 2)  NOT NULL,
    attention_score          NUMERIC(5, 2)  NOT NULL,
    completion_percentage    NUMERIC(5, 2),
    calculated_at            TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at               TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at               TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT analytics_snapshot_pk PRIMARY KEY (id),
    CONSTRAINT analytics_snapshot_project_fk FOREIGN KEY (project_id) REFERENCES project (id)
);

CREATE INDEX idx_analytics_health_score ON analytics (health_score);
CREATE INDEX idx_analytics_risk_score ON analytics (risk_score);
CREATE INDEX idx_analytics_attention_score ON analytics (attention_score);
CREATE INDEX idx_analytics_snapshot_project_id ON analytics_snapshot (project_id);
CREATE INDEX idx_analytics_snapshot_calculated_at ON analytics_snapshot (calculated_at);

COMMENT ON TABLE analytics IS 'Latest computed project analytics (local scoring only).';
COMMENT ON TABLE analytics_snapshot IS 'Historical analytics snapshots for trends.';
