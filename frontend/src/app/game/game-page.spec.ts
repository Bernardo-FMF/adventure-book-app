import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { GameState } from '../core/api/game.models';
import { ActiveGames } from '../core/state/active-games';
import { GamePage } from './game-page';

const GAME_ID = 'abc-123';

const gameAt = (overrides: Partial<GameState> = {}): GameState => ({
  id: GAME_ID,
  bookSummary: { id: 7, title: 'The Mountain Pass' },
  status: 'IN_PROGRESS',
  health: 10,
  section: {
    sectionRef: '1',
    text: 'The pass is closed.',
    type: 'BEGIN',
    options: [{ id: 11, description: 'Take the old road' }],
  },
  consequence: null as unknown as GameState['consequence'],
  ...overrides,
});

describe('GamePage', () => {
  let fixture: ComponentFixture<GamePage>;
  let http: HttpTestingController;
  let router: Router;
  let navigate: ReturnType<typeof vi.fn>;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });
    http = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    navigate = vi.fn().mockResolvedValue(true);
    (router as unknown as { navigate: unknown }).navigate = navigate;
  });

  afterEach(() => {
    http.verify();
    localStorage.clear();
  });

  const start = (): void => {
    fixture = TestBed.createComponent(GamePage);
    fixture.componentRef.setInput('gameId', GAME_ID);
    fixture.detectChanges();
  };

  const loadWith = (state: GameState): void => {
    start();
    http.expectOne(`/api/games/${GAME_ID}`).flush(state);
    fixture.detectChanges();
  };

  const choose = (optionId: number): void => {
    (fixture.componentInstance as unknown as { choose(id: number): void }).choose(optionId);
  };

  it('loads the game named by the route', () => {
    loadWith(gameAt());

    expect(fixture.nativeElement.textContent).toContain('The pass is closed.');
    expect(fixture.nativeElement.textContent).toContain('The Mountain Pass');
  });

  it('sends a choice', () => {
    loadWith(gameAt());

    choose(11);

    const request = http.expectOne(`/api/games/${GAME_ID}/choices/11`);
    expect(request.request.method).toBe('POST');
    request.flush(gameAt({ section: { ...gameAt().section, text: 'Pines close in.' } }));
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Pines close in.');
  });

  it('removes a finished game from the active games', () => {
    const games = TestBed.inject(ActiveGames);
    games.remember({ gameId: GAME_ID, bookId: 7 });

    loadWith(gameAt({ status: 'FINISHED' }));

    expect(games.findGame(7)).toBeUndefined();
  });
});
