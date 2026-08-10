# Production Security Checklist (M10)

Lightweight threat checklist for operators. Product architecture and analytics model are frozen.

## Authentication & secrets

| Check | Status / action |
|-------|-----------------|
| `JWT_SECRET` set from secrets manager / env (not repo) | Required |
| Secret length ≥ 32 characters | Enforced at prod startup |
| Dev default JWT secret never used in prod | Enforced at prod startup |
| DB password, Redis password, OpenProject API key via env | Required |
| `.env` / secrets not committed | Policy |

## Network & CORS

| Check | Status / action |
|-------|-----------------|
| `CORS_ALLOWED_ORIGINS` explicit HTTPS origins | Enforced at prod startup (no `*`) |
| TLS terminated at reverse proxy / load balancer | Ops |
| Actuator (`/actuator/prometheus`) not public internet | Network ACL / internal scrape only |
| Swagger / OpenAPI disabled or non-public in prod | `OPENAPI_PUBLIC=false` + springdoc disabled in `prod` profile |

## Application

| Check | Status / action |
|-------|-----------------|
| JWT required on all `/api/**` except login | Code |
| Security headers (`X-Content-Type-Options`, `X-Frame-Options`, `Referrer-Policy`, `Cache-Control: no-store`) | Code (`SecurityHeadersFilter`) |
| Passwords stored with BCrypt | Code |
| OpenProject is sync-only; no live OP calls from dashboards | Architecture freeze |
| Analytics access is app-owned (not OP role inference) | Product freeze; grant UI in M11 |

## Data

| Check | Status / action |
|-------|-----------------|
| Report + snapshot retention configured | `REPORT_RETENTION_DAYS`, `ANALYTICS_SNAPSHOT_RETENTION_DAYS` |
| Backups include Postgres + report files | `scripts/backup-postgres.sh` |
| Flyway migrations reviewed before prod apply | RB-007 |

## Residual risks (accepted / deferred)

- Rate limiting at edge (proxy) recommended; not in-app for M10.
- OAuth for OpenProject replaces env API key later (credential seam already frozen).
- Workspace analytics access grants implemented in M11.
