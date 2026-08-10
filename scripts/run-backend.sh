#!/usr/bin/env bash
# Start the Spring Boot backend with gitignored .env loaded into the process.
# Usage (from repo root): ./scripts/run-backend.sh
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

if [[ ! -f .env ]]; then
  echo "Missing .env — copy .env.example to .env and set OPENPROJECT_API_KEY / OPENPROJECT_URL."
  exit 1
fi

# Export all variables from .env into this process (and thus into Spring Boot).
set -a
# shellcheck disable=SC1091
source "$ROOT/.env"
set +a

if [[ -z "${OPENPROJECT_API_KEY:-}" ]]; then
  echo "OPENPROJECT_API_KEY is empty in .env — sync will fail with 'API key is not configured'."
  exit 1
fi

# Normalize trailing slash on URL (workspace URL in UI is separate; this is env default).
if [[ -n "${OPENPROJECT_URL:-}" ]]; then
  OPENPROJECT_URL="${OPENPROJECT_URL%/}"
  export OPENPROJECT_URL
fi

echo "Starting backend with OPENPROJECT_URL=${OPENPROJECT_URL:-'(unset)'} and OPENPROJECT_API_KEY set (length ${#OPENPROJECT_API_KEY})."
echo "Postgres/Redis should already be up (e.g. docker compose in docker/)."

cd "$ROOT/backend"
exec mvn spring-boot:run
