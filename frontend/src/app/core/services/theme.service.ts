import { Injectable, inject, signal } from '@angular/core';
import { AuthService } from './auth.service';

export type PaTheme = 'light' | 'dark';

/**
 * Applies monochrome theme tokens via documentElement data-theme.
 * Persists through user preferences when authenticated.
 */
@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly authService = inject(AuthService);
  private readonly themeSignal = signal<PaTheme>(this.readInitialTheme());

  readonly theme = this.themeSignal.asReadonly();

  constructor() {
    this.apply(this.themeSignal());
  }

  /** Call after login / loadCurrentUser so saved preference wins. */
  syncFromUserPreferences(): void {
    const saved = this.authService.currentUser()?.preferences?.theme;
    if (saved === 'dark' || saved === 'light') {
      this.setTheme(saved);
      return;
    }
    this.apply(this.themeSignal());
  }

  setTheme(theme: PaTheme): void {
    this.themeSignal.set(theme);
    this.persistLocal(theme);
    this.apply(theme);
  }

  toggle(): void {
    this.setTheme(this.themeSignal() === 'dark' ? 'light' : 'dark');
  }

  private apply(theme: PaTheme): void {
    document.documentElement.dataset['theme'] = theme;
    document.documentElement.style.colorScheme = theme;
  }

  private readInitialTheme(): PaTheme {
    const stored = localStorage.getItem('pa.theme');
    if (stored === 'dark' || stored === 'light') {
      return stored;
    }
    // Product default: dark-first premium analytics workstation
    return 'dark';
  }

  /** Cache guest/session choice locally (Preferences API remains source of truth when logged in). */
  persistLocal(theme: PaTheme): void {
    localStorage.setItem('pa.theme', theme);
  }
}
