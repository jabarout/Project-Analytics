# API Specification

Version: 1.0

Status: Draft

---

# 1. Purpose

This document defines the REST API exposed by the Project Analytics backend.

The API enables the Angular frontend and future external clients to access analytical data, dashboards, reports, and administrative functions.

All APIs return JSON unless otherwise specified.

The API follows RESTful design principles.

---

# 2. General Principles

The API shall:

- Use HTTPS exclusively.
- Be stateless.
- Exchange JSON payloads.
- Use JWT authentication.
- Return consistent response formats.
- Be fully documented with OpenAPI.

---

# 3. Base URL

```
/api/v1
```

Example:

```
/api/v1/projects
```

---

# 4. Authentication

Protected endpoints require:

```
Authorization: Bearer <JWT_TOKEN>
```

Expired or invalid tokens return:

```
401 Unauthorized
```

---

# 5. Standard Response Format

Successful responses:

```json
{
  "success": true,
  "data": {},
  "timestamp": "2026-01-01T12:00:00Z"
}
```

---

Error responses:

```json
{
  "success": false,
  "error": {
    "code": "PROJECT_NOT_FOUND",
    "message": "Project does not exist."
  },
  "timestamp": "2026-01-01T12:00:00Z"
}
```

---

# 6. Authentication Endpoints

## Login

POST

```
/auth/login
```

Request

```json
{
  "username": "",
  "password": ""
}
```

Response

```json
{
  "token": "",
  "expiresAt": ""
}
```

---

## Logout

POST

```
/auth/logout
```

---

## Current User

GET

```
/auth/me
```

Returns authenticated user information.

---

# 7. Workspace Endpoints

## List Workspaces

GET

```
/workspaces
```

---

## Get Workspace

GET

```
/workspaces/{id}
```

---

## Synchronize Workspace

POST

```
/workspaces/{id}/synchronize
```

Starts manual synchronization.

---

## Synchronization Status

GET

```
/workspaces/{id}/synchronization
```

Returns latest synchronization information.

---

## Workspace Dashboard (Primary Analytics Scope)

GET

```
/workspaces/{id}/dashboard
```

Returns a ready-to-display dashboard for **all synchronized projects** in the workspace (“All Projects”).

This is the primary post-synchronization analytics surface. Users must not be required to create a portfolio before this endpoint is useful.

GET

```
/workspaces/{id}/kpis
```

Returns workspace-level KPIs aggregated from local PostgreSQL for all projects in the workspace.

---

# 8. Portfolio Endpoints

Portfolios are **optional** organizational subsets within a workspace. Primary analytics remain workspace-scoped.

## List Portfolios

GET

```
/portfolios
```

Optional filter: `workspaceId`.

---

## Portfolio Details

GET

```
/portfolios/{id}
```

---

## Portfolio Dashboard

GET

```
/portfolios/{id}/dashboard
```

Returns dashboard DTO for **member projects only**.

---

## Portfolio KPIs

GET

```
/portfolios/{id}/kpis
```

---

# 9. Project Endpoints

## List Projects

GET

```
/projects
```

Supports:

- pagination
- sorting
- filtering

---

## Project Details

GET

```
/projects/{id}
```

---

## Project Dashboard

GET

```
/projects/{id}/dashboard
```

---

## Project Timeline

GET

```
/projects/{id}/timeline
```

---

## Project Work Packages

GET

```
/projects/{id}/work-packages
```

---

# 10. Analytics Endpoints

## Health Score

GET

```
/analytics/projects/{id}/health
```

---

## Risk Score

GET

```
/analytics/projects/{id}/risk
```

---

## Attention Score

GET

```
/analytics/projects/{id}/attention
```

---

## KPI List

GET

```
/analytics/projects/{id}/kpis
```

---

## Trend Analysis

GET

```
/analytics/projects/{id}/trends
```

---

# 11. Dashboard Endpoints

## Executive Dashboard

GET

```
/dashboards/executive
```

---

## Portfolio Dashboard

GET

```
/dashboards/portfolio/{id}
```

---

## Project Dashboard

GET

```
/dashboards/project/{id}
```

---

# 12. Recommendation Endpoints

## Recommendations

GET

```
/projects/{id}/recommendations
```

---

## Recommendation Details

GET

```
/recommendations/{id}
```

---

# 13. Report Endpoints

## Generate Report

POST

```
/reports
```

---

## Report Status

GET

```
/reports/{id}
```

---

## Download Report

GET

```
/reports/{id}/download
```

Returns:

- PDF
- Excel

---

# 14. Alert Endpoints

## Alerts

GET

```
/alerts
```

---

## Alert Details

GET

```
/alerts/{id}
```

---

## Acknowledge Alert

PATCH

```
/alerts/{id}/acknowledge
```

---

# 15. User Endpoints

## Current User

GET

```
/users/me
```

---

## Update Preferences

PUT

```
/users/me/preferences
```

---

## Theme

PATCH

```
/users/me/theme
```

---

# 16. Query Parameters

Supported parameters:

Pagination

```
?page=0

&size=20
```

Sorting

```
?sort=name

&direction=asc
```

Filtering

```
?status=ACTIVE

&priority=HIGH
```

Search

```
?q=construction
```

---

# 17. HTTP Status Codes

200 OK

201 Created

202 Accepted

204 No Content

400 Bad Request

401 Unauthorized

403 Forbidden

404 Not Found

409 Conflict

422 Unprocessable Entity

500 Internal Server Error

---

# 18. Versioning

Current version:

```
v1
```

Future versions:

```
/api/v2
```

Older versions should remain supported for a defined deprecation period.

---

# 19. Rate Limiting

Future support:

- Per-user limits
- Per-IP limits
- Administrative limits

Excessive requests return:

```
429 Too Many Requests
```

---

# 20. Security

Every endpoint shall:

- Require HTTPS.
- Validate JWT.
- Verify permissions.
- Validate input.
- Sanitize output.

Sensitive fields must never be exposed.

---

# 21. OpenAPI

Every endpoint must include:

- Summary
- Description
- Parameters
- Request Schema
- Response Schema
- Error Responses
- Authentication Requirements

Swagger UI shall be enabled in development environments.

---

# 22. API Principles

- Resource-oriented URLs
- Stateless communication
- Consistent naming
- Consistent response format
- Idempotent GET operations
- Proper HTTP verbs
- Explicit error messages

---

# 23. AI Implementation Notes

When implementing the API:

- Follow REST conventions.
- Never expose database entities directly.
- Use DTOs for all requests and responses.
- Validate every request.
- Return meaningful HTTP status codes.
- Document every endpoint with OpenAPI annotations.
- Keep controllers thin and delegate business logic to services.

---

# End of Document