# RB-002 — Synchronization failure / stale sync

## Symptoms

- Alert: `SyncFailuresElevated` or `NoSuccessfulSynchronization`
- UI: “OpenProject unavailable” / sync FAILED
- Metric: `pa_sync_last_success_age_seconds` large

## Checks

1. Confirm backend readiness is UP (RB-001).
2. Logs: `workspaceId` MDC + “Synchronization FAILED”.
3. OpenProject credentials: `OPENPROJECT_API_KEY` set in process env (not only `.env` file).
4. Workspace base URL correct (404 host vs 401/400 API).
5. Concurrent run: `SYNC_003` means already running.
6. Prometheus: `pa_sync_runs_total{status="failure"}`.

## Remediation

1. Fix credentials/URL; restart backend with `./scripts/run-backend.sh`.
2. Disable or remove dead workspaces (bad URLs).
3. Manual sync from UI or API after fix.
4. If age alert only: confirm scheduled sync enabled (`SYNC_ENABLED`, interval).

## Notes

- OpenProject is used **only** during synchronization.
- Dashboards never call OpenProject live.
- Alert window default: 24h (`pa_sync_last_success_age_seconds > 86400`); align with `SYNC_SUCCESS_STALE_SECONDS`.
