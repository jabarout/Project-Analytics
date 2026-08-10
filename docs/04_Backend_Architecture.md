# Backend Architecture

Version: 1.0

Status: Draft

---

# 1. Purpose

This document defines the backend architecture of Project Analytics.

The backend is responsible for all business logic, analytics, synchronization, security, persistence, reporting, and communication with OpenProject.

The frontend must never implement business rules.

---

# 2. Technology Stack

Framework: Spring Boot

Language: Java 21

Build Tool: Maven

ORM: Spring Data JPA + Hibernate

Database: PostgreSQL

Cache: Redis

Security: Spring Security + JWT

Documentation: OpenAPI / Swagger

Testing:

- JUnit 5
- Mockito
- Testcontainers

---

# 3. Architectural Style

The backend follows:

- Clean Architecture
- Domain Driven Design
- SOLID Principles
- Feature-based modular architecture

Each feature owns its own:

- Controller
- Service
- DTOs
- Mapper
- Repository
- Domain Model
- Tests

---

# 4. Package Structure

```
com.projectanalytics

├── common
│
├── authentication
│
├── synchronization
│
├── portfolio
│
├── project
│
├── analytics
│
├── dashboard
│
├── reporting
│
├── recommendation
│
├── notification
│
├── configuration
│
└── infrastructure
```

Each package is independent.

---

# 5. Common Module

Contains reusable components.

Examples:

- Exceptions
- Constants
- Utilities
- Generic Responses
- Base Entities
- Pagination
- Validation
- Logging Helpers

Business logic must never be placed here.

---

# 6. Authentication Module

Responsibilities:

- Login
- Logout
- JWT Generation
- JWT Validation
- User Management
- Role Management
- Password Encoding

Exposes authentication endpoints.

---

# 7. Synchronization Module

Responsible for communication with OpenProject.

Responsibilities:

- API Client
- Synchronization Scheduler
- Incremental Synchronization
- Manual Synchronization
- Mapping
- Validation
- Retry Logic
- Synchronization History

No analytics are calculated here.

OpenProject authentication is not owned by the synchronization orchestration services. Infrastructure resolves credentials through `OpenProjectCredentialResolver` (default: environment API key) and the HTTP client applies Authorization headers. This port is designed so a future OAuth 2.0 implementation can be added without redesigning the synchronization engine.

---

# 8. Portfolio Module

Responsibilities:

- Portfolio Management
- Portfolio KPIs
- Portfolio Dashboard
- Portfolio Statistics

Provides aggregated portfolio information.

---

# 9. Project Module

Responsibilities:

- Project Information
- Project Dashboard
- Timeline
- Budget
- Milestones
- Team Information

Does not calculate business metrics.

---

# 10. Analytics Module

The heart of the backend.

Responsible for:

- Health Score
- Risk Score
- Attention Score
- KPI Calculation
- Trend Analysis
- Historical Metrics

Every algorithm belongs here.

---

# 11. Dashboard Module

Provides aggregated information for dashboards.

Responsibilities:

- Dashboard DTOs
- Widget Data
- Dashboard Composition

The frontend receives ready-to-display objects.

---

# 12. Recommendation Module

Generates business recommendations.

Responsibilities:

- Recommendation Engine
- Recommendation Prioritization
- Explainability

Recommendations must always include explanations.

---

# 13. Reporting Module

Responsible for report generation.

Supported formats:

- PDF
- Excel

Future:

- HTML
- PowerPoint

---

# 14. Notification Module

Responsible for:

- Alerts
- Notifications
- Synchronization Messages
- Future Email Support

---

# 15. Controllers

Controllers expose REST endpoints.

Controllers must:

- Validate requests
- Call services
- Return DTOs

Controllers must never contain business logic.

---

# 16. Services

Services implement application use cases.

Services:

- Coordinate repositories
- Execute business operations
- Handle transactions

Services should remain focused.

---

# 17. Repositories

Repositories provide persistence.

Repositories:

- Query Database
- Save Entities
- Delete Entities

Repositories never contain business rules.

---

# 18. DTOs

DTOs are the only objects exchanged through the API.

Rules:

- Never expose entities.
- Separate Request DTOs from Response DTOs.
- Keep DTOs immutable whenever possible.

---

# 19. Mappers

Responsible for conversion between:

- Entities
- DTOs
- Domain Objects

Recommended library:

MapStruct

---

# 20. Exception Handling

Global Exception Handler handles:

- Validation Errors
- Authentication Errors
- Authorization Errors
- Business Exceptions
- Database Exceptions
- Unknown Exceptions

All responses follow the same error format.

---

# 21. Validation

Validation is performed before business logic executes.

Validation includes:

- Required Fields
- Business Constraints
- Data Formats
- Range Validation

---

# 22. Transactions

Transactional operations belong to the service layer.

Transactions must:

- Remain short
- Avoid unnecessary locking
- Roll back on failures

---

# 23. Caching

Redis is used for:

- Dashboard Data
- KPI Results
- User Permissions
- Configuration

Cache invalidation occurs after synchronization.

---

# 24. Scheduling

Scheduled jobs include:

- Automatic Synchronization
- Cache Cleanup
- Report Generation
- Maintenance Tasks

Spring Scheduler manages scheduled execution.

---

# 25. Logging

Every service logs:

- Start
- Success
- Failure

Sensitive information is never logged.

---

# 26. Security

Security responsibilities include:

- JWT Validation
- Authorization
- Role Verification
- Endpoint Protection

Business services must never trust client input.

---

# 27. Testing Strategy

Required tests:

- Unit Tests
- Integration Tests
- Repository Tests
- Controller Tests

Coverage should focus on business logic.

---

# 28. Backend Principles

- Controllers are thin.
- Services contain use cases.
- Domain contains business rules.
- Infrastructure contains framework code.
- Never duplicate logic.
- Keep dependencies unidirectional.
- Every feature is independently testable.

---

# 29. AI Implementation Notes

When implementing the backend:

- Follow the documented package structure.
- Reuse existing services whenever possible.
- Never expose entities through REST.
- Use constructor dependency injection.
- Prefer composition over inheritance.
- Generate tests for every feature.
- Keep modules independent.
- Follow Clean Architecture.
- Respect SOLID principles.

---

# End of Document