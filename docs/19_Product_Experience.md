# Product Experience Architecture (M11)

Version: 1.2  
Status: **Architecturally frozen**  
Last updated: 2026-07-31

---

# 1. Purpose

This document is the **frozen Product Experience architecture** for Project Analytics (M11).

It defines:

- Product identity and primary audience
- Analytics access model (intent)
- Product surfaces and the role of each
- Interaction flows for the primary persona
- Default landing behavior
- Navigation information architecture (IA)
- Experience principles P1–P6

**Product identity:** Project Analytics is a **management intelligence layer** on top of OpenProject. It serves people who **monitor and prioritize multiple projects**—not day-to-day task execution, and not a catalog of job titles.

**Implementation policy:** M11 is **architecture-only until M10 completes**. Do not reopen product/UX debates during M10. After M10, implement this document as written.

---

# 2. Frozen experience principles (P1–P6)

| ID | Principle | Product rule |
|----|-----------|--------------|
| **P1** | Score literacy | Every score answers: **how bad**, **why**, **what next** (progressive disclosure L0→L3). |
| **P2** | Scores vs counts | Scores **summarize complexity**. Raw counts remain **counts** and carry **direct actions** (open list, filter Explorer, open project). Never replace a count with only a score badge. |
| **P3** | Exceptions first | Home and triage surfaces lead with **exceptions** (projects that Need Attention, rising risk, critical recommendations). Summaries and “all green” content come after. |
| **P4** | Visual explainability | Factor contribution (bars / weighted drivers) is first-class on score surfaces. No opaque single number without a path to drivers. |
| **P5** | Terminology | UI label **Needs Attention** (API may retain `attention`). Consistent vocabulary across Home, Explorer, Detail, Reports, Recommendations. |
| **P6** | Explorer-first IA | **Project Explorer** is the primary **daily analytical workspace**. Dashboards (Home) are **triage entry points**, not the long-session analysis surface. |

### Progressive disclosure levels (reference)

| Level | Question answered | Typical UI |
|-------|-------------------|------------|
| **L0** | What needs me? | Exception list, status chips, Needs Attention queue |
| **L1** | How bad? | Health / Risk / Needs Attention scores + band |
| **L2** | Why? | Factor contribution bars, top drivers, linked counts |
| **L3** | What next / evidence? | Recommendations, drill to work-package counts, report export |

### Surface philosophy

| Surface role | Purpose |
|--------------|---------|
| **Home** | Fast **triage** — what needs attention now across many projects |
| **Explorer** | Daily **analytical work** — compare, filter, prioritize |
| **Scores** | **Synthesis** — how bad / how healthy (with explainability) |
| **Counts** | **Operational signals** — concrete things to inspect (actionable, not re-scored) |
| **Project Detail** | Deep **understanding** of one project in multi-project context |
| **Reports** | **Communication** artifacts (immutable snapshots) |
| **Recommendations** | **What next** — decision support for where to focus |

---

# 3. Product identity & audience (frozen)

## 3.1 Positioning

| System | Responsibility |
|--------|----------------|
| **OpenProject** | Project **execution** (work packages, boards, planning, day-to-day delivery) |
| **Project Analytics** | Project **intelligence** (multi-project monitoring, prioritization, explainable scores, recommendations, reports) |

## 3.2 Feature filter (frozen)

Every new feature must answer:

> **Does this help someone oversee and prioritize multiple projects?**

| Answer | Disposition |
|--------|-------------|
| **Yes** | Belongs in Project Analytics |
| **Primarily individual tasks or day-to-day project execution** | Belongs in OpenProject |

Examples:

- Cross-project Needs Attention queue → **in**
- Portfolio compare and risk trends → **in**
- “My assigned work packages” board → **OpenProject**
- Editing a task status → **OpenProject**

## 3.3 Primary persona (frozen)

**A user responsible for monitoring and prioritizing multiple projects or portfolios within a workspace.**

