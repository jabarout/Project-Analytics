# Development Roadmap

Version: 1.0

Status: Draft

---

# 1. Purpose

This document defines the implementation roadmap for Project Analytics.

The roadmap divides development into incremental milestones, allowing each phase to produce a stable, testable, and deployable version of the application.

Each milestone should be completed, tested, and documented before moving to the next.

---

# 2. Development Principles

Development shall follow these principles:

- Incremental delivery
- Feature completeness
- Continuous testing
- Continuous documentation
- Modular implementation
- Backward compatibility between milestones

Every milestone should result in a working application.

---

# 3. Milestone Overview

| Milestone | Name | Goal |
|------------|------|------|
| M1 | Foundation | Build project skeleton and infrastructure |
| M2 | Authentication | Secure application access |
| M3 | Synchronization | Import OpenProject data |
| M4 | Portfolio Management | Portfolio overview |
| M5 | Project Analytics | Project dashboards and KPIs |
| M6 | Dashboards | Executive dashboards |
| M7 | Reporting | PDF and Excel reports |
| M8 | Recommendations | Decision intelligence |
| M9 | Monitoring | Operations & observability |
| M10 | Production Hardening | Security, retention, performance, deploy readiness |
| M11 | Product Experience | Implement frozen PE architecture (`19_Product_Experience.md`) |
| M12 | Decision metrics | Progress/schedule consistency (ProgressMetrics); portfolio depth |
| M13 | Quality gate | Product freeze, happy path, handoff, bug bash |
| M14a | PA Account Registration | Sign up / login; no OP access until connect/grant |
| M14 | OpenProject Connection Security | OAuth preferred; API-key alt; encrypted credentials; OP eligibility; Connections UI |
| M15 | Analytics Access & Isolation | Workspace memberships, grants, backend enforcement, isolation tests |
| M16a | Visual polish & dark mode | Tokens, dark mode, spacing/empty states — no IA redesign |
| M16b | Motion & transitions | Subtle route/loading/KPI/micro-interactions; non-blocking for M17 |
| M17 | Deploy & customer package | One clean production deploy + ops package |
| M18 | Soft intelligence (optional) | Trends / reco evidence only — not AI platform |

---

# 4. Milestone 1 — Foundation

Objectives:

- Initialize backend
- Initialize frontend
- Configure Docker
- Configure PostgreSQL
- Configure Redis
- Configure project structure
- Configure CI/CD
- Configure logging
- Configure OpenAPI
- Configure Flyway

Deliverables:

- Running backend
- Running frontend
- Database migrations
- Docker Compose environment
- Swagger UI

---

# 5. Milestone 2 — Authentication

Objectives:

- User model
- Login
- JWT authentication
- Authorization
- Route guards
- User preferences

Deliverables:

- Secure login
- Protected APIs
- Protected frontend routes

---

# 6. Milestone 3 — Synchronization

Objectives:

- OpenProject client
- Workspace management
- Synchronization service
- Incremental synchronization
- Manual synchronization
- Synchronization history

Deliverables:

- Local project database synchronized with OpenProject

---

# 7. Milestone 4 — Portfolio Management

Objectives:

- Portfolio entities
- Portfolio API
- Portfolio dashboard
- Portfolio KPIs

Deliverables:

- Portfolio overview page

---

# 8. Milestone 5 — Project Analytics

Objectives:

- Dedicated analytics module (all scoring algorithms)
- KPI calculations from local PostgreSQL only
- Health Score, Risk Score, Attention Score (explainable DTOs)
- Project dashboards
- Workspace-level aggregation of project analytics (“All Projects”)
- Historical analytics
- No formulas embedded in PortfolioService or controllers

Deliverables:

- Project analytics pages
- Workspace dashboard/KPI consumption of analytics results (primary UX after sync)

Constraints:

- OpenProject is never called from analytics
- Portfolios remain optional subsets of scored projects

---

# 9. Milestone 6 — Dashboards

Objectives:

- Executive dashboard
- Workspace dashboard experience (primary)
- Optional portfolio dashboards
- Widget system
- Charts
- KPI cards
- Alerts
- Trends

Deliverables:

- Complete dashboard experience aligned with workspace-first product architecture

---

# 10. Milestone 7 — Reporting

Objectives:

- PDF reports
- Excel reports
- Report history
- Download endpoints

Deliverables:

- Report generation workflow

---

# 11. Milestone 8 — Recommendation Engine

Objectives:

- Recommendation rules
- Explainability
- Executive summaries
- Priority ranking

Deliverables:

- Actionable recommendations

---

# 12. Milestone 9 — Monitoring

Objectives:

- Prometheus
- Grafana
- Health checks
- Metrics
- Performance monitoring
- Structured logging

Deliverables:

- Observable production environment

---

# 13. Milestone 10 — Production Hardening

**Status:** Implemented (pending review). **No product/UX redesign** — identity and M11 PE architecture remain frozen.

Objectives:

- Security hardening (prod defaults, headers, secrets, review)
- Data retention (reports + analytics snapshots)
- Performance baselines and targeted optimization
- Load / critical-path test expansion
- Deployment path and production documentation
- Dependency and configuration readiness

