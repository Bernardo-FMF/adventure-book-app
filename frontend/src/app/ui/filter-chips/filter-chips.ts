import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { EnumLabel } from '../../core/format/enum-label';

@Component({
  selector: 'ab-filter-chips',
  imports: [EnumLabel],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './filter-chips.html',
})
export class FilterChips<T extends string> {
  readonly options = input.required<readonly T[]>();
  readonly selected = input.required<readonly T[]>();

  public readonly toggled = output<T>();

  protected isSelected(option: T): boolean {
    return this.selected().includes(option);
  }
}
