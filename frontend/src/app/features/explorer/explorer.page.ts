import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { WorkspaceApiService } from '../../core/services/workspace-api.service';
import { AnalyticsApiService } from '../../core/services/analytics-api.service';
import { PortfolioApiService } from '../../core/services/portfolio-api.service';
import { SavedViewsService } from '../../core/services/saved-views.service';
import { Workspace } from '../../core/models/workspace.model';
import { PortfolioSummary } from '../../core/models/portfolio.model';
import {
  ExplorerFilters,
  ExplorerGroupBy,
  ExplorerProjectRow,
  ExplorerSortSpec,
  ExplorerViewState,
  SavedExplorerView,
  defaultExplorerViewState,
} from '../../core/models/explorer.model';
import {
  applyExplorerPipeline,
  explorerStateFromParams,
  explorerStateToParams,
} from '../../shared/analytics/explorer-query';
import { ExplorerFilterPanelComponent } from '../../shared/components/explorer/explorer-filter-panel.component';
import { ProjectTableComponent } from '../../shared/components/explorer/project-table.component';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state.component';
import { BarChartComponent, BarChartDatum } from '../../shared/components/dashboard/bar-chart.component';
import { healthDistribution } from '../../shared/analytics/distribution';
import { downloadExplorerCsv } from '../../shared/utils/csv-export';

/**
 * Primary analytical workspace (M11A/B Explorer). Filter, sort, group, Saved Views.
 */
