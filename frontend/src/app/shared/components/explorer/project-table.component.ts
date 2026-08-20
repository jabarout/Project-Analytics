import { Component, computed, input, output } from '@angular/core';
import { RouterLink } from '@angular/router';
import {
  ExplorerColumnId,
  ExplorerProjectRow,
  ExplorerSortKey,
  ExplorerSortSpec,
} from '../../../core/models/explorer.model';

/**
 * Reusable project results table for Explorer (and optional embedded lists).
 */
@Component({
  selector: 'app-project-table',
  standalone: true,
  imports: [RouterLink],
  template: `
    <div class="ptable" [class.ptable--dense]="density() === 'dense'">
      <table>
        <thead>
          <tr>
            @for (col of columns(); track col) {
              <th>
                @if (isSortable(col)) {
                  <button type="button" class="pa-btn-reset ptable__sort" (click)="toggleSort(col)">
                    {{ columnLabel(col) }}
                    @if (sortMark(col); as mark) {
                      <span aria-hidden="true">{{ mark }}</span>
                    }
                  </button>
                } @else {
                  {{ columnLabel(col) }}
                }
              </th>
            }
          </tr>
        </thead>
        <tbody>
          @for (row of rows(); track row.projectId) {
            <tr>
              @for (col of columns(); track col) {
                <td>
                  @switch (col) {
                    @case ('name') {
                      <a [routerLink]="['/projects', row.projectId]">{{ row.name }}</a>
                    }
                    @case ('status') {
                      {{ row.status || '—' }}
                    }
                    @case ('progress') {
                      {{ row.progress != null ? row.progress + '%' : '—' }}
                    }
                    @case ('expectedProgress') {
                      {{ row.expectedProgress != null ? row.expectedProgress + '%' : '—' }}
                    }
                    @case ('progressGap') {
                      <span [class]="gapClass(row.progressGap)">
                        {{ formatGap(row.progressGap) }}
                      </span>
                    }
                    @case ('healthScore') {
                      <span [class]="scoreClass(row.healthScore, 'health')">
                        {{ formatScore(row.healthScore) }}
                      </span>
                    }
                    @case ('riskScore') {
                      <span [class]="scoreClass(row.riskScore, 'risk')">
                        {{ formatScore(row.riskScore) }}
                      </span>
                    }
                    @case ('attentionScore') {
                      <span [class]="scoreClass(row.attentionScore, 'attention')">
                        {{ formatScore(row.attentionScore) }}
                        @if (row.needsAttention) {
                          <span class="ptable__chip">Needs Attention</span>
                        }
                      </span>
                    }
                    @case ('delayed') {
                      {{ row.delayed ? 'Yes' : 'No' }}
                    }
                    @case ('overdueWorkPackageCount') {
                      {{ row.overdueWorkPackageCount }}
                    }
                    @case ('endDate') {
                      {{ row.endDate || '—' }}
                    }
                    @case ('nextDeadline') {
                      {{ row.nextDeadline || '—' }}
                      @if (row.nextDeadlineSource === 'work_package') {
                        <span class="ptable__chip">WP due</span>
                      }
                    }
                    @case ('projectAdmin') {
                      {{ row.projectAdmin || '—' }}
                    }
                    @case ('portfolioNames') {
                      {{ row.portfolioNames.length ? row.portfolioNames.join(', ') : '—' }}
                    }
                    @default {
                      —
                    }
                  }
                </td>
              }
            </tr>
          } @empty {
            <tr>
              <td [attr.colspan]="columns().length" class="ptable__empty">No projects match the current view.</td>
            </tr>
          }
        </tbody>
      </table>
    </div>
  `,
  styles: `
    .ptable {
      overflow: auto;
      border: 2px solid var(--pa-border-strong);
      border-radius: var(--pa-radius-lg);
      background: var(--pa-surface);
    }
    table {
      width: 100%;
      border-collapse: collapse;
      font-size: 0.9rem;
    }
    th,
    td {
      text-align: left;
      padding: 0.7rem 0.85rem;
      border-bottom: 1px solid var(--pa-border);
      white-space: nowrap;
    }
    th {
      font-size: 0.72rem;
      text-transform: uppercase;
      letter-spacing: 0.04em;
      color: var(--pa-text-secondary);
      background: var(--pa-surface-muted);
    }
    .ptable--dense th,
    .ptable--dense td {
      padding: 0.4rem 0.55rem;
      font-size: 0.82rem;
    }
    .ptable__sort {
      display: inline-flex;
      gap: 0.25rem;
      align-items: center;
      font: inherit;
      color: inherit;
      font-weight: inherit;
    }
    .ptable__empty {
      text-align: center;
      color: var(--pa-text-secondary);
      padding: 1.5rem !important;
    }
    .ptable__chip {
      display: inline-block;
      margin-left: 0.35rem;
      font-size: 0.65rem;
      padding: 0.1rem 0.35rem;
      border-radius: var(--pa-radius-pill);
      background: var(--pa-danger-muted);
      color: var(--pa-danger);
      vertical-align: middle;
    }
    .score--critical {
      color: var(--pa-danger);
      font-weight: 650;
    }
    .score--watch {
      color: var(--pa-warning);
      font-weight: 600;
    }
    .score--ok {
      color: var(--pa-success);
      font-weight: 600;
    }
    .gap--behind {
      color: var(--pa-danger);
      font-weight: 650;
    }
    .gap--ahead {
      color: var(--pa-success);
      font-weight: 600;
    }
    a {
      font-weight: 600;
      text-decoration: none;
    }
    a:hover {
      text-decoration: underline;
    }
  `,
})
export class ProjectTableComponent {
  readonly rows = input.required<readonly ExplorerProjectRow[]>();
  readonly columns = input.required<readonly ExplorerColumnId[]>();
  readonly sort = input<readonly ExplorerSortSpec[]>([]);
  readonly density = input<'comfortable' | 'dense'>('comfortable');
  readonly sortChange = output<ExplorerSortSpec>();

