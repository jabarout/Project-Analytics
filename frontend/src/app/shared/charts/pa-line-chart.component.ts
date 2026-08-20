import {
  Component,
  computed,
  effect,
  inject,
  input,
  signal,
} from '@angular/core';
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

export interface PaLineSeries {
  readonly id: string;
  readonly name: string;
  /** Y values aligned to shared categories; null = gap */
  readonly values: readonly (number | null)[];
  /** Soft luminous stroke (Home hero only — selective) */
  readonly luminous?: boolean;
  /** Optional explicit series color (hex/rgb); otherwise viz palette */
  readonly color?: string;
}

/**
 * Modern smooth multi-series line/area chart using the PA ECharts theme.
 * Emphasizes latest values + rich tooltips without labeling every point.
 */
@Component({
  selector: 'app-pa-line-chart',
  standalone: true,
  imports: [NgxEchartsDirective, PaChartShellComponent],
  template: `
    <app-pa-chart-shell
      [title]="title()"
      [subtitle]="effectiveSubtitle()"
      [size]="size()"
      [empty]="empty()"
      [emptyMessage]="emptyMessage()"
      [featured]="featured()"
    >
      @if (!empty()) {
        <div echarts [options]="options()" [autoResize]="true" class="pa-line-host"></div>
      }
    </app-pa-chart-shell>
  `,
  styles: `
    :host {
      display: block;
      min-width: 0;
    }
    .pa-line-host {
      width: 100%;
      height: 100%;
      min-height: inherit;
    }
  `,
})
export class PaLineChartComponent {
  private readonly themeService = inject(ThemeService);
  private readonly themeTick = signal(0);

  readonly title = input.required<string>();
  readonly subtitle = input<string>('');
  readonly size = input<PaChartSize>('lg');
  readonly categories = input.required<readonly string[]>();
  readonly series = input.required<readonly PaLineSeries[]>();
  readonly yMin = input<number | null>(0);
  readonly yMax = input<number | null>(100);
  readonly yName = input<string>('');
  readonly metricLabel = input('Health score');
  /** Optional per-category sample sizes (e.g. projects in Average Health wave). */
  readonly sampleSizes = input<readonly number[]>([]);
  /** Append per-series first→last deltas under the subtitle (disable when caller supplies its own). */
  readonly autoTrendSummary = input(true);
  readonly featured = input(false);
  readonly emptyMessage = input('Not enough history for a trend chart yet.');

  constructor() {
    effect(() => {
      this.themeService.theme();
      this.themeTick.update((n) => n + 1);
    });
  }

  readonly empty = computed(() => {
    const cats = this.categories();
    const series = this.series();
    if (cats.length < 2 || series.length === 0) {
      return true;
    }
    return !series.some((s) => s.values.filter((v) => v != null).length >= 2);
  });

  /** Subtitle + optional trend direction summary from first→last real values. */
  readonly effectiveSubtitle = computed(() => {
    const base = this.subtitle();
    if (!this.autoTrendSummary()) {
      return base;
    }
    const directions = this.series()
      .map((s) => {
        const nums = s.values.map((v, i) => ({ v, i })).filter((x) => x.v != null) as {
          v: number;
          i: number;
        }[];
        if (nums.length < 2) {
          return null;
        }
        const first = nums[0].v;
        const last = nums[nums.length - 1].v;
        const delta = Math.round((last - first) * 10) / 10;
        const arrow = delta > 0.5 ? '↑' : delta < -0.5 ? '↓' : '→';
        const sign = delta > 0 ? `+${delta}` : `${delta}`;
        return `${s.name} ${arrow} ${sign}`;
      })
      .filter(Boolean);
    if (!directions.length) {
      return base;
    }
    const trendLine = directions.join(' · ');
    return base ? `${base}\n${trendLine}` : trendLine;
  });

  readonly options = computed(() => {
    this.themeTick();
    return this.buildOptions();
  });

