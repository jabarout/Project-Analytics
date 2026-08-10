# Project Analytics

# Product Requirements Specification

Version: 1.0
Status: Draft

---

# 1. Purpose

This document defines all functional and non-functional requirements of Project Analytics.

It serves as the reference for software development, testing, validation, and future maintenance.

Every implemented feature must be traceable to a requirement defined in this document.

No feature shall be implemented unless it is described here or explicitly approved during future revisions.

---

# 2. Product Overview

Project Analytics is an enterprise analytics platform built on top of OpenProject.

Its purpose is to transform operational project data into business intelligence through dashboards, analytics, reports, recommendations and explainable metrics.

The platform is intended to complement OpenProject rather than replace it.

---

# 2.1 Product Architecture Decisions (Frozen)

The following decisions are the official product architecture source of truth and supersede informal assumptions.

## OpenProject integration

- Long-term authentication for OpenProject integrations is **OAuth 2.0**.
- **One Workspace = one OpenProject instance.**
- The environment API key (`OPENPROJECT_API_KEY`) is a **temporary** implementation until the dedicated OAuth milestone.
- `OpenProjectCredentialResolver` is the migration seam: OAuth replaces only the credential provider and HTTP auth application—not the synchronization engine, import pipeline, or analytics modules.

## Product identity and primary audience

- Project Analytics is a **management intelligence layer** on top of OpenProject—not a replacement and not a task-execution tool.
- **Primary persona:** a user responsible for **monitoring and prioritizing multiple projects or portfolios** within a workspace.
- The product does **not** target specific job titles (CEO, PMO, Engineering Director, Project Manager, etc.). Responsibility for multi-project oversight defines the audience, not title.
- **Feature filter:** every feature must help someone **oversee and prioritize multiple projects**. Individual task / day-to-day execution belongs in OpenProject.
- Full statement: `00_Project_Vision.md` §9–§10 and `19_Product_Experience.md`.

## Analytics access (application-owned)

- Analytics access is a **Project Analytics concept**, not derived from OpenProject roles or permissions.
- Do **not** infer “manager” (or any hierarchy) from OpenProject.
- A **workspace administrator** decides which users can access the analytics workspace.
- OpenProject remains sync source only; its RBAC does not define analytics eligibility.

## Primary user journey

1. **Connect Workspace** (API key now / OAuth later).
2. **Synchronize** operational data into local PostgreSQL.
3. **Immediate Dashboard & Analytics** on local data for users with analytics access.

Users must not interact with OpenProject while using dashboards. OpenProject is the **synchronization source only**.

## Workspace vs portfolio

- The **workspace owns** all synchronized projects and is the **primary analytics scope** (“All Projects” for that OpenProject instance).
- **Portfolios** are **optional analytical collections** / views (e.g. Finance, Employee Projects) — not exclusive owners.
- Users must **not** be required to create a portfolio before analytics are useful.
- Primary analytical UX: **Home** (triage) + **Explorer** (daily multi-project work); see Product Experience (M11).

## Portfolio membership

- **Model:** many-to-many — a project may belong to zero or more portfolios.
- **Nature:** membership is **purely organizational**. It has no effect on project ownership, synchronization, or analytics **calculation**.
- **Effect:** membership only selects which projects are **included** in portfolio-scoped dashboards, recommendations, and reports.
- **Sync:** projects attach to the workspace only; portfolio membership is not required at import.
- **Manual membership:** add/remove projects on a portfolio without changing workspace ownership.
- **Out of scope for now:** dynamic rule/filter-based portfolio membership (future enhancement only).

---

# 3. Functional Requirements

## FR-001 Authentication

The system shall authenticate users securely.

Capabilities include:

- User login
- User logout
- Password management
- Session management
- Role-based authorization

---

## FR-002 User Management

The system shall manage user information and permissions.

Capabilities include:

- User profile
- Roles
- Permissions
- Preferences

---

## FR-003 OpenProject Synchronization

The system shall synchronize project data from OpenProject into local PostgreSQL.

Capabilities include:

- Workspace connection to one OpenProject instance
- Manual synchronization
- Scheduled synchronization
- Incremental synchronization
- Synchronization status
- Synchronization history
- Error handling

After a successful synchronization, analytics and dashboards shall be available from local data without further OpenProject interaction.

---

## FR-003a Workspace Dashboard (Primary)

The system shall provide a **workspace-level dashboard** as the primary analytics surface for a connected OpenProject instance.

