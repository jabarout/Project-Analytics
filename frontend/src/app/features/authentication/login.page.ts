import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';

@Component({
  selector: 'app-login-page',
  standalone: true,
  imports: [ReactiveFormsModule, LoadingSpinnerComponent],
  templateUrl: './login.page.html',
  styleUrl: './login.page.scss',
})
export class LoginPage implements OnInit {
  private readonly formBuilder = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly mode = signal<'login' | 'register' | 'forgot' | 'reset'>('login');
  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly infoMessage = signal<string | null>(null);

  readonly loginForm = this.formBuilder.nonNullable.group({
    username: ['', [Validators.required]],
    password: ['', [Validators.required]],
  });

  readonly registerForm = this.formBuilder.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    username: [''],
    password: ['', [Validators.required, Validators.minLength(8)]],
  });

  readonly forgotForm = this.formBuilder.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
  });

  readonly resetForm = this.formBuilder.nonNullable.group({
    token: ['', [Validators.required]],
    newPassword: ['', [Validators.required, Validators.minLength(8)]],
  });

  ngOnInit(): void {
    const token = this.route.snapshot.queryParamMap.get('token');
    if (this.router.url.includes('reset-password') || token) {
      this.mode.set('reset');
      if (token) {
        this.resetForm.patchValue({ token });
      }
    }
  }

  setMode(next: 'login' | 'register' | 'forgot' | 'reset'): void {
    this.mode.set(next);
    this.errorMessage.set(null);
    this.infoMessage.set(null);
  }

  submitLogin(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);

    this.authService.login(this.loginForm.getRawValue()).subscribe({
      next: () => {
        this.authService.loadCurrentUser().subscribe({
          next: () => {
            this.loading.set(false);
            void this.router.navigateByUrl('/');
          },
          error: () => {
            this.loading.set(false);
            void this.router.navigateByUrl('/');
          },
        });
      },
      error: (error: { error?: { error?: { message?: string } } }) => {
        this.loading.set(false);
        this.errorMessage.set(
          error?.error?.error?.message ?? 'Invalid credentials. Please try again.'
        );
      },
    });
  }

  submitRegister(): void {
    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMessage.set(null);

    const raw = this.registerForm.getRawValue();
    const payload = {
      email: raw.email.trim(),
      password: raw.password,
      username: raw.username.trim() ? raw.username.trim() : undefined,
    };

    this.authService.register(payload).subscribe({
      next: () => {
        this.authService.loadCurrentUser().subscribe({
          next: () => {
            this.loading.set(false);
            void this.router.navigateByUrl('/workspaces');
          },
          error: () => {
            this.loading.set(false);
            void this.router.navigateByUrl('/workspaces');
          },
        });
      },
      error: (error: { error?: { error?: { message?: string } } }) => {
        this.loading.set(false);
        this.errorMessage.set(
          error?.error?.error?.message ?? 'Unable to create account. Please try again.'
        );
      },
    });
  }

  submitForgot(): void {
    if (this.forgotForm.invalid) {
      this.forgotForm.markAllAsTouched();
      return;
    }
    this.loading.set(true);
    this.errorMessage.set(null);
    this.infoMessage.set(null);
    this.authService.forgotPassword({ email: this.forgotForm.getRawValue().email.trim() }).subscribe({
      next: (result) => {
        this.loading.set(false);
        this.infoMessage.set(
          result.message ||
            'If an account exists for that email, password reset instructions have been sent.'
        );
      },
      error: () => {
        this.loading.set(false);
        // Still show generic message (do not leak existence via errors).
        this.infoMessage.set(
          'If an account exists for that email, password reset instructions have been sent.'
        );
      },
    });
  }

  submitReset(): void {
    if (this.resetForm.invalid) {
      this.resetForm.markAllAsTouched();
      return;
    }
    this.loading.set(true);
    this.errorMessage.set(null);
    const raw = this.resetForm.getRawValue();
    this.authService
      .resetPassword({ token: raw.token.trim(), newPassword: raw.newPassword })
      .subscribe({
        next: (result) => {
          this.loading.set(false);
          this.infoMessage.set(result.message || 'Password updated.');
          this.setMode('login');
        },
        error: (error: { error?: { error?: { message?: string } } }) => {
          this.loading.set(false);
          this.errorMessage.set(
            error?.error?.error?.message ?? 'Unable to reset password. The link may be invalid or expired.'
          );
        },
      });
  }
}
