import { Component } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';
import { AppFooterComponent } from '../../shared/components/app-footer/app-footer.component';
import { BrandLogoComponent } from '../../shared/components/brand-logo/brand-logo.component';

/**
 * Minimal public chrome for Privacy / Contact / Terms (no auth required).
 */
@Component({
  selector: 'app-public-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, AppFooterComponent, BrandLogoComponent],
  template: `
    <div class="public-shell">
      <header class="public-shell__header">
        <a routerLink="/login" class="public-shell__brand" aria-label="Project Analytics">
          <app-brand-logo size="md" />
        </a>
        <a routerLink="/login" class="pa-btn pa-btn--secondary pa-btn--sm">Sign in</a>
      </header>
      <main class="public-shell__main pa-page-enter">
        <router-outlet />
      </main>
      <app-footer />
    </div>
  `,
  styles: `
    .public-shell {
      min-height: 100vh;
      display: flex;
      flex-direction: column;
      background: var(--pa-bg);
      color: var(--pa-text);
    }
    .public-shell__header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 1rem 2rem;
      border-bottom: 1px solid var(--pa-border);
      background: var(--pa-surface);
    }
    .public-shell__brand {
      font-weight: 650;
      text-decoration: none;
      color: var(--pa-text);
      letter-spacing: -0.01em;
    }
    .public-shell__brand:hover {
      text-decoration: none;
    }
    .public-shell__main {
      flex: 1;
      width: min(720px, 100%);
      margin: 0 auto;
      padding: 2.5rem 1.5rem 3rem;
    }
  `,
})
export class PublicLayoutComponent {}
