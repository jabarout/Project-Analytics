import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { ApiResponse } from '../models/api-response.model';
import {
  CreatePortfolioRequest,
  PortfolioDetail,
  PortfolioSummary,
  UpdatePortfolioRequest,
} from '../models/portfolio.model';
import { ConfigurationService } from './configuration.service';

/**
 * Portfolio HTTP client. Backend serves local synchronized data only.
 */
@Injectable({ providedIn: 'root' })
export class PortfolioApiService {
  private readonly http = inject(HttpClient);
  private readonly configuration = inject(ConfigurationService);

  listPortfolios(workspaceId?: string): Observable<PortfolioSummary[]> {
    let params = new HttpParams();
    if (workspaceId) {
      params = params.set('workspaceId', workspaceId);
    }
    return this.http
      .get<ApiResponse<PortfolioSummary[]>>(`${this.configuration.apiBaseUrl}/portfolios`, { params })
      .pipe(map((response) => response.data));
  }

  getPortfolio(id: string): Observable<PortfolioDetail> {
    return this.http
      .get<ApiResponse<PortfolioDetail>>(`${this.configuration.apiBaseUrl}/portfolios/${id}`)
      .pipe(map((response) => response.data));
  }

  createPortfolio(request: CreatePortfolioRequest): Observable<PortfolioSummary> {
    return this.http
      .post<ApiResponse<PortfolioSummary>>(`${this.configuration.apiBaseUrl}/portfolios`, request)
      .pipe(map((response) => response.data));
  }

  updatePortfolio(id: string, request: UpdatePortfolioRequest): Observable<PortfolioSummary> {
    return this.http
      .put<ApiResponse<PortfolioSummary>>(`${this.configuration.apiBaseUrl}/portfolios/${id}`, request)
      .pipe(map((response) => response.data));
  }

  deletePortfolio(id: string): Observable<void> {
    return this.http
      .delete<void>(`${this.configuration.apiBaseUrl}/portfolios/${id}`)
      .pipe(map(() => void 0));
  }

  /** Adds project membership (many-to-many). Does not move ownership. */
  addProject(portfolioId: string, projectId: string): Observable<PortfolioDetail> {
    return this.http
      .post<ApiResponse<PortfolioDetail>>(`${this.configuration.apiBaseUrl}/portfolios/${portfolioId}/projects`, {
        projectId,
      })
      .pipe(map((response) => response.data));
  }

  /** Bulk-add memberships after filtering/selecting many projects. */
  addProjectsBulk(portfolioId: string, projectIds: readonly string[]): Observable<PortfolioDetail> {
    return this.http
      .post<ApiResponse<PortfolioDetail>>(
        `${this.configuration.apiBaseUrl}/portfolios/${portfolioId}/projects/bulk`,
        { projectIds }
      )
      .pipe(map((response) => response.data));
  }

  /** Removes project membership only; project stays in the workspace. */
  removeProject(portfolioId: string, projectId: string): Observable<PortfolioDetail> {
    return this.http
      .delete<ApiResponse<PortfolioDetail>>(
        `${this.configuration.apiBaseUrl}/portfolios/${portfolioId}/projects/${projectId}`
      )
      .pipe(map((response) => response.data));
  }

  listWorkspaceProjects(workspaceId: string): Observable<PortfolioDetail['projects']> {
    return this.http
      .get<ApiResponse<PortfolioDetail['projects']>>(
        `${this.configuration.apiBaseUrl}/workspaces/${workspaceId}/projects`
      )
      .pipe(map((response) => response.data));
  }
}
