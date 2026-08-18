# Project State

Version: 1.0

Status: Active

Last updated: 2026-08-18

**Active phase:** **N1 — Connect & auth soak** (approved ladder; not started in code).  
**Just completed (product):** **M14a / M14 / M15** — registration, OAuth (per-workspace client credentials), grants & isolation.  
**Next (approved order):** **N1 → N2 → N3/M16a → N4/M16b → N5/M17** (N6/M18 optional).  
**Gates:** N1+N2 mandatory before visual work; N3 mandatory; **N4 non-blocking for M17**.  
**Recent:** Multi-OP OAuth client credentials; Hybrid connect UX soak pending; Community KPI adaptation  
**Frozen / do not reopen:** M10 hardening, overall architecture, PE principles (`19_Product_Experience.md`), **M11A PE specification** (`20_M11A_Product_Experience_Specification.md` v1.1), M12 ProgressMetrics / score formula ownership, **Hybrid access model**.  
**Access model:** Hybrid (OP eligibility for connect; PA grants for ongoing analytics).

---

# 1. Purpose

This document tracks the implementation status of Project Analytics relative to the architecture documentation and development roadmap.

It must be updated after every completed milestone.

---

# 2. Overall Status

| Area | Status |
|------|--------|
| Architecture documentation | **Product architecture frozen** (pre-M5 decisions) |
| Milestone 1 — Foundation | **Complete** |
| Milestone 2 — Authentication | **Complete** (approved) |
| Milestone 3 — Synchronization | **Complete** (approved) |
| Pre-M4 — OpenProject credential resolver | **Complete** |
| Milestone 4 — Portfolio Management | **Implemented** (pending review) |
| Pre-M5 — Product architecture freeze (docs) | **Complete** |
| Milestone 5 — Project Analytics | **Complete** (approved) |
| Milestone 6 — Dashboards | **Complete** (approved) |
| Milestone 7 — Reporting | **Complete** (approved) |
| Milestone 8 — Recommendations | **Implemented** (pending review) |
| Milestone 9 — Operations & Observability | **Implemented** (pending review) |
| Milestone 10 — Production Hardening | **Complete** (approved) |
| Milestone 11A — PE Specification | **Frozen** (approved, v1.1 refinements R1–R6) |
| Milestone 11B — PE Implementation | **Largely complete** — E1–E6; UI polish deferred to **M16** |
| Milestone 12 — Decision metrics | **Complete** — ProgressMetrics SoT, extended metrics, portfolio depth, Home classic triage |
| Milestone 13 — Product freeze & quality gate | **Complete** — happy path docs, known limitations, tests green, local handoff ready |
| Milestone 14a — PA Account Registration | **Complete** — signup, password reset, rate limits |
| Milestone 14 — OpenProject Connection Security | **Complete** — OAuth+PKCE, per-workspace OAuth clients, API-key alt, eligibility, encryption |
| Milestone 15 — Analytics Access & Isolation | **Complete** — memberships, grant/revoke UI+API, isolation tests |
| **N1** Connect & auth soak | **Next** — manual connect/auth verification (docs after) |
| **N2** Regression gate | **Planned** — full suite + smoke before UI |
| **N3 / M16a** Visual polish & dark mode | **Planned** — mandatory |
| **N4 / M16b** Motion & transitions | **Planned** — subtle; non-blocking for M17 |
| **N5 / M17** Deploy & customer package | **Planned** — after N3 (N4 optional) |
| **N6 / M18** Soft intelligence | **Optional** |

---

# 2.1 Frozen product architecture (official source of truth)

The following decisions are frozen in PRD, Domain Model, System Architecture, Security, and UI/UX guidelines. M5+ must follow them.

### OpenProject integration

- Long-term auth: **OAuth 2.0**
- **One Workspace = one OpenProject instance**
- Env **API key is temporary** until OAuth milestone
- **`OpenProjectCredentialResolver`** is the migration seam (credential provider only; no redesign of sync engine, import, or analytics)

