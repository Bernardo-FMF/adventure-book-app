import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';

@Component({
  selector: 'ab-choice-card',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './choice-card.html',
})
export class ChoiceCard {
  readonly position = input.required<number>();
  readonly description = input.required<string>();
  readonly disabled = input(false);

  readonly chosen = output<void>();
}
