# System Architecture

Version: 1.0

Status: Draft

---

# 1. Purpose

This document defines the complete software architecture of Project Analytics.

It serves as the technical blueprint for the implementation of the platform and describes how every major component interacts with the others.

Every implementation must follow the architecture described in this document.

The architecture prioritizes maintainability, scalability, modularity, security and extensibility.

---

# 2. Architectural Vision

Project Analytics is an enterprise Decision Intelligence Platform built on top of OpenProject.

OpenProject remains responsible for project execution and operational management.

Project Analytics consumes project information, analyzes it, enriches it with business intelligence and exposes the results through dashboards, reports and APIs.

The application must never duplicate the responsibilities of OpenProject.

Instead, it transforms operational information into strategic information.

---

# 3. Architectural Goals

The architecture has the following goals:

- Separate business logic from presentation.
- Keep the platform modular.
- Support future growth without redesign.
- Allow independent development of backend and frontend.
- Minimize coupling between modules.
- Maximize code reuse.
- Ensure high testability.
- Support AI-assisted software development.
- Facilitate future cloud deployment.
- Keep OpenProject as the single source of truth.

---

# 4. Architectural Principles

The entire platform follows the following principles.

## Separation of Concerns

Every module is responsible for one specific concern.

Business logic, persistence, presentation and infrastructure remain separated.

---

## Single Responsibility

Every class, service and component has one clearly defined responsibility.

---

## Modularity

Each functional area is implemented independently.

Modules communicate only through well-defined interfaces.

---

## Scalability

The architecture must support increasing numbers of:

- Users
- Projects
- Portfolios
- Reports
- Dashboards

without architectural redesign.

---

## Maintainability

The platform must remain understandable by developers unfamiliar with the project.

Every module should be replaceable without affecting unrelated modules.

---

## Extensibility

Future features should be added by extending existing modules rather than modifying unrelated components.

---

## Testability

Every business capability must be independently testable.

---

## Explainability

Every calculated business metric must be explainable.

Users should always understand why a score or recommendation exists.

---

# 5. High-Level Architecture

Project Analytics follows a layered enterprise architecture.

```

```
                    +-----------------------+
                    |       Angular UI      |
                    +-----------+-----------+
                                |
                                |
                    REST / HTTPS
                                |
+-----------------------------------------------------------+
|                    Spring Boot Backend                    |
|-----------------------------------------------------------|
| Authentication                                            |
| Synchronization                                            |
| Analytics                                                  |
| Decision Intelligence                                      |
| Reporting                                                  |
| Notifications                                              |
| REST API                                                   |
+---------------------------+-------------------------------+
                            |
                            |
                 PostgreSQL / Redis
                            |
                            |
                   OpenProject REST API
```

```markdown

The frontend never communicates directly with OpenProject.

All communication passes through the backend.

Business logic never executes inside the frontend.

The backend acts as the single integration point between OpenProject and every client application.

---

# 6. Technology Stack

## Frontend

- Angular
- TypeScript
- Angular Signals
- Angular Router
- Angular CDK
- RxJS

---

## Backend

- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA
- Hibernate
- MapStruct
- Lombok
- Validation API

---

## Database

PostgreSQL

---

## Cache

Redis

---

## Build Tools

Maven

Node.js

npm

---

## Infrastructure

Docker

Docker Compose

Future:

- Kubernetes

---

## Monitoring

Prometheus

Grafana

OpenTelemetry

---

## Documentation

OpenAPI

Swagger

Markdown

Mermaid

---

# 7. System Components

The platform is composed of the following major components.

## Frontend

Responsible for:

- User interaction
- Navigation
- Dashboards
- Forms
- Charts
- Visualization

The frontend contains no business calculations.

---

## REST API

Acts as the communication layer between frontend and backend.

Responsible for:

- Authentication
- Validation
- Request routing
- Response formatting

---

## Authentication Module

Responsible for:

- Login
- Logout
- Authorization
- JWT validation
- User identity

---

## Synchronization Module

Responsible for:

