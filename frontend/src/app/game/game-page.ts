import { ChangeDetectionStrategy, Component, inject, input, signal } from '@angular/core';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { NgIcon, provideIcons } from '@ng-icons/core';
import { phosphorArrowLeftLight, phosphorBookOpenLight } from '@ng-icons/phosphor-icons/light';
import { catchError, EMPTY, Observable, of, Subject, switchMap, tap } from 'rxjs';
import { GameApi } from '../core/api/game.api';
import { GameState, MAX_HEALTH } from '../core/api/game.models';
import { ActiveGames } from '../core/state/active-games';
import { ChoiceCard } from '../ui/choice-card/choice-card';
import { HealthMeter } from '../ui/health-meter/health-meter';

@Component({
  selector: 'ab-game-page',
  imports: [ChoiceCard, HealthMeter, NgIcon, RouterLink],
  viewProviders: [
    provideIcons({
      phosphorArrowLeftLight,
      phosphorBookOpenLight,
    }),
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './game-page.html',
})
export class GamePage {
  private readonly api = inject(GameApi);
  private readonly activeGames = inject(ActiveGames);
  private readonly router = inject(Router);

  // path param
  readonly gameId = input.required<string>();

  protected readonly maxHealth = MAX_HEALTH;
  protected readonly state = signal<GameState | null>(null);
  protected readonly loading = signal(true);
  protected readonly failed = signal(false);

  private readonly choices = new Subject<number>();

  constructor() {
    // Turn the gameId signal into an emitter, running if gameId changes value, preventing stale data.
    toObservable(this.gameId)
      .pipe(
        tap(() => this.setRequestState()),
        switchMap((id) => this.handleError(this.api.get(id))),
        takeUntilDestroyed(),
      )
      .subscribe((state) => this.handleStateChange(state));

    // When an option is pushed onto the choices subject, we make the request to make the choice to the backend.
    // This is necessary since the game progression is server-oriented.
    this.choices
      .pipe(
        tap(() => this.setRequestState()),
        switchMap((optionId) => this.handleError(this.api.makeChoice(this.gameId(), optionId))),
        takeUntilDestroyed(),
      )
      .subscribe((state) => this.handleStateChange(state));
  }

  protected choose(optionId: number): void {
    this.choices.next(optionId);
  }

  /**
   * The game state endpoints (get and makeChoice) can return different errors, depending on the scenario.
   * There are 2 use cases that change the UI behavior, so we need to handle them:
   * - If it's a 409, it means that the choice was invalid. This can mean 2 things - either the game was already over
   *   or the option chosen wasn't even a valid option given the current section.
   *   This branch can be tolerant to failures, we can just retry the get request once to obtain the most recent game state.
   * - If it's a 404, it means that the game wasn't found and the session we're trying to play is invalid.
   *   The solution is just to remove the game from the active sessions.
   */
  private handleError(call: Observable<GameState>): Observable<GameState | null> {
    return call.pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 409) {
          return this.api.get(this.gameId()).pipe(catchError(() => of(null)));
        }
        if (error.status === 404) {
          this.finish();
          return EMPTY;
        }
        return of(null);
      }),
    );
  }

  private setRequestState(): void {
    this.loading.set(true);
    this.failed.set(false);
  }

  private handleStateChange(state: GameState | null): void {
    if (state) {
      this.state.set(state);
      if (state.status !== 'IN_PROGRESS') {
        this.activeGames.remove(state.id);
      }
    }
    this.failed.set(state === null);
    this.loading.set(false);
  }

  private finish(): void {
    this.activeGames.remove(this.gameId());
    this.router.navigate(['/']);
  }
}
