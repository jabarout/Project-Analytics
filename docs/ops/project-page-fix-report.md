# Project page — KPI copy, spacing, factor tooltips

**Date:** 2026-08-20  
**Status:** Done

## Shipped

1. **KPI descriptions** — Health / Risk / Needs Attention use full glossary (summary + thresholds + detail) plus this-project explanation. WP delivery KPIs get short infoBodies.
2. **Removed** Completed vs remaining donut chart.
3. **Spacing** — `.project__wp-delivery` column gap so KPI row and charts don’t touch; larger page gap + trend/overdue spacing.
4. **Factor hovers** — short axis labels (Schedule, Incomplete work, From Health, …); full factor sentence on hover via `detail` + tooltip `appendTo: 'body'` (escapes chart shell `overflow: hidden`).

## Verify

Playwright on a live project: chart removed, gaps ≥ 12/16px, Health KPI detailed, factor tooltips show full sentences (e.g. “Schedule dates incomplete…”, “Attention contribution from health…”).

Artifacts: `/tmp/pa-project-fix/report.json`
