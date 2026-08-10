# M11A — Product Experience Specification

Version: 1.1  
Status: **Frozen** (approved) — implementation = **M11B**  
Last updated: 2026-07-31

---

# 0. Purpose and constraints

## 0.1 Purpose

M11A defines the **final UX presentation and exploration specification** for Project Analytics before implementation (M11B).

It refines **how** existing analytics, scores, counts, and recommendations are shown and navigated. It does **not** redesign product identity or business logic.

**Approval:** M11A is **frozen** with the refinements in §0.5. M11B implements this document without reopening UX philosophy.

## 0.2 Frozen (do not modify or reopen)

| Area | Source of truth |
|------|-----------------|
| M10 Production Hardening | Implemented / approved |
| Analytics engine & scoring | `08_Analytics_Engine.md`, analytics module |
| Health / Risk / Attention calculations | Unchanged formulas |
| Recommendations engine | Unchanged rules & consumers |
| Product vision & primary persona | `00_Project_Vision.md` |
| Overall architecture | Workspace owns projects; portfolios M2M organizational; OP sync-only |
| PE principles P1–P6 | `19_Product_Experience.md` |

## 0.3 Feature filter (still binding)

Every element on screen must help someone **oversee and prioritize multiple projects**. If it primarily serves single-task execution, it belongs in OpenProject.

## 0.4 Metric composition rule (presentation only)

M11A KPIs and charts **compose existing local fields and analytics DTOs** (project progress, end dates, health/risk/attention scores, work-package counts already used by scoring inputs, recommendation lists).  

They must **not**:

- Introduce new scoring algorithms  
- Re-score on the client  
- Call OpenProject at read time  
- Change recommendation rule logic  

Where a management metric is not yet exposed as a dedicated DTO field, M11B may add **read-only aggregation endpoints or DTO fields** that average/count existing stored values—without changing how scores are calculated.

## 0.5 Freeze refinements (approved)

| # | Refinement | Spec location |
|---|------------|---------------|
| R1 | Replace **Average Needs Attention** (score) with a **count and/or percentage** of projects needing attention | §3.2, §4.1, §8 |
| R2 | Explorer supports **sorting** and **grouping** | §4.3, §5.3–§5.5 |
| R3 | **Upcoming deadlines** window is defined and **configurable** (default 14 days) | §4.1, §5.1, §8.1 |
| R4 | Portfolio pages include a **Portfolio Health Summary** section | §4.2 |
| R5 | **Saved Views** supersede saved filters (filters + sort + group + columns) | §5.6 |
| R6 | **Connections** = OpenProject connection & sync only; workspace management only when multiple connections exist | §2.1, §4.7 |

---

# 1. Critical review of the current experience

## 1.1 What works

| Strength | Evidence |
|----------|----------|
| Local-first analytics on Home / Executive / Portfolio | No live OP calls in dashboards |
| Score triad present (Health, Risk, Attention) | KPI rows + project detail explanations |
| Exceptions partially present | Critical / high attention / overdue project KPIs; attention tables |
| Recommendations surface | Workspace, portfolio, project, executive |
| Portfolio membership UX | Searchable multi-select exists |

## 1.2 Gaps (decision value)

| Gap | Impact |
|-----|--------|
| **No Project Explorer** as primary analytical workspace | P6 unfulfilled; managers cannot filter/compare at scale |
| **KPI cards are not actionable** | Click does nothing; counts are not drill targets (violates P2) |
| **KPI set is incomplete for management** | Missing avg progress, delayed projects, overdue WPs, upcoming deadlines, reco summary as first-class cards |
| **“Attention” UI label** | Still uses “Attention” / “High attention” instead of **Needs Attention** (P5) |
| **Home leads with dense KPI grid + one bar chart** | Exceptions not first enough; summary text and score bar add little vs. exception queue |
| **“Scope score snapshot” bar chart** | Redundant with three average KPIs; low decision value |
| **Portfolio page mixes management overview with membership admin** | Cognitive overload; hard to scan as a management board |
| **No progressive disclosure of score factors on detail** | Explanations are text-only; no contribution bars (P4) |
| **No contextual “View All”** | Cannot go Critical → Explorer pre-filtered to critical in this scope |
| **Executive route as parallel product** | Conflicts with single primary persona; better as multi-workspace mode of Home or secondary nav |
| **Recalculate / Export as primary chrome** | Ops actions compete with triage; should be secondary |
| **No saved filters, no rich filter model** | Daily analytical work is underserved |
| **Journey is flat** | Login → dashboard dump; weak guided path Home → Explorer → Detail |

## 1.3 Design thesis for M11A

> **Home and Portfolio Overview answer “where do we hurt?”**  
> **Explorer answers “show me exactly those projects and let me compare.”**  
> **Project Detail answers “why this one, and what next?”**  
> Every number either **synthesizes** (score) or **opens a filtered set** (count).

