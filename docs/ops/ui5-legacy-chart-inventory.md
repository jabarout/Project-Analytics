# UI-5 preparation — Legacy chart inventory & migration plan

**Status:** Inventory / proposal only — **no code changes** · **UI-5 not started**  
**Date:** 2026-08-19  

**Approved architecture (target end state):**

| Layer | System |
|-------|--------|
| PA UI chrome | SCSS / `--pa-*` (monochrome) |
| PA charts | **ECharts only** (PA theme, sizing, bright viz colors) |

**Not approved yet:** Starting UI-5 implementation.

---

## 1. Search method

Grep of `frontend/src` for:

- Selectors: `app-bar-chart`, `app-donut-chart`, `app-ring-metric`, `app-trend-chart`, `app-factor-bars`
- SVG chart markup (`viewBox`, `polyline`, `stroke-dasharray` in dashboard components)
- ECharts usage (`ngx-echarts`, `app-pa-*`)
- Pages: Home, Explorer, Project Detail, Portfolio Analytics, Executive, Reports, Settings, Workspaces, Login, Legal

**Pages with no data charts:** Reports, Settings, Workspaces, Login, Legal, Portfolio list (cards only).

---

## 2. Already on ECharts (keep / fix concept — not “legacy SVG”)

| Page | Visualization | Implementation | Notes |
|------|---------------|----------------|-------|
| Home | Health score evolution (hero) | `app-pa-line-chart` (ECharts) | **Concept wrong for scale** — still multi-project lines. Approved rethink: **Average Health over time** + ranked drivers (not implemented yet). |
| Home | Recommendation severity | `app-pa-bar-chart` (ECharts) | Correct type; keep. Remove any remaining SVG twin (already done). |

Unused but present: `app-pa-nightingale-chart` (not on any page; eval concluded bar wins).

---

## 3. Complete inventory — remaining legacy / custom visualizations

| # | Page | Current visualization | Current implementation | Data / meaning | Proposed ECharts type | Reason |
|---|------|----------------------|------------------------|----------------|----------------------|--------|
| L1 | **Home** | Health distribution | SVG `app-donut-chart` | Counts of projects by Health band (Critical / Watch / Healthy / Unknown) from explorer rows | **ECharts pie (donut)** *or* **stacked horizontal bar** | Part-to-whole of few bands; donut remains clear; stacked bar may compare counts more precisely. Prefer **donut** if center “Projects” total stays useful; else stacked bar. |
| L2 | **Home** | Progress distribution | SVG `app-bar-chart` | Counts in progress bands 0–33 / 34–66 / 67–100% | **ECharts bar** | Categorical counts — bar is the right type. |
| L3 | **Home** | Needs Attention split | SVG `app-donut-chart` | Needs Attention vs Stable project counts | **ECharts pie (donut)** or **two-segment bar** | Binary split; donut or simple bar both fine — prefer **bar** for easier count comparison (2 categories). |
| L4 | **Home** | Overdue work packages | SVG `app-bar-chart` | Projects with overdue WPs vs none | **ECharts bar** | Same as L2 — categorical counts. |
| L5 | **Home** | Average progress | SVG `app-ring-metric` | `kpis.averageCompletion` (WP completion average %) | **ECharts gauge** (or ring via pie) | Single % KPI — gauge/ring communicates “fill toward 100%”. |
| L6 | **Explorer** | Health distribution (current result set) | SVG `app-bar-chart` | Health bands on **filtered** explorer rows; click → filter | **ECharts bar** | Filtered categorical counts + drill; bar remains best. Preserve click→filter. |
| L7 | **Project Detail** | Completed vs remaining | SVG `app-donut-chart` | WP completed vs open/remaining counts | **ECharts pie (donut)** | Classic part-to-whole of delivery. |
| L8 | **Project Detail** | Work package status distribution | SVG `app-bar-chart` | Counts by WP status | **ECharts bar** (horizontal if many statuses) | Many categories — bar (horizontal) reads better than pie. |
| L9 | **Project Detail** | Completion | SVG `app-ring-metric` | Project `completionPercentage` | **ECharts gauge** | Single progress %. |
| L10 | **Project Detail** | Health / Risk / Needs Attention **factor bars** (×3) | Custom CSS `app-factor-bars` (div tracks, not SVG) | Score factor contributions from analytics factors | **ECharts horizontal bar** | Explainability bars; ECharts bar keeps PA theme/tooltips; still contribution magnitudes, not a new score. |
| L11 | **Project Detail** | Score trends (historical snapshots) | SVG `app-trend-chart` | Project `trends[]`: Health, Risk, Needs Attention over snapshots | **ECharts multi-series line** (≤3 series) | Real time-series; 3 fixed series is readable (unlike N projects). Use PA line theme; bright series colors. |
| L12 | **Portfolio Analytics** | Health distribution | SVG `app-donut-chart` | Same band counts on portfolio member rows | Same as L1 | Scope = portfolio members. |
| L13 | **Portfolio Analytics** | Risk distribution | SVG `app-bar-chart` | Risk band counts | **ECharts bar** | Categorical. |
| L14 | **Portfolio Analytics** | Actual progress bands | SVG `app-bar-chart` | Progress band counts | **ECharts bar** | Categorical. |
| L15 | **Portfolio Analytics** | Needs Attention split | SVG `app-donut-chart` | Needs vs Stable | Same as L3 | Prefer bar for 2-way split. |
| L16 | **Portfolio Analytics** | Overdue work packages | SVG `app-bar-chart` | Has overdue WP vs none | **ECharts bar** | Categorical. |
| L17 | **Executive Dashboard** | Average health by workspace | SVG `app-bar-chart` | Per-workspace `averageHealthScore` | **ECharts bar** (horizontal if many workspaces) | Compare scopes — bar. |
| L18 | **Executive Dashboard** | Average attention by workspace | SVG `app-bar-chart` | Per-workspace attention average | **ECharts bar** | Same. |

