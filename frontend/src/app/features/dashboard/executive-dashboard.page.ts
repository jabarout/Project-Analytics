import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import {
  DashboardApiService,
  ExecutiveDashboard,
} from '../../core/services/dashboard-api.service';
import { RecommendationApiService } from '../../core/services/recommendation-api.service';
import { WorkspaceApiService } from '../../core/services/workspace-api.service';
import { RecommendationBundle } from '../../core/models/recommendation.model';
import { KpiCardComponent } from '../../shared/components/dashboard/kpi-card.component';
import { InsightListComponent } from '../../shared/components/dashboard/insight-list.component';
import { AttentionTableComponent } from '../../shared/components/dashboard/attention-table.component';
import { RecommendationListComponent } from '../../shared/components/dashboard/recommendation-list.component';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state.component';
import { ThemeService } from '../../core/services/theme.service';
import { PaBarChartComponent, PaBarDatum } from '../../shared/charts/pa-bar-chart.component';
import { readPaVizTokens } from '../../shared/charts/pa-echarts-theme';
import { PaRevealDirective } from '../../shared/directives/pa-reveal.directive';

/**
 * Cross-workspace overview (Option C): only meaningful when the user can access 2+ workspaces.
 * 0 or 1 workspace → redirect to Home (Home owns connect empty-state / single-ws triage).
 */
@Component({
  selector: 'app-executive-dashboard-page',
  standalone: true,
  imports: [
    PaRevealDirective,
    RouterLink,
    KpiCardComponent,
    InsightListComponent,
    AttentionTableComponent,
    PaBarChartComponent,
    RecommendationListComponent,
    LoadingSpinnerComponent,
    EmptyStateComponent,
  ],
  templateUrl: './executive-dashboard.page.html',
  styleUrl: './executive-dashboard.page.scss',
})
export class ExecutiveDashboardPage implements OnInit {
  private readonly router = inject(Router);
  private readonly workspaceApi = inject(WorkspaceApiService);
  private readonly dashboardApi = inject(DashboardApiService);
  private readonly recommendationApi = inject(RecommendationApiService);
  private readonly themeService = inject(ThemeService);

  readonly loading = signal(true);
  readonly errorMessage = signal<string | null>(null);
  readonly dashboard = signal<ExecutiveDashboard | null>(null);
  readonly recommendations = signal<RecommendationBundle | null>(null);

  /** Prefer horizontal when workspace names are long / several scopes. */
  readonly useHorizontalBars = computed(() => {
    const workspaces = this.dashboard()?.workspaces ?? [];
    return workspaces.length > 4 || workspaces.some((w) => w.workspaceName.length > 14);
  });

  readonly healthBars = computed((): PaBarDatum[] => {
    this.themeService.theme();
    const data = this.dashboard();
    if (!data) {
      return [];
    }
    const tokens = readPaVizTokens();
    return data.workspaces.map((workspace) => ({
      name: workspace.workspaceName,
      value: workspace.averageHealthScore ?? 0,
      id: workspace.workspaceId,
      color: tokens.success,
    }));
  });

  readonly attentionBars = computed((): PaBarDatum[] => {
    this.themeService.theme();
    const data = this.dashboard();
    if (!data) {
      return [];
    }
    const tokens = readPaVizTokens();
    return data.workspaces.map((workspace) => ({
      name: workspace.workspaceName,
      value: workspace.averageAttentionScore ?? 0,
      id: workspace.workspaceId,
      color: tokens.warning,
    }));
  });

  ngOnInit(): void {
    this.workspaceApi.listWorkspaces().subscribe({
      next: (workspaces) => {
        if (workspaces.length < 2) {
          void this.router.navigate(['/'], { replaceUrl: true });
          return;
        }
        this.reload();
      },
      error: () => {
        // Cannot evaluate Option C gate — send user to Home rather than a broken Executive.
        void this.router.navigate(['/'], { replaceUrl: true });
      },
    });
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
