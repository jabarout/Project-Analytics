import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

/**
 * Minimal layout for unauthenticated screens.
 */
@Component({
  selector: 'app-auth-layout',
  standalone: true,
  imports: [RouterOutlet],
  template: `
    <div class="auth-shell">
      <div class="auth-shell__panel">
        <header class="auth-shell__header">
          <p class="auth-shell__eyebrow">Project Analytics</p>
          <h1>Secure sign-in</h1>
          <p class="auth-shell__subtitle">
            Decision intelligence for OpenProject. Operational data remains in OpenProject.
          </p>
        </header>
        <router-outlet />
      </div>
    </div>
  `,
  styles: `
    .auth-shell {
      min-height: 100vh;
      display: grid;
      place-items: center;
      padding: 2rem;
      background:
        radial-gradient(circle at top left, rgba(29, 78, 216, 0.12), transparent 40%),
        var(--pa-bg);
    }

    .auth-shell__panel {
      width: min(420px, 100%);
      background: var(--pa-surface);
      border: 1px solid var(--pa-border);
      border-radius: 16px;
      padding: 2rem;
      box-shadow: 0 18px 40px rgba(15, 23, 42, 0.08);
    }

    .auth-shell__header {
      margin-bottom: 1.5rem;
    }

    .auth-shell__eyebrow {
      margin: 0 0 0.35rem;
      text-transform: uppercase;
      letter-spacing: 0.08em;
      font-size: 0.75rem;
      color: var(--pa-text-muted);
    }

    h1 {
      margin: 0 0 0.5rem;
      font-size: 1.5rem;
    }

    .auth-shell__subtitle {
      margin: 0;
      color: var(--pa-text-muted);
      line-height: 1.5;
    }
  `,
})
export class AuthLayoutComponent {}
