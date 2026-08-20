import { Routes } from '@angular/router';
import { authGuard, guestGuard } from './core/guards/auth.guard';
import { ContactPage } from './features/legal/contact.page';
import { PrivacyPage } from './features/legal/privacy.page';
import { TermsPage } from './features/legal/terms.page';
import { OAuthCompletePage } from './features/oauth/oauth-complete.page';
import { AuthLayoutComponent } from './layouts/auth-layout/auth-layout.component';
import { MainLayoutComponent } from './layouts/main-layout/main-layout.component';
import { PublicLayoutComponent } from './layouts/public-layout/public-layout.component';

/**
 * Application routes. Feature modules are lazy-loaded; protected routes use authGuard.
 * Legal pages and /oauth/complete are public (reachable without login).
 */
export const routes: Routes = [
  {
    path: 'login',
    component: AuthLayoutComponent,
    canActivate: [guestGuard],
    loadChildren: () =>
      import('./features/authentication/authentication.routes').then((m) => m.AUTHENTICATION_ROUTES),
  },
  {
    path: 'privacy',
    component: PublicLayoutComponent,
    children: [{ path: '', component: PrivacyPage }],
  },
  {
    path: 'contact',
    component: PublicLayoutComponent,
    children: [{ path: '', component: ContactPage }],
  },
  {
    path: 'terms',
    component: PublicLayoutComponent,
    children: [{ path: '', component: TermsPage }],
  },
  {
    path: 'oauth/complete',
    component: PublicLayoutComponent,
    children: [{ path: '', component: OAuthCompletePage }],
  },
  {
    path: '',
    component: MainLayoutComponent,
    canActivate: [authGuard],
    children: [
      {
        path: '',
        loadChildren: () => import('./features/home/home.routes').then((m) => m.HOME_ROUTES),
      },
      {
        path: 'explorer',
        loadChildren: () =>
          import('./features/explorer/explorer.routes').then((m) => m.EXPLORER_ROUTES),
      },
      {
        path: 'executive',
        loadChildren: () =>
          import('./features/dashboard/dashboard.routes').then((m) => m.DASHBOARD_ROUTES),
      },
      {
        path: 'workspaces',
        loadChildren: () =>
          import('./features/workspaces/workspaces.routes').then((m) => m.WORKSPACES_ROUTES),
      },
      {
        path: 'portfolios',
        loadChildren: () =>
          import('./features/portfolio/portfolio.routes').then((m) => m.PORTFOLIO_ROUTES),
      },
      {
        path: 'projects',
        loadChildren: () =>
          import('./features/projects/projects.routes').then((m) => m.PROJECTS_ROUTES),
      },
      {
        path: 'reports',
        loadChildren: () => import('./features/reports/reports.routes').then((m) => m.REPORTS_ROUTES),
      },
      {
        path: 'settings',
        loadChildren: () => import('./features/settings/settings.routes').then((m) => m.SETTINGS_ROUTES),
      },
    ],
  },
  {
    path: '**',
    redirectTo: '',
  },
];