### Primary user journey

**Connect Workspace → Synchronize → Immediate Workspace Dashboard & Analytics on local PostgreSQL**

OpenProject is never used during dashboard usage—only as synchronization source.

### Workspace vs portfolio (frozen)

- **Workspace** owns all synchronized projects — primary analytics scope (“All Projects”)
- **Portfolios** = optional **analytical collections** (views/categories), not project ownership
- Users need **not** create a portfolio before analytics are useful
- Primary UX after sync = **Workspace Dashboard**

### Portfolio membership (frozen many-to-many)

**Portfolio membership is purely organizational.** It has **no effect** on:

- project ownership (workspace owns projects)
- synchronization
- analytics **calculation** (Health / Risk / Attention scoring remains per-project in the analytics engine)

It **only** affects which projects are **included** when computing **portfolio-scoped** dashboards, recommendations, and reports.

- A project may belong to **zero or more** portfolios (`portfolio_project`)
- Sync attaches projects to the **workspace only**
- Dynamic rule-based portfolios: **future only**

### Analytics module (M5 constraint)

- Health / Risk / Attention algorithms live only in the analytics module
- Consume local PostgreSQL only; expose clear DTOs
- No formulas embedded in PortfolioService or controllers

### Product identity & primary audience (frozen)

- Project Analytics is a **management intelligence layer** on OpenProject—not a replacement and not for day-to-day task execution.
- **Primary persona:** user responsible for **monitoring and prioritizing multiple projects or portfolios** within a workspace (not a job title).
- **Feature filter:** *Does this help someone oversee and prioritize multiple projects?* If yes → here; if primarily individual task/execution → OpenProject.
- Full statement: `00_Project_Vision.md` §6, §9–§10; experience: `19_Product_Experience.md`.

### Analytics access (frozen intent)

- Access is an **application concept**; do **not** infer manager/decision-maker status from OpenProject roles.
- **Workspace administrator** grants who can access the analytics workspace.
- Independent of OpenProject organizational hierarchy.
- Implementation of grants ships with **M11**; rule is frozen now (`09_Security.md` §5.1).

### M11 Product Experience (architecturally frozen)

- Full PE architecture frozen in `19_Product_Experience.md` (P1–P6, surfaces, primary persona, landings, flows).
- **No UX/product iteration during M10.** After M10, implement M11 per frozen doc without reopening product discussions.
- Vision feature filter remains binding: multi-project oversee/prioritize → in; task execution → OpenProject.

---

# 2.2 M10 — Production Hardening (plan)

**Goal:** Make the system production-ready operationally. **No domain redesign. No M11 UX.**

| Workstream | Scope | Out |
|------------|-------|-----|
| **Security** | Prod CORS/security headers review; secrets not in repo; actuator exposure review; authz regression; light threat model / checklist in docs | OAuth; full RBAC redesign; analytics access UI |
| **Retention** | Configurable purge for report artifacts + metadata; analytics snapshot retention job | Changing report immutability model |
| **Performance** | Index/query review; hot-path profiling notes; targeted fixes; document known full-rescore cost | Full incremental analytics redesign (optional stretch only if cheap) |
| **Quality** | Critical API/E2E path coverage expansion; load baseline (script + doc); CI green | New product features |
| **Deployment** | Prod profile validation; compose/deploy docs; env template completeness; release checklist | Multi-region / K8s-only packaging unless already needed |
| **Docs** | Update Project State, ops notes, config for retention/security | PE redesign |

**Definition of done (M10):**

1. Security checklist completed and critical gaps fixed.
2. Retention jobs (or equivalent documented automation) for reports and snapshots with config.
3. Performance notes + any critical fixes landed; load baseline recorded.
4. Critical-path tests pass in CI; deploy/runbooks updated.
5. `13_Project_State.md` records M10 complete; next = M11 implementation.

---

