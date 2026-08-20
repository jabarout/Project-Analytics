# Home — Metric Semantics & Information Clarity — Stop Report

**Status:** Complete — awaiting approval  
**Date:** 2026-08-19  
**Scope:** Home copy, chart legends/notes, remove two low-value charts. **No section reorder/rename.**

---

## Implemented

### KPI under-titles + info/hover (approved wording)

| KPI | Under-title |
|-----|-------------|
| Active projects | Projects that are not archived in OpenProject. |
| Projects needing attention | Projects with a Needs Attention score of 50 or higher. |
| Critical projects | Projects with Critical Health (Health score below 40). |
| Projects with overdue WPs | Projects with at least one open work package past its due date. |
| Overdue work packages | Open work packages past their due date. |
| Upcoming WP deadlines | Work packages due in the next 14 days. |
| Average progress | Average share of work packages completed across projects. |
| Average health | Average of every project’s Health score (0–100). Higher = healthier delivery. |
| Average risk | Average of every project’s Risk score (0–100). Higher = more delivery risk. |

Hover/info bodies updated in `score-glossary.ts` to explain **how scores are calculated** in plain language (schedule alignment, WP completion, overdue WPs vs project finish date, three distinct Risk factors, Attention composition, how to open Project Detail factors via Explorer).

### Charts

| Chart | Change |
|-------|--------|
| Health distribution | Kept + **band legend** (Critical / Watch / Healthy / Unknown) |
| Progress distribution | Kept + note (WP completion definition + 0–33 / 34–66 / 67–100) |
| Needs Attention split | Kept + legend (≥50 vs &lt;50) |
| Average Progress donut | Kept + clarified subtitle/note |
| **Overdue work packages** | **Removed** |
| **Recommendation severity** | **Removed** |

Progress band labels updated to `0–33% complete` / `34–66% complete` / `67–100% complete` (shared `distribution.ts`).

### Exception queue

Short lede explaining inclusion: Needs Attention, Critical Health, and/or overdue work packages (up to 8, highest Attention first).

### Unchanged

- Section order / sticky nav: Overview → Visual analytics → Synthesis → Exception queue → Recommendations  
- Scoring formulas / business logic  
- IA / layout of sections  

---

## Verification

| Check | Result |
|--------|--------|
| `ng build` | Green |
| Sticky nav labels / section ids | Unchanged |
| Removed charts | Not present |
| Legends / notes / KPI hints | Present on Home |
| Screenshots | `/tmp/pa-home-semantics/screenshots/` |

---

## STOP

Awaiting approval before any further Home layout/viz or N5 work.
