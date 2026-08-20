# Known limitations (buyer-honest)

Last updated: 2026-08-19 (N1 connect/auth soak)

These are **accepted** constraints of the current product — not secret bugs.  
Fix targets are noted when planned.

---

## Integration

| Limitation | Detail | Target |
|------------|--------|--------|
| OpenProject auth | OAuth preferred: per-workspace OAuth client id/secret (entered at connect) + encrypted access/refresh tokens. Global `OPENPROJECT_OAUTH_CLIENT_*` optional local default only. API-key alt remains. | Each OP admin still creates an OAuth app manually (no DCR) |
| **Same OP URL = one PA workspace** | Reconnecting the same `baseUrl` requires disconnect first, or Workspace Admin credential rotate. A second PA user cannot become a second Workspace Admin via connect. | By design (Hybrid) |
| **OP OAuth UX quirks (Community)** | After OP login, 2FA auth stage may drop `back_url` → land on OP home; prior grants often skip consent UI; OP login CSRF **422** on double-submit / multiple OP tabs. Workaround: refresh PA — connection may already exist. | OP-side / UX guidance; optional later polish |
| Env API-key fallback | `OPENPROJECT_API_KEY` used only when connect body leaves apiKey blank **and** fallback is enabled (dev/test). Pasting a key in the UI stores that key on the workspace; `.env` need not be cleared. | Prod: fallback forced off |
| Workspace membership isolation | Analytics APIs gated by membership. Workspace Admin grant/revoke by PA email on Connections. Promote-to-admin not in v1. | Optional admin promote later |
| Connect / credentials | Workspace Admin may rotate credentials; legacy URL-only create disabled; OAuth + API-key share eligibility. | — |
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
| Analytics access grants | Grant/revoke by email on Connections (Workspace Admin only). Grantees see analytics; no sync/disconnect/grants. No promote-to-admin in v1. | Optional admin promote later |
| Self-signup | Open by default (`REGISTRATION_ENABLED`); **must confirm email before login**; no analytics until connect/grant | By design (M14a) |
| Email confirmation | Local/dev logs confirmation link when mail is disabled. **Real deploy needs SMTP** (same as password reset). TTL `EMAIL_CONFIRMATION_TTL_MINUTES` (default 24h). | **N5 / M17** |
| Password reset | Works locally with mail disabled (reset link logged / token stored). **Real deploy needs SMTP** (`PASSWORD_RESET_MAIL_ENABLED=true` + `spring.mail.*`). | **N5 / M17** |
| Dev seed admin | `admin` / documented demo password — **must change/disable for real deploy** (see security-checklist). Seed/backfilled users are already `email_verified`. | M17 / ops |
| Legal pages | Privacy / Contact / Terms are **draft placeholders** in the product footer — replace with real copy before customer deploy. | **N5 / M17** |

### N1 soak (2026-08-19) — confirmed

| Check | Result |
|-------|--------|
| OAuth connect + sync | PASS (BEARER_TOKEN, encrypted client secret, eligibility admin) |
| API-key reconnect + sync | PASS (`auth_scheme=API_KEY`, 7 projects / 96 WPs) |
| Multi-OP live | Single local OP only; automated distinct-client test accepted |
| Already-connected deny | PASS — clear M15 message |
| M15 grant | PASS — grantee sees data; not Workspace Admin |
| Password-reset API | PASS — generic success; tokens created; mail off by default |

---

## Product experience

| Limitation | Detail | Target |
|------------|--------|--------|
| UI polish deferred | Functional PE; not final visual craft | **N3 / M16a** |
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
