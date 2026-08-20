# UI-5.4 — Executive Dashboard — Stop Report

**Status:** Complete — awaiting approval  
**Date:** 2026-08-19  
**Scope:** Executive Dashboard visualizations only. No IA / metrics / auth changes.

---

## Inventory (before)

| # | Visualization | Legacy impl | Data / meaning |
|---|---------------|-------------|----------------|
| 1 | Average health by workspace | SVG `app-bar-chart` | Per-workspace `averageHealthScore` (labels truncated to 10 chars) |
| 2 | Average attention by workspace | SVG `app-bar-chart` | Per-workspace `averageAttentionScore` |

---

## Replacements

| Legacy | ECharts type | Why |
|--------|--------------|-----|
| Average health SVG bar | **`app-pa-bar-chart`** | Compare scores across workspaces — bar is correct |
| Average attention SVG bar | **`app-pa-bar-chart`** | Same |

**Orientation:** Vertical by default; **horizontal** when >4 workspaces or any name >14 chars (long labels).  
**Value label:** `Score` (not Count).  
**Zeros:** `includeZeros=true` so a 0 average still renders (score axis, not count omit).  
**Colors:** Health → success; Attention → warning (theme-aware).  
**Names:** Full workspace name (no 10-char truncate); ECharts truncates axis labels when needed.

**Legacy on Executive after migration:** **0**.

---

## Shared enhancement

- `pa-bar-chart`: new `includeZeros` input (default `false` keeps count-distribution omit-zero behavior).

---

## Data / API issues

None. Uses existing executive dashboard payload.

---

## Legacy component status (app-wide)

After UI-5.4, **no page imports** remaining for:

| Component | Status |
|-----------|--------|
| `app-bar-chart` | Unused |
| `app-donut-chart` | Unused |
| `app-ring-metric` | Unused |
| `app-trend-chart` | Unused |
| `app-factor-bars` | Unused |

**Not deleted in this batch** — cleanup is a separate approval step.

---

## Verification

| Check | Result |
|--------|--------|
| `ng build` | Green |
| Dark/light × desktop/mobile | Executive loads; **0** legacy charts |
| ECharts present | **2** bars (2 canvases) |
| Chart heights | Desktop ~343px; mobile ~360px (≤380) |
| Screenshots | `/tmp/pa-ui54-verify/screenshots/` |

---

## STOP

**UI-5 chart page migration complete** (Home → Explorer → Project → Portfolio → Executive).

### Product direction locked after this batch (2026-08-19)

See `docs/ops/executive-page-product-direction.md` (**option C**):

| Workspaces | Behavior (intent; **not implemented yet**) |
|------------|-----------------------------------------------|
| 0 | Redirect Home / connect |
| 1 | Redirect Home |
| 2+ | Keep Executive as cross-workspace overview |

**Frozen until UI-5 visual review completes:** no redirects, no Executive redesign, no deletion of Executive or legacy chart components. Cleanup = **separate final phase**.
