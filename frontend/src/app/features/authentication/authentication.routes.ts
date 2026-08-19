import { Routes } from '@angular/router';
import { LoginPage } from './login.page';

export const AUTHENTICATION_ROUTES: Routes = [
  {
    path: '',
    component: LoginPage,
  },
  {
    path: 'reset-password',
    component: LoginPage,
  },
  {
    path: 'confirm-email',
    component: LoginPage,
  },
];
