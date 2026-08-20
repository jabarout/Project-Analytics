import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';

/**
 * Public OAuth return target. Must not sit behind authGuard: the OpenProject
 * popup/return window often has no PA JWT, so /workspaces would bounce to /login.
 */
@Component({
  selector: 'app-oauth-complete-page',
  standalone: true,
  imports: [RouterLink],
  template: `
    <article class="oauth-complete">
      <h1>{{ heading() }}</h1>
      <p class="oauth-complete__message">{{ message() }}</p>
      @if (notifiedOpener()) {
        <p class="oauth-complete__hint">This window can be closed.</p>
      } @else {
        <p class="oauth-complete__hint">
          Return to the Project Analytics tab that started this connection, or continue here.
          You can close this window.
        </p>
        <p>
          <a routerLink="/workspaces" class="pa-btn pa-btn--primary">Continue to Connections</a>
        </p>
      }
    </article>
  `,
  styles: `
    .oauth-complete h1 {
      margin: 0 0 0.75rem;
      font-size: 1.45rem;
      letter-spacing: -0.02em;
    }
    .oauth-complete__message,
    .oauth-complete__hint {
      margin: 0 0 0.85rem;
      line-height: 1.5;
      color: var(--pa-text-secondary);
    }
    .oauth-complete__message {
      color: var(--pa-text);
    }
  `,
})
export class OAuthCompletePage implements OnInit {
  private readonly route = inject(ActivatedRoute);

  readonly heading = signal('OpenProject connection');
  readonly message = signal('');
  readonly notifiedOpener = signal(false);

  ngOnInit(): void {
    const params = this.route.snapshot.queryParamMap;
    const oauth = params.get('oauth');
    if (oauth !== 'success' && oauth !== 'error') {
      this.heading.set('OpenProject connection');
      this.message.set(
        'OpenProject OAuth did not return a recognizable result. You can close this window and return to Project Analytics.'
      );
      return;
    }

    const message =
      oauth === 'success'
        ? 'Connected via OpenProject OAuth. Eligibility verified — you are Workspace Admin. Synchronize to load data.'
        : (params.get('message') ?? 'OpenProject OAuth connect failed.');

    this.heading.set(oauth === 'success' ? 'Connected to OpenProject' : 'OpenProject connection failed');
    this.message.set(message);

    if (window.opener && !window.opener.closed) {
      try {
        window.opener.postMessage(
          {
            source: 'pa-openproject-oauth',
            oauth,
            message,
          },
          window.location.origin
        );
      } catch {
        // Cross-window messaging can fail; opener still polls popup close.
      }
      this.notifiedOpener.set(true);
      window.close();
    }
  }
}
