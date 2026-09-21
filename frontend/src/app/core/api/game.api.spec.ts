import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { GameApi } from './game.api';

describe('GameApi', () => {
  let api: GameApi;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    api = TestBed.inject(GameApi);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('makes a request to start a game by book id', () => {
    api.start(7).subscribe();

    const request = http.expectOne('/api/games');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ bookId: 7 });
    request.flush(null);
  });

  it('reads a single game by its id', () => {
    api.get('abc-123').subscribe();

    const request = http.expectOne('/api/games/abc-123');
    expect(request.request.method).toBe('GET');
    request.flush(null);
  });

  it('makes a choice within a game', () => {
    api.makeChoice('abc-123', 42).subscribe();

    const request = http.expectOne('/api/games/abc-123/choices/42');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toBeNull();
    request.flush(null);
  });

  it('lists the games in progress', () => {
    let received: unknown;
    api.listActiveGames().subscribe((games) => (received = games));

    const request = http.expectOne('/api/games');
    expect(request.request.method).toBe('GET');
    request.flush([{ gameId: 'abc-123', bookId: 7 }]);

    expect(received).toEqual([{ gameId: 'abc-123', bookId: 7 }]);
  });
});
