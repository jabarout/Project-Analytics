# Analytics Engine

Version: 1.0

Status: Draft

---

# 1. Purpose

The Analytics Engine is the core intelligence component of Project Analytics.

Its responsibility is to transform synchronized operational project data into actionable business intelligence.

It calculates business metrics, detects risks, measures project health, identifies trends, and generates recommendations.

The Analytics Engine never modifies operational data originating from OpenProject.

---

# 2. Objectives

The Analytics Engine shall:

- Calculate KPIs
- Measure project health
- Detect project risks
- Rank project priorities
- Detect trends
- Generate recommendations
- Explain every calculated metric
- Support executive decision-making

---

# 3. Data Sources

The Analytics Engine consumes synchronized data from:

- Projects
- Work Packages
- Milestones
- Statuses
- Priorities
- Users
- Time Tracking
- Budget Information (when available)

It never communicates directly with the frontend.

---

# 4. Processing Pipeline

```

```
Synchronized Data

↓

Validation

↓

Normalization

↓

Metric Calculation

↓

Health Assessment

↓

Risk Assessment

↓

Attention Assessment

↓

Recommendation Generation

↓

Persistence

↓

Dashboard APIs
```

```markdown

---

# 5. KPI Categories

The engine computes several categories of KPIs.

## Schedule KPIs

Examples:

- Progress
- Planned Progress
- Schedule Variance
- Milestone Completion
- Delayed Work Packages

---

## Budget KPIs

Examples:

- Budget Utilization
- Budget Variance
- Estimated Cost
- Remaining Budget

---

## Resource KPIs

Examples:

- Assigned Resources
- Workload Distribution
- Capacity Utilization
- Overloaded Resources

---

## Delivery KPIs

Examples:

- Completed Tasks
- Open Tasks
- Critical Tasks
- Blocked Tasks

---

## Quality KPIs

Examples:

- Reopened Work Packages
- Critical Issues
- High Priority Tasks
- Resolution Time

---

# 6. Health Score

The Health Score provides an overall measure of project condition.

Range:

0–100

Suggested interpretation:

| Score | Status |
|--------|--------|
| 90–100 | Excellent |
| 75–89 | Healthy |
| 60–74 | Moderate |
| 40–59 | At Risk |
| 0–39 | Critical |

Health Score is calculated using configurable weighted factors.

Example factors include:

- Schedule Performance
- Budget Performance
- Task Completion
- Issue Severity
- Risk Level

The exact weighting should remain configurable rather than hard-coded.

---

# 7. Risk Score

Risk Score estimates the likelihood of project failure or significant disruption.

Range:

0–100

Example contributing factors:

- Delayed milestones
- High number of critical tasks
- Budget overrun
- Low completion rate
- Resource overload

Higher scores indicate higher risk.

---

# 8. Attention Score

Attention Score prioritizes projects requiring immediate management attention.

It combines:

- Health Score
- Risk Score
- Trend Analysis
- Business Priority

Projects with the highest Attention Score appear first in executive dashboards.

---

# 9. Trend Analysis

Historical analytics are used to detect trends.

Examples:

- Progress increasing
- Progress decreasing
- Budget consumption accelerating
- Increasing overdue work packages
- Health Score deterioration
- Risk escalation

Trend calculations should use historical snapshots rather than current values alone.

---

# 10. Recommendation Engine

Recommendations are generated from analytics.

Each recommendation contains:

- Title
- Description
- Severity
- Explanation
- Supporting metrics
- Timestamp

Recommendations must always reference measurable evidence.

---

# 11. Explainability

Every calculated metric must be explainable.

Example:

Health Score = 58

Explanation:

- Budget exceeds forecast by 12%.
- Four milestones are overdue.
- Completion rate decreased over the last two reporting periods.

Users should understand why a value was produced.

---

# 12. Historical Analytics

Historical snapshots enable:

- Trend charts
- KPI evolution
- Health evolution
- Risk evolution
- Executive comparisons

Snapshots should be stored periodically after synchronization.

---

# 13. Configurable Rules

Business rules should be configurable.

Examples:

- Health Score weights
- Risk thresholds
- Alert thresholds
- Recommendation triggers

Configuration should not require code changes.

---

# 14. Alert Generation

Alerts are created when thresholds are exceeded.

Examples:

- Health Score below threshold
- Budget exceeded
- Milestone overdue
- Risk Score above threshold
- Synchronization failure

Alerts should include severity and supporting information.

---

# 15. Performance Requirements

Analytics calculations should:

- Execute asynchronously where appropriate
- Avoid unnecessary recalculation
- Recalculate only affected entities after incremental synchronization
- Support caching of computed results

Long-running calculations should not block user requests.

---

# 15.1 Milestone 5 implementation status (intentional)

## Post-synchronization recalculation scope

**Current (M5):** After any successful synchronization of a workspace, analytics recalculate **all projects** in that workspace, then refresh portfolio stored score averages for portfolios in that workspace.

This applies to initial, incremental, manual, and scheduled sync alike. Correctness is prioritized over partial recalculation.

**Target (deferred — not a redesign):** After incremental synchronization, recalculate only **affected** projects (e.g. those upserted or touched during the sync run), then recompute workspace/portfolio aggregates from stored project scores. The scoring engine and dashboard APIs remain unchanged; only the recalculation trigger/set selection improves.

