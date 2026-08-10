# Known limitations (buyer-honest)

Last updated: 2026-08-10 (M13)

These are **accepted** constraints of the current product — not secret bugs.  
Fix targets are noted when planned.

---

## Integration

| Limitation | Detail | Target |
|------------|--------|--------|
| OpenProject auth is API key | Env `OPENPROJECT_API_KEY`; not re-read without process restart | **M14 OAuth** |
| One credential style | UI stores workspace URL only; key is process env | M14 |
| Full-workspace rescore after sync | Correctness over partial rescore | Later optimization |

---

## Analytics

| Limitation | Detail | Target |
|------------|--------|--------|
| Budget variance usually null | No spent-budget field in local model | When OP spend is modeled |
| Expected progress / gap null | Requires usable project start **and** end dates | Data quality in OP |
| Progress prefers WP completion | OP project progress field ignored when WPs exist (by design) | Documented M12 |
| Schedule variance only | No separate “days to deadline” field | Use schedule variance |
| Trends are snapshot history | Limited soft intelligence | Optional M18 |

---

## Access & multi-user

| Limitation | Detail | Target |
|------------|--------|--------|
| Analytics access grants incomplete | Rule frozen; full grant UI not finished | **M15** |
| Dev seed admin | `admin` / `Admin123!` — change for any real deploy | M17 / ops |

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
