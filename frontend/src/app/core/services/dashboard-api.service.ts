import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { ApiResponse } from '../models/api-response.model';
import { ProjectAttentionSummary, ScopeAnalyticsKpis } from '../models/analytics.model';
import { ConfigurationService } from './configuration.service';

export interface WorkspaceDashboardCard {
  readonly workspaceId: string;
  readonly workspaceName: string;
  readonly synchronizationStatus: string;
  readonly totalProjects: number;
  readonly activeProjects: number;
  readonly criticalProjects: number;
  readonly highAttentionProjects: number;
  readonly averageHealthScore: number | null;
  readonly averageRiskScore: number | null;
  readonly averageAttentionScore: number | null;
}

export interface ExecutiveDashboard {
  readonly workspaceCount: number;
  readonly portfolioCount: number;
  readonly totalProjects: number;
  readonly criticalProjects: number;
  readonly highAttentionProjects: number;
  readonly workspaces: readonly WorkspaceDashboardCard[];
  readonly topAttentionProjects: readonly ProjectAttentionSummary[];
  readonly insights: readonly string[];
  readonly workspaceKpis: readonly ScopeAnalyticsKpis[];
}

@Injectable({ providedIn: 'root' })
export class DashboardApiService {
  private readonly http = inject(HttpClient);
  private readonly configuration = inject(ConfigurationService);

  getExecutiveDashboard(): Observable<ExecutiveDashboard> {
    return this.http
      .get<ApiResponse<ExecutiveDashboard>>(`${this.configuration.apiBaseUrl}/dashboards/executive`)
      .pipe(map((response) => response.data));
  }

  /**
   * Triggers authenticated download of executive CSV from the backend.
   */
  downloadExecutiveCsv(): void {
    const url = `${this.configuration.apiBaseUrl}/dashboards/executive/export.csv`;
    this.http.get(url, { responseType: 'blob', observe: 'response' }).subscribe((response) => {
      const blob = response.body;
      if (!blob) {
        return;
      }
      const objectUrl = URL.createObjectURL(blob);
      const anchor = document.createElement('a');
      anchor.href = objectUrl;
      anchor.download = 'executive-dashboard.csv';
      anchor.click();
      URL.revokeObjectURL(objectUrl);
    });
  }

  downloadWorkspaceCsv(workspaceId: string): void {
    const url = `${this.configuration.apiBaseUrl}/dashboards/workspace/${workspaceId}/export.csv`;
    this.http.get(url, { responseType: 'blob', observe: 'response' }).subscribe((response) => {
      const blob = response.body;
      if (!blob) {
        return;
      }
      const objectUrl = URL.createObjectURL(blob);
      const anchor = document.createElement('a');
      anchor.href = objectUrl;
      anchor.download = `workspace-${workspaceId}-dashboard.csv`;
      anchor.click();
      URL.revokeObjectURL(objectUrl);
    });
  }
}