# 3. Milestone 3 — Synchronization

## Goal

Import OpenProject operational data into the local analytical store (FR-003): workspace management, OpenProject client, manual + scheduled + incremental sync, history, failure safety.

## Implemented

### Database (Flyway `V3__workspace_and_operational_data.sql`)

- `workspace`
- `portfolio` (default portfolio per workspace for project FK)
- `project`
- `work_package`
- `synchronization_history`

### Backend

| Package | Responsibility |
|---------|----------------|
| `infrastructure.openproject` | REST client (API v3), Basic auth (`apikey`), pagination, incremental filters |
| `synchronization` | Workspace API, sync orchestration, history, scheduler, cache invalidation |
| `portfolio.persistence` | Portfolio entity (structure only; KPIs later) |
| `project.persistence` | Project + work package entities (no analytics) |

**API**

- `GET /api/v1/workspaces`
- `GET /api/v1/workspaces/{id}`
- `POST /api/v1/workspaces` (register connection; required to operate beyond list/get)
- `POST /api/v1/workspaces/{id}/synchronize` (manual)
- `GET /api/v1/workspaces/{id}/synchronization` (latest status)

**Sync behaviour**

- Types: INITIAL / MANUAL / SCHEDULED (first successful run stored as INITIAL)
- Incremental: uses last SUCCESS `finished_at` as `modifiedSince` filter for OpenProject
- Concurrent runs blocked (`SYNC_003`)
- Import transaction rolls back on failure; history SUCCESS/FAILED written in `REQUIRES_NEW` transactions
- Cache invalidation hook after success; `PostSynchronizationHook` no-op until M5 analytics
- Scheduler: `projectanalytics.sync.enabled` + `interval-ms` (disabled in tests)

**Config**

- `OPENPROJECT_URL`, `OPENPROJECT_API_KEY`, `OPENPROJECT_TIMEOUT`, `OPENPROJECT_VERIFY_SSL`
- `SYNC_ENABLED`, `SYNC_INTERVAL_MS`, batch/retry/timeout settings

**Error codes:** SYNC_001–006, WORKSPACE_001–002

### Frontend

- Feature `workspaces`: register workspace, list, manual synchronize, show last run stats
- Nav entry under main layout
- `WorkspaceApiService` + unit test

### Tests

- Backend: 23 tests (includes sync service + workspace API integration with mocked OpenProject client)
- Frontend: unit tests including workspace API service

## Architectural constraints respected

- No analytics calculation in sync module
- OpenProject remains system of record
- Failures do not corrupt prior operational data
- No schema redesign; Flyway-only evolution
- API key via environment (not stored on workspace table — matches documented columns)

## Not in M3

- Portfolio dashboard / KPIs (M4)
- Health/Risk/Attention scores (M5)
- Full OP resource set (memberships, custom fields) beyond projects + work packages

---

# 3.1 Pre-M4 — OpenProject credential resolver

## Goal

Introduce `OpenProjectCredentialResolver` and centralize HTTP authentication in the OpenProject client with **no behavioral change**. Document OAuth 2.0 as the planned future OpenProject auth mechanism.

## Implemented

- `OpenProjectCredentialResolver` port
- `EnvironmentApiKeyOpenProjectCredentialResolver` (default; uses `OPENPROJECT_API_KEY`)
- `OpenProjectCredentials` / `OpenProjectAuthScheme` (`API_KEY` current, `BEARER_TOKEN` reserved)
- `RestOpenProjectClient.buildAuthorizationHeader` — single place for Basic vs future Bearer
- `OperationalDataImportService` resolves credentials via the port (no direct API key access)
- Docs updated: System Architecture, Backend Architecture, Configuration, Security
- Unit tests for resolver and authorization header construction

## Explicitly out of scope

- OAuth endpoints
- Token storage
- Database / Flyway changes for OAuth

---

# 4. How to run sync locally

