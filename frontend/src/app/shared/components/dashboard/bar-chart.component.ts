import { Component, computed, input, output } from '@angular/core';

export interface BarChartDatum {
  readonly label: string;
  readonly value: number;
  readonly color?: string;
  readonly drill?: string;
  readonly healthMin?: number;
  readonly healthMax?: number;
  readonly progressMin?: number;
  readonly progressMax?: number;
}

/**
 * Interactive SVG bar chart. Click emits the datum for Explorer filter wiring.
 */
@Component({
  selector: 'app-bar-chart',
  standalone: true,
  template: `
    <section class="chart">
      <h3>{{ title() }}</h3>
      @if (data().length === 0 || total() === 0) {
        <p class="chart__empty">No chart data yet.</p>
      } @else {
        <svg [attr.viewBox]="'0 0 ' + width + ' ' + height" role="img" [attr.aria-label]="title()">
          @for (bar of bars(); track bar.label; let i = $index) {
            <g
              class="chart__bar"
              [class.chart__bar--clickable]="interactive()"
              (click)="onBarClick(i)"
              (keydown.enter)="onBarClick(i)"
              [attr.tabindex]="interactive() ? 0 : null"
              role="button"
              [attr.aria-label]="bar.label + ': ' + bar.value"
            >
              <rect
                [attr.x]="bar.x"
                [attr.y]="bar.y"
                [attr.width]="bar.w"
                [attr.height]="bar.h"
                [attr.fill]="bar.color"
                rx="4"
              />
              <text [attr.x]="bar.x + bar.w / 2" [attr.y]="height - 8" text-anchor="middle" class="chart__label">
                {{ bar.label }}
              </text>
              <text [attr.x]="bar.x + bar.w / 2" [attr.y]="bar.y - 6" text-anchor="middle" class="chart__value">
                {{ bar.value }}
              </text>
            </g>
          }
        </svg>
        @if (interactive()) {
          <p class="chart__hint">Click a segment to filter in Explorer</p>
        }
      }
    </section>
  `,
  styles: `
    .chart {
      padding: 0.55rem 0.65rem 0.5rem;
      border: 1px solid var(--pa-border);
      border-radius: 10px;
      background: var(--pa-surface);
      max-width: 220px;
    }
    h3 {
      margin: 0 0 0.35rem;
      font-size: 0.72rem;
      font-weight: 650;
      color: var(--pa-text-muted);
      text-transform: uppercase;
      letter-spacing: 0.03em;
    }
    svg {
      width: 100%;
      max-height: 110px;
      height: auto;
      display: block;
    }
    .chart__bar--clickable {
      cursor: pointer;
    }
    .chart__bar--clickable:hover rect {
      opacity: 0.88;
    }
    .chart__label,
    .chart__value {
      font-size: 8px;
      fill: #64748b;
    }
    .chart__value {
      fill: #0f172a;
      font-weight: 600;
    }
    .chart__empty {
      margin: 0;
      font-size: 0.78rem;
      color: var(--pa-text-muted);
    }
    .chart__hint {
      margin: 0.25rem 0 0;
      font-size: 0.65rem;
      color: var(--pa-text-muted);
    }
  `,
})
export class BarChartComponent {
  readonly title = input('Chart');
  readonly data = input.required<readonly BarChartDatum[]>();
  readonly interactive = input(false);
  readonly segmentClick = output<BarChartDatum>();
  /** Compact default footprint for dashboard grids. */
  readonly width = 200;
  readonly height = 120;

  readonly total = computed(() => this.data().reduce((s, d) => s + d.value, 0));

  readonly bars = computed(() => {
    const items = this.data();
    if (items.length === 0) {
      return [];
    }
    const max = Math.max(...items.map((d) => d.value), 1);
    const gap = 10;
    const plotHeight = 72;
    const plotTop = 16;
    const barWidth = (this.width - gap * (items.length + 1)) / items.length;
    return items.map((item, index) => {
      const h = (item.value / max) * plotHeight;
      const x = gap + index * (barWidth + gap);
      const y = plotTop + (plotHeight - h);
      return {
        label: item.label,
        value: Math.round(item.value * 10) / 10,
        x,
        y,
        w: barWidth,
        h: Math.max(h, item.value > 0 ? 2 : 0),
        color: item.color ?? '#1d4ed8',
        source: item,
      };
    });
  });

  onBarClick(index: number): void {
    if (!this.interactive()) {
      return;
    }
    const item = this.data()[index];
    if (item && item.value > 0) {
      this.segmentClick.emit(item);
    }
  }
}
