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
} as const satisfies Record<string, ScoreGlossaryEntry>;