@Component({
  selector: 'app-explorer-page',
  standalone: true,
  imports: [
    RouterLink,
    FormsModule,
    ExplorerFilterPanelComponent,
    ProjectTableComponent,
    LoadingSpinnerComponent,
    EmptyStateComponent,
    BarChartComponent,
  ],
  templateUrl: './explorer.page.html',
  styleUrl: './explorer.page.scss',
})
export class ExplorerPage implements OnInit {
  private readonly workspaceApi = inject(WorkspaceApiService);
  private readonly analyticsApi = inject(AnalyticsApiService);
  private readonly portfolioApi = inject(PortfolioApiService);
  private readonly savedViews = inject(SavedViewsService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly loading = signal(true);
  readonly errorMessage = signal<string | null>(null);
  readonly workspaces = signal<Workspace[]>([]);
  readonly portfolios = signal<PortfolioSummary[]>([]);
  readonly rows = signal<ExplorerProjectRow[]>([]);
  readonly viewState = signal<ExplorerViewState>(defaultExplorerViewState());
  readonly savedViewList = signal<SavedExplorerView[]>([]);
  readonly saveName = signal('');

  readonly pipeline = computed(() => applyExplorerPipeline(this.rows(), this.viewState()));
  readonly matchCount = computed(() => this.pipeline().filtered.length);
  readonly resultHealthChart = computed(() => healthDistribution(this.pipeline().filtered));
  readonly adminSuggestions = computed(() => {
    const set = new Set<string>();
    for (const row of this.rows()) {
      const admin = row.projectAdmin?.trim();
      if (!admin) {
        continue;
      }
      // May be "Alice, Bob"
      for (const part of admin.split(',')) {
        if (part.trim()) {
          set.add(part.trim());
        }
      }
    }
    return [...set].sort((a, b) => a.localeCompare(b));
  });

  ngOnInit(): void {
    this.savedViewList.set(this.savedViews.list());
    this.workspaceApi.listWorkspaces().subscribe({
      next: (items) => {
        this.workspaces.set(items);
        const params = this.route.snapshot.queryParamMap;
        const paramMap: Record<string, string> = {};
        params.keys.forEach((k) => {
          const v = params.get(k);
          if (v != null) {
            paramMap[k] = v;
          }
        });
        const fallback = items[0]?.id ?? null;
        let state = explorerStateFromParams(paramMap, fallback);
        if (!state.filters.workspaceId && fallback) {
          state = {
            ...state,
            filters: { ...state.filters, workspaceId: fallback },
          };
        }
        if (!params.keys.length) {
          const def = this.savedViews.getDefault();
          if (def) {
            state = def.state;
          }
        }
        this.viewState.set(state);
        this.loading.set(false);
        if (state.filters.workspaceId) {
          this.loadScope(state.filters.workspaceId, state.filters.portfolioId);
        }
        this.route.queryParamMap.subscribe((q) => {
          const map: Record<string, string> = {};
          q.keys.forEach((k) => {
            const v = q.get(k);
            if (v != null) {
              map[k] = v;
            }
          });
          if (!q.keys.length) {
            return;
          }
          const next = explorerStateFromParams(map, this.viewState().filters.workspaceId);
          const prev = this.viewState();
          const scopeChanged =
            next.filters.workspaceId !== prev.filters.workspaceId ||
            next.filters.portfolioId !== prev.filters.portfolioId;
          this.viewState.set(next);
          if (scopeChanged && next.filters.workspaceId) {
            this.loadScope(next.filters.workspaceId, next.filters.portfolioId);
          }
        });
      },
      error: () => {
        this.loading.set(false);
        this.errorMessage.set('Unable to load workspaces.');
      },
    });
  }

  loadScope(workspaceId: string, portfolioId: string | null): void {
    this.loading.set(true);
    this.errorMessage.set(null);
    this.portfolioApi.listPortfolios(workspaceId).subscribe({
      next: (list) => this.portfolios.set(list),
      error: () => this.portfolios.set([]),
    });
    this.analyticsApi.getExplorerProjects(workspaceId, portfolioId).subscribe({
      next: (data) => {
        this.rows.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.errorMessage.set(
          'Explorer data unavailable. Synchronize the workspace and recalculate analytics if needed.'
        );
      },
    });
  }

  onWorkspaceChange(workspaceId: string): void {
    this.patchFilters({ workspaceId, portfolioId: null });
    this.loadScope(workspaceId, null);
  }

  patchFilters(partial: Partial<ExplorerFilters>): void {
    const next: ExplorerViewState = {
      ...this.viewState(),
      filters: { ...this.viewState().filters, ...partial },
    };
    // Portfolio change reloads membership set from API
    if ('portfolioId' in partial && next.filters.workspaceId) {
      this.loadScope(next.filters.workspaceId, next.filters.portfolioId);
    }
    this.commitState(next);
  }

  setGroupBy(groupBy: ExplorerGroupBy): void {
    this.commitState({ ...this.viewState(), groupBy });
  }

  onSortChange(spec: ExplorerSortSpec): void {
    this.commitState({ ...this.viewState(), sort: [spec] });
  }

  resetFilters(): void {
    const ws = this.viewState().filters.workspaceId;
    const next = defaultExplorerViewState(ws);
    this.commitState(next);
    if (ws) {
      this.loadScope(ws, null);
    }
  }

  saveCurrentView(): void {
    const name = this.saveName().trim();
    if (!name) {
      return;
    }
    this.savedViews.save(name, this.viewState());
    this.savedViewList.set(this.savedViews.list());
    this.saveName.set('');
  }

  applySavedView(id: string): void {
    const view = this.savedViewList().find((v) => v.id === id);
    if (!view) {
      return;
    }
    this.commitState(view.state);
    if (view.state.filters.workspaceId) {
      this.loadScope(view.state.filters.workspaceId, view.state.filters.portfolioId);
    }
  }

  deleteSavedView(id: string): void {
    this.savedViews.delete(id);
    this.savedViewList.set(this.savedViews.list());
  }

  exportCsv(): void {
    const filtered = this.pipeline().filtered;
    const ws = this.viewState().filters.workspaceId ?? 'workspace';
    downloadExplorerCsv(filtered, this.viewState().columns, `explorer-${ws}.csv`);
  }

  onResultChartSegment(segment: BarChartDatum): void {
    if (segment.drill === 'critical') {
      this.patchFilters({ criticalOnly: true });
      return;
    }
    this.patchFilters({
      healthMin: segment.healthMin ?? null,
      healthMax: segment.healthMax ?? null,
      criticalOnly: false,
    });
  }

  private commitState(state: ExplorerViewState): void {
    this.viewState.set(state);
    void this.router.navigate([], {
      relativeTo: this.route,
      queryParams: explorerStateToParams(state),
      replaceUrl: true,
    });
  }
}