---

# 2. Information architecture

## 2.1 Primary navigation (analytics users)

| Order | Nav item | Route (conceptual) | Role |
|-------|----------|--------------------|------|
| 1 | **Home** | `/` | Workspace (or multi-workspace) **triage** |
| 2 | **Explorer** | `/explorer` | Primary **analytical workspace** |
| 3 | **Portfolios** | `/portfolios` | List + **Portfolio overview** (management) |
| 4 | **Reports** | `/reports` | Immutable communication artifacts |
| 5 | **Recommendations** | `/recommendations` | Full ranked list (optional; also embedded) |
| 6 | **Connections** | `/workspaces` | OpenProject **connection & synchronization** (see §4.7) |
| 7 | **Settings** | `/settings` | Preferences / access admin as authorized |

### Navigation rules

1. **Explorer** is always one click away from Home and Portfolio Overview.  
2. Active scope (workspace, optional portfolio) is visible in a **global scope bar**.  
3. **Executive** as a separate top-level product is **removed from primary nav**. Multi-workspace roll-up becomes:
   - Home with **Workspace = All** (if multi-workspace access), or  
   - Home section “Workspaces at a glance” when more than one workspace exists.  
4. Project Detail is **not** a primary nav item; reached by drill-down only.  
5. Sync freshness always visible in the shell (banner if stale/failed).  
6. **Connections** focuses on connect + sync; multi-workspace management UI only when multiple connections exist (R6).

## 2.2 Scope model

```
Scope = {
  workspaceId: UUID | "ALL",   // ALL only if user has multi-workspace access
  portfolioId: UUID | null,    // null = all projects in workspace
  explorerFilters: FilterState // see §5
}
```

- Portfolios remain **filters/collections**, not owners.  
- Changing portfolio on Home updates KPIs to that membership set.  
- Scope travels: Home → Explorer → Reports generation defaults.

## 2.3 Surface map

```
Login
  └─► Home (triage) ──────────────────────────────┐
        │                                          │
        ├─► Explorer (pre-filtered) ◄──────────────┤  all "View All" / chart clicks
        │        │                                 │
        │        └─► Project Detail (L0–L3)        │
        │                 │                        │
        │                 ├─► Recommendations      │
        │                 └─► Report (project)     │
        │                                          │
        ├─► Portfolio Overview (same pattern) ─────┘
        ├─► Recommendations (scoped list)
        └─► Reports (scoped generate)
```

---

# 3. Shared design system (presentation)

## 3.1 Terminology (P5)

| Avoid (UI) | Use (UI) | API (unchanged) |
|------------|----------|-----------------|
| Attention score | **Needs Attention** score | `attentionScore` |
| High attention | **Needs Attention** (count / band) | existing thresholds |
| Critical | **Critical** (health band; keep) | existing critical definition |
| Overdue projects | **Delayed projects** (project end date / schedule) *or* keep “Overdue projects” with glossary | existing overdue project KPI |
| Overdue work packages | **Overdue work packages** | count from local WP data |

**Delayed projects** (management language): projects past planned end date or flagged overdue by existing KPI semantics—**same underlying data**, clearer manager label. UI label: **Delayed projects**. Tooltip may show “Past planned end / schedule overdue.”

## 3.2 KPI card contract

Every KPI card **must** include:

| Element | Rule |
|---------|------|
| **Label** | Management language |
| **Value** | Number or “—” if no data |
| **Semantics** | Score vs count (visual distinction) |
| **Severity** | Neutral / watch / critical band coloring (not color-only: include text/icon) |
| **Hint** | Short definition or threshold (e.g. “Health &lt; 40”) |
| **Action** | Primary: **View all** / open Explorer with filters; secondary: expand definition |
| **Context** | Action preserves current workspace/portfolio scope |

Cards that cannot define a meaningful drill action **do not ship**.

### Score cards vs count cards

| Type | Examples | Click behavior |
|------|----------|----------------|
| **Synthesis (score)** | Avg health, avg risk, avg progress | Open Explorer sorted by that metric, or open “score distribution” panel + Explorer |
| **Exception count / rate** | Critical, Delayed, **Projects needing attention (count + %)**, Overdue WPs, Upcoming deadlines | **View all** → Explorer (or focused list) with matching filter |
| **Volume** | Total projects | Explorer unfiltered (scope only) |
| **Recommendation** | Open reco count / critical reco count | Recommendations list or Explorer with `hasRecommendation=true` |

**R1:** Do **not** show “Average Needs Attention” as a synthesis score KPI. Needs Attention is communicated as **how many / what share** of projects need attention (count and percentage), not as a mean score. Per-project Needs Attention **score** remains on Explorer columns and Project Detail.

## 3.3 Progressive disclosure (P1, P4)

