export interface PortfolioSummary {
  readonly id: string;
  readonly workspaceId: string;
  readonly name: string;
  readonly description: string | null;
  readonly healthScore: number | null;
  readonly attentionScore: number | null;
  readonly totalProjects: number;
  readonly activeProjects: number;
}

export interface PortfolioProjectSummary {
  readonly id: string;
  readonly openProjectId: number;
  readonly name: string;
  readonly status: string | null;
  readonly budget: number | null;
  readonly progress: number | null;
  readonly startDate: string | null;
  readonly endDate: string | null;
  readonly synchronizedAt: string | null;
}

export interface PortfolioDetail extends PortfolioSummary {
  readonly projects: readonly PortfolioProjectSummary[];
}

export interface PortfolioKpis {
  readonly portfolioId: string;
  readonly totalProjects: number;
  readonly activeProjects: number;
  readonly archivedProjects: number;
  readonly overdueProjects: number;
  readonly totalWorkPackages: number;
  readonly overdueWorkPackages: number;
  readonly totalBudget: number;
  readonly averageProgress: number | null;
  readonly healthScore: number | null;
  readonly attentionScore: number | null;
  readonly lastSynchronizedAt: string | null;
}

export interface PortfolioDashboard {
  readonly portfolioId: string;
  readonly portfolioName: string;
  readonly workspaceId: string;
  readonly kpis: PortfolioKpis;
  readonly executiveSummary: string;
  readonly activeProjects: readonly PortfolioProjectSummary[];
  readonly overdueProjects: readonly PortfolioProjectSummary[];
  readonly operationalInsights: readonly string[];
}

export interface CreatePortfolioRequest {
  readonly workspaceId: string;
  readonly name: string;
  readonly description?: string | null;
  /** Optional initial members (many-to-many membership only). */
  readonly projectIds?: readonly string[];
}

export interface UpdatePortfolioRequest {
  readonly name: string;
  readonly description?: string | null;
}
