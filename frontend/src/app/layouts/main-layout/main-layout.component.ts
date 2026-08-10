import { Component, OnInit, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { ConfigurationService } from '../../core/services/configuration.service';

/**
 * Primary application shell (header + navigation + content).
 * Layout only — no business logic.
 */
@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './main-layout.component.html',
  styleUrl: './main-layout.component.scss',
})
export class MainLayoutComponent implements OnInit {
  private readonly configuration = inject(ConfigurationService);
  private readonly authService = inject(AuthService);

  readonly applicationName = this.configuration.applicationName;
  readonly currentUser = this.authService.currentUser;

  ngOnInit(): void {
    if (this.authService.isAuthenticated() && !this.currentUser()) {
      this.authService.loadCurrentUser().subscribe();
    }
  }

  logout(): void {
    this.authService.logout().subscribe();
  }
}
