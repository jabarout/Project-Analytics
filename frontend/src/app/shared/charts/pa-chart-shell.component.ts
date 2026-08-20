import { Component, input } from '@angular/core';
import { PaChartSize, chartHeightCss } from './pa-echarts-theme';

/**
 * Shared chrome around ECharts canvases — title, empty state, fixed height rhythm.
 */
@Component({
  selector: 'app-pa-chart-shell',
  standalone: true,
  template: `
    <section
      class="pa-chart-shell"
      [class.pa-chart-shell--featured]="featured()"
      [style.--pa-chart-shell-h]="heightCss"
    >
      <div class="pa-chart-shell__body">
        <header class="pa-chart-shell__header">
          <div>
            <h3>{{ title() }}</h3>
            @if (subtitle()) {
              <p class="pa-chart-shell__subtitle">{{ subtitle() }}</p>
            }
          </div>
          <ng-content select="[shellActions]" />
        </header>
        @if (empty()) {
          <p class="pa-chart-shell__empty">{{ emptyMessage() }}</p>
        } @else {
          <div class="pa-chart-shell__canvas">
            <ng-content />
          </div>
        }
      </div>
      <footer class="pa-chart-shell__footer">
        <ng-content select="[shellFooter]" />
      </footer>
    </section>
  `,
  styles: `
    :host {
      display: block;
      min-width: 0;
    }
    .pa-chart-shell {
      display: flex;
      flex-direction: column;
      gap: 0;
      padding: 0;
      border: 2px solid var(--pa-border-strong);
      border-radius: var(--pa-radius-lg);
      background:
        linear-gradient(155deg, color-mix(in srgb, var(--pa-surface-muted) 65%, transparent) 0%, transparent 52%),
        linear-gradient(320deg, color-mix(in srgb, var(--pa-surface-strong) 18%, transparent) 0%, transparent 40%),
        var(--pa-surface);
      color: var(--pa-text);
      min-width: 0;
      overflow: hidden;
    }
    .pa-chart-shell--featured {
      background:
        linear-gradient(145deg, color-mix(in srgb, var(--pa-viz-1) 16%, transparent) 0%, transparent 44%),
        linear-gradient(210deg, color-mix(in srgb, var(--pa-viz-2) 12%, transparent) 0%, transparent 52%),
        var(--pa-surface);
      border-color: color-mix(in srgb, var(--pa-viz-1) 32%, var(--pa-border-strong));
    }
    .pa-chart-shell__body {
      display: flex;
      flex-direction: column;
      gap: 0.55rem;
      padding: 1.1rem 1.2rem 1rem;
      min-width: 0;
    }
    .pa-chart-shell__header {
      display: flex;
      justify-content: space-between;
      gap: 0.75rem;
      align-items: flex-start;
    }
    h3 {
      margin: 0;
      font-size: 0.82rem;
      font-weight: 700;
      letter-spacing: 0.02em;
      text-transform: uppercase;
      color: var(--pa-text-secondary);
    }
    .pa-chart-shell__subtitle {
      margin: 0.25rem 0 0;
      font-size: var(--pa-font-xs);
      color: var(--pa-text-tertiary);
      line-height: 1.4;
      font-weight: 500;
      text-transform: none;
      letter-spacing: 0;
      white-space: pre-line;
    }
    .pa-chart-shell__empty {
      margin: 0;
      min-height: var(--pa-chart-shell-h, var(--pa-chart-h-md));
      display: flex;
      align-items: center;
      color: var(--pa-text-secondary);
      font-size: var(--pa-font-sm);
    }
    .pa-chart-shell__canvas {
      width: 100%;
      height: var(--pa-chart-shell-h, var(--pa-chart-h-md));
      min-height: var(--pa-chart-h-sm);
      max-height: var(--pa-chart-h-max);
    }
    .pa-chart-shell__canvas :deep(div[echarts]),
    .pa-chart-shell__canvas :deep(.echarts-container) {
      width: 100% !important;
      height: 100% !important;
    }
    /* Footer strip fused to the card — hidden when no projected content */
    .pa-chart-shell__footer:not(:has(*)) {
      display: none;
    }
    .pa-chart-shell__footer {
      flex-shrink: 0;
      margin: 0;
      padding: 0.7rem 1.1rem 0.8rem;
      border-top: 1px solid var(--pa-border);
      background:
        linear-gradient(
          180deg,
          color-mix(in srgb, var(--pa-surface-muted) 88%, transparent) 0%,
          color-mix(in srgb, var(--pa-surface-strong) 35%, var(--pa-surface-muted)) 100%
        );
    }
  `,
})
export class PaChartShellComponent {
  readonly title = input.required<string>();
  readonly subtitle = input<string>('');
  readonly size = input<PaChartSize>('md');
  readonly empty = input(false);
  readonly emptyMessage = input('Not enough data to chart yet.');
  /** Soft viz-tinted gradient surface for hero charts */
  readonly featured = input(false);

  get heightCss(): string {
    return chartHeightCss(this.size());
  }
}
