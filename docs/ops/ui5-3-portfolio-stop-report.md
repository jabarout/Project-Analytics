# UI-5.3 — Portfolio Analytics — Stop Report

**Status:** Complete — awaiting approval before UI-5.4 Executive  
**Date:** 2026-08-19  
**Scope:** Portfolio Detail Visual analytics only. No IA / metrics / auth changes. Portfolio list has no charts (unchanged).

---

## Inventory (before)

| # | Visualization | Legacy impl | Data / meaning |
|---|---------------|-------------|----------------|
| 1 | Health distribution | SVG `app-donut-chart` | Member counts by Health band |
| 2 | Risk distribution | SVG `app-bar-chart` | Member counts by Risk band |
| 3 | Actual progress bands | SVG `app-bar-chart` | Member counts by progress % band |
| 4 | Needs Attention split | SVG `app-donut-chart` | Needs Attention vs Stable |
| 5 | Overdue work packages | SVG `app-bar-chart` | Has overdue open WPs vs none |

---

## Replacements

| Legacy | ECharts type | Why |
|--------|--------------|-----|
| Health SVG donut | **`app-pa-donut-chart`** | Part-to-whole of few Health bands; center “Projects” total |
| Risk SVG bar | **`app-pa-bar-chart`** (vertical) | Categorical counts |
| Progress SVG bar | **`app-pa-bar-chart`** (vertical) | Categorical counts |
| Needs Attention SVG donut | **`app-pa-bar-chart`** (vertical) | Two-way split — bar compares counts more clearly (same choice as Home) |
| Overdue SVG bar | **`app-pa-bar-chart`** (vertical) | Categorical counts |

**Legacy on Portfolio page after migration:** **0** (`app-bar-chart` / `app-donut-chart` removed from this page).

**Click → Explorer:** Preserved via `onDistributionClick` (band filters / drill presets + `portfolioId` scope).

---

## Data / API issues

None. Uses existing portfolio member explorer rows + shared `distribution.ts` helpers (same as Home/Explorer).

---

## Before / after

| | Before | After |
|--|--------|--------|
| Chart engine | SVG custom | ECharts only on Portfolio Visual analytics |
| Needs Attention | SVG donut | Bar (2-way split) |
| Sizing | Mixed | PA shells `md` (~343px verified) |
| Drill | Segment click → Explorer | Donut / bar click → Explorer with portfolio + band params |

---

## Verification

| Check | Result |
|--------|--------|
| `ng build` (development) | Green |
| Dark/light × desktop/mobile | Portfolio detail loads; charts render |
| ECharts present | **1** donut + **4** bars (**5** canvases) |
| Legacy chart components | **0** |
| Chart heights | 343px (within PA max ≤380) |
| Bar click → Explorer | Yes (`portfolioId` + workspace) |
| Donut click → Explorer | Yes (`healthMin`/`healthMax` + `portfolioId`) |
| Screenshots | `/tmp/pa-ui53-verify/screenshots/` |

---

## STOP

**Do not start UI-5.4 Executive** until you approve this Portfolio batch.
