import { Params } from '@angular/router';
import {
  DEFAULT_EXPLORER_COLUMNS,
  DEFAULT_UPCOMING_DEADLINE_DAYS,
  ExplorerColumnId,
  ExplorerFilters,
  ExplorerGroupBy,
  ExplorerProjectRow,
  ExplorerSortKey,
  ExplorerSortSpec,
  ExplorerViewState,
  defaultExplorerViewState,
  emptyExplorerFilters,
} from '../../core/models/explorer.model';

const SORT_KEYS: readonly ExplorerSortKey[] = [
  'name',
  'status',
  'progress',
  'healthScore',
  'riskScore',
  'attentionScore',
  'delayed',
  'overdueWorkPackageCount',
  'endDate',
];

const GROUP_BY: readonly ExplorerGroupBy[] = [
  'none',
  'healthBand',
  'riskBand',
  'needsAttentionBand',
  'status',
  'delayed',
  'portfolio',
];

const COLUMNS: readonly ExplorerColumnId[] = [
  'name',
  'status',
  'progress',
  'expectedProgress',
  'progressGap',
  'healthScore',
  'riskScore',
  'attentionScore',
  'delayed',
  'overdueWorkPackageCount',
  'endDate',
  'nextDeadline',
  'projectAdmin',
  'portfolioNames',
  'recommendations',
];

function parseNumber(value: string | null): number | null {
  if (value == null || value === '') {
    return null;
  }
  const n = Number(value);
  return Number.isFinite(n) ? n : null;
}

function parseBool(value: string | null): boolean {
  return value === '1' || value === 'true';
}

/**
 * Drill-down presets for KPI "View all" actions (M11A context preservation).
 */
export type ExplorerDrillPreset =
  | 'critical'
  | 'delayed'
  | 'needsAttention'
  | 'hasOverdueWp'
  | 'upcoming'
  | 'clearExceptions';

export function explorerStateFromParams(params: Params, fallbackWorkspaceId: string | null): ExplorerViewState {
  const base = defaultExplorerViewState(params['workspaceId'] ?? fallbackWorkspaceId);
  const filters: ExplorerFilters = {
    ...emptyExplorerFilters(params['workspaceId'] ?? fallbackWorkspaceId),
    portfolioId: params['portfolioId'] ?? null,
    search: params['q'] ?? '',
    statuses: params['status']
      ? String(params['status'])
          .split(',')
          .map((s) => s.trim())
          .filter(Boolean)
      : [],
    healthMin: parseNumber(params['healthMin'] ?? null),
    healthMax: parseNumber(params['healthMax'] ?? null),
    progressMin: parseNumber(params['progressMin'] ?? null),
    progressMax: parseNumber(params['progressMax'] ?? null),
    riskMin: parseNumber(params['riskMin'] ?? null),
    riskMax: parseNumber(params['riskMax'] ?? null),
    delayedOnly: parseBool(params['delayed'] ?? null),
    criticalOnly: parseBool(params['critical'] ?? null),
    needsAttentionOnly: parseBool(params['needsAttention'] ?? null),
    hasOverdueWp: parseBool(params['hasOverdueWp'] ?? null),
    upcomingDeadlineDays: parseNumber(params['upcomingDeadlineDays'] ?? null),
    hasRecommendation:
      params['hasRecommendation'] == null
        ? null
        : parseBool(String(params['hasRecommendation'])),
    projectAdmin: params['projectAdmin'] ?? '',
  };

  let sort: readonly ExplorerSortSpec[] = base.sort;
  if (params['sort']) {
    const parts = String(params['sort']).split(',');
    const parsed: ExplorerSortSpec[] = [];
    for (const part of parts) {
      const [keyRaw, dirRaw] = part.split(':');
      const key = keyRaw as ExplorerSortKey;
      if (SORT_KEYS.includes(key)) {
        parsed.push({
          key,
          direction: dirRaw === 'asc' ? 'asc' : 'desc',
        });
      }
    }
    if (parsed.length > 0) {
      sort = parsed;
    }
  }

  const groupByRaw = (params['groupBy'] as ExplorerGroupBy) ?? 'none';
  const groupBy = GROUP_BY.includes(groupByRaw) ? groupByRaw : 'none';

  let columns = [...DEFAULT_EXPLORER_COLUMNS];
  if (params['columns']) {
    const cols = String(params['columns'])
      .split(',')
      .filter((c): c is ExplorerColumnId => COLUMNS.includes(c as ExplorerColumnId));
    if (cols.length > 0) {
      columns = cols;
    }
  }

  const density = params['density'] === 'dense' ? 'dense' : 'comfortable';

  return { filters, sort, groupBy, columns, density };
}

