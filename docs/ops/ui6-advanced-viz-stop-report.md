# UI-6 — Advanced Analytics Viz — Stop Report

**Status:** Complete — **no new chart types shipped** (data-warranted gate)  
**Date:** 2026-08-19  
**Approach (approved):** Audit first; add at most 1–2 viz only where they clearly beat existing bars/donuts/lines. May conclude nothing to add.

**Not done:** Radar / Scatter / Gauge primitives or page wiring. Chart ECharts foundation and page migrations remain as-is from UI-4/UI-5.

---

## Candidates evaluated

| Candidate | Verdict | Why |
|-----------|---------|-----|
| **Radar** (project factor profile) | **Not warranted** | Exactly **3** factors per score; scales differ across Health/Risk/Attention; horizontal `pa-bar` already compares magnitudes + long labels better |
| **Scatter** (Health × Risk) | **Not clearly warranted** | Fields exist on explorer rows, but typical N is small (~7 in demo); Explorer table + band bars already triage; needs denser clouds to beat current UX |
| **Gauge** (completion / scores) | **Not warranted** | Explicit prior gate: prefer **donut** for %; Home + Project already use donuts with center % |
| **KPI evolutionary ↑↓** | **Not warranted as UI-6** | `deltaDirection` / `deltaLabel` exist on KPI cards but **zero call sites**; real Δ already on Home Average Health subtitle + ranked drivers; generic KPI period deltas need new backend aggregates |

---

## Data map (confirmed available)

| Need | Available | Where |
|------|-----------|--------|
| Factor breakdown | Yes (3 per score) | Project dashboard `analytics.*.factors[]` |
| Health / Risk / Attention points | Yes | Explorer rows API |
| Completion % | Yes | Already visualized as donut |
| Aggregate Health history + project Δ | Yes | `…/health-trends` + Home drivers |

---

## Implementation

**None.** No new shared chart components. No page template changes. No metric/API changes.

This closes UI-6 under the data-warranted rule rather than shipping unused radar/scatter/gauge wrappers “for later.”

---

## Intentionally deferred / revisit later

| Item | When to reopen |
|------|----------------|
| Health × Risk scatter on Explorer | Production workspaces routinely have **dozens+** projects and triage needs a quadrant |
| Factor radar | Only if factor model expands to many comparable spokes **and** product wants profile shape over magnitude bars |
| Gauge | Do not reopen without overturning the donut-for-% gate |
| KPI card deltas | After backend exposes previous-period KPI fields (not Average Health alone) |

---

## Verification

| Check | Result |
|--------|--------|
| Code changes for new viz | **None** |
| Existing pages / ECharts | Untouched |
| Build | Not required (no code); workspace still builds from prior Final Cleanup |

---

## STOP

UI-6 complete with **zero** advanced chart additions. Do not invent viz to fill the phase. Next work only on explicit request (e.g. N5/M17).
