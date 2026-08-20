# Demo happy path (buyer walkthrough)

Executable checklist for a trustworthy demo or buyer handoff.  
**Target duration:** under 30 minutes after infra is up.

Related ops: `n5.1-prod-compose.md`, `security-checklist.md`, `known-limitations.md`.

---

## 0. Prerequisites

**Local demo**

- [ ] Docker: Postgres + Redis up (`docker compose` in `docker/`)
- [ ] Repo-root `.env` has OpenProject URL + credentials path you will use (API key and/or OAuth)
- [ ] Backend via **`./scripts/run-backend.sh`**
- [ ] Frontend: `cd frontend && npm start` → `http://localhost:4200`

**Optional prod-style stack:** see `n5.1-prod-compose.md` (isolated ports 8089 / 8081).

---

## 1. Login & chrome

- [ ] Open UI → login (local seed: `admin` / `Admin123!` — **change before real deploy**)
- [ ] Theme works (Settings or existing preference); language shows **English only**
- [ ] Footer links: **Privacy**, **Terms of use**, **Contact** (Contact email opens mail client)

---

## 2. Connect & synchronize

- [ ] **Connections** → connect workspace (API key or OAuth) with correct OpenProject base URL
- [ ] **Synchronize** → SUCCESS
- [ ] Projects appear in **Explorer** (or Home has triage data after analytics)

**If fail:** `README-XTENSUS.md` / `known-limitations.md` (key, 401, SSL, timeout).

---

## 3. Recalculate (if scores empty)

- [ ] Home → **Recalculate** (or confirm post-sync recalc already ran)
- [ ] Health / Risk / Needs Attention present on projects
- [ ] Progress shows a value when work packages exist

---

## 4. Home (Synthesis-first)

Sticky **On this page** order:

1. Synthesis → 2. Overview → 3. Visual analytics → 4. Exception queue → 5. Recommendations

- [ ] **Synthesis** first: Average progress / health / risk (hover shows detailed Health & Risk copy)
- [ ] **Overview** counts; Critical / Needs Attention hovers are short (point up to Synthesis)
- [ ] **Visual analytics**: Average Health line; distribution charts with fused legends (no overlapping text)
- [ ] KPI / chart drill → Explorer with expected filters
- [ ] **Exception queue** lists flagged projects; open one → Project Detail
- [ ] **Recommendations** load or show a clean empty state (no crash)

---

## 5. Explorer

- [ ] Filters: Needs Attention / Critical / overdue WPs work
- [ ] Sort works; open a project row → Project Detail

---

## 6. Project Detail

- [ ] Top KPIs: Health, Risk, Needs Attention, Actual progress (detailed hovers)
- [ ] Work package delivery KPIs; charts: **Work package status** + **Completion** (no “Completed vs remaining”)
- [ ] Health / Risk / Needs Attention **factor** charts: short axis labels; hover shows full factor text
- [ ] Score trends chart; clear gap before **Overdue work packages** table
- [ ] Assignee bottlenecks load without error

---

## 7. Portfolio

- [ ] Create or open a portfolio; Membership can add projects
- [ ] Analytics order: **Progress & delivery quality** → **Overview** → **Visual analytics**
- [ ] Charts present: Health distribution, Progress bands, Overdue split (legends fused; **no** Risk distribution / Needs Attention split charts)
- [ ] Copy says **projects** (not “members”) in user-facing analytics text
- [ ] Drill to Explorer scoped to the portfolio

---

## 8. Reports & settings

- [ ] Generate or open a workspace report without API error
- [ ] **Settings**: email stays inside the profile card; Language = English only; save theme

---

## 9. Known acceptable limits (not demo failures)

See `docs/ops/known-limitations.md` and Project State. Examples:

- Expected progress / gap unavailable without project start/end dates
- Budget variance often null without spend data
- Community OpenProject date/field quirks as documented

---

## Sign-off

| Role | Name | Date | Pass? |
|------|------|------|-------|
| Demo operator | | | |
| Buyer / reviewer | | | |