```bash
# Configure OPENPROJECT_URL + OPENPROJECT_API_KEY in .env
cd docker && docker compose up -d postgres redis
cd backend && mvn spring-boot:run
cd frontend && npm start
```

1. Login as `admin` / `Admin123!`
2. Open **Workspaces**
3. Register base URL
4. **Synchronize now**

---

# 5. Milestone 4 — Portfolio Management

## Goal

Portfolio overview, KPIs, dashboard, and CRUD on the **local** domain model (FR-004 partial operational view).

## Constraint (enforced)

- All portfolio features use **local PostgreSQL only**
- **No** OpenProject API calls from the portfolio module
- OpenProject remains an external sync source only (M3)

## Implemented

### Backend (`com.projectanalytics.portfolio`)

- CRUD: create, list, get, update, delete (delete only when empty)
- Assign project to portfolio (same workspace)
- `GET /portfolios/{id}/kpis` — operational aggregates from local `project` / `work_package`
- `GET /portfolios/{id}/dashboard` — ready-to-display DTO + executive summary + insights
- `PortfolioLocalMetricsService` — pure local queries (counts, budget sum, avg progress, overdue)

### Frontend

- `/portfolios` list + create
- `/portfolios/:id` detail/dashboard
- Nav entry

### Explicitly deferred to M5 Analytics Engine

- Full Health Score / Risk Score / Attention Score algorithms
- Recommendations engine, alerts module

Stored `health_score` / `attention_score` columns are exposed when present; M4 does not invent full scoring formulas.

## Tests

- Backend: portfolio service + API integration (local fixtures only)
- Frontend: portfolio API service unit test

---

# 6. Milestone 5 — Project Analytics

## Goal

Dedicated analytics engine for Health / Risk / Attention; workspace dashboard as primary surface; shared engine for workspace, portfolio, and project dashboards.

## Implemented

### Module `com.projectanalytics.analytics`

- Scoring calculators (configurable weights): Health, Risk, Attention
- `ProjectAnalyticsEngine` — single engine for all scopes
- Persistence: `analytics`, `analytics_snapshot` (Flyway V4)
- Recalculation after sync via `AnalyticsRecalculationHook`
- Query/aggregation: `AnalyticsQueryService` builds shared `ScopeDashboardResponse`

### APIs

- `GET /workspaces/{id}/dashboard` — **primary** All Projects dashboard
- `GET /workspaces/{id}/kpis`
- `GET /portfolios/{id}/dashboard|kpis` — same engine, member subset
- `GET /projects/{id}/dashboard`
- `GET /analytics/projects/{id}/health|risk|attention|kpis|trends`
- `POST /analytics/workspaces/{id}/recalculate`

### Frontend

- Home = Workspace Dashboard (primary)
- Project detail with explainable scores
- Portfolio detail consumes shared analytics dashboard DTO

### Constraints honored

- Local PostgreSQL only for scoring
- No OpenProject I/O from analytics
- No formulas in PortfolioService/controllers
- Workspace is primary analytics scope

### Known limitations (documented; deferred, not blockers for M5 approval)

1. **Post-sync recalculation scope:** After sync, **all projects in the workspace** are rescored (not only projects changed by incremental sync). Portfolio averages for the workspace are then refreshed. Affected-project-only recalculation is a planned performance improvement (see `08_Analytics_Engine.md` §15.1).

2. **`analytics_snapshot` retention:** Snapshots are **append-only** today. Trends read the latest rows; there is no purge job yet. Configurable retention is deferred (ops/maintenance), not an analytics redesign.

---

# 7. Milestone 6 — Dashboards

## Goal

Visualization and UX on top of the stable analytics architecture — **no new business scoring logic**.

## Implemented

### Backend (`dashboard` module)

- `GET /dashboards/executive` — cross-workspace composition from analytics outputs
- Aliases: `/dashboards/workspace|portfolio|project/{id}`
- Lightweight CSV export: `/dashboards/executive/export.csv`, `/dashboards/workspace/{id}/export.csv`
- `ExecutiveDashboardService` only composes existing analytics DTOs

