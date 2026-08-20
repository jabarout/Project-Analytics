# VPS deploy preparation — STOP report

**Date:** 2026-08-20  
**Scope:** Prepare VPS Docker + Caddy package only. **No SSH, no live VPS up, no Let’s Encrypt.**

## 1. Files changed / added

| File | Role |
|------|------|
| `docker/docker-compose.vps.yml` | Caddy :80/:443; strip host ports from DB/Redis/backend/frontend |
| `docker/Caddyfile` | `{$DOMAIN}` → `reverse_proxy frontend:80` |
| `docs/ops/prod.env.vps.example` | Placeholder env (`YOUR_DOMAIN`, secrets blanks) |
| `docs/ops/vps-deploy.md` | Step-by-step runbook for you on the VPS |
| `docs/ops/DEPLOY.md` | Points to VPS path + correct smoke URLs |
| `docs/ops/smtp-setup.md` | Prod URLs use `YOUR_DOMAIN` |
| `docs/ops/README.md` | Index links |
| `docs/ops/vps-deploy-report.md` | This report |

## 2. Architecture confirmed

```text
Internet → Caddy :80/:443 → frontend:80
                              └─ /api/* → backend:8080   (existing frontend/docker/nginx.conf)
                           backend → postgres:5432, redis:6379
```

**nginx `/api/` proxy** already targets `http://backend:8080/api/` — no new API routing invented.

## 3. Health / smoke URLs (verified from config)

| Audience | Endpoint | Source |
|----------|----------|--------|
| **Browser / public** | `https://YOUR_DOMAIN/health` → `OK` | frontend `nginx.conf` `location = /health` |
| **Internal (VPS)** | `docker exec pa-vps-backend curl … http://localhost:8080/actuator/health` | backend Docker healthcheck |

Do **not** assume `/actuator/health` is public through Caddy (it is not required to be).

## 4. Local Compose validation

```text
published_ports = 80, 443 only
```

Merged config with base + prod + vps: **PASS**.  
Postgres/Redis/backend/frontend: **no** public `published` ports.

## 5. What you still do on the VPS

1. Install Docker + open firewall **80/443**  
2. Copy/clone the repo onto the VPS  
3. `cp docs/ops/prod.env.vps.example prod.env` → fill secrets + real domain when ready  
4. `docker compose -p project-analytics-vps -f docker/docker-compose.yml -f docker/docker-compose.prod.yml -f docker/docker-compose.vps.yml --env-file prod.env up -d --build`  
5. Point DNS **A/AAAA** at the VPS → wait for Caddy / Let’s Encrypt  
6. Smoke: `https://YOUR_DOMAIN/health`, login, sync; change seed admin password  
7. Schedule backups  

Full detail: **`docs/ops/vps-deploy.md`**

## 6. Out of scope (this gate)

- SSH to your server  
- Real certificate issuance  
- N6 / admin accounts UI  

---

## STOP

VPS deployment **preparation** complete. Live bring-up is on your VPS when DNS/`prod.env` are ready.
