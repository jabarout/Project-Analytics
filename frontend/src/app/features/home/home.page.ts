import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { WorkspaceApiService } from '../../core/services/workspace-api.service';
import { AnalyticsApiService } from '../../core/services/analytics-api.service';
import { RecommendationApiService } from '../../core/services/recommendation-api.service';
import { Workspace } from '../../core/models/workspace.model';
import { ScopeDashboard } from '../../core/models/analytics.model';
import { RecommendationBundle } from '../../core/models/recommendation.model';
import { ExplorerProjectRow } from '../../core/models/explorer.model';
import { KpiCardComponent } from '../../shared/components/dashboard/kpi-card.component';
import { AttentionTableComponent } from '../../shared/components/dashboard/attention-table.component';
import { RecommendationListComponent } from '../../shared/components/dashboard/recommendation-list.component';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state.component';
import { ProjectTableComponent } from '../../shared/components/explorer/project-table.component';
import { BarChartComponent, BarChartDatum } from '../../shared/components/dashboard/bar-chart.component';
import { drillDownQuery } from '../../shared/analytics/explorer-query';
import {
  delayedVsOnTrack,
  healthDistribution,
  needsAttentionSplit,
  progressDistribution,
  recommendationSeverityBars,
} from '../../shared/analytics/distribution';
import {
  DEFAULT_UPCOMING_DEADLINE_DAYS,
  formatCountWithPercent,
} from '../../shared/analytics/analytics-thresholds';
import { SCORE_GLOSSARY } from '../../shared/analytics/score-glossary';

