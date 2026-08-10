-- M12 cleanup: days_to_deadline was briefly added in an earlier V10 draft, then
-- removed in favor of existing schedule_variance (no parallel deadline metric).
-- Safe on fresh installs where the column never existed.

ALTER TABLE analytics DROP COLUMN IF EXISTS days_to_deadline;
ALTER TABLE analytics_snapshot DROP COLUMN IF EXISTS days_to_deadline;
