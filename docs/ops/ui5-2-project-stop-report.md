# UI-5.2 — Project Detail — Stop Report

**Status:** Complete — awaiting approval before UI-5.3 Portfolio  
**Date:** 2026-08-19  
**Scope:** Project Detail visualizations only. No IA / metrics / auth changes.

---

## Inventory (before)

| # | Visualization | Legacy impl | Data / meaning |
|---|---------------|-------------|----------------|
| 1 | Completed vs remaining | SVG `app-donut-chart` | WP completed vs open counts |
| 2 | Work package status distribution | SVG `app-bar-chart` | Open / In progress / Completed / Overdue / Blocked counts |
| 3 | Completion | SVG `app-ring-metric` | `completionPercentage` (0–100) |
| 4 | Health / Risk / Needs Attention factors (×3) | CSS `app-factor-bars` | Canonical score factor contributions |
| 5 | Score trends | SVG `app-trend-chart` | Historical Health / Risk / Attention snapshots |

---

## Replacements

| Legacy | ECharts type | Why |
|--------|--------------|-----|
| Completed vs remaining SVG donut | **`app-pa-donut-chart`** | Part-to-whole of WP delivery |
| WP status SVG bar | **`app-pa-bar-chart` horizontal** | Several labeled buckets — horizontal bars compare more clearly |
| Completion SVG ring | **`app-pa-donut-chart`** with `centerValue` = % | Proportion toward 100% (not a gauge dial) |
| Factor CSS bars (×3) | **`app-pa-bar-chart` horizontal** | Contribution magnitudes / long labels |
| Score trends SVG | **`app-pa-line-chart`** (3 series) | Real time-series; fixed Health/Risk/Attention — readable at project scope |

**Legacy on Project page after migration:** **0** (`app-bar-chart`, `app-donut-chart`, `app-ring-metric`, `app-trend-chart`, `app-factor-bars` all removed from this page).

---

## Shared enhancements
- `pa-bar-chart`: `orientation` (`vertical` \| `horizontal`) + `valueLabel`
- `pa-line-chart`: optional `color` per series + `featured` input (Home Average Health still featured)

---

## Data / API issues
None for this batch. Uses existing project dashboard + WP analytics + trends payloads.

---

## Before / after

| | Before | After |
|--|--------|--------|
| Chart engine | SVG/CSS custom | ECharts only on Project |
| Sizing | Mixed / stretch risk | PA shells `sm`/`md`/`lg` (verified ~283 / ~343 / 380px) |
| Trends | Custom SVG polyline | Smooth multi-series line, semantic colors, tooltips |

---

## Verification

| Check | Result |
|--------|--------|
| `ng build` | Green |
| Dark/light × desktop/mobile | Project page loads; **0** legacy chart components |
| ECharts present | 4 bars + 2 donuts + 1 line |
| Chart heights | Within PA max (≤380px) |

---

## STOP

**Do not start UI-5.3 Portfolio** until you approve this Project batch.
