# SMTP setup STOP report

**Date:** 2026-08-20  
**Scope:** Gmail SMTP for `projectanalytics.contact@gmail.com`  
**Next:** Web deployment (when you ask)

## 1. Files changed

| File | Change |
|------|--------|
| `backend/src/main/resources/application.yml` | `spring.mail.*` from `MAIL_*` env |
| `.env.example` | Gmail SMTP template (no secrets) |
| `docker/docker-compose.yml` | Pass mail env to backend |
| `docker/docker-compose.prod.yml` | Pass mail env to backend |
| `docs/ops/smtp-setup.md` | Operator guide |
| `docs/ops/DEPLOY.md` | Link to SMTP step |
| `docs/ops/smtp-setup-report.md` | This report |
| `.env` (gitignored) | Local mail enabled + App Password (**not committed**) |

## 2. Verification

| Check | Result |
|-------|--------|
| Backend starts with mail env | Pass |
| `POST /auth/register` for contact Gmail | **200** — account created, awaiting confirmation |
| No SMTP/auth ERROR in backend log after register | Pass (send did not throw) |
| `POST /auth/resend-confirmation` | **200** |

**Inbox confirmed by operator (2026-08-20):** confirmation email arrived successfully.

## 3. Security note

The App Password was shared in chat once. After mail works, **rotate** it in Google Account → App passwords and update `.env` only.

## 4. Remaining before public go-live

- Confirm inbox delivery  
- **Web deployment** (TLS, public host, prod secrets, CORS, seed admin password)

---

## STOP

SMTP wiring + live send path verified at API level. Await inbox confirmation / next: web deploy.
