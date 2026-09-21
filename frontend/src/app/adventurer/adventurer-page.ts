import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { PlayerState } from '../core/state/player';
import { ActiveGames } from '../core/state/active-games';
import { Router } from '@angular/router';
import { errorMessage } from '../core/api/error-message';
import { NgIcon, provideIcons } from '@ng-icons/core';
import { phosphorSparkleLight, phosphorUserLight } from '@ng-icons/phosphor-icons/light';

@Component({
  selector: 'ab-adventurer-page',
  imports: [NgIcon],
  viewProviders: [
    provideIcons({
      phosphorSparkleLight,
      phosphorUserLight,
    }),
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './adventurer-page.html',
})
export class AdventurerPage {
  private readonly playerState = inject(PlayerState);
  private readonly activeGames = inject(ActiveGames);
  private readonly router = inject(Router);

  protected readonly name = signal('');
  protected readonly submitting = signal(false);
  protected readonly invalid = signal(false);
  protected readonly error = signal<string | null>(null);

  protected onInput(event: Event): void {
    this.name.set((event.target as HTMLInputElement).value);
    this.invalid.set(false);
  }

  protected onSubmit(event: Event): void {
    event.preventDefault();

    const username = this.name().trim();
    if (username.length === 0) {
      this.invalid.set(true);
      return;
    }

    this.error.set(null);
    this.submitting.set(true);

    this.playerState.store(username);
    this.activeGames.load().subscribe({
      next: () => {
        this.router.navigate(['/']);
      },
      error: (failure: unknown) => {
        this.playerState.remove();
        this.error.set(errorMessage(failure, 'Failed to create adventurer session'));
        this.submitting.set(false);
      },
    });
  }
}
