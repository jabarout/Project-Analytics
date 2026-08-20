# Release Checklist

Use before promoting a build to staging/production.  
Day-of security scan: **`docs/ops/security-checklist.md`** (Day-of deploy section).  
Prod compose: **`docs/ops/n5.1-prod-compose.md`**.

## Pre-release

- [ ] CI green (backend tests + package, frontend tests + production build)
- [ ] Flyway migrations reviewed; backup taken (RB-006 / RB-007)
- [ ] Changelog / Project State updated for the milestone
- [ ] Security checklist day-of items reviewed

## Configuration

- [ ] `SPRING_PROFILES_ACTIVE=prod`
- [ ] `JWT_SECRET` strong, unique, ≥ 32 chars (not the local/dev default)
- [ ] `CREDENTIALS_ENCRYPTION_KEY` ≥ 32 chars and ≠ `JWT_SECRET`
- [ ] `CORS_ALLOWED_ORIGINS` explicit production frontend origin(s)
- [ ] `DB_*`, `REDIS_*`, OpenProject credentials from secrets store
- [ ] `REPORT_STORAGE_PATH` on durable volume
- [ ] Retention: `REPORT_RETENTION_DAYS`, `ANALYTICS_SNAPSHOT_RETENTION_DAYS`
- [ ] `OPENAPI_PUBLIC=false` (prod profile / overlay default)
- [ ] Seed admin password changed before non-local use

## Deploy

- [ ] Database migrated (Flyway on startup or controlled job)
- [ ] Backend readiness: `/actuator/health/readiness`
- [ ] Liveness: `/actuator/health/liveness`
- [ ] Prometheus scrape target healthy (if used)
- [ ] Frontend via nginx `/api/` proxy (Docker) or correct public origin
- [ ] Smoke: login → workspace → Home / Explorer

## Post-deploy

- [ ] Confirm sync schedule and last success age
- [ ] Confirm retention jobs
- [ ] Backup job scheduled (`scripts/backup-postgres.sh`)
- [ ] Alerts routed (if Alertmanager used)

## Rollback

- [ ] Previous application image available
- [ ] DB restore procedure known (RB-006) — prefer forward fix for Flyway
