# RB-007 — Flyway migration failure during deployment

## Symptoms

- Backend fails to start
- Logs: Flyway migrate error / checksum mismatch / failed migration
- Readiness never becomes UP

## Checks

1. Backend logs for Flyway stack traces.
2. Table `flyway_schema_history` in Postgres (success/failure, checksum).
3. Confirm migration files were not edited after apply (checksum mismatch).
4. Confirm DB user has DDL rights.

## Remediation

1. **Never** edit applied migrations in shared environments. Add a new `V(n+1)__*.sql` instead.
2. If deploy introduced a bad migration that failed mid-way:
   - Prefer restore from last good backup (RB-006) on pilot/dev if data allows.
   - Or fix forward with a new migration after restoring to a consistent state.
3. Checksum mismatch after accidental edit:
   - Restore correct migration content from git; use Flyway repair only with senior ops approval and documented process.
4. Re-deploy backend; confirm Flyway “successfully applied” / “schema is up to date”.
5. Verify `/actuator/health/readiness` and smoke login + one dashboard.

## Notes

- M9 does not add business schema; this runbook covers all Flyway deploys (V1+).
- Application uses `spring.jpa.hibernate.ddl-auto=validate` — schema must match migrations.
