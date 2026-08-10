# RB-006 — Restore from backup

## Procedure

1. Stop backend (and any writers).
2. Identify backup directory under `BACKUP_DIR` (default `./data/backups/<timestamp>`).
3. Run: `./scripts/restore-postgres.sh ./data/backups/<timestamp>`
4. Confirm reports directory restored when `reports.tgz` present.
5. Start backend; check readiness; login; open workspace dashboard; try report download for a known id.

## Policy

- Postgres dump = users, workspaces, projects, analytics, report **metadata**, portfolios.
- `reports.tgz` = immutable report **files** (must match metadata).
- Redis is not restored (cache).
