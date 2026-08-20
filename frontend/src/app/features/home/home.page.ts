import {
  AfterViewInit,
  Component,
  OnDestroy,
  OnInit,
  computed,
  inject,
  signal,
} from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { WorkspaceApiService } from '../../core/services/workspace-api.service';
import { AnalyticsApiService } from '../../core/services/analytics-api.service';
import { RecommendationApiService } from '../../core/services/recommendation-api.service';
import { Workspace } from '../../core/models/workspace.model';
import { ScopeDashboard, WorkspaceHealthTrend } from '../../core/models/analytics.model';
import { RecommendationBundle } from '../../core/models/recommendation.model';
import { ExplorerProjectRow } from '../../core/models/explorer.model';
import { KpiCardComponent } from '../../shared/components/dashboard/kpi-card.component';
import { AttentionTableComponent } from '../../shared/components/dashboard/attention-table.component';
import { RecommendationListComponent } from '../../shared/components/dashboard/recommendation-list.component';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state.component';
import { ProjectTableComponent } from '../../shared/components/explorer/project-table.component';
import { HealthDriversComponent } from '../../shared/components/dashboard/health-drivers.component';
import { PaLineChartComponent, PaLineSeries } from '../../shared/charts/pa-line-chart.component';
import { PaBarChartComponent, PaBarDatum } from '../../shared/charts/pa-bar-chart.component';
import { PaDonutChartComponent, PaDonutSlice } from '../../shared/charts/pa-donut-chart.component';
import { drillDownQuery } from '../../shared/analytics/explorer-query';
import {
  healthDistribution,
  needsAttentionSplit,
  progressDistribution,
} from '../../shared/analytics/distribution';
import {
  DEFAULT_UPCOMING_DEADLINE_DAYS,
  formatCountWithPercent,
} from '../../shared/analytics/analytics-thresholds';
import { SCORE_GLOSSARY } from '../../shared/analytics/score-glossary';
import { PaRevealDirective } from '../../shared/directives/pa-reveal.directive';

