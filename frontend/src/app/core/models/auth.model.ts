export interface LoginRequest {
  /** Username or email. */
  readonly username: string;
  readonly password: string;
}

export interface RegisterRequest {
  readonly email: string;
  readonly password: string;
  readonly username?: string;
}

export interface ForgotPasswordRequest {
  readonly email: string;
}

export interface ResetPasswordRequest {
  readonly token: string;
  readonly newPassword: string;
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
