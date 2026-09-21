import { Routes } from '@angular/router';
import { anonymousGuard } from './core/state/anonymous-guard';
import { playerGuard } from './core/state/player-guard';

export const routes: Routes = [
  {
    path: 'adventurer',
    canActivate: [anonymousGuard],
    loadComponent: () => import('./adventurer/adventurer-page').then((m) => m.AdventurerPage),
  },
  {
    path: '',
    loadComponent: () => import('./layout/layout').then((m) => m.Layout),
    children: [
      {
        path: '',
        loadComponent: () => import('./books/books-page').then((m) => m.BooksPage),
      },
      {
        path: 'games/:gameId',
        canActivate: [playerGuard],
        loadComponent: () => import('./game/game-page').then((m) => m.GamePage),
      },
      { path: '**', redirectTo: '' },
    ],
  },
];
