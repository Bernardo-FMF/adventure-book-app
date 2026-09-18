import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';

export type BadgeTone = 'easy' | 'medium' | 'hard' | 'neutral' | 'tag';

@Component({
  selector: 'ab-badge',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `<span [class]="classes()"><ng-content /></span>`,
})
export class Badge {
  readonly tone = input<BadgeTone>('neutral');

  private static readonly TONES: Record<BadgeTone, string> = {
    easy: 'bg-easy-bg text-easy-fg',
    medium: 'bg-medium-bg text-medium-fg',
    hard: 'bg-hard-bg text-hard-fg',
    neutral: 'bg-parchment-dim text-ink-soft',
    tag: 'bg-tag-bg text-tag-fg',
  };

  protected readonly classes = computed(
    () =>
      `inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-semibold ${Badge.TONES[this.tone()]}`,
  );
}