export function explorerStateToParams(state: ExplorerViewState): Params {
  const f = state.filters;
  const params: Params = {};
  if (f.workspaceId) {
    params['workspaceId'] = f.workspaceId;
  }
  if (f.portfolioId) {
    params['portfolioId'] = f.portfolioId;
  }
  if (f.search) {
    params['q'] = f.search;
  }
  if (f.statuses.length) {
    params['status'] = f.statuses.join(',');
  }
  if (f.healthMin != null) {
    params['healthMin'] = f.healthMin;
  }
  if (f.healthMax != null) {
    params['healthMax'] = f.healthMax;
  }
  if (f.progressMin != null) {
    params['progressMin'] = f.progressMin;
  }
  if (f.progressMax != null) {
    params['progressMax'] = f.progressMax;
  }
  if (f.riskMin != null) {
    params['riskMin'] = f.riskMin;
  }
  if (f.riskMax != null) {
    params['riskMax'] = f.riskMax;
  }
  if (f.delayedOnly) {
    params['delayed'] = '1';
  }
  if (f.criticalOnly) {
    params['critical'] = '1';
  }
  if (f.needsAttentionOnly) {
    params['needsAttention'] = '1';
  }
  if (f.hasOverdueWp) {
    params['hasOverdueWp'] = '1';
  }
  if (f.upcomingDeadlineDays != null) {
    params['upcomingDeadlineDays'] = f.upcomingDeadlineDays;
  }
  if (f.hasRecommendation === true) {
    params['hasRecommendation'] = '1';
  }
  if (f.hasRecommendation === false) {
    params['hasRecommendation'] = '0';
  }
  if (f.projectAdmin) {
    params['projectAdmin'] = f.projectAdmin;
  }
  if (state.sort.length) {
    params['sort'] = state.sort.map((s) => `${s.key}:${s.direction}`).join(',');
  }
  if (state.groupBy !== 'none') {
    params['groupBy'] = state.groupBy;
  }
  if (state.columns.join(',') !== DEFAULT_EXPLORER_COLUMNS.join(',')) {
    params['columns'] = state.columns.join(',');
  }
  if (state.density === 'dense') {
    params['density'] = 'dense';
  }
  return params;
}

/** Build Explorer query params for a scoped drill-down (Home / Portfolio KPI actions). */
export function drillDownQuery(
  workspaceId: string,
  portfolioId: string | null,
  preset: ExplorerDrillPreset,
  upcomingDays: number = DEFAULT_UPCOMING_DEADLINE_DAYS
): Params {
  const base: Params = { workspaceId };
  if (portfolioId) {
    base['portfolioId'] = portfolioId;
  }
  switch (preset) {
    case 'critical':
      return { ...base, critical: '1', sort: 'healthScore:asc' };
    case 'delayed':
      return { ...base, delayed: '1', sort: 'endDate:asc' };
    case 'needsAttention':
      return { ...base, needsAttention: '1', sort: 'attentionScore:desc' };
    case 'hasOverdueWp':
      return { ...base, hasOverdueWp: '1', sort: 'overdueWorkPackageCount:desc' };
    case 'upcoming':
      return { ...base, upcomingDeadlineDays: upcomingDays, sort: 'endDate:asc' };
    case 'clearExceptions':
      return { ...base, sort: 'name:asc' };
    default:
      return base;
  }
}

function inRange(value: number | null, min: number | null, max: number | null): boolean {
  if (value == null) {
    return min == null && max == null;
  }
  if (min != null && value < min) {
    return false;
  }
  if (max != null && value > max) {
    return false;
  }
  return true;
}

/**
 * Upcoming deadline window uses nextDeadline (project finish, else open WP due).
 * Does not require the project status to be "closing" — that is a separate status field.
 */
function isUpcoming(row: ExplorerProjectRow, days: number, today: Date): boolean {
  const deadline = row.nextDeadline || row.endDate;
  if (!deadline) {
    return false;
  }
  const end = new Date(deadline + 'T00:00:00');
  if (Number.isNaN(end.getTime())) {
    return false;
  }
  const start = new Date(today);
  start.setHours(0, 0, 0, 0);
  const limit = new Date(start);
  limit.setDate(limit.getDate() + days);
  return end >= start && end <= limit;
}

