import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { GameSummary } from '../api/game.models';
import { ActiveGames } from './active-games';

describe('ActiveGames', () => {
  let games: ActiveGames;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    games = TestBed.inject(ActiveGames);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  const flush = (body: GameSummary[]) => http.expectOne('/api/games').flush(body);

  it('finds the game for a book once loaded', () => {
    games.load().subscribe();
    flush([{ gameId: 'abc', bookId: 7 }]);

    expect(games.findGame(7)).toBe('abc');
    expect(games.findGame(8)).toBeUndefined();
  });

  it('adds a freshly started game without asking the backend again', () => {
    games.remember({ gameId: 'abc', bookId: 7 });

    expect(games.findGame(7)).toBe('abc');
  });

  it('clears removes all active games', () => {
    games.remember({ gameId: 'abc', bookId: 7 });

    games.clear();

    expect(games.findGame(7)).toBeUndefined();
  });
});
