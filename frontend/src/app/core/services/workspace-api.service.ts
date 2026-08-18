import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { ApiResponse } from '../models/api-response.model';
import {
  CreateWorkspaceRequest,
  SynchronizationStatus,
  Workspace,
  WorkspaceMember,
} from '../models/workspace.model';
import { PortfolioProjectSummary } from '../models/portfolio.model';
import { ConfigurationService } from './configuration.service';

/**
 * HTTP client for workspace and synchronization endpoints.
 */
@Injectable({ providedIn: 'root' })
export class WorkspaceApiService {
  private readonly http = inject(HttpClient);
  private readonly configuration = inject(ConfigurationService);

  listWorkspaces(): Observable<Workspace[]> {
    return this.http
      .get<ApiResponse<Workspace[]>>(`${this.configuration.apiBaseUrl}/workspaces`)
      .pipe(map((response) => response.data));
  }

  listWorkspaceProjects(workspaceId: string): Observable<PortfolioProjectSummary[]> {
    return this.http
      .get<ApiResponse<PortfolioProjectSummary[]>>(
        `${this.configuration.apiBaseUrl}/workspaces/${workspaceId}/projects`
      )
      .pipe(map((response) => response.data));
  }

  createWorkspace(request: CreateWorkspaceRequest): Observable<Workspace> {
    return this.http
      .post<ApiResponse<Workspace>>(`${this.configuration.apiBaseUrl}/workspaces`, request)
      .pipe(map((response) => response.data));
  }

  oauthStatus(): Observable<{
    enabled: boolean;
    redirectUri: string | null;
    globalClientDefaultsAvailable: boolean;
  }> {
    return this.http
      .get<
        ApiResponse<{
          enabled: boolean;
          redirectUri: string | null;
          globalClientDefaultsAvailable: boolean;
        }>
      >(`${this.configuration.apiBaseUrl}/workspaces/oauth/status`)
      .pipe(map((response) => response.data));
  }

  /** Preferred connect path: returns OP authorization URL. Client id/secret are per OpenProject. */
  startOAuth(request: {
    baseUrl: string;
    name?: string;
    clientId?: string;
    clientSecret?: string;
  }): Observable<{
    authorizationUrl: string;
    state: string;
    oauthConfigured: boolean;
  }> {
    return this.http
      .post<
        ApiResponse<{ authorizationUrl: string; state: string; oauthConfigured: boolean }>
      >(`${this.configuration.apiBaseUrl}/workspaces/oauth/start`, request)
      .pipe(map((response) => response.data));
  }

  /** Alternative connect path: eligibility check + encrypted API key. */
  connectWithApiKey(request: {
    baseUrl: string;
    name?: string;
    apiKey?: string;
  }): Observable<Workspace> {
    return this.http
      .post<ApiResponse<Workspace>>(`${this.configuration.apiBaseUrl}/workspaces/connect/api-key`, request)
      .pipe(map((response) => response.data));
  }

  renameWorkspace(workspaceId: string, name: string): Observable<Workspace> {
    return this.http
      .put<ApiResponse<Workspace>>(`${this.configuration.apiBaseUrl}/workspaces/${workspaceId}`, { name })
      .pipe(map((response) => response.data));
  }

  deleteWorkspace(workspaceId: string): Observable<void> {
    return this.http
      .delete<void>(`${this.configuration.apiBaseUrl}/workspaces/${workspaceId}`)
      .pipe(map(() => void 0));
  }

  synchronize(workspaceId: string): Observable<SynchronizationStatus> {
    return this.http
      .post<ApiResponse<SynchronizationStatus>>(
        `${this.configuration.apiBaseUrl}/workspaces/${workspaceId}/synchronize`,
        {}
      )
      .pipe(map((response) => response.data));
  }

  getSynchronizationStatus(workspaceId: string): Observable<SynchronizationStatus> {
    return this.http
      .get<ApiResponse<SynchronizationStatus>>(
        `${this.configuration.apiBaseUrl}/workspaces/${workspaceId}/synchronization`
      )
      .pipe(map((response) => response.data));
  }

  listMembers(workspaceId: string): Observable<WorkspaceMember[]> {
    return this.http
      .get<ApiResponse<WorkspaceMember[]>>(
        `${this.configuration.apiBaseUrl}/workspaces/${workspaceId}/members`
      )
      .pipe(map((response) => response.data));
  }

  grantMember(workspaceId: string, email: string): Observable<WorkspaceMember> {
    return this.http
      .post<ApiResponse<WorkspaceMember>>(
        `${this.configuration.apiBaseUrl}/workspaces/${workspaceId}/members`,
        { email }
      )
      .pipe(map((response) => response.data));
  }

  revokeMember(workspaceId: string, userId: string): Observable<void> {
    return this.http
      .delete<void>(`${this.configuration.apiBaseUrl}/workspaces/${workspaceId}/members/${userId}`)
      .pipe(map(() => void 0));
  }
}
