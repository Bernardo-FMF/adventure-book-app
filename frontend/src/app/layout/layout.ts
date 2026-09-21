import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Header } from './header';
import { PlayerState } from '../core/state/player';
import { ActiveGames } from '../core/state/active-games';

@Component({
  selector: 'ab-layout',
  imports: [RouterOutlet, Header],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <ab-header />
    <router-outlet />
  `,
})
export class Layout {
  private readonly playerState = inject(PlayerState);
  private readonly activeGames = inject(ActiveGames);

  constructor() {
    if (this.playerState.hasSession()) {
      this.activeGames.fetch();
    }
  }
}