| Level | Where |
|-------|--------|
| L0 | Exception strips, count KPIs, Needs Attention queue |
| L1 | Score KPIs and bands |
| L2 | Factor contribution bars on Project Detail; optional expand on Home for top project |
| L3 | Recommendations + count evidence lists |

## 3.4 Empty / loading / stale states

| State | UX |
|-------|-----|
| No workspace | CTA → Connections |
| Never synced | CTA → Synchronize (not empty charts) |
| Sync stale | Banner + continue with last data |
| Zero exceptions | Positive state: “No critical or delayed projects in scope” + still show synthesis KPIs |
| No portfolio members | Empty portfolio overview + link to membership (admin section) |

---

# 4. Page specifications

## 4.1 Home — Workspace triage

**Business question:** *What needs my attention across this workspace right now?*

**Default scope:** Selected workspace, portfolio = All projects.

### Layout (top → bottom)

```
┌─────────────────────────────────────────────────────────────────┐
│ Scope bar: Workspace [select] · Portfolio [All | …] · Freshness │
├─────────────────────────────────────────────────────────────────┤
│ A. EXCEPTIONS FIRST (L0)                                        │
│    [Needs Attention projects] [Critical] [Delayed] [Overdue WPs]│
│    each card → View all → Explorer                              │
├─────────────────────────────────────────────────────────────────┤
│ B. MANAGEMENT SYNTHESIS (L1)                                    │
│    [Avg progress] [Avg health] [Avg risk]                       │
│    [Total projects] [Upcoming deadlines] [Reco summary]         │
│    (Needs Attention appears only as count/% in tier A — R1)     │
├─────────────────────────────────────────────────────────────────┤
│ C. EXCEPTION QUEUE (table, max 5–10)                            │
│    Needs Attention / Critical / Delayed projects                │
│    columns: Name, Health, Risk, Needs Att., Progress, Delayed?  │
│    row → Project Detail · footer: View all in Explorer          │
├─────────────────────────────────────────────────────────────────┤
│ D. CHARTS (interactive, limited)                                │
│    1) Health distribution (histogram / stacked bands)           │
│    2) Progress overview (avg or banded)                         │
│    3) Optional: delayed vs on-track counts (donut/bar)          │
├─────────────────────────────────────────────────────────────────┤
│ E. RECOMMENDATIONS STRIP (top N)                                │
│    + View all recommendations                                   │
├─────────────────────────────────────────────────────────────────┤
│ F. SECONDARY ACTIONS (collapsed/menu)                           │
│    Export · Recalculate · Generate report                       │
└─────────────────────────────────────────────────────────────────┘
```

### Home KPI catalog (required)

| KPI | Type | Definition (presentation) | Drill-down filter |
|-----|------|---------------------------|-------------------|
| **Projects needing attention** | **Count + %** | Count of projects in Needs Attention band; **percentage** = count / total projects in scope × 100. Display e.g. `7 (23%)`. **Not** an average attention score (R1). | `needsAttention=true` |
| **Critical projects** | Count | Existing critical (e.g. health &lt; 40) | `critical=true` |
| **Delayed projects** | Count | Existing overdue-project KPI | `delayed=true` |
| **Overdue work packages** | Count | Sum/count of overdue WPs in scope (local data) | `hasOverdueWp=true` (Explorer may expand to project list with overdue WP count &gt; 0) |
| **Upcoming deadlines** | Count | Projects (or primary deadlines) with end/due within the **upcoming window** (§8.1); default **14 days**, configurable | `upcomingDeadlineDays={window}` |
| **Average progress** | Score-like | Mean of project `progress` in scope | Explorer sorted by progress asc |
| **Average health** | Score | Existing avg health | Explorer sorted by health asc |
| **Average risk** | Score | Existing avg risk | Explorer sorted by risk desc |
| **Total projects** | Volume | Count in scope | Explorer clear exception filters |
| **Recommendation summary** | Count | Open recommendations in scope (or critical reco count) | Recommendations page or `hasRecommendation=true` |

**Removed from catalog:** Average Needs Attention (mean score).

### Home — widgets to remove or demote

| Current widget | Decision | Reason |
|----------------|----------|--------|
| Dense 8-card row without actions | **Replace** | No drill; weak ordering |
| “Scope score snapshot” 3-bar chart | **Remove from Home** | Duplicates avg score KPIs |
| Long executive summary paragraph first | **Demote** below exceptions or one-line status |
| Full recommendation list unbounded | **Cap** top 5 + View all |
| Recalculate as primary button | **Secondary** menu |

### Multi-workspace glance (if applicable)

When user can access &gt;1 workspace, Home may show a compact **workspace comparison strip** (name, projects, avg health, critical count) — each card opens that workspace Home. This replaces a separate Executive product surface.

---

## 4.2 Portfolio overview

**Business question:** *How is this analytical collection performing, and where should I look next?*

Portfolio pages split into two modes/tabs:

