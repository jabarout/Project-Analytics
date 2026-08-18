/**
 * Explorer read model and view state (M11A/B). Client filters only; scores from backend.
 */

export interface ExplorerProjectRow {
  readonly projectId: string;
  readonly workspaceId: string;
  readonly name: string;
  readonly status: string | null;
  /** Canonical progress = analytics completion % when available. */
  readonly progress: number | null;
  readonly expectedProgress: number | null;
  readonly progressGap: number | null;
  readonly budget: number | null;
  readonly startDate: string | null;
  readonly endDate: string | null;
  readonly synchronizedAt: string | null;
  readonly healthScore: number | null;
  readonly healthStatus: string | null;
  readonly riskScore: number | null;
  readonly riskLevel: string | null;
  readonly attentionScore: number | null;
  readonly attentionLabel: string | null;
  readonly delayed: boolean;
  readonly critical: boolean;
  readonly needsAttention: boolean;
  readonly overdueWorkPackageCount: number;
  readonly overdueRatio: number | null;
  /** Days past end date (positive = late); null if no end date. */
  readonly scheduleVariance: number | null;
  readonly portfolioIds: readonly string[];
  readonly portfolioNames: readonly string[];
  /** OpenProject project admin name(s) from memberships. */
  readonly projectAdmin: string | null;
  /**
   * Effective deadline for upcoming filters:
   * project end date, else earliest open WP due date.
   */
  readonly nextDeadline: string | null;
  readonly nextDeadlineSource: 'project' | 'work_package' | null;
}

export type ExplorerSortKey =
  | 'name'
  | 'status'
  | 'progress'
  | 'healthScore'
  | 'riskScore'
  | 'attentionScore'
  | 'delayed'
  | 'overdueWorkPackageCount'
  | 'endDate';

export type ExplorerGroupBy =
  | 'none'
  | 'healthBand'
  | 'riskBand'
  | 'needsAttentionBand'
  | 'status'
  | 'delayed'
  | 'portfolio';

export type ExplorerColumnId =
  | 'name'
  | 'status'
  | 'progress'
  | 'expectedProgress'
  | 'progressGap'
  | 'healthScore'
  | 'riskScore'
  | 'attentionScore'
  | 'delayed'
  | 'overdueWorkPackageCount'
  | 'endDate'
  | 'nextDeadline'
  | 'projectAdmin'
  | 'portfolioNames'
  | 'recommendations';

/** Portfolio overview member table — Community-reliable columns only. */
export const PORTFOLIO_MEMBER_ANALYTICS_COLUMNS: readonly ExplorerColumnId[] = [
  'name',
  'healthScore',
  'riskScore',
  'attentionScore',
  'progress',
  'overdueWorkPackageCount',
  'projectAdmin',
];

export interface ExplorerSortSpec {
  readonly key: ExplorerSortKey;
  readonly direction: 'asc' | 'desc';
}

export interface ExplorerFilters {
  readonly workspaceId: string | null;
  readonly portfolioId: string | null;
  readonly search: string;
  readonly statuses: readonly string[];
  readonly healthMin: number | null;
  readonly healthMax: number | null;
  readonly progressMin: number | null;
  readonly progressMax: number | null;
  readonly riskMin: number | null;
  readonly riskMax: number | null;
  readonly delayedOnly: boolean;
  readonly criticalOnly: boolean;
  readonly needsAttentionOnly: boolean;
  readonly hasOverdueWp: boolean;
  readonly upcomingDeadlineDays: number | null;
  readonly hasRecommendation: boolean | null;
  /** Filter by project admin name (substring, case-insensitive). */
  readonly projectAdmin: string;
}

export interface ExplorerViewState {
  readonly filters: ExplorerFilters;
  readonly sort: readonly ExplorerSortSpec[];
  readonly groupBy: ExplorerGroupBy;
  readonly columns: readonly ExplorerColumnId[];
  readonly density: 'comfortable' | 'dense';
}

export interface SavedExplorerView {
  readonly id: string;
  readonly name: string;
  readonly state: ExplorerViewState;
  readonly createdAt: string;
  readonly isDefault?: boolean;
}

export const DEFAULT_UPCOMING_DEADLINE_DAYS = 14;

/** Default columns use Community-reliable fields (WP progress/overdue; not project end dates). */
export const DEFAULT_EXPLORER_COLUMNS: readonly ExplorerColumnId[] = [
  'name',
  'status',
  'progress',
  'healthScore',
  'riskScore',
  'attentionScore',
  'overdueWorkPackageCount',
  'nextDeadline',
  'projectAdmin',
  'portfolioNames',
];

export function emptyExplorerFilters(workspaceId: string | null = null): ExplorerFilters {
  return {
    workspaceId,
    portfolioId: null,
    search: '',
    statuses: [],
    healthMin: null,
    healthMax: null,
    progressMin: null,
    progressMax: null,
    riskMin: null,
    riskMax: null,
    delayedOnly: false,
    criticalOnly: false,
    needsAttentionOnly: false,
    hasOverdueWp: false,
    upcomingDeadlineDays: null,
    hasRecommendation: null,
    projectAdmin: '',
  };
}

export function defaultExplorerViewState(workspaceId: string | null = null): ExplorerViewState {
  return {
    filters: emptyExplorerFilters(workspaceId),
    sort: [{ key: 'attentionScore', direction: 'desc' }],
    groupBy: 'none',
    columns: [...DEFAULT_EXPLORER_COLUMNS],
    density: 'comfortable',
  };
}
