import { Component, OnInit, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { ConfigurationService } from '../../core/services/configuration.service';
import { ThemeService } from '../../core/services/theme.service';
import { AppFooterComponent } from '../../shared/components/app-footer/app-footer.component';
import { BrandLogoComponent } from '../../shared/components/brand-logo/brand-logo.component';

/**
 * Primary application shell (header + navigation + content + footer).
 * Layout only — no business logic.
 */
@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, AppFooterComponent, BrandLogoComponent],
  templateUrl: './main-layout.component.html',
  styleUrl: './main-layout.component.scss',
})
export class MainLayoutComponent implements OnInit {
  private readonly configuration = inject(ConfigurationService);
  private readonly authService = inject(AuthService);
  private readonly themeService = inject(ThemeService);

  readonly applicationName = this.configuration.applicationName;
  readonly currentUser = this.authService.currentUser;

  ngOnInit(): void {
    if (this.authService.isAuthenticated() && !this.currentUser()) {
      this.authService.loadCurrentUser().subscribe({
        next: () => this.themeService.syncFromUserPreferences(),
      });
    } else {
      this.themeService.syncFromUserPreferences();
    }
  }

  logout(): void {
    this.authService.logout().subscribe();
  }
}