- Connecting to OpenProject
- Importing data
- Updating local analytics
- Detecting changes
- Synchronization history

---

## Analytics Module

Responsible for:

- KPI calculation
- Health Score
- Risk Score
- Attention Score
- Portfolio analytics
- Historical metrics

---

## Decision Intelligence Module

Responsible for:

- Executive summaries
- Recommendations
- Insights
- Explainability
- Prioritization

---

## Reporting Module

Responsible for:

- PDF generation
- Excel export
- Scheduled reports
- Executive reports

---

## Persistence Layer

Responsible for:

- PostgreSQL
- Redis
- Data access
- Transactions

---

## External Integration Layer

Responsible for communicating with:

- OpenProject

Future integrations may include:

- Jira
- Microsoft Project
- Azure DevOps

without affecting internal modules.

---

# End of Part 1
# 8. Layered Architecture

The backend follows a layered architecture based on Clean Architecture principles.

Business rules remain independent from frameworks and infrastructure.

The layers are organized as follows:

```

```
Presentation Layer
        │
        ▼
Application Layer
        │
        ▼
Domain Layer
        │
        ▼
Infrastructure Layer
```

```markdown

### Presentation Layer

Responsibilities:

- REST Controllers
- Request Validation
- Authentication
- Response Formatting
- Error Handling

The Presentation Layer must never contain business logic.

---

### Application Layer

Responsibilities:

- Use Cases
- Orchestration
- Transactions
- Service Coordination

This layer coordinates business operations.

It should not contain persistence logic.

---

### Domain Layer

The Domain Layer is the heart of the application.

Responsibilities:

- Business Rules
- Domain Models
- Domain Services
- Business Policies
- Domain Events

This layer has no dependency on Spring Boot or any framework.

---

### Infrastructure Layer

Responsibilities:

- Database
- Redis
- External APIs
- OpenProject Integration
- Email
- File Storage
- Logging

Framework-specific code belongs only in this layer.

---

# 9. Communication Flow

All requests follow the same execution path.

```

```
Angular

↓

REST Controller

↓

Application Service

↓

Domain Service

↓

Repository

↓

PostgreSQL
```

```markdown

Synchronization requests follow a different flow.

```

```
Scheduler / Manual Request

↓

Synchronization Service

↓

OpenProject REST API

↓

Mapping Layer

↓

Validation

↓

Persistence

↓

Analytics Refresh

↓

Cache Refresh
```

```markdown

The frontend never communicates directly with OpenProject.

---

# 10. Synchronization Layer

The Synchronization Layer is responsible for importing operational data from OpenProject.

It acts as the gateway between external project data and internal business analytics.

## Responsibilities

- Retrieve projects
- Retrieve work packages
- Retrieve users
- Retrieve memberships
- Retrieve statuses
- Retrieve priorities
- Retrieve milestones
- Detect updates
- Store synchronization history
- Handle failures
- Retry failed synchronizations

---

## OpenProject credentials

The synchronization engine must not hard-code how OpenProject is authenticated.

Credentials are obtained through an `OpenProjectCredentialResolver` (or equivalent port). The OpenProject HTTP client applies authentication headers in a single place based on the resolved credentials.

**Current implementation (temporary)**

- Environment-configured API key (`OPENPROJECT_API_KEY`)
- HTTP Basic authentication with username `apikey`
- Valid until the dedicated OAuth milestone

**Future implementation (official long-term target)**

- OAuth 2.0 for OpenProject integrations
- One Workspace = one OpenProject instance
- Delivered in a dedicated later milestone
- Replaces or extends only the credential resolver and client auth application
- Must not redesign the synchronization engine, import pipeline, or analytics modules

No OAuth endpoints, token storage, or OAuth-related schema changes are required until that milestone.

---

## Primary user journey and analytics scopes

Official product flow:

1. **Connect Workspace** (API key now / OAuth later) to one OpenProject instance.
2. **Synchronize** operational data into local PostgreSQL.
3. **Workspace Dashboard & Analytics** immediately on local data (“All Projects”).