Deliverables:

- Hardened, deployable application with ops-ready retention and security posture
- Documented production runbook additions as needed
- M10 recorded complete in `13_Project_State.md`

**Explicitly out of M10:**

- M11 Product Experience UI/IA implementation
- OAuth for OpenProject (later credential work; API key remains temporary)
- New analytical product features or domain scoring changes
- Workspace analytics access grant UI (M11; access **rule** already frozen)

---

# 13.1 Milestone 11 — Product Experience

**Architecture status:** Principles frozen in `19_Product_Experience.md` / `00_Project_Vision.md`.  
**UX specification status:** **M11A frozen** in `20_M11A_Product_Experience_Specification.md` (v1.1).

### M11A (specification) — complete

Presentation/exploration only: Home triage, Explorer, portfolio health summary, drill-down, Saved Views, Connections focus.

### M11B (implementation) — next

Implement M11A without reopening UX philosophy:

- Home exception-first KPIs (Needs Attention as **count + %**)
- Explorer with filters, **sort**, **group**, **Saved Views**
- Portfolio Overview + **Portfolio Health Summary**
- Upcoming deadlines window (default 14d, configurable)
- Connections = connect/sync (multi only when needed)
- Drill-down / View all context rules

**Policy:** Do not change analytics scoring, recommendations engine, M10 hardening, or product vision.

---

# 14. Testing Roadmap

Every milestone must include:

- Unit tests
- Integration tests
- API tests
- Frontend tests

Regression tests should be executed before closing each milestone.

---

# 15. Documentation Roadmap

After completing a milestone:

- Update documentation
- Update architecture diagrams
- Update API documentation
- Update Project_State.md

Documentation should always reflect the implemented system.

---

# 16. Definition of Done

A milestone is considered complete when:

- All planned features are implemented.
- Tests pass successfully.
- Documentation is updated.
- Code review is completed.
- No critical defects remain.
- CI/CD pipeline succeeds.

---

# 17. Risk Management

Potential risks include:

- OpenProject API changes
- Performance bottlenecks
- Synchronization failures
- Security vulnerabilities
- Scope expansion

Mitigation strategies should be reviewed regularly throughout development.

---

# 18. Post-M11 product ladder (sellable ~$2k product)

Target: finished OpenProject companion — trustworthy, deployable, not enterprise SaaS.

**Frozen (do not reopen during N1–N5):** PE structure · M10 hardening · M12 ProgressMetrics ownership · Hybrid access model.

| Milestone | Status | Goal |
|-----------|--------|------|
| **M12** Decision metrics | **Complete** | ProgressMetrics SoT; extended metrics; portfolio analytics depth; classic Home |
| **M13** Quality gate | **Complete** | Happy path, handoff docs, known limits, tests green; local company testing ready |
| **M14a** PA Account Registration | **Complete** | Email/password signup; **email confirmation before login**; password reset; rate limits; no first-user admin |
| **M14** OpenProject Connection Security | **Complete** | OAuth + PKCE; per-workspace OAuth client credentials; API-key alt; eligibility; encryption |
| **M15** Analytics Access & Isolation | **Complete** | Memberships; grant/revoke by email; isolation/grant-matrix tests |
| **N1** Connect & auth soak | **Complete** | Manual verification of connect/auth flows; quirks documented |
| **N2** Regression gate | **Complete** | Full automated + smoke gate before UI work |
| **UI modernization** (replaces monolithic N3 batch) | **In progress** | Gated UI-1…UI-8; Option B (SCSS tokens + later ECharts); Zard/Bklit reference only |
| **M16b / N4** Optional motion extras | Complete | Scroll reveals / placement; **non-blocking for M17** |
| **M17 / N5** Deploy & customer package | **Complete** (2026-08-20) | Start at `docs/ops/DEPLOY.md` |
| **M18 / N6** Soft intelligence | Optional | Snapshot trends, reco evidence; no LLM requirement |

### Execution order (approved)

```text
N1 → N2 → N3/M16a → N4/M16b → N5/M17
N6/M18 optional after N5
```

- **N1 and N2 are mandatory** before any visual work.
- **N3 is mandatory.**
- **N4** must be professional and subtle; **must not block N5/M17** if N3 is complete and N4 adds unnecessary risk.

---

## 18.1 N1 — Connect & auth soak

**Goal:** Hybrid connect is demo-reliable on local OpenProject.

**Priority:** **Manual testing first**; document confirmed behavior and OP-specific quirks **afterward**.

| ID | Task | Acceptance | Tests |
|----|------|------------|-------|
| N1.1 | Manual OAuth connect (popup flow, per-workspace client id/secret) | Connect succeeds; quirks noted | Manual + existing OAuth API tests |
| N1.2 | API-key fallback | Connect + sync without OAuth | Manual + connect hardening tests |
| N1.3 | Multi-OpenProject behavior | Distinct client credentials per workspace URL | Manual + multi-client OAuth test |
| N1.4 | Already-connected / M15 | Second user denied cleanly; grants still work | Manual + grant/isolation tests |
| N1.5 | Password-reset / SMTP readiness | Local path verified; SMTP checklist for real deploy | Manual + password-reset tests |
| N1.6 | Document findings | Known-limitations / ops notes updated from **confirmed** results | Doc review |

