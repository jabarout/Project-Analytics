# N4 Correction — Scroll / Reveal Motion — Stop Report

**Status:** Complete — awaiting approval before N5/M17  
**Date:** 2026-08-19  
**Scope:** Visual/UX rethink of N4 scroll reveals only. UI-7 interaction motion untouched. ECharts untouched.

---

## 1. Research summary (before code)

| Source / pattern | Takeaway |
|------------------|----------|
| AOS defaults | ~**400ms** duration; fade-up is the restrained default |
| Product design systems (e.g. Verdigris) | Reveal often **~500ms** + **ease-out**; translate ~20–30px for slide-up |
| Interaction writing (e.g. medium IO notes) | Micro-UI ~250ms; scroll reveals need **longer to be perceived**; prefer gentle fade + small slide over drama |
| Premium dashboard practice | Animate **composed sections**, not every leaf control; once-only; don’t leave on-screen content invisible |

**Why the first N4 feel failed:** 180ms + 6px reads as a glitch/pop, not a settle. Nested reveals (section + KPI children + chart children) multiplied micro-pops.

---

## 2. Recommended PA model (approved)

| Parameter | Before | After |
|-----------|--------|--------|
| Duration | ~180ms | **480ms** (`--pa-reveal-duration`) |
| Distance | 6px | **18px** (`--pa-reveal-distance`) |
| Easing | generic ease-out | **`cubic-bezier(0.22, 1, 0.36, 1)`** — smooth settle |
| Stagger | 40ms / cap 120ms | **80ms / cap 240ms** (card grids only) |
| Trigger | early micro-threshold | **14% visible**, rootMargin bottom **−12%** |
| Above / near fold | animated anyway | **Instant present** (no motion) if top < **112%** of viewport |
| Composition | many nested reveals | **One reveal per composed section** |

Charts: still **container reveal only**; ECharts owns series motion.

Reduced motion: duration/distance → 0; all targets instant-visible; no translate/stagger.

---

## 3. Placement audit (trimmed)

| Change | Why |
|--------|-----|
| Home: keep **section-level** reveals only | Removed nested KPI/chart pops |
| Explorer: **results** only | Removed filter-panel + nested chart reveals (controls stay immediate) |
| Project: removed first KPI row reveal | Above-fold scores stay present; WP block / factors / trend / tables / reco remain |
| Portfolio: removed lone title reveals | Titles don’t animate without their content |
| Chart grids: single container (no child stagger) | One composed chart block entering |
| Card grids (portfolio list, factor cards, workspace list) | Keep mild stagger where composition benefits |

---

## 4. Verification

| Check | Result |
|--------|--------|
| Tokens (motion) | 480ms / 18px / settle easing |
| Tokens (reduced) | 0ms / 0px |
| Desktop Home load | Near-fold sections **instant**; deeper sections **pending** then reveal on scroll |
| Mobile Home load | First near-fold section instant; rest reveal on scroll |
| After full scroll | **0** pending leftovers |
| Reduced motion | All instant / visible |
| Build | Green |
| Artifacts | `/tmp/pa-n4-correction/` |

---

## STOP

N4 correction complete. **Do not start N5/M17** until you approve the corrected scroll-reveal feel.
