# UI-5 follow-up — Chrome Button Consistency — Stop Report

**Status:** Complete — awaiting approval  
**Date:** 2026-08-19  
**Scope only:** Explorer · Executive · Reports · Connections · Settings (+ Explorer filter panel shared control)  
**Not in scope:** UI-6, Executive redirects, legacy chart deletion, charts/ECharts, IA/nav, Login/auth, Portfolios/Home/Project (already on `pa-btn`)

---

## Audit (before changes)

### Established system (canonical)

| Class | Role |
|-------|------|
| `pa-btn pa-btn--primary` | Filled inverse (dark: white / light: near-black) |
| `pa-btn pa-btn--secondary` | Bordered surface control |
| `pa-btn pa-btn--outline` | Transparent + strong border |
| `pa-btn pa-btn--ghost` | Quiet text action |
| `pa-btn pa-btn--danger` | Destructive (semantic red only) |
| `pa-btn--sm` | Dense card / table actions |
| `pa-btn-reset` | Opt-out for sort headers / tabs / chips |

### Conflicting patterns found on target pages

| Pattern | Where | Problem |
|---------|--------|---------|
| Bare `<button>` | Explorer Save/Export; Executive Refresh; Connections Sync/Grant; Settings submit | Relied on dual global `button:not(.pa-btn)` rules — same look *sometimes*, but **not** the explicit shared grammar Home uses |
| `class="secondary"` / `danger` / `linkish` | Executive, Reports, Connections | Parallel legacy modifiers (`button.secondary` etc.) alongside `.pa-btn--*` |
| `class="danger secondary"` | Connections Revoke | Conflicting modifiers |
| `class="explorer__link"` on Export | Explorer | Link-ish class on a real button |
| Page-local SCSS | Connections / Settings | Layout-only (`justify-self`, `align-self`) — **kept**; no visual overrides fighting `pa-btn` |

### Intentionally preserved (not converted to filled pills)

| Control | Treatment |
|---------|-----------|
| Explorer table column sort | `pa-btn-reset` (unchanged) |
| Report Download (row action) | `pa-btn pa-btn--ghost pa-btn--sm` (quiet, not primary) |
| Connections Cancel / “Use API key instead” | `pa-btn pa-btn--ghost` |
| Disconnect / Revoke | `pa-btn pa-btn--danger` (+ `--sm` in cards) |

Global legacy `button.secondary` / `linkish` rules **left in place** for Login/auth (out of scope). Target pages no longer depend on them.

---

## Mapping applied

### Explorer
| Control | After |
|---------|--------|
| Save view | `pa-btn pa-btn--secondary` |
| Export CSV | `pa-btn pa-btn--secondary` |
| Go to Connections (empty) | `a.pa-btn pa-btn--secondary` |
| Reset filters (shared panel) | `pa-btn pa-btn--outline` + layout stretch |
| Sort headers | `pa-btn-reset` (unchanged) |

### Executive
| Control | After |
|---------|--------|
| Refresh | `pa-btn pa-btn--secondary` |
| Export CSV | `pa-btn pa-btn--secondary` |

### Reports
| Control | After |
|---------|--------|
| Refresh history | `pa-btn pa-btn--secondary` |
| Generate | `pa-btn pa-btn--primary` |
| Download | `pa-btn pa-btn--ghost pa-btn--sm` |

### Connections
| Control | After |
|---------|--------|
| Connect with OAuth / Open sign-in / Sync | `pa-btn pa-btn--primary` (+ `--sm` on card Sync) |
| Connect with API key / Save name / Grant | `pa-btn pa-btn--secondary` (+ `--sm` in cards) |
| Cancel / Use API key | `pa-btn pa-btn--ghost` |
| Disconnect / Revoke | `pa-btn pa-btn--danger pa-btn--sm` |

### Settings
| Control | After |
|---------|--------|
| Save preferences | `pa-btn pa-btn--primary` |

---

## Verification

| Check | Result |
|--------|--------|
| `ng build` | Green |
| Naked buttons on 5 pages (excl. `pa-btn-reset`) | **0** (dark/light × desktop/mobile) |
| Focus-visible ring | Present (dark: white; light: near-black) |
| Disabled opacity | **0.45** (Grant access, Save view when empty) |
| Dark primary contrast | White fill / dark text — not black-on-black |
| Dark secondary | Surface `#26262e` + strong border + white text |
| Danger | Semantic red fill, white text |
| Screenshots | `/tmp/pa-btn-consistency/screenshots/` |

Matrix: all 5 pages × dark/light × desktop/mobile.

---

## Out of scope / deferred

- Login / auth link buttons (still `auth__link` / submit grammar)
- Portfolios list create button (not in this phase’s page list)
- Removing global dual `button.secondary` CSS entirely (safe after Login migrates)
- UI-6, Executive option C redirects, legacy chart file deletion

---

## STOP

Chrome button consistency for the listed pages is done. **Do not continue** to UI-6 / cleanup / redirects until you approve and choose the next phase.
