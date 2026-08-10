import { Component, input, output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ExplorerFilters, ExplorerGroupBy } from '../../../core/models/explorer.model';
import { DEFAULT_UPCOMING_DEADLINE_DAYS } from '../../analytics/analytics-thresholds';
import { PortfolioSummary } from '../../../core/models/portfolio.model';

@Component({
  selector: 'app-explorer-filter-panel',
  standalone: true,
  imports: [FormsModule],
  template: `
    <aside class="filters" aria-label="Explorer filters">
      <h3>Filters</h3>

      <label>
        Search
        <input
          type="search"
          [ngModel]="filters().search"
          (ngModelChange)="patch({ search: $event })"
          placeholder="Project name"
        />
      </label>

      <label>
        Portfolio
        <select
          [ngModel]="filters().portfolioId ?? ''"
          (ngModelChange)="patch({ portfolioId: $event || null })"
        >
          <option value="">All projects</option>
          @for (p of portfolios(); track p.id) {
            <option [value]="p.id">{{ p.name }}</option>
          }
        </select>
      </label>

      <fieldset>
        <legend>Exception toggles</legend>
        <label class="check">
          <input
            type="checkbox"
            [ngModel]="filters().criticalOnly"
            (ngModelChange)="patch({ criticalOnly: $event })"
          />
          Critical only
        </label>
        <label class="check">
          <input
            type="checkbox"
            [ngModel]="filters().delayedOnly"
            (ngModelChange)="patch({ delayedOnly: $event })"
          />
          Delayed only
        </label>
        <label class="check">
          <input
            type="checkbox"
            [ngModel]="filters().needsAttentionOnly"
            (ngModelChange)="patch({ needsAttentionOnly: $event })"
          />
          Needs Attention only
        </label>
        <label class="check">
          <input
            type="checkbox"
            [ngModel]="filters().hasOverdueWp"
            (ngModelChange)="patch({ hasOverdueWp: $event })"
          />
          Overdue work packages
        </label>
      </fieldset>

      <label>
        Project admin
        <input
          type="search"
          [ngModel]="filters().projectAdmin"
          (ngModelChange)="patch({ projectAdmin: $event })"
          placeholder="e.g. Alice"
          list="explorer-admin-suggestions"
          autocomplete="off"
        />
        @if (adminSuggestions().length) {
          <datalist id="explorer-admin-suggestions">
            @for (name of adminSuggestions(); track name) {
              <option [value]="name"></option>
            }
          </datalist>
        }
      </label>
      <p class="filters__hint">
        OpenProject members with Project admin / Manager role. Re-sync after upgrade to load admins.
      </p>
      <p class="filters__hint">
        Upcoming deadlines use project finish date, or the next open work-package due date if finish date is empty.
      </p>

      <label>
        Upcoming deadlines (days)
        <select
          [ngModel]="filters().upcomingDeadlineDays ?? ''"
          (ngModelChange)="onUpcomingChange($event)"
        >
          <option value="">Off</option>
          <option [value]="7">7</option>
          <option [value]="14">14 (default)</option>
          <option [value]="30">30</option>
        </select>
      </label>

      <fieldset class="ranges">
        <legend>Health range</legend>
        <div class="range-row">
          <input
            type="number"
            min="0"
            max="100"
            placeholder="Min"
            [ngModel]="filters().healthMin"
            (ngModelChange)="patch({ healthMin: toNum($event) })"
          />
          <input
            type="number"
            min="0"
            max="100"
            placeholder="Max"
            [ngModel]="filters().healthMax"
            (ngModelChange)="patch({ healthMax: toNum($event) })"
          />
        </div>
      </fieldset>

      <fieldset class="ranges">
        <legend>Progress range</legend>
        <div class="range-row">
          <input
            type="number"
            min="0"
            max="100"
            placeholder="Min"
            [ngModel]="filters().progressMin"
            (ngModelChange)="patch({ progressMin: toNum($event) })"
          />
          <input
            type="number"
            min="0"
            max="100"
            placeholder="Max"
            [ngModel]="filters().progressMax"
            (ngModelChange)="patch({ progressMax: toNum($event) })"
          />
        </div>
      </fieldset>

      <fieldset class="ranges">
        <legend>Risk range</legend>
        <div class="range-row">
          <input
            type="number"
            min="0"
            max="100"
            placeholder="Min"
            [ngModel]="filters().riskMin"
            (ngModelChange)="patch({ riskMin: toNum($event) })"
          />
          <input
            type="number"
            min="0"
            max="100"
            placeholder="Max"
            [ngModel]="filters().riskMax"
            (ngModelChange)="patch({ riskMax: toNum($event) })"
          />
        </div>
      </fieldset>

      <label>
        Group by
        <select [ngModel]="groupBy()" (ngModelChange)="groupByChange.emit($event)">
          <option value="none">None</option>
          <option value="healthBand">Health band</option>
          <option value="riskBand">Risk band</option>
          <option value="needsAttentionBand">Needs Attention</option>
          <option value="status">Status</option>
          <option value="delayed">Delayed</option>
          <option value="portfolio">Portfolio</option>
        </select>
      </label>

      <button type="button" class="filters__reset" (click)="reset.emit()">Reset filters</button>
    </aside>
  `,
  styles: `
    .filters {
      display: flex;
      flex-direction: column;
      gap: 0.85rem;
      padding: 1rem;
      border: 1px solid var(--pa-border);
      border-radius: 12px;
      background: var(--pa-surface);
      min-width: 220px;
    }
    h3 {
      margin: 0;
      font-size: 0.95rem;
    }
    label,
    legend {
      display: flex;
      flex-direction: column;
      gap: 0.3rem;
      font-size: 0.78rem;
      font-weight: 600;
      color: var(--pa-text-muted);
    }
    input,
    select {
      border: 1px solid var(--pa-border);
      border-radius: 0.5rem;
      padding: 0.45rem 0.55rem;
      background: #fff;
      color: var(--pa-text);
    }
    fieldset {
      border: 1px solid var(--pa-border);
      border-radius: 0.65rem;
      margin: 0;
      padding: 0.55rem 0.65rem;
    }
    .check {
      flex-direction: row;
      align-items: center;
      gap: 0.45rem;
      font-weight: 500;
      color: var(--pa-text);
      margin-top: 0.35rem;
    }
    .range-row {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 0.4rem;
    }
    .filters__reset {
      border: 1px solid var(--pa-border);
      background: var(--pa-surface-muted);
      border-radius: 0.55rem;
      padding: 0.5rem 0.7rem;
      cursor: pointer;
    }
    .filters__hint {
      margin: -0.35rem 0 0;
      font-size: 0.72rem;
      font-weight: 500;
      color: var(--pa-text-muted);
      line-height: 1.3;
    }
  `,
})
export class ExplorerFilterPanelComponent {
  readonly filters = input.required<ExplorerFilters>();
  readonly groupBy = input.required<ExplorerGroupBy>();
  readonly portfolios = input<readonly PortfolioSummary[]>([]);
  /** Distinct project admin names for autocomplete. */
  readonly adminSuggestions = input<readonly string[]>([]);
  readonly filtersChange = output<Partial<ExplorerFilters>>();
  readonly groupByChange = output<ExplorerGroupBy>();
  readonly reset = output<void>();

  readonly defaultUpcoming = DEFAULT_UPCOMING_DEADLINE_DAYS;

  patch(partial: Partial<ExplorerFilters>): void {
    this.filtersChange.emit(partial);
  }

  toNum(value: unknown): number | null {
    if (value === '' || value == null) {
      return null;
    }
    const n = Number(value);
    return Number.isFinite(n) ? n : null;
  }

  onUpcomingChange(value: string | number): void {
    if (value === '' || value == null) {
      this.patch({ upcomingDeadlineDays: null });
      return;
    }
    this.patch({ upcomingDeadlineDays: Number(value) });
  }
}
