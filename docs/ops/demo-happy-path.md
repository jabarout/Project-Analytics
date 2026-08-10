# Demo happy path (M13 quality gate)

Executable checklist for a trustworthy demo or buyer handoff.  
**Target duration:** under 30 minutes after infra is up.

---

## 0. Prerequisites

- [ ] Docker: Postgres + Redis up (`docker compose` in `docker/`)
- [ ] Repo-root `.env` has non-empty `OPENPROJECT_API_KEY` and sensible `OPENPROJECT_URL`
- [ ] Backend via **`./scripts/run-backend.sh`** (not bare `mvn spring-boot:run`)
- [ ] Frontend: `cd frontend && npm start` → UI reachable
- [ ] Log shows: `OpenProject credentials: API key configured`

---

## 1. Login

- [ ] Open UI → login with seed admin (`admin` / `Admin123!` in dev)
- [ ] No console/network auth errors

---

## 2. Connect & synchronize

- [ ] **Connections** → connect workspace with correct OpenProject **base URL**
- [ ] **Synchronize** → SUCCESS
- [ ] Projects appear in **Explorer** (or Home exception data non-empty after analytics)

**If fail:** see `README-XTENSUS.md` troubleshooting (key not loaded, 401, SSL, timeout).

---

## 3. Recalculate analytics (if scores empty)

- [ ] Home → **Recalculate** (or post-sync auto-recalc already ran)
- [ ] Health / Risk / Attention values present on projects
- [ ] Progress shows a number when work packages exist (not stuck on blank when WPs exist)

---

## 4. Home triage

- [ ] Exception KPIs clickable → Explorer with expected filters
- [ ] Charts render; segment click drills to Explorer
- [ ] Exception queue lists critical / delayed / Needs Attention projects
- [ ] Recommendations section loads or empty state (no crash)

---

## 5. Explorer

- [ ] Filter by Needs Attention / Critical / Delayed works
- [ ] Sort and group work
- [ ] Open a project row → Project Detail

---

## 6. Project Detail

- [ ] Health / Risk / Needs Attention scores + explanations
- [ ] Factor bars show after recalculation (not permanently empty)
- [ ] Completion / progress consistent with Explorer for same project
- [ ] Overdue WP table / assignee bottlenecks load without error

---

## 7. Portfolio

- [ ] Create or open a portfolio; members visible
- [ ] Analytics tab: summary, exceptions, progress quality, member table
- [ ] Drill to Explorer scoped to portfolio

---

## 8. Reports & recommendations

- [ ] Generate a workspace KPI report (or open Reports with scope prefilled)
- [ ] Recommendations list for workspace or project without API error

---

## 9. Known acceptable limits (not failures)

See **Known limitations** in `13_Project_State.md` §14 / below. Do not treat as demo blockers:

- Budget variance often null (no spent budget in local model)
- Expected progress / gap null when project start/end dates missing
- API key still temporary until M14 OAuth
- Access grants UI incomplete until M15

---

## Sign-off

| Role | Name | Date | Pass? |
|------|------|------|-------|
| Automated quality gate | CI / local `mvn test` + frontend build | 2026-08-10 | **Pass** |
| Doc pack | Happy path + known limitations + Xtensus | 2026-08-10 | **Pass** |
| Company manual walkthrough | (optional re-confirm on their OP) | | |

**M13 closed:** automated critical path green; handoff docs complete; no known P0 on automated paths.  
**Company testers:** follow this checklist on their OpenProject; report any P0 against `known-limitations.md` first (may be accepted limits).