**Exit:** N1 checklist signed off; no open P0 connect bugs for local demo.  
**Status:** **Complete** (2026-08-19) — see `docs/ops/known-limitations.md` § N1 soak.

---

## 18.2 N2 — Regression gate

**Depends on:** N1. **Blocks:** N3+. **Status:** **Complete** (2026-08-19).

| ID | Task | Acceptance | Tests |
|----|------|------------|-------|
| N2.1 | Full backend suite | All green | `mvn test` — **73 tests, 0 failures** |
| N2.2 | Security matrix | Isolation, grants, OAuth, API-key overwrite, rate-limit, password-reset pass | Those integration classes — **PASS** |
| N2.3 | Frontend build | Succeeds | `ng build` — **PASS** (budget warnings only on workspaces/portfolio SCSS) |
| N2.4 | Smoke happy path | Login → connect/sync → Home/Explorer/Detail → report smoke | API smoke + Playwright UI smoke — **PASS** |
| N2.5 | Docs sync | Project State / Roadmap reflect post-N2 Next = N3 | Doc review — **this update** |

**Exit:** Regression report green; go/no-go for N3.  
**Result:** **GO for N3** (awaiting explicit approval to start visual polish).

---

## 18.3 N3 / M16a — Visual polish, dark mode, motion & footer (mandatory)

**Depends on:** N2. **No IA redesign.** **Status:** **In progress** (approved 2026-08-19).

**Visual direction:** Monochromatic-first (black / white / grey shades for chrome). No blue/indigo/purple brand accent. Semantic color only for healthy / attention / critical / errors. Revolut-inspired restraint (flat surfaces, pills, type/spacing hierarchy) — not a fintech lookalike.

| ID | Task | Acceptance | Tests |
|----|------|------------|-------|
| N3.1 | Design tokens (light + dark) | Neutral ladder + semantic tokens; no brand hue accent | Visual review |
| N3.2 | Dark mode wiring | Settings theme + persistence; ThemeService on document | Manual theme switch |
| N3.3 | Shared controls + surfaces | Pill buttons, cards, focus rings (neutral) | UI checklist |
| N3.4 | Shell polish (main + auth) | Header/nav/content coherent in both themes | Manual |
| N3.5 | Core feature surfaces | Home, Explorer, Project, Connections, login — no hardcoded blue | Manual |
| N3.6 | Motion + micro-interactions | Button press, hover, short route fade; `prefers-reduced-motion` | Manual + OS toggle |
| N3.7 | Charts / KPI | Greyscale structure; semantic chips only | Manual |
| N3.8 | Footer + legal placeholders | Privacy / Contact / Terms public; draft banners | Manual logged-out |
| N3.9 | A11y baseline | Contrast/focus in both themes | Spot-check |

**Exit:** Premium monochrome UI + dark mode + subtle motion + footer/legal; go/no-go for N5 (N4 optional).

---

## 18.4 N4 / M16b — Optional motion extras (non-blocking for M17)

**Depends on:** N3. **May be deferred.** Core motion/micro-interactions moved into **N3**.

| ID | Task | Acceptance | Tests |
|----|------|------------|-------|
| N4.1 | Scroll reveals *(optional)* | Only if performant/professional | Manual (if shipped) |
| N4.2 | Extra motion polish | Only if needed after N3 | Manual |
| N4.3 | Perf gate | No jank → simplify or defer before M17 | DevTools spot-check |

**Exit (ship):** optional extras signed off. **Exit (defer):** Project State notes N4 deferred; N5 proceeds on N3.

---

## 18.5 N5 / M17 — Deploy & customer package

**Depends on:** N3 (mandatory). N4 optional.

| ID | Task | Acceptance | Tests |
|----|------|------------|-------|
| N5.1 | Prod compose / profile | Stack up from docs | Smoke on prod profile |
| N5.2 | Env template | All required keys; no secrets | Review vs ProductionSecurityValidator |
| N5.3 | Backup/restore | Scripts + runbook | Dry-run |
| N5.4 | Security deploy checklist | Seed admin, encryption key, SMTP, CORS, rate limits | Ops checklist |
| N5.5 | Demo walkthrough | Buyer path on polished UI | Manual script |
| N5.6 | Handoff package | Versioned ops package / README | Package smoke |

**Exit:** Deploy & demo in one sitting from docs alone.

---

### Explicitly later / out of $2k scope

- AI forecasting platform
- Multi-tenancy / SaaS billing
- Additional PM tool integrations
- Mobile application
- Plugin ecosystem

These may return as v2 only after M17.

---

# 19. AI Implementation Notes

When implementing the roadmap:

- Complete milestones sequentially.
- Avoid partial implementations.
- Keep every milestone deployable.
- Generate tests alongside features.
- Update documentation after each completed milestone.
- Respect the architecture defined in previous documents.

---

# End of Document