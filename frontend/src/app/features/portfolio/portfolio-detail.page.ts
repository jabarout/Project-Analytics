import { DatePipe } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { PortfolioApiService } from '../../core/services/portfolio-api.service';
import { AnalyticsApiService } from '../../core/services/analytics-api.service';
import { RecommendationApiService } from '../../core/services/recommendation-api.service';
import { PortfolioDetail, PortfolioProjectSummary } from '../../core/models/portfolio.model';
import { ScopeDashboard } from '../../core/models/analytics.model';
import { RecommendationBundle } from '../../core/models/recommendation.model';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state.component';
import { KpiCardComponent } from '../../shared/components/dashboard/kpi-card.component';
import { InsightListComponent } from '../../shared/components/dashboard/insight-list.component';
import { AttentionTableComponent } from '../../shared/components/dashboard/attention-table.component';
import { RecommendationListComponent } from '../../shared/components/dashboard/recommendation-list.component';
import { ProjectMembershipPickerComponent } from '../../shared/components/portfolio/project-membership-picker.component';
import { ProjectTableComponent } from '../../shared/components/explorer/project-table.component';
import { drillDownQuery, ExplorerDrillPreset } from '../../shared/analytics/explorer-query';
import {
  formatCountWithPercent,
  healthBand,
} from '../../shared/analytics/analytics-thresholds';
import { BarChartComponent, BarChartDatum } from '../../shared/components/dashboard/bar-chart.component';
import {
  delayedVsOnTrack,
  healthDistribution,
  needsAttentionSplit,
  progressDistribution,
  progressGapSplit,
  riskDistribution,
} from '../../shared/analytics/distribution';
import {
  ExplorerProjectRow,
  PORTFOLIO_MEMBER_ANALYTICS_COLUMNS,
} from '../../core/models/explorer.model';
import { SCORE_GLOSSARY } from '../../shared/analytics/score-glossary';

/**
 * Portfolio analytical deep-dive + membership management.
 * Home stays lean triage; this page carries schedule/progress quality detail.
 */
