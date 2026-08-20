import { ExplorerProjectRow } from '../../core/models/explorer.model';
import { CRITICAL_HEALTH_MAX, DEFAULT_UPCOMING_DEADLINE_DAYS } from './analytics-thresholds';
import { ExplorerDrillPreset } from './explorer-query';

/** Shared segment shape for distribution helpers (formerly on legacy SVG bar-chart). */
export interface ChartSegmentDatum {
  readonly label: string;
  readonly value: number;
  readonly color?: string;
  readonly drill?: string;
  readonly healthMin?: number;
  readonly healthMax?: number;
  readonly progressMin?: number;
  readonly progressMax?: number;
}

export interface InteractiveChartSegment extends ChartSegmentDatum {
  readonly drill?: ExplorerDrillPreset | 'healthCritical' | 'healthWatch' | 'healthOk' | 'progressLow' | 'progressMid' | 'progressHigh';
  readonly healthMin?: number;
  readonly healthMax?: number;
  readonly progressMin?: number;
  readonly progressMax?: number;
}

export function healthDistribution(rows: readonly ExplorerProjectRow[]): InteractiveChartSegment[] {
  let critical = 0;
  let watch = 0;
  let healthy = 0;
  let unknown = 0;
  for (const r of rows) {
    if (r.healthScore == null) {
      unknown++;
    } else if (r.healthScore < CRITICAL_HEALTH_MAX) {
      critical++;
    } else if (r.healthScore < 70) {
      watch++;
    } else {
      healthy++;
    }
  }
  return [
    { label: 'Critical', value: critical, color: 'var(--pa-danger)', drill: 'critical', healthMax: 39.99 },
    { label: 'Watch', value: watch, color: 'var(--pa-warning)', drill: 'healthWatch', healthMin: 40, healthMax: 69.99 },
    { label: 'Healthy', value: healthy, color: 'var(--pa-success)', drill: 'healthOk', healthMin: 70 },
    ...(unknown
      ? [{ label: 'Unknown', value: unknown, color: 'var(--pa-chart-3)' } as InteractiveChartSegment]
      : []),
  ];
}

export function progressDistribution(rows: readonly ExplorerProjectRow[]): InteractiveChartSegment[] {
  let low = 0;
  let mid = 0;
  let high = 0;
  let unknown = 0;
  for (const r of rows) {
    if (r.progress == null) {
      unknown++;
    } else if (r.progress < 34) {
      low++;
    } else if (r.progress < 67) {
      mid++;
    } else {
      high++;
    }
  }
  // Greyscale bands — progress volume, not health semantics
  return [
    { label: '0–33% complete', value: low, color: 'var(--pa-chart-1)', progressMax: 33 },
    { label: '34–66% complete', value: mid, color: 'var(--pa-chart-2)', progressMin: 34, progressMax: 66 },
    { label: '67–100% complete', value: high, color: 'var(--pa-chart-3)', progressMin: 67 },
    ...(unknown
      ? [{ label: 'Unknown', value: unknown, color: 'var(--pa-chart-4)' } as InteractiveChartSegment]
      : []),
  ];
}

export function delayedVsOnTrack(rows: readonly ExplorerProjectRow[]): InteractiveChartSegment[] {
  let delayed = 0;
  let onTrack = 0;
  for (const r of rows) {
    if (r.delayed) {
      delayed++;
    } else {
      onTrack++;
    }
  }
  return [
    { label: 'Delayed', value: delayed, color: 'var(--pa-danger)', drill: 'delayed' },
    { label: 'On track', value: onTrack, color: 'var(--pa-success)' },
  ];
}

