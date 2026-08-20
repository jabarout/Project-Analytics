#!/usr/bin/env bash
# M9 — Backup PostgreSQL and immutable report artifacts together with metadata.
# Policy: reports are historical point-in-time files; backup report files + DB (metadata).
#
# Usage (from repo root or scripts/):
#   ./scripts/backup-postgres.sh
# Env: DB_*, REPORT_STORAGE_PATH, BACKUP_DIR, BACKUP_RETENTION_DAYS

set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

if [[ -f .env ]]; then
  set -a
  # shellcheck disable=SC1091
  source .env
  set +a
fi

DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-projectanalytics}"
DB_USERNAME="${DB_USERNAME:-projectanalytics}"
DB_PASSWORD="${DB_PASSWORD:-projectanalytics}"
# Repo-root default matches run-backend.sh (cwd=backend/). Docker sets /data/reports.
REPORT_STORAGE_PATH="${REPORT_STORAGE_PATH:-./backend/data/reports}"
BACKUP_DIR="${BACKUP_DIR:-./data/backups}"
BACKUP_RETENTION_DAYS="${BACKUP_RETENTION_DAYS:-14}"
STAMP="$(date -u +%Y%m%dT%H%M%SZ)"
OUT_DIR="${BACKUP_DIR}/${STAMP}"

mkdir -p "$OUT_DIR"

echo "Backing up PostgreSQL ${DB_NAME}@${DB_HOST}:${DB_PORT} → ${OUT_DIR}"
export PGPASSWORD="$DB_PASSWORD"

if command -v pg_dump >/dev/null 2>&1; then
  pg_dump -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USERNAME" -d "$DB_NAME" \
    --format=custom --file="${OUT_DIR}/postgres.dump"
elif docker ps --format '{{.Names}}' 2>/dev/null | grep -q '^pa-postgres$'; then
  docker exec -e PGPASSWORD="$DB_PASSWORD" pa-postgres \
    pg_dump -U "$DB_USERNAME" -d "$DB_NAME" --format=custom \
    > "${OUT_DIR}/postgres.dump"
else
  echo "ERROR: pg_dump not found and container pa-postgres is not running." >&2
  exit 1
fi

# Immutable report files (paired with report table metadata in Postgres)
if [[ -d "$REPORT_STORAGE_PATH" ]]; then
  echo "Backing up report files from ${REPORT_STORAGE_PATH}"
  tar -czf "${OUT_DIR}/reports.tgz" -C "$(dirname "$REPORT_STORAGE_PATH")" "$(basename "$REPORT_STORAGE_PATH")"
else
  echo "WARN: REPORT_STORAGE_PATH=${REPORT_STORAGE_PATH} missing — metadata-only backup."
  : > "${OUT_DIR}/reports.MISSING"
fi

cat > "${OUT_DIR}/MANIFEST.txt" <<EOF
Project Analytics backup
timestamp_utc=${STAMP}
database=${DB_NAME}
host=${DB_HOST}:${DB_PORT}
reports_path=${REPORT_STORAGE_PATH}
policy=Reports are immutable historical artifacts; files are backed up with DB metadata.
EOF

# Retention
if [[ -d "$BACKUP_DIR" ]]; then
  find "$BACKUP_DIR" -mindepth 1 -maxdepth 1 -type d -mtime "+${BACKUP_RETENTION_DAYS}" -exec rm -rf {} +
fi

echo "Backup complete: ${OUT_DIR}"
ls -lah "$OUT_DIR"
