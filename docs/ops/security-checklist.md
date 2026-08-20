# Production Security Checklist

Operator checklist for a production (or customer) deploy.  
Related: `n5.1-prod-compose.md`, `n5.2-env-template.md`, `n5.3-backup-restore.md`, `.env.example` (PRODUCTION section).

---

## Day-of deploy (scan this first)

- [ ] `SPRING_PROFILES_ACTIVE=prod` (forced by `docker-compose.prod.yml` overlay)
- [ ] `JWT_SECRET` ≥ 32 chars, **not** the local/dev default — from secrets/env, not git
- [ ] `CREDENTIALS_ENCRYPTION_KEY` ≥ 32 chars and **≠** `JWT_SECRET`
- [ ] `CORS_ALLOWED_ORIGINS` = explicit frontend origin(s), **no `*`** (prefer HTTPS)
- [ ] Strong `DB_PASSWORD` (and Redis password if Redis auth is enabled)
- [ ] `OPENAPI_PUBLIC=false` / OpenAPI disabled (prod profile + overlay)
- [ ] `OP_ALLOW_ENV_API_KEY_FALLBACK=false` (prod profile + overlay)
- [ ] Seed admin password changed or account disabled (see below)
- [ ] SMTP configured if signup confirmation / password reset must send mail
- [ ] Auth rate limits on (`AUTH_RATE_LIMIT_*`); edge/WAF rate limit in front
- [ ] TLS terminated at reverse proxy / load balancer
- [ ] Actuator / Prometheus not exposed on the public internet
- [ ] Backup taken or scheduled (`./scripts/backup-postgres.sh` / RB-006)
- [ ] Smoke: health UP → login → sync/workspace → Home

---

## Authentication & secrets

| Check | Status / action |
|-------|-----------------|
| `JWT_SECRET` from secrets/env (not repo) | Required — enforced at prod startup |
| Secret length ≥ 32 | Enforced at prod startup |
| Dev default JWT secret never used in prod | Enforced at prod startup |
| `CREDENTIALS_ENCRYPTION_KEY` ≥ 32 and ≠ `JWT_SECRET` | Enforced at prod startup |
| DB password (and Redis password if used) via env | Required |
| OpenProject credentials per workspace (API key connect / OAuth); env API key not used as prod fallback | Prod: `allow-env-api-key-fallback=false` |
| `.env` / `prod.env` not committed | Policy |
| Change/disable Flyway seed admin before real deploy | Ops (below) |

## Network & CORS

| Check | Status / action |
|-------|-----------------|
| `CORS_ALLOWED_ORIGINS` explicit production frontend origin(s) | Enforced at prod startup (no `*`) |
| TLS at reverse proxy / load balancer | Ops |
| Actuator (`/actuator/prometheus`) not public | Network ACL / internal scrape only |
| Swagger / OpenAPI off in prod | `OPENAPI_PUBLIC=false` + springdoc disabled in `prod` profile |

## Application

| Check | Status / action |
|-------|-----------------|
| JWT on `/api/**` except public auth routes | Code |
| Security headers (`X-Content-Type-Options`, `X-Frame-Options`, `Referrer-Policy`, `Cache-Control: no-store`) | Code (`SecurityHeadersFilter`) |
| Passwords stored with BCrypt | Code |
| OpenProject is sync-only; dashboards use local analytics | Architecture freeze |
| Analytics access is app-owned (grants / memberships) | M15 |

## Data & backup

| Check | Status / action |
|-------|-----------------|
| Report + snapshot retention set | `REPORT_RETENTION_DAYS`, `ANALYTICS_SNAPSHOT_RETENTION_DAYS` |
| Backups include Postgres + report files | `scripts/backup-postgres.sh` — see N5.3 / RB-006 |
| Flyway migrations reviewed before prod apply | RB-007 |

## Seed platform admin (deploy hygiene)

Flyway seeds a local platform admin (`admin` / demo password documented for local use).  
**Before any non-local deploy:**

1. Change that password (or disable the account).
2. Do **not** rely on first signup becoming platform admin.
3. Keep Platform Admin separate from Workspace Admin; create ops accounts out-of-band.

## Auth rate limiting

In-app limits on login / register / forgot / reset / confirm / resend (`AUTH_RATE_LIMIT_*`).  
**Still put an edge/WAF rate limit in front of production.**

JWT is stored in browser `localStorage` for now (XSS risk accepted short-term). Password change bumps `credentials_version` and invalidates old JWTs.

## Email (confirmation & password reset)

New accounts must confirm email before login when mail confirmation is active.  
Uses `PASSWORD_RESET_MAIL_ENABLED` + `spring.mail.*` (same path as password reset).  
TTL: `EMAIL_CONFIRMATION_TTL_MINUTES` (default 24h).

For production user flows that need email: enable SMTP **before** go-live.

## Legal / contact (footer)

Public pages shipped: **Privacy**, **Terms of use**, **Contact**  
Contact mailbox: `projectanalytics.contact@gmail.com`  

Still review/adapt legal copy for your organisation and jurisdiction before a public customer deploy.

## Residual risks (accepted / deferred)

- Prefer edge rate limiting in addition to in-app limits.
- httpOnly cookie JWT migration = optional later change.
- Prefer HTTPS-only CORS origins in real deployments (localhost in prod only warns).
