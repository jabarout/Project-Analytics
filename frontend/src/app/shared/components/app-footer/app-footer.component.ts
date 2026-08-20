import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { BrandLogoComponent } from '../brand-logo/brand-logo.component';

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [RouterLink, BrandLogoComponent],
  template: `
    <footer class="pa-footer">
      <div class="pa-footer__inner">
        <div class="pa-footer__brand">
          <app-brand-logo size="sm" />
          <span>Decision intelligence for OpenProject</span>
        </div>
        <nav class="pa-footer__links" aria-label="Legal and support">
          <a routerLink="/privacy">Privacy</a>
          <a routerLink="/terms">Terms of use</a>
          <a routerLink="/contact">Contact</a>
        </nav>
        <p class="pa-footer__copy">© {{ year }} Project Analytics</p>
      </div>
    </footer>
  `,
  styles: `
    .pa-footer {
      margin-top: auto;
      border-top: 1px solid var(--pa-border);
      background: var(--pa-surface);
      color: var(--pa-text-secondary);
    }
    .pa-footer__inner {
      display: flex;
      flex-wrap: wrap;
      align-items: center;
      justify-content: space-between;
      gap: 1rem 1.5rem;
      padding: 1.25rem 2rem;
      max-width: 1400px;
      margin: 0 auto;
    }
    .pa-footer__brand {
      display: flex;
      flex-direction: column;
      gap: 0.35rem;
      align-items: flex-start;
    }
    .pa-footer__brand span {
      font-size: 0.85rem;
      color: var(--pa-text-tertiary);
    }
    .pa-footer__links {
      display: flex;
      flex-wrap: wrap;
      gap: 1rem 1.35rem;
    }
    .pa-footer__links a {
      color: var(--pa-text-secondary);
      text-decoration: none;
      font-size: 0.9rem;
      font-weight: 500;
      transition: color var(--pa-motion-fast, 120ms) var(--pa-ease, ease);
    }
    .pa-footer__links a:hover {
      color: var(--pa-text);
      text-decoration: underline;
    }
    @media (prefers-reduced-motion: reduce) {
      .pa-footer__links a {
        transition: none;
      }
    }
    .pa-footer__copy {
      margin: 0;
      font-size: 0.8rem;
      color: var(--pa-text-tertiary);
    }
    @media (max-width: 700px) {
      .pa-footer__inner {
        flex-direction: column;
        align-items: flex-start;
        padding: 1.1rem 1.25rem;
      }
    }
  `,
})
export class AppFooterComponent {
  readonly year = new Date().getFullYear();
}
