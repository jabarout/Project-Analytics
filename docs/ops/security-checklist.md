# Production Security Checklist (M10)

Lightweight threat checklist for operators. Product architecture and analytics model are frozen.

## Authentication & secrets

| Check | Status / action |
|-------|-----------------|
| `JWT_SECRET` set from secrets manager / env (not repo) | Required |
| Secret length ≥ 32 characters | Enforced at prod startup |
| Dev default JWT secret never used in prod | Enforced at prod startup |
| `CREDENTIALS_ENCRYPTION_KEY` set in prod (≥32 chars, **≠** JWT_SECRET) | Enforced at prod startup (Phase 3) |
| DB password, Redis password via env | Required |
| OpenProject credentials per workspace (API key connect / OAuth); env key not used in prod | Prod: `allow-env-api-key-fallback=false` |
| `.env` / secrets not committed | Policy |
| Change/disable Flyway seed admin password before real deploy | Ops (see below) |

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
| Analytics access is app-owned (not OP role inference) | Product freeze; Phase 6 / M15 grant API+UI |

## Data

| Check | Status / action |
|-------|-----------------|
| Report + snapshot retention configured | `REPORT_RETENTION_DAYS`, `ANALYTICS_SNAPSHOT_RETENTION_DAYS` |
| Backups include Postgres + report files | `scripts/backup-postgres.sh` |
| Flyway migrations reviewed before prod apply | RB-007 |

## Seed platform admin (deploy hygiene)

The Flyway seed creates a local platform admin (`admin` / documented demo password in V2 comments and README).  
**Before any non-local deploy:**

1. Change that password in the database (or disable the account).
2. Do **not** rely on first signup becoming platform admin (Hybrid rule forbids this).
3. Prefer creating ops accounts out-of-band; keep Platform Admin separate from Workspace Admin.

## Auth rate limiting (Phase 5)

In-app limits on `POST /auth/login|register|forgot-password|reset-password|confirm-email|resend-confirmation` (IP + route, fixed window).  
Configure via `AUTH_RATE_LIMIT_*`. **Still put an edge/WAF rate limit in front of production.**

JWT remains in browser `localStorage` for now (XSS risk accepted short-term). Password change bumps `credentials_version` and invalidates old JWTs.

## Email confirmation on signup

New accounts must confirm email before login (`AUTH_008` until verified).  
Uses the same mail settings as password reset (`PASSWORD_RESET_MAIL_ENABLED` + `spring.mail.*`).  
TTL: `EMAIL_CONFIRMATION_TTL_MINUTES` (default 24h). Existing users were backfilled as verified (Flyway V17).

## Residual risks (accepted / deferred)

- Prefer edge rate limiting in addition to in-app limits.
- httpOnly cookie JWT migration = optional later change (not Phase 5).
- OAuth for OpenProject = Phase 7 (authorization code + PKCE; callback public; state one-time; same eligibility as API-key).
- Workspace analytics grant API+UI = Phase 6 / M15 (done: grant/revoke by email; no promote-to-admin).
- Membership isolation on analytics APIs = Phase 1 (done).
- Password recovery = Phase 4 (done; **enable SMTP before deploy**: `PASSWORD_RESET_MAIL_ENABLED=true` and `spring.mail.host` / port / username / password / from-address).
- Signup email confirmation = done (same SMTP; must confirm before login).
