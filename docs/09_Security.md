# Security

Version: 1.0

Status: Draft

---

# 1. Purpose

This document defines the security architecture of Project Analytics.

Security protects users, business information, synchronized project data, and system resources while ensuring compliance with modern security best practices.

Security is implemented as a cross-cutting concern throughout the entire application.

---

# 2. Security Objectives

The platform shall ensure:

- Confidentiality
- Integrity
- Availability
- Accountability
- Auditability

Every architectural decision should support one or more of these objectives.

---

# 3. Authentication

Authentication verifies the identity of users.

The platform uses:

- Spring Security
- JWT Authentication

Authentication flow:

```

```
User

↓

Login

↓

Credential Validation

↓

JWT Generation

↓

Authenticated Requests

↓

Token Validation

↓

Authorized Resource
```

```markdown

Passwords are never stored in plain text.

---

# 4. Password Security

Passwords shall:

- Be hashed using BCrypt.
- Never be logged.
- Never be returned through APIs.

Minimum password policy:

- Minimum length
- Uppercase letter
- Lowercase letter
- Number
- Special character

Password rules should remain configurable.

---

# 5. Authorization

Authorization determines which resources a user may access.

Permissions are enforced on the backend.

The frontend may hide unavailable actions for usability, but authorization decisions are never trusted to the client.

## 5.1 Product access model (frozen) — Hybrid

| Rule | Detail |
|------|--------|
| **PA account ≠ OP access** | Sign-up/login creates a Project Analytics identity only. |
| **OP eligibility for connect** | Establishing a workspace requires OpenProject auth (OAuth preferred; API key alt) **plus** an eligibility check (default: OP `admin` or Project admin role). Credentials stay server-side. |
| **Connector = PA Workspace Admin** | Successful eligible connect grants that PA user Workspace Admin + analytics access for the workspace. |
| **App-owned ongoing grants** | Additional PA users receive analytics access via Workspace Admin grants (M15); they need not OAuth separately. |
| **Workspace isolation** | Backend must enforce membership on every workspace-scoped API. UI hiding is not sufficient. |
| **Admin concepts** | PA Platform Admin ≠ PA Workspace Admin ≠ OpenProject Administrator. |

See `00_Project_Vision.md` §10 and `01_Product_Requirements.md` §2.1.  
Implemented across **M14a** (registration), **M14** (connection/eligibility), **M15** (grants + isolation).

## 5.2 Application roles (current / transitional)

The application currently uses RBAC with example roles such as:

- Administrator (platform / workspace administration, including access grants)
- Portfolio Manager
- Project Manager
- Executive
- Viewer

**Product direction:** analytical UX is designed for a **single primary audience** (multi-project oversight), not job-title-specific product modes. Self-registered users receive platform role `VIEWER` until they become a **Workspace Admin** via eligible OpenProject connect (M14) or receive analytics grants (M15). Platform `ADMINISTRATOR` remains deploy/seed/ops only — never auto-assigned on first signup.

Future simplification of role catalogs must preserve: (1) platform admin, (2) workspace admin capabilities, (3) explicit analytics access, (4) backend enforcement.

---

# 6. JWT Security

JWT tokens contain only necessary information.

Recommended claims:

- User ID
- Username
- Roles
- Expiration

Tokens must:

- Expire automatically.
- Be signed securely.
- Be validated on every protected request.

---

# 7. API Security

Every protected endpoint shall:

- Require HTTPS.
- Validate JWT.
- Validate permissions.
- Validate request payloads.
- Return appropriate HTTP status codes.

Public endpoints should be explicitly documented.

---

# 8. Input Validation

All external input is considered untrusted.

Validation includes:

- Required fields
- Data type validation
- Range validation
- Length validation
- Enumeration validation

Business validation occurs after structural validation.

---

# 9. Output Encoding

User-supplied content rendered in the UI must be treated as untrusted.

The frontend should rely on Angular's built-in escaping and avoid bypassing security mechanisms unless absolutely necessary.

The backend should return structured data rather than preformatted HTML whenever possible.

---

# 10. SQL Injection Protection

SQL Injection is prevented through:

- JPA/Hibernate
- Parameterized queries
- Repository abstraction

Dynamic SQL should be minimized.

User input must never be concatenated directly into SQL statements.

---

# 11. Cross-Site Scripting (XSS)

The application protects against XSS through:

- Output encoding
- Angular template escaping
- Content Security Policy (CSP)

User-generated HTML should only be rendered when required and must be sanitized.

---

# 12. Cross-Site Request Forgery (CSRF)

