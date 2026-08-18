/**
 * Generic KPI literacy copy aligned with the analytics scoring model (presentation only).
 * Does not invent factors beyond Health / Risk / Needs Attention construction.
 */

export interface ScoreGlossaryEntry {
  readonly title: string;
  readonly summary: string;
  readonly thresholds: string;
  readonly detail: string;
}

export const SCORE_GLOSSARY = {
  health: {
    title: 'Health',
    summary:
      'Summarizes project delivery condition from local schedule, progress, and overdue work signals.',
    thresholds: 'Critical when score is below 40. Higher is healthier (0–100).',
    detail:
      'Health combines schedule pressure, delivery/progress, and overdue work-package pressure using the analytics engine. Progress is WP completion when work packages exist (not the OpenProject project field alone). Open the project to see factor contributions for the exact mix on this project.',
  },
  risk: {
    title: 'Risk',
    summary: 'Summarizes elevated delivery risk from overdue work, schedule stress, and weak completion.',
    thresholds: 'Elevated risk typically from 40 upward; high concern near 70+ (0–100, higher = more risk).',
    detail:
      'Risk weights overdue work packages, schedule variance, and completion weakness. It is a synthesis score — use overdue counts and Project Detail for operational evidence.',
  },
  needsAttention: {
    title: 'Needs Attention',
    summary: 'Prioritization signal combining health pressure, risk, and overdue load.',
    thresholds: 'Projects with score ≥ 50 are treated as needing attention in dashboards and filters.',
    detail:
      'Needs Attention rises when health is weak, risk is elevated, and overdue work accumulates. On multi-project views we show how many projects cross the threshold (count and %), not an average of this score.',
  },
  actualProgress: {
    title: 'Actual progress',
    summary: 'How far delivery has progressed based on local work packages (completed ÷ total × 100).',
    thresholds: '0–100%. Uses OpenProject project progress only when no work packages are synced.',
    detail:
      'This is the canonical progress used across Explorer, Home, and scores. It does not require project start/end dates.',
  },
  expectedProgress: {
    title: 'Expected progress',
    summary: 'Where progress “should” be if delivery tracked calendar time between project start and end.',
    thresholds: 'Requires both start date and end date in OpenProject (end after start). Otherwise unavailable (—).',
    detail:
      'Computed as elapsed days ÷ total schedule days × 100. If start/end are missing in OpenProject, this value stays empty on purpose — set dates in OpenProject and Synchronize to enable it.',
  },
  progressGap: {
    title: 'Progress gap',
    summary: 'Actual progress minus expected progress. Negative means behind the calendar schedule.',
    thresholds: 'Only available when Expected progress can be calculated (needs start + end dates).',
    detail:
      'Example: actual 40% and expected 70% → gap −30 (behind). Without project dates, gap cannot be computed and shows —.',
  },
  scheduleVariance: {
    title: 'Schedule variance (days)',
    summary: 'Days relative to the project end date: positive = past the finish date; negative = days still remaining.',
    thresholds: 'Requires a project end date in OpenProject. Otherwise unavailable (—).',
    detail:
      'Does not invent a finish date. Set the project finish date in OpenProject and Synchronize to populate this KPI.',
  },
  overdueRatio: {
    title: 'Overdue ratio',
    summary: 'Share of work packages that are open and past their due date.',
    thresholds: 'Requires synchronized work packages. Shown as a percentage of total WPs.',
    detail: 'Uses local WP due dates and completion status. Independent of project start/end dates.',
  },
} as const satisfies Record<string, ScoreGlossaryEntry>;