### Frontend

- Shared widgets: KPI card, insight list, attention table, bar chart, trend chart
- Executive dashboard page (`/executive`) with charts + export
- Workspace home rebuilt with widgets + CSV export
- Project page: KPI cards + trend chart
- Portfolio detail: same widget system

### Explicitly not in M6

- New Health/Risk/Attention formulas
- PDF/Excel formal reporting (M7)
- Real-time OpenProject calls from dashboards

---

# 8. Milestone 7 — Reporting

## Goal

Formal PDF/Excel report generation, report history, and download workflow (FR-007) from **existing** analytics and dashboard outputs — **no new scoring**.

## Implemented

### Database (Flyway `V5__reporting.sql`)

- `report` table: documented columns (`id`, `generated_by`, `report_type`, `file_path`, `generated_at`) plus `title`, `format`, `status`, `scope_type`, `scope_id`, `file_name`, `content_type`, `file_size_bytes`, `error_message` for PDF/Excel workflow

### Backend (`com.projectanalytics.reporting`)

| Component | Responsibility |
|-----------|----------------|
| `ReportContentAssembler` | Builds intermediate `ReportDocument` from analytics/dashboard DTOs only |
| `PdfReportGenerator` / `ExcelReportGenerator` | Presentational renderers (OpenPDF / Apache POI) |
| `ReportingService` | Generate → store file → persist history; list/get/download |
| `ReportController` | Thin REST API |

**API**

- `POST /api/v1/reports` — generate (EXECUTIVE, PORTFOLIO, PROJECT, KPI, RISK × PDF | EXCEL)
- `GET /api/v1/reports` — history (newest first)
- `GET /api/v1/reports/{id}` — status/metadata
- `GET /api/v1/reports/{id}/download` — file bytes

**Config**

- `REPORT_STORAGE_PATH` (default `./data/reports`)
- `REPORT_RETENTION_DAYS` (reserved; purge job deferred)
- `MAX_REPORT_SIZE` (`projectanalytics.reporting.max-size-bytes`)

**Error codes:** REPORT_001–004 (catalogued in `ErrorCode`)

### Frontend

- Feature `reports`: generate form + history table + download
- Nav entry **Reports**
- `ReportApiService` + unit tests

### Constraints honored

- Local analytics/dashboard data only; **no OpenProject I/O**
- **No new Health/Risk/Attention formulas**
- Controllers thin; scoring remains in analytics module
- Files stored outside the application binary

### Explicitly deferred

- Scheduled reports
- HTML/PowerPoint formats
- Report retention purge job
- Async generation queue
- Finer-grained report ACLs (creator-only / workspace-bound / role-based) — authenticated-only is acceptable for documented single-tenant scope

### Approval notes (M7)

- Reports are immutable point-in-time artifacts (download reads stored file; never regenerates against current data)
- Missing file with intact metadata → `REPORT_004`, fail closed, no historical rewrite
- Reporting consumes analytics/dashboard outputs only; no new scoring
- Core path complete: **Sync → Analytics → Dashboards → Reporting**

## Tests

- Backend: `ReportingServiceIntegrationTest` (PDF/Excel generate, history, download, validation)
- Full suite: **40** tests, 0 failures
- Frontend: report API unit tests; production build includes `reports-routes`

---

# 9. Milestone 8 — Recommendations

## Goal

Deterministic, explainable recommendations as a **consumer** of the analytics engine — not an extension of scoring. No new formulas, no OpenProject I/O. Own module DTOs for dashboards and reports.

## Implemented

### Database (Flyway `V6__recommendation.sql`)

- `recommendation` table: `analytics_id`, `project_id`, `rule_code`, `title`, `description`, `severity`, `explanation`, `suggested_action`, `priority_rank`, `supporting_metrics`, `generated_at`

