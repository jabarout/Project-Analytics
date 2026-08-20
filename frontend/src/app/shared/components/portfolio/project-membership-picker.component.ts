import { Component, computed, effect, input, output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { PortfolioProjectSummary } from '../../../core/models/portfolio.model';
import { ExplorerProjectRow } from '../../../core/models/explorer.model';

/**
 * Searchable multi-select for portfolio membership with optional analytics filters (M11B).
 * When analyticsRows are provided, Health/Risk/Progress/Delayed/Needs Attention filters apply.
 */
@Component({
  selector: 'app-project-membership-picker',
  standalone: true,
  imports: [FormsModule],
  template: `
    <div class="picker">
      <div class="picker__toolbar">
        <label class="picker__search">
          <span>Search</span>
          <input
            type="search"
            [ngModel]="query()"
            (ngModelChange)="query.set($event)"
            placeholder="Name or status…"
            autocomplete="off"
          />
        </label>
        <label class="picker__status">
          <span>Status</span>
          <select [ngModel]="statusFilter()" (ngModelChange)="statusFilter.set($event)">
            <option value="">All statuses</option>
            @for (status of statusOptions(); track status) {
              <option [value]="status">{{ status }}</option>
            }
          </select>
        </label>
        <div class="picker__actions">
          <button type="button" class="pa-btn pa-btn--secondary pa-btn--sm" (click)="selectAllFiltered()">
            Select all filtered
          </button>
          <button type="button" class="pa-btn pa-btn--secondary pa-btn--sm" (click)="clearSelection()">
            Clear
          </button>
        </div>
      </div>

      @if (hasAnalytics()) {
        <div class="picker__analytics-filters">
          <label class="check">
            <input type="checkbox" [ngModel]="criticalOnly()" (ngModelChange)="criticalOnly.set($event)" />
            Critical
          </label>
          <label class="check">
            <input type="checkbox" [ngModel]="delayedOnly()" (ngModelChange)="delayedOnly.set($event)" />
            Delayed
          </label>
          <label class="check">
            <input
              type="checkbox"
              [ngModel]="needsAttentionOnly()"
              (ngModelChange)="needsAttentionOnly.set($event)"
            />
            Needs Attention
          </label>
          <label class="check">
            <input type="checkbox" [ngModel]="hasOverdueWp()" (ngModelChange)="hasOverdueWp.set($event)" />
            Overdue WPs
          </label>
          <label class="range range--wide">
            <span>Project admin</span>
            <input
              type="search"
              [ngModel]="projectAdmin()"
              (ngModelChange)="projectAdmin.set($event)"
              placeholder="e.g. Alice"
              list="picker-admin-suggestions"
              autocomplete="off"
            />
            @if (adminSuggestions().length) {
              <datalist id="picker-admin-suggestions">
                @for (name of adminSuggestions(); track name) {
                  <option [value]="name"></option>
                }
              </datalist>
            }
          </label>
          <label class="range">
            <span>Health min</span>
            <input
              type="number"
              min="0"
              max="100"
              [ngModel]="healthMin()"
              (ngModelChange)="healthMin.set(toNum($event))"
              placeholder="0"
            />
          </label>
          <label class="range">
            <span>Health max</span>
            <input
              type="number"
              min="0"
              max="100"
              [ngModel]="healthMax()"
              (ngModelChange)="healthMax.set(toNum($event))"
              placeholder="100"
            />
          </label>
          <label class="range">
            <span>Risk min</span>
            <input
              type="number"
              min="0"
              max="100"
              [ngModel]="riskMin()"
              (ngModelChange)="riskMin.set(toNum($event))"
              placeholder="0"
            />
          </label>
          <label class="range">
            <span>Risk max</span>
            <input
              type="number"
              min="0"
              max="100"
              [ngModel]="riskMax()"
              (ngModelChange)="riskMax.set(toNum($event))"
              placeholder="100"
            />
          </label>
          <label class="range">
            <span>Progress min</span>
            <input
              type="number"
              min="0"
              max="100"
              [ngModel]="progressMin()"
              (ngModelChange)="progressMin.set(toNum($event))"
              placeholder="0"
            />
          </label>
          <label class="range">
            <span>Progress max</span>
            <input
              type="number"
              min="0"
              max="100"
              [ngModel]="progressMax()"
              (ngModelChange)="progressMax.set(toNum($event))"
              placeholder="100"
            />
          </label>
        </div>
      }

      <p class="picker__meta">
        Showing {{ filtered().length }} of {{ projects().length }} project(s) ·
        {{ selectedIds().size }} selected
      </p>

      <div class="picker__list" role="group" [attr.aria-label]="title()">
        @if (filtered().length === 0) {
          <p class="picker__empty">No projects match this filter.</p>
        } @else {
          @for (project of filtered(); track project.id) {
            <label class="picker__row">
              <input
                type="checkbox"
                [checked]="selectedIds().has(project.id)"
                (change)="toggle(project.id, $event)"
              />
              <span class="picker__name">{{ project.name }}</span>
              <span class="picker__meta-cols">
                @if (analyticsFor(project.id); as row) {
                  <span class="chip" title="Health">H {{ formatScore(row.healthScore) }}</span>
                  <span class="chip" title="Risk">R {{ formatScore(row.riskScore) }}</span>
                  <span class="chip" title="Needs Attention">A {{ formatScore(row.attentionScore) }}</span>
                  @if (row.delayed) {
                    <span class="chip chip--warn">Delayed</span>
                  }
                  @if (row.critical) {
                    <span class="chip chip--crit">Critical</span>
                  }
                  @if (row.needsAttention) {
                    <span class="chip chip--attn">Needs Att.</span>
                  }
                  @if (row.overdueWorkPackageCount > 0) {
                    <span class="chip">OWP {{ row.overdueWorkPackageCount }}</span>
                  }
                }
                <span class="picker__status-badge">{{ project.status || '—' }}</span>
              </span>
            </label>
          }
        }
      </div>
    </div>
  `,
  styles: `
    .picker {
      display: flex;
      flex-direction: column;
      gap: 0.65rem;
      border: 2px solid var(--pa-border-strong);
      border-radius: var(--pa-radius-lg);
      padding: 0.85rem;
      background: var(--pa-surface-muted);
    }
    .picker__toolbar {
      display: grid;
      grid-template-columns: 1.4fr 0.8fr auto;
      gap: 0.65rem;
      align-items: end;
    }
    @media (max-width: 720px) {
      .picker__toolbar {
        grid-template-columns: 1fr;
      }
    }
    .picker__search,
    .picker__status,
    .range {
      display: flex;
      flex-direction: column;
      gap: 0.3rem;
      font-size: 0.78rem;
      font-weight: 600;
      color: var(--pa-text-secondary);
    }
    input[type='search'],
    input[type='number'],
    select {
      font: inherit;
      border: 2px solid var(--pa-border-strong);
      border-radius: var(--pa-radius-md);
      padding: 0.45rem 0.55rem;
      background: var(--pa-surface);
      color: var(--pa-text);
    }
    .picker__analytics-filters {
      display: flex;
      flex-wrap: wrap;
      gap: 0.55rem 0.75rem;
      align-items: end;
      padding: 0.55rem 0.65rem;
      border: 2px solid var(--pa-border);
      border-radius: var(--pa-radius-md);
      background: var(--pa-surface);
    }
    .check {
      display: inline-flex;
      align-items: center;
      gap: 0.3rem;
      font-size: 0.82rem;
      font-weight: 500;
      color: var(--pa-text);
    }
    .range input {
      width: 4.5rem;
    }
    .range--wide input {
      width: 9rem;
    }
    .picker__actions {
      display: flex;
      gap: 0.4rem;
      flex-wrap: wrap;
    }
    .picker__actions button {
      /* layout only — global button grammar */
      min-height: 2.15rem;
      padding: 0.35rem 0.9rem;
      font-size: 0.875rem;
    }
    .picker__meta {
      margin: 0;
      font-size: 0.85rem;
      color: var(--pa-text-secondary);
    }
    .picker__list {
      max-height: 320px;
      overflow: auto;
      border: 2px solid var(--pa-border-strong);
      border-radius: var(--pa-radius-md);
      background: var(--pa-surface);
    }
    .picker__row {
      display: grid;
      grid-template-columns: auto minmax(0, 1fr) auto;
      gap: 0.55rem;
      align-items: center;
      padding: 0.45rem 0.65rem;
      border-bottom: 1px solid var(--pa-border);
      cursor: pointer;
      font-weight: 500;
    }
    .picker__row:last-child {
      border-bottom: none;
    }
    .picker__row:hover {
      background: var(--pa-surface-muted);
    }
    .picker__name {
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    .picker__meta-cols {
      display: flex;
      flex-wrap: wrap;
      gap: 0.25rem;
      justify-content: flex-end;
      align-items: center;
    }
    .chip {
      font-size: 0.68rem;
      border: 1px solid var(--pa-border);
      border-radius: var(--pa-radius-pill);
      padding: 0.08rem 0.35rem;
      color: var(--pa-text-secondary);
      white-space: nowrap;
    }
    .chip--warn {
      color: var(--pa-warning);
      border-color: color-mix(in srgb, var(--pa-warning) 40%, var(--pa-border));
      background: var(--pa-warning-muted);
    }
    .chip--crit {
      color: var(--pa-danger);
      border-color: color-mix(in srgb, var(--pa-danger) 40%, var(--pa-border));
      background: var(--pa-danger-muted);
    }
    .chip--attn {
      color: var(--pa-text);
      border-color: var(--pa-border-strong);
    }
    .picker__status-badge {
      font-size: 0.72rem;
      color: var(--pa-text-secondary);
      text-transform: uppercase;
      letter-spacing: 0.03em;
    }
    .picker__empty {
      margin: 0;
      padding: 1rem;
      color: var(--pa-text-secondary);
    }
  `,
})
export class ProjectMembershipPickerComponent {
  readonly title = input('Projects');
  readonly projects = input.required<readonly PortfolioProjectSummary[]>();
  /** Optional explorer rows for analytical filters and row badges. */
  readonly analyticsRows = input<readonly ExplorerProjectRow[]>([]);
  readonly selected = input<readonly string[] | null>(null);
  readonly selectionChange = output<string[]>();

  readonly query = signal('');
  readonly statusFilter = signal('');
  readonly criticalOnly = signal(false);
  readonly delayedOnly = signal(false);
  readonly needsAttentionOnly = signal(false);
  readonly hasOverdueWp = signal(false);
  readonly projectAdmin = signal('');
  readonly healthMin = signal<number | null>(null);
  readonly healthMax = signal<number | null>(null);
  readonly riskMin = signal<number | null>(null);
  readonly riskMax = signal<number | null>(null);
  readonly progressMin = signal<number | null>(null);
  readonly progressMax = signal<number | null>(null);
  readonly selectedIds = signal<Set<string>>(new Set());

  constructor() {
    effect(() => {
      const external = this.selected();
      if (external != null) {
        this.selectedIds.set(new Set(external));
      }
    });
  }

  readonly hasAnalytics = computed(() => this.analyticsRows().length > 0);

  readonly analyticsById = computed(() => {
    const map = new Map<string, ExplorerProjectRow>();
    for (const row of this.analyticsRows()) {
      map.set(row.projectId, row);
    }
    return map;
  });

  readonly adminSuggestions = computed(() => {
    const set = new Set<string>();
    for (const row of this.analyticsRows()) {
      const admin = row.projectAdmin?.trim();
      if (!admin) {
        continue;
      }
      for (const part of admin.split(',')) {
        if (part.trim()) {
          set.add(part.trim());
        }
      }
    }
    return [...set].sort((a, b) => a.localeCompare(b));
  });

  readonly statusOptions = computed(() => {
    const set = new Set<string>();
    for (const project of this.projects()) {
      if (project.status) {
        set.add(project.status);
      }
    }
    return [...set].sort((a, b) => a.localeCompare(b));
  });

  readonly filtered = computed(() => {
    const q = this.query().trim().toLowerCase();
    const status = this.statusFilter();
    const byId = this.analyticsById();
    return this.projects().filter((project) => {
      if (status && (project.status ?? '') !== status) {
        return false;
      }
      if (q) {
        const haystack = `${project.name} ${project.status ?? ''}`.toLowerCase();
        if (!haystack.includes(q)) {
          return false;
        }
      }
      const row = byId.get(project.id);
      if (this.criticalOnly() && !row?.critical) {
        return false;
      }
      if (this.delayedOnly() && !row?.delayed) {
        return false;
      }
      if (this.needsAttentionOnly() && !row?.needsAttention) {
        return false;
      }
      if (this.hasOverdueWp() && !(row && row.overdueWorkPackageCount > 0)) {
        return false;
      }
      const adminQ = this.projectAdmin().trim().toLowerCase();
      if (adminQ) {
        const admin = (row?.projectAdmin ?? '').toLowerCase();
        if (!admin || !admin.includes(adminQ)) {
          return false;
        }
      }
      if (!inRange(row?.healthScore ?? null, this.healthMin(), this.healthMax())) {
        return false;
      }
      if (!inRange(row?.riskScore ?? null, this.riskMin(), this.riskMax())) {
        return false;
      }
      const progress = row?.progress ?? project.progress;
      if (!inRange(progress, this.progressMin(), this.progressMax())) {
        return false;
      }
      return true;
    });
  });

  analyticsFor(projectId: string): ExplorerProjectRow | undefined {
    return this.analyticsById().get(projectId);
  }

  formatScore(value: number | null | undefined): string {
    return value == null ? '—' : String(value);
  }

  toNum(value: unknown): number | null {
    if (value === '' || value == null) {
      return null;
    }
    const n = Number(value);
    return Number.isFinite(n) ? n : null;
  }

  toggle(projectId: string, event: Event): void {
    const checked = (event.target as HTMLInputElement).checked;
    const next = new Set(this.selectedIds());
    if (checked) {
      next.add(projectId);
    } else {
      next.delete(projectId);
    }
    this.emit(next);
  }

  selectAllFiltered(): void {
    const next = new Set(this.selectedIds());
    for (const project of this.filtered()) {
      next.add(project.id);
    }
    this.emit(next);
  }

  clearSelection(): void {
    this.emit(new Set());
  }

  private emit(next: Set<string>): void {
    this.selectedIds.set(next);
    this.selectionChange.emit([...next]);
  }
}

function inRange(value: number | null, min: number | null, max: number | null): boolean {
  if (min == null && max == null) {
    return true;
  }
  if (value == null) {
    return false;
  }
  if (min != null && value < min) {
    return false;
  }
  if (max != null && value > max) {
    return false;
  }
  return true;
}
