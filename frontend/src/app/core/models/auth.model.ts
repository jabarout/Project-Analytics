export interface LoginRequest {
  readonly username: string;
  readonly password: string;
}

export interface LoginResponse {
  readonly token: string;
  readonly expiresAt: string;
}

export interface UserPreference {
  readonly theme: string;
  readonly language: string;
  readonly dashboardConfiguration: string | null;
}

export interface UserProfile {
  readonly id: string;
  readonly username: string;
  readonly email: string;
  readonly role: string;
  readonly enabled: boolean;
  readonly preferences: UserPreference;
}

export interface UpdatePreferencesRequest {
  readonly theme: string;
  readonly language: string;
  readonly dashboardConfiguration?: string | null;
}
