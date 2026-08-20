# Portfolio — Progress-first IA + chart legend fusion

**Date:** 2026-08-20  
**Status:** Done  
**Scope:** Portfolio detail Analytics tab

## Shipped

1. **Section order:** Progress & delivery quality → Overview → Visual analytics → Member intelligence (no sticky nav).
2. **Hover strategy (aligned with Home):**
   - Avg health / Avg risk at top: detailed (mean + summary + thresholds + factors).
   - Overview Critical health / Needs Attention: shortened; point to averages above.
3. **Charts removed:** Risk distribution, Needs Attention split.
4. **Remaining charts** (Health distribution, Actual progress bands, Overdue work packages) use fused `shellFooter` legends with color swatches (same pattern as Home).
5. **Shared styles:** `.pa-chart-legend*` + `.pa-chart-block` moved to global `styles.scss` for Home + Portfolio.

## Verify (Playwright)

| Check | Result |
|-------|--------|
| Progress before Overview | Pass |
| No Risk / Needs Attention charts | Pass |
| Three remaining charts present | Pass |
| Legends in shell footers | Pass |
| Avg health detailed / Critical short | Pass |

Artifacts: `/tmp/pa-portfolio-restructure/report.json`, screenshots under `/tmp/pa-portfolio-restructure/screenshots/`.
