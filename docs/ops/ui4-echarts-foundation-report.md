# UI-4 — ECharts Foundation + First Prototypes — Stop Report

**Status:** Complete — awaiting approval before UI-5  
**Date:** 2026-08-19  
**Deps:** `echarts@^6.1.0`, `ngx-echarts@^22.0.0`

## Implemented

### PA ECharts theme (complete before migration)
- `shared/charts/pa-echarts-theme.ts` — reads live `--pa-*` / `--pa-viz-*`, light+dark
- Shared defaults: transparent bg, axes, dashed grid, tooltip, crosshair, legend, animation
- **No stock ECharts palette leakage**
- ECharts-native animation retained; `prefers-reduced-motion` disables duration

### Global chart sizing
- Tokens: `--pa-chart-h-sm|md|lg|max` (200 / 260 / 320 / 380)
- `.pa-charts` 2-up desktop / 1-col ≤900px
- Applied to **existing SVG hosts** (bar, donut, ring, trend) + new ECharts shell
- Verified heights: Explorer bar **260px** (was unbounded); Home tiles **260px**; trend **320px**

### Homepage Health-score line chart
- **Metric confirmed:** `healthScore` = stored **Health score 0–100** from analytics snapshots (`TrendPointResponse`)
- Data: real `GET /analytics/projects/{id}/trends` for ≤3 top Needs Attention projects with ≥2 points
- Smooth line + gradient area + selective luminous glow on primary series only
- Crosshair tooltip; empty state when no history
- Axis labels include time so same-day recalculations remain distinct

### Nightingale prototype (recommendation severity)
- Polished rose (`roseType: 'area'`, padAngle, borderRadius, semantic Critical/High colors)
- Shown beside the existing severity **bar** for evaluation

### KPI gradients
- Subtle severity / delta washes; value text stays `--pa-text` high contrast
- Bright `--pa-viz-up` / `--pa-viz-down` for evolutionary deltas

### Recommendations unified
- Single `recommendation-list` upgraded (severity chips, hierarchy, gradient item chrome)
- Home uses `compact`; Project uses full — same design system

## Nightingale evaluation (honest)

| Criterion | Bar | Nightingale |
|-----------|-----|-------------|
| Compare category counts | **Clearer** | Harder (radius perception) |
| Visual punch | Moderate | **Higher** |
| Dark/light | Good | Good |
| Analytical default | **Yes** | Experiment |

**Recommendation:** **Keep the bar chart as the default severity visualization.**  
Nightingale is a successful polish prototype but is not analytically superior for comparing recommendation counts. For UI-5, either remove it from Home or keep it only as an optional alternate — do not replace the bar solely for visual impressiveness.

## Verification
- `ng build` green (bundle budget warning from ECharts — expected)
- Playwright: dark/light × desktop/mobile Home + Explorer + Project
- Line + Nightingale canvases render when data exists
- Chart height rhythm consistent across pages

## Intentionally not done (UI-5+)
- Full SVG → ECharts migration
- Project Detail trend ECharts replacement
- Portfolio / Executive chart migration

## UI-5 readiness
**Yes**, after you approve: migrate remaining charts onto PA theme wrappers with this sizing/color language.

**Stopped. Not starting UI-5.**
