import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { GameState, GameSummary } from './game.models';

@Injectable({ providedIn: 'root' })
export class GameApi {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/games';

  start(bookId: number): Observable<GameState> {
    return this.http.post<GameState>(this.baseUrl, { bookId });
  }

  get(gameId: string): Observable<GameState> {
    return this.http.get<GameState>(`${this.baseUrl}/${gameId}`);
  }

  makeChoice(gameId: string, optionId: number): Observable<GameState> {
    return this.http.post<GameState>(`${this.baseUrl}/${gameId}/choices/${optionId}`, null);
  }

  listActiveGames(): Observable<GameSummary[]> {
    return this.http.get<GameSummary[]>(this.baseUrl);
  }
}
