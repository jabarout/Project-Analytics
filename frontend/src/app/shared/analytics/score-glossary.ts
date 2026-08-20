/**
 * Generic KPI literacy copy aligned with the analytics scoring model (presentation only).
 * Under-title = short human meaning; thresholds/detail = precise hover/info copy.
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
      'A 0–100 score of how healthy delivery looks. Higher means stronger delivery health; lower means more delivery concerns.',
    thresholds: 'Critical when Health is below 40. Higher is healthier (0–100 score, not a %).',
    detail:
      'Health is calculated from three signals: (1) schedule alignment — whether Progress keeps up with the project start→end timeline in OpenProject, (2) work-package completion — completed ÷ total work packages (or OpenProject project progress if there are no WPs), (3) overdue pressure — open work packages past their due date (or a past project finish date if there are no WPs). To see these three factors for one project: open it from Explorer and scroll to Health factors.',
  },
  risk: {
    title: 'Risk',
    summary:
      'A 0–100 score of delivery risk. Higher means more risk; lower means less operational delivery risk.',
    thresholds: 'Higher Risk is worse (0–100 score, not a %). Elevated concern often appears from the mid-range upward.',
    detail:
      'Risk is calculated from three separate signals: (1) the share of open work packages past their due date, (2) whether the project finish date is already past while not archived, (3) how much work is still incomplete (low Progress) — incomplete work is not the same as overdue. Average Risk on Home is the mean of each project’s Risk score.',
  },
  needsAttention: {
    title: 'Needs Attention',
    summary:
      'A 0–100 priority score that rises when a project needs closer management attention. Higher means more urgent.',
    thresholds: 'Projects with Needs Attention score ≥ 50 are counted on Home and used in filters.',
    detail:
      'Needs Attention is calculated from: (1) how weak Health is, (2) how high Risk is, and (3) whether there are overdue work packages or a past project finish date. The Home count is how many projects reach Attention ≥ 50. To see which pressures matter most for one project: open it from Explorer and scroll to Needs Attention factors.',
  },
  actualProgress: {
    title: 'Progress',
    summary: 'How much of the project’s work is completed, based on synchronized work packages.',
    thresholds: '0–100%. Uses OpenProject project progress only when no work packages are synced.',
    detail:
      'When work packages exist: completed work packages ÷ total work packages × 100. If a project has no work packages, falls back to OpenProject’s project progress field if set; otherwise 0. Average Progress on Home averages those project values.',
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
  healthBands: {
    title: 'Health bands',
    summary: 'How projects are grouped by Health score on Home.',
    thresholds: 'Critical: below 40 · Watch: 40–69 · Healthy: 70–100 · Unknown: not scored yet.',
    detail:
      'These bands are used by the Health distribution chart. “At Risk” may appear as a project Health status label for scores 40–59, but on this chart those projects sit in Watch.',
  },
  exceptionQueue: {
    title: 'Exception queue',
    summary:
      'Projects flagged because they need attention, are in Critical Health, and/or have overdue work packages.',
    thresholds: 'Included if Attention ≥ 50, Health < 40, or at least one open overdue work package. Top 8 by Attention.',
    detail:
      'Sorted by Needs Attention score (highest first), limited to 8 rows. Open a project from Explorer to inspect Health, Risk, and Needs Attention factors.',
  },
} as const satisfies Record<string, ScoreGlossaryEntry>;