| Tab | Purpose |
|-----|---------|
| **Overview** | Management triage for **member projects only** (default) |
| **Membership** | Add/remove projects (organizational only) — secondary |

### Overview layout

```
┌─────────────────────────────────────────────────────────────────┐
│ Header: Portfolio name · workspace crumb · Open in Explorer     │
├─────────────────────────────────────────────────────────────────┤
│ 0. PORTFOLIO HEALTH SUMMARY (R4) — always first                 │
│    Narrative + band chips + key rates (see below)               │
├─────────────────────────────────────────────────────────────────┤
│ 1. Exception KPIs (members only)                                │
│ 2. Synthesis KPIs (avg progress, health, risk — no avg Needs Att.)│
│ 3. Exception queue                                              │
│ 4. Charts (health distribution, progress, delayed vs on-track)  │
│ 5. Trend (if data) · Recommendations · Membership tab link      │
└─────────────────────────────────────────────────────────────────┘
```

### Portfolio Health Summary (required — R4)

A dedicated **summary strip/panel** at the top of Portfolio Overview that answers: *Is this portfolio healthy, and how bad is the exception load?*

| Element | Content |
|---------|---------|
| **Headline status** | Portfolio health band derived from **average health** of members (Healthy / Watch / Critical thresholds — presentation mapping only; no new formula beyond existing scores) |
| **Member coverage** | `N members` · optional `N of M workspace projects` |
| **Exception rates** | Critical count + % · Delayed count + % · **Projects needing attention count + %** |
| **Synthesis line** | Avg health · Avg risk · Avg progress (compact) |
| **One-line insight** | Optional short text from existing insights DTO if available; otherwise omit |
| **Primary CTA** | **Open in Explorer** (portfolio scope) · secondary: View all critical / delayed / needing attention |

This section is **not** a second scoring engine. It composes existing member analytics for management scanability.

### Aggregated statistics (portfolio)

Reuse Home KPI catalog constrained to members (including Needs Attention as **count + %**, not average score).

### Drill-down

All cards use `portfolioId` in scope → Explorer / Detail.

### Portfolio list page

| Column / card | Content |
|---------------|---------|
| Name | Link to Overview |
| Members | Count |
| Health summary | Band chip from avg health + critical/delayed badges |
| Critical | Count badge |
| Delayed | Count badge |
| Needing attention | Count (or count + %) |

Row click → Portfolio Overview (not Membership).

---

## 4.3 Explorer — primary analytical workspace

**Business question:** *Show me the set of projects that match my management question so I can compare and prioritize.*

This is the **main daily surface** (P6). Table-first, filter-first, high density.

### Layout

```
┌──────────────────────────────────────────────────────────────────┐
│ Scope: Workspace · Portfolio                                     │
│ [Save view] [Saved views ▾] [Reset] · Group by [—] · Density     │
├────────────────────┬─────────────────────────────────────────────┤
│ FILTER PANEL       │ TOOLBAR: search · column picker · export    │
│ (collapsible)      │ multi-sort · clear group                    │
│                    ├─────────────────────────────────────────────┤
│ See §5             │ RESULTS (flat table or grouped sections)    │
│                    │ one row per project                         │
│                    │ select row → Detail                         │
│                    ├─────────────────────────────────────────────┤
│                    │ FOOTER: count matching · pagination         │
└────────────────────┴─────────────────────────────────────────────┘
```

### Default columns (required)

| Column | Notes |
|--------|--------|
| Project name | Link to Detail |
| Status | Project status |
| Progress % | Local field |
| Health | Score + band chip |
| Risk | Score + band chip |
| Needs Attention | Per-project **score** + band chip (detail literacy; not the Home avg KPI) |
| Delayed | Yes/No or days overdue |
| Overdue WPs | Count (actionable) |
| Upcoming deadline | Next end/due within configured window (§8.1) |
| Recommendations | Count or highest severity |
| Portfolio(s) | Names (truncated) |
| Owner | If available on project; else hide column until data exists |

### Optional columns

Start date, end date, budget, last calculated at, workspace (if multi-ws).

### Sorting (R2)

| Capability | Spec |
|------------|------|
| **Column sort** | Click column header: asc → desc → clear (or cycle) |
| **Multi-sort** | Optional secondary sort (e.g. Needs Attention desc, then Risk desc) via shift-click or sort builder |
| **Default sort** | Exception entry: **Needs Attention score desc**, then Risk desc, then Health asc |
| **Persistence** | Sort is part of **Saved Views** (§5.6) and URL state |

### Grouping (R2)

| Capability | Spec |
|------------|------|
| **Group by** | Single dimension at a time (v1): none \| health band \| risk band \| needs-attention band \| status \| delayed (Y/N) \| portfolio \| owner (if available) |
| **Group headers** | Show group label + project count + optional mini stats (avg health, critical count in group) |
| **Collapse** | Groups collapsible; expand-all / collapse-all |
| **Interaction with filters** | Filters apply first; grouping partitions the filtered result set |
| **Sort within groups** | Active sort applies inside each group |
| **Persistence** | Grouping is part of **Saved Views** |

