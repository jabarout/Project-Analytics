import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, catchError, map, of, tap, throwError } from 'rxjs';
import { ApiResponse } from '../models/api-response.model';
import {
  LoginRequest,
  LoginResponse,
  UpdatePreferencesRequest,
  UserPreference,
  UserProfile,
} from '../models/auth.model';
import { ConfigurationService } from './configuration.service';

const TOKEN_STORAGE_KEY = 'pa.auth.token';

/**
 * Authentication state and API client. No business calculations.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly configuration = inject(ConfigurationService);
  private readonly router = inject(Router);

  private readonly tokenSignal = signal<string | null>(this.readStoredToken());
  private readonly currentUserSignal = signal<UserProfile | null>(null);

  readonly token = this.tokenSignal.asReadonly();
  readonly currentUser = this.currentUserSignal.asReadonly();
  readonly isAuthenticated = computed(() => !!this.tokenSignal());

  login(credentials: LoginRequest): Observable<LoginResponse> {
    const url = `${this.configuration.apiBaseUrl}/auth/login`;
    return this.http.post<ApiResponse<LoginResponse>>(url, credentials).pipe(
      map((response) => response.data),
      tap((data) => {
        this.persistToken(data.token);
        this.tokenSignal.set(data.token);
      }),
      catchError((error) => throwError(() => error))
    );
  }

  loadCurrentUser(): Observable<UserProfile | null> {
    if (!this.tokenSignal()) {
      this.currentUserSignal.set(null);
      return of(null);
    }

    const url = `${this.configuration.apiBaseUrl}/auth/me`;
    return this.http.get<ApiResponse<UserProfile>>(url).pipe(
      map((response) => response.data),
      tap((user) => this.currentUserSignal.set(user)),
      catchError(() => {
        this.clearSession();
        return of(null);
      })
    );
  }

  logout(): Observable<void> {
    const url = `${this.configuration.apiBaseUrl}/auth/logout`;
    const finalizeLogout = () => {
      this.clearSession();
      void this.router.navigateByUrl('/login');
    };

    if (!this.tokenSignal()) {
      finalizeLogout();
      return of(void 0);
    }

    return this.http.post<ApiResponse<null>>(url, {}).pipe(
      map(() => void 0),
      tap(() => finalizeLogout()),
      catchError(() => {
        finalizeLogout();
        return of(void 0);
      })
    );
  }

  updatePreferences(request: UpdatePreferencesRequest): Observable<UserPreference> {
    const url = `${this.configuration.apiBaseUrl}/users/me/preferences`;
    return this.http.put<ApiResponse<UserPreference>>(url, request).pipe(
      map((response) => response.data),
      tap((preferences) => {
        const current = this.currentUserSignal();
        if (current) {
          this.currentUserSignal.set({ ...current, preferences });
        }
      })
    );
  }

  updateTheme(theme: string): Observable<UserPreference> {
    const url = `${this.configuration.apiBaseUrl}/users/me/theme`;
    return this.http.patch<ApiResponse<UserPreference>>(url, { theme }).pipe(
      map((response) => response.data),
      tap((preferences) => {
        const current = this.currentUserSignal();
        if (current) {
          this.currentUserSignal.set({ ...current, preferences });
        }
      })
    );
  }

  clearSession(): void {
    this.tokenSignal.set(null);
    this.currentUserSignal.set(null);
    localStorage.removeItem(TOKEN_STORAGE_KEY);
  }

  private persistToken(token: string): void {
    localStorage.setItem(TOKEN_STORAGE_KEY, token);
  }

  private readStoredToken(): string | null {
    return localStorage.getItem(TOKEN_STORAGE_KEY);
  }
}
