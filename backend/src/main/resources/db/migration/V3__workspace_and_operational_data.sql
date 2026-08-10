-- =============================================================================
-- Milestone 3 — Synchronization: workspace and operational OpenProject data
-- =============================================================================
-- OpenProject remains the system of record. These tables store a synchronized
-- analytical copy only. Analytics tables arrive in later milestones.
-- =============================================================================

CREATE TABLE workspace (
    id                       UUID         NOT NULL,
    name                     VARCHAR(200) NOT NULL,
    base_url                 VARCHAR(500) NOT NULL,
    version                  VARCHAR(50),
    synchronization_status   VARCHAR(50)  NOT NULL DEFAULT 'NEVER_RUN',
    created_at               TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at               TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT workspace_pk PRIMARY KEY (id),
    CONSTRAINT workspace_base_url_uq UNIQUE (base_url)
);

CREATE TABLE portfolio (
    id                UUID         NOT NULL,
    workspace_id      UUID         NOT NULL,
    name              VARCHAR(200) NOT NULL,
    description       TEXT,
    health_score      NUMERIC(5, 2),
    attention_score   NUMERIC(5, 2),
    created_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT portfolio_pk PRIMARY KEY (id),
    CONSTRAINT portfolio_workspace_fk FOREIGN KEY (workspace_id) REFERENCES workspace (id),
    CONSTRAINT portfolio_workspace_name_uq UNIQUE (workspace_id, name)
);

CREATE TABLE project (
    id                UUID         NOT NULL,
    portfolio_id      UUID         NOT NULL,
    openproject_id    BIGINT       NOT NULL,
    name              VARCHAR(500) NOT NULL,
    description       TEXT,
    status            VARCHAR(100),
    budget            NUMERIC(19, 4),
    progress          NUMERIC(5, 2),
    start_date        DATE,
    end_date          DATE,
    synchronized_at   TIMESTAMP WITH TIME ZONE,
    created_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT project_pk PRIMARY KEY (id),
    CONSTRAINT project_portfolio_fk FOREIGN KEY (portfolio_id) REFERENCES portfolio (id),
    CONSTRAINT project_openproject_id_uq UNIQUE (portfolio_id, openproject_id)
);

CREATE TABLE work_package (
    id                UUID         NOT NULL,
    project_id        UUID         NOT NULL,
    openproject_id    BIGINT       NOT NULL,
    subject           VARCHAR(1000) NOT NULL,
    type              VARCHAR(100),
    status            VARCHAR(100),
    priority          VARCHAR(100),
    assignee          VARCHAR(255),
    estimated_hours   NUMERIC(12, 2),
    spent_hours       NUMERIC(12, 2),
    due_date          DATE,
    synchronized_at   TIMESTAMP WITH TIME ZONE,
    created_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT work_package_pk PRIMARY KEY (id),
    CONSTRAINT work_package_project_fk FOREIGN KEY (project_id) REFERENCES project (id),
    CONSTRAINT work_package_openproject_id_uq UNIQUE (project_id, openproject_id)
);

CREATE TABLE synchronization_history (
    id                            UUID         NOT NULL,
    workspace_id                  UUID         NOT NULL,
    started_at                    TIMESTAMP WITH TIME ZONE NOT NULL,
    finished_at                   TIMESTAMP WITH TIME ZONE,
    duration_ms                   BIGINT,
    status                        VARCHAR(50)  NOT NULL,
    sync_type                     VARCHAR(50)  NOT NULL,
    synchronized_projects         INTEGER      NOT NULL DEFAULT 0,
    synchronized_work_packages    INTEGER      NOT NULL DEFAULT 0,
    error_message                 TEXT,
    created_at                    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT synchronization_history_pk PRIMARY KEY (id),
    CONSTRAINT synchronization_history_workspace_fk FOREIGN KEY (workspace_id) REFERENCES workspace (id)
);

CREATE INDEX idx_portfolio_workspace_id ON portfolio (workspace_id);
CREATE INDEX idx_project_portfolio_id ON project (portfolio_id);
CREATE INDEX idx_project_status ON project (status);
CREATE INDEX idx_work_package_project_id ON work_package (project_id);
CREATE INDEX idx_sync_history_workspace_id ON synchronization_history (workspace_id);
CREATE INDEX idx_sync_history_status ON synchronization_history (status);

COMMENT ON TABLE workspace IS 'Connected OpenProject instance (Workspace).';
COMMENT ON TABLE portfolio IS 'Logical collection of projects within a workspace.';
COMMENT ON TABLE project IS 'Synchronized OpenProject project (analytical copy).';
COMMENT ON TABLE work_package IS 'Synchronized OpenProject work package (analytical copy).';
COMMENT ON TABLE synchronization_history IS 'Audit log of synchronization executions.';