### Row interactions

- Click name / row → Project Detail  
- Click overdue WP count → Detail scrolled to counts **or** keep Explorer with that project highlighted (prefer Detail L3 counts)  
- Click recommendation badge → Detail recommendations section  

### Empty filter result

“No projects match. Clear filters / adjust range.” + Reset.

### Entry points into Explorer

| From | Pre-applied filters |
|------|---------------------|
| Home Critical KPI | `critical=true` + scope |
| Home Delayed | `delayed=true` |
| Home Overdue WPs | `hasOverdueWp=true` |
| Home Needs Attention | `needsAttention=true` |
| Home Upcoming | `upcomingDeadlineDays={window}` |
| Portfolio Open in Explorer | `portfolioId` |
| Chart band click | Corresponding health/risk band |
| Recommendations View projects | `hasRecommendation=true` (+ severity if applicable) |
| Saved View | Full view state (§5.6) |

---

## 4.4 Project Detail

**Business question:** *Why is this project in this state, and what should I focus on next?*

### Layout

```
Header: name · status · progress · scope crumbs (Workspace / Portfolio / Explorer back)
Band chips: Health | Risk | Needs Attention

L1 Score cards (3) + Completion / Progress
L2 Factor contribution bars (per score) — visual explainability (P4)
    using existing factor payloads if present; else structured explanation blocks
Counts strip: Overdue WPs | Open WPs | High-priority open | Days vs plan
    counts remain counts; links show evidence lists (local), not OP boards
Trend chart: health / risk / Needs Attention over snapshots
Recommendations for this project
Actions: Generate project report · Back to Explorer (preserve filters) · Back to Home
```

### Do not add

- Work package board / edit  
- Gantt  
- Live OpenProject panels  

---

## 4.5 Recommendations

| Placement | Behavior |
|-----------|----------|
| Home / Portfolio strip | Top N by severity/priority, scoped |
| Full page `/recommendations` | Filter by scope, severity, project search |
| Project Detail | Project-only list |

Each item links to **Project Detail** with reco context. Optional “Show in Explorer” for multi-project reco sets.

No new rules; presentation + navigation only.

---

## 4.6 Reports

Unchanged product role: immutable PDF/Excel from local analytics.

UX refinements only:

- Default scope from current scope bar  
- Entry points: Home secondary, Explorer toolbar, Project Detail, Portfolio Overview  
- History list remains  

---

## 4.7 Connections & Settings

**Not part of the daily analytical journey.** Visually secondary in nav for analytics users.

### Connections (R6) — focused scope

| In scope | Out of scope (unless multi-connection) |
|----------|----------------------------------------|
| Connect / disconnect OpenProject instance | Portfolio organization (use Portfolios) |
| Credential / API key (temp) / future OAuth entry | Analytics KPI browsing |
| **Synchronize** + last sync status / errors | Full multi-tenant “workspace admin console” as default UX |
| Sync freshness messaging | |

**Single connection (common case):** Connections is a **connection + sync control panel** for the one OpenProject link. Do not present a heavy multi-workspace management shell.

**Multiple connections:** Only when more than one workspace/connection exists, show a **connection list** (name, URL, sync status) to switch, add, or remove connections. Workspace switcher for analytics remains primarily the **scope bar** on Home/Explorer.

### Settings

Preferences, theme, and (when implemented) analytics access grants. No change to product identity.

---

# 5. Filter design (Explorer)

## 5.1 Filter dimensions

| Filter | Control | Values / behavior |
|--------|---------|-------------------|
| **Workspace** | Select | Required; or All if multi-ws |
| **Portfolio** | Select | All \| specific portfolio (membership filter) |
| **Project** | Search multi-select | Name contains / pick list |
| **Status** | Multi-select | From distinct project statuses in data |
| **Health range** | Dual slider or min/max | 0–100 on health score |
| **Progress range** | Dual slider | 0–100 on progress % |
| **Risk level / range** | Band chips and/or slider | Low/Med/High bands **or** 0–100 |
| **Delayed only** | Toggle | `delayed=true` |
| **Critical only** | Toggle | `critical=true` |
| **Needs Attention only** | Toggle | `needsAttention=true` |
| **Overdue work packages** | Toggle or min count | `hasOverdueWp` or `overdueWpMin` |
| **Upcoming deadlines** | Toggle + days | Uses **upcoming window** (§8.1); UI shows current window (e.g. “Next 14 days”); user may override days in filter |
| **Recommendation status** | Select | Any / Has reco / Critical reco / No reco |
| **Owner** | Select/search | Only if owner data exists; else disabled with “Not available” |
| **Date range** | Date pair | Filter by project end date or last calculated_at (label clearly) |

