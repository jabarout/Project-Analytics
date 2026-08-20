import { Directive, ElementRef, OnDestroy, OnInit, inject, input } from '@angular/core';

/**
 * N4 scroll / viewport reveal — composed sections enter with a perceptible,
 * premium ease-out (not a micro-pop).
 *
 * Apply to meaningful content groups (section / chart container / card grid).
 * Do not nest reveals. Above-fold content is shown instantly (no motion).
 * Chart containers only — ECharts keeps its own series animation.
 *
 * Usage:
 *   <section paReveal>...</section>
 *   <div class="portfolios__grid" paReveal [paRevealStagger]="true">...</div>
 */
@Directive({
  selector: '[paReveal]',
  standalone: true,
})
export class PaRevealDirective implements OnInit, OnDestroy {
  private readonly host = inject(ElementRef<HTMLElement>);

  /**
   * When true, reveal each direct child with a deliberate capped stagger
   * (80ms steps, max 240ms) instead of the host as one block.
   * Prefer for small card grids only — not every KPI row.
   */
  readonly paRevealStagger = input(false);

  /** Extra delay steps for a single host reveal (0–3 typical). */
  readonly paRevealDelay = input(0);

  private observer: IntersectionObserver | null = null;

  ngOnInit(): void {
    const host = this.host.nativeElement;
    const reduced =
      typeof matchMedia !== 'undefined' && matchMedia('(prefers-reduced-motion: reduce)').matches;

    const stagger = this.paRevealStagger();
    const targets: HTMLElement[] = stagger
      ? (Array.from(host.children).filter((n) => n instanceof HTMLElement) as HTMLElement[])
      : [host];

    if (targets.length === 0) {
      return;
    }

    if (reduced) {
      for (const target of targets) {
        target.classList.add('pa-reveal', 'pa-reveal--visible', 'pa-reveal--instant');
      }
      return;
    }

    const toObserve: HTMLElement[] = [];

    targets.forEach((target, index) => {
      target.classList.add('pa-reveal');
      const steps = stagger ? index : this.paRevealDelay();
      const delayMs = Math.min(Math.max(steps, 0) * 80, 240);
      if (delayMs > 0) {
        target.style.setProperty('--pa-reveal-delay', `${delayMs}ms`);
      }

      // Already discoverable in the first screenful — present, don't animate.
      if (this.isAboveFold(target)) {
        target.classList.add('pa-reveal--visible', 'pa-reveal--instant');
        return;
      }

      toObserve.push(target);
    });

    if (toObserve.length === 0) {
      return;
    }

    this.observer = new IntersectionObserver(
      (entries) => {
        for (const entry of entries) {
          if (!entry.isIntersecting) {
            continue;
          }
          entry.target.classList.add('pa-reveal--visible');
          this.observer?.unobserve(entry.target);
        }
      },
      {
        root: null,
        // Reveal as the composed section enters view — early enough to feel natural.
        rootMargin: '0px 0px -8% 0px',
        threshold: 0.1,
      }
    );

    for (const target of toObserve) {
      this.observer.observe(target);
    }
  }

  ngOnDestroy(): void {
    this.observer?.disconnect();
    this.observer = null;
  }

  /**
   * Instant only for content clearly in the upper viewport (primary first screen).
   * Near-fold / below-fold sections keep the scroll reveal so the polished motion
   * starts from the beginning of the content flow (e.g. Home Visual), not mid-page.
   */
  private isAboveFold(el: HTMLElement): boolean {
    const rect = el.getBoundingClientRect();
    const vh = typeof window !== 'undefined' ? window.innerHeight : 800;
    return rect.top < vh * 0.58 && rect.bottom > 24;
  }
}
