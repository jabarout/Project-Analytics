# Executive page — product direction (locked)

**Status:** Locked product direction — **implemented in Final Cleanup (2026-08-19)**  
**Date:** 2026-08-19 (updated on implementation)  
**Supersedes (for IA behavior):** Informal “keep Executive forever” and the M11A handoff note that implied unconditional deprecate/redirect, refined here to **option C** (conditional keep).

Frozen M11A (`20_M11A_Product_Experience_Specification.md`) remains the PE baseline: no separate Executive *product*, single primary persona, multi-workspace roll-up belongs with Home when needed.

---

## Product model (reminder)

| Rule | Meaning |
|------|---------|
| **1 OpenProject URL → 1 PA workspace** | Hard constraint |
| **1 PA user → N workspaces** | Allowed (multiple OP connects and/or grants) |
| **Common case** | One connected OP instance → one workspace |

---

## Locked behavior for `/executive`

| Workspaces the user can access | Intended behavior |
|--------------------------------|-------------------|
| **0** | Redirect to **Home** (connect / empty flow) — Executive has nothing to show |
| **1** | Redirect to **Home** — Executive adds no value over workspace-level Home |
| **2+** | **Show Executive** as the **cross-workspace overview** (compare scopes across OP instances) |

Rationale: preserve support for users managing multiple OpenProject instances **without** maintaining a redundant dashboard for the common single-workspace case.

---

## Implementation (Final Cleanup)

| Workspaces | Behavior |
|------------|----------|
| **0** | `replaceUrl` navigate to `/` (Home connect empty-state) |
| **1** | `replaceUrl` navigate to `/` (single-workspace Home) |
| **2+** | Render Executive cross-workspace overview |

Gate runs on every visit to `/executive` via `listWorkspaces()` before loading the executive dashboard. Direct URL navigation follows the same rule. Page remains **off primary nav**.

Home multi-workspace glance strip remains a separate optional enhancement (not required for Option C).

---

## Related

- Analysis context: UI-5.4 stop + product Q&A (2026-08-19)  
- M11A: Executive not in primary nav; multi-ws glance on Home when count > 1  
- Final Cleanup stop report: `docs/ops/final-cleanup-report.md`
