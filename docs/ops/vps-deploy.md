# VPS deploy runbook (Docker Compose + Caddy)

You run these commands **on the remote Linux VPS** over your own SSH.  
This workstation is for development/admin docs only — no SSH from the agent.

**Stack files**

| File | Role |
|------|------|
| `docker/docker-compose.yml` | Base: Postgres, Redis, backend, frontend |
| `docker/docker-compose.prod.yml` | Prod profile + required secrets |
| `docker/docker-compose.vps.yml` | Caddy :80/:443; strip public ports from other services |
| `docker/Caddyfile` | TLS + reverse_proxy → `frontend:80` |
| `docs/ops/prod.env.vps.example` | Env template (`YOUR_DOMAIN` + secrets) |

**Internal routing (unchanged)**

- Browser → Caddy → **frontend:80**
- Frontend nginx `/api/` → **backend:8080** (see `frontend/docker/nginx.conf`)
- Backend → **postgres:5432**, **redis:6379**

---

## Phase A — Local validation (already done in CI/dev machine)

Validates merged Compose only. **Does not** bind a real certificate.

```bash
# From repo root — dummy DOMAIN for config parse only
DOMAIN=example.invalid JWT_SECRET=aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa \
CREDENTIALS_ENCRYPTION_KEY=bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb \
CORS_ALLOWED_ORIGINS=https://example.invalid \
DB_NAME=projectanalytics DB_USERNAME=projectanalytics DB_PASSWORD=dummy-password-ok \
docker compose -p project-analytics-vps-check \
  -f docker/docker-compose.yml \
  -f docker/docker-compose.prod.yml \
  -f docker/docker-compose.vps.yml \
  --env-file docs/ops/prod.env.vps.example \
  config >/dev/null
```

(Use a filled temp env file if `:?` required vars block parse — see stop report.)

---

## Phase B — Prepare the VPS

1. Install Docker Engine + Compose plugin.
2. Open firewall **80** and **443** (and SSH). Do **not** expose 5432/6379/8080.
3. Clone or copy this repository onto the VPS.
4. Working directory = repo root (contains `docker/`, `docs/`, `scripts/`).

---

## Phase C — Configure `prod.env`

```bash
cp docs/ops/prod.env.vps.example prod.env
# Edit prod.env:
#   - Replace YOUR_DOMAIN with your real hostname (when known)
#   - Set JWT_SECRET, CREDENTIALS_ENCRYPTION_KEY, DB_PASSWORD (openssl rand -base64 48)
#   - Set MAIL_PASSWORD (Gmail App Password, quoted if spaces)
# Never commit prod.env
```

Until DNS exists you may keep `DOMAIN=YOUR_DOMAIN` only for file editing; **Caddy cannot get a real Let’s Encrypt cert until DNS A/AAAA points at the VPS.**

---

## Phase D — Start the stack (on the VPS)

```bash
docker compose -p project-analytics-vps \
  -f docker/docker-compose.yml \
  -f docker/docker-compose.prod.yml \
  -f docker/docker-compose.vps.yml \
  --env-file prod.env \
  up -d --build
```

Check containers:

```bash
docker compose -p project-analytics-vps \
  -f docker/docker-compose.yml \
  -f docker/docker-compose.prod.yml \
  -f docker/docker-compose.vps.yml \
  --env-file prod.env \
  ps
```

Expect: `pa-vps-postgres`, `pa-vps-redis`, `pa-vps-backend`, `pa-vps-frontend`, `pa-vps-caddy`.

---

## Phase E — DNS, then HTTPS

1. Create DNS **A** (and **AAAA** if IPv6) for your hostname → VPS public IP.
2. Wait for propagation.
3. Caddy will obtain/renew Let’s Encrypt certificates automatically for `DOMAIN`.
4. If TLS fails, check Caddy logs: `docker logs pa-vps-caddy`

---

## Phase F — Smoke checks (production)

**Browser-facing (via Caddy → frontend nginx)**

| Check | URL / action |
|-------|----------------|
| Frontend liveness | `https://YOUR_DOMAIN/health` → plain text `OK` |
| App shell | `https://YOUR_DOMAIN/` → login page |
| API via same origin | Login works (browser calls `/api/v1/...`; nginx proxies to backend) |

**Internal backend health** (not required in the browser; use on the VPS):

```bash
docker exec pa-vps-backend curl -fsS http://localhost:8080/actuator/health
```

Expect JSON with `"status":"UP"`.

**After login**

- [ ] Connections → sync OpenProject  
- [ ] Home loads  
- [ ] Signup confirmation / password reset mail use `https://YOUR_DOMAIN` links  
- [ ] Change seed `admin` password (security checklist)

---

## Phase G — Backup

On the VPS (with DB reachable on the compose network or via `docker exec`):

```bash
# Prefer exec into postgres or run scripts with DB_HOST=postgres from a one-off container;
# simplest host approach if you temporarily publish nothing: docker exec backup
docker exec pa-vps-postgres pg_dump -U "$DB_USERNAME" -d "$DB_NAME" --format=custom > backup.dump
```

Or adapt `./scripts/backup-postgres.sh` with `DB_HOST`/`DB_PORT` matching how you reach Postgres from the host. See `docs/ops/n5.3-backup-restore.md` and RB-006.

Schedule a daily backup cron once the site is live.

---

## Troubleshooting

| Symptom | Likely cause |
|---------|----------------|
| TLS / ACME errors | DNS not pointing at VPS yet |
| Login CORS errors | `CORS_ALLOWED_ORIGINS` ≠ `https://YOUR_DOMAIN` |
| API 502 from UI | Backend unhealthy — check `docker logs pa-vps-backend` |
| Mail links wrong host | `PASSWORD_RESET_FRONTEND_BASE_URL` still placeholder |
| Port already allocated | Something else bound to 80/443 on the VPS |

---

## Tear down

```bash
docker compose -p project-analytics-vps \
  -f docker/docker-compose.yml \
  -f docker/docker-compose.prod.yml \
  -f docker/docker-compose.vps.yml \
  --env-file prod.env \
  down
```