Users never call OpenProject while viewing dashboards. OpenProject is the synchronization source only.

Analytics scopes:

| Scope | Role |
|-------|------|
| Workspace | Primary — all synchronized projects for that OpenProject instance |
| Portfolio | Optional — user-defined subset of projects |
| Project | Detailed project analytics |

Portfolios are optional organizational subsets. A technical Default Portfolio may hold projects for FK integrity; primary UX is the Workspace Dashboard, not portfolio creation.

Portfolio membership: auto-assign to Default Portfolio on sync; manual reassignment to custom portfolios; near-term searchable multi-select UX; no dynamic rules required now.

---

## Synchronization Types

### Initial Synchronization

Executed when the platform connects to OpenProject for the first time.

Purpose:

- Download complete project information.

---

### Incremental Synchronization

Executed periodically.

Purpose:

- Retrieve only modified data.

---

### Manual Synchronization

Executed on user request.

Purpose:

- Force immediate synchronization.

---

### Scheduled Synchronization

Executed automatically according to configured intervals.

---

## Synchronization Workflow

```

```
Connect

↓

Authenticate

↓

Retrieve Projects

↓

Retrieve Related Resources

↓

Validate

↓

Normalize

↓

Persist

↓

Recalculate Analytics

↓

Refresh Cache

↓

Notify System
```

```markdown

---

## Failure Handling

Synchronization failures shall never corrupt existing data.

Failures are logged.

Failed operations may be retried automatically.

Users receive synchronization status information.

---

# 11. Analytics Layer

The Analytics Layer transforms synchronized data into business intelligence.

This layer contains all KPI calculations.

Responsibilities include:

- Portfolio KPIs
- Project KPIs
- Health Score
- Risk Score
- Attention Score
- Trend Analysis
- Budget Indicators
- Schedule Indicators
- Historical Analytics

No analytics are calculated by the frontend.

---

## Analytics Pipeline

```

```
Synchronized Data

↓

Validation

↓

Business Rules

↓

Metric Calculation

↓

Health Calculation

↓

Recommendation Generation

↓

Persistence

↓

Dashboard API
```

```markdown

---

## Analytics Principles

Every metric must satisfy the following rules:

- Deterministic
- Explainable
- Repeatable
- Versioned
- Testable

---

# 12. Decision Intelligence Layer

The Decision Intelligence Layer converts analytics into executive decisions.

Its objective is to reduce the amount of manual interpretation required by decision makers.

Responsibilities include:

- Executive Summaries
- Recommendations
- Attention Ranking
- Insight Generation
- Business Explanations
- Priority Detection

---

## Example

Instead of displaying:

Health Score = 54

The platform explains:

Health Score is 54 because:

- Budget exceeds forecast by 18%
- Three critical milestones are delayed
- Team velocity decreased during the last two iterations

This principle applies to every recommendation generated by the platform.

---

# 13. Reporting Layer

The Reporting Layer generates business reports.

Supported report types include:

- Executive Report
- Portfolio Report
- Project Report
- KPI Report
- Risk Report

Reports shall support:

- PDF
- Excel

Future formats may include HTML and PowerPoint.

---

# End of Part 2
# 14. Data Flow

Project Analytics follows a unidirectional data flow to ensure consistency, traceability and maintainability.

All business information originates from OpenProject.

No operational data is created or modified directly by Project Analytics.

The complete flow is illustrated below.

```

```
OpenProject
      │
      ▼
Synchronization Layer
      │
      ▼
Validation Layer
      │
      ▼
Persistence Layer
      │
      ▼
Analytics Engine
      │
      ▼
Decision Intelligence
      │
      ▼
REST API
      │
      ▼
Angular Frontend
```

```markdown

Every business metric displayed to users must originate from this pipeline.

---

# 15. Request Flow

The following sequence describes a typical dashboard request.

```

```
User

↓

Angular

↓

Authentication

↓

REST Controller

↓

Application Service

↓

Analytics Service

↓

Repository

↓

PostgreSQL / Redis

↓

Application Service

↓

REST Controller