## 5.2 Filter UX rules

1. **URL-serializable** filter state (shareable, back-button friendly).  
2. Active filters shown as **chips** above the table; chip × removes one.  
3. **Reset** clears to scope-only (workspace + portfolio); does not delete Saved Views.  
4. Changing workspace clears project-specific selections; portfolio may reset to All.  
5. Toggles **Critical / Delayed / Needs Attention** are mutual with free ranges when they conflict—last applied wins, or ranges auto-adjust to band.  
6. Filters never trigger rescore; they only query local analytics projections.

## 5.3 Sorting (R2)

| Rule | Detail |
|------|--------|
| Default (exception entry) | Needs Attention score **desc**, Risk **desc**, Health **asc** |
| Column header sort | Any default/optional column |
| Multi-sort | Supported (primary + secondary) |
| URL + Saved Views | Sort state is serializable |

## 5.4 Grouping (R2)

| Rule | Detail |
|------|--------|
| Dimensions (v1) | none, health band, risk band, needs-attention band, status, delayed, portfolio, owner (if data) |
| One group dimension | Single `groupBy` at a time for v1 |
| Headers | Label, count, optional group-level mini stats |
| Collapsible | Yes |
| Order of groups | Stable, meaningful (e.g. Critical → Watch → Healthy for health bands) |

## 5.5 Performance note (no architecture change)

Server-side filtering/sorting preferred for large workspaces; client acceptable for small N with same API contract. Implementation detail for M11B.

## 5.6 Saved Views (R5) — supersedes “Saved Filters”

A **Saved View** is a named, reusable Explorer configuration—not filters alone.

### Persisted state

| Aspect | Included |
|--------|----------|
| **Scope** | Workspace (+ portfolio if set) |
| **Filters** | Full filter dimension set (§5.1) |
| **Sorting** | Primary (+ secondary if any) |
| **Grouping** | `groupBy` dimension or none |
| **Visible columns** | Ordered list of column ids shown |
| **Optional** | Density (comfortable/dense), page size |

### UX

| Action | Behavior |
|--------|----------|
| **Save view** | Name required; optional “update existing” |
| **Saved views menu** | List user views; apply replaces current Explorer state |
| **Default view** | Optional mark one view as personal default for Explorer landing |
| **Reset** | Back to scope-only + default columns/sort (not a delete) |
| **Share** | URL reflects current state; formal shared-team views may be M11B+ if storage allows |
| **Storage** | User preference / backend (M11B chooses); must survive reload |

### Naming

UI label: **Saved Views** (not “Saved Filters”). Migration: any prior “saved filter” concept is replaced by Saved Views.

---

# 6. Navigation and drill-down behavior

## 6.1 Universal rule

> **Every KPI, exception chip, and interactive chart element must define a drill target.**  
> Default target = **Explorer** with context-preserving filters.  
> Project-level rows/targets = **Project Detail**.

## 6.2 Context preservation

| Current page | Drill scope |
|--------------|-------------|
| Home (workspace) | `workspaceId` + filters |
| Home (portfolio selected) | `workspaceId` + `portfolioId` + filters |
| Portfolio Overview | `workspaceId` + `portfolioId` + filters |
| Explorer | Update filters in place |
| Multi-workspace strip | Switch `workspaceId` then apply |

## 6.3 Standard “View all” patterns

| Label | Target |
|-------|--------|
| View all critical projects | Explorer `critical=true` |
| View all delayed projects | Explorer `delayed=true` |
| View all needing attention | Explorer `needsAttention=true` |
| View overdue work packages | Explorer `hasOverdueWp=true` (projects with overdue WPs) |
| View upcoming deadlines | Explorer `upcomingDeadlineDays=N` |
| View all recommendations | `/recommendations` with scope |
| Open in Explorer | Explorer with current scope, no exception toggle |

## 6.4 Breadcrumbs

```
Home / {Workspace}
Home / {Workspace} / {Portfolio}
Explorer / {Workspace} [/ {Portfolio}] [/ filters summary]
Project / {Name}     with "Back to Explorer" restoring query string
```

## 6.5 Journey (guided)

```
Login
  → Home (exceptions first)           // “What needs me?”
  → View all / chart / queue row
  → Explorer (filtered set)           // “Compare and prioritize”
  → Project Detail                    // “Why / what next?”
  → Recommendation or Report          // “Act / communicate”
  → Back to Explorer (filters kept)
```

Natural pressure: **monitor → investigate set → inspect one → decide**.

---

# 7. Chart recommendations

## 7.1 Principles

1. One chart = one management question.  
2. Prefer **interactive** charts that apply Explorer filters on click.  
3. Remove charts that only restate three KPI averages.  
4. No chart without a drill definition.  
5. Hide charts when insufficient data (e.g. &lt;2 trend points).

## 7.2 Catalog by page

### Home / Portfolio Overview

