import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { AppFooterComponent } from '../../shared/components/app-footer/app-footer.component';
import { BrandLogoComponent } from '../../shared/components/brand-logo/brand-logo.component';

/**
 * Minimal layout for unauthenticated screens.
 */
@Component({
  selector: 'app-auth-layout',
  standalone: true,
  imports: [RouterOutlet, AppFooterComponent, BrandLogoComponent],
  template: `
    <div class="auth-shell">
      <div class="auth-shell__center">
        <div class="auth-shell__panel pa-card">
          <header class="auth-shell__header">
            <app-brand-logo size="lg" />
            <p class="auth-shell__eyebrow">Secure access</p>
            <h1>Sign in to Project Analytics</h1>
            <p class="auth-shell__subtitle">
              Decision intelligence for OpenProject. Operational data remains in OpenProject.
            </p>
          </header>
          <router-outlet />
        </div>
      </div>
      <app-footer />
    </div>
  `,
  styles: `
    .auth-shell {
      min-height: 100vh;
      display: flex;
      flex-direction: column;
      background: var(--pa-bg);
      color: var(--pa-text);
    }

    .auth-shell__center {
      flex: 1;
      display: grid;
      place-items: center;
      padding: 2rem 1.25rem;
    }

    .auth-shell__panel {
      width: min(440px, 100%);
      padding: 2rem;
    }

    .auth-shell__header {
      margin-bottom: 1.5rem;
      display: flex;
      flex-direction: column;
      gap: 0.65rem;
    }

    .auth-shell__eyebrow {
      margin: 0.35rem 0 0;
      text-transform: uppercase;
      letter-spacing: 0.08em;
      font-size: 0.72rem;
      font-weight: 650;
      color: var(--pa-text-tertiary);
    }

    h1 {
      margin: 0;
      font-size: 1.45rem;
      letter-spacing: -0.02em;
      font-weight: 700;
    }

    .auth-shell__subtitle {
      margin: 0;
      color: var(--pa-text-secondary);
      line-height: 1.5;
      font-size: 0.95rem;
    }
  `,
})
export class AuthLayoutComponent {}
