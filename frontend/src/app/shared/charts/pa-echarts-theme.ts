import type { EChartsCoreOption } from 'echarts/core';

export type PaChartSize = 'sm' | 'md' | 'lg';

export interface PaVizTokens {
  readonly theme: 'light' | 'dark';
  readonly bg: string;
  readonly surface: string;
  readonly surfaceMuted: string;
  readonly border: string;
  readonly borderStrong: string;
  readonly text: string;
  readonly textSecondary: string;
  readonly textTertiary: string;
  readonly success: string;
  readonly warning: string;
  readonly danger: string;
  readonly viz: readonly [string, string, string, string, string];
  readonly vizUp: string;
  readonly vizDown: string;
  readonly reducedMotion: boolean;
}

function cssVar(name: string, fallback = ''): string {
  if (typeof document === 'undefined') {
    return fallback;
  }
  const value = getComputedStyle(document.documentElement).getPropertyValue(name).trim();
  return value || fallback;
}

/** Read live PA design tokens for ECharts option factories. */
export function readPaVizTokens(): PaVizTokens {
  const themeAttr = document.documentElement.getAttribute('data-theme');
  const theme: 'light' | 'dark' = themeAttr === 'light' ? 'light' : 'dark';
  const reducedMotion =
    typeof window !== 'undefined' && window.matchMedia('(prefers-reduced-motion: reduce)').matches;

  return {
    theme,
    bg: cssVar('--pa-bg', theme === 'dark' ? '#000' : '#e4e4ea'),
    surface: cssVar('--pa-surface', theme === 'dark' ? '#1a1a1f' : '#fff'),
    surfaceMuted: cssVar('--pa-surface-muted', theme === 'dark' ? '#26262e' : '#ececf2'),
    border: cssVar('--pa-border', theme === 'dark' ? 'rgba(255,255,255,0.18)' : '#b4b6c0'),
    borderStrong: cssVar('--pa-border-strong', theme === 'dark' ? 'rgba(255,255,255,0.34)' : '#7a7d8a'),
    text: cssVar('--pa-text', theme === 'dark' ? '#fff' : '#050507'),
    textSecondary: cssVar('--pa-text-secondary', theme === 'dark' ? 'rgba(255,255,255,0.88)' : '#2e3038'),
    textTertiary: cssVar('--pa-text-tertiary', theme === 'dark' ? 'rgba(255,255,255,0.64)' : '#555866'),
    success: cssVar('--pa-success', '#12b886'),
    warning: cssVar('--pa-warning', '#f59f00'),
    danger: cssVar('--pa-danger', '#fa5252'),
    viz: [
      cssVar('--pa-viz-1', '#2f6bff'),
      cssVar('--pa-viz-2', '#12b886'),
      cssVar('--pa-viz-3', '#f59f00'),
      cssVar('--pa-viz-4', '#e64980'),
      cssVar('--pa-viz-5', '#7048e8'),
    ],
    vizUp: cssVar('--pa-viz-up', '#12b886'),
    vizDown: cssVar('--pa-viz-down', '#fa5252'),
    reducedMotion,
  };
}

export function chartHeightCss(size: PaChartSize): string {
  switch (size) {
    case 'sm':
      return 'var(--pa-chart-h-sm)';
    case 'lg':
      return 'var(--pa-chart-h-lg)';
    default:
      return 'var(--pa-chart-h-md)';
  }
}

/** Shared chrome defaults — never leave ECharts stock palette/axis styling. */
export function paBaseChartOption(tokens: PaVizTokens): EChartsCoreOption {
  const animDuration = tokens.reducedMotion ? 0 : 650;
  const animUpdate = tokens.reducedMotion ? 0 : 420;

  return {
    color: [...tokens.viz],
    backgroundColor: 'transparent',
    textStyle: {
      color: tokens.textSecondary,
      fontFamily: 'Inter, Segoe UI, Roboto, Helvetica, Arial, sans-serif',
      fontSize: 12,
    },
    animation: !tokens.reducedMotion,
    animationDuration: animDuration,
    animationDurationUpdate: animUpdate,
    animationEasing: 'cubicOut',
    animationEasingUpdate: 'cubicInOut',
    grid: {
      left: 48,
      right: 20,
      top: 36,
      bottom: 36,
      containLabel: false,
    },
    legend: {
      top: 0,
      left: 0,
      icon: 'circle',
      itemWidth: 8,
      itemHeight: 8,
      textStyle: {
        color: tokens.textSecondary,
        fontSize: 12,
        fontWeight: 560,
      },
    },
    tooltip: {
      trigger: 'axis',
      backgroundColor: tokens.surface,
      borderColor: tokens.borderStrong,
      borderWidth: 1,
      padding: [10, 12],
      textStyle: {
        color: tokens.text,
        fontSize: 12,
      },
      extraCssText: `box-shadow: 0 10px 28px rgba(0,0,0,${tokens.theme === 'dark' ? 0.45 : 0.12}); border-radius: 10px;`,
      axisPointer: {
        type: 'cross',
        crossStyle: {
          color: tokens.textTertiary,
          width: 1,
          type: 'dashed',
        },
        lineStyle: {
          color: tokens.borderStrong,
          width: 1,
          type: 'dashed',
        },
        label: {
          backgroundColor: tokens.surfaceMuted,
          color: tokens.text,
          borderColor: tokens.border,
          borderWidth: 1,
        },
      },
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      axisLine: { lineStyle: { color: tokens.borderStrong } },
      axisTick: { show: false },
      axisLabel: {
        color: tokens.textTertiary,
        fontSize: 11,
        hideOverlap: true,
      },
      splitLine: { show: false },
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: {
        color: tokens.textTertiary,
        fontSize: 11,
      },
      splitLine: {
        show: true,
        lineStyle: {
          color: tokens.border,
          type: 'dashed',
          opacity: tokens.theme === 'dark' ? 0.45 : 0.7,
        },
      },
    },
  };
}

/** Hex/rgb color with alpha for gradients and glow (expects #rrggbb). */
export function withAlpha(color: string, alpha: number): string {
  const hex = color.trim();
  if (hex.startsWith('#') && (hex.length === 7 || hex.length === 4)) {
    const full =
      hex.length === 4
        ? `#${hex[1]}${hex[1]}${hex[2]}${hex[2]}${hex[3]}${hex[3]}`
        : hex;
    const r = parseInt(full.slice(1, 3), 16);
    const g = parseInt(full.slice(3, 5), 16);
    const b = parseInt(full.slice(5, 7), 16);
    return `rgba(${r}, ${g}, ${b}, ${alpha})`;
  }
  if (hex.startsWith('rgb(')) {
    return hex.replace('rgb(', 'rgba(').replace(')', `, ${alpha})`);
  }
  return color;
}
