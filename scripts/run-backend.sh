#!/usr/bin/env bash
# Start the Spring Boot backend with gitignored .env loaded into the process.
# Usage (from repo root): ./scripts/run-backend.sh
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

if [[ ! -f .env ]]; then
  echo "Missing .env — copy .env.example to .env (OpenProject URL/API key are entered on Connections, not required here)."
  exit 1
fi

# Export all variables from .env into this process (and thus into Spring Boot).
set -a
# shellcheck disable=SC1091
source "$ROOT/.env"
set +a

# Normalize trailing slash on optional env default URL (workspace URL in UI is separate).
if [[ -n "${OPENPROJECT_URL:-}" ]]; then
  OPENPROJECT_URL="${OPENPROJECT_URL%/}"
  export OPENPROJECT_URL
fi

if [[ -n "${OPENPROJECT_API_KEY:-}" ]]; then
  echo "Starting backend (optional env OPENPROJECT_API_KEY present, length ${#OPENPROJECT_API_KEY}; default URL=${OPENPROJECT_URL:-unset})."
else
  echo "Starting backend (no env OpenProject API key — connect a workspace on the Connections screen)."
fi
echo "Postgres/Redis should already be up (e.g. docker compose in docker/)."

cd "$ROOT/backend"
exec mvn spring-boot:run