  readonly sortableKeys = computed(() => new Set<ExplorerSortKey>([
    'name',
    'status',
    'progress',
    'healthScore',
    'riskScore',
    'attentionScore',
    'delayed',
    'overdueWorkPackageCount',
    'endDate',
  ]));

  isSortable(col: ExplorerColumnId): boolean {
    return this.sortableKeys().has(col as ExplorerSortKey);
  }

  columnLabel(col: ExplorerColumnId): string {
    const labels: Record<ExplorerColumnId, string> = {
      name: 'Project',
      status: 'Status',
      progress: 'Actual progress',
      expectedProgress: 'Expected',
      progressGap: 'Progress gap',
      healthScore: 'Health',
      riskScore: 'Risk',
      attentionScore: 'Needs Attention',
      delayed: 'Delayed',
      overdueWorkPackageCount: 'Overdue WPs',
      endDate: 'Project finish',
      nextDeadline: 'Next deadline',
      projectAdmin: 'Project admin',
      portfolioNames: 'Portfolios',
      recommendations: 'Recos',
    };
    return labels[col] ?? col;
  }

  formatGap(value: number | null | undefined): string {
    if (value == null) {
      return '—';
    }
    const sign = value > 0 ? '+' : '';
    return `${sign}${value}`;
  }

  gapClass(value: number | null | undefined): string {
    if (value == null) {
      return '';
    }
    if (value < -5) {
      return 'gap--behind';
    }
    if (value > 5) {
      return 'gap--ahead';
    }
    return '';
  }

  sortMark(col: ExplorerColumnId): string {
    const match = this.sort().find((s) => s.key === col);
    if (!match) {
      return '';
    }
    return match.direction === 'asc' ? '↑' : '↓';
  }

  toggleSort(col: ExplorerColumnId): void {
    if (!this.isSortable(col)) {
      return;
    }
    const key = col as ExplorerSortKey;
    const current = this.sort().find((s) => s.key === key);
    const direction = current?.direction === 'desc' ? 'asc' : 'desc';
    this.sortChange.emit({ key, direction });
  }

  formatScore(value: number | null): string {
    return value == null ? '—' : String(value);
  }

  scoreClass(value: number | null, kind: 'health' | 'risk' | 'attention'): string {
    if (value == null) {
      return '';
    }
    if (kind === 'health') {
      if (value < 40) {
        return 'score--critical';
      }
      if (value < 70) {
        return 'score--watch';
      }
      return 'score--ok';
    }
    if (kind === 'risk' || kind === 'attention') {
      if (value >= 50) {
        return 'score--critical';
      }
      if (value >= 30) {
        return 'score--watch';
      }
      return 'score--ok';
    }
    return '';
  }
}