function compareValues(a: unknown, b: unknown, direction: 'asc' | 'desc'): number {
  const mul = direction === 'asc' ? 1 : -1;
  if (a == null && b == null) {
    return 0;
  }
  if (a == null) {
    return 1;
  }
  if (b == null) {
    return -1;
  }
  if (typeof a === 'boolean' && typeof b === 'boolean') {
    return (Number(a) - Number(b)) * mul;
  }
  if (typeof a === 'number' && typeof b === 'number') {
    return (a - b) * mul;
  }
  return String(a).localeCompare(String(b), undefined, { sensitivity: 'base' }) * mul;
}

export function applyExplorerPipeline(
  rows: readonly ExplorerProjectRow[],
  state: ExplorerViewState
): {
  readonly filtered: ExplorerProjectRow[];
  readonly groups: { readonly key: string; readonly label: string; readonly rows: ExplorerProjectRow[] }[];
} {
  const f = state.filters;
  const today = new Date();
  let filtered = rows.filter((row) => {
    if (f.search) {
      const q = f.search.toLowerCase();
      if (!row.name.toLowerCase().includes(q)) {
        return false;
      }
    }
    if (f.statuses.length && (!row.status || !f.statuses.includes(row.status))) {
      return false;
    }
    if (!inRange(row.healthScore, f.healthMin, f.healthMax)) {
      return false;
    }
    if (!inRange(row.progress, f.progressMin, f.progressMax)) {
      return false;
    }
    if (!inRange(row.riskScore, f.riskMin, f.riskMax)) {
      return false;
    }
    if (f.delayedOnly && !row.delayed) {
      return false;
    }
    if (f.criticalOnly && !row.critical) {
      return false;
    }
    if (f.needsAttentionOnly && !row.needsAttention) {
      return false;
    }
    if (f.hasOverdueWp && row.overdueWorkPackageCount <= 0) {
      return false;
    }
    if (f.upcomingDeadlineDays != null && !isUpcoming(row, f.upcomingDeadlineDays, today)) {
      return false;
    }
    if (f.projectAdmin) {
      const q = f.projectAdmin.trim().toLowerCase();
      const admin = (row.projectAdmin ?? '').toLowerCase();
      if (!admin || !admin.includes(q)) {
        return false;
      }
    }
    return true;
  });

  const sorted = [...filtered].sort((a, b) => {
    for (const spec of state.sort) {
      const av = a[spec.key as keyof ExplorerProjectRow];
      const bv = b[spec.key as keyof ExplorerProjectRow];
      const cmp = compareValues(av, bv, spec.direction);
      if (cmp !== 0) {
        return cmp;
      }
    }
    return a.name.localeCompare(b.name);
  });

  if (state.groupBy === 'none') {
    return { filtered: sorted, groups: [{ key: 'all', label: 'All projects', rows: sorted }] };
  }

  const groupMap = new Map<string, ExplorerProjectRow[]>();
  for (const row of sorted) {
    const keys = groupKeysForRow(row, state.groupBy);
    for (const key of keys) {
      const list = groupMap.get(key) ?? [];
      list.push(row);
      groupMap.set(key, list);
    }
  }

  const groups = [...groupMap.entries()]
    .map(([key, groupRows]) => ({
      key,
      label: key,
      rows: groupRows,
    }))
    .sort((a, b) => a.label.localeCompare(b.label));

  return { filtered: sorted, groups };
}

function groupKeysForRow(row: ExplorerProjectRow, groupBy: ExplorerGroupBy): string[] {
  switch (groupBy) {
    case 'healthBand':
      if (row.healthScore == null) {
        return ['Unknown health'];
      }
      if (row.healthScore < 40) {
        return ['Critical health'];
      }
      if (row.healthScore < 70) {
        return ['Watch health'];
      }
      return ['Healthy'];
    case 'riskBand':
      if (row.riskScore == null) {
        return ['Unknown risk'];
      }
      if (row.riskScore >= 70) {
        return ['High risk'];
      }
      if (row.riskScore >= 40) {
        return ['Medium risk'];
      }
      return ['Low risk'];
    case 'needsAttentionBand':
      return [row.needsAttention ? 'Needs Attention' : 'Stable'];
    case 'status':
      return [row.status?.trim() || 'No status'];
    case 'delayed':
      return [row.delayed ? 'Delayed' : 'On track'];
    case 'portfolio':
      return row.portfolioNames.length ? [...row.portfolioNames] : ['No portfolio'];
    default:
      return ['All'];
  }
}