| Attribute | Definition |
|-----------|------------|
| **Job** | Oversee many projects; prioritize attention; understand risk/health trends; decide where to focus; communicate status |
| **Not defined by** | Job title (CEO, PMO, Engineering Director, Project Manager, etc.) |
| **Success** | Quickly see what needs attention; understand why; act on prioritization; produce a report when needed |
| **Primary surfaces** | Home (triage), Explorer (daily work), Project Detail, Recommendations, Reports |
| **Does not use this app for** | Personal task lists, single-WP execution, replacing OpenProject ops |

A person whose main concern is their **assigned work packages** continues working in **OpenProject**.

A person who oversees **many projects** and needs to prioritize, identify risks, understand trends, and decide where to focus uses **Project Analytics**.

## 3.4 Platform administrator (secondary, not the product audience)

| Attribute | Definition |
|-----------|------------|
| **Job** | Keep the analytics workspace usable: connection, sync, **who may access analytics** |
| **Surfaces** | Connections, access grants, Settings, optional Portfolios membership hygiene |
| **Note** | An OpenProject administrator is **not** automatically this person and is **not** automatically an analytics user |

Administration exists so the primary persona can work. It is not the center of product design for analytical features.

---

# 4. Analytics access (frozen intent)

Access is a **Project Analytics concept**. It is not inferred from OpenProject.

| Rule | Detail |
|------|--------|
| **No OP role inference** | Do not treat OpenProject roles/permissions as “is a manager” or “needs analytics.” |
| **Workspace-administered** | A **workspace administrator** decides which users can access the analytics workspace. |
| **Independent of OP hierarchy** | No import or interpretation of organizational hierarchy from OpenProject for access. |
| **Gate** | Without analytics access → no multi-project intelligence surfaces for that workspace. With access → primary persona experience. |
| **Admin capabilities** | Distinct from analytics consumption (connect, sync, grant access). A user may have one, both, or neither depending on grants. |

### Why this matters

- An OpenProject **administrator** may only run the tool, not make portfolio decisions.
- A **project lead** in OpenProject may or may not need cross-project analytics.
- Trying to guess “manager” from OP creates wrong access and couples product identity to another system’s RBAC.

**Implementation** of grant UI / membership storage may ship with M11 or a dedicated access milestone. The product rule is frozen now (`09_Security.md` §5.1).

### Transitional note on application roles

Current global roles (e.g. Administrator, Project Manager, Executive, Viewer) may still exist in code. **Product direction:** do not design divergent analytical products per job title. Prefer:

1. **Workspace analytics access** (yes/no, and later optional capability flags if needed)
2. **Workspace admin** capabilities (connection, sync, grants)

Title-specific dashboards as separate product modes are **out**.

---

# 5. Product surfaces

## 5.1 Home (triage)

**Business question:** *What needs attention across my multi-project scope right now?*

- Default scope: all projects in the **workspace** (optional portfolio filter).
- Layout order: **exceptions → key scores → counts with actions → recommendations strip**.
- Short session: scan, pick a project, or open Explorer pre-filtered.
- Not for long multi-filter analysis (that is Explorer).

## 5.2 Project Explorer

**Business question:** *How do I compare, filter, and prioritize projects in daily analytical work?*

- Primary **daily analytical workspace** for the primary persona (P6).
- Table/list-first: sortable, filterable, multi-criteria.
- Columns include scores **and** actionable counts.
- Row → Project Detail; portfolio membership is a **filter**, not a separate product.

## 5.3 Project Detail

**Business question:** *Why is this project in this state, and where should I focus next?*

- Full progressive disclosure L0–L3 for one project **in service of multi-project prioritization**.
- Scores with contribution factors; counts with direct meaning; recommendations.
- Never edits OpenProject data. Execution remains in OpenProject if the user acts on findings.

## 5.4 Reports

**Business question:** *How do I communicate a frozen multi-project (or project) status view?*

- Immutable reports (workspace / portfolio / project scope).
- Consumers of analytics DTOs only.
- Supporting surface—not the daily desk.

## 5.5 Recommendations

**Business question:** *Where should I focus next?*

