# N4 — Scroll / Reveal Motion — Stop Report

**Status:** Complete — awaiting approval before N5/M17  
**Date:** 2026-08-19  
**Name:** N4 / Scroll–Reveal Motion (distinct from completed UI-6 advanced-viz audit and UI-7 interaction motion)

**Not done:** N5 deploy package · UI-6 chart types · ECharts internals · IA/metrics/OAuth changes

---

## Approach

| Decision | Choice |
|----------|--------|
| Mechanism | One shared Angular standalone directive: `PaRevealDirective` (`[paReveal]`) |
| Detection | `IntersectionObserver` (unobserve after first reveal — no repeat flicker) |
| Motion | Opacity 0→1 + `translateY(6px)`→0 over `--pa-motion-enter` (~180ms) |
| Stagger | Optional `[paRevealStagger]="true"` on groups: 40ms steps, **capped at 120ms** |
| Reduced motion | Immediate visible state; **no translate, no stagger, no transition** |
| Charts | Reveal **containers** only; ECharts series animation unchanged |

---

## Files

| File | Role |
|------|------|
| `shared/directives/pa-reveal.directive.ts` | **New** shared viewport reveal |
| `styles.scss` | `.pa-reveal` / `.pa-reveal--visible` + reduced-motion + host blockify |
| Feature pages (imports + template attrs) | Home, Explorer, Project, Portfolio list/detail, Reports, Connections, Settings, Executive |

---

## Where applied

| Page | Reveal targets |
|------|----------------|
| **Home** | Sections (overview/visual/synthesis/exceptions/recommendations); KPI rows staggered; chart grids staggered |
| **Explorer** | Filter panel host; results; health chart container |
| **Project** | KPI rows; WP delivery block; WP chart grid; factor score-grid; trend container; overdue/bottleneck blocks; recommendations |
| **Portfolio detail** | Health summary; KPI rows; chart grid; member intelligence; membership block |
| **Portfolio list** | Create form; card grid (stagger) |
| **Reports** | Generate form; history |
| **Connections** | Connect form; workspace card list (stagger) |
| **Settings** | Profile strip; preferences form |
| **Executive** | KPI row; charts; workspace cards; attention/insights/recommendations |

Sticky Home section nav / page headers: **not** revealed (stay stable).

---

## Verification

| Check | Result |
|--------|--------|
| `ng build` | Green |
| Dark desktop motion | All targets visible after scroll (e.g. Home 23/23) |
| Dark desktop reduced-motion | All visible immediately; no hidden leftovers |
| Light mobile motion | Same — 0 still-hidden |
| Light mobile reduced-motion | Same |
| Repeat scroll | Once-only unobserve — no layout jump / re-trigger flicker |
| Overflow / page errors | None in sweep |
| Artifacts | `/tmp/pa-n4-reveal/` |

---

## STOP

N4 scroll/reveal is complete. **Hold N5/M17** until you approve this phase.