### Backend (`com.projectanalytics.recommendation`)

| Component | Responsibility |
|-----------|----------------|
| `RecommendationRule` implementations | Deterministic triggers on Health/Risk/Attention/completion/trends |
| `RecommendationEngine` | Applies rules; ranks by severity |
| `RecommendationService` | Loads analytics DTOs, evaluates, persists, exposes bundles |
| `RecommendationController` | Thin REST API |
| Own DTOs | `RecommendationResponse`, `RecommendationBundleResponse`, `SupportingMetricResponse` |

**Rules (threshold-configurable, not scoring):**

- `CRITICAL_HEALTH` — health below critical threshold
- `ELEVATED_RISK` — risk at/above high threshold
- `HIGH_ATTENTION` — attention at/above high threshold
- `DECLINING_HEALTH_TREND` — health decline across snapshots
- `LOW_COMPLETION_HIGH_ATTENTION` — stalled high-attention delivery

**API**

- `GET /api/v1/projects/{id}/recommendations`
- `GET /api/v1/workspaces/{id}/recommendations`
- `GET /api/v1/portfolios/{id}/recommendations`
- `GET /api/v1/recommendations/executive`
- `GET /api/v1/recommendations/{id}`

**Config:** `projectanalytics.recommendation.*` thresholds and max counts

**Error codes:** `RECOMMENDATION_001`, `ANALYTICS_004` (generation failure)

### Consumption (composition only)

- Dashboards (frontend widgets): workspace, executive, portfolio, project load recommendation DTOs separately
- Reports: `ReportContentAssembler` includes a recommendations section from `RecommendationService`
- Analytics module unchanged (still sole scoring source)

### Frontend

- Shared `app-recommendation-list` widget
- Wired into home, executive, portfolio detail, project detail
- `RecommendationApiService` + unit test

### Explicitly deferred

- AI-generated insights (optional future layer on same recommendation outputs)
- Alerts module (M roadmap / FR-009 adjacent)
- Rule DSL / admin UI for arbitrary rule editing

### Constraints honored

- Consumer of analytics DTOs only
- Independent package and DTOs
- No OpenProject calls
- No new Health/Risk/Attention formulas
- Explainability: title, description, severity, explanation, supporting metrics, suggested action

## Tests

- Backend: `RecommendationServiceIntegrationTest`
- Full suite green; frontend unit tests green; production build OK

---

# 10. Architecture decision — Portfolio membership (pre-production freeze)

**Decision:** Portfolios are many-to-many analytical collections over workspace-owned projects.

| Rule | Implementation |
|------|----------------|
| Workspace owns projects | `project.workspace_id` |
| Portfolio membership | `portfolio_project` (portfolio_id, project_id) |
| Zero or more portfolios per project | Supported |
| Sync | Imports into workspace only (no forced default portfolio ownership) |
| Add/remove membership | `POST/DELETE /portfolios/{id}/projects/...` — does not move ownership |
| Delete portfolio | Removes collection + memberships; projects remain in workspace |
| Portfolio-scoped dashboards / recommendations / reports | **Include** member projects only (filter/scope) |
| Project scoring (Health/Risk/Attention) | **Unaffected** by membership — still computed per project from local data |
| Synchronization | **Unaffected** by membership |

Flyway: `V7__portfolio_project_membership.sql` migrates existing exclusive `portfolio_id` into `portfolio_project` and re-keys projects by workspace.

---

# 11. Milestone 9 — Operations & Observability

## Goal

Additive ops stack only. No domain redesign. Product architecture remains frozen.

## Implemented

### Health / probes

- Liveness group: process only (`livenessState`, `ping`) — **independent of OpenProject and local infra**
- Readiness group: **local infrastructure only** (`readinessState`, `db`, `redis`, `diskSpace`) — never OpenProject
- Security permits `/actuator/health/**`, `/actuator/info`, `/actuator/prometheus`

### Logging

