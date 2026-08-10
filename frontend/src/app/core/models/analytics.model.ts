export interface ScoredMetric {
  readonly score: number;
  readonly label: string;
  readonly explanation: string;
  readonly factors: readonly {
    readonly code: string;
    readonly description: string;
    readonly contribution: number;
    readonly rawValue: number | null;
  }[];
}

export interface ProjectAnalytics {
  readonly projectId: string;
  readonly projectName: string;
  readonly health: ScoredMetric;
  readonly risk: ScoredMetric;
  readonly attention: ScoredMetric;
  /** Canonical actual progress % (WP-based when WPs exist). */
  readonly completionPercentage: number | null;
  /** Schedule-based expected progress %; null when dates incomplete. */
  readonly expectedProgress: number | null;
  /** actual − expected; negative = behind schedule. */
  readonly progressGap: number | null;
  /** overdue / total WPs (0–1); null when no WPs. */
  readonly overdueRatio: number | null;
  readonly avgOverdueAgeDays: number | null;
  readonly maxOverdueAgeDays: number | null;
  /** Days past end date (positive = late); null if no end date. */
  readonly scheduleVariance: number | null;
  readonly budgetVariance: number | null;
  readonly calculatedAt: string;
}

export interface ScopeAnalyticsKpis {
  readonly scopeId: string;
  readonly scopeType: string;
  readonly totalProjects: number;
  readonly activeProjects: number;
  readonly criticalProjects: number;
  readonly highAttentionProjects: number;
  readonly overdueProjects: number;
  readonly totalWorkPackages: number;
  readonly averageHealthScore: number | null;
  readonly averageRiskScore: number | null;
  readonly averageAttentionScore: number | null;
  /** Average of stored completion % (canonical actual progress). */
  readonly averageCompletion: number | null;
  readonly averageExpectedProgress: number | null;
  readonly averageProgressGap: number | null;
  readonly projectsBehindSchedule: number;
  readonly averageOverdueRatio: number | null;
  readonly projectsWithOverdueWorkPackages: number;
  readonly totalBudget: number;
  readonly lastCalculatedAt: string | null;
}

export interface ProjectAttentionSummary {
  readonly projectId: string;
  readonly projectName: string;
  readonly status: string | null;
  readonly healthScore: number | null;
  readonly healthStatus: string | null;
  readonly riskScore: number | null;
  readonly riskLevel: string | null;
  readonly attentionScore: number | null;
  readonly attentionLabel: string | null;
}

export interface ScopeDashboard {
  readonly scopeId: string;
  readonly scopeType: string;
  readonly scopeName: string;
  readonly workspaceId: string;
  readonly kpis: ScopeAnalyticsKpis;
  readonly executiveSummary: string;
  readonly topAttentionProjects: readonly ProjectAttentionSummary[];
  readonly criticalHealthProjects: readonly ProjectAttentionSummary[];
  readonly insights: readonly string[];
}

export interface ProjectDashboard {
  readonly projectId: string;
  readonly projectName: string;
  readonly workspaceId: string;
  readonly portfolioId: string;
  readonly status: string | null;
  readonly progress: number | null;
  readonly budget: number | null;
  readonly startDate: string | null;
  readonly endDate: string | null;
  readonly synchronizedAt: string | null;
  readonly analytics: ProjectAnalytics;
  readonly trends: readonly {
    readonly calculatedAt: string;
    readonly healthScore: number;
    readonly riskScore: number;
    readonly attentionScore: number;
    readonly completionPercentage: number | null;
  }[];
}

export interface ProjectWorkPackageAnalytics {
  readonly projectId: string;
  readonly totalWorkPackages: number;
  readonly openWorkPackages: number;
  readonly completedWorkPackages: number;
  readonly overdueWorkPackages: number;
  readonly highPriorityOpen: number;
  readonly blockedWorkPackages: number;
  readonly inProgressWorkPackages: number;
  readonly statusDistribution: readonly { readonly status: string; readonly count: number }[];
  readonly overdueWorkPackagesList: readonly {
    readonly id: string;
    readonly subject: string;
    readonly status: string | null;
    readonly priority: string | null;
    readonly assignee: string | null;
    readonly dueDate: string | null;
  }[];
  readonly assigneeBottlenecks: readonly {
    readonly assignee: string;
    readonly openCount: number;
    readonly overdueCount: number;
    readonly totalCount: number;
  }[];
}
