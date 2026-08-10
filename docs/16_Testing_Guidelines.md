# Testing Guidelines

Version: 1.0

Status: Draft

---

# 1. Purpose

This document defines the testing strategy for Project Analytics.

Its objective is to ensure that every feature is reliable, maintainable, and behaves correctly before being released.

Testing is considered an integral part of development rather than an optional activity.

---

# 2. Testing Principles

Testing shall follow these principles:

- Test business behavior, not implementation details.
- Automate whenever possible.
- Keep tests deterministic.
- Keep tests independent.
- Make tests easy to understand.
- Fail fast.
- Avoid flaky tests.

Every bug fixed should include a regression test whenever practical.

---

# 3. Testing Pyramid

The project follows the testing pyramid.

```
                E2E
               /   \
          Integration
          /         \
       Unit Tests
```

Priority:

- Many Unit Tests
- Some Integration Tests
- Few End-to-End Tests

---

# 4. Backend Testing

Backend tests include:

- Unit Tests
- Repository Tests
- Integration Tests
- API Tests
- Security Tests

Frameworks:

- JUnit 5
- Mockito
- Testcontainers
- Spring Boot Test

---

# 5. Frontend Testing

Frontend tests include:

- Component Tests
- Service Tests
- Route Tests
- End-to-End Tests

Frameworks:

- Jasmine
- Karma
- Cypress

---

# 6. Unit Tests

Unit tests verify individual classes.

Examples:

- Health Score calculation
- Risk Score calculation
- Recommendation generation
- Utility methods

Characteristics:

- Fast
- Independent
- No database
- No external services

---

# 7. Integration Tests

Integration tests verify interaction between components.

Examples:

- Service + Repository
- REST Controller + Service
- Synchronization Pipeline
- Database Persistence

Use Testcontainers for PostgreSQL and Redis where appropriate.

---

# 8. Repository Tests

Repository tests verify:

- Queries
- Relationships
- Constraints
- Pagination
- Sorting

Use realistic datasets.

---

# 9. API Tests

Every REST endpoint should be tested.

Verify:

- Status codes
- Response body
- Validation
- Authentication
- Authorization
- Error responses

---

# 10. Security Tests

Security tests verify:

- Login
- JWT validation
- Unauthorized access
- Forbidden access
- Role permissions
- Invalid tokens

Sensitive endpoints require dedicated tests.

---

# 11. Analytics Tests

The Analytics Engine requires extensive testing.

Verify:

- KPI calculations
- Health Score
- Risk Score
- Attention Score
- Trend analysis
- Recommendation generation

Identical inputs must always produce identical outputs unless explicitly designed otherwise.

---

# 12. Synchronization Tests

Verify:

- Initial synchronization
- Incremental synchronization
- Retry behavior
- Failure recovery
- Mapping
- Data consistency

Synchronization should not create duplicate records.

---

# 13. Frontend Component Tests

Verify:

- Rendering
- User interaction
- Input validation
- State changes
- Error handling

Components should be tested independently where practical.

---

# 14. End-to-End Tests

Critical user journeys include:

- Login
- Dashboard loading
- Portfolio navigation
- Project analytics
- Report generation
- Synchronization

These tests simulate real user behavior.

---

# 15. Performance Tests

Performance testing should verify:

- Dashboard loading
- API response times
- Synchronization duration
- Large dataset handling

Benchmark results should be tracked over time.

---

# 16. Test Data

Test data should be:

- Predictable
- Repeatable
- Independent
- Representative

Avoid relying on production data.

Factories and builders are recommended for creating test objects.

---

# 17. Mocking Guidelines

Mock:

- External APIs
- Email services
- Time-dependent services
- Third-party integrations

Do not mock business logic unnecessarily.

Mock only external dependencies.

---

# 18. Code Coverage

Recommended minimum coverage:

| Layer | Target |
|--------|--------:|
| Domain | 95% |
| Services | 90% |
| Controllers | 80% |
| Repositories | 80% |
| Utilities | 90% |
| Frontend Components | 80% |

Coverage is an indicator, not a goal by itself.

High coverage does not guarantee high-quality tests.

---

# 19. Continuous Integration

Every Pull Request should automatically execute:

- Unit Tests
- Integration Tests
- API Tests
- Frontend Tests
- Static Analysis

Builds should fail if mandatory tests fail.

---

# 20. Regression Testing

Whenever a defect is fixed:

- Reproduce the issue.
- Add an automated test.
- Verify the fix.
- Ensure the test prevents recurrence.

---

# 21. Test Naming

Tests should clearly describe expected behavior.

Examples:

```
shouldCalculateHealthScoreWhenProjectIsHealthy()

shouldRejectInvalidJwtToken()

shouldGenerateRecommendationForCriticalRisk()
```

Avoid generic names such as:

```
test1()

testMethod()

verify()
```

---

# 22. Test Organization

Backend:

```
src/test/java
```

Frontend:

```
src/**/*.spec.ts
```

Mirror the production package structure whenever possible.

---

# 23. Best Practices

- One assertion objective per test.
- Keep tests short.
- Avoid duplicated setup.
- Use descriptive test data.
- Prefer builders over long constructors.
- Keep tests isolated.
- Avoid sleeps and arbitrary delays.

---

# 24. AI Implementation Notes

AI assistants shall:

- Generate tests together with production code.
- Avoid leaving new code untested.
- Reuse existing test utilities.
- Keep tests readable.
- Prefer behavior-focused assertions.
- Update tests whenever business rules change.

---

# End of Document