-- =============================================================================
-- Portfolio membership redesign (many-to-many)
-- =============================================================================
-- Workspace owns synchronized projects.
-- Portfolios are optional analytical collections; a project may belong to zero
-- or more portfolios via portfolio_project.
-- =============================================================================

-- 1) Project ownership moves to workspace
ALTER TABLE project ADD COLUMN workspace_id UUID;

UPDATE project
SET workspace_id = (
    SELECT p.workspace_id
    FROM portfolio p
    WHERE p.id = project.portfolio_id
);

ALTER TABLE project ALTER COLUMN workspace_id SET NOT NULL;

ALTER TABLE project
    ADD CONSTRAINT project_workspace_fk
        FOREIGN KEY (workspace_id) REFERENCES workspace (id);

-- 2) Membership join table
CREATE TABLE portfolio_project (
    portfolio_id   UUID NOT NULL,
    project_id     UUID NOT NULL,
    created_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT portfolio_project_pk PRIMARY KEY (portfolio_id, project_id),
    CONSTRAINT portfolio_project_portfolio_fk FOREIGN KEY (portfolio_id) REFERENCES portfolio (id) ON DELETE CASCADE,
    CONSTRAINT portfolio_project_project_fk FOREIGN KEY (project_id) REFERENCES project (id) ON DELETE CASCADE
);

CREATE INDEX idx_portfolio_project_project_id ON portfolio_project (project_id);

-- 3) Preserve existing exclusive memberships as M2M rows
INSERT INTO portfolio_project (portfolio_id, project_id, created_at)
SELECT portfolio_id, id, CURRENT_TIMESTAMP
FROM project
WHERE portfolio_id IS NOT NULL;

-- 4) Drop exclusive ownership on project
ALTER TABLE project DROP CONSTRAINT project_openproject_id_uq;
ALTER TABLE project DROP CONSTRAINT project_portfolio_fk;
DROP INDEX IF EXISTS idx_project_portfolio_id;
ALTER TABLE project DROP COLUMN portfolio_id;

ALTER TABLE project
    ADD CONSTRAINT project_workspace_openproject_id_uq UNIQUE (workspace_id, openproject_id);

CREATE INDEX idx_project_workspace_id ON project (workspace_id);

COMMENT ON TABLE portfolio_project IS 'Many-to-many: optional portfolio membership for analytical views (not project ownership).';
COMMENT ON COLUMN project.workspace_id IS 'Owning workspace (all synchronized projects).';
