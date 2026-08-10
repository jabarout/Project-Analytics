# Performance Notes (M10)

Operational notes only. Analytics scoring algorithms and product surfaces remain frozen.

## Known cost: full-workspace rescore

After synchronization, analytics recalculation scores **every project** in the workspace:

- Loads work packages per project from local PostgreSQL.
- Computes Health / Risk / Attention via the single analytics engine.
- Writes latest `analytics` row and appends `analytics_snapshot`.

**Complexity (order of magnitude):** `O(projects × work_packages_per_project)` CPU + DB reads.

**Why not incremental in M10:** Correctness and architecture freeze prefer full local rescore over partial invalidation. Incremental analytics remains a future optimization if metrics show sustained cost.

**Mitigations in M10:**

- Composite indexes (Flyway `V8__performance_indexes.sql`) for:
  - `analytics_snapshot (project_id, calculated_at DESC)`
  - `work_package (project_id, due_date)`
  - `synchronization_history (workspace_id, started_at DESC)`
  - report age purge index
- Metric: `pa_analytics_recalculate_duration_seconds`, `pa_analytics_projects_scored_total`
- Retention purge limits snapshot table growth

## Hot paths

| Path | Notes |
|------|--------|
| Sync + recalculate | Dominant write path after OP import |
| Workspace / portfolio dashboards | Read latest `analytics` rows; no rescore |
| Project trend | Last N snapshots by `project_id` + `calculated_at` |
| Report generation | CPU for PDF/Excel; file I/O to `REPORT_STORAGE_PATH` |

## Load baseline

```bash
./scripts/load-baseline.sh
# optional overrides:
# BASE_URL=http://localhost:8080 CONCURRENCY=10 REQUESTS=100 ./scripts/load-baseline.sh
```

Record output under `data/load-baseline/`. Use readiness + authenticated list endpoints as the critical path sample.

## Tuning levers (config only)

| Variable | Effect |
|----------|--------|
| `SYNC_INTERVAL_MS` | How often scheduled sync/rescore runs |
| `REPORT_RETENTION_DAYS` / purge | Cap report disk + metadata growth |
| `ANALYTICS_SNAPSHOT_RETENTION_DAYS` | Cap snapshot history growth |
| DB connection pool (Spring defaults) | Scale with concurrent API users |
