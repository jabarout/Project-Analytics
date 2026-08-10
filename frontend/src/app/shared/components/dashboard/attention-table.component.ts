import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ProjectAttentionSummary } from '../../../core/models/analytics.model';

@Component({
  selector: 'app-attention-table',
  standalone: true,
  imports: [RouterLink],
  template: `
    <section class="table-widget">
      <h3>{{ title() }}</h3>
      @if (projects().length === 0) {
        <p class="table-widget__empty">No projects to display.</p>
      } @else {
        <table>
          <thead>
            <tr>
              <th>Project</th>
              <th>Health</th>
              <th>Risk</th>
              <th>Attention</th>
            </tr>
          </thead>
          <tbody>
            @for (project of projects(); track project.projectId) {
              <tr>
                <td>
                  <a [routerLink]="['/projects', project.projectId]">{{ project.projectName }}</a>
                </td>
                <td>{{ formatScore(project.healthScore) }} {{ project.healthStatus ? '(' + project.healthStatus + ')' : '' }}</td>
                <td>{{ formatScore(project.riskScore) }} {{ project.riskLevel ? '(' + project.riskLevel + ')' : '' }}</td>
                <td>{{ formatScore(project.attentionScore) }} {{ project.attentionLabel ? '(' + project.attentionLabel + ')' : '' }}</td>
              </tr>
            }
          </tbody>
        </table>
      }
    </section>
  `,
  styles: `
    .table-widget {
      padding: 1.1rem;
      border: 1px solid var(--pa-border);
      border-radius: 12px;
      background: var(--pa-surface);
      overflow: auto;
    }
    h3 {
      margin: 0 0 0.75rem;
    }
    table {
      width: 100%;
      border-collapse: collapse;
    }
    th,
    td {
      text-align: left;
      padding: 0.55rem 0.35rem;
      border-bottom: 1px solid var(--pa-border);
      font-size: 0.92rem;
    }
    th {
      font-size: 0.72rem;
      text-transform: uppercase;
      letter-spacing: 0.05em;
      color: var(--pa-text-muted);
    }
    a {
      color: #1d4ed8;
      text-decoration: none;
      font-weight: 600;
    }
    .table-widget__empty {
      margin: 0;
      color: var(--pa-text-muted);
    }
  `,
})
export class AttentionTableComponent {
  readonly title = input('Attention ranking');
  readonly projects = input.required<readonly ProjectAttentionSummary[]>();

  formatScore(value: number | null): string {
    return value == null ? '—' : String(value);
  }
}
