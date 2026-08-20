import { Component, computed, inject, input } from '@angular/core';
import { ThemeService } from '../../../core/services/theme.service';

/**
 * Theme-aware product logo. Does not recolor assets — swaps light/dark artwork.
 * - Dark theme → light-on-dark mark
 * - Light theme → dark-on-light mark
 */
@Component({
  selector: 'app-brand-logo',
  standalone: true,
  template: `
    <img
      class="brand-logo"
      [class.brand-logo--sm]="size() === 'sm'"
      [class.brand-logo--md]="size() === 'md'"
      [class.brand-logo--lg]="size() === 'lg'"
      [src]="src()"
      [alt]="alt()"
      decoding="async"
    />
  `,
  styles: `
    .brand-logo {
      display: block;
      width: auto;
      height: 2.25rem;
      object-fit: contain;
      object-position: left center;
    }
    .brand-logo--sm {
      height: 1.75rem;
    }
    .brand-logo--md {
      height: 2.25rem;
    }
    .brand-logo--lg {
      height: 3rem;
    }
  `,
})
export class BrandLogoComponent {
  private readonly themeService = inject(ThemeService);

  readonly size = input<'sm' | 'md' | 'lg'>('md');
  readonly alt = input('Project Analytics');

  /**
   * Files were visually verified:
   * - logo-on-dark.png = light-colored artwork for dark UI
   * - logo-on-light.png = dark-colored artwork for light UI
   */
  readonly src = computed(() =>
    this.themeService.theme() === 'dark'
      ? `/brand/logo-on-dark.png?v=2`
      : `/brand/logo-on-light.png?v=2`
  );
}
