#!/usr/bin/env bash
# M10 load baseline — critical-path API smoke under concurrent clients.
# Requires a running backend and valid credentials. Does not mutate product data beyond login.
#
# Usage:
#   BASE_URL=http://localhost:8080 \
#   USERNAME=admin PASSWORD='Admin123!' \
#   CONCURRENCY=5 REQUESTS=50 \
#   ./scripts/load-baseline.sh
#
# Results are written to data/load-baseline/<timestamp>.txt

set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
USERNAME="${USERNAME:-admin}"
PASSWORD="${PASSWORD:-Admin123!}"
CONCURRENCY="${CONCURRENCY:-5}"
REQUESTS="${REQUESTS:-50}"
OUT_DIR="${OUT_DIR:-./data/load-baseline}"
STAMP="$(date -u +%Y%m%dT%H%M%SZ)"
OUT_FILE="${OUT_DIR}/${STAMP}.txt"

mkdir -p "${OUT_DIR}"

echo "M10 load baseline" | tee "${OUT_FILE}"
echo "BASE_URL=${BASE_URL} CONCURRENCY=${CONCURRENCY} REQUESTS=${REQUESTS}" | tee -a "${OUT_FILE}"
echo "started_at=${STAMP}" | tee -a "${OUT_FILE}"

LOGIN_JSON=$(curl -sS -X POST "${BASE_URL}/api/v1/auth/login" \
  -H 'Content-Type: application/json' \
  -d "{\"username\":\"${USERNAME}\",\"password\":\"${PASSWORD}\"}")

TOKEN=$(echo "${LOGIN_JSON}" | sed -n 's/.*"token"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p' | head -1)
if [[ -z "${TOKEN}" ]]; then
  echo "ERROR: login failed; response=${LOGIN_JSON}" | tee -a "${OUT_FILE}"
  exit 1
fi

run_batch() {
  local path="$1"
  local label="$2"
  echo "--- ${label} (${path}) ---" | tee -a "${OUT_FILE}"
  # GNU parallel preferred; fall back to xargs
  if command -v parallel >/dev/null 2>&1; then
    seq 1 "${REQUESTS}" | parallel -j "${CONCURRENCY}" --bar \
      "curl -sS -o /dev/null -w '%{http_code} %{time_total}\n' -H 'Authorization: Bearer ${TOKEN}' '${BASE_URL}${path}'" \
      | tee -a "${OUT_FILE}"
  else
    seq 1 "${REQUESTS}" | xargs -I{} -P "${CONCURRENCY}" \
      curl -sS -o /dev/null -w "%{http_code} %{time_total}\n" \
      -H "Authorization: Bearer ${TOKEN}" \
      "${BASE_URL}${path}" \
      | tee -a "${OUT_FILE}"
  fi
}

# Critical path: health, me, workspaces list (authenticated surface)
run_batch "/actuator/health/readiness" "readiness"
run_batch "/api/v1/auth/me" "auth_me"
run_batch "/api/v1/workspaces" "workspaces"

echo "finished_at=$(date -u +%Y%m%dT%H%M%SZ)" | tee -a "${OUT_FILE}"
echo "Results: ${OUT_FILE}"
