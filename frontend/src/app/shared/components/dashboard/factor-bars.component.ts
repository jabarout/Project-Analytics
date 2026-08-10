import { Component, input } from '@angular/core';

export interface FactorBarItem {
  readonly code: string;
  readonly description: string;
  readonly contribution: number;
}

/**
 * Visual explainability bars for score factors (P4). Presentation only.
 */
@Component({
  selector: 'app-factor-bars',
  standalone: true,
  template: `
    <div class="factors">
      <h4>{{ title() }}</h4>
      @for (factor of factors(); track factor.code) {
        <div class="factors__row">
          <div class="factors__meta">
            <span>{{ factor.description || factor.code }}</span>
            <span>{{ factor.contribution }}</span>
          </div>
          <div class="factors__track">
            <div class="factors__fill" [style.width.%]="barWidth(factor.contribution)"></div>
          </div>
        </div>
      } @empty {
        <p class="factors__empty">No factor breakdown available.</p>
      }
    </div>
  `,
  styles: `
    .factors h4 {
      margin: 0 0 0.65rem;
      font-size: 0.9rem;
    }
    .factors__row {
      margin-bottom: 0.55rem;
    }
    .factors__meta {
      display: flex;
      justify-content: space-between;
      font-size: 0.8rem;
      color: var(--pa-text-muted);
      margin-bottom: 0.2rem;
    }
    .factors__track {
      height: 0.45rem;
      border-radius: 999px;
      background: var(--pa-surface-muted);
      overflow: hidden;
    }
    .factors__fill {
      height: 100%;
      border-radius: 999px;
      background: linear-gradient(90deg, #1d4ed8, #0f766e);
      min-width: 2px;
    }
    .factors__empty {
      margin: 0;
      color: var(--pa-text-muted);
      font-size: 0.85rem;
    }
  `,
})
export class FactorBarsComponent {
  readonly title = input('Contribution factors');
  readonly factors = input<readonly FactorBarItem[]>([]);

  barWidth(contribution: number): number {
    const abs = Math.abs(contribution);
    // Contributions may be weights 0–1 or percentage points — normalize to bar width.
    if (abs <= 1) {
      return Math.min(100, abs * 100);
    }
    return Math.min(100, abs);
  }
}
