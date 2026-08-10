import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import {
  DashboardApiService,
  ExecutiveDashboard,
} from '../../core/services/dashboard-api.service';
import { RecommendationApiService } from '../../core/services/recommendation-api.service';
import { RecommendationBundle } from '../../core/models/recommendation.model';
import { KpiCardComponent } from '../../shared/components/dashboard/kpi-card.component';
import { InsightListComponent } from '../../shared/components/dashboard/insight-list.component';
import { AttentionTableComponent } from '../../shared/components/dashboard/attention-table.component';
import { BarChartComponent, BarChartDatum } from '../../shared/components/dashboard/bar-chart.component';
import { RecommendationListComponent } from '../../shared/components/dashboard/recommendation-list.component';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state.component';

@Component({
  selector: 'app-executive-dashboard-page',
  standalone: true,
  imports: [
    RouterLink,
    KpiCardComponent,
    InsightListComponent,
    AttentionTableComponent,
    BarChartComponent,
    RecommendationListComponent,
    LoadingSpinnerComponent,
    EmptyStateComponent,
  ],
  templateUrl: './executive-dashboard.page.html',
  styleUrl: './executive-dashboard.page.scss',
})
export class ExecutiveDashboardPage implements OnInit {
  private readonly dashboardApi = inject(DashboardApiService);
  private readonly recommendationApi = inject(RecommendationApiService);

  readonly loading = signal(true);
  readonly errorMessage = signal<string | null>(null);
  readonly dashboard = signal<ExecutiveDashboard | null>(null);
  readonly recommendations = signal<RecommendationBundle | null>(null);

  readonly healthChart = computed<BarChartDatum[]>(() => {
    const data = this.dashboard();
    if (!data) {
      return [];
    }
    return data.workspaces.map((workspace) => ({
      label: workspace.workspaceName.slice(0, 10),
      value: workspace.averageHealthScore ?? 0,
      color: '#0f766e',
    }));
  });

  readonly attentionChart = computed<BarChartDatum[]>(() => {
    const data = this.dashboard();
    if (!data) {
      return [];
    }
    return data.workspaces.map((workspace) => ({
      label: workspace.workspaceName.slice(0, 10),
      value: workspace.averageAttentionScore ?? 0,
      color: '#1d4ed8',
    }));
  });

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.loading.set(true);
    this.errorMessage.set(null);
    this.recommendations.set(null);
    this.dashboardApi.getExecutiveDashboard().subscribe({
      next: (board) => {
        this.dashboard.set(board);
        this.loading.set(false);
        this.recommendationApi.getExecutiveRecommendations().subscribe({
          next: (bundle) => this.recommendations.set(bundle),
          error: () => this.recommendations.set(null),
        });
      },
      error: () => {
        this.loading.set(false);
        this.errorMessage.set('Unable to load executive dashboard.');
      },
    });
  }

  exportCsv(): void {
    this.dashboardApi.downloadExecutiveCsv();
  }
}
