import { Component, computed, input } from '@angular/core';

export interface TrendPoint {
  readonly calculatedAt: string;
  readonly healthScore: number;
  readonly riskScore: number;
  readonly attentionScore: number;
}

/**
 * Simple multi-series line chart for analytics trends (presentation only).
 */
@Component({
  selector: 'app-trend-chart',
  standalone: true,
  template: `
    <section class="trend">
      <h3>{{ title() }}</h3>
      @if (points().length < 2) {
        <p class="trend__empty">Not enough history for a trend chart yet. Recalculate analytics after syncs.</p>
      } @else {
        <svg viewBox="0 0 400 180" role="img" [attr.aria-label]="title()">
          <polyline [attr.points]="healthPath()" fill="none" stroke="#0f766e" stroke-width="2.5" />
          <polyline [attr.points]="riskPath()" fill="none" stroke="#b91c1c" stroke-width="2.5" />
          <polyline [attr.points]="attentionPath()" fill="none" stroke="#1d4ed8" stroke-width="2.5" />
        </svg>
        <div class="trend__legend">
          <span class="health">Health</span>
          <span class="risk">Risk</span>
          <span class="attention">Attention</span>
        </div>
      }
    </section>
  `,
  styles: `
    .trend {
      padding: 1.1rem;
      border: 1px solid var(--pa-border);
      border-radius: 12px;
      background: var(--pa-surface);
    }
    h3 {
      margin: 0 0 0.75rem;
    }
    svg {
      width: 100%;
      height: auto;
      background: #f8fafc;
      border-radius: 8px;
    }
    .trend__empty {
      margin: 0;
      color: var(--pa-text-muted);
    }
    .trend__legend {
      display: flex;
      gap: 1rem;
      margin-top: 0.65rem;
      font-size: 0.82rem;
    }
    .health {
      color: #0f766e;
      font-weight: 600;
    }
    .risk {
      color: #b91c1c;
      font-weight: 600;
    }
    .attention {
      color: #1d4ed8;
      font-weight: 600;
    }
  `,
})
export class TrendChartComponent {
  readonly title = input('Score trends');
  readonly points = input.required<readonly TrendPoint[]>();

  private readonly series = computed(() => {
    const pts = this.points();
    if (pts.length === 0) {
      return { health: '', risk: '', attention: '' };
    }
    const n = pts.length;
    const map = (selector: (p: TrendPoint) => number) =>
      pts
        .map((p, i) => {
          const x = 20 + (i * 360) / Math.max(n - 1, 1);
          const y = 160 - (selector(p) / 100) * 140;
          return `${x},${y}`;
        })
        .join(' ');
    return {
      health: map((p) => p.healthScore),
      risk: map((p) => p.riskScore),
      attention: map((p) => p.attentionScore),
    };
  });

  healthPath = computed(() => this.series().health);
  riskPath = computed(() => this.series().risk);
  attentionPath = computed(() => this.series().attention);
}
