# Release Checklist (M10)

Use before promoting a build to staging/production.

## Pre-release

- [ ] CI green (backend tests + package, frontend tests + production build)
- [ ] Flyway migrations reviewed (`V8`+); backup taken (RB-006 / RB-007)
- [ ] Changelog / Project State updated for the milestone
- [ ] No product UX changes mixed into hardening release (M11 separate)

## Configuration

- [ ] `SPRING_PROFILES_ACTIVE=prod`
- [ ] `JWT_SECRET` strong, unique, ≥ 32 chars
- [ ] `CORS_ALLOWED_ORIGINS` explicit production frontend origin(s)
- [ ] `DB_*`, `REDIS_*`, `OPENPROJECT_*` from secrets store
- [ ] `REPORT_STORAGE_PATH` on durable volume
- [ ] Retention: `REPORT_RETENTION_DAYS`, `ANALYTICS_SNAPSHOT_RETENTION_DAYS`
- [ ] `OPENAPI_PUBLIC=false` (prod profile default)

## Deploy

- [ ] Database migrated (Flyway on startup or controlled job)
- [ ] Backend readiness: `/actuator/health/readiness`
- [ ] Liveness: `/actuator/health/liveness`
- [ ] Prometheus scrape target healthy
- [ ] Frontend built with production `API_BASE_URL` / reverse-proxy path
- [ ] Smoke: login → list workspaces → open dashboard / report generate if used

## Post-deploy

- [ ] Confirm sync schedule and last success age metric
- [ ] Confirm retention jobs schedule (cron defaults 02:30 / 03:00 UTC-ish per app TZ)
- [ ] Backup job scheduled
- [ ] Alerts routed (Alertmanager)

## Rollback

- [ ] Previous application image available
- [ ] DB restore procedure known (RB-006) — prefer forward fix for Flyway