- Request id: `X-Request-Id` + MDC (existing filter)
- Sync sets MDC `workspaceId`
- Prod JSON logs: `logback-prod.xml` (Logstash encoder)

### Metrics (`pa_*` frozen namespace)

- `pa_sync_runs_total`, `pa_sync_duration_seconds`, `pa_sync_projects_total`, `pa_sync_work_packages_total`
- `pa_sync_last_success_age_seconds` (for stale-sync alert)
- `pa_analytics_recalculate_duration_seconds`, `pa_analytics_projects_scored_total`
- `pa_report_generated_total`, `pa_report_generation_duration_seconds`
- Micrometer Prometheus registry; OTel tracing deferred but Micrometer remains the bridge

### Observability compose

- `docker/docker-compose.observability.yml` — Prometheus, Alertmanager, Grafana
- Dashboards: platform overview; sync/analytics/reports
- Alerts: BackendDown, HighHttp5xxRate, SyncFailuresElevated, **NoSuccessfulSynchronization** (default 24h)

### Backup / restore

- `scripts/backup-postgres.sh` — Postgres dump + report files (`reports.tgz`)
- `scripts/restore-postgres.sh`
- Policy: reports are immutable historical artifacts; backed up **with** metadata

### Runbooks

- `docs/ops/runbooks/RB-001` … `RB-007` including **Flyway migration failures (RB-007)**
- Index: `docs/ops/README.md`

## Explicitly out of M9 (M10+)

- Retention purge jobs, incremental analytics, security threat model, load/E2E expansion, deploy automation, OAuth/RBAC

---

# 12. Milestone 10 — Production Hardening (implemented)

## Delivered

### Security

- `SecurityHeadersFilter` — `X-Content-Type-Options`, `X-Frame-Options`, `Referrer-Policy`, `Permissions-Policy`, `Cache-Control: no-store`
- `ProductionSecurityValidator` (prod profile) — fail-fast if weak/default JWT secret or wildcard/empty CORS
- Prod: OpenAPI/Swagger disabled; `openapi-public=false`
- Actuator exposure limited; health details hidden in prod
- Checklist: `docs/ops/security-checklist.md`

### Retention

- `ReportRetentionService` — scheduled purge of report rows + files (`REPORT_RETENTION_DAYS`, `REPORT_PURGE_ENABLED`)
- `AnalyticsSnapshotRetentionService` — age-based snapshot purge (latest `analytics` untouched)
- Metric: `pa_retention_purge_total`
- Runbook: `docs/ops/runbooks/RB-008-retention-purge.md`

### Performance

- Flyway `V8__performance_indexes.sql` (snapshot trends, WP by project/due, sync history, report age)
- Notes: `docs/ops/performance-notes.md` (full-workspace rescore cost documented; no algorithm change)

### Quality / deploy

- Tests: security headers, prod validator, report + snapshot retention
- Load baseline script: `scripts/load-baseline.sh`
- Release checklist: `docs/ops/release-checklist.md`
- `.env.example` + configuration docs updated

## Explicitly out of M10 (unchanged policy)

- M11 Product Experience UI/IA
- OAuth for OpenProject
- Analytics access grant UI
- Incremental rescore redesign
- New product features

---

# 13. Milestone 12 — Decision metrics (complete)

**Constraint (still binding):** extend Health/Risk/Attention and snapshots — do **not** rebuild scoring or PE.

## Delivered

- **`ProgressMetrics`** — single source of truth for actual progress, expected progress, progress gap, overdue ratio, schedule variance days
- Canonical progress: WP completion when WPs exist, else OP project progress field
- Flyway **V10** extended analytics columns + factor JSON; **V11** drops obsolete `days_to_deadline` (use `schedule_variance`)
- Project Detail + Portfolio Analytics expose decision metrics; **Home stays classic exception triage**
- Scope KPI aggregates average **stored** fields only
- Docs: `08_Analytics_Engine.md` §15.2

