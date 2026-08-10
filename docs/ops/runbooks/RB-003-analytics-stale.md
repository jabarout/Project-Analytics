# RB-003 — Analytics appear stale

## Symptoms

- Dashboards empty or old scores after sync
- `pa_analytics_projects_scored_total` not increasing after sync

## Checks

1. Sync succeeded recently (RB-002).
2. Logs: `Recalculated analytics for N project(s)`.
3. Manual: `POST /api/v1/analytics/workspaces/{id}/recalculate` (authenticated).

## Remediation

1. Re-run workspace recalculate.
2. If post-sync hook failed, fix exception in logs and re-sync or recalculate.
3. Confirm local DB has `analytics` rows for projects.

## Notes

- Scoring is local only; OpenProject is not consulted for scores.
- Portfolio membership does not change scoring — only inclusion in portfolio scopes.
