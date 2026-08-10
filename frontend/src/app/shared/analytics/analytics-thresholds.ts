/** Presentation thresholds aligned with backend scope KPIs (not client scoring). */

export const CRITICAL_HEALTH_MAX = 40;
export const NEEDS_ATTENTION_MIN = 50;
export const DEFAULT_UPCOMING_DEADLINE_DAYS = 14;

export type SeverityBand = 'neutral' | 'watch' | 'critical' | 'positive';

export function healthBand(score: number | null | undefined): SeverityBand {
  if (score == null) {
    return 'neutral';
  }
  if (score < CRITICAL_HEALTH_MAX) {
    return 'critical';
  }
  if (score < 70) {
    return 'watch';
  }
  return 'positive';
}

export function riskBand(score: number | null | undefined): SeverityBand {
  if (score == null) {
    return 'neutral';
  }
  if (score >= 70) {
    return 'critical';
  }
  if (score >= 40) {
    return 'watch';
  }
  return 'positive';
}

export function needsAttentionBand(score: number | null | undefined): SeverityBand {
  if (score == null) {
    return 'neutral';
  }
  if (score >= NEEDS_ATTENTION_MIN) {
    return 'critical';
  }
  if (score >= 30) {
    return 'watch';
  }
  return 'positive';
}

export function formatPercent(count: number, total: number): string {
  if (total <= 0) {
    return '0%';
  }
  return `${Math.round((count / total) * 100)}%`;
}

export function formatCountWithPercent(count: number, total: number): string {
  return `${count} (${formatPercent(count, total)})`;
}
