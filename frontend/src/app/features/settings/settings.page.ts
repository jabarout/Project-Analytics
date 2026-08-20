import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';
import { PaTheme, ThemeService } from '../../core/services/theme.service';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { PaRevealDirective } from '../../shared/directives/pa-reveal.directive';

@Component({
  selector: 'app-settings-page',
  standalone: true,
  imports: [
    PaRevealDirective,ReactiveFormsModule, LoadingSpinnerComponent],
  templateUrl: './settings.page.html',
  styleUrl: './settings.page.scss',
})
export class SettingsPage implements OnInit {
  private readonly formBuilder = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly themeService = inject(ThemeService);

  readonly loading = signal(false);
  readonly message = signal<string | null>(null);
  readonly errorMessage = signal<string | null>(null);
  readonly user = this.authService.currentUser;

  readonly form = this.formBuilder.nonNullable.group({
    theme: ['dark' as PaTheme, Validators.required],
  });

  ngOnInit(): void {
    const preferences = this.user()?.preferences;
    if (preferences) {
      this.form.patchValue({
        theme: (preferences.theme === 'dark' ? 'dark' : 'light') as PaTheme,
      });
    } else {
      this.form.patchValue({ theme: this.themeService.theme() });
    }

    this.form.controls.theme.valueChanges.subscribe((theme) => {
      if (theme === 'light' || theme === 'dark') {
        this.themeService.setTheme(theme);
      }
    });
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.message.set(null);
    this.errorMessage.set(null);

    const raw = this.form.getRawValue();
    this.themeService.setTheme(raw.theme as PaTheme);

    // Language is English-only in this version; always persist `en`.
    this.authService.updatePreferences({ theme: raw.theme, language: 'en' }).subscribe({
      next: () => {
        this.loading.set(false);
        this.message.set('Preferences saved.');
      },
      error: () => {
        this.loading.set(false);
        this.errorMessage.set('Unable to save preferences.');
      },
    });
  }
}
