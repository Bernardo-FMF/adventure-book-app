import { PlayerState } from '../core/state/player';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { ActiveGames } from '../core/state/active-games';
import { Router, RouterLink } from '@angular/router';

@Component({
  selector: 'ab-header',
  imports: [RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './header.html',
})
export class Header {
  private readonly playerState = inject(PlayerState);
  private readonly activeGames = inject(ActiveGames);
  private readonly router = inject(Router);

  protected readonly name = this.playerState.name;

  protected remove(): void {
    this.playerState.remove();
    this.activeGames.clear();
    this.router.navigate(['/']);
  }
}