↓

Angular

↓

Dashboard
```

```markdown

The frontend must never perform business calculations.

The backend always returns complete business objects ready for visualization.

---

# 16. Caching Strategy

Redis is used to reduce database load and improve dashboard performance.

The following information may be cached:

- Dashboard summaries
- Portfolio KPIs
- Project KPIs
- Frequently requested reports
- User permissions
- Application configuration

Caches must be invalidated whenever synchronization updates the underlying data.

Business data must never become inconsistent because of outdated cache entries.

---

## Cache Principles

- Cache only computed data.
- Never cache mutable business logic.
- Prefer automatic invalidation.
- Keep cache expiration configurable.
- Cache must improve performance without affecting correctness.

---

# 17. Error Handling Strategy

The application must handle failures consistently across every layer.

Errors shall be categorized into:

## Validation Errors

Examples:

- Missing required fields
- Invalid parameters
- Incorrect formats

Return HTTP 400.

---

## Authentication Errors

Examples:

- Invalid credentials
- Expired token
- Unauthorized request

Return HTTP 401.

---

## Authorization Errors

Examples:

- Missing permissions
- Access denied

Return HTTP 403.

---

## Resource Errors

Examples:

- Project not found
- Report not found

Return HTTP 404.

---

## Business Errors

Examples:

- Synchronization already running
- Invalid business operation

Return HTTP 409.

---

## Internal Errors

Unexpected exceptions.

Return HTTP 500.

Internal implementation details must never be exposed to users.

Every unexpected exception must be logged.

---

# 18. Logging Strategy

Logging must provide sufficient information for debugging, monitoring and auditing.

Each log entry shall include:

- Timestamp
- Severity
- Module
- Request Identifier
- User Identifier (when applicable)
- Message

Sensitive information must never be written to logs.

---

## Log Levels

DEBUG

Development information.

---

INFO

Normal application events.

Examples:

- Login
- Synchronization started
- Report generated

---

WARN

Recoverable problems.

Examples:

- Slow API response
- Retry attempt
- Cache miss

---

ERROR

Unexpected failures requiring investigation.

---

# 19. Monitoring

The application shall expose operational metrics.

Metrics include:

- API response time
- Synchronization duration
- Number of synchronized projects
- Database performance
- Cache hit ratio
- Active users
- Report generation time
- Error rate
- Memory usage
- CPU usage

Prometheus collects metrics.

Grafana visualizes metrics.

---

# 20. Performance Requirements

The platform shall satisfy the following objectives.

Dashboard loading:

Less than 2 seconds.

---

Cached API request:

Less than 500 milliseconds.

---

Synchronization:

Incremental synchronization should complete as quickly as practical for the amount of changed data while preserving correctness.

---

Database:

Queries shall be optimized through indexes and pagination.

---

Frontend:

Pages should remain responsive while data is loading.

Loading indicators must always be displayed.

---

# 21. Scalability

The architecture is designed to support future growth.

Future improvements may include:

- Multiple backend instances
- Kubernetes deployment
- Load balancing
- Distributed caching
- Message queues
- Event-driven architecture
- Multiple OpenProject instances
- Multi-tenancy

These additions must not require redesign of existing business modules.

---

# 22. Architecture Constraints

The following rules are mandatory.

- OpenProject is the single source of truth.
- Business logic belongs exclusively in the backend.
- The frontend performs no business calculations.
- Every API must be documented.
- Every module must remain independent.
- Every feature must be testable.
- Every business metric must be explainable.
- Every service must have a single responsibility.
- Circular dependencies are forbidden.
- Direct database access from the frontend is forbidden.
- Business rules must never be duplicated.

---

# 23. Future Evolution

The architecture is intentionally designed to support future capabilities including:

- AI-powered forecasting
- Predictive risk analysis
- Natural language querying
- Advanced portfolio optimization
- Additional project management integrations
- Plugin architecture
- Mobile applications
- Enterprise SaaS deployment

Future features should be implemented by extending existing modules rather than modifying the core architecture.

---

# End of Document