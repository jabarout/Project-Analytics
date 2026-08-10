# RB-005 — Rotate OpenProject API key (temporary auth)

## Symptoms

- Sync fails after key rotation or leak response

## Procedure

1. Issue new key in OpenProject (user or global API token); revoke old key.
2. Update **both** if switching instance:
   - `OPENPROJECT_API_KEY` in `.env` (or deployment secret)
   - `OPENPROJECT_URL` (optional default) **and** the **Connections** workspace base URL in the UI to the company OpenProject URL
3. **Restart the backend** with env loaded:
   ```bash
   # from repo root — loads .env into the process
   ./scripts/run-backend.sh
   ```
   Spring Boot does **not** re-read `.env` while running. Editing the file alone is not enough.
4. Startup log should show: `OpenProject credentials: API key configured (length=N)`.
   If you see `API key is NOT configured in this process`, the JVM never received the variable.
5. Verify: Connections → Synchronize → SUCCESS.

## Company / tester handoff checklist

| Item | Where |
|------|--------|
| Company OpenProject base URL | Connections form (workspace URL) |
| Company API key | `.env` → `OPENPROJECT_API_KEY` (platform-level, temporary until OAuth) |
| Optional default URL | `.env` → `OPENPROJECT_URL` (only used when creating a connection without typing URL) |
| TLS issues (self-signed) | `.env` → `OPENPROJECT_VERIFY_SSL=false` then restart |
| Backend start | Always `./scripts/run-backend.sh` (or export env then `mvn spring-boot:run`) |

## Common failure: "OpenProject API key is not configured"

| Cause | Fix |
|-------|-----|
| Backend started without sourcing `.env` | Use `./scripts/run-backend.sh` |
| Key changed in `.env` but process not restarted | Restart backend |
| Key empty / quoted wrong / spaces | `OPENPROJECT_API_KEY=opapi-...` no spaces, no quotes unless needed |
| Docker backend without compose env | Pass `OPENPROJECT_API_KEY` into the container (compose already maps it from host env) |

## Notes

- Long-term: OAuth via `OpenProjectCredentialResolver`.
- One env key is platform-level until OAuth / per-workspace credentials.
- The UI never stores the API key; only the OpenProject **base URL** is stored per connection.
