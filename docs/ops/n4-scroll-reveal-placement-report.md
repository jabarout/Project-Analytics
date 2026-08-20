# N4 Placement Correction — Scroll Reveal Completeness — Stop Report

**Status:** Complete — awaiting approval before N5/M17  
**Date:** 2026-08-19  
**Scope:** Placement/completeness only. Animation model unchanged (480ms / 18px / settle easing / 80–240ms stagger / reduced-motion).

---

## Problem

After the timing correction, Home’s polished reveal effectively started at **Synthesis**, because **Visual analytics** was treated as near-fold and shown instantly.

---

## What changed (placement + gate only)

### Above-fold gate (same directive, same motion tokens)

| Before | After |
|--------|--------|
| Instant if `top < 112%` of viewport | Instant only if `top < 58%` of viewport (true first screen) |
| Near-fold skipped the reveal | Near/below-fold sections use the approved scroll reveal |

Observer threshold left early enough (`threshold: 0.1`, `rootMargin: 0 0 -8% 0`) so sections enter naturally while scrolling.

### Home

| Section | Behavior |
|---------|----------|
| Header / workspace chips / sticky “On this page” nav | No reveal |
| **Overview** | Instant when above-fold (desktop verified) |
| **Visual analytics** | **Scroll reveal** (no longer skipped) |
| Synthesis / Exceptions / Recommendations | Scroll reveal |
| Nested KPI/chart pops | Still removed (section composition) |

### Project Detail

| Block | Behavior |
|-------|----------|
| Crumbs / title / first score KPI row | Immediate (no `paReveal`) |
| WP delivery (KPIs + charts as one block) | Composed reveal |
| Factor score-grid | Composed reveal (**no** child stagger) |
| Score trends container | Composed reveal |
| Overdue / bottlenecks / recommendations | Composed reveals |

### Portfolio Analytical

| Block | Behavior |
|-------|----------|
| Health summary | Reveal (instant when above-fold) |
| Overview KPIs | Composed `detail__flow-section` (no KPI stagger) |
| Progress & delivery | Composed section |
| Visual analytics | Composed section (chart container) |
| Member intelligence | Composed section |
| Attention two-col / insights+recommendations | Composed reveals |
| Membership tab block | Reveal |

---

## Verification

| Page | Load (desktop dark) | After full scroll |
|------|---------------------|-------------------|
| **Home** | Overview **INSTANT**; Visual+later **PENDING** | 5/5 visible, 0 stuck |
| **Project** | First KPIs static; deeper blocks pending then reveal | 6/6 visible |
| **Portfolio** | Health + Overview instant when above-fold; later pending | 7/7 visible |
| Mobile / reduced-motion | Reveals or instant as expected; reduced = all instant | 0 stuck |
| Build | Green | — |

Artifacts: `/tmp/pa-n4-placement/`

---

## Unchanged (by design)

- Reveal duration / distance / easing / stagger caps  
- `PaRevealDirective` architecture  
- UI-7 interactions  
- ECharts series animation  
- Theme / IA / metrics / business logic  

---

## STOP

Placement correction complete. **Do not start N5/M17** until you approve.
