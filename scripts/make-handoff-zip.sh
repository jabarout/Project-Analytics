#!/usr/bin/env bash
# Create a handoff zip (no node_modules / build / .git / secrets).
# Recipients should open docs/ops/DEPLOY.md first.
#
# Usage:
#   ./scripts/make-handoff-zip.sh [output.zip]
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
OUT="${1:-$HOME/Project-Analytics-Handoff.zip}"

rm -f "$OUT"
cd "$ROOT"

zip -r "$OUT" . \
  -x "frontend/node_modules/*" \
  -x "frontend/node_modules/**" \
  -x "frontend/dist/*" \
  -x "frontend/dist/**" \
  -x "frontend/.angular/*" \
  -x "frontend/.angular/**" \
  -x "backend/target/*" \
  -x "backend/target/**" \
  -x "backend/data/*" \
  -x "backend/data/**" \
  -x "data/backups/*" \
  -x "data/backups/**" \
  -x ".git/*" \
  -x ".git/**" \
  -x "**/.git/*" \
  -x "**/node_modules/*" \
  -x "**/target/*" \
  -x "**/.angular/*" \
  -x "**/dist/*" \
  -x "**/*.log" \
  -x "**/coverage/*" \
  -x "**/.vite/*" \
  -x ".env" \
  -x ".env.local" \
  -x ".env.*.local" \
  -x "prod.env" \
  -x "*.env.local" \
  >/dev/null

ls -lh "$OUT"
echo "Ready: $OUT"
echo "Tell recipients to open docs/ops/DEPLOY.md first (then copy .env.example → prod.env)."
