# RB-008 — Retention purge issues

**Related:** report file/metadata purge; analytics snapshot purge (M10)

## Symptoms

- Disk under `REPORT_STORAGE_PATH` grows without bound
- `analytics_snapshot` table large; slow trend queries
- Logs show purge failures deleting files
- Metric `pa_retention_purge_total` not increasing when expected

## Checks

1. Config:
   - `REPORT_PURGE_ENABLED` / `projectanalytics.reporting.purge-enabled`
   - `REPORT_RETENTION_DAYS`
   - `ANALYTICS_SNAPSHOT_PURGE_ENABLED`
   - `ANALYTICS_SNAPSHOT_RETENTION_DAYS`
2. Application logs for `Report retention purge` / `Analytics snapshot retention purge`
3. Filesystem permissions on report storage path
4. Database connectivity / Flyway applied

## Mitigation

1. Confirm purge not disabled for the environment.
2. Manually trigger by restarting after reducing retention days **only** in non-prod first.
3. Free disk: delete orphaned files only if no matching `report` row (prefer backup first).
4. Snapshot purge is age-based SQL delete; latest `analytics` rows are never purged.

## Notes

- Reports are immutable while retained; purge is policy, not soft-delete product feature.
- Do not change scoring or product UX to address retention.
