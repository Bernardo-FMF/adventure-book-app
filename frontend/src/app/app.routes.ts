import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./books/books-page').then((m) => m.BooksPage),
  },
  {
    path: 'games/:gameId',
    loadComponent: () => import('./game/game-page').then((m) => m.GamePage),
  },
  { path: '**', redirectTo: '' }
];
