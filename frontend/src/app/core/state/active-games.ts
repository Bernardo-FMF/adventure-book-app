import { Injectable, signal } from '@angular/core';

const STORAGE_KEY = 'adventure-book.active-games';

@Injectable({ providedIn: 'root' })
export class ActiveGames {
  private readonly games = signal<Record<number, string>>(this.read());

  gameFor(bookId: number): string | undefined {
    return this.games()[bookId];
  }

  save(bookId: number, gameId: string): void {
    this.games.update((current) => ({ ...current, [bookId]: gameId }));
    this.write();
  }

  removeGame(gameId: string): void {
    this.games.update((current) =>
      Object.fromEntries(Object.entries(current).filter(([, id]) => id !== gameId)),
    );
    this.write();
  }

  private read(): Record<number, string> {
    return JSON.parse(localStorage.getItem(STORAGE_KEY) ?? '{}');
  }

  private write(): void {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(this.games()));
  }
}
