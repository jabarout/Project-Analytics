import { Routes } from '@angular/router';
import { PortfolioListPage } from './portfolio-list.page';
import { PortfolioDetailPage } from './portfolio-detail.page';

export const PORTFOLIO_ROUTES: Routes = [
  {
    path: '',
    component: PortfolioListPage,
  },
  {
    path: ':id',
    component: PortfolioDetailPage,
  },
];
