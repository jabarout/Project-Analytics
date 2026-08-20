# UI-5.0 Home — section navigation UX — Stop Report

**Status:** Complete — **not** starting UI-5.1  
**Date:** 2026-08-19

## Mapping (existing sections only)

| Nav label | Existing Home section |
|-----------|------------------------|
| Overview | Overview KPIs |
| Visual analytics | Visual analytics (Average Health, drivers, charts) |
| Synthesis | Synthesis KPIs |
| Exception queue | Exception queue + Top Needs Attention |
| Recommendations | Recommendations list |

No section rename/reorder of content — only anchors + navigator.

## Behavior
- Sticky horizontal pill nav (PA `.pa-segment` / `pa-btn-reset` grammar)
- Click → smooth scroll (`prefers-reduced-motion` → instant)
- IntersectionObserver scroll-spy updates active pill
- Mobile: horizontal scroll track (no cramped wrap)

## Verification
| Check | Result |
|--------|--------|
| Build | Green |
| Dark desktop | Sticky; click Visual analytics → active; scroll → Recommendations active |
| Light mobile | Same |
| IA | Unchanged section titles/content |

## STOP
Awaiting final UI-5.0 approval before UI-5.1 Explorer.
