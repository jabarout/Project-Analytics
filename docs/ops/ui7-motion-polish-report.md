# UI-7 — Non-chart Motion / Interaction Polish — Stop Report

**Status:** Complete — awaiting approval  
**Date:** 2026-08-19  
**Scope:** Non-chart chrome motion only. ECharts / chart sizing / viz colors untouched.  
**Not started:** UI-6 · Final Cleanup (Executive redirects / legacy chart deletion)

Screenshots / machine report: `/tmp/pa-ui7-verify/`

---

## Audit (before)

| Area | Gap |
|------|-----|
| Home workspace chips / section nav | Hover states existed; **no transitions** |
| Shell nav | Color/bg transition only; weak active affordance; no press/focus polish |
| KPI clickable cards | Used `filter: brightness` (not ideal under reduced-motion) |
| Portfolio / Executive cards | Instant border change; no hover lift |
| Empty states | Static appear |
| Inputs/selects | No border transition |
| Segmented controls | Transition incomplete for press scale |
| Reduced-motion | Tokens zeroed, but transforms/filters/enter animations not fully gated everywhere |

No dialogs/modals exist in the app (native `confirm` only) — N/A.

---

## Implemented (restrained)

| Surface | Change |
|---------|--------|
| **Tokens** | Added `--pa-motion-enter: 180ms` + `--pa-ease-out`; reduced-motion zeros all three |
| **Buttons / segments** | Press scale `0.985`; segment transitions include transform; danger brightness gated under reduced-motion |
| **Inputs** | Border/background transition on focus |
| **Shell nav** | Border + press + focus-visible; active gets subtle border |
| **Home chips / section nav** | Full color/border/transform transitions + press |
| **KPI cards** | Hover: 1px lift + soft shadow (no brightness filter); press scale; reduced-motion clears transform/shadow |
| **Portfolio / Executive cards** | Hover lift + shadow; reduced-motion safe |
| **Connections cards** | Border hover transition |
| **Recommendations** | Subtle hover border/shadow |
| **Empty state** | `.pa-enter` (fade-up 4px / 180ms) |
| **Page content** | `.pa-page-enter` uses enter token + ease-out |
| **Footer links** | Color transition |

Durations stay short (120–180ms). No bounce, no large travel, no second chart animation system.

---

## Verification

| Check | Motion on | Reduced-motion |
|--------|-----------|----------------|
| Tokens | 120 / 160 / 180 ms | **0 / 0 / 0 ms** |
| KPI hover | translateY ≈ −1px + soft shadow | transform/shadow **none** |
| Button `:active` | scale ≈ 0.985 | **none** |
| Page enter | `pa-fade-in` 0.18s | **none** |
| Chip/nav transitions | 0.12s | **0s** |
| Build | Green | — |
| Themes × viewports | Dark/light desktop + mobile screenshots | Same |

---

## Deferred

| Bucket | Items |
|--------|--------|
| **UI-6** | Advanced viz types |
| **Final Cleanup** | Option C redirects; delete legacy chart components |
| **Out of scope** | Chart ECharts animations (already UI-4/5); Angular route animation package; custom modal system |

---

## STOP

UI-7 complete. **Do not start UI-6 or Final Cleanup** until you approve and choose the next phase.
