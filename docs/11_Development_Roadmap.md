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
| M14 | OpenProject OAuth | Replace temporary API key via credential resolver seam |
| M15 | Analytics access grants | App-owned who-can-see-analytics UI + enforcement |
| M16 | UI polish | Visual/UX consistency (no IA redesign) |
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

| Milestone | Status | Goal |
|-----------|--------|------|
| **M12** Decision metrics | **Complete** | ProgressMetrics SoT; extended metrics; portfolio analytics depth; classic Home |
| **M13** Quality gate | **Complete** | Happy path, handoff docs, known limits, tests green; local company testing ready |
| **M14** OpenProject OAuth | Planned (hold until explicit go-ahead) | OAuth via `OpenProjectCredentialResolver`; API key optional/dev |
| **M15** Analytics access grants | Planned | Workspace admin grants analytics access (frozen product rule) |
| **M16** UI polish | Planned | Spacing, empty states, terminology, consistency — PE frozen |
| **M17** Deploy & customer package | Planned | Compose/prod profile, env template, backup, demo walkthrough |
| **M18** Soft intelligence | Optional | Snapshot trends, reco evidence; no LLM requirement |

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