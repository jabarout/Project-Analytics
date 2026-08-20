# Deploy & customer handoff

**Start here** for production-oriented deploy and demo.  
Local day-to-day setup (dev stack) remains in `README-XTENSUS.md`.

N5 / M17 package: compose → env → backup → security → demo → this handoff index.

---

## 1. Prerequisites

- Docker (Compose v2+)
- For local non-Docker backend: Java 21 + Maven; Node 20/22 for `ng serve`
- A private env file with secrets (**never commit** `prod.env` / `.env`)

---

## 2. Configure environment

```bash
cp .env.example prod.env   # or .env for local
# Edit prod.env — fill PRODUCTION section secrets
```

| Doc | Purpose |
|-----|---------|
| [`.env.example`](../../.env.example) | Local + **PRODUCTION** key template |
| [n5.2-env-template.md](./n5.2-env-template.md) | Required keys vs `ProductionSecurityValidator` |

**Must set for prod:** `JWT_SECRET`, `CREDENTIALS_ENCRYPTION_KEY`, `CORS_ALLOWED_ORIGINS`, `DB_*` passwords.

---

## 3. Start on a VPS (public HTTPS)

**Primary path:** remote Linux VPS with Caddy (ports **80/443** only).  
Step-by-step: **[vps-deploy.md](./vps-deploy.md)**  
Env template: [prod.env.vps.example](./prod.env.vps.example)

```bash
# On the VPS, after filling prod.env (DOMAIN=your.hostname):
docker compose -p project-analytics-vps \
  -f docker/docker-compose.yml \
  -f docker/docker-compose.prod.yml \
  -f docker/docker-compose.vps.yml \
  --env-file prod.env \
  up -d --build
```

Browser smoke (after DNS): `https://YOUR_DOMAIN/health` → `OK`  
Internal backend: `docker exec pa-vps-backend curl -fsS http://localhost:8080/actuator/health`

### Local/prod smoke without Caddy (optional)

Isolated host ports for laptop testing: Postgres **5433**, Redis **6380**, Backend **8081**, Frontend **8089**.  
See [n5.1-prod-compose.md](./n5.1-prod-compose.md).

```bash
curl -fsS http://localhost:8081/actuator/health
curl -fsS http://localhost:8089/health
```

---

## 4. SMTP (signup confirmation & password reset)

Before public signup/reset flows: configure Gmail (or other SMTP).  
Guide: [smtp-setup.md](./smtp-setup.md)

## 5. Seed admin hygiene

Before any non-local use: change or disable the Flyway seed `admin` password.  
See **Day-of deploy** in [security-checklist.md](./security-checklist.md).

---

## 6. Backup & restore

```bash
./scripts/backup-postgres.sh
./scripts/restore-postgres.sh ./data/backups/<timestamp>   # destructive — stop backend first
```

Details: [n5.3-backup-restore.md](./n5.3-backup-restore.md), [runbooks/RB-006-restore-from-backup.md](./runbooks/RB-006-restore-from-backup.md)

---

## 7. Security checklist

Complete [security-checklist.md](./security-checklist.md) (Day-of deploy section) before go-live.  
Also: [release-checklist.md](./release-checklist.md)

---

## 8. Demo walkthrough

Buyer path (&lt; ~30 min): [demo-happy-path.md](./demo-happy-path.md)  
Honest limits: [known-limitations.md](./known-limitations.md)

---

## 9. Package a handoff zip

From repo root (excludes `.env`, `node_modules`, builds, `.git`):

```bash
./scripts/make-handoff-zip.sh /tmp/Project-Analytics-Handoff.zip
```

Recipients: open **this file** (`docs/ops/DEPLOY.md`) first, then configure `prod.env` from `.env.example`.

---

## Legal / contact

Footer pages: Privacy, Terms of use, Contact (`projectanalytics.contact@gmail.com`).  
Review legal copy for your organisation before a public deploy.
