# UI-8 — Final UI/UX Audit — Stop Report

**Status:** Complete — awaiting approval  
**Date:** 2026-08-19  
**Scope:** Final consistency, accessibility, responsive, and visual polish only.  
**Not started:** UI-6 · UI-7 · Final Cleanup (Executive redirects / legacy chart deletion)

Screenshots: `/tmp/pa-ui8-audit/screenshots-after/`  
Pre-fix findings: `/tmp/pa-ui8-audit/findings.json`

---

## Issues found (audit)

### Fixed in UI-8

| Area | Finding | Fix |
|------|---------|-----|
| **Buttons** | Login submit/`auth__link`, Portfolio Create, membership Select/Clear, Portfolio Remove still off `pa-btn` | Migrated to `pa-btn--primary` / `--secondary` / `--ghost` as appropriate; Login tabs aligned with `.pa-segment` |
| **Responsive** | Connections mobile ~17px page overflow (card/actions) | `min-width: 0`, grant field width constraints, actions stretch |
| **Responsive** | Portfolio membership 7-col table lacked scroll wrapper | `.detail__table-scroll` + `overflow-x: auto` |
| **Responsive** | Portfolio health-summary no wrap | `flex-wrap: wrap` |
| **A11y labels** | Connections grant email placeholder-only | Wrapped in labeled field |
| **A11y labels** | Explorer saved-view input unnamed | Visible “Saved view name” label |
| **A11y labels** | Filter panel Health/Progress/Risk min/max | `aria-label` on each input |
| **Focus** | Login fields + `.pa-field` used `:focus` | Switched to `:focus-visible` |
| **Contrast chrome** | Home workspace chips `1px` border vs 2px system | `2px` border |

### Confirmed healthy (no change)

| Check | Result |
|--------|--------|
| Legacy charts on pages | **0** (files remain on disk) |
| Oversized ECharts shells (>400px) | **0** |
| KPI value overflow (sweep) | **0** |
| Legal/Privacy/Terms/Contact layout | OK (draft copy intentional) |
| Chart engine on migrated pages | ECharts only (`pa-bar` / `pa-donut` / `pa-line`) |
| Executive product redirects | **Not implemented** (option C still locked) |
| Dark mode default | Unchanged |

---

## Issues intentionally deferred

### → UI-6 (advanced viz)
- New chart types (radar / scatter / gauge)
- Any “more viz” beyond existing ECharts surfaces

### → UI-7 (motion)
- Non-chart motion system / micro-interaction extras
- Gating hover `filter: brightness` under `prefers-reduced-motion` (tokens already zero durations; residual brightness filters only)

### → Final Cleanup
- Delete unused legacy chart components (+ relocate `BarChartDatum` off legacy bar module)
- Delete unused `pa-nightingale-chart`
- Implement Executive **option C** redirects (0 / 1 / 2+ workspaces)
- Remove global dual `button.secondary` / `linkish` CSS after Login fully on `pa-btn` (Login now uses `pa-btn`; leftover global rules still harmless for back-compat)
- Legal/N5 production copy replacement

### Left as acceptable / low risk
- KPI card `overflow: hidden` + line-clamp (intentional literacy clipping; values wrap)
- Attention/Explorer tables that scroll inside widgets (not page overflow)
- KPI info “i” remains title/tooltip span (not a separate keyboard disclosure widget)

---

## Verification

| Check | Result |
|--------|--------|
| `ng build` | Green |
| Post-fix matrix | Login, Privacy, Home, Explorer, Project, Portfolios, Portfolio, Reports, Connections, Settings, Executive × dark/light × mobile(375)/desktop |
| Naked buttons / unlabeled inputs / page overflowX / legacy charts / oversized charts | **0 issues** |

---

## STOP

UI-8 final audit fixes are done. **Do not continue** to UI-6, UI-7, or Final Cleanup until you approve and choose the next phase.