### Legacy component files (to delete after last consumer migrates)

| Component | Tech |
|-----------|------|
| `bar-chart.component.ts` | Custom SVG |
| `donut-chart.component.ts` | Custom SVG |
| `ring-metric.component.ts` | Custom SVG |
| `trend-chart.component.ts` | Custom SVG |
| `factor-bars.component.ts` | Custom CSS bars |

### Not charts (out of UI-5 chart migration)

KPI cards, recommendation list, attention/project tables, empty states, loading spinner, Insights text lists.

---

## 4. Homepage Average Health rethink (approved — still to implement)

| Item | Decision |
|------|----------|
| Default hero | **One** ECharts line: **Average Health over time** = temporal extension of existing `averageHealthScore` (equal-weight mean of project Health in each snapshot wave) |
| Not default | One line per project |
| Companion | Compact ranked drivers (largest Health Δ improve / worsen) from real snapshots |
| Optional later | Explicit multi-select project compare |
| Metric | **No new formula** — same definition as Home KPI “Average health” |

### Backend / API gap

| Need | Status |
|------|--------|
| `GET /analytics/workspaces/{id}/trends` → `{ calculatedAt, averageHealthScore, sampleSize }[]` | **Missing** (preferred long-term) |
| Client fan-out of all project trends + mean | Feasible for tiny demos; **not** acceptable at 50–100 projects |

**Recommendation:** Add workspace trends (+ optional driver deltas) on the backend as part of the **Home chart correction** (before or as Phase 0 of UI-5), not as a new metric definition.

---

## 5. Proposed UI-5 migration order

**Principle:** Replace legacy in place (remove SVG/CSS chart); one visual language (PA ECharts theme + sizing); choose best type per row above; preserve data/drill semantics.

### Phase 0 — Home hero correction (pre- or first UI-5 gate)

1. Workspace Average Health trend API (or approved interim)  
2. Replace current multi-project Home line with **aggregate line** + **ranked drivers**  
3. Confirm empty states / `n` in tooltip  

### Phase 1 — Shared wrappers

Extend `app-pa-bar-chart`, add `app-pa-donut-chart` / `app-pa-gauge-chart` (or one flexible categorical + gauge), wire drill `barClick` / `segmentClick` for Explorer/Home/Portfolio.

### Phase 2 — Home remaining legacy (L1–L5)

Migrate donuts/bars/ring on Home → ECharts; delete Home SVG usage.

### Phase 3 — Explorer (L6) — **UI-5.1 (next after Home approval)**

Health distribution bar + filter wiring. Same PA ECharts theme/sizing/gradients/tooltips; remove SVG after verify.

### Phase 4 — Project Detail (L7–L11) — **UI-5.2**

WP charts + factor bars + score trends → ECharts (re-evaluate types; 3-series project trend stays multi-line). Remove legacy after verify.

### Phase 5 — Portfolio (L12–L16) — **UI-5.3**

Portfolio Analytics distributions → ECharts with same principles as Home tiles; no SVG left on page.

### Phase 6 — Executive (L17–L18) — **UI-5.4**

Workspace comparison bars → ECharts.

### Phase 7 — Cleanup

Delete unused legacy components + Nightingale if still unused; docs; visual sweep dark/light/desktop/mobile.

---

## 6. Architecture confirmation

| Statement | Confirmed |
|-----------|-----------|
| Final chart system is **ECharts only** | Yes |
| Final UI chrome is **SCSS / `--pa-*`** | Yes |
| No indefinite SVG + ECharts dual system | Yes — UI-5 removes replaced legacy |
| No blind 1:1 fancy chart types | Yes — type chosen per analytical fit |
| PA theme, sizing, viz colors, motion, reduced-motion | Mandatory for every migration |
| Homepage default = Average Health + drivers (not N project lines) | Approved direction; **not yet implemented** |
| UI-5 implementation | **Not started — awaiting approval** |

---

## 7. Counts

| Category | Count |
|----------|-------|
| Legacy SVG chart instances (page usages) | **15** (L1–L9, L11–L18; L11 = trend) |
| Legacy CSS factor-bar instances | **3** (L10 × Health/Risk/Attention) |
| Already ECharts on pages | **2** (Home line — needs rethink; Home severity bar — keep) |
| Pages with zero charts | Reports, Settings, Workspaces, auth, legal, portfolio list |

---

## 8. Stop

Awaiting your approval of:

1. This inventory & proposed ECharts types  
2. Migration order (Phases 0–7)  
3. Backend workspace trends API for Average Health  

**No UI-5 code until you approve.**
