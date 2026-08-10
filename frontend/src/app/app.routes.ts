import { Routes } from '@angular/router';
import { authGuard, guestGuard } from './core/guards/auth.guard';
import { AuthLayoutComponent } from './layouts/auth-layout/auth-layout.component';
import { MainLayoutComponent } from './layouts/main-layout/main-layout.component';

/**
 * Application routes. Feature modules are lazy-loaded; protected routes use authGuard.
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