## Analytics snapshot retention

**Current (M5 + M10):** Each project rescoring appends a row to `analytics_snapshot`. Trend APIs read the latest N snapshots per project. Snapshot retention is configurable (M10).

---

# 15.2 Milestone 12 — Decision metrics (evolutionary)

M12 **extends** the existing engine. It does **not** replace Health / Risk / Attention formulas or rebuild scoring.

## Architecture rules (binding for M12+)

1. **Preserve existing architecture** — extend Health / Risk / Attention, snapshots, and DTOs; do not rebuild.
2. **Search before inventing** — before any new metric, find an existing calculation or field that can be reused.
3. **No duplicates** — no parallel formulas, no parallel DTO fields for the same quantity, no parallel metric-calculation classes.
4. **`ProgressMetrics` is the single source of truth** for progress and schedule-related quantities (`actualProgress`, `expectedProgress`, `progressGap`, `overdueRatio`, `scheduleVarianceDays`). Calculators and the engine **call** it; they do not re-derive those values.
5. **Prefer extending existing structures** over new abstractions unless there is a clear architectural reason.

## Canonical progress (cross-page consistency)

All surfaces (Explorer `progress`, Project Detail progress, `completionPercentage`, schedule/delivery score inputs) use the same **actual progress**:

| Priority | Source |
|----------|--------|
| 1 | When work packages exist: `(completed / total) × 100` |
| 2 | Else when OpenProject `project.progress` is set: that value (clamped 0–100) |
| 3 | Else: `0` |

Rationale: OP project progress is often unset or stale relative to synchronized work packages. Prefer WP completion when available.

Implementation: `ProgressMetrics.actualProgress(ProjectScoringInput)` → stored as existing `completion_percentage`.

## Extended schedule / overdue metrics (nullable when incomplete)

| Metric | Formula | Storage / API field | Null when |
|--------|---------|---------------------|-----------|
| **Actual progress** | see above | existing `completionPercentage` | — (0 if unknown) |
| **Expected progress** | `(elapsedDays / totalDays) × 100` (clamped 0–100) | `expectedProgress` | start/end incomplete |
| **Progress gap** | `actual − expected` (negative = behind) | `progressGap` | expected is null |
| **Overdue ratio** | `overdueOpenWPs / totalWPs` (0–1) | `overdueRatio` | no work packages |
| **Avg / max overdue age (days)** | days past due among overdue open WPs | `avgOverdueAgeDays` / `maxOverdueAgeDays` | no overdue open WPs |
| **Schedule variance (days)** | days from end → as-of (**positive = late**) | existing `scheduleVariance` | no end date |

Do **not** add a second deadline field (e.g. “days to deadline”); invert/display `scheduleVariance` in the UI if needed.

These M12 fields are stored on `analytics` and partially on `analytics_snapshot` (Flyway `V10`). UI may expose them with minimal layout; polish is deferred.

## Score explainability

Score factors (Health / Risk / Attention contribution breakdown) are serialized as JSON on `analytics` at recalculation time and returned on Project Analytics / Detail APIs so factor bars work after reload (not only on fresh compute).

## Scope KPI aggregates (workspace / portfolio)

`ScopeAnalyticsKpiResponse` averages **stored** project analytics fields only (no second schedule formula):

| Aggregate | Source field |
|-----------|--------------|
| averageCompletion | completion_percentage |
| averageExpectedProgress | expected_progress |
| averageProgressGap | progress_gap |
| projectsBehindSchedule | count where progress_gap &lt; 0 |
| averageOverdueRatio | overdue_ratio |
| projectsWithOverdueWorkPackages | count where overdue_ratio &gt; 0 |

**Surface discipline:** Home stays lean triage. Portfolio analytical page is the deep-dive for progress/schedule quality, member intelligence, and dual attention/critical lists.

## What M12 deliberately does **not** change

- Health / Risk / Attention **weights** and label thresholds (unless a later deliberate change is documented)
- Recommendation rule set architecture
- Explorer-first PE architecture
- Budget variance remains null until spent budget exists in the local model
- Home page clutter — quality over quantity on triage; detail lives on Portfolio / Project / Explorer

---

# 16. Future AI Enhancements

Future versions may include:

- Predictive completion dates
- Budget forecasting
- Resource forecasting
- Natural language summaries
- Root cause analysis
- Anomaly detection
- What-if scenario simulation

These capabilities should extend the existing engine without replacing its core architecture.

---

# 17. Testing Strategy

The Analytics Engine shall be tested through:

- Unit tests for each calculation
- Integration tests for processing pipelines
- Regression tests for scoring algorithms
- Performance tests for large datasets

Business calculations must produce deterministic results for identical inputs.

---

# 18. Design Principles

The Analytics Engine shall:

- Be deterministic
- Be explainable
- Be configurable
- Be modular
- Be testable
- Be extensible
- Be independent of UI concerns

---

# 19. AI Implementation Notes

When implementing the Analytics Engine:

- Separate calculation logic by responsibility.
- Keep scoring algorithms modular.
- Avoid duplicated formulas.
- Externalize thresholds and weights.
- Preserve historical snapshots.
- Return explanations alongside calculated values.
- Ensure all algorithms are covered by automated tests.

---

# End of Document