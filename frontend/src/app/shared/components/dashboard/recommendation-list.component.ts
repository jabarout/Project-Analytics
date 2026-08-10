import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Recommendation } from '../../../core/models/recommendation.model';

/**
 * Presentational recommendation list. Receives prepared recommendation DTOs only.
 */
@Component({
  selector: 'app-recommendation-list',
  standalone: true,
  imports: [RouterLink],
  template: `
    <section class="reco">
      <h3>{{ title() }}</h3>
      @if (summary()) {
        <p class="reco__summary">{{ summary() }}</p>
      }
      @if (items().length === 0) {
        <p class="reco__empty">No recommendations at current analytics thresholds.</p>
      } @else {
        <ul>
          @for (item of items(); track item.id) {
            <li [class]="'reco__item reco__item--' + item.severity.toLowerCase()">
              <div class="reco__head">
                <span class="reco__severity">{{ humanize(item.severity) }}</span>
                <strong>{{ item.title }}</strong>
              </div>
              <p class="reco__desc">{{ item.description }}</p>
              <p class="reco__why"><span>Why:</span> {{ item.explanation }}</p>
              @if (item.suggestedAction) {
                <p class="reco__action"><span>Suggested action:</span> {{ item.suggestedAction }}</p>
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
      padding: 1.1rem;
      border: 1px solid var(--pa-border);
      border-radius: 12px;
      background: var(--pa-surface);
    }
    h3 {
      margin: 0 0 0.55rem;
      font-size: 1rem;
    }
    .reco__summary {
      margin: 0 0 0.85rem;
      color: var(--pa-text-muted);
      line-height: 1.45;
    }
    .reco__empty {
      margin: 0;
      color: var(--pa-text-muted);
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
      border: 1px solid var(--pa-border);
      border-radius: 10px;
      padding: 0.8rem 0.9rem;
      border-left-width: 4px;
    }
    .reco__item--critical {
      border-left-color: #b91c1c;
    }
    .reco__item--high {
      border-left-color: #c2410c;
    }
    .reco__item--medium {
      border-left-color: #1d4ed8;
    }
    .reco__item--low {
      border-left-color: #64748b;
    }
    .reco__head {
      display: flex;
      gap: 0.55rem;
      align-items: center;
      flex-wrap: wrap;
    }
    .reco__severity {
      font-size: 0.7rem;
      font-weight: 700;
      letter-spacing: 0.04em;
      color: var(--pa-text-muted);
    }
    .reco__desc,
    .reco__why,
    .reco__action {
      margin: 0.4rem 0 0;
      line-height: 1.45;
      font-size: 0.92rem;
    }
    .reco__why span,
    .reco__action span {
      font-weight: 600;
      color: var(--pa-text-muted);
    }
    .reco__meta {
      margin-top: 0.55rem;
      display: flex;
      justify-content: space-between;
      gap: 0.75rem;
      font-size: 0.82rem;
      color: var(--pa-text-muted);
    }
    .reco__meta a {
      font-weight: 600;
      text-decoration: none;
    }
  `,
})
export class RecommendationListComponent {
  readonly title = input('Recommendations');
  readonly summary = input<string>('');
  readonly items = input.required<readonly Recommendation[]>();

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
