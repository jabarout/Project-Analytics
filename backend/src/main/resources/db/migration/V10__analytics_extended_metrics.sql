-- =============================================================================
-- M12 — Extended analytics metrics (progress gap, overdue aging, score factors)
-- Additive only; does not change Health/Risk/Attention score formulas.
-- One ADD COLUMN per statement for H2 + PostgreSQL compatibility.
-- Deadline exposure reuses existing schedule_variance (no parallel days_to_deadline).
-- =============================================================================

ALTER TABLE analytics ADD COLUMN expected_progress NUMERIC(5, 2);
ALTER TABLE analytics ADD COLUMN progress_gap NUMERIC(5, 2);
ALTER TABLE analytics ADD COLUMN overdue_ratio NUMERIC(5, 4);
ALTER TABLE analytics ADD COLUMN avg_overdue_age_days NUMERIC(8, 2);
ALTER TABLE analytics ADD COLUMN max_overdue_age_days INTEGER;
ALTER TABLE analytics ADD COLUMN health_factors_json TEXT;
ALTER TABLE analytics ADD COLUMN risk_factors_json TEXT;
ALTER TABLE analytics ADD COLUMN attention_factors_json TEXT;

ALTER TABLE analytics_snapshot ADD COLUMN expected_progress NUMERIC(5, 2);
ALTER TABLE analytics_snapshot ADD COLUMN progress_gap NUMERIC(5, 2);
ALTER TABLE analytics_snapshot ADD COLUMN overdue_ratio NUMERIC(5, 4);

COMMENT ON COLUMN analytics.expected_progress IS 'Schedule-based expected progress % (null when dates incomplete). From ProgressMetrics.';
COMMENT ON COLUMN analytics.progress_gap IS 'actual progress - expected progress (negative = behind). From ProgressMetrics.';
COMMENT ON COLUMN analytics.overdue_ratio IS 'overdue WP count / total WP count (0-1). From ProgressMetrics.';
COMMENT ON COLUMN analytics.avg_overdue_age_days IS 'Average days past due among overdue open WPs.';
COMMENT ON COLUMN analytics.max_overdue_age_days IS 'Max days past due among overdue open WPs.';
COMMENT ON COLUMN analytics.health_factors_json IS 'Serialized score factors for health explainability.';
COMMENT ON COLUMN analytics.risk_factors_json IS 'Serialized score factors for risk explainability.';
COMMENT ON COLUMN analytics.attention_factors_json IS 'Serialized score factors for attention explainability.';
