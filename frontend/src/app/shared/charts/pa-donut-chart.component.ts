import { Component, computed, effect, inject, input, output, signal } from '@angular/core';
import { NgxEchartsDirective } from 'ngx-echarts';
import type { EChartsCoreOption } from 'echarts/core';
import { ThemeService } from '../../core/services/theme.service';
import { PaChartShellComponent } from './pa-chart-shell.component';
import {
  PaChartSize,
  paBaseChartOption,
  readPaVizTokens,
  withAlpha,
} from './pa-echarts-theme';

export interface PaDonutSlice {
  readonly name: string;
  readonly value: number;
  readonly color?: string;
  readonly id?: string;
}

/**
 * ECharts donut / part-to-whole.
 * Pie is centered; hole label is an HTML overlay at 50%/50% (same origin as series.center).
 * Legend sits below so geometry stays aligned on resize.
 */
@Component({
  selector: 'app-pa-donut-chart',
  standalone: true,
  imports: [NgxEchartsDirective, PaChartShellComponent],
  template: `
    <app-pa-chart-shell
      [title]="title()"
      [subtitle]="subtitle()"
      [size]="size()"
      [empty]="empty()"
      [emptyMessage]="emptyMessage()"
    >
      @if (!empty()) {
        <div class="pa-donut-host">
          <div
            echarts
            [options]="options()"
            [autoResize]="true"
            class="pa-donut-host__chart"
            (chartClick)="onClick($event)"
          ></div>
          <div class="pa-donut-center" aria-hidden="true">
            <span class="pa-donut-center__value">{{ centerDisplay() }}</span>
            <span class="pa-donut-center__label">{{ centerLabel() }}</span>
          </div>
        </div>
      }
      <ng-content select="[shellFooter]" ngProjectAs="[shellFooter]" />
    </app-pa-chart-shell>
  `,
  styles: `
    :host {
      display: block;
      min-width: 0;
    }
    .pa-donut-host {
      position: relative;
      width: 100%;
      height: 100%;
    }
    .pa-donut-host__chart {
      width: 100%;
      height: 100%;
    }
    /*
     * Overlay locked to pie series.center ['50%','46%'] — slightly above true middle
     * so the hole sits above the bottom legend band.
     */
    .pa-donut-center {
      position: absolute;
      left: 50%;
      top: 46%;
      transform: translate(-50%, -50%);
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      text-align: center;
      pointer-events: none;
      z-index: 5;
      max-width: 40%;
      line-height: 1.15;
    }
    .pa-donut-center__value {
      font-size: clamp(1.15rem, 2.8vw, 1.45rem);
      font-weight: 750;
      letter-spacing: -0.03em;
      color: var(--pa-text);
      font-variant-numeric: tabular-nums;
    }
    .pa-donut-center__label {
      margin-top: 0.12rem;
      font-size: 0.68rem;
      font-weight: 600;
      color: var(--pa-text-tertiary);
    }
  `,
})
export class PaDonutChartComponent {
  private readonly themeService = inject(ThemeService);
  private readonly themeTick = signal(0);

  readonly title = input.required<string>();
  readonly subtitle = input<string>('');
  readonly size = input<PaChartSize>('md');
  readonly data = input.required<readonly PaDonutSlice[]>();
  readonly centerLabel = input('Total');
  readonly centerValue = input<string | number | null>(null);
  readonly emptyMessage = input('No chart data yet.');
  readonly interactive = input(false);
  readonly sliceClick = output<PaDonutSlice>();

  constructor() {
    effect(() => {
      this.themeService.theme();
      this.themeTick.update((n) => n + 1);
    });
  }

  readonly empty = computed(() => {
    const data = this.data();
    return data.length === 0 || data.every((d) => d.value <= 0);
  });

  readonly centerDisplay = computed(() => {
    const override = this.centerValue();
    if (override != null && override !== '') {
      return String(override);
    }
    const sum = this.data()
      .filter((d) => d.value > 0)
      .reduce((s, d) => s + d.value, 0);
    return String(Math.round(sum * 10) / 10);
  });

  readonly options = computed(() => {
    this.themeTick();
    return this.buildOptions();
  });

  onClick(event: { dataIndex?: number }): void {
    if (!this.interactive()) {
      return;
    }
    const idx = event.dataIndex;
    if (idx == null) {
      return;
    }
    const slice = this.data().filter((d) => d.value > 0)[idx];
    if (slice) {
      this.sliceClick.emit(slice);
    }
  }

  private buildOptions(): EChartsCoreOption {
    const tokens = readPaVizTokens();
    const base = paBaseChartOption(tokens);
    const rows = this.data().filter((d) => d.value > 0);

    const resolveColor = (d: PaDonutSlice, i: number): string => {
      if (d.color) {
        return d.color;
      }
      const n = d.name.toLowerCase();
      if (n.includes('critical')) {
        return tokens.danger;
      }
      if (n.includes('watch') || n.includes('attention') || n.includes('high')) {
        return tokens.warning;
      }
      if (n.includes('healthy') || n.includes('stable') || n.includes('ok') || n.includes('complete')) {
        return tokens.success;
      }
      if (n.includes('remaining')) {
        return tokens.theme === 'dark' ? 'rgba(255,255,255,0.18)' : 'rgba(0,0,0,0.12)';
      }
      return tokens.viz[i % tokens.viz.length];
    };

    return {
      ...base,
      xAxis: undefined,
      yAxis: undefined,
      grid: undefined,
      legend: {
        orient: 'horizontal',
        bottom: 0,
        left: 'center',
        icon: 'circle',
        itemWidth: 8,
        itemHeight: 8,
        itemGap: 12,
        textStyle: { color: tokens.textSecondary, fontSize: 11, fontWeight: 600 },
      },
      tooltip: {
        trigger: 'item',
        backgroundColor: tokens.surface,
        borderColor: tokens.borderStrong,
        borderWidth: 1,
        textStyle: { color: tokens.text },
        extraCssText: `box-shadow: 0 10px 28px rgba(0,0,0,${tokens.theme === 'dark' ? 0.45 : 0.12}); border-radius: 10px;`,
        formatter: (p: { name?: string; value?: number; percent?: number }) =>
          `<b>${p.name}</b><br/>${p.value} (${p.percent}%)`,
      },
      series: [
        {
          type: 'pie',
          radius: ['40%', '62%'],
          center: ['50%', '46%'],
          padAngle: 2.5,
          itemStyle: {
            borderRadius: 6,
            borderColor: tokens.surface,
            borderWidth: 2,
          },
          label: { show: false },
          labelLine: { show: false },
          data: rows.map((d, i) => {
            const color = resolveColor(d, i);
            return {
              name: d.name,
              value: d.value,
              itemStyle: {
                color: {
                  type: 'linear',
                  x: 0,
                  y: 0,
                  x2: 1,
                  y2: 1,
                  colorStops: [
                    { offset: 0, color },
                    { offset: 1, color: withAlpha(color, tokens.theme === 'dark' ? 0.62 : 0.78) },
                  ],
                },
                shadowBlur: tokens.theme === 'dark' ? 10 : 4,
                shadowColor: withAlpha(color, 0.4),
              },
            };
          }),
          emphasis: {
            scale: true,
            scaleSize: 6,
            focus: 'self',
            itemStyle: {
              shadowBlur: 16,
              shadowColor: withAlpha(tokens.text, 0.3),
            },
          },
          blur: {
            itemStyle: { opacity: 0.45 },
          },
        },
      ],
    };
  }
}
