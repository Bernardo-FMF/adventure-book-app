import { ChangeDetectionStrategy, Component, input } from '@angular/core';

@Component({
  selector: 'ab-game-page',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './game-page.html',
})
export class GamePage {
  readonly gameId = input.required<string>();
}
