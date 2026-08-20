# UI-5.1 — Explorer — Stop Report

**Status:** Complete — awaiting approval before UI-5.2 Project  
**Date:** 2026-08-19  
**Scope:** Explorer visualizations only. No IA / filters / metrics / auth changes.

---

## Inventory (before)

| Page | Visualization | Implementation | Data |
|------|---------------|----------------|------|
| Explorer | Health distribution (current result set) | Legacy SVG `app-bar-chart` | `healthDistribution(filtered rows)` — Critical / Watch / Healthy / Unknown counts; click → Health filters |

No other Explorer charts (filters/table are not chart viz).

---

## What changed

| Legacy | Replaced with | Why this type |
|--------|---------------|---------------|
| SVG Health distribution bar | **ECharts `app-pa-bar-chart`** | Categorical **counts** by Health band — bar remains the best comparison form (not donut/gauge) |

- Removed `app-bar-chart` from Explorer (no old+new twin).
- Preserved click-to-filter: Critical → `criticalOnly`; Watch/Healthy → `healthMin`/`healthMax` (same as before).
- PA theme, `--pa-chart-h-md` sizing (shell ~340px, not oversized), bright semantic band colors, bar gradients, tooltips with counts.
- Wrapped in `.pa-charts` for global layout rhythm.

---

## Data / API issues

None. Uses existing explorer rows + `healthDistribution` only.

---

## Before / after behavior

| | Before | After |
|--|--------|--------|
| Renderer | Custom SVG | ECharts |
| Interaction | Click bar → filter | Same (verified: Healthy → `healthMin=70`, match count 7→2) |
| Height | Could stretch with container | Capped via PA chart shell `size="md"` |

---

## Verification

| Check | Result |
|--------|--------|
| Build | Green |
| Dark/light × desktop/mobile | ECharts canvas present; **0** legacy SVG chart components |
| Chart height | ~md shell (~340px), not giant |
| Click filter | Works (Healthy band) |

---

## STOP

**Do not start UI-5.2 Project** until you approve this Explorer batch.
