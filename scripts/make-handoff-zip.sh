#!/usr/bin/env bash
# Create a small zip for company handoff (no node_modules / build / .git).
# Includes .env if present (secrets — only for private handoff).
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
OUT="${1:-$HOME/Project-Analytics-Xtensus.zip}"

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
  >/dev/null

ls -lh "$OUT"
echo "Ready: $OUT"
echo "Tell recipients to open README-XTENSUS.md first."
