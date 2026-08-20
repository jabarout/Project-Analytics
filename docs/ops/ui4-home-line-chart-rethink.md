# Homepage line chart rethink — data analysis & proposal

**Status:** Analysis only — **no implementation** · **UI-5 not started**  
**Date:** 2026-08-19  
**Product preference (confirmed):** Single aggregate Health line + ranked drivers list

---

## 1. What the current chart does (and why it’s wrong)

Today’s Homepage hero charts **one Health-score series per project** (≤3 exception projects).

That fails as a workspace default:

| Scale | Failure |
|-------|---------|
| 20–100 projects | Spaghetti lines, huge legends, color overload |
| UX | Legend click to hide series is not an explicit “choose what to compare” control |
| Question answered | “How did a few projects move?” — not “How is overall Health evolving?” |

---

## 2. What data actually exists

### Canonical scores (unchanged formulas)

| Field | Meaning | Source |
|-------|---------|--------|
| `healthScore` | Project **Health** 0–100 | `analytics` + `analytics_snapshot` |
| `riskScore` | Project **Risk** 0–100 | same |
| `attentionScore` | Project **Needs Attention** 0–100 | same |
| `completionPercentage` | Actual progress % | same |

### Already-productized workspace aggregate (current point only)

Workspace / portfolio KPIs already expose:

**`averageHealthScore`** = equal-weight mean of stored project Health scores in scope  

Implemented in `AnalyticsQueryService.buildScopeKpis()` — same aggregation used on Home KPI “Average health”.

This is **not** a new metric. It is the existing scope KPI.

### Historical storage

| Capability | Exists? |
|------------|---------|
| Per-project snapshot history | **Yes** — `analytics_snapshot` (Health/Risk/Attention/…) + `GET …/analytics/projects/{id}/trends` |
| Workspace-level trend API | **No** |
| Pre-computed historical `averageHealthScore` series | **No** |

### Empirical check (local demo workspace, 7 projects)

- Workspace recalculate writes snapshots for **all** projects in near-identical timestamp waves.
- Minute-bucket clustering shows **full coverage** (n=7) at each wave.
- Mean Health per wave (e.g. 62.12 → 62.6) matches the spirit of current `averageHealthScore` (62.6 latest).

So: historical “Average Health” **can** be reconstructed from existing snapshot rows without inventing a scoring formula.

---

## 3. Is an aggregate Health trend analytically valid?

**Yes — with a strict definition that mirrors the existing KPI.**

### Proposed series (canonical)

For each **recalculation wave** `T` in the workspace:

1. Take every project in the workspace that has an `analytics_snapshot` in that wave (same sync/recalc batch; group by time window, e.g. same minute / same `calculated_at` cluster).
2. Compute  
   **`avgHealth(T) = mean(health_score of those snapshots)`**  
   — identical aggregation to today’s `averageHealthScore`, applied historically.
3. Plot **one line**: `avgHealth(T)` over waves.

### What we must *not* do

| Anti-pattern | Why |
|--------------|-----|
| Invent a new “portfolio health index” / weighted formula | New metric — forbidden without PE change |
| Fabricate missing points | No fake history |
| Carry-forward last-known Health for projects missing at `T` **without labeling** | Silent bias vs current KPI definition |
| Average only “top attention” projects and call it workspace Health | Misleading scope |

### Coverage edge cases (honest empty / partial)

| Situation | Behavior |
|-----------|----------|
| &lt; 2 waves with ≥1 snapshot each | Empty state (same as “not enough history”) |
| Wave with sparse project coverage | Use mean of **projects present in that wave only**; show `n=` in tooltip |
| Prefer sync with KPI | Prefer waves from workspace recalculate (near-full coverage) |

---

## 4. Recommended Homepage shape (matches your preference)

### A. Primary — one overall Health trend (ECharts line)

- **Title:** e.g. “Average Health over time”
- **Subtitle:** “Equal-weight mean of project Health scores (0–100) · same definition as Average health KPI”
- **One series**, bright viz color, area fill, latest value label, rich tooltip (`avg`, `n` projects in wave, timestamp)
- **Trend direction** from first→last wave delta (↑/↓)

### B. Secondary — ranked drivers (not 20 lines)

Compact list / small multiples **next to or under** the aggregate chart:

| Column | Data |
|--------|------|
| Project | name → Project Detail |
| Latest Health | last snapshot / current analytics |
| Δ Health | last − previous snapshot (or first→last in window) — **delta of existing Health**, not a new score |
| Direction | ↑ improve / ↓ worsen |

Sort by |Δ| or by worsening first. Cap display (e.g. top 5 worseners + top 5 improvers) with “View all in Explorer”.

This answers “who is driving the move?” without spaghetti.

### C. Optional later — explicit project compare (not default)

Only if needed after A+B:

- “Compare projects” control with search + multi-select + count + **hard max** (e.g. 5)
- User must opt in; **no** legend-only hide/show as the selection UX

**Out of default Home path.**

---

## 5. Implementation implications (when you approve a fix — still not UI-5)

| Layer | Need |
|-------|------|
| **Backend (preferred)** | `GET /analytics/workspaces/{id}/trends` returning waves: `{ calculatedAt, averageHealthScore, sampleSize }` (+ optional per-project deltas for drivers). Aggregation = existing mean. |
| **Frontend-only workaround** | Fan-out project trends and average client-side — **OK for demos (7 projects), unacceptable for 100** (N API calls). Not recommended as the product path. |
| **Home UI** | Replace multi-series project lines with aggregate line + ranked drivers; keep ECharts as the engine. |

This is a **UI-4 / Home chart correction**, not the full UI-5 SVG migration.

---

## 6. Alternatives if you reject historical averaging

If product later forbids reconstructing historical averages from snapshots:

| Option | Valid? | Notes |
|--------|--------|-------|
| Keep only **current** Average health KPI (no line) | Yes | Honest; loses evolution story |
| Wait for a **stored** workspace trend table written at recalculate time | Yes | Strongest long-term; small backend write on recalculate |
| Multi-select project compare only | Yes | Scales only with explicit selection; doesn’t answer “overall Health” |

Given current data + existing `averageHealthScore` definition, **historical mean of snapshot Health is the best valid answer** to “How is overall project health evolving?”

---

## 7. Decision asked

Approve:

1. **Aggregate Health trend** (temporal extension of existing Average health KPI) as Home default line, **and**
2. **Ranked Health Δ drivers** as the companion viz, **and**
3. Prefer a **workspace trends API** (not client fan-out), **and**
4. Defer multi-select project compare,

then implement as a **Home chart correction** (still before UI-5)?

**Stopped here — no code changes in this step · UI-5 not started.**