| Chart | Type | Question | Interaction |
|-------|------|----------|-------------|
| **Health distribution** | Stacked bar or histogram by band (Critical / At risk / Healthy) | How are projects distributed by health? | Click band → Explorer health range / critical |
| **Progress overview** | Banded bar (0–25, 26–50, 51–75, 76–100) or horizontal avg vs target | Where is delivery progress? | Click band → progress range filter |
| **Delayed vs on track** | Simple 2-segment bar or donut | How many delayed? | Click Delayed → `delayed=true` |
| **Risk snapshot** | Band counts (optional if space) | Where is risk concentrated? | Click → risk filter |
| **Needs Attention trend** (optional) | Line of avg Needs Attention or count over time | Is pressure rising? | Click point → date range if supported; else non-interactive legend only |

### Remove / avoid on Home

| Chart | Why |
|-------|-----|
| 3-bar “avg health / risk / attention” | Redundant with KPI scores |
| Pie of random categories without action | Low decision value |
| Activity feed / decorative sparklines | Out of scope unless tied to exceptions |

### Explorer

| Chart | Type | Notes |
|-------|------|-------|
| Optional mini distribution | Health histogram of **current result set** | Click re-filters; can be collapsed |

### Project Detail

| Chart | Type | Notes |
|-------|------|-------|
| **Score trends** | Multi-line (Health, Risk, Needs Attention) | Keep; improve legend and empty state |
| **Factor contribution** | Horizontal bars per score | Primary explainability chart (P4) |

### Multi-workspace glance

| Chart | Type | Notes |
|-------|------|-------|
| Avg health by workspace | Horizontal bar | Click → that workspace Home |
| Critical count by workspace | Bar | Click → workspace + critical Explorer |

## 7.3 Interactivity standard

| Gesture | Result |
|---------|--------|
| Click segment/band | Apply filter → navigate to Explorer (or update Explorer if already there) |
| Click legend | Toggle series visibility (detail trends only) |
| Hover | Tooltip: label, value, % of total |
| Keyboard | Focusable segments where feasible (a11y) |

---

# 8. KPI design summary (single catalog)

Canonical management KPIs for **any multi-project scope** (workspace or portfolio):

### Exception tier (always first)

1. **Projects needing attention** — **count + percentage** of projects in scope (R1; not average score)  
2. Critical projects  
3. Delayed projects  
4. Overdue work packages  
5. Upcoming deadlines (within configured window — §8.1)

### Synthesis tier

6. Average progress  
7. Average health  
8. Average risk  

### Context tier

9. Total projects  
10. Recommendation summary (count / critical count)

### Explicitly not primary KPIs

| Metric | Reason |
|--------|--------|
| **Average Needs Attention** (mean score) | Replaced by count + % (R1); score stays on rows/detail |
| Workspace count (on single-ws Home) | Ops noise |
| “Active” alone without definition | Prefer status filter in Explorer |
| Duplicate avg score bar chart values | Covered by synthesis KPIs |
| Budget variance as default Home KPI | Optional column/detail unless product prioritizes finance later |

## 8.1 Upcoming deadlines window (R3)

| Property | Spec |
|----------|------|
| **Definition** | Count of projects whose **planned end date** (and/or next material due date if exposed) falls in `[today, today + W days]` inclusive, and is not already classified solely as delayed-past if product prefers split—default: **upcoming = future within window**; **delayed = past end**. |
| **Default window W** | **14 days** |
| **Configurable** | Yes — application config e.g. `projectanalytics.ui.upcoming-deadline-days` / env `UPCOMING_DEADLINE_DAYS` (M11B). Optional per-user override later. |
| **UI** | KPI label: **Upcoming deadlines (14d)** or **Upcoming deadlines** with hint “Next 14 days”. Explorer filter shows the active window and allows temporary override (7 / 14 / 30 / custom). |
| **Drill-down** | Explorer with `upcomingDeadlineDays=W` |
| **No engine change** | Pure date comparison on local project/WP fields |

---

# 9. Wireframe-level page inventory (M11B checklist)

| Page | New / rewrite | Priority |
|------|---------------|----------|
| Shell nav + scope bar | Rewrite | P0 |
| Home triage | Rewrite layout + KPI actions (count/% Needs Attention) | P0 |
| Explorer | **New** primary surface + sort/group + Saved Views | P0 |
| Portfolio list | Enhance cards + health summary badges | P1 |
| Portfolio overview tab | Rewrite + **Portfolio Health Summary** | P0 |
| Portfolio membership tab | Keep; secondary | P1 |
| Project Detail | Enhance L2 factors + counts strip | P0 |
| Recommendations page | New or promote embedded | P1 |
| Reports | Scope defaults only | P2 |
| Executive page | **Deprecate** as primary; fold into Home multi-ws | P1 |
| Connections | Connection + sync focus (R6); multi-list only if multi-connection | P1 |
| Settings | Unchanged role | — |

