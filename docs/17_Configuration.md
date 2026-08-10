# Configuration

Version: 1.0

Status: Draft

---

# 1. Purpose

This document defines the configuration strategy for Project Analytics.

The objective is to ensure that every environment is configurable without modifying application code.

All configuration values must be externalized.

Configuration must never be hardcoded.

---

# 2. Configuration Principles

The application shall:

- Use environment variables.
- Support environment-specific configuration.
- Keep secrets outside the repository.
- Validate required configuration during startup.
- Provide sensible defaults only for development.

---

# 3. Environments

Supported environments:

Development

Testing

Staging

Production

Each environment has its own configuration.

---

# 4. Backend Configuration

Spring Boot uses:

```
application.yml
```

Environment-specific files:

```
application-dev.yml

application-test.yml

application-stage.yml

application-prod.yml
```

No environment-specific values should be committed directly inside the main configuration.

---

# 5. Frontend Configuration

Angular environments:

```
environment.ts

environment.development.ts

environment.production.ts
```

Frontend configuration should contain only non-sensitive information.

Secrets must never be stored in frontend code.

---

# 6. Database Configuration

Required variables:

```
DB_HOST

DB_PORT

DB_NAME

DB_USERNAME

DB_PASSWORD
```

Example:

```
DB_HOST=postgres

DB_PORT=5432

DB_NAME=projectanalytics
```

---

# 7. Redis Configuration

Required variables:

```
REDIS_HOST

REDIS_PORT

REDIS_PASSWORD
```

Example:

```
REDIS_HOST=redis

REDIS_PORT=6379
```

---

# 8. JWT Configuration

Required variables:

```
JWT_SECRET

JWT_EXPIRATION

JWT_REFRESH_EXPIRATION
```

The JWT secret must:

- Be randomly generated.
- Be unique per environment.
- Never be committed to Git.

---

# 9. OpenProject Configuration

Required variables:

```
OPENPROJECT_URL

OPENPROJECT_API_KEY

OPENPROJECT_TIMEOUT

OPENPROJECT_VERIFY_SSL
```

The API key must be stored securely.

These variables feed the default `OpenProjectCredentialResolver` implementation (environment API key). The synchronization engine consumes only the resolved connection properties.

**Future (OAuth 2.0) — official long-term target**

OAuth 2.0 is the official long-term mechanism for authenticating OpenProject integrations (one Workspace = one OpenProject instance). When introduced, configuration will expand (for example client id/secret, redirect URI, token endpoint settings). Only the credential resolver and HTTP client auth path change; the synchronization engine, import pipeline, and analytics modules do not. Until the OAuth milestone ships, `OPENPROJECT_API_KEY` remains the temporary active path.

---

# 10. Server Configuration

Variables:

```
SERVER_PORT

SERVER_CONTEXT_PATH

SERVER_TIMEZONE
```

Example:

```
SERVER_PORT=8080
```

---

# 11. Logging Configuration

Variables:

```
LOG_LEVEL

LOG_FILE

LOG_PATTERN
```

Development:

```
DEBUG
```

Production:

```
INFO
```

Debug logging should not remain enabled in production unless required for troubleshooting.

---

# 12. File Storage Configuration

Variables:

```
REPORT_STORAGE_PATH

REPORT_RETENTION_DAYS

REPORT_PURGE_ENABLED

MAX_REPORT_SIZE
```

Generated reports should be stored outside the application binary.

**Backup policy (M9):** Reports are immutable historical artifacts. Backup Postgres metadata and report files together (`scripts/backup-postgres.sh`).

**Retention (M10):** When `REPORT_PURGE_ENABLED=true`, a scheduled job deletes report metadata and files older than `REPORT_RETENTION_DAYS`.

---

# 12.0.1 Analytics snapshot retention (M10)

```
ANALYTICS_SNAPSHOT_RETENTION_DAYS=90
ANALYTICS_SNAPSHOT_PURGE_ENABLED=true
```

Purges historical `analytics_snapshot` rows only. Latest per-project `analytics` scores are never deleted by this job.

---

# 12.0.2 Production security toggles (M10)

```
OPENAPI_PUBLIC=false   # prod profile default; Swagger not public
CORS_ALLOWED_ORIGINS=https://your-frontend.example.com
JWT_SECRET=<strong-secret-min-32-chars>
```

Prod startup fails if JWT secret is missing, too short, or the development default, or if CORS is empty/`*`.

---

# 12.1 Observability Configuration (M9)

```
# Probes:
#   /actuator/health/liveness   — process only (no OpenProject, no DB requirement)
#   /actuator/health/readiness  — local infra: app + DB + Redis (when enabled)
# Metrics:
#   /actuator/prometheus        — custom business meters use frozen pa_* prefix

SYNC_SUCCESS_STALE_SECONDS=86400

PROMETHEUS_PORT=9090
ALERTMANAGER_PORT=9093
GRAFANA_PORT=3000
GRAFANA_ADMIN_USER=admin
GRAFANA_ADMIN_PASSWORD=

BACKUP_DIR=./data/backups
BACKUP_RETENTION_DAYS=14
```

OpenTelemetry tracing is deferred; Micrometer is the bridge for future OTEL export.

See `docs/ops/README.md`.

---

# 13. Synchronization Configuration

Variables:

```
SYNC_ENABLED

SYNC_INTERVAL

SYNC_BATCH_SIZE

SYNC_RETRY_COUNT

SYNC_TIMEOUT
```

These values should be configurable without code changes.

---

# 14. Analytics Configuration

Variables:

```
HEALTH_SCORE_ENABLED

RISK_SCORE_ENABLED

ATTENTION_SCORE_ENABLED

HISTORY_RETENTION_DAYS
```

Business thresholds and scoring weights should be configurable through application settings or dedicated configuration rather than hardcoded values.

**M5 note:** Scoring weight properties are active under `projectanalytics.analytics.*`. `HISTORY_RETENTION_DAYS` is reserved for a future snapshot-purge job; M5 does not yet delete old `analytics_snapshot` rows automatically.

---

# 15. Monitoring Configuration

Variables:

```
PROMETHEUS_ENABLED

METRICS_ENABLED

HEALTH_ENDPOINT_ENABLED
```

Monitoring should remain enabled in production.

---

# 16. Docker Configuration

Docker Compose provides:

- PostgreSQL
- Redis
- Backend
- Frontend

Configuration should be injected through environment variables.

Example:

```
docker-compose.yml

.env
```

The `.env` file should not contain production secrets and should not be committed if it includes sensitive values.

---

# 17. Kubernetes Configuration

Future deployment may use:

- ConfigMaps
- Secrets
- Ingress
- Persistent Volumes

Configuration should remain compatible with container orchestration.

---

# 18. Configuration Validation

During startup the backend shall verify:

- Required variables exist.
- Required secrets exist.
- Invalid values are rejected.
- Missing configuration stops startup with meaningful errors.

Fail fast when configuration is invalid.

---

# 19. Configuration Documentation

Every configuration property should include:

- Name
- Purpose
- Default value (if any)
- Required or optional
- Environment applicability

Configuration changes should be documented.

---

# 20. AI Implementation Notes

AI assistants shall:

- Never hardcode secrets.
- Use environment variables.
- Keep development and production configurations separate.
- Validate configuration during startup.
- Document any newly introduced configuration variables.

---

# End of Document