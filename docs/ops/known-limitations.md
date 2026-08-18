# Known limitations (buyer-honest)

Last updated: 2026-08-18 (Community KPI adaptation)

These are **accepted** constraints of the current product — not secret bugs.  
Fix targets are noted when planned.

---

## Integration

| Limitation | Detail | Target |
|------------|--------|--------|
| OpenProject auth | OAuth preferred: per-workspace OAuth client id/secret (entered at connect) + encrypted access/refresh tokens. Global `OPENPROJECT_OAUTH_CLIENT_*` optional local default only. API-key alt remains. | Each OP admin still creates an OAuth app manually (no DCR) |
| One credential style | UI stores workspace URL only; key is process env | M14 |
| No PA self-signup | Seed admin only until M14a | **M14a** |
| Workspace membership isolation | Phases 1+6: analytics APIs gated by membership; Workspace Admin grant/revoke by PA email on Connections. Promote-to-admin not in v1. | OAuth (Phase 7); optional admin promote later |
| Connect / credentials | Workspace Admin may rotate credentials; legacy URL-only create disabled; env API-key fallback off in **prod**; OAuth + API-key connect paths share eligibility. | Multi-instance sticky not required (OAuth state in DB) |
| **Synchronize vs Recalculate** | **Synchronize** (Connections) pulls from OpenProject and **removes** local projects/WPs deleted remotely. **Recalculate** (Home) only recomputes scores from **local** data — it does not contact OpenProject. | By design |
| Full catalog sync | Sync fetches the full project/WP catalog (not incremental-only) so deletions are detected | Performance note; correctness first |
| **No project start/end dates (Community API)** | Expected progress, progress gap, schedule variance, and “delayed by project end date” are **not shown** on Home/Project/Portfolio (or KPI reports). Dashboards use WP completion, overdue WP dues, priorities, and Health/Risk/Attention instead. | Product adaptation for Community |
| **Active ≠ status title** | OpenProject `active` means not archived. Local status may be “On track” / “At risk” / etc. **Active projects** counts non-archived members — not only the literal string `ACTIVE`. | By design |

---

## Analytics

| Limitation | Detail | Target |
|------------|--------|--------|
| Budget variance usually null | No spent-budget field in local model | When OP spend is modeled |
| Expected progress / gap null | Requires usable project start **and** end dates — **not shown** on Community dashboards | Data quality in OP / Enterprise dates |
| Progress prefers WP completion | OP project progress field ignored when WPs exist (by design) | Documented M12 |
| Schedule variance (project end) | Backend may compute when end date exists; Community UI uses WP overdue instead | Prefer overdue WPs |
| Trends are snapshot history | Limited soft intelligence | Optional M18 |

---

## Access & multi-user

| Limitation | Detail | Target |
|------------|--------|--------|
| Analytics access grants | Grant/revoke by email on Connections (Workspace Admin only). No promote-to-admin in v1. | Optional admin promote; OAuth |
| Dev seed admin | `admin` / documented demo password — **must change/disable for real deploy** (see security-checklist) | M17 / ops |

---

## Product experience

| Limitation | Detail | Target |
|------------|--------|--------|
| UI polish deferred | Functional PE; not final visual craft | **M16** |
| Home is classic triage | Not a second Project Detail | By design |
| Execution stays in OpenProject | No boards/task editing here | Vision freeze |

---

## Ops

| Limitation | Detail | Target |
|------------|--------|--------|
| Local/dev-oriented package | Compose + scripts; not managed SaaS | **M17** |
| OTel tracing deferred | Micrometer metrics present | Later |

---

## Not planned for ~$2k SKU

- Multi-tenant SaaS billing  
- Mobile app  
- Plugin marketplace  
- Non-OpenProject PM integrations  
- Full predictive AI suite  