If JWTs are used in the `Authorization` header rather than cookies, CSRF risk is reduced because browsers do not automatically attach authorization headers.

If cookie-based authentication is introduced in the future, appropriate CSRF protection should be enabled.

---

# 13. Cross-Origin Resource Sharing (CORS)

Allowed origins must be explicitly configured.

Production should never allow unrestricted origins.

Allowed methods include:

- GET
- POST
- PUT
- PATCH
- DELETE

---

# 14. Sensitive Data

Sensitive information includes:

- Passwords
- JWT secrets
- API keys
- Database credentials

Sensitive data shall:

- Never appear in logs.
- Never be exposed by APIs.
- Never be committed to source control.

---

# 15. Secrets Management

Secrets should be stored outside the application source code.

Examples:

- Environment Variables
- Docker Secrets
- Kubernetes Secrets
- Dedicated Secret Managers

Different environments must use different secrets.

---

# 15.1 OpenProject integration authentication

Project Analytics user authentication (JWT) is independent of OpenProject integration credentials.

**Implemented (M14 Phase 7)**

- OpenProject integration authentication is **OAuth 2.0 authorization code + PKCE** (preferred) with **API key** as the supported alternative.
- **One Workspace = one OpenProject instance.**
- Per-workspace credentials (API key or OAuth access/refresh tokens) are encrypted at rest (`CREDENTIALS_ENCRYPTION_KEY`).
- `CompositeOpenProjectCredentialResolver` prefers stored workspace credentials; env `OPENPROJECT_API_KEY` is local/dev fallback only (disabled in prod).
- After OAuth token exchange **or** API-key connect, the **same** OpenProject eligibility check runs (global `admin` or allow-listed role title). OAuth success alone is not enough.
- Eligible connector becomes PA Workspace Admin + analytics access. M15 grants are unchanged: additional PA users get analytics via Workspace Admin grants and do not OAuth separately.
- OAuth access tokens near expiry are refreshed using the stored refresh token when the OAuth client is configured.
- Synchronization engine, import pipeline, and analytics modules are not redesigned for OAuth.

**Dashboard boundary**

- Dashboard and analytics requests use Project Analytics JWT only.
- They must not require OpenProject credentials or live OpenProject API calls.
- OpenProject credentials are used only during synchronization (and connection setup).

---

# 16. HTTPS

All production traffic shall use HTTPS.

TLS certificates should be issued by a trusted certificate authority.

HTTP requests should be redirected to HTTPS.

---

# 17. Audit Logging

Security-relevant events should be logged.

Examples:

- Login
- Logout
- Failed authentication
- Permission denied
- Password change
- Administrative actions

Logs should include timestamps and user identifiers where appropriate.

---

# 18. Rate Limiting

Future support may include:

- Login attempt limits
- API request limits
- IP-based throttling

Repeated failed login attempts may trigger temporary lockouts or delays.

---

# 19. Session Management

JWT expiration should be configurable.

Expired tokens are rejected.

Logout should invalidate refresh mechanisms if refresh tokens are implemented in the future.

---

# 20. Error Handling

Security errors must never expose:

- Stack traces
- SQL statements
- Internal implementation details
- Server configuration

Users receive clear but non-sensitive error messages.

---

# 21. Dependency Security

Dependencies should:

- Be kept up to date.
- Be scanned regularly for known vulnerabilities.
- Come from trusted sources.

Outdated libraries should be upgraded as part of regular maintenance.

---

# 22. Backup and Recovery

Database backups should:

- Be performed regularly.
- Be encrypted where appropriate.
- Be tested periodically through restoration procedures.

Backup retention should follow organizational policies.

---

# 23. Monitoring

Security monitoring should include:

- Failed login attempts
- Unauthorized access
- Suspicious API usage
- Elevated error rates
- Synchronization failures

Alerts should be generated for critical security events.

---

# 24. Security Testing

Security testing should include:

- Authentication tests
- Authorization tests
- Input validation tests
- Dependency vulnerability scanning
- Penetration testing
- Regression testing for security fixes

Security tests should be integrated into the CI/CD pipeline where practical.

---

# 25. Security Principles

- Deny by default.
- Least privilege.
- Defense in depth.
- Secure by default.
- Validate all input.
- Protect sensitive data.
- Log important security events.
- Keep dependencies current.

---

# 26. AI Implementation Notes

When implementing security:

- Use Spring Security with JWT.
- Hash passwords using BCrypt.
- Validate all requests.
- Protect every sensitive endpoint.
- Never expose internal exceptions.
- Externalize secrets.
- Keep authentication and authorization responsibilities separate.
- Write automated security tests for authentication and authorization.

---

# End of Document