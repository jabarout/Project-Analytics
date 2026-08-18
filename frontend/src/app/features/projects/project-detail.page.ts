import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AnalyticsApiService } from '../../core/services/analytics-api.service';
import { RecommendationApiService } from '../../core/services/recommendation-api.service';
import { ProjectDashboard, ProjectWorkPackageAnalytics } from '../../core/models/analytics.model';
import { RecommendationBundle } from '../../core/models/recommendation.model';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state.component';
import { KpiCardComponent } from '../../shared/components/dashboard/kpi-card.component';
import { TrendChartComponent } from '../../shared/components/dashboard/trend-chart.component';
import { RecommendationListComponent } from '../../shared/components/dashboard/recommendation-list.component';
import { FactorBarsComponent } from '../../shared/components/dashboard/factor-bars.component';
import { BarChartComponent } from '../../shared/components/dashboard/bar-chart.component';
import { SCORE_GLOSSARY } from '../../shared/analytics/score-glossary';

@Component({
  selector: 'app-project-detail-page',
  standalone: true,
  imports: [
    RouterLink,
    LoadingSpinnerComponent,
    EmptyStateComponent,
    KpiCardComponent,
    TrendChartComponent,
    RecommendationListComponent,
    FactorBarsComponent,
    BarChartComponent,
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

  readonly wpStatusChart = computed(() => {
    const wp = this.wpAnalytics();
    if (!wp) {
      return [];
    }
    // Prefer synthetic management buckets for clarity
    return [
      { label: 'Open', value: wp.openWorkPackages, color: '#1d4ed8' },
      { label: 'In progress', value: wp.inProgressWorkPackages, color: '#b45309' },
      { label: 'Completed', value: wp.completedWorkPackages, color: '#0f766e' },
      { label: 'Overdue', value: wp.overdueWorkPackages, color: '#b91c1c' },
      { label: 'Blocked', value: wp.blockedWorkPackages, color: '#7c3aed' },
    ];
  });

  /** Completed vs remaining — based on WP status counts (Community-reliable). */
  readonly completionRemainChart = computed(() => {
    const wp = this.wpAnalytics();
    if (!wp || wp.totalWorkPackages <= 0) {
      return [];
    }
    return [
      { label: 'Completed', value: wp.completedWorkPackages, color: '#0f766e' },
      { label: 'Remaining', value: wp.openWorkPackages, color: '#b45309' },
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
}
