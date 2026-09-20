import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { NgIcon, provideIcons } from '@ng-icons/core';
import { phosphorHeartLight } from '@ng-icons/phosphor-icons/light';

@Component({
  selector: 'ab-health-meter',
  imports: [NgIcon],
  viewProviders: [provideIcons({ phosphorHeartLight })],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './health-meter.html',
})
export class HealthMeter {
  readonly health = input.required<number>();
  readonly max = input.required<number>();

  protected readonly percentage = computed(() => (this.health() / this.max()) * 100);
}
