# RB-006 — Restore from backup

## Quick backup

From repository root (Postgres reachable — local `pa-postgres` or `pg_dump`):

```bash
./scripts/backup-postgres.sh
```

Creates `./data/backups/<UTC-timestamp>/` with:

- `postgres.dump` — database (custom format)
- `reports.tgz` — report files (or `reports.MISSING` if the reports dir was absent)
- `MANIFEST.txt`

Set `REPORT_STORAGE_PATH` if your reports are not at `./backend/data/reports` (Docker: `/data/reports`).

## Quick restore

**WARNING:** Overwrites the target database and report files. Stop the backend first.

```bash
./scripts/restore-postgres.sh ./data/backups/<timestamp>
```

Then start the backend and check `/actuator/health`.

## Procedure

1. Stop backend (and any writers).
2. Identify backup directory under `BACKUP_DIR` (default `./data/backups/<timestamp>`).
3. Run: `./scripts/restore-postgres.sh ./data/backups/<timestamp>`
4. Confirm reports directory restored when `reports.tgz` is present.
5. Start backend; check readiness; login; open a workspace dashboard; try a known report download.

## Policy

- Postgres dump = users, workspaces, projects, analytics, report **metadata**, portfolios.
- `reports.tgz` = immutable report **files** (must match metadata).
- Redis is not restored (cache only).

## Docker notes

- Scripts auto-use `docker exec` on `pa-postgres` when host `pg_dump` / `pg_restore` are missing.
- Prod compose uses `pa-prod-postgres` — for that container, either install client tools on the host pointing at the mapped port, or adjust the script / exec target for that name.
