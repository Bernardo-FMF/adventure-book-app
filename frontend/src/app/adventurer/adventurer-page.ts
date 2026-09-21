import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { PlayerState } from '../core/state/player';
import { ActiveGames } from '../core/state/active-games';
import { Router } from '@angular/router';
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
  protected readonly failed = signal(false);

  protected onInput(event: Event): void {
    this.name.set((event.target as HTMLInputElement).value);
    // Clearing on input, so the message does not sit there while the reader is already typing the fix.
    this.invalid.set(false);
  }

  protected onSubmit(event: Event): void {
    event.preventDefault();

    const username = this.name().trim();
    if (username.length === 0) {
      this.invalid.set(true);
      return;
    }

    this.failed.set(false);
    this.submitting.set(true);

    this.playerState.store(username);
    this.activeGames.load().subscribe({
      next: () => {
        this.router.navigate(['/']);
      },
      error: () => {
        this.playerState.remove();
        this.failed.set(true);
        this.submitting.set(false);
      },
    });
  }
}
