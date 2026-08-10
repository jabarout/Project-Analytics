# Coding Standards

Version: 1.0

Status: Draft

---

# 1. Purpose

This document defines the coding standards for Project Analytics.

Its objective is to ensure that all code written by developers and AI assistants remains:

- Consistent
- Readable
- Maintainable
- Testable
- Scalable

These standards apply to every repository in the project.

---

# 2. General Principles

Every implementation shall follow these principles:

- Readability over cleverness.
- Explicit code over implicit behavior.
- Simplicity over complexity.
- Composition over inheritance.
- Single Responsibility Principle.
- Avoid premature optimization.
- Eliminate duplicated code.

Code should be easy to understand without requiring additional explanation.

---

# 3. Repository Organization

The repository follows a feature-first architecture.

Backend modules:

- authentication
- synchronization
- portfolio
- project
- analytics
- dashboard
- reporting
- recommendation
- notification

Frontend features follow the same organization whenever possible.

No module should directly depend on another module's internal implementation.

---

# 4. Naming Conventions

## Packages

Use lowercase.

Example:

```text
com.projectanalytics.analytics
```

---

## Classes

Use PascalCase.

Examples:

```text
ProjectService

RecommendationEngine

HealthCalculator
```

---

## Interfaces

Interfaces describe behavior.

Examples:

```text
AuthenticationService

ReportGenerator
```

Avoid prefixes such as:

```text
IAuthenticationService
```

---

## Methods

Use camelCase.

Methods should describe actions.

Examples:

```text
calculateHealthScore()

generateRecommendations()

findProjectById()

synchronizeWorkspace()
```

Avoid ambiguous names such as:

```text
process()

execute()

run()
```

unless their purpose is immediately obvious.

---

## Variables

Use descriptive camelCase.

Good:

```text
projectHealthScore

remainingBudget

currentWorkspace
```

Bad:

```text
a

tmp

value

obj
```

---

## Constants

Use UPPER_SNAKE_CASE.

Example:

```text
DEFAULT_PAGE_SIZE

JWT_EXPIRATION_MINUTES

MAX_RETRY_COUNT
```

---

# 5. Java Standards

Java version:

21

---

Use:

- Records for immutable DTOs when appropriate.
- Enums instead of magic strings.
- Optional only as a return type.
- Constructor injection.
- try-with-resources when managing resources.

Avoid:

- Field injection
- Static mutable state
- Wildcard imports
- Raw types

---

# 6. Spring Boot Standards

Controllers:

- Thin
- Stateless
- Request validation only

Services:

- Business logic
- Transactions
- Orchestration

Repositories:

- Persistence only

Configuration:

- Externalized
- Environment-specific

---

# 7. Angular Standards

Use:

- Standalone Components
- Signals
- Reactive Forms
- Lazy Loading

Avoid:

- Business calculations inside components
- Large components
- Direct DOM manipulation

Components should communicate through Inputs, Outputs, and services.

---

# 8. TypeScript Standards

Enable strict mode.

Prefer:

- readonly properties
- explicit types
- interfaces for API contracts

Avoid:

- any
- unnecessary type assertions
- duplicated interfaces

---

# 9. REST API Standards

Every endpoint shall:

- Use nouns in URLs.
- Return JSON.
- Use DTOs.
- Return appropriate HTTP status codes.
- Validate input.

Example:

```text
GET /projects/{id}

POST /reports

PATCH /alerts/{id}/acknowledge
```

---

# 10. Exception Standards

Create domain-specific exceptions.

Examples:

```text
ProjectNotFoundException

SynchronizationException

ReportGenerationException
```

Do not throw generic `Exception` or `RuntimeException` directly from business code.

Use a global exception handler to translate exceptions into API responses.

---

# 11. Logging Standards

Log:

- Important business events
- Warnings
- Errors

Do not log:

- Passwords
- Tokens
- Secrets
- Personal sensitive information

Use parameterized logging instead of string concatenation.

Example:

```java
logger.info("Project {} synchronized successfully.", projectId);
```

---

# 12. Documentation Standards

Every public class should include a concise description when its purpose is not immediately obvious.

Document:

- Complex algorithms
- Non-trivial business rules
- Public APIs

Avoid comments that merely repeat the code.

Bad:

```java
// Increment i
i++;
```

Good:

```java
// Recalculate the health score after synchronization completes.
```

---

# 13. Code Quality Rules

Methods should:

- Have a single responsibility.
- Prefer fewer than ~40 lines where practical.
- Minimize nesting by using early returns.
- Use meaningful parameter names.

Classes should:

- Have a clear purpose.
- Avoid excessive size.
- Minimize dependencies.

---

# 14. Refactoring Rules

Refactoring should:

- Preserve behavior.
- Improve readability.
- Reduce duplication.
- Simplify maintenance.

Large refactorings should be performed separately from feature work whenever possible.

---

# 15. Performance Guidelines

Avoid:

- N+1 queries
- Unnecessary object creation
- Duplicate API calls
- Repeated calculations

Prefer:

- Pagination
- Caching where appropriate
- Batch operations
- Efficient database queries

Optimize based on evidence rather than assumptions.

---

# 16. Security Guidelines

Always:

- Validate input.
- Encode output where required.
- Externalize secrets.
- Verify authorization.
- Follow the Security document.

Never:

- Hardcode credentials.
- Expose stack traces.
- Log sensitive data.

---

# 17. Code Review Checklist

Before merging, verify:

- Code compiles.
- Tests pass.
- Documentation updated if required.
- No duplicated logic.
- Naming follows standards.
- Security considerations addressed.
- Performance implications reviewed.

---

# 18. AI Implementation Notes

When generating code, AI assistants shall:

- Follow these coding standards.
- Prefer consistency over personal style.
- Reuse existing classes whenever possible.
- Avoid introducing unnecessary abstractions.
- Explain architectural deviations before implementing them.
- Generate tests alongside production code when appropriate.

---

# End of Document