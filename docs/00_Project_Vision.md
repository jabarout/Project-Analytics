# Project Analytics

# Project Vision

Version: 1.1  
Status: Active (product identity frozen)

---

# 1. Introduction

Project Analytics is an enterprise web application that provides a **management intelligence layer** on top of OpenProject.

OpenProject remains the operational project management system and the single source of truth for project execution.

Project Analytics does **not** replace OpenProject. It does not manage individual work packages, boards, or day-to-day task execution.

Instead, it transforms synchronized operational data into analytics, explainable scores, cross-project insights, recommendations, and reports that support **monitoring and prioritization across multiple projects**.

---

# 2. Vision

Our vision is simple:

> Transform project data into multi-project decisions.

Rather than forcing people who oversee many projects to interpret hundreds of work packages, deadlines, schedules, and reports, Project Analytics presents what matters most across the portfolio of projects, explains why it matters, and recommends where to focus next.

Every page of the application must answer one important business question for someone responsible for **oversight and prioritization**, not task execution.

---

# 3. Problem Statement

Traditional project management systems excel at planning and execution but provide limited decision support for **cross-project** monitoring.

People who oversee many projects often struggle to answer:

- Which projects require immediate attention?
- Why is a project unhealthy?
- Which risks are increasing?
- What is causing schedule pressure across the set?
- Where should limited attention and resources go next?
- What trends are emerging across projects or portfolios?

These answers usually require manually reviewing multiple dashboards, reports, and spreadsheets—or living inside operational tools that optimize for single-project execution.

Project Analytics eliminates that gap by centralizing multi-project analytics and presenting explainable business intelligence.

---

# 4. Mission

The mission of Project Analytics is to provide a reliable **decision intelligence** platform built on top of OpenProject.

The platform collects project information, analyzes it using business rules, computes meaningful indicators, and presents the results through surfaces that help users **oversee, prioritize, and decide** across many projects—while operational project management remains in OpenProject.

---

# 5. Objectives

The primary objectives of the platform are:

- Synchronize project data from OpenProject.
- Compute project-level and multi-project (workspace / portfolio) analytics.
- Detect risks and anomalies across projects.
- Measure project health with explainable scores.
- Prioritize projects that need attention.
- Generate explainable recommendations for where to focus.
- Provide triage and analytical surfaces for multi-project oversight.
- Produce professional reports for communication.
- Maintain a scalable and maintainable architecture.
- Control analytics access as an application concern (not inferred from OpenProject).

---

# 6. Product Positioning

| System | Responsibility |
|--------|----------------|
| **OpenProject** | Project **execution** — work packages, planning, boards, day-to-day delivery |
| **Project Analytics** | Project **intelligence** — multi-project monitoring, prioritization, explainable scores, recommendations, reports |

OpenProject manages projects.

Project Analytics explains and prioritizes projects **in aggregate**.

The relationship is complementary, not competitive.

### Feature filter (frozen)

Every new feature must answer:

> **Does this help someone oversee and prioritize multiple projects?**

| Answer | Disposition |
|--------|-------------|
| **Yes** | Belongs in Project Analytics |
| **Primarily about individual tasks or day-to-day project execution** | Belongs in OpenProject (out of scope here) |

---

# 7. Scope

Project Analytics includes:

- Multi-project and portfolio analytics views
- Project-level analytical detail (for prioritization context)
- KPI monitoring and risk / health / needs-attention assessment
- Cross-project comparison and triage
- Explainable recommendations
- Reporting for stakeholder communication
- Synchronization with OpenProject
- Application authentication and **workspace analytics access** control
- Historical analytics
- Operational observability of the platform

---

# 8. Out of Scope

Version 1.0 intentionally excludes:

- Project planning
- Task / work package management
- Editing work packages
- Gantt chart management
- Resource scheduling
- Agile board management
- Personal “my assigned work” execution workflows
- Inferring organizational hierarchy or “manager” status from OpenProject roles
- Native mobile applications
- Custom machine learning models
- Plugin marketplace
- Replacement of OpenProject functionality

These capabilities remain the responsibility of OpenProject (or are explicitly deferred).

---

# 9. Primary audience (frozen)

## 9.1 Primary persona

**A user responsible for monitoring and prioritizing multiple projects or portfolios within a workspace.**

The application provides decision support through analytics, explainable scores, recommendations, and cross-project insights.

Operational project management remains in OpenProject.

### What this is not

The product does **not** target a specific job title (CEO, PMO lead, Engineering Director, Project Manager, etc.).

Job titles vary by organization. What matters is **responsibility**:

| Uses Project Analytics | Continues in OpenProject |
|------------------------|---------------------------|
| Oversees **many** projects | Focuses on **assigned work packages** and delivery execution |
| Needs to **prioritize**, identify risks, understand trends, decide where to focus | Needs boards, tasks, time logging, day-to-day project ops |
| Wants multi-project **intelligence** | Wants single-project **execution** tools |

Examples for clarity only (not product “roles”): a developer concerned with their own tickets stays in OpenProject; a person who oversees dozens of projects uses Project Analytics—regardless of title.

## 9.2 Platform administration (secondary)

Separately, **workspace administrators** configure the analytics workspace: OpenProject connection, synchronization, and **who may access analytics** in this application.

Administration is a platform concern, not the primary product audience for analytical features.

---

# 10. Access model (frozen intent)

Analytics access is a **concept of Project Analytics**, not a projection of OpenProject permissions.

| Rule | Detail |
|------|--------|
| **No OP role inference** | Do not decide who is a “manager” from OpenProject roles or permissions. An OpenProject administrator is not necessarily a business decision-maker; a project lead may or may not need cross-project analytics. |
| **App-owned access** | A **workspace administrator** decides which users can access the analytics workspace. |
| **Independent of OP hierarchy** | Organizational hierarchy is not imported or interpreted from OpenProject for access control. |
| **Focused product** | Access answers “may this person use multi-project intelligence here?”—not “what is their job title?” |

Implementation of grant UI and membership model may land with M11 (or a dedicated access milestone); the **product rule** is frozen now.

---

# 11. Success Criteria

The platform is considered successful when users who oversee multiple projects can:

- Identify projects that need attention within seconds.
- Understand project health without manual multi-tool analysis.
- Detect risks before they become critical.
- Compare and prioritize across a workspace or portfolio.
- Generate reports for communication quickly.
- Trust every metric because it is explainable.
- Scale the platform without architectural redesign.
- Receive access based on explicit analytics grants—not OpenProject role guessing.

---

# 12. Guiding Principles

Project Analytics follows these principles:

- OpenProject remains the single source of truth for operational data.
- Management intelligence belongs to Project Analytics.
- The product serves **multi-project oversight and prioritization**, not task execution.
- Every metric must be explainable.
- Every surface answers one business question for the primary persona.
- **Feature filter:** multi-project oversee/prioritize → in; individual task execution → OpenProject.
- Analytics access is granted in this application; it is not inferred from OpenProject.
- Business logic belongs in the backend.
- The frontend is responsible for presentation and user interaction.
- Architecture must prioritize maintainability, scalability, and clarity.
- The platform should be understandable by both software engineers and AI development tools.

---

# 13. Long-Term Vision

Project Analytics is designed as a foundation for future enterprise capabilities, including predictive analytics, advanced forecasting, AI-powered decision support, cross-portfolio optimization, and enterprise reporting—always as a **management intelligence layer**, not an OpenProject replacement.

The architecture must remain modular, extensible, and independent from the implementation details of OpenProject.

---

End of Document