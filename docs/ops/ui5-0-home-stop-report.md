# UI-5.0 — Home batch — Stop Report

**Status:** Complete — awaiting approval before Explorer / next batch  
**Date:** 2026-08-19  
**Scope:** Home only (visualization + supporting read API). No IA / auth / metric formula changes.

---

## Architecture decision: workspace trends API

**Needed:** Yes — client fan-out of per-project trends does not scale to large workspaces.

**Endpoint:** `GET /api/v1/analytics/workspaces/{workspaceId}/health-trends`

**Metric definition (unchanged):**
- Per wave: equal-weight mean of project `health_score` values (scale 2, `HALF_UP`)
- Same aggregation as existing `averageHealthScore` KPI in `buildScopeKpis`
- Waves clustered by truncated UTC minute (aligned with workspace recalculate)
- Drivers: `last − first` Health per project (≥2 snapshots); top 5 improving / worsening

---

## What shipped on Home

| Item | Implementation |
|------|----------------|
| Primary | ECharts **Average Health over time** (single series) |
| Secondary | **Health drivers** (Improving / Worsening, semantic colors) |
| Health distribution | ECharts donut (replaced SVG) |
| Progress distribution | ECharts bar (replaced SVG) |
| Needs Attention split | ECharts **bar** (2-way compare; not donut) |
| Overdue WP split | ECharts bar (replaced SVG) |
| Recommendation severity | ECharts bar (already migrated) |
| Average progress | ECharts **donut** Complete vs Remaining (not a gauge — proportion of the same KPI %) |

**Legacy SVG on Home:** `app-bar-chart` / `app-donut-chart` / `app-ring-metric` usages **removed** (0 remaining on Home).

**Not done (next batches):** Explorer, Project, Portfolio, Executive.

---

## Chart-type choices (Home)

| Viz | Chosen | Why |
|-----|--------|-----|
| Average Health history | Line | Trend over time |
| Drivers | Ranked list | Explains aggregate; not spaghetti lines |
| Health bands | Donut | Part-to-whole + center total |
| Progress / Needs / Overdue / Severity | Bar | Category count comparison |
| Average progress | Donut (complete/remaining) | Single % as proportion — not gauge chrome |

---

## Verification

- Frontend build green
- Backend compile + restart; API returns waves with `averageHealthScore` / `sampleSize` matching KPI (e.g. latest 62.6, n=7)
- Playwright Home dark/light: Average Health line canvas, drivers present, **0** SVG chart components, ECharts bars/donuts present

---

## STOP

**Do not continue to Explorer / Project / Portfolio / Executive until you approve this Home batch.**
