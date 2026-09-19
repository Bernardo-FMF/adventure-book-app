import { ChangeDetectionStrategy, Component, output } from '@angular/core';
import { NgIcon, provideIcons } from '@ng-icons/core';
import { phosphorMagnifyingGlassLight } from '@ng-icons/phosphor-icons/light';

@Component({
  selector: 'ab-search-input',
  imports: [NgIcon],
  viewProviders: [provideIcons({ phosphorMagnifyingGlassLight })],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './search-input.html',
})
export class SearchInput {
  readonly search = output<string>();

  protected onInput(event: Event): void {
    this.search.emit((event.target as HTMLInputElement).value);
  }
}