- Rule-based, ranked, explainable.
- Home strip, Project Detail panel, optional scoped list.
- Action layer for prioritization—not a second dashboard product.

## 5.6 Supporting surfaces

| Surface | Purpose | Who |
|---------|---------|-----|
| **Connections** | Connect OpenProject, sync, credentials | Workspace admin |
| **Access / members** | Grant analytics access for the workspace | Workspace admin |
| **Portfolios** | Organizational project collections (M2M filter only) | Users with analytics access (membership edit per permission) |
| **Settings** | Preferences / platform settings as authorized | Admin / self |

There is **no** separate “Executive product” or “PM product.” One analytical experience; scope and filters vary.

---

# 6. Default landing (frozen intent)

After login, when the user has **analytics access** and the workspace has synced data:

| User | Default landing | Rationale |
|------|-----------------|-----------|
| **Primary persona** (analytics access) | **Project Explorer** | Daily multi-project analytical work is the center of gravity (P6). |
| **Workspace admin only** (no analytics focus / setup path) | **Connections** | Ops-first until analytics is ready; if admin also has analytics access, Explorer remains fine after first successful sync. |

### Landing fallbacks

| Condition | Landing |
|-----------|---------|
| Not authenticated | Login |
| Authenticated, **no analytics access** to any workspace | Clear “no access” / request access state (not empty Explorer) |
| Has access, **no workspace** / not connected | Connections (admin) or waiting state |
| Workspace exists, **never synced / empty** | Setup with **Synchronize** CTA (not empty analysis) |
| Sync stale / failed | Role/default landing **with** freshness/error banner (use last good data when present) |

### Optional preference (post-M11, not required to freeze)

Users with analytics access may prefer “Start on Home” (triage-first). Product default remains **Explorer**.

---

# 7. Interaction flows

Notation: `→` navigation; `↗` secondary branch.

---

## 7.1 Primary persona — multi-project oversight (main journey)

### Daily analytical loop

```
Login
  → Explorer (default)     [requires analytics access]
       → filter / sort (Needs Attention, risk, health, counts, portfolio, search)
       → open Project Detail
            → L0–L3: how bad → why (factors) → what next (recommendations)
            → operational counts with actions (remain counts)
       → return to Explorer (filters preserved)
```

### Morning / stand-up triage

```
Explorer or nav → Home
  → exceptions first (Needs Attention queue)
  → score synthesis for workspace / portfolio
  → top recommendations
  → drill to Project Detail or open Explorer pre-filtered
```

### Prioritization via recommendations

```
Home strip | Project Detail | Recommendations list
  → open recommendation
  → linked project + score/count context
  → (outside this app) execute in OpenProject if needed
```

### Communication

```
Home | Explorer | Project Detail
  → Reports → generate immutable artifact (workspace / portfolio / project)
  → download / share
```

**Mental model**

| Surface | Metaphor |
|---------|----------|
| **Home** | Inbox / stand-up board — what needs attention now |
| **Explorer** | Desk — compare and prioritize the set |
| **Project Detail** | Case file — understand one project deeply |
| **Recommendations** | Suggested focus — what next |
| **Reports** | Memo — communicate a frozen view |
| **OpenProject** | Floor — do the work |

---

## 7.2 Workspace administrator — readiness & access

```
Login
  → Connections
       → connect OpenProject (API key now / OAuth later)
       → synchronize
  → grant analytics access to users who oversee multiple projects
  → optional: Portfolios membership hygiene
  ↗ Explorer / Home to verify intelligence looks correct (if admin also has analytics access)
```

**Mental model:** Connections and access grants are the control room. Analytical surfaces are verification or dual-hat use—not a second primary product audience.

---

# 8. Cross-surface navigation map