## Explicitly deferred from M12

| Item | Target |
|------|--------|
| Richer snapshot trends / P6–P7 | Optional M18 or later |
| Reco evidence expansion / P8 | Optional M18 |
| Assignee load / P9 | Later (vision filter) |
| Chart dataset overhaul / P10 | M16 polish if needed |
| UI visual polish / dark mode | **N3 / M16a** |
| Motion & transitions | **N4 / M16b** (optional vs M17) |
| OAuth | **M14** (complete; N1 soak remaining) |

---

# 14. Milestone 13 — Product freeze & quality gate (complete)

**Goal:** Ship-ready trust for local company testing — not new features.  
**Closed:** 2026-08-10

## Delivered

1. Happy-path demo checklist — `docs/ops/demo-happy-path.md`
2. Known limitations — `docs/ops/known-limitations.md`
3. Handoff links — `README-XTENSUS.md`, root `README.md`, `docs/ops/README.md`
4. Roadmap post-M11 ladder formalized (M12–M18) in `11_Development_Roadmap.md`
5. Verification:
   - Full backend `mvn test` — **green**
   - Frontend production-dev build — **green**
   - Run artifacts present: `.env.example`, `scripts/run-backend.sh`, `docker/docker-compose.yml`
6. No known P0 on automated critical paths (auth, sync, analytics, portfolio, reports integration tests)

## Out of scope at M13 close (historical)

- OAuth / grants / polish / deploy were deferred then; **M14–M15 are now complete**. Remaining ladder: **N1–N5** (see §15).

## Local testing capability after M13

A company tester **can run the application locally** using `README-XTENSUS.md`:

1. Docker Postgres + Redis  
2. `.env` with `OPENPROJECT_API_KEY`  
3. `./scripts/run-backend.sh`  
4. Frontend `npm start`  
5. Login → connect → sync → Home / Explorer  

Limitations (API key, polish, grants) are documented, not hidden.

---

# 15. Post-M11 product ladder (approved N1–N5)

Full task breakdown, acceptance criteria, and tests: **`docs/11_Development_Roadmap.md` §18**.

| Order | Phase | Goal | Gate |
|-------|--------|------|------|
| Done | M12–M13 | Metrics + quality gate | — |
| Done | M14a / M14 / M15 | Registration, OAuth (multi-OP clients), grants/isolation | — |
| **Next** | **N1 Connect & auth soak** | **Manual** verify OAuth, API-key, multi-OP, M15 deny, password-reset/SMTP; document quirks after | Mandatory before UI |
| Then | **N2 Regression gate** | Full tests + smoke + docs truth | Mandatory before UI |
| Then | **N3 / M16a** | Visual polish + **dark mode** | Mandatory |
| Then | **N4 / M16b** | Route/loading/KPI/micro-interactions; reduced-motion; scroll reveals **nice-to-have** | **Non-blocking for M17** |
| Then | **N5 / M17** | Deploy & customer package | After N3 |
| Optional | **N6 / M18** | Soft intelligence | After N5 |

**N1 note:** Prioritize real manual testing of connection/authentication flows over documentation. Document confirmed behavior and remaining OpenProject-specific quirks afterward.

**N4 note:** Prioritize route transitions, loading/skeleton, KPI/chart, card/button micro-interactions, a11y/reduced-motion. Scroll reveals optional if natural and performant.

**Not in $2k scope:** multi-tenancy SaaS, mobile, plugin ecosystem, multi-PM integrations, advanced predictive AI.

### M11A freeze refinements (R1–R6) — still binding

1. Needs Attention on Home/portfolio = **count + %**, not average score  
2. Explorer **sort + group**  
3. Upcoming deadlines window **default 14d, configurable**  
4. **Portfolio Health Summary** section  
5. **Saved Views** (filters + sort + group + columns)  
6. **Connections** = OP connect/sync; multi-workspace UI only when multiple connections

---

# End of Document
