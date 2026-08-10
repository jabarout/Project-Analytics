# RB-004 — Report generation or download failure

## Symptoms

- Report status FAILED / REPORT_004
- Download: file missing from storage

## Checks

1. Logs: report type/format/duration; path.
2. Disk space on report storage host.
3. `REPORT_STORAGE_PATH` exists and is writable by the process.
4. DB `report` row status and `file_path`.

## Remediation

1. Free disk; fix permissions; restart if path changed.
2. **Do not** “regenerate over” an old report id — reports are **immutable** historical artifacts. Generate a **new** report for current data.
3. If file lost but metadata remains: restore from backup (`scripts/restore-postgres.sh` with reports.tgz).

## Policy

Reports are immutable point-in-time files. Backups must include **Postgres metadata + report files**.
