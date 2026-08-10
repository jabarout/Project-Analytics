import { Component, input } from '@angular/core';

@Component({
  selector: 'app-insight-list',
  standalone: true,
  template: `
    <section class="insights">
      <h3>{{ title() }}</h3>
      @if (items().length === 0) {
        <p class="insights__empty">No insights available.</p>
      } @else {
        <ul>
          @for (item of items(); track item) {
            <li>{{ item }}</li>
          }
        </ul>
      }
    </section>
  `,
  styles: `
    .insights {
      padding: 1.1rem;
      border: 1px solid var(--pa-border);
      border-radius: 12px;
      background: var(--pa-surface);
    }
    h3 {
      margin: 0 0 0.75rem;
      font-size: 1rem;
    }
    ul {
      margin: 0;
      padding-left: 1.15rem;
      line-height: 1.5;
    }
    .insights__empty {
      margin: 0;
      color: var(--pa-text-muted);
    }
  `,
})
export class InsightListComponent {
  readonly title = input('Insights');
  readonly items = input.required<readonly string[]>();
}
