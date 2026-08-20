import { Component, input } from '@angular/core';

/**
 * Generic empty-state presentation component.
 */
@Component({
  selector: 'app-empty-state',
  standalone: true,
  template: `
    <div class="empty-state pa-enter">
      <h2 class="empty-state__title">{{ title() }}</h2>
      @if (message()) {
        <p class="empty-state__message">{{ message() }}</p>
      }
    </div>
  `,
  styles: `
    .empty-state {
      padding: 2rem;
      border-radius: var(--pa-radius-lg);
      background: var(--pa-surface-muted);
      border: 2px solid var(--pa-border-strong);
      color: var(--pa-text);
    }

    .empty-state__title {
      margin: 0 0 0.5rem;
      font-size: var(--pa-font-lg, 1.125rem);
      font-weight: var(--pa-weight-semibold, 650);
      color: var(--pa-text);
    }

    .empty-state__message {
      margin: 0;
      color: var(--pa-text-secondary);
      line-height: var(--pa-leading-normal, 1.5);
    }
  `,
})
export class EmptyStateComponent {
  readonly title = input.required<string>();
  readonly message = input<string>('');
}
