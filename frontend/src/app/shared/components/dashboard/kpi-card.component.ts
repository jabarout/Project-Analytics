import { Component, input, output } from '@angular/core';
import { SeverityBand } from '../../analytics/analytics-thresholds';

export type DeltaDirection = 'up' | 'down' | 'flat' | null;

/**
 * Reusable management KPI card with optional literacy tooltip and drill-down.
 * Layout is overflow-safe at desktop and narrow widths.
 */
@Component({
  selector: 'app-kpi-card',
  standalone: true,
  template: `
    <article
      class="kpi-card"
      [class.kpi-card--accent]="accent()"
      [class.kpi-card--clickable]="clickable()"
      [class.kpi-card--critical]="severity() === 'critical'"
      [class.kpi-card--watch]="severity() === 'watch'"
      [class.kpi-card--positive]="severity() === 'positive'"
      [class.kpi-card--delta-up]="deltaDirection() === 'up'"
      [class.kpi-card--delta-down]="deltaDirection() === 'down'"
      [attr.role]="clickable() ? 'button' : null"
      [attr.tabindex]="clickable() ? 0 : null"
      (click)="onActivate($event)"
      (keydown.enter)="onActivate($event)"
      (keydown.space)="onActivate($event); $event.preventDefault()"
    >
      <div class="kpi-card__top">
        <h3 [attr.title]="label()">
          <span class="kpi-card__label">{{ label() }}</span>
          @if (infoTitle()) {
            <span
              class="kpi-card__info"
              [attr.title]="infoTitle() + ' — ' + infoBody()"
              [attr.aria-label]="infoTitle() + '. ' + infoBody()"
              (click)="$event.stopPropagation()"
            >
              i
            </span>
          }
        </h3>
        @if (kind() === 'count') {
          <span class="kpi-card__kind">Count</span>
        } @else if (kind() === 'score') {
          <span class="kpi-card__kind">Score</span>
        } @else if (kind() === 'volume') {
          <span class="kpi-card__kind">Volume</span>
        }
      </div>

      <div class="kpi-card__value-row">
        <p class="kpi-card__value">{{ value() }}</p>
        @if (deltaDirection()) {
          <span
            class="kpi-card__delta"
            [class.kpi-card__delta--up]="deltaDirection() === 'up'"
            [class.kpi-card__delta--down]="deltaDirection() === 'down'"
            [class.kpi-card__delta--flat]="deltaDirection() === 'flat'"
            [attr.aria-label]="deltaLabel() || 'change'"
          >
            @if (deltaDirection() === 'up') {
              ↑
            } @else if (deltaDirection() === 'down') {
              ↓
            } @else {
              →
            }
            @if (deltaLabel()) {
              <span>{{ deltaLabel() }}</span>
            }
          </span>
        }
      </div>

      @if (hint()) {
        <p class="kpi-card__hint" [attr.title]="hint()">{{ hint() }}</p>
      }
      @if (infoBody() && showInfoExpanded()) {
        <p class="kpi-card__explain">{{ infoBody() }}</p>
      }
      @if (actionLabel()) {
        <p class="kpi-card__action">{{ actionLabel() }}</p>
      }
    </article>
  `,
  styles: `
    .kpi-card {
      padding: 1rem 1.05rem;
      border-radius: var(--pa-radius-lg, 18px);
      border: 2px solid var(--pa-border-strong);
      background:
        linear-gradient(165deg, color-mix(in srgb, var(--pa-surface-muted) 55%, transparent) 0%, transparent 42%),
        var(--pa-surface);
      min-height: 7.25rem;
      min-width: 0;
      display: flex;
      flex-direction: column;
      gap: 0.35rem;
      overflow: hidden;
      box-shadow: none;
    }
    .kpi-card--accent {
      border-color: var(--pa-border-strong);
      background:
        linear-gradient(165deg, color-mix(in srgb, var(--pa-surface-strong) 35%, transparent) 0%, transparent 48%),
        var(--pa-surface);
    }
    .kpi-card--critical {
      border-color: color-mix(in srgb, var(--pa-danger) 40%, var(--pa-border-strong));
      background:
        linear-gradient(160deg, color-mix(in srgb, var(--pa-danger) 16%, transparent) 0%, transparent 55%),
        var(--pa-surface);
    }
    .kpi-card--watch {
      border-color: color-mix(in srgb, var(--pa-warning) 35%, var(--pa-border-strong));
      background:
        linear-gradient(160deg, color-mix(in srgb, var(--pa-warning) 14%, transparent) 0%, transparent 55%),
        var(--pa-surface);
    }
    .kpi-card--positive {
      border-color: color-mix(in srgb, var(--pa-success) 30%, var(--pa-border-strong));
      background:
        linear-gradient(160deg, color-mix(in srgb, var(--pa-success) 14%, transparent) 0%, transparent 55%),
        var(--pa-surface);
    }
    .kpi-card--delta-up {
      background:
        linear-gradient(160deg, color-mix(in srgb, var(--pa-viz-up) 18%, transparent) 0%, transparent 52%),
        var(--pa-surface);
    }
    .kpi-card--delta-down {
      background:
        linear-gradient(160deg, color-mix(in srgb, var(--pa-viz-down) 18%, transparent) 0%, transparent 52%),
        var(--pa-surface);
    }
    .kpi-card--clickable {
      cursor: pointer;
      transition:
        background-color var(--pa-motion-fast, 120ms) var(--pa-ease, ease),
        border-color var(--pa-motion-fast, 120ms) var(--pa-ease, ease),
        box-shadow var(--pa-motion-fast, 120ms) var(--pa-ease, ease),
        transform var(--pa-motion-fast, 120ms) var(--pa-ease, ease);
    }
    .kpi-card--clickable:hover {
      border-color: var(--pa-text-tertiary);
      box-shadow: 0 6px 18px color-mix(in srgb, var(--pa-text) 8%, transparent);
      transform: translateY(-1px);
    }
    .kpi-card--clickable:active {
      transform: scale(0.99);
      box-shadow: none;
    }
    .kpi-card--clickable:focus-visible {
      outline: 2px solid var(--pa-focus-ring);
      outline-offset: 2px;
    }
    .kpi-card__top {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      gap: 0.5rem;
      min-width: 0;
    }
    h3 {
      margin: 0;
      min-width: 0;
      flex: 1;
      font-size: 0.7rem;
      line-height: 1.3;
      text-transform: uppercase;
      letter-spacing: 0.04em;
      color: var(--pa-text-secondary);
      font-weight: 650;
      display: flex;
      align-items: flex-start;
      gap: 0.35rem;
    }
    .kpi-card__label {
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
      word-break: break-word;
      overflow-wrap: anywhere;
    }
    .kpi-card__info {
      flex: 0 0 auto;
      display: inline-flex;
      width: 1rem;
      height: 1rem;
      border-radius: 999px;
      border: 1px solid var(--pa-border);
      align-items: center;
      justify-content: center;
      font-size: 0.65rem;
      color: var(--pa-text-secondary);
      cursor: help;
      background: var(--pa-surface-muted);
      margin-top: 0.05rem;
    }
    .kpi-card__kind {
      flex: 0 0 auto;
      font-size: 0.62rem;
      text-transform: uppercase;
      letter-spacing: 0.04em;
      color: var(--pa-text-tertiary);
      border: 1px solid var(--pa-border);
      border-radius: 999px;
      padding: 0.12rem 0.4rem;
      background: var(--pa-surface-muted);
      white-space: nowrap;
    }
    .kpi-card__value-row {
      display: flex;
      align-items: baseline;
      gap: 0.5rem;
      flex-wrap: wrap;
      min-width: 0;
      margin-top: 0.15rem;
    }
    .kpi-card__value {
      margin: 0;
      font-size: var(--pa-font-kpi, clamp(1.35rem, 2.5vw, 1.75rem));
      font-weight: var(--pa-weight-bold, 750);
      letter-spacing: -0.03em;
      font-variant-numeric: tabular-nums;
      color: var(--pa-text);
      line-height: var(--pa-leading-tight, 1.2);
      min-width: 0;
      overflow-wrap: anywhere;
      word-break: break-word;
    }
    .kpi-card__delta {
      display: inline-flex;
      align-items: center;
      gap: 0.2rem;
      font-size: 0.78rem;
      font-weight: 700;
      font-variant-numeric: tabular-nums;
      white-space: nowrap;
    }
    .kpi-card__delta--up {
      color: var(--pa-viz-up, var(--pa-success));
    }
    .kpi-card__delta--down {
      color: var(--pa-viz-down, var(--pa-danger));
    }
    .kpi-card__delta--flat {
      color: var(--pa-text-tertiary);
    }
    .kpi-card__hint {
      margin: 0;
      font-size: 0.78rem;
      line-height: 1.35;
      color: var(--pa-text-secondary);
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }
    .kpi-card__explain {
      margin: 0;
      font-size: 0.76rem;
      line-height: 1.35;
      color: var(--pa-text-secondary);
      display: -webkit-box;
      -webkit-line-clamp: 3;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }
    .kpi-card__action {
      margin-top: auto;
      padding-top: 0.35rem;
      font-size: 0.78rem;
      font-weight: 650;
      color: var(--pa-text-secondary);
    }
    @media (prefers-reduced-motion: reduce) {
      .kpi-card--clickable {
        transition: none;
      }
      .kpi-card--clickable:hover,
      .kpi-card--clickable:active {
        transform: none;
        box-shadow: none;
      }
    }
  `,
})
export class KpiCardComponent {
  readonly label = input.required<string>();
  readonly value = input.required<string | number>();
  readonly hint = input<string>('');
  readonly accent = input(false);
  readonly kind = input<'count' | 'score' | 'volume' | 'none'>('none');
  readonly severity = input<SeverityBand>('neutral');
  readonly actionLabel = input<string>('');
  readonly clickable = input(false);
  readonly infoTitle = input<string>('');
  readonly infoBody = input<string>('');
  readonly showInfoExpanded = input(false);
  /** Semantic delta for evolutionary metrics (green up / red down by default meaning). */
  readonly deltaDirection = input<DeltaDirection>(null);
  readonly deltaLabel = input<string>('');
  readonly activated = output<void>();

  onActivate(event: Event): void {
    if (this.clickable()) {
      this.activated.emit();
    }
  }
}
