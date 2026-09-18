import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', loadComponent: () => import('./books/books-page').then((m) => m.BooksPage) },
];
