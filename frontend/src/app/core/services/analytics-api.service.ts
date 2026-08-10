import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { ApiResponse } from '../models/api-response.model';
import {
  ProjectAnalytics,
  ProjectDashboard,
  ProjectWorkPackageAnalytics,
  ScopeAnalyticsKpis,
  ScopeDashboard,
} from '../models/analytics.model';
import { ExplorerProjectRow } from '../models/explorer.model';
import { ConfigurationService } from './configuration.service';

/**
 * Analytics and scope dashboard client. Backend scoring only; no local formulas.
 */
@Injectable({ providedIn: 'root' })
export class AnalyticsApiService {
  private readonly http = inject(HttpClient);
  private readonly configuration = inject(ConfigurationService);

  getExplorerProjects(workspaceId: string, portfolioId?: string | null): Observable<ExplorerProjectRow[]> {
    let params = new HttpParams();
    if (portfolioId) {
      params = params.set('portfolioId', portfolioId);
    }
    return this.http
      .get<ApiResponse<ExplorerProjectRow[]>>(
        `${this.configuration.apiBaseUrl}/analytics/workspaces/${workspaceId}/explorer-projects`,
        { params }
      )
      .pipe(map((response) => response.data));
  }

  getWorkspaceDashboard(workspaceId: string): Observable<ScopeDashboard> {
    return this.http
      .get<ApiResponse<ScopeDashboard>>(
        `${this.configuration.apiBaseUrl}/workspaces/${workspaceId}/dashboard`
      )
      .pipe(map((response) => response.data));
  }

  getWorkspaceKpis(workspaceId: string): Observable<ScopeAnalyticsKpis> {
    return this.http
      .get<ApiResponse<ScopeAnalyticsKpis>>(
        `${this.configuration.apiBaseUrl}/workspaces/${workspaceId}/kpis`
      )
      .pipe(map((response) => response.data));
  }

  getPortfolioDashboard(portfolioId: string): Observable<ScopeDashboard> {
    return this.http
      .get<ApiResponse<ScopeDashboard>>(
        `${this.configuration.apiBaseUrl}/portfolios/${portfolioId}/dashboard`
      )
      .pipe(map((response) => response.data));
  }

  getProjectDashboard(projectId: string): Observable<ProjectDashboard> {
    return this.http
      .get<ApiResponse<ProjectDashboard>>(
        `${this.configuration.apiBaseUrl}/projects/${projectId}/dashboard`
      )
      .pipe(map((response) => response.data));
  }

  getProjectWorkPackageAnalytics(projectId: string): Observable<ProjectWorkPackageAnalytics> {
    return this.http
      .get<ApiResponse<ProjectWorkPackageAnalytics>>(
        `${this.configuration.apiBaseUrl}/projects/${projectId}/work-package-analytics`
      )
      .pipe(map((response) => response.data));
  }

  getProjectAnalytics(projectId: string): Observable<ProjectAnalytics> {
    return this.http
      .get<ApiResponse<ProjectAnalytics>>(
        `${this.configuration.apiBaseUrl}/analytics/projects/${projectId}/kpis`
      )
      .pipe(map((response) => response.data));
  }

  recalculateWorkspace(workspaceId: string): Observable<{ projectsScored: number }> {
    return this.http
      .post<ApiResponse<{ projectsScored: number }>>(
        `${this.configuration.apiBaseUrl}/analytics/workspaces/${workspaceId}/recalculate`,
        {}
      )
      .pipe(map((response) => response.data));
  }
}