@Component({
  selector: 'app-portfolio-detail-page',
  standalone: true,
  imports: [
    DatePipe,
    RouterLink,
    LoadingSpinnerComponent,
    EmptyStateComponent,
    KpiCardComponent,
    InsightListComponent,
    AttentionTableComponent,
    RecommendationListComponent,
    ProjectMembershipPickerComponent,
    ProjectTableComponent,
    BarChartComponent,
  ],
  templateUrl: './portfolio-detail.page.html',
  styleUrl: './portfolio-detail.page.scss',
})
export class PortfolioDetailPage implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly portfolioApi = inject(PortfolioApiService);
  private readonly analyticsApi = inject(AnalyticsApiService);
  private readonly recommendationApi = inject(RecommendationApiService);

  readonly glossary = SCORE_GLOSSARY;
  readonly memberColumns = PORTFOLIO_MEMBER_ANALYTICS_COLUMNS;
  readonly loading = signal(true);
  readonly adding = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly message = signal<string | null>(null);
  readonly detail = signal<PortfolioDetail | null>(null);
  readonly dashboard = signal<ScopeDashboard | null>(null);
  readonly recommendations = signal<RecommendationBundle | null>(null);
  readonly workspaceProjects = signal<readonly PortfolioProjectSummary[]>([]);
  readonly explorerRows = signal<ExplorerProjectRow[]>([]);
  readonly workspaceExplorerRows = signal<ExplorerProjectRow[]>([]);
  readonly selectedProjectIds = signal<string[]>([]);
  readonly activeTab = signal<'overview' | 'membership'>('overview');

  private portfolioId: string | null = null;

  readonly needsAttentionDisplay = computed(() => {
    const board = this.dashboard();
    if (!board) {
      return '—';
    }
    return formatCountWithPercent(board.kpis.highAttentionProjects, board.kpis.totalProjects);
  });

  readonly healthSummaryBand = computed(() => healthBand(this.dashboard()?.kpis.averageHealthScore));

  /** Members sorted by attention (desc) for the intelligence table. */
  readonly memberRows = computed(() => {
    return [...this.explorerRows()].sort((a, b) => {
      const aa = a.attentionScore ?? -1;
      const bb = b.attentionScore ?? -1;
      return bb - aa;
    });
  });

  readonly healthChart = computed(() => healthDistribution(this.explorerRows()));
  readonly riskChart = computed(() => riskDistribution(this.explorerRows()));
  readonly progressGapChart = computed(() => progressGapSplit(this.explorerRows()));
  readonly progressChart = computed(() => progressDistribution(this.explorerRows()));
  readonly delayedChart = computed(() => delayedVsOnTrack(this.explorerRows()));
  readonly needsChart = computed(() => needsAttentionSplit(this.explorerRows()));

  /** True when at least one member has a computable progress gap (has schedule dates). */
  readonly hasScheduleSignal = computed(() =>
    this.explorerRows().some((r) => r.progressGap != null)
  );

  /** Portfolio-level actual vs expected — only when both averages exist. */
  readonly progressCompareChart = computed(() => {
    const k = this.dashboard()?.kpis;
    if (!k || k.averageCompletion == null || k.averageExpectedProgress == null) {
      return [];
    }
    return [
      { label: 'Avg actual', value: k.averageCompletion, color: '#0f766e' },
      { label: 'Avg expected', value: k.averageExpectedProgress, color: '#1d4ed8' },
    ];
  });

  readonly availableProjects = computed(() => {
    const members = new Set((this.detail()?.projects ?? []).map((p) => p.id));
    return this.workspaceProjects().filter((p) => !members.has(p.id));
  });

  private readonly rowById = computed(() => {
    const map = new Map<string, ExplorerProjectRow>();
    for (const row of this.explorerRows()) {
      map.set(row.projectId, row);
    }
    return map;
  });

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      this.errorMessage.set('Missing portfolio id.');
      this.loading.set(false);
      return;
    }
    this.portfolioId = id;
    this.load(id);
  }

  /** Difference in percentage points (not a % of a whole) — no % suffix. */
  formatGap(value: number | null | undefined): string {
    if (value == null) {
      return '—';
    }
    const sign = value > 0 ? '+' : '';
    return `${sign}${value} pts`;
  }

  formatPercent(value: number | null | undefined): string {
    if (value == null) {
      return '—';
    }
    return `${value}%`;
  }

  formatOverdueRatio(ratio: number | null | undefined): string {
    if (ratio == null) {
      return '—';
    }
    return `${(ratio * 100).toFixed(0)}%`;
  }

  memberMetric(projectId: string, field: 'progress' | 'gap' | 'health' | 'risk'): string {
    const row = this.rowById().get(projectId);
    if (!row) {
      return '—';
    }
    switch (field) {
      case 'progress':
        return row.progress != null ? `${row.progress}%` : '—';
      case 'gap':
        return this.formatGap(row.progressGap);
      case 'health':
        return row.healthScore != null ? String(row.healthScore) : '—';
      case 'risk':
        return row.riskScore != null ? String(row.riskScore) : '—';
    }
  }

  onSelectionChange(ids: string[]): void {
    this.selectedProjectIds.set(ids);
  }

  addSelected(): void {
    const portfolioId = this.portfolioId;
    const projectIds = this.selectedProjectIds();
    if (!portfolioId || projectIds.length === 0) {
      return;
    }
    this.message.set(null);
    this.adding.set(true);
    this.portfolioApi.addProjectsBulk(portfolioId, projectIds).subscribe({
      next: () => {
        this.adding.set(false);
        this.selectedProjectIds.set([]);
        this.message.set(
          `Added ${projectIds.length} project(s) to this portfolio (still owned by the workspace).`
        );
        this.load(portfolioId);
      },
      error: () => {
        this.adding.set(false);
        this.errorMessage.set('Unable to add selected projects to portfolio.');
      },
    });
  }

  removeProject(projectId: string): void {
    const portfolioId = this.portfolioId;
    if (!portfolioId) {
      return;
    }
    this.portfolioApi.removeProject(portfolioId, projectId).subscribe({
      next: () => {
        this.message.set('Removed from this portfolio. Project remains in the workspace.');
        this.load(portfolioId);
      },
      error: () => this.errorMessage.set('Unable to remove project from portfolio.'),
    });
  }

  deletePortfolio(): void {
    const portfolioId = this.portfolioId;
    if (!portfolioId) {
      return;
    }
    if (!confirm('Delete this portfolio collection? Projects stay in the workspace.')) {
      return;
    }
    this.portfolioApi.deletePortfolio(portfolioId).subscribe({
      next: () => this.router.navigate(['/portfolios']),
      error: () => this.errorMessage.set('Unable to delete portfolio.'),
    });
  }

  openExplorer(preset: ExplorerDrillPreset): void {
    const detail = this.detail();
    const portfolioId = this.portfolioId;
    if (!detail || !portfolioId) {
      return;
    }
    void this.router.navigate(['/explorer'], {
      queryParams: drillDownQuery(detail.workspaceId, portfolioId, preset),
    });
  }

  onChartSegment(segment: BarChartDatum): void {
    const detail = this.detail();
    const portfolioId = this.portfolioId;
    if (!detail || !portfolioId) {
      return;
    }
    if (segment.drill === 'critical' || segment.drill === 'delayed' || segment.drill === 'needsAttention') {
      this.openExplorer(segment.drill);
      return;
    }
    const params: Record<string, string | number> = {
      workspaceId: detail.workspaceId,
      portfolioId,
    };
    if (segment.healthMin != null) {
      params['healthMin'] = segment.healthMin;
    }
    if (segment.healthMax != null) {
      params['healthMax'] = segment.healthMax;
    }
    if (segment.progressMin != null) {
      params['progressMin'] = segment.progressMin;
    }
    if (segment.progressMax != null) {
      params['progressMax'] = segment.progressMax;
    }
    void this.router.navigate(['/explorer'], { queryParams: params });
  }

  private load(id: string): void {
    this.loading.set(true);
    this.errorMessage.set(null);
    this.portfolioApi.getPortfolio(id).subscribe({
      next: (detail) => {
        this.detail.set(detail);
        this.portfolioApi.listWorkspaceProjects(detail.workspaceId).subscribe({
          next: (projects) => this.workspaceProjects.set(projects),
          error: () => this.workspaceProjects.set([]),
        });
        this.analyticsApi.getExplorerProjects(detail.workspaceId, id).subscribe({
          next: (rows) => this.explorerRows.set(rows),
          error: () => this.explorerRows.set([]),
        });
        this.analyticsApi.getExplorerProjects(detail.workspaceId).subscribe({
          next: (rows) => this.workspaceExplorerRows.set(rows),
          error: () => this.workspaceExplorerRows.set([]),
        });
        this.analyticsApi.getPortfolioDashboard(id).subscribe({
          next: (dashboard) => {
            this.dashboard.set(dashboard);
            this.loading.set(false);
            this.recommendationApi.getPortfolioRecommendations(id).subscribe({
              next: (bundle) => this.recommendations.set(bundle),
              error: () => this.recommendations.set(null),
            });
          },
          error: () => {
            this.loading.set(false);
            this.errorMessage.set('Unable to load portfolio analytics dashboard.');
          },
        });
      },
      error: () => {
        this.loading.set(false);
        this.errorMessage.set('Portfolio not found or unavailable.');
      },
    });
  }
}
