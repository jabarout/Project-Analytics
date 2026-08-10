import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { ApiResponse } from '../models/api-response.model';
import { Recommendation, RecommendationBundle } from '../models/recommendation.model';
import { ConfigurationService } from './configuration.service';

/**
 * Recommendation client. Backend owns rule evaluation; no client-side scoring.
 */
@Injectable({ providedIn: 'root' })
export class RecommendationApiService {
  private readonly http = inject(HttpClient);
  private readonly configuration = inject(ConfigurationService);

  getProjectRecommendations(projectId: string): Observable<RecommendationBundle> {
    return this.http
      .get<ApiResponse<RecommendationBundle>>(
        `${this.configuration.apiBaseUrl}/projects/${projectId}/recommendations`
      )
      .pipe(map((response) => response.data));
  }

  getWorkspaceRecommendations(workspaceId: string): Observable<RecommendationBundle> {
    return this.http
      .get<ApiResponse<RecommendationBundle>>(
        `${this.configuration.apiBaseUrl}/workspaces/${workspaceId}/recommendations`
      )
      .pipe(map((response) => response.data));
  }

  getPortfolioRecommendations(portfolioId: string): Observable<RecommendationBundle> {
    return this.http
      .get<ApiResponse<RecommendationBundle>>(
        `${this.configuration.apiBaseUrl}/portfolios/${portfolioId}/recommendations`
      )
      .pipe(map((response) => response.data));
  }

  getExecutiveRecommendations(): Observable<RecommendationBundle> {
    return this.http
      .get<ApiResponse<RecommendationBundle>>(
        `${this.configuration.apiBaseUrl}/recommendations/executive`
      )
      .pipe(map((response) => response.data));
  }

  getRecommendation(id: string): Observable<Recommendation> {
    return this.http
      .get<ApiResponse<Recommendation>>(`${this.configuration.apiBaseUrl}/recommendations/${id}`)
      .pipe(map((response) => response.data));
  }
}
