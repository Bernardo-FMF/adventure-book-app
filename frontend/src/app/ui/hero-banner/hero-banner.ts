import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { NgIcon, provideIcons } from '@ng-icons/core';
import { phosphorBookOpenLight, phosphorSparkleLight } from '@ng-icons/phosphor-icons/light';

@Component({
  selector: 'ab-hero-banner',
  imports: [NgIcon],
  viewProviders: [provideIcons({ phosphorSparkleLight, phosphorBookOpenLight })],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './hero-banner.html',
})
export class HeroBanner {
  readonly bookCount = input<number | null>(null);
}
