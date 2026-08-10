# Error Catalog

Version: 1.0

Status: Draft

---

# 1. Purpose

This document defines every business error that may be returned by Project Analytics.

The objectives are:

- Consistent API responses
- Predictable error handling
- Easier debugging
- Easier frontend implementation
- Better user experience

Every error returned by the backend shall reference one of the error codes defined in this document.

---

# 2. Error Response Format

Every error response shall follow this structure.

```json
{
  "success": false,
  "error": {
    "code": "PROJECT_001",
    "message": "Project not found.",
    "details": [],
    "timestamp": "2026-08-15T09:42:11Z",
    "path": "/api/v1/projects/15"
  }
}
```

---

# 3. Error Categories

| Prefix | Category |
|---------|----------|
| AUTH | Authentication |
| USER | User |
| PROJECT | Project |
| PORTFOLIO | Portfolio |
| SYNC | Synchronization |
| ANALYTICS | Analytics |
| REPORT | Reporting |
| DASHBOARD | Dashboard |
| ALERT | Alerts |
| VALIDATION | Validation |
| SYSTEM | Internal System |

---

# 4. Authentication Errors

| Code | HTTP | Description |
|------|------|-------------|
| AUTH_001 | 401 | Invalid credentials |
| AUTH_002 | 401 | JWT token expired |
| AUTH_003 | 401 | JWT token invalid |
| AUTH_004 | 401 | Authentication required |
| AUTH_005 | 403 | Access denied |
| AUTH_006 | 403 | Insufficient permissions |

---

# 5. User Errors

| Code | HTTP | Description |
|------|------|-------------|
| USER_001 | 404 | User not found |
| USER_002 | 409 | Email already exists |
| USER_003 | 409 | Username already exists |
| USER_004 | 400 | Invalid password |
| USER_005 | 400 | Invalid user preferences |

---

# 6. Project Errors

| Code | HTTP | Description |
|------|------|-------------|
| PROJECT_001 | 404 | Project not found |
| PROJECT_002 | 409 | Duplicate project |
| PROJECT_003 | 400 | Invalid project state |
| PROJECT_004 | 400 | Invalid project identifier |

---

# 7. Portfolio Errors

| Code | HTTP | Description |
|------|------|-------------|
| PORTFOLIO_001 | 404 | Portfolio not found |
| PORTFOLIO_002 | 409 | Portfolio already exists |
| PORTFOLIO_003 | 400 | Invalid portfolio configuration |

---

# 8. Synchronization Errors

| Code | HTTP | Description |
|------|------|-------------|
| SYNC_001 | 500 | Synchronization failed |
| SYNC_002 | 408 | OpenProject timeout |
| SYNC_003 | 409 | Synchronization already running |
| SYNC_004 | 503 | OpenProject unavailable |
| SYNC_005 | 400 | Invalid synchronization request |
| SYNC_006 | 500 | Data mapping failed |

---

# 9. Analytics Errors

| Code | HTTP | Description |
|------|------|-------------|
| ANALYTICS_001 | 500 | Health score calculation failed |
| ANALYTICS_002 | 500 | Risk score calculation failed |
| ANALYTICS_003 | 500 | Attention score calculation failed |
| ANALYTICS_004 | 500 | Recommendation generation failed |
| ANALYTICS_005 | 404 | Analytics unavailable |

---

# 10. Reporting Errors

| Code | HTTP | Description |
|------|------|-------------|
| REPORT_001 | 404 | Report not found |
| REPORT_002 | 500 | Report generation failed |
| REPORT_003 | 409 | Report already exists |
| REPORT_004 | 500 | Report export failed |

---

# 10.1 Recommendation Errors

| Code | HTTP | Description |
|------|------|-------------|
| RECOMMENDATION_001 | 404 | Recommendation not found |
| ANALYTICS_004 | 500 | Recommendation generation failed |

---

# 11. Dashboard Errors

| Code | HTTP | Description |
|------|------|-------------|
| DASHBOARD_001 | 404 | Dashboard not found |
| DASHBOARD_002 | 400 | Invalid widget configuration |
| DASHBOARD_003 | 409 | Duplicate dashboard |

---

# 12. Alert Errors

| Code | HTTP | Description |
|------|------|-------------|
| ALERT_001 | 404 | Alert not found |
| ALERT_002 | 409 | Alert already acknowledged |
| ALERT_003 | 400 | Invalid alert state |

---

# 13. Validation Errors

| Code | HTTP | Description |
|------|------|-------------|
| VALIDATION_001 | 400 | Required field missing |
| VALIDATION_002 | 400 | Invalid request format |
| VALIDATION_003 | 400 | Invalid parameter value |
| VALIDATION_004 | 400 | Constraint violation |
| VALIDATION_005 | 400 | Unsupported enum value |

---

# 14. System Errors

| Code | HTTP | Description |
|------|------|-------------|
| SYSTEM_001 | 500 | Unexpected server error |
| SYSTEM_002 | 503 | Service unavailable |
| SYSTEM_003 | 500 | Database error |
| SYSTEM_004 | 500 | Cache unavailable |
| SYSTEM_005 | 500 | Internal configuration error |

---

# 15. Error Handling Rules

Every error shall:

- Have a unique error code.
- Return the appropriate HTTP status.
- Include a human-readable message.
- Exclude sensitive implementation details.
- Be logged on the backend.

---

# 16. Frontend Behavior

The frontend should:

- Display user-friendly error messages.
- Log technical details only in development.
- Redirect users appropriately after authentication errors.
- Allow retry for recoverable errors such as temporary synchronization failures.

---

# 17. Logging Requirements

Backend logs should include:

- Error code
- Request ID
- User ID (if available)
- Timestamp
- Stack trace (server logs only)

Stack traces must never be returned in API responses.

---

# 18. Future Extensions

Additional categories may be introduced for:

- Notifications
- AI Services
- Multi-tenancy
- Plugin System
- External Integrations

New categories should follow the existing naming convention.

---

# 19. AI Implementation Notes

When implementing error handling:

- Use only documented error codes.
- Do not invent new codes without updating this document.
- Map business exceptions to the appropriate catalog entry.
- Keep API responses consistent across all modules.
- Ensure every documented error has corresponding automated tests where applicable.

---

# End of Document