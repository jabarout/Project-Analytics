# Home — Synthesis-first IA + legend containment

**Date:** 2026-08-20  
**Status:** Done — STOP  
**Scope:** Home only

## What shipped

1. **Section order:** Synthesis → Overview → Visual analytics → Exception queue → Recommendations.
2. **Sticky nav** (`homeSections`) matches that order; default / workspace-load `activeSectionId` = `home-synthesis`.
3. **Hover copy:**
   - Synthesis Average Health / Risk: detailed (mean + summary + thresholds + factor detail).
   - Overview Critical: short band/count only; points to Average Health above.
   - Overview Needs Attention: compact definition; references Health/Risk above (no factor essay repeat).
4. **Legend containment:** `.pa-charts > .home__chart-block` exempt from global `max-height` clamp; legends/notes `flex-shrink: 0` in normal flow so they reserve their own height.
5. **paReveal:** unchanged tokens; sections still each have `paReveal`.

## Verify (Playwright, `localhost:4200`, admin / Admin123!)

| Check | Result |
|-------|--------|
| Nav order | Pass |
| DOM section order / Synthesis above Overview | Pass |
| Nav click → correct section + active chip | Pass |
| Legend overlap (dark desktop + light mobile) | Pass (`overlapsNeighbor: false`) |
| Chart-block `max-height: none`, legends below charts | Pass |
| Average Health aria/title detailed | Pass |
| Critical info short (no three-signal essay) | Pass |

Artifacts: `/tmp/pa-home-synthesis/report.json`, screenshots under `/tmp/pa-home-synthesis/screenshots/`.

## Out of scope (held)

- N5 / M17  
- Other page IA  
- Metric formula changes  