@Component({
  selector: 'app-home-page',
  standalone: true,
  imports: [
    PaRevealDirective,
    RouterLink,
    KpiCardComponent,
    AttentionTableComponent,
    RecommendationListComponent,
    LoadingSpinnerComponent,
    EmptyStateComponent,
    ProjectTableComponent,
    HealthDriversComponent,
    PaLineChartComponent,
    PaBarChartComponent,
    PaDonutChartComponent,
  ],
  templateUrl: './home.page.html',
  styleUrl: './home.page.scss',
})
export class HomePage implements OnInit, AfterViewInit, OnDestroy {
  private readonly workspaceApi = inject(WorkspaceApiService);
  private readonly analyticsApi = inject(AnalyticsApiService);
  private readonly recommendationApi = inject(RecommendationApiService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly glossary = SCORE_GLOSSARY;

  /** Maps to existing Home section titles — Synthesis-first IA. */
  readonly homeSections = [
    { id: 'home-synthesis', label: 'Synthesis' },
    { id: 'home-overview', label: 'Overview' },
    { id: 'home-visual', label: 'Visual analytics' },
    { id: 'home-exceptions', label: 'Exception queue' },
    { id: 'home-recommendations', label: 'Recommendations' },
  ] as const;

  readonly loading = signal(true);
  readonly recalculating = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly workspaces = signal<Workspace[]>([]);
  readonly selectedWorkspaceId = signal<string | null>(null);
  readonly dashboard = signal<ScopeDashboard | null>(null);
  readonly recommendations = signal<RecommendationBundle | null>(null);
  readonly explorerRows = signal<ExplorerProjectRow[]>([]);
  /** Canonical Average Health over time + drivers from workspace health-trends API. */
  readonly healthTrend = signal<WorkspaceHealthTrend | null>(null);
  readonly activeSectionId = signal<string>('home-synthesis');

  private sectionObserver: IntersectionObserver | null = null;
  private suppressSpyUntil = 0;

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
      .filter(
        (r) => r.needsAttention || r.critical || (r.overdueWorkPackageCount ?? 0) > 0
      )
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
      const deadline = r.nextDeadline;
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
  readonly needsChart = computed(() => needsAttentionSplit(this.explorerRows()));

  readonly healthDonut = computed((): PaDonutSlice[] =>
    this.healthChart().map((b) => ({ name: b.label, value: b.value, id: b.label }))
  );

  readonly progressBars = computed((): PaBarDatum[] =>
    this.progressChart().map((b) => ({ name: b.label, value: b.value, id: b.label }))
  );

  readonly needsBars = computed((): PaBarDatum[] =>
    this.needsChart().map((b) => ({ name: b.label, value: b.value, id: b.label }))
  );

  /** Average completion as part-to-whole (not a decorative gauge). */
  readonly averageProgressDonut = computed((): PaDonutSlice[] => {
    const board = this.dashboard();
    const avg = board?.kpis.averageCompletion;
    if (avg == null) {
      return [];
    }
    const complete = Math.max(0, Math.min(100, avg));
    const remaining = Math.max(0, 100 - complete);
    return [
      { name: 'Complete', value: Math.round(complete * 10) / 10 },
      { name: 'Remaining', value: Math.round(remaining * 10) / 10 },
    ];
  });

  /** Hole shows the KPI %, not the slice sum (100). */
  readonly averageProgressCenter = computed(() => {
    const avg = this.dashboard()?.kpis.averageCompletion;
    if (avg == null) {
      return null;
    }
    return Math.round(avg * 10) / 10;
  });

  readonly avgHealthCategories = computed(() => {
    const pts = this.healthTrend()?.points ?? [];
    return pts.map((p) => this.formatTrendLabel(p.calculatedAt));
  });

  readonly avgHealthSampleSizes = computed(() => {
    const pts = this.healthTrend()?.points ?? [];
    return pts.map((p) => p.sampleSize);
  });

  readonly avgHealthSeries = computed((): PaLineSeries[] => {
    const pts = this.healthTrend()?.points ?? [];
    if (pts.length < 2) {
      return [];
    }
    return [
      {
        id: 'average-health',
        name: 'Average Health',
        values: pts.map((p) => Number(p.averageHealthScore)),
        luminous: true,
      },
    ];
  });

  readonly avgHealthSubtitle = computed(() => {
    const pts = this.healthTrend()?.points ?? [];
    if (pts.length < 2) {
      return 'Equal-weight mean of project Health scores (0–100) · same definition as Average health KPI';
    }
    const first = Number(pts[0].averageHealthScore);
    const last = Number(pts[pts.length - 1].averageHealthScore);
    const delta = Math.round((last - first) * 10) / 10;
    const arrow = delta > 0.05 ? '↑' : delta < -0.05 ? '↓' : '→';
    const sign = delta > 0 ? `+${delta}` : `${delta}`;
    const n = pts[pts.length - 1].sampleSize;
    return `Equal-weight mean of project Health · same as Average health KPI\n${arrow} ${sign} across history · latest wave n=${n}`;
  });

  readonly queueColumns = [
    'name',
    'healthScore',
    'riskScore',
    'attentionScore',
    'progress',
    'overdueWorkPackageCount',
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

  ngAfterViewInit(): void {
    // Dashboard may load after first CD cycle — poll briefly for section nodes.
    const tryBind = (attempt: number) => {
      const nodes = document.querySelectorAll<HTMLElement>('[data-home-section]');
      if (nodes.length > 0) {
        this.bindSectionObserver(nodes);
        return;
      }
      if (attempt < 20) {
        window.setTimeout(() => tryBind(attempt + 1), 100);
      }
    };
    tryBind(0);
  }

  ngOnDestroy(): void {
    this.sectionObserver?.disconnect();
    this.sectionObserver = null;
  }

  scrollToSection(sectionId: string): void {
    const el = document.getElementById(sectionId);
    if (!el) {
      return;
    }
    this.activeSectionId.set(sectionId);
    // Ignore spy updates briefly so click target stays active during smooth scroll.
    this.suppressSpyUntil = Date.now() + 700;
    const reduceMotion =
      typeof window !== 'undefined' &&
      window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    el.scrollIntoView({
      behavior: reduceMotion ? 'auto' : 'smooth',
      block: 'start',
    });
  }

  private bindSectionObserver(nodes: NodeListOf<HTMLElement>): void {
    this.sectionObserver?.disconnect();
    this.sectionObserver = new IntersectionObserver(
      (entries) => {
        if (Date.now() < this.suppressSpyUntil) {
          return;
        }
        const visible = entries
          .filter((e) => e.isIntersecting)
          .sort((a, b) => b.intersectionRatio - a.intersectionRatio);
        const top = visible[0];
        const id = top?.target.getAttribute('data-home-section');
        if (id) {
          this.activeSectionId.set(id);
        }
      },
      {
        root: null,
        // Bias toward the section occupying the upper viewport under the sticky nav
        rootMargin: '-20% 0px -55% 0px',
        threshold: [0.1, 0.25, 0.5, 0.75],
      }
    );
    nodes.forEach((node) => this.sectionObserver?.observe(node));
  }

  selectWorkspace(workspaceId: string): void {
    this.selectedWorkspaceId.set(workspaceId);
    this.dashboard.set(null);
    this.recommendations.set(null);
    this.explorerRows.set([]);
    this.healthTrend.set(null);
    this.errorMessage.set(null);
    this.loading.set(true);
    this.analyticsApi.getWorkspaceDashboard(workspaceId).subscribe({
      next: (board) => {
        this.dashboard.set(board);
        this.loading.set(false);
        this.activeSectionId.set('home-synthesis');
        // Re-bind spy once section DOM exists for this dashboard render.
        window.setTimeout(() => {
          const nodes = document.querySelectorAll<HTMLElement>('[data-home-section]');
          if (nodes.length) {
            this.bindSectionObserver(nodes);
          }
        }, 0);
        this.recommendationApi.getWorkspaceRecommendations(workspaceId).subscribe({
          next: (bundle) => this.recommendations.set(bundle),
          error: () => this.recommendations.set(null),
        });
        this.analyticsApi.getWorkspaceHealthTrends(workspaceId).subscribe({
          next: (trend) => this.healthTrend.set(trend),
          error: () => this.healthTrend.set(null),
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

  onDistributionClick(name: string): void {
    const segment = [...this.healthChart(), ...this.progressChart(), ...this.needsChart()].find(
      (s) => s.label === name
    );
    if (!segment) {
      return;
    }
    this.onChartSegment(segment);
  }

  onChartSegment(segment: {
    label: string;
    drill?: string;
    healthMin?: number;
    healthMax?: number;
    progressMin?: number;
    progressMax?: number;
  }): void {
    const workspaceId = this.selectedWorkspaceId();
    if (!workspaceId) {
      return;
    }
    if (
      segment.drill === 'critical' ||
      segment.drill === 'delayed' ||
      segment.drill === 'needsAttention' ||
      segment.drill === 'hasOverdueWp'
    ) {
      this.openExplorer(segment.drill);
      return;
    }
    if (segment.label === 'Critical' || segment.label === 'High' || segment.label === 'Needs Attention') {
      this.openExplorer('needsAttention');
      return;
    }
    if (segment.label === 'Has overdue WPs') {
      this.openExplorer('hasOverdueWp');
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

  formatPercent(value: number | null | undefined): string {
    if (value == null) {
      return '—';
    }
    return `${value}%`;
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

  private formatTrendLabel(iso: string): string {
    const d = new Date(iso);
    if (Number.isNaN(d.getTime())) {
      return iso;
    }
    return d.toLocaleString(undefined, {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  }
}