@Component({
  selector: 'app-home-page',
  standalone: true,
  imports: [
    RouterLink,
    KpiCardComponent,
    AttentionTableComponent,
    RecommendationListComponent,
    LoadingSpinnerComponent,
    EmptyStateComponent,
    ProjectTableComponent,
    BarChartComponent,
  ],
  templateUrl: './home.page.html',
  styleUrl: './home.page.scss',
})
export class HomePage implements OnInit {
  private readonly workspaceApi = inject(WorkspaceApiService);
  private readonly analyticsApi = inject(AnalyticsApiService);
  private readonly recommendationApi = inject(RecommendationApiService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly glossary = SCORE_GLOSSARY;

  readonly loading = signal(true);
  readonly recalculating = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly workspaces = signal<Workspace[]>([]);
  readonly selectedWorkspaceId = signal<string | null>(null);
  readonly dashboard = signal<ScopeDashboard | null>(null);
  readonly recommendations = signal<RecommendationBundle | null>(null);
  readonly explorerRows = signal<ExplorerProjectRow[]>([]);

  readonly needsAttentionDisplay = computed(() => {
    const board = this.dashboard();
    if (!board) {
      return '—';
    }
    return formatCountWithPercent(board.kpis.highAttentionProjects, board.kpis.totalProjects);
  });

  readonly exceptionQueue = computed(() => {
    const rows = this.explorerRows();
    return [...rows]
      .filter((r) => r.needsAttention || r.critical || r.delayed)
      .sort((a, b) => (b.attentionScore ?? 0) - (a.attentionScore ?? 0))
      .slice(0, 8);
  });

  readonly upcomingCount = computed(() => {
    const days = DEFAULT_UPCOMING_DEADLINE_DAYS;
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const limit = new Date(today);
    limit.setDate(limit.getDate() + days);
    return this.explorerRows().filter((r) => {
      if (r.delayed) {
        return false;
      }
      const deadline = r.nextDeadline || r.endDate;
      if (!deadline) {
        return false;
      }
      const end = new Date(deadline + 'T00:00:00');
      if (Number.isNaN(end.getTime())) {
        return false;
      }
      return end >= today && end <= limit;
    }).length;
  });

  readonly overdueWpTotal = computed(() =>
    this.explorerRows().reduce((sum, r) => sum + (r.overdueWorkPackageCount || 0), 0)
  );

  readonly healthChart = computed(() => healthDistribution(this.explorerRows()));
  readonly progressChart = computed(() => progressDistribution(this.explorerRows()));
  readonly delayedChart = computed(() => delayedVsOnTrack(this.explorerRows()));
  readonly needsChart = computed(() => needsAttentionSplit(this.explorerRows()));
  readonly recoChart = computed(() =>
    recommendationSeverityBars(this.recommendations()?.recommendations ?? [])
  );

  readonly queueColumns = [
    'name',
    'healthScore',
    'riskScore',
    'attentionScore',
    'progress',
    'delayed',
  ] as const;

  ngOnInit(): void {
    const queryWorkspaceId = this.route.snapshot.queryParamMap.get('workspaceId');
    this.workspaceApi.listWorkspaces().subscribe({
      next: (items) => {
        this.workspaces.set(items);
        this.loading.set(false);
        if (items.length === 0) {
          return;
        }
        const preferred =
          queryWorkspaceId && items.some((w) => w.id === queryWorkspaceId)
            ? queryWorkspaceId
            : items[0].id;
        this.selectWorkspace(preferred);
      },
      error: () => {
        this.loading.set(false);
        this.errorMessage.set('Unable to load workspaces.');
      },
    });
  }

  selectWorkspace(workspaceId: string): void {
    this.selectedWorkspaceId.set(workspaceId);
    this.dashboard.set(null);
    this.recommendations.set(null);
    this.explorerRows.set([]);
    this.errorMessage.set(null);
    this.loading.set(true);
    this.analyticsApi.getWorkspaceDashboard(workspaceId).subscribe({
      next: (board) => {
        this.dashboard.set(board);
        this.loading.set(false);
        this.recommendationApi.getWorkspaceRecommendations(workspaceId).subscribe({
          next: (bundle) => this.recommendations.set(bundle),
          error: () => this.recommendations.set(null),
        });
      },
      error: () => {
        this.loading.set(false);
        this.errorMessage.set(
          'Workspace dashboard unavailable. Synchronize the workspace and recalculate analytics if needed.'
        );
      },
    });
    this.analyticsApi.getExplorerProjects(workspaceId).subscribe({
      next: (rows) => this.explorerRows.set(rows),
      error: () => this.explorerRows.set([]),
    });
  }

  openExplorer(
    preset: 'critical' | 'delayed' | 'needsAttention' | 'hasOverdueWp' | 'upcoming' | 'clearExceptions'
  ): void {
    const workspaceId = this.selectedWorkspaceId();
    if (!workspaceId) {
      return;
    }
    void this.router.navigate(['/explorer'], {
      queryParams: drillDownQuery(workspaceId, null, preset),
    });
  }

  onChartSegment(segment: BarChartDatum): void {
    const workspaceId = this.selectedWorkspaceId();
    if (!workspaceId) {
      return;
    }
    if (segment.drill === 'critical' || segment.drill === 'delayed' || segment.drill === 'needsAttention') {
      this.openExplorer(segment.drill);
      return;
    }
    if (segment.label === 'Critical' || segment.label === 'High') {
      this.openExplorer('needsAttention');
      return;
    }
    const params: Record<string, string | number> = { workspaceId };
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

  generateReport(): void {
    const workspaceId = this.selectedWorkspaceId();
    void this.router.navigate(['/reports'], {
      queryParams: workspaceId
        ? { reportType: 'KPI', scopeType: 'WORKSPACE', scopeId: workspaceId }
        : {},
    });
  }

  recalculate(): void {
    const workspaceId = this.selectedWorkspaceId();
    if (!workspaceId || this.recalculating()) {
      return;
    }
    this.recalculating.set(true);
    this.loading.set(true);
    this.errorMessage.set(null);
    this.analyticsApi.recalculateWorkspace(workspaceId).subscribe({
      next: () => {
        this.recalculating.set(false);
        this.selectWorkspace(workspaceId);
      },
      error: () => {
        this.recalculating.set(false);
        this.loading.set(false);
        this.errorMessage.set('Analytics recalculation failed.');
      },
    });
  }
}
