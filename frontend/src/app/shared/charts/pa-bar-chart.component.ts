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

export interface PaBarDatum {
  readonly name: string;
  readonly value: number;
  /** Explicit series color (semantic); otherwise viz palette */
  readonly color?: string;
  readonly id?: string;
  /** Full hover copy when `name` is abbreviated for axis labels */
  readonly detail?: string;
}

/**
 * ECharts categorical bar chart — PA theme, bright series colors, gradient fills.
 */
@Component({
  selector: 'app-pa-bar-chart',
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
        <div
          echarts
          [options]="options()"
          [autoResize]="true"
          class="pa-bar-host"
          (chartClick)="onChartClick($event)"
        ></div>
      }
      <ng-content select="[shellFooter]" ngProjectAs="[shellFooter]" />
    </app-pa-chart-shell>
  `,
  styles: `
    :host {
      display: block;
      min-width: 0;
    }
    .pa-bar-host {
      width: 100%;
      height: 100%;
    }
  `,
})
export class PaBarChartComponent {
  private readonly themeService = inject(ThemeService);
  private readonly themeTick = signal(0);

  readonly title = input.required<string>();
  readonly subtitle = input<string>('');
  readonly size = input<PaChartSize>('md');
  readonly data = input.required<readonly PaBarDatum[]>();
  readonly emptyMessage = input('No chart data yet.');
  readonly interactive = input(false);
  /** Vertical for category counts; horizontal for factor / long-label breakdowns. */
  readonly orientation = input<'vertical' | 'horizontal'>('vertical');
  /** Tooltip value noun (e.g. Count, Contribution). */
  readonly valueLabel = input('Count');
  /**
   * When false (default), zero-value categories are omitted — right for count distributions.
   * When true, zeros stay — needed for score comparisons (0–100 averages).
   */
  readonly includeZeros = input(false);
  /**
   * Wider y-axis labels for horizontal factor charts (px).
   * Also expands left grid so more of the label is visible before truncate.
   */
  readonly categoryLabelWidth = input<number | null>(null);
  readonly barClick = output<PaBarDatum>();

  constructor() {
    effect(() => {
      this.themeService.theme();
      this.themeTick.update((n) => n + 1);
    });
  }

  readonly empty = computed(() => {
    const data = this.data();
    if (data.length === 0) {
      return true;
    }
    if (this.includeZeros()) {
      return false;
    }
    return data.every((d) => d.value <= 0);
  });

  readonly options = computed(() => {
    this.themeTick();
    return this.buildOptions();
  });

  private visibleRows(): PaBarDatum[] {
    const data = this.data();
    return this.includeZeros() ? [...data] : data.filter((d) => d.value > 0);
  }

  onChartClick(event: { dataIndex?: number; name?: string }): void {
    if (!this.interactive()) {
      return;
    }
    // Chart series may omit zero-value categories — index against the visible set.
    const visible = this.visibleRows();
    const byName = event.name ? visible.find((d) => d.name === event.name) : undefined;
    const byIndex = event.dataIndex != null ? visible[event.dataIndex] : undefined;
    const datum = byName ?? byIndex;
    if (datum) {
      this.barClick.emit(datum);
    }
  }

  private buildOptions(): EChartsCoreOption {
    const tokens = readPaVizTokens();
    const base = paBaseChartOption(tokens);
    const rows = this.visibleRows();

    const colors = rows.map((d, i) => {
      if (d.color && !d.color.startsWith('var(')) {
        return d.color;
      }
      const n = d.name.toLowerCase();
      if (n.includes('critical')) {
        return tokens.danger;
      }
      if (n.includes('watch') || n.includes('high') || n.includes('attention')) {
        return tokens.warning;
      }
      if (n.includes('healthy') || n.includes('stable') || n.includes('ok')) {
        return tokens.success;
      }
      if (n.includes('no overdue')) {
        return tokens.viz[1];
      }
      if (n.includes('overdue')) {
        return tokens.danger;
      }
      if (n.includes('medium')) {
        return tokens.viz[0];
      }
      if (n.includes('unknown')) {
        return tokens.theme === 'dark' ? 'rgba(255,255,255,0.35)' : 'rgba(0,0,0,0.35)';
      }
      return tokens.viz[i % tokens.viz.length];
    });

    const horizontal = this.orientation() === 'horizontal';
    const valueNoun = this.valueLabel();
    const labelWidth = horizontal ? (this.categoryLabelWidth() ?? 110) : undefined;
    const categoryAxis = {
      type: 'category' as const,
      data: rows.map((d) => d.name),
      axisTick: { show: false },
      axisLine: { lineStyle: { color: tokens.borderStrong } },
      axisLabel: {
        color: tokens.textSecondary,
        fontSize: 11,
        fontWeight: 600,
        width: labelWidth,
        overflow: horizontal ? ('truncate' as const) : undefined,
      },
    };
    const valueAxis = {
      type: 'value' as const,
      minInterval: valueNoun === 'Count' ? 1 : undefined,
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: tokens.textTertiary, fontSize: 11 },
      splitLine: {
        lineStyle: { color: tokens.border, type: 'dashed' as const, opacity: 0.55 },
      },
    };

    const baseTooltip = (base['tooltip'] as Record<string, unknown>) ?? {};
    const baseExtra = typeof baseTooltip['extraCssText'] === 'string' ? baseTooltip['extraCssText'] : '';

    return {
      ...base,
      legend: { show: false },
      tooltip: {
        ...baseTooltip,
        trigger: 'axis',
        axisPointer: { type: 'shadow' },
        // Escape chart shell overflow:hidden so long factor copy is fully visible
        appendTo: 'body',
        appendToBody: true,
        confine: false,
        extraCssText: `${baseExtra}; max-width: min(28rem, 90vw); white-space: normal;`,
        formatter: (params: unknown) => {
          const list = Array.isArray(params) ? params : [params];
          const p = list[0] as { name?: string; value?: number; color?: string; dataIndex?: number };
          const row = typeof p.dataIndex === 'number' ? rows[p.dataIndex] : undefined;
          const title = (row?.detail && row.detail.trim()) || p.name || row?.name || '';
          const safeTitle = String(title)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;');
          return `<div style="font-weight:700;margin-bottom:6px;line-height:1.35;max-width:26rem">${safeTitle}</div>
            <span style="display:inline-block;width:8px;height:8px;border-radius:50%;background:${p.color};margin-right:6px"></span>
            ${valueNoun}: <b>${p.value ?? 0}</b>`;
        },
      },
      grid: {
        left: horizontal ? Math.max(120, (labelWidth ?? 110) + 16) : 44,
        right: horizontal ? 48 : 16,
        top: 28,
        bottom: horizontal ? 24 : 32,
      },
      xAxis: horizontal ? valueAxis : categoryAxis,
      yAxis: horizontal ? categoryAxis : valueAxis,
      series: [
        {
          type: 'bar',
          barMaxWidth: horizontal ? 22 : 48,
          cursor: this.interactive() ? 'pointer' : 'default',
          data: rows.map((d, i) => ({
            value: d.value,
            name: d.name,
            itemStyle: {
              color: {
                type: 'linear',
                x: horizontal ? 0 : 0,
                y: 0,
                x2: horizontal ? 1 : 0,
                y2: horizontal ? 0 : 1,
                colorStops: [
                  { offset: 0, color: withAlpha(colors[i], 1) },
                  { offset: 0.45, color: colors[i] },
                  { offset: 1, color: withAlpha(colors[i], tokens.theme === 'dark' ? 0.45 : 0.62) },
                ],
              },
              borderRadius: horizontal ? [4, 10, 10, 4] : [10, 10, 4, 4],
              shadowBlur: tokens.theme === 'dark' ? 8 : 3,
              shadowColor: withAlpha(colors[i], 0.35),
            },
          })),
          label: {
            show: true,
            position: horizontal ? 'right' : 'top',
            color: tokens.text,
            fontWeight: 700,
            fontSize: 12,
          },
          emphasis: {
            focus: 'self',
            itemStyle: {
              shadowBlur: 12,
              shadowColor: withAlpha(tokens.text, 0.25),
            },
          },
        },
      ],
    };
  }
}
