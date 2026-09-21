import { inject, Injectable, signal } from '@angular/core';
import { GameApi } from '../api/game.api';
import { GameSummary } from '../api/game.models';
import { catchError, Observable, of, tap } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ActiveGames {
  private readonly gameApi = inject(GameApi);
  private readonly activeGames = signal<GameSummary[]>([]);

  load(): Observable<GameSummary[]> {
    return this.gameApi.listActiveGames().pipe(tap((games) => this.activeGames.set(games)));
  }

  fetch(): void {
    this.load()
      .pipe(catchError(() => of([])))
      .subscribe();
  }

  // A game that has just been started is known in full, so it is added directly rather than refetched. Replacing any
  // entry for the same book keeps the one-active-game-per-book invariant that the database enforces.
  remember(summary: GameSummary): void {
    this.activeGames.update((games) => [...games.filter((game) => game.bookId !== summary.bookId), summary]);
  }

  findGame(bookId: number): string | undefined {
    return this.activeGames().find((game) => game.bookId === bookId)?.gameId;
  }

  remove(gameId: string): void {
    this.activeGames.update((games) => games.filter((game) => game.gameId !== gameId));
  }

  clear(): void {
    this.activeGames.set([]);
  }
}
