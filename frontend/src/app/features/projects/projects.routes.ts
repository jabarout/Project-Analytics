import { Routes } from '@angular/router';
import { ProjectDetailPage } from './project-detail.page';

export const PROJECTS_ROUTES: Routes = [
  {
    path: ':id',
    component: ProjectDetailPage,
  },
];
