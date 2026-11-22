import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', title: 'maxmya // bench log', loadComponent: () => import('./pages/home').then((m) => m.Home) },
  { path: 'projects', title: 'Projects // maxmya', loadComponent: () => import('./pages/projects').then((m) => m.Projects) },
  { path: 'p/:slug', title: 'Project // maxmya', loadComponent: () => import('./pages/project').then((m) => m.ProjectPage) },
  { path: 'log', title: 'Log // maxmya', loadComponent: () => import('./pages/log').then((m) => m.Log) },
  { path: 'about', title: 'About // maxmya', loadComponent: () => import('./pages/about').then((m) => m.About) },
  { path: '**', loadComponent: () => import('./pages/not-found').then((m) => m.NotFound) }
];
