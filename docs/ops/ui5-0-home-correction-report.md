# UI-5.0 Home correction — Stop Report

**Status:** Complete — **not** starting UI-5.1  
**Date:** 2026-08-19

## Fixes

### 1. Donut center content
- Replaced unreliable ECharts `graphic` / pie-label centering with an **HTML overlay** locked to the same origin as `series.center` (`50%` / `46%`).
- Legend moved **below** the pie so the hole stays geometrically centered.
- Center stays above the series (`z-index`), readable in dark/light, and tracks resize.
- Average progress hole shows the **KPI %** via `centerValue` (not slice sum 100).

### 2. Selective gradients
- Donut slice fills: diagonal color gradients + soft glow
- Bar fills: richer vertical gradients + shadow
- Line area fills: stronger under-curve gradient
- Chart shells: dual-angle surface gradients; featured hero keeps viz-tint wash
- Chrome remains monochrome

### 3. Home architecture unchanged
- Average Health over time + ranked Improving/Worsening drivers retained

### 4. Migration plan (still explicit for later batches)
Documented in `docs/ops/ui5-legacy-chart-inventory.md`:
- **UI-5.1** Explorer  
- **UI-5.2** Project Detail  
- **UI-5.3** Portfolio Analytics  
- **UI-5.4** Executive  

Same principles: ECharts only after verify, type chosen by data, PA theme/sizing/colors/gradients/tooltips/motion.

## Verification
| Check | Result |
|--------|--------|
| Build | Green |
| Legacy SVG on Home | **0** |
| Donut hole (dark/light, desktop/mobile) | Number + label **inside** hole |
| Average Health + drivers | Unchanged concept |

## STOP
Awaiting approval before **UI-5.1 Explorer**.
