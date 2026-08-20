# UI-4 Correction Pass — Stop Report

**Status:** Correction complete — **not** starting UI-5  
**Date:** 2026-08-19

## What changed

### 1. No more duplicate old/new charts (prototypes)
- **Removed** Homepage SVG `Recommendation severity` bar
- **Removed** Nightingale from Homepage (evaluation stands: bar wins analytically)
- **Added** single **ECharts** `app-pa-bar-chart` for recommendation severity (gradient bars, value labels, semantic Critical/High colors)
- Nightingale component remains in codebase for optional future use; not shown beside a legacy SVG twin

### 2. Homepage line chart — richer information hierarchy
- Clear **legend** (project/series names)
- **Y-axis** titled “Health score”
- **Tooltip** shows metric name, timestamp, each series value with color swatch
- **Latest value** emphasized (markPoint + end/top labels) — not every point labeled
- **Trend direction** in subtitle (`↑/↓/→` + delta per series from first→last real points)
- Axis dates thinned to ≤8 real timestamps for readability (still real snapshot times)
- Selective luminous glow on primary series only

### 3. Stronger visualization colors
- Boosted `--pa-viz-*` (especially dark theme: brighter blue/teal/orange/pink/violet)
- Line strokes thicker; area gradients more saturated
- Chart shell **featured** mode: subtle viz-tinted surface gradient (hero only)

### 4. Gradients beyond KPI cards (selective)
- ECharts chart shells: soft surface gradient
- Featured hero shell: light viz hue wash
- ECharts severity bars: vertical color gradients
- KPI + recommendation gradients retained from prior pass
- Chrome (nav/buttons) still monochrome

## Nightingale / UI-5 notes
- **Type decision unchanged:** keep **bar** for severity, not rose
- **Implementation path:** severity now uses **ECharts bar** (legacy SVG removed for this viz)
- Full remaining SVG → ECharts migration stays **UI-5** after your approval

## Verification
- Build green
- Dark + light Home: line canvas present, ECharts severity bar present, Nightingale absent, SVG severity absent

## Stop
Awaiting approval of this correction before **UI-5**.
EOF