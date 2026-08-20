import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Recommendation } from '../../../core/models/recommendation.model';

/**
 * Unified recommendation list (Home, Project, Portfolio).
 * Project Detail density is the quality reference; Home may pass compact=true.
 */
@Component({
  selector: 'app-recommendation-list',
  standalone: true,
  imports: [RouterLink],
  template: `
    <section class="reco" [class.reco--compact]="compact()">
      <header class="reco__header">
        <h3>{{ title() }}</h3>
        @if (!compact() && items().length) {
          <span class="reco__count">{{ items().length }}</span>
        }
      </header>
      @if (summary()) {
        <p class="reco__summary">{{ summary() }}</p>
      }
      @if (items().length === 0) {
        <p class="reco__empty">No recommendations at current analytics thresholds.</p>
      } @else {
        <ul>
          @for (item of items(); track item.id) {
            <li [class]="'reco__item reco__item--' + severityClass(item.severity)">
              <div class="reco__head">
                <span class="reco__severity reco__severity--{{ severityClass(item.severity) }}">
                  {{ humanize(item.severity) }}
                </span>
                <strong class="reco__title">{{ item.title }}</strong>
              </div>
              <p class="reco__desc">{{ item.description }}</p>
              @if (!compact()) {
                <p class="reco__why"><span>Why</span> {{ item.explanation }}</p>
                @if (item.suggestedAction) {
                  <p class="reco__action"><span>Suggested action</span> {{ item.suggestedAction }}</p>
                }
              } @else if (item.suggestedAction) {
                <p class="reco__action reco__action--compact">{{ item.suggestedAction }}</p>
              }
              <div class="reco__meta">
                <a [routerLink]="['/projects', item.projectId]">{{ item.projectName }}</a>
                <span>{{ humanize(item.ruleCode) }}</span>
              </div>
            </li>
          }
        </ul>
      }
    </section>
  `,
  styles: `
    .reco {
      padding: 1.15rem 1.25rem;
      border: 2px solid var(--pa-border-strong);
      border-radius: var(--pa-radius-lg);
      background:
        linear-gradient(180deg, color-mix(in srgb, var(--pa-surface-muted) 40%, transparent), transparent 40%),
        var(--pa-surface);
    }
    .reco__header {
      display: flex;
      align-items: baseline;
      justify-content: space-between;
      gap: 0.75rem;
      margin-bottom: 0.35rem;
    }
    h3 {
      margin: 0;
      font-size: 1.05rem;
      font-weight: 700;
      letter-spacing: -0.015em;
    }
    .reco__count {
      font-size: 0.75rem;
      font-weight: 700;
      color: var(--pa-text-tertiary);
      border: 1px solid var(--pa-border);
      border-radius: var(--pa-radius-pill);
      padding: 0.15rem 0.55rem;
      background: var(--pa-surface-muted);
    }
    .reco__summary {
      margin: 0 0 0.95rem;
      color: var(--pa-text-secondary);
      line-height: 1.45;
      font-size: 0.92rem;
    }
    .reco__empty {
      margin: 0;
      color: var(--pa-text-secondary);
    }
    ul {
      list-style: none;
      margin: 0;
      padding: 0;
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
    }
    .reco__item {
      border: 2px solid var(--pa-border-strong);
      border-radius: var(--pa-radius-md);
      padding: 0.9rem 1rem;
      background: var(--pa-surface);
      transition:
        border-color var(--pa-motion-fast) var(--pa-ease),
        box-shadow var(--pa-motion-fast) var(--pa-ease);
    }
    .reco__item:hover {
      border-color: var(--pa-text-tertiary);
      box-shadow: 0 4px 14px color-mix(in srgb, var(--pa-text) 6%, transparent);
    }
    @media (prefers-reduced-motion: reduce) {
      .reco__item {
        transition: none;
      }
      .reco__item:hover {
        box-shadow: none;
      }
    }
    .reco__item--critical {
      border-color: color-mix(in srgb, var(--pa-danger) 45%, var(--pa-border-strong));
      background: linear-gradient(180deg, var(--pa-danger-muted), var(--pa-surface));
    }
    .reco__item--high {
      border-color: color-mix(in srgb, var(--pa-warning) 40%, var(--pa-border-strong));
      background: linear-gradient(180deg, var(--pa-warning-muted), var(--pa-surface));
    }
    .reco__item--medium {
      border-color: var(--pa-border-strong);
    }
    .reco__item--low {
      border-color: var(--pa-border);
    }
    .reco__head {
      display: flex;
      gap: 0.55rem;
      align-items: center;
      flex-wrap: wrap;
    }
    .reco__severity {
      font-size: 0.68rem;
      font-weight: 750;
      letter-spacing: 0.05em;
      text-transform: uppercase;
      border-radius: var(--pa-radius-pill);
      padding: 0.18rem 0.55rem;
      border: 1px solid transparent;
      background: var(--pa-surface-muted);
      color: var(--pa-text-secondary);
    }
    .reco__severity--critical {
      background: var(--pa-danger-muted);
      color: var(--pa-danger);
      border-color: color-mix(in srgb, var(--pa-danger) 35%, transparent);
    }
    .reco__severity--high {
      background: var(--pa-warning-muted);
      color: var(--pa-warning);
      border-color: color-mix(in srgb, var(--pa-warning) 35%, transparent);
    }
    .reco__severity--medium {
      background: color-mix(in srgb, var(--pa-viz-1) 14%, var(--pa-surface-muted));
      color: var(--pa-text);
    }
    .reco__title {
      font-size: 0.98rem;
      letter-spacing: -0.01em;
      color: var(--pa-text);
    }
    .reco__desc,
    .reco__why,
    .reco__action {
      margin: 0.45rem 0 0;
      line-height: 1.45;
      font-size: 0.92rem;
      color: var(--pa-text);
    }
    .reco__why span,
    .reco__action span {
      display: inline-block;
      margin-right: 0.35rem;
      font-weight: 700;
      font-size: 0.72rem;
      letter-spacing: 0.04em;
      text-transform: uppercase;
      color: var(--pa-text-tertiary);
    }
    .reco__action--compact {
      color: var(--pa-text-secondary);
      font-size: 0.88rem;
    }
    .reco__meta {
      margin-top: 0.65rem;
      display: flex;
      justify-content: space-between;
      gap: 0.75rem;
      font-size: 0.82rem;
      color: var(--pa-text-secondary);
    }
    .reco__meta a {
      font-weight: 650;
      text-decoration: none;
      color: var(--pa-text);
    }
    .reco__meta a:hover {
      text-decoration: underline;
    }
    .reco--compact .reco__item {
      padding: 0.75rem 0.85rem;
    }
    .reco--compact .reco__desc {
      font-size: 0.88rem;
      color: var(--pa-text-secondary);
    }
  `,
})
export class RecommendationListComponent {
  readonly title = input('Recommendations');
  readonly summary = input<string>('');
  readonly items = input.required<readonly Recommendation[]>();
  /** Home top-N uses compact; Project/Portfolio use full hierarchy. */
  readonly compact = input(false);

  severityClass(value: string | null | undefined): string {
    const s = (value ?? '').toUpperCase();
    if (s.includes('CRITICAL')) {
      return 'critical';
    }
    if (s.includes('HIGH')) {
      return 'high';
    }
    if (s.includes('MEDIUM') || s.includes('WARN')) {
      return 'medium';
    }
    return 'low';
  }

  /** HIGH_ATTENTION → High Attention; keeps API enums intact. */
  humanize(value: string | null | undefined): string {
    if (!value) {
      return '';
    }
    return value
      .replace(/_/g, ' ')
      .toLowerCase()
      .replace(/\b\w/g, (c) => c.toUpperCase());
  }
}
