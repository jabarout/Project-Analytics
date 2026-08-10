# RB-001 — Backend down / readiness failing

## Symptoms

- Alert: `BackendDown` or `HighHttp5xxRate`
- `/actuator/health/readiness` not 200
- UI API errors

## Checks

1. Liveness: `curl -sS http://localhost:8080/actuator/health/liveness`
2. Readiness: `curl -sS http://localhost:8080/actuator/health/readiness`
3. Logs: search by `requestId` (response header `X-Request-Id`)
4. Postgres: `docker exec pa-postgres pg_isready`
5. Redis: `docker exec pa-redis redis-cli ping`
6. Port 8080: `ss -ltnp 'sport = :8080'`

## Remediation

1. If readiness fails and Postgres is down → start Postgres, then backend.
2. If Redis down and `REDIS_ENABLED=true` → restore Redis or temporarily disable only with product approval.
3. Restart backend with env loaded: `./scripts/run-backend.sh`
4. Confirm readiness UP and Grafana overview shows traffic.

## Notes

- Liveness does **not** depend on OpenProject.
- Readiness depends only on **local** infrastructure (DB, Redis when enabled).
