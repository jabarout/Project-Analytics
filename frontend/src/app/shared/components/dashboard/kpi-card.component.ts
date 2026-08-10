import { Component, input, output } from '@angular/core';
import { SeverityBand } from '../../analytics/analytics-thresholds';

/**
 * Reusable management KPI card with optional literacy tooltip and drill-down.
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
      [attr.role]="clickable() ? 'button' : null"
      [attr.tabindex]="clickable() ? 0 : null"
      (click)="onActivate($event)"
      (keydown.enter)="onActivate($event)"
      (keydown.space)="onActivate($event); $event.preventDefault()"
    >
      <div class="kpi-card__top">
        <h3>
          {{ label() }}
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
        }
      </div>
      <p class="kpi-card__value">{{ value() }}</p>
      @if (hint()) {
        <p class="kpi-card__hint">{{ hint() }}</p>
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
      padding: 0.95rem;
      border-radius: 12px;
      border: 1px solid var(--pa-border);
      background: var(--pa-surface);
      min-height: 5.5rem;
      display: flex;
      flex-direction: column;
      gap: 0.15rem;
    }
    .kpi-card--accent {
      border-color: rgba(29, 78, 216, 0.35);
      background: linear-gradient(180deg, rgba(29, 78, 216, 0.06), var(--pa-surface));
    }
    .kpi-card--critical {
      border-color: rgba(185, 28, 28, 0.45);
      background: linear-gradient(180deg, rgba(185, 28, 28, 0.06), var(--pa-surface));
    }
    .kpi-card--watch {
      border-color: rgba(180, 83, 9, 0.4);
      background: linear-gradient(180deg, rgba(180, 83, 9, 0.05), var(--pa-surface));
    }
    .kpi-card--positive {
      border-color: rgba(15, 118, 110, 0.35);
    }
    .kpi-card--clickable {
      cursor: pointer;
      transition: box-shadow 0.15s ease, transform 0.1s ease;
    }
    .kpi-card--clickable:hover {
      box-shadow: 0 6px 16px rgba(15, 23, 42, 0.08);
      transform: translateY(-1px);
    }
    .kpi-card--clickable:focus-visible {
      outline: 2px solid #1d4ed8;
      outline-offset: 2px;
    }
    .kpi-card__top {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      gap: 0.5rem;
    }
    h3 {
      margin: 0;
      font-size: 0.72rem;
      text-transform: uppercase;
      letter-spacing: 0.05em;
      color: var(--pa-text-muted);
      font-weight: 600;
      display: inline-flex;
      align-items: center;
      gap: 0.35rem;
    }
    .kpi-card__info {
      display: inline-flex;
      width: 1rem;
      height: 1rem;
      border-radius: 999px;
      border: 1px solid var(--pa-border);
      align-items: center;
      justify-content: center;
      font-size: 0.65rem;
      font-style: normal;
      text-transform: none;
      letter-spacing: 0;
      color: #1d4ed8;
      cursor: help;
      background: #fff;
    }
    .kpi-card__kind {
      font-size: 0.65rem;
      text-transform: uppercase;
      letter-spacing: 0.04em;
      color: var(--pa-text-muted);
      border: 1px solid var(--pa-border);
      border-radius: 999px;
      padding: 0.1rem 0.4rem;
    }
    .kpi-card__value {
      margin: 0.35rem 0 0;
      font-size: 1.45rem;
      font-weight: 750;
    }
    .kpi-card__hint {
      margin: 0.25rem 0 0;
      font-size: 0.8rem;
      color: var(--pa-text-muted);
    }
    .kpi-card__explain {
      margin: 0.4rem 0 0;
      font-size: 0.78rem;
      line-height: 1.35;
      color: var(--pa-text-muted);
    }
    .kpi-card__action {
      margin: 0.45rem 0 0;
      font-size: 0.78rem;
      font-weight: 600;
      color: #1d4ed8;
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
  readonly activated = output<void>();

  onActivate(event: Event): void {
    if (this.clickable()) {
      this.activated.emit();
    }
  }
}
