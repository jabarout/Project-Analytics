import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-settings-page',
  standalone: true,
  imports: [ReactiveFormsModule, LoadingSpinnerComponent],
  templateUrl: './settings.page.html',
  styleUrl: './settings.page.scss',
})
export class SettingsPage implements OnInit {
  private readonly formBuilder = inject(FormBuilder);
  private readonly authService = inject(AuthService);

  readonly loading = signal(false);
  readonly message = signal<string | null>(null);
  readonly errorMessage = signal<string | null>(null);
  readonly user = this.authService.currentUser;

  readonly form = this.formBuilder.nonNullable.group({
    theme: ['light', Validators.required],
    language: ['en', Validators.required],
  });

  ngOnInit(): void {
    const preferences = this.user()?.preferences;
    if (preferences) {
      this.form.patchValue({
        theme: preferences.theme,
        language: preferences.language,
      });
    }
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.message.set(null);
    this.errorMessage.set(null);

    this.authService.updatePreferences(this.form.getRawValue()).subscribe({
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
