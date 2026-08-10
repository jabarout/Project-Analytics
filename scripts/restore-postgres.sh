#!/usr/bin/env bash
# M9 — Restore PostgreSQL dump and report file archive from a backup directory.
#
# Usage:
#   ./scripts/restore-postgres.sh ./data/backups/20260731T120000Z
#
# WARNING: Overwrites the target database and report storage. Stop the backend first.

set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

BACKUP_PATH="${1:-}"
if [[ -z "$BACKUP_PATH" || ! -d "$BACKUP_PATH" ]]; then
  echo "Usage: $0 <backup-directory>" >&2
  exit 1
fi

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
REPORT_STORAGE_PATH="${REPORT_STORAGE_PATH:-./data/reports}"

DUMP="${BACKUP_PATH}/postgres.dump"
if [[ ! -f "$DUMP" ]]; then
  echo "ERROR: ${DUMP} not found" >&2
  exit 1
fi

echo "Restoring PostgreSQL from ${DUMP} (destroys existing data in ${DB_NAME})"
export PGPASSWORD="$DB_PASSWORD"

if command -v pg_restore >/dev/null 2>&1; then
  pg_restore -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USERNAME" -d "$DB_NAME" \
    --clean --if-exists --no-owner --no-acl "$DUMP"
elif docker ps --format '{{.Names}}' 2>/dev/null | grep -q '^pa-postgres$'; then
  docker exec -i -e PGPASSWORD="$DB_PASSWORD" pa-postgres \
    pg_restore -U "$DB_USERNAME" -d "$DB_NAME" --clean --if-exists --no-owner --no-acl \
    < "$DUMP"
else
  echo "ERROR: pg_restore not found and container pa-postgres is not running." >&2
  exit 1
fi

if [[ -f "${BACKUP_PATH}/reports.tgz" ]]; then
  echo "Restoring report files to ${REPORT_STORAGE_PATH}"
  mkdir -p "$(dirname "$REPORT_STORAGE_PATH")"
  tar -xzf "${BACKUP_PATH}/reports.tgz" -C "$(dirname "$REPORT_STORAGE_PATH")"
else
  echo "WARN: No reports.tgz in backup — report downloads may fail for historical files (metadata still in DB)."
fi

echo "Restore complete. Start backend and verify /actuator/health/readiness."