/** Community-friendly split: projects with overdue open WPs vs none (uses WP due dates). */
export function overdueWpProjectsSplit(rows: readonly ExplorerProjectRow[]): InteractiveChartSegment[] {
  let withOverdue = 0;
  let clean = 0;
  for (const r of rows) {
    if ((r.overdueWorkPackageCount ?? 0) > 0) {
      withOverdue++;
    } else {
      clean++;
    }
  }
  return [
    { label: 'Has overdue WPs', value: withOverdue, color: 'var(--pa-danger)', drill: 'hasOverdueWp' },
    { label: 'No overdue WPs', value: clean, color: 'var(--pa-viz-2)' },
  ];
}

export function needsAttentionSplit(rows: readonly ExplorerProjectRow[]): InteractiveChartSegment[] {
  let needs = 0;
  let stable = 0;
  for (const r of rows) {
    if (r.needsAttention) {
      needs++;
    } else {
      stable++;
    }
  }
  return [
    { label: 'Needs Attention', value: needs, color: 'var(--pa-warning)', drill: 'needsAttention' },
    { label: 'Stable', value: stable, color: 'var(--pa-success)' },
  ];
}

export function recommendationSeverityBars(
  items: readonly { severity?: string | null }[]
): InteractiveChartSegment[] {
  let critical = 0;
  let high = 0;
  let medium = 0;
  let low = 0;
  for (const item of items) {
    const s = (item.severity ?? '').toUpperCase();
    if (s.includes('CRITICAL')) {
      critical++;
    } else if (s.includes('HIGH')) {
      high++;
    } else if (s.includes('MEDIUM') || s.includes('WARN')) {
      medium++;
    } else {
      low++;
    }
  }
  return [
    { label: 'Critical', value: critical, color: 'var(--pa-danger)' },
    { label: 'High', value: high, color: 'var(--pa-warning)' },
    { label: 'Medium', value: medium, color: 'var(--pa-chart-2)' },
    { label: 'Other', value: low, color: 'var(--pa-chart-3)' },
  ];
}

/** Risk bands from stored risk scores (reuse Explorer rows; no new formulas). */
export function riskDistribution(rows: readonly ExplorerProjectRow[]): InteractiveChartSegment[] {
  let low = 0;
  let medium = 0;
  let high = 0;
  let critical = 0;
  let unknown = 0;
  for (const r of rows) {
    if (r.riskScore == null) {
      unknown++;
    } else if (r.riskScore >= 75) {
      critical++;
    } else if (r.riskScore >= 50) {
      high++;
    } else if (r.riskScore >= 25) {
      medium++;
    } else {
      low++;
    }
  }
  return [
    { label: 'Low', value: low, color: 'var(--pa-success)' },
    { label: 'Medium', value: medium, color: 'var(--pa-warning)' },
    { label: 'High', value: high, color: 'var(--pa-danger)' },
    { label: 'Critical', value: critical, color: 'var(--pa-danger)' },
    ...(unknown
      ? [{ label: 'Unknown', value: unknown, color: 'var(--pa-chart-3)' } as InteractiveChartSegment]
      : []),
  ];
}

/**
 * Schedule progress stance from stored progressGap (ProgressMetrics).
 * Behind: gap &lt; -5; on track: |gap| ≤ 5; ahead: gap &gt; 5.
 */
export function progressGapSplit(rows: readonly ExplorerProjectRow[]): InteractiveChartSegment[] {
  let behind = 0;
  let onTrack = 0;
  let ahead = 0;
  let unknown = 0;
  for (const r of rows) {
    if (r.progressGap == null) {
      unknown++;
    } else if (r.progressGap < -5) {
      behind++;
    } else if (r.progressGap > 5) {
      ahead++;
    } else {
      onTrack++;
    }
  }
  return [
    { label: 'Behind schedule', value: behind, color: 'var(--pa-danger)' },
    { label: 'On track', value: onTrack, color: 'var(--pa-success)' },
    { label: 'Ahead', value: ahead, color: 'var(--pa-text)' },
    ...(unknown
      ? [{ label: 'No schedule', value: unknown, color: 'var(--pa-chart-3)' } as InteractiveChartSegment]
      : []),
  ];
}

export { DEFAULT_UPCOMING_DEADLINE_DAYS };
