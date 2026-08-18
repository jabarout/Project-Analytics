# OpenProject eligibility spike (Community 17.7)

Date: 2026-08-18  
Purpose: Decide how Project Analytics determines whether an OpenProject identity may **connect/sync** a workspace (Hybrid M14).

## Method

- Used per-user API tokens (`apikey:<token>` Basic auth) for Admin, Alice, Bob.
- Bob temporarily reduced to **Member-only** (removed Project admin on Bravo) for the deny case.
- Endpoints: `GET /api/v3/users/me`, filtered `GET /api/v3/memberships`, `GET /api/v3/projects`.

## Results

| Identity | `users/me.admin` | Role titles on memberships | Projects visible | Eligible under v1 policy? |
|----------|------------------|----------------------------|------------------|---------------------------|
| OpenProject Admin (env key) | `true` | Member, Project admin | 7 (all) | **Yes** (`admin`) |
| Alice Nguyen | absent/`null` (not true) | Member, **Project admin** (Echo, Alpha) | 6 | **Yes** (Project admin) |
| Bob Okoro (Member-only) | absent/`null` | **Member only** | 6 | **No** |

## Conclusions for M14

1. **`/users/me` is reliable** for identity + global `admin` flag (treat only explicit `true` as admin).
2. **Membership role titles are reliable** for Project admin vs Member on Community.
3. **Do not require global Administrator** — Alice would be wrongly denied.
4. **Do not treat OAuth/API-key success alone as eligibility** — Bob authenticates but must be denied.
5. **v1 configured policy:** allow if `admin == true` **OR** any membership role title matches allow-list (default: `Project admin`, case-insensitive). Make allow-list configurable.
6. Sync catalog visibility follows the connected OP identity (Admin sees more projects than Alice/Bob) — document that PA shows what the token can see.
7. Rich role `permissions` arrays / capabilities API were not dependable on this instance — prefer role **titles** for v1.

## Note

Bob’s Project admin role was removed only for this spike; restore in OP if needed for demo data consistency.
