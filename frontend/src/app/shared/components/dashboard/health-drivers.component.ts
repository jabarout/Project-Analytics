import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';

export interface HealthDriverRow {
  readonly projectId: string;
  readonly projectName: string;
  readonly firstHealthScore: number;
  readonly lastHealthScore: number;
  readonly delta: number;
}

/**
 * Compact ranked Health Δ drivers — improving vs worsening.
 */
@Component({
  selector: 'app-health-drivers',
  standalone: true,
  imports: [RouterLink],
  template: `
    <section class="drivers">
      <header class="drivers__header">
        <h3>{{ title() }}</h3>
        <p class="drivers__hint">{{ subtitle() }}</p>
      </header>
      <div class="drivers__cols">
        <div class="drivers__col drivers__col--up">
          <h4>Improving</h4>
          @if (improving().length === 0) {
            <p class="drivers__empty">No improving projects in snapshot history.</p>
          } @else {
            <ul>
              @for (row of improving(); track row.projectId) {
                <li>
                  <a [routerLink]="['/projects', row.projectId]">{{ row.projectName }}</a>
                  <span class="drivers__delta drivers__delta--up">↑ {{ formatDelta(row.delta) }}</span>
                  <span class="drivers__scores">{{ formatScore(row.firstHealthScore) }} → {{ formatScore(row.lastHealthScore) }}</span>
                </li>
              }
            </ul>
          }
        </div>
        <div class="drivers__col drivers__col--down">
          <h4>Worsening</h4>
          @if (worsening().length === 0) {
            <p class="drivers__empty">No worsening projects in snapshot history.</p>
          } @else {
            <ul>
              @for (row of worsening(); track row.projectId) {
                <li>
                  <a [routerLink]="['/projects', row.projectId]">{{ row.projectName }}</a>
                  <span class="drivers__delta drivers__delta--down">↓ {{ formatDelta(row.delta) }}</span>
                  <span class="drivers__scores">{{ formatScore(row.firstHealthScore) }} → {{ formatScore(row.lastHealthScore) }}</span>
                </li>
              }
            </ul>
          }
        </div>
      </div>
    </section>
  `,
  styles: `
    .drivers {
      padding: 1.1rem 1.2rem;
      border: 2px solid var(--pa-border-strong);
      border-radius: var(--pa-radius-lg);
      background:
        linear-gradient(165deg, color-mix(in srgb, var(--pa-surface-muted) 45%, transparent), transparent 48%),
        var(--pa-surface);
    }
    .drivers__header {
      margin-bottom: 0.85rem;
    }
    h3 {
      margin: 0;
      font-size: 0.82rem;
      font-weight: 700;
      letter-spacing: 0.02em;
      text-transform: uppercase;
      color: var(--pa-text-secondary);
    }
    .drivers__hint {
      margin: 0.3rem 0 0;
      font-size: var(--pa-font-xs);
      color: var(--pa-text-tertiary);
      line-height: 1.4;
    }
    .drivers__cols {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 1rem;
    }
    @media (max-width: 720px) {
      .drivers__cols {
        grid-template-columns: 1fr;
      }
    }
    h4 {
      margin: 0 0 0.55rem;
      font-size: 0.78rem;
      font-weight: 750;
      letter-spacing: 0.04em;
      text-transform: uppercase;
    }
    .drivers__col--up h4 {
      color: var(--pa-viz-up, var(--pa-success));
    }
    .drivers__col--down h4 {
      color: var(--pa-viz-down, var(--pa-danger));
    }
    ul {
      list-style: none;
      margin: 0;
      padding: 0;
      display: flex;
      flex-direction: column;
      gap: 0.55rem;
    }
    li {
      display: grid;
      grid-template-columns: minmax(0, 1fr) auto;
      gap: 0.2rem 0.65rem;
      padding: 0.55rem 0.65rem;
      border-radius: var(--pa-radius-md);
      border: 1px solid var(--pa-border);
      background: var(--pa-surface);
    }
    .drivers__col--up li {
      border-color: color-mix(in srgb, var(--pa-viz-up, var(--pa-success)) 30%, var(--pa-border));
      background: linear-gradient(90deg, color-mix(in srgb, var(--pa-viz-up, var(--pa-success)) 10%, transparent), var(--pa-surface));
    }
    .drivers__col--down li {
      border-color: color-mix(in srgb, var(--pa-viz-down, var(--pa-danger)) 30%, var(--pa-border));
      background: linear-gradient(90deg, color-mix(in srgb, var(--pa-viz-down, var(--pa-danger)) 10%, transparent), var(--pa-surface));
    }
    a {
      grid-column: 1;
      font-weight: 650;
      color: var(--pa-text);
      text-decoration: none;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    a:hover {
      text-decoration: underline;
    }
    .drivers__delta {
      grid-column: 2;
      grid-row: 1;
      font-weight: 750;
      font-variant-numeric: tabular-nums;
      font-size: 0.88rem;
    }
    .drivers__delta--up {
      color: var(--pa-viz-up, var(--pa-success));
    }
    .drivers__delta--down {
      color: var(--pa-viz-down, var(--pa-danger));
    }
    .drivers__scores {
      grid-column: 1 / -1;
      font-size: 0.75rem;
      color: var(--pa-text-tertiary);
      font-variant-numeric: tabular-nums;
    }
    .drivers__empty {
      margin: 0;
      font-size: 0.85rem;
      color: var(--pa-text-secondary);
    }
  `,
})
export class HealthDriversComponent {
  readonly title = input('Health drivers');
  readonly subtitle = input('Largest Health score changes across stored snapshots (last − first).');
  readonly improving = input.required<readonly HealthDriverRow[]>();
  readonly worsening = input.required<readonly HealthDriverRow[]>();

  formatDelta(delta: number): string {
    const v = Math.round(delta * 10) / 10;
    return v > 0 ? `+${v}` : `${v}`;
  }

  formatScore(score: number): string {
    return `${Math.round(score * 10) / 10}`;
  }
}
