import { Component, input } from '@angular/core';

/**
 * Reusable loading indicator. Contains no business knowledge.
 */
@Component({
  selector: 'app-loading-spinner',
  standalone: true,
  template: `
    <div class="spinner" role="status" [attr.aria-label]="label()">
      <span class="spinner__dot" aria-hidden="true"></span>
      @if (label()) {
        <span class="spinner__label">{{ label() }}</span>
      }
    </div>
  `,
  styles: `
    .spinner {
      display: inline-flex;
      align-items: center;
      gap: 0.75rem;
      color: var(--pa-text-muted, #5b6472);
      font-size: 0.95rem;
    }

    .spinner__dot {
      width: 1rem;
      height: 1rem;
      border-radius: 50%;
      border: 2px solid currentColor;
      border-right-color: transparent;
      animation: pa-spin 0.7s linear infinite;
    }

    @keyframes pa-spin {
      to {
        transform: rotate(360deg);
      }
    }
  `,
})
export class LoadingSpinnerComponent {
  readonly label = input('Loading…');
}