The workspace dashboard shall present analytics for **all synchronized projects** in that workspace (“All Projects”).

Users shall not be required to create a portfolio before this dashboard is useful.

Capabilities include (as analytics become available):

- Workspace KPIs
- Overall Health / Attention aggregates
- Active and critical projects
- Budget and schedule overview
- Executive summary
- Recommendations and alerts (when those modules exist)

---

## FR-004 Portfolio Dashboard (Optional Scope)

Portfolios are **optional** user-defined subsets of projects within a workspace.

When a portfolio exists, the system shall provide a portfolio dashboard displaying analytics for **member projects only**:

- Portfolio KPIs
- Overall Health
- Attention Score
- Active Projects
- Critical Projects
- Budget Overview
- Schedule Overview
- Executive Summary
- Recommendations
- Alerts

Portfolio membership:

- Projects are assigned automatically to a technical Default Portfolio during synchronization (implementation detail).
- Users may manually assign projects to custom portfolios.
- Near-term UX: searchable multi-select for assignment.
- Dynamic rule-based membership is not required in the current roadmap phase.

---

## FR-005 Project Dashboard

Each project shall have its own dashboard containing:

- Overview
- Health
- Risks
- Budget
- Timeline
- Milestones
- KPIs
- Activity
- Team
- Recommendations

---

## FR-006 Analytics

The system shall compute business analytics including:

- Health Score
- Risk Score
- Attention Score
- Progress
- Budget Performance
- Schedule Performance
- Resource Indicators
- Portfolio Statistics

---

## FR-007 Reporting

The platform shall generate reports including:

- Portfolio Reports
- Project Reports
- Executive Reports
- Risk Reports
- KPI Reports

Reports shall support export in PDF and Excel formats.

---

## FR-008 Recommendation Engine

The system shall generate recommendations based on business rules.

Recommendations must explain:

- Why they were generated
- Which metrics influenced them
- Suggested actions

---

## FR-009 Alerts

The system shall notify users when important conditions occur.

Examples include:

- Critical risks
- Budget overruns
- Delayed milestones
- Failed synchronization
- Health deterioration

---

## FR-010 Search

Users shall be able to search:

- Projects
- Reports
- Users
- Dashboards

---

## FR-011 Filtering

Users shall filter information using:

- Portfolio
- Project
- Date
- Status
- Risk
- Health
- Tags

---

## FR-012 User Preferences

Users shall configure:

- Theme
- Language
- Dashboard Layout
- Notification Preferences
- Time Zone

---

## FR-013 AI Insights

The system shall generate AI-assisted summaries based exclusively on synchronized project data.

AI-generated content must remain explainable and traceable.

---

# 4. Non-Functional Requirements

The platform shall satisfy the following quality requirements.

## Performance

- Dashboard loading under 2 seconds
- API response under 500 ms for cached requests
- Efficient handling of large portfolios

---

## Scalability

The architecture shall support:

- Thousands of projects
- Thousands of users
- Horizontal scaling
- Future distributed deployment

---

## Availability

The platform shall remain available during synchronization whenever possible.

---

## Reliability

Synchronization failures shall never corrupt analytics.

---

## Security

The platform shall implement:

- Authentication
- Authorization
- HTTPS
- Secure password storage
- Audit logging

---

## Maintainability

The system shall:

- Follow Clean Architecture
- Follow SOLID principles
- Be modular
- Be testable
- Be documented

---

## Accessibility

The user interface shall comply with WCAG AA guidelines whenever practical.

---

# 5. Business Constraints

Project Analytics shall never replace OpenProject.

Business calculations belong exclusively to Project Analytics.

Operational project management remains the responsibility of OpenProject.

---

# 6. Assumptions

The platform assumes:

- OpenProject API availability
- Reliable network communication
- PostgreSQL database availability
- Authenticated users

---

# 7. Acceptance Criteria

The application shall be considered complete when:

- Every functional requirement has been implemented.
- Every requirement has corresponding tests.
- Dashboards display synchronized analytics.
- Reports are generated correctly.
- AI summaries are explainable.
- Performance objectives are achieved.
- Security requirements are satisfied.

---

# 8. Requirement Traceability

Every implementation task shall reference one or more Functional Requirements (FR).

Every API endpoint, database table, frontend component, backend service and automated test must be traceable to the corresponding requirement.

No implementation shall exist without a documented business purpose.

---

End of Document