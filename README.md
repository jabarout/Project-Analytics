# Project Analytics

Enterprise Decision Intelligence Platform for OpenProject

---

## Quick links

| Audience | Start here |
|----------|------------|
| Local / company laptop setup | [`README-XTENSUS.md`](README-XTENSUS.md) |
| **Production deploy & customer handoff** | [`docs/ops/DEPLOY.md`](docs/ops/DEPLOY.md) |
| Ops index | [`docs/ops/README.md`](docs/ops/README.md) |

## Overview

Project Analytics is an enterprise web application designed to extend OpenProject by transforming operational project data into meaningful business intelligence.

Instead of replacing OpenProject, the application complements it by providing advanced analytics, executive dashboards, health monitoring, risk analysis, reporting, explainable KPIs, and AI-assisted insights.

OpenProject remains the operational project management system and the single source of truth.

Project Analytics becomes the decision intelligence layer.

---

## Objectives

The platform aims to:

- Synchronize project data from OpenProject.
- Analyze project and portfolio performance.
- Compute business KPIs.
- Calculate Health, Risk, and Attention Scores.
- Generate executive dashboards.
- Produce reports.
- Provide explainable recommendations.
- Offer AI-assisted summaries based on project data.

---

## Technology Stack

### Frontend

- Angular
- Angular Signals
- Angular CDK
- TypeScript

### Backend

- Spring Boot
- Spring Security
- Spring Data JPA

### Database

- PostgreSQL

### Cache

- Redis

### Infrastructure

- Docker
- Docker Compose
- Kubernetes (Future)

### Monitoring

- Prometheus
- Grafana

---

## Repository Structure

```
project-analytics/

backend/
frontend/
docker/
docs/
.github/

README.md
AI_INSTRUCTIONS.md
```

---

## Documentation

The complete software specification is located inside the `docs` directory.

Every contributor must read the documentation before modifying the project.

Documentation order:

1. Project Vision
2. Product Requirements
3. System Architecture
4. Domain Model
5. Backend Architecture
6. Frontend Architecture
7. Database Design
8. API Specification
9. Analytics Engine
10. Security
11. UI/UX Guidelines
12. Development Roadmap
13. AI Implementation Guide
14. Project State

---

## Development Workflow

Before implementing any feature:

1. Read `AI_INSTRUCTIONS.md`
2. Read every document inside `docs`
3. Analyze the repository
4. Produce an implementation plan
5. Implement only the requested milestone
6. Generate tests
7. Update documentation
8. Update `Project_State.md`

---

## Development Principles

Project Analytics follows:

- Clean Architecture
- Domain Driven Design (DDD)
- SOLID Principles
- Modular Design
- RESTful APIs
- Test-Driven Development where appropriate
- Explainable Business Intelligence

---

## Guiding Principles

- OpenProject is the source of truth.
- Business logic belongs in the backend.
- The frontend is responsible for presentation.
- Every dashboard answers a business question.
- Every business metric must be explainable.
- Every feature must be documented.
- Every implementation must be tested.

---

## Current Status

**Milestones:** M1–M12 complete; **M13 quality gate complete** (local test/handoff ready).  
**Next (when approved):** M14 OpenProject OAuth — not started by default.

### Local testing (company / developers)

See **[README-XTENSUS.md](./README-XTENSUS.md)** (short).  
If the zip already has `.env`, use it; start with `./scripts/run-backend.sh`.

- Demo checklist: [`docs/ops/demo-happy-path.md`](./docs/ops/demo-happy-path.md)
- Known limitations: [`docs/ops/known-limitations.md`](./docs/ops/known-limitations.md)

### Stack snapshot

- Backend: Spring Boot (auth, sync, analytics, portfolios, reports, recommendations, ops)
- Frontend: Angular (Home, Explorer, Portfolio, Project Detail, Connections, Reports)
- Database: Flyway through V11 (analytics extended metrics)
- Deployment: Docker Compose (local Postgres + Redis); backend via `run-backend.sh` or compose

See `docs/13_Project_State.md` for details.
  
OpenProject connection: set `OPENPROJECT_URL` and `OPENPROJECT_API_KEY`.

**How to run for company / Xtensus testing:** see **[README-XTENSUS.md](README-XTENSUS.md)**  
(Use `./scripts/run-backend.sh` — it loads `.env` and starts Spring Boot in one step.)

---

## License

This repository is intended for educational and engineering purposes.

---

End of Document.
