# UI-5 — Visual review (cross-page)

**Status:** Complete — awaiting approval before next UI gate  
**Date:** 2026-08-19  
**Scope:** Dark/light × desktop/mobile pass after chart migration. Executive product behavior **not** changed (option C frozen, redirects not implemented). Legacy chart files **not** deleted.

Screenshots: `/tmp/pa-ui5-visual-review/screenshots/`  
Machine report: `/tmp/pa-ui5-visual-review/report.json`

---

## Matrix covered

| Page | Desktop dark/light | Mobile dark/light |
|------|--------------------|-------------------|
| Home | Yes | Yes |
| Explorer | Yes | Yes |
| Portfolio detail | Yes | Yes |
| Project detail | Yes | Yes |
| Executive (frozen) | Yes | Yes |
| Portfolios list, Reports, Connections, Settings | Desktop only | — |

---

## Chart migration health

| Check | Result |
|--------|--------|
| Legacy SVG/CSS charts on any reviewed page | **0** |
| Oversized charts (>400px) | **0** |
| Console page errors (sweep) | **0** |
| Nightingale on pages | **0** (unused component remains on disk) |

ECharts counts (dark desktop): Home 7 · Explorer 1 · Portfolio 5 · Project 7 · Executive 2.

---

## Fixed in this review

| Issue | Fix |
|-------|-----|
| Project Detail **mobile horizontal page scroll** (~45px) from overdue / bottleneck tables | Wrap tables in `.project__table-scroll` (`overflow-x: auto`); verified `scrollWidth === clientWidth` |

---

## Findings deferred (not fixed)

### Button grammar drift (UI-2/UI-3 debt)

Global `button` / `.secondary` / `.danger` / `.linkish` still style many controls, but pages are inconsistent about explicit `pa-btn`:

| Surface | Examples still without `pa-btn*` |
|---------|----------------------------------|
| Explorer | Save view, Export CSV, Reset filters |
| Executive | Refresh, Export CSV |
| Reports | Refresh history, Generate, Download |
| Portfolios list | Create portfolio, Select all / Clear |
| Connections | OAuth / sync / grant / disconnect |
| Settings | Save preferences |

**Home / Portfolio detail / Project** are largely on `pa-btn`. Recommend folding into **UI-8 final audit** (or a small chrome polish batch), not UI-6 viz.

### Executive (product — frozen)

- Still reachable by URL with **1 workspace** → single-bar charts (known; option C redirects **not** implemented yet).
- Off primary nav (correct).
- No redesign in this review.

### Optional later (UI-6+)

- Advanced viz only where data warrants (radar/scatter/gauge) — **not** required to close UI-5.
- UI-7 non-chart motion.
- UI-8: a11y/responsive pass, footer/legal check, **delete unused legacy chart components**, implement option C redirects.

---

## Recommendation

**UI-5 (chart migration + visual review) is ready to close** after your approval.

Suggested next gate (pick one):

1. **UI-8-lean chrome polish** — `pa-btn` consistency on Explorer/Reports/Connections/etc. (high leverage, low risk)  
2. **UI-6** — only if you want advanced viz soon (optional)  
3. **Final cleanup phase** — option C redirects + delete legacy chart components (after you explicitly open that phase)

---

## STOP

Awaiting approval. No Executive redirects and no legacy chart deletion until you open that final phase.
