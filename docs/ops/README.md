# Operations (M9 + M10 + M13)

Additive observability and operational tooling. Product domain architecture and M11 PE are frozen.

## Quality gate (M13)

| Doc | Purpose |
|-----|---------|
| [demo-happy-path.md](./demo-happy-path.md) | Connect → sync → triage → Explorer → detail → portfolio → report |
| [known-limitations.md](./known-limitations.md) | Buyer-honest product bounds |
| [release-checklist.md](./release-checklist.md) | Release / deploy checklist (M10) |

## Stack

| Component | Local URL | Purpose |
|-----------|-----------|---------|
| Backend health | http://localhost:8080/actuator/health | Aggregate |
| Liveness | http://localhost:8080/actuator/health/liveness | Process alive (no OP, no DB requirement) |
| Readiness | http://localhost:8080/actuator/health/readiness | Local infra: app + DB + Redis |
| Prometheus metrics | http://localhost:8080/actuator/prometheus | Scrape (`pa_*` custom metrics) |
| Prometheus UI | http://localhost:9090 | With observability compose |
| Alertmanager | http://localhost:9093 | Pilot alerts |
| Grafana | http://localhost:3000 | Dashboards |

## Start observability

```bash
cd docker
docker compose -f docker-compose.yml -f docker-compose.observability.yml up -d
```

## Custom metrics namespace

All business meters use the frozen prefix **`pa_`**.

Examples: `pa_sync_runs_total`, `pa_sync_last_success_age_seconds`, `pa_analytics_recalculate_duration_seconds`, `pa_report_generated_total`.

OpenTelemetry tracing is deferred; Micrometer remains the bridge for future OTEL export.

## Backup policy

- **PostgreSQL** and **report files** are backed up together.
- Reports are **immutable historical artifacts**; restore must restore metadata (DB) and file blobs.

```bash
./scripts/backup-postgres.sh
./scripts/restore-postgres.sh ./data/backups/<timestamp>
```

## Retention (M10)

| Target | Config | Default schedule |
|--------|--------|------------------|
| Report files + `report` rows | `REPORT_RETENTION_DAYS`, `REPORT_PURGE_ENABLED` | `0 30 2 * * *` |
| `analytics_snapshot` rows | `ANALYTICS_SNAPSHOT_RETENTION_DAYS`, `ANALYTICS_SNAPSHOT_PURGE_ENABLED` | `0 0 3 * * *` |

Metric: `pa_retention_purge_total{target=report|analytics_snapshot}`.

## Production hardening docs (M10)

- [security-checklist.md](security-checklist.md)
- [performance-notes.md](performance-notes.md)
- [release-checklist.md](release-checklist.md)
- Load baseline: `./scripts/load-baseline.sh`

## Runbooks

See [runbooks/](runbooks/) (RB-001 … RB-008).