---

# 10. User journey specification

## 10.1 Happy path — morning triage

1. Login → **Home** (or Explorer if preference; product default for M11A: **Home for triage entry is acceptable if Explorer is equally primary**—see §10.4).  
2. Scan exception KPIs (red/watch first).  
3. Open **View all delayed** → Explorer.  
4. Sort by Needs Attention; open worst project.  
5. Read factors + recommendations.  
6. Generate report or leave reco for stand-up.  
7. Return to Explorer; clear filter; next exception.

## 10.2 Portfolio stand-up

1. Portfolios → Overview for “Finance”.  
2. Note critical + delayed.  
3. Open Explorer with portfolio filter.  
4. Drill two projects; export portfolio report.

## 10.3 Deep investigation

1. Explorer: health 0–40 + overdue WPs + has recommendation.  
2. Compare five projects in table.  
3. Detail on top two; read trends.  
4. Recommendations full list for follow-up.

## 10.4 Default landing (M11A refinement)

Frozen PE said Explorer default. M11A **keeps Explorer as the analytical center of gravity** but recognizes triage needs:

| Recommendation | Landing |
|----------------|---------|
| **Default** | **Home** for first paint after login when data exists (**triage-first morning**), with prominent **Explorer** in nav and every KPI drilling into Explorer |
| **Alternative** | User preference: “Start on Explorer” |

Rationale: managers often need “what hurts?” in 30 seconds before filtering. Explorer remains the **primary long-session workspace** (P6). This is a **presentation sequencing** decision, not a product identity change.

If this conflicts with `19_Product_Experience.md` default table, **M11A supersedes landing only**; all other PE freezes stand.

---

# 11. Accessibility and density

- Color never sole severity indicator (icons + text bands).  
- Tables keyboard-navigable; filters labeled.  
- Responsive: filter panel collapses to drawer on narrow screens.  
- Dense table default on desktop; comfortable mode toggle optional.

---

# 12. Out of scope for M11A / M11B

| Item | Notes |
|------|-------|
| New scoring formulas | Frozen |
| Recommendation rule changes | Frozen |
| OAuth | Separate |
| OpenProject task UI | Out |
| Real-time collaboration | Out |
| Custom dashboard builder | Out |
| Dynamic rule-based portfolios | Future |
| Inferring managers from OP roles | Forbidden |

M11B may add **DTO fields / query params** for filters and aggregates that are pure projections of existing data.

---

# 13. Acceptance criteria — M11A frozen

Approved and frozen with refinements R1–R6:

1. [x] Home exception-first layout and KPI catalog  
2. [x] Portfolio Overview vs Membership split + **Portfolio Health Summary**  
3. [x] Explorer as main analytical workspace with full filter set  
4. [x] Explorer **sorting** and **grouping**  
5. [x] **Saved Views** (filters + sort + group + columns)  
6. [x] Universal drill-down / View all context rules  
7. [x] Chart catalog (keep / remove / interact)  
8. [x] IA and journey Home → Explorer → Detail  
9. [x] Terminology Needs Attention / Delayed  
10. [x] Needs Attention as **count + %**, not average score  
11. [x] Upcoming deadlines window **default 14d, configurable**  
12. [x] Connections = connection + sync; multi only when needed  
13. [x] Landing: Home triage default with Explorer primary workspace  
14. [x] No changes required to analytics/recommendation engines  

**Next:** **M11B implementation** executes this document without reopening UX philosophy.

---

# 14. Relationship to `19_Product_Experience.md`

| Topic | M11 (frozen architecture) | M11A (this spec) |
|-------|---------------------------|------------------|
| Persona / identity | Frozen | Unchanged |
| P1–P6 | Frozen | Operationalized into layouts |
| Surfaces | Defined abstractly | Concrete layouts, KPIs, charts |
| Explorer | Principle | Full filter + sort + group + Saved Views |
| Drill-down | Implied by P2 | Universal contract |
| Landing | Explorer default | Home triage default + Explorer primary workspace (landing-only supersession) |
| Needs Attention on Home | Score literacy | **Count + %** of projects (not mean score) |

---

# 15. Implementation handoff notes (for M11B, not binding design)

- Prefer query-param driven Explorer state (filters, sort, groupBy, columns).  
- Reuse existing scope dashboard endpoints; extend with missing aggregates (avg progress, overdue WP totals, upcoming deadlines, needs-attention **count and %**) as **read models**.  
- Map UI “Needs Attention” → API `attention` fields without renaming backend.  
- Config: `UPCOMING_DEADLINE_DAYS` default 14.  
- Saved Views storage: user preference or API—must persist full view state (§5.6).  
- Deprecate or redirect `/executive` to Home multi-workspace mode.  
- Connections UI: sync-first; multi-connection list only when count &gt; 1.  
- No client-side scoring.

---

End of Document