```
         ┌──────────────────────────────────────┐
         │  Analytics access granted?           │
         └──────────────────────────────────────┘
                    │ yes                │ admin setup
                    ▼                    ▼
             ┌─────────────┐      ┌─────────────┐
             │  Explorer   │◄────►│ Connections │
             │ daily work  │      │ + access    │
             └──────┬──────┘      └─────────────┘
                    │
             ┌──────┴──────┐
             ▼             ▼
        ┌─────────┐  ┌──────────┐
        │  Home   │  │  Detail  │
        │ triage  │  │  L0–L3   │
        └────┬────┘  └────┬─────┘
             │            │
             └─────┬──────┘
                   ▼
        Recommendations · Reports · Counts→evidence
```

### Shared rules

1. **One analytics engine** — all surfaces consume the same Health / Risk / Needs Attention DTOs.
2. **Scope travels** — workspace and optional portfolio filter persist Home ↔ Explorer ↔ Reports.
3. **Counts are actions** — clicking a count lists or filters underlying evidence; never only a score badge.
4. **OpenProject stays outside the analysis loop** — sync source only; execution of work remains in OpenProject.
5. **Recommendations never invent scores** — they reference existing analytics outcomes.
6. **No job-title product modes** — one experience for multi-project oversight.

---

# 9. Surface usage matrix (simplified)

| Surface | Primary persona (analytics access) | Workspace admin |
|---------|------------------------------------|-----------------|
| **Home** | Triage (frequent) | Verify / freshness |
| **Explorer** | **Primary daily** | Verify after sync |
| **Project Detail** | Heavy (prioritization drill) | Rare |
| **Reports** | Periodic communication | Rare |
| **Recommendations** | Daily decision support | — |
| **Connections / access** | — | **Primary** |
| **Portfolios** | Filter + light membership | Setup |

---

# 10. Relationship to other freezes

Unchanged:

- Workspace owns projects; portfolios M2M organizational only
- Single analytics scoring engine; dashboards/reports/recommendations are consumers
- OpenProject sync-only; env API key temporary; OAuth later
- No product feature implementation during M10

Refined by this document / Vision:

| Topic | Freeze |
|-------|--------|
| Audience | Multi-project oversight persona; **not** job-title matrix |
| Access | App-owned workspace analytics grants; **not** OP role inference |
| Primary UX | Home = triage; Explorer = daily analytical work |
| Attention | UI: **Needs Attention**; API may keep `attention` |
| Feature filter | Oversee/prioritize many projects → in; task execution → OpenProject |

---

# 11. Freeze status — M11 architecturally frozen

| Topic | Status |
|-------|--------|
| Product identity & feature filter | **Frozen** |
| Primary persona | **Frozen** |
| Analytics access model (intent) | **Frozen** |
| Principles P1–P6 | **Frozen** |
| Surfaces, landings, flows, navigation | **Frozen** |
| Implementation | **After M10 only** — execute this doc; do not re-litigate product |

### Frozen for architecture; deferred as implementation detail (not product re-openers)

These may be decided during M11 **implementation** without changing product identity:

- Exact widget layouts and Explorer column catalog
- Visual design tokens / spacing
- Preference “remember last page” / “start on Home”
- Access-grant API/UI shape (rule already frozen: workspace admin grants analytics access)
- Recommendation presentation density (rules remain consumers of analytics)

### Explicitly out of M11 architecture freeze

- OAuth for OpenProject (credential milestone; resolver seam already frozen)
- New domain scoring algorithms
- OpenProject task-execution features

---

# 12. Implementation order

1. **M10 — Production Hardening** (current) — security, retention, performance, tests, deploy path. **No product UX redesign.**
2. **M11 — Product Experience implementation** — build Home/Explorer/Detail/Reports/Recommendations experience and access grants **per this frozen document**.

---

# 13. Design guidance (for M11 implementers)

When implementing or proposing a capability:

1. Does it help **oversee and prioritize multiple projects**?
2. Would a person who only manages **their own work packages** need it? If that is the main user, it belongs in OpenProject.
3. Does access require guessing an OpenProject role? If yes, use **workspace analytics grants**.
4. Does it invent a second experience for a job title? If yes, collapse into the single primary persona + filters/scope.

Do **not** reopen product philosophy discussions during M10 or mid-M11 unless a hard technical contradiction appears; escalate with a written delta against this document.

---

End of Document
