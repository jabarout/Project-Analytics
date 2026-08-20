import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AnalyticsApiService } from '../../core/services/analytics-api.service';
import { RecommendationApiService } from '../../core/services/recommendation-api.service';
import { ProjectDashboard, ProjectWorkPackageAnalytics } from '../../core/models/analytics.model';
import { RecommendationBundle } from '../../core/models/recommendation.model';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state.component';
import { KpiCardComponent } from '../../shared/components/dashboard/kpi-card.component';
import { RecommendationListComponent } from '../../shared/components/dashboard/recommendation-list.component';
import { PaBarChartComponent, PaBarDatum } from '../../shared/charts/pa-bar-chart.component';
import { PaDonutChartComponent, PaDonutSlice } from '../../shared/charts/pa-donut-chart.component';
import { PaLineChartComponent, PaLineSeries } from '../../shared/charts/pa-line-chart.component';
import { SCORE_GLOSSARY } from '../../shared/analytics/score-glossary';
import { readPaVizTokens } from '../../shared/charts/pa-echarts-theme';
import { PaRevealDirective } from '../../shared/directives/pa-reveal.directive';

@Component({
  selector: 'app-project-detail-page',
  standalone: true,
  imports: [
    PaRevealDirective,
    RouterLink,
    LoadingSpinnerComponent,
    EmptyStateComponent,
    KpiCardComponent,
    RecommendationListComponent,
    PaBarChartComponent,
    PaDonutChartComponent,
    PaLineChartComponent,
  ],
  templateUrl: './project-detail.page.html',
  styleUrl: './project-detail.page.scss',
})
export class ProjectDetailPage implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly analyticsApi = inject(AnalyticsApiService);
  private readonly recommendationApi = inject(RecommendationApiService);

  readonly glossary = SCORE_GLOSSARY;
  readonly loading = signal(true);
  readonly errorMessage = signal<string | null>(null);
  readonly dashboard = signal<ProjectDashboard | null>(null);
  readonly recommendations = signal<RecommendationBundle | null>(null);
  readonly wpAnalytics = signal<ProjectWorkPackageAnalytics | null>(null);

  /** WP status buckets — categorical counts → vertical ECharts bar. */
  readonly wpStatusBars = computed((): PaBarDatum[] => {
    const wp = this.wpAnalytics();
    if (!wp) {
      return [];
    }
    return [
      { name: 'Open', value: wp.openWorkPackages },
      { name: 'In progress', value: wp.inProgressWorkPackages },
      { name: 'Completed', value: wp.completedWorkPackages },
      { name: 'Overdue', value: wp.overdueWorkPackages },
      { name: 'Blocked', value: wp.blockedWorkPackages },
    ].filter((d) => d.value > 0);
  });

  readonly completionCenter = computed(() => {
    const pct = this.dashboard()?.analytics.completionPercentage;
    return pct != null ? Math.round(pct * 10) / 10 : null;
  });

  /** Single completion % as Complete/Remaining donut (not a gauge). */
  readonly completionDonut = computed((): PaDonutSlice[] => {
    const pct = this.dashboard()?.analytics.completionPercentage;
    if (pct == null) {
      return [];
    }
    const complete = Math.max(0, Math.min(100, pct));
    return [
      { name: 'Complete', value: Math.round(complete * 10) / 10 },
      { name: 'Remaining', value: Math.round((100 - complete) * 10) / 10 },
    ];
  });

  readonly healthFactorBars = computed(() =>
    this.toFactorBars(this.dashboard()?.analytics.health.factors ?? [])
  );
  readonly riskFactorBars = computed(() =>
    this.toFactorBars(this.dashboard()?.analytics.risk.factors ?? [])
  );
  readonly attentionFactorBars = computed(() =>
    this.toFactorBars(this.dashboard()?.analytics.attention.factors ?? [])
  );

  /**
   * Project score trends — fixed 3 series (Health / Risk / Needs Attention).
   * Readable at project scope (unlike N-project Home lines).
   */
  readonly trendCategories = computed(() => {
    const points = this.dashboard()?.trends ?? [];
    return points.map((p) => this.formatTrendLabel(p.calculatedAt));
  });

  readonly trendSeries = computed((): PaLineSeries[] => {
    const points = this.dashboard()?.trends ?? [];
    if (points.length < 2) {
      return [];
    }
    // Resolve bright semantic colors once per theme tick via CSS tokens when available
    let success = '#12b886';
    let warning = '#f59f00';
    let danger = '#fa5252';
    try {
      const t = readPaVizTokens();
      success = t.success;
      warning = t.warning;
      danger = t.danger;
    } catch {
      /* SSR / early */
    }
    return [
      {
        id: 'health',
        name: 'Health',
        values: points.map((p) => Number(p.healthScore)),
        color: success,
        luminous: true,
      },
      {
        id: 'risk',
        name: 'Risk',
        values: points.map((p) => Number(p.riskScore)),
        color: danger,
      },
      {
        id: 'attention',
        name: 'Needs Attention',
        values: points.map((p) => Number(p.attentionScore)),
        color: warning,
      },
    ];
  });

  /** Display overdue ratio as a percentage string, or em dash when unknown. */
  overdueRatioDisplay(ratio: number | null | undefined): string {
    if (ratio == null) {
      return '—';
    }
    return `${(ratio * 100).toFixed(0)}%`;
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      this.errorMessage.set('Missing project id.');
      this.loading.set(false);
      return;
    }
    this.analyticsApi.getProjectDashboard(id).subscribe({
      next: (board) => {
        this.dashboard.set(board);
        this.loading.set(false);
        this.recommendationApi.getProjectRecommendations(id).subscribe({
          next: (bundle) => this.recommendations.set(bundle),
          error: () => this.recommendations.set(null),
        });
      },
      error: () => {
        this.loading.set(false);
        this.errorMessage.set('Unable to load project analytics.');
      },
    });
    this.analyticsApi.getProjectWorkPackageAnalytics(id).subscribe({
      next: (wp) => this.wpAnalytics.set(wp),
      error: () => this.wpAnalytics.set(null),
    });
  }

  private toFactorBars(
    factors: readonly { code: string; description: string; contribution: number }[]
  ): PaBarDatum[] {
    return factors.map((f) => {
      const abs = Math.abs(f.contribution);
      const value = abs <= 1 ? Math.round(abs * 1000) / 10 : Math.round(abs * 10) / 10;
      const detail = (f.description || f.code).trim();
      return {
        // Short axis label; full factor sentence stays on hover via `detail`
        name: this.factorAxisLabel(f.code, detail),
        value,
        id: f.code,
        detail,
      };
    });
  }

  /** Compact y-axis labels so factor charts stay readable; full text is in tooltip. */
  private factorAxisLabel(code: string, fallback: string): string {
    switch (code) {
      case 'SCHEDULE':
        return 'Schedule';
      case 'DELIVERY':
        return 'Delivery';
      case 'OVERDUE':
        return 'Overdue';
      case 'OVERDUE_WP':
        return 'Overdue WPs';
      case 'SCHEDULE_RISK':
        return 'End date';
      case 'LOW_COMPLETION':
        return 'Incomplete work';
      case 'HEALTH_PRESSURE':
        return 'From Health';
      case 'RISK_PRESSURE':
        return 'From Risk';
      case 'OVERDUE_PRESSURE':
        return 'Overdue pressure';
      default:
        return fallback.length > 28 ? `${fallback.slice(0, 27)}…` : fallback;
    }
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