  private buildOptions(): EChartsCoreOption {
    const tokens = readPaVizTokens();
    const base = paBaseChartOption(tokens);
    const categories = [...this.categories()];
    const seriesInputs = this.series();
    const metric = this.metricLabel();

    const series = seriesInputs.map((s, index) => {
      const color =
        s.color && !s.color.startsWith('var(') ? s.color : tokens.viz[index % tokens.viz.length];
      const luminous = !!s.luminous && !tokens.reducedMotion;
      const lastIdx = this.lastDefinedIndex(s.values);

      return {
        name: s.name,
        type: 'line' as const,
        smooth: 0.4,
        symbol: 'circle',
        symbolSize: 8,
        showSymbol: true,
        sampling: 'lttb' as const,
        data: s.values.map((v) => v),
        lineStyle: {
          width: luminous ? 3.5 : 3,
          color,
          shadowBlur: luminous ? (tokens.theme === 'dark' ? 18 : 10) : tokens.theme === 'dark' ? 8 : 0,
          shadowColor: withAlpha(color, tokens.theme === 'dark' ? 0.65 : 0.28),
        },
        itemStyle: {
          color,
          borderColor: tokens.surface,
          borderWidth: 2,
        },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: withAlpha(color, tokens.theme === 'dark' ? 0.5 : 0.34) },
              { offset: 0.4, color: withAlpha(color, tokens.theme === 'dark' ? 0.22 : 0.16) },
              { offset: 1, color: withAlpha(color, 0.02) },
            ],
          },
        },
        // Only label the latest point — avoids clutter
        label: {
          show: true,
          position: 'top',
          distance: 6,
          color: tokens.text,
          fontWeight: 750,
          fontSize: 12,
          formatter: (params: { dataIndex: number; value: number | null }) => {
            if (params.dataIndex !== lastIdx || params.value == null) {
              return '';
            }
            return `${Math.round(Number(params.value) * 10) / 10}`;
          },
        },
        endLabel: {
          show: lastIdx >= 0,
          formatter: (params: { value?: number }) => {
            if (params.value == null) {
              return '';
            }
            return String(Math.round(Number(params.value) * 10) / 10);
          },
          color,
          fontWeight: 750,
          fontSize: 12,
          distance: 10,
        },
        markPoint:
          lastIdx >= 0
            ? {
                symbol: 'circle',
                symbolSize: 12,
                data: [
                  {
                    name: 'Latest',
                    coord: [lastIdx, s.values[lastIdx] as number],
                    value: s.values[lastIdx] as number,
                    itemStyle: {
                      color,
                      borderColor: tokens.text,
                      borderWidth: 2,
                      shadowBlur: 10,
                      shadowColor: withAlpha(color, 0.55),
                    },
                    label: { show: false },
                  },
                ],
              }
            : undefined,
        emphasis: {
          focus: 'series' as const,
          scale: true,
          lineStyle: { width: 4 },
          itemStyle: {
            borderWidth: 3,
            shadowBlur: 12,
            shadowColor: withAlpha(color, 0.5),
          },
        },
        blur: {
          lineStyle: { opacity: 0.12 },
          areaStyle: { opacity: 0.04 },
        },
      };
    });

    return {
      ...base,
      color: [...tokens.viz],
      legend: {
        show: true,
        top: 0,
        left: 0,
        icon: 'roundRect',
        itemWidth: 14,
        itemHeight: 8,
        itemGap: 14,
        textStyle: {
          color: tokens.text,
          fontSize: 12,
          fontWeight: 650,
        },
        data: seriesInputs.map((s) => s.name),
      },
      tooltip: {
        trigger: 'axis',
        backgroundColor: tokens.surface,
        borderColor: tokens.borderStrong,
        borderWidth: 1,
        padding: [12, 14],
        textStyle: { color: tokens.text, fontSize: 12 },
        extraCssText: `box-shadow: 0 12px 32px rgba(0,0,0,${tokens.theme === 'dark' ? 0.5 : 0.14}); border-radius: 12px;`,
        axisPointer: {
          type: 'cross',
          crossStyle: { color: tokens.textTertiary, width: 1, type: 'dashed' },
          lineStyle: { color: tokens.borderStrong, width: 1, type: 'dashed' },
          label: {
            backgroundColor: tokens.surfaceMuted,
            color: tokens.text,
            borderColor: tokens.border,
            borderWidth: 1,
          },
        },
        formatter: (params: unknown) => {
          const list = (Array.isArray(params) ? params : [params]) as Array<{
            axisValue?: string;
            seriesName?: string;
            value?: number | null;
            color?: string;
            dataIndex?: number;
          }>;
          if (!list.length) {
            return '';
          }
          const when = list[0].axisValue ?? '';
          const idx = list[0].dataIndex ?? 0;
          const samples = this.sampleSizes();
          const n = samples[idx];
          const sampleLine =
            n != null
              ? `<div style="font-size:11px;color:${tokens.textTertiary};margin-top:4px">${n} project${n === 1 ? '' : 's'} in wave</div>`
              : '';
          const rows = list
            .filter((p) => p.value != null)
            .map((p) => {
              const v = Math.round(Number(p.value) * 10) / 10;
              return `<div style="display:flex;align-items:center;gap:8px;margin-top:6px">
                <span style="width:10px;height:10px;border-radius:50%;background:${p.color};box-shadow:0 0 8px ${p.color}"></span>
                <span style="flex:1;font-weight:600">${p.seriesName}</span>
                <span style="font-variant-numeric:tabular-nums;font-weight:750">${v}</span>
              </div>`;
            })
            .join('');
          return `<div style="font-size:11px;color:${tokens.textTertiary};margin-bottom:2px">${metric}</div>
            <div style="font-weight:700;margin-bottom:2px">${when}</div>${rows}${sampleLine}`;
        },
      },
      grid: {
        left: 52,
        right: 88,
        top: 44,
        bottom: categories.length > 6 ? 58 : 40,
        containLabel: false,
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: categories,
        axisTick: { show: false },
        axisLine: { lineStyle: { color: tokens.borderStrong, width: 1.5 } },
        axisLabel: {
          color: tokens.textSecondary,
          fontSize: 10,
          fontWeight: 560,
          hideOverlap: true,
          rotate: categories.length > 6 ? 32 : 0,
          interval: categories.length > 10 ? 'auto' : 0,
        },
        splitLine: { show: false },
      },
      yAxis: {
        type: 'value',
        min: this.yMin() ?? undefined,
        max: this.yMax() ?? undefined,
        name: this.yName() || metric,
        nameLocation: 'middle',
        nameGap: 36,
        nameTextStyle: {
          color: tokens.textSecondary,
          fontSize: 11,
          fontWeight: 650,
        },
        axisLine: { show: false },
        axisTick: { show: false },
        axisLabel: {
          color: tokens.textSecondary,
          fontSize: 11,
          fontWeight: 560,
          formatter: (v: number) => `${v}`,
        },
        splitLine: {
          show: true,
          lineStyle: {
            color: tokens.border,
            type: 'dashed',
            opacity: tokens.theme === 'dark' ? 0.5 : 0.75,
          },
        },
      },
      series,
    };
  }

  private lastDefinedIndex(values: readonly (number | null)[]): number {
    for (let i = values.length - 1; i >= 0; i--) {
      if (values[i] != null) {
        return i;
      }
    }
    return -1;
  }
}
