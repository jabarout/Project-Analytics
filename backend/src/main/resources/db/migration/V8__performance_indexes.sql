-- =============================================================================
-- M10 — Production Hardening: performance indexes (no domain model change)
-- =============================================================================
-- Supports common analytical hot paths: trend queries, workspace project lists,
-- work-package loads by project, sync history by workspace, overdue-related filters.
-- =============================================================================

-- Trend / history lookup per project (AnalyticsQueryService top-N by calculated_at)
CREATE INDEX IF NOT EXISTS idx_analytics_snapshot_project_calculated
    ON analytics_snapshot (project_id, calculated_at DESC);

-- Work packages: load by project is the scoring hot path
CREATE INDEX IF NOT EXISTS idx_work_package_project_due_date
    ON work_package (project_id, due_date);

-- Sync ops / freshness queries
CREATE INDEX IF NOT EXISTS idx_sync_history_workspace_started
    ON synchronization_history (workspace_id, started_at DESC);

-- Report retention purge and history listing
CREATE INDEX IF NOT EXISTS idx_report_generated_at_asc
    ON report (generated_at);
