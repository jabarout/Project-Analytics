import { Component, input } from '@angular/core';

/**
 * Generic empty-state presentation component.
 */
@Component({
  selector: 'app-empty-state',
  standalone: true,
  template: `
    <div class="empty-state">
      <h2 class="empty-state__title">{{ title() }}</h2>
      @if (message()) {
        <p class="empty-state__message">{{ message() }}</p>
      }
    </div>
  `,
  styles: `
    .empty-state {
      padding: 2rem;
      border-radius: 12px;
      background: var(--pa-surface-muted, #f4f6f8);
      border: 1px solid var(--pa-border, #e2e8f0);
    }

    .empty-state__title {
      margin: 0 0 0.5rem;
      font-size: 1.15rem;
      font-weight: 600;
    }

    .empty-state__message {
      margin: 0;
      color: var(--pa-text-muted, #5b6472);
    }
  `,
})
export class EmptyStateComponent {
  readonly title = input.required<string>();
  readonly message = input<string>('');
}
