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

## 5.1 Product access model (frozen)

Analytics access is owned by **Project Analytics**, not inferred from OpenProject.

| Rule | Detail |
|------|--------|
| **No OpenProject role inference** | Do not map OpenProject roles/permissions to “manager,” “executive,” or analytics eligibility. An OpenProject administrator is not necessarily a multi-project decision-maker. |
| **Workspace analytics access** | A **workspace administrator** grants (or revokes) which application users may access analytics for that workspace. |
| **Independent hierarchy** | Organizational hierarchy is not imported from OpenProject for authorization. |
| **Product focus** | Access answers whether the user may use the **management intelligence** layer for multi-project oversight—not their job title. |

See `00_Project_Vision.md` §10 and `01_Product_Requirements.md` §2.1.

Implementation of workspace membership / grant UI may follow with M11 or a dedicated access milestone; the rule above is binding for design.

## 5.2 Application roles (current / transitional)

The application currently uses RBAC with example roles such as:

- Administrator (platform / workspace administration, including access grants)
- Portfolio Manager
- Project Manager
- Executive
- Viewer

**Product direction:** analytical UX is designed for a **single primary audience** (multi-project oversight), not job-title-specific product modes. Fine-grained title-based roles should not drive divergent analytical experiences. Workspace **analytics access** (granted/denied) is the primary gate; administration capabilities remain distinct for workspace admins.

Future simplification of role catalogs must preserve: (1) workspace admin capabilities, (2) explicit analytics access, (3) backend enforcement.

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

**Official long-term target**

- OpenProject integration authentication is **OAuth 2.0**.
- **One Workspace = one OpenProject instance.**
- OAuth is introduced in a dedicated milestone.
- `OpenProjectCredentialResolver` remains the migration seam: OAuth replaces only the credential provider and the OpenProject HTTP client’s Authorization header construction.
- Synchronization engine, import pipeline, and analytics modules must not be redesigned for OAuth.

**Current (temporary)**

- OpenProject is accessed with a platform API key from environment configuration (`OPENPROJECT_API_KEY`).
- Credentials are resolved via `OpenProjectCredentialResolver` and applied only in the OpenProject HTTP client.
- The API key path remains valid until the OAuth milestone ships.

**Out of scope until the OAuth milestone**

- OAuth endpoints
- Token storage
- OAuth-related database schema

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