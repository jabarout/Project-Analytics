# UI-3 Hard Consistency Gate — Stop Report

**Status:** Complete — awaiting approval before UI-4  
**Date:** 2026-08-19  
**Scope:** Chrome consistency only. Charts / ECharts untouched.

## No contradiction

The UI-3 brief matches the gated plan: eliminate page-local style orphans, keep PA SCSS/`--pa-*` as the sole UI system, do not install ZardUI/Bklit/Tailwind/React/ECharts, stop before UI-4.

## What changed

### Global system
- `.pa-field` input borders aligned to **2px**
- New shared **`.pa-segment`** for pill tabs/pickers (Home workspace picker, Portfolio Analytics/Membership tabs)

### Shared chrome (non-chart)
- empty-state, explorer filter panel, project table, membership picker
- attention table, insight list, recommendation list
- Token borders/radii; reco items use semantic border mixes (no left rails)
- Table sort headers use `pa-btn-reset`

### Pages neutralized
Workspaces, Explorer, Reports, Portfolio list/detail, Settings, Home — removed button/input fights; layout-only page SCSS remains.

### Charts
Untouched (UI-4 gate honored).

## Verification

| Check | Result |
|--------|--------|
| `ng build` | Green |
| Playwright sweep | 46 authenticated/public visits × dark/light × desktop/mobile |
| Card chrome borders | 2px only |
| Button radii (non-reset) | 9999px (pill) only |
| Routes covered | Login, legal, Home, Explorer, Project Detail, Portfolio list/detail, Connections, Reports, Settings, Executive |
| Measured issues | 0 |

## Intentionally page-specific (kept on purpose)

| Area | Why |
|------|-----|
| Page layout grids (`.kpi-row`, `.charts`, form grids) | Composition, not control grammar |
| Login `.auth__tabs` | Segmented auth mode switcher (distinct from app `.pa-segment`) |
| Portfolio `.health-summary[data-band=*]` | Semantic band chrome via `--pa-danger/warning/success` |
| Workspaces OAuth setup dashed box | Onboarding callout |
| Chart component internals | Deferred to UI-4 |

## Remaining page-local SCSS

Still present as **layout shells** (expected):  
`login`, `home`, `explorer`, `workspaces`, `reports`, `settings`, `portfolio-*`, `project-detail`, `executive-dashboard` — all on `--pa-*`, no orphan button/color systems.

## UI-4 readiness

**Yes — chrome is consistent enough to start UI-4**, which should be: complete PA ECharts theme first (no default ECharts palette leak), then chart migration.

**Stopped here. Not starting UI-4 until you approve.**
