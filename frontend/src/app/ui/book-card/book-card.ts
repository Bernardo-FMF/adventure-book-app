import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NgIcon, provideIcons } from '@ng-icons/core';
import { phosphorBookOpenLight, phosphorStarLight } from '@ng-icons/phosphor-icons/light';
import { Badge, BadgeTone } from '../badge/badge';
import { Book } from '../../core/api/book.models';

@Component({
  selector: 'ab-book-card',
  imports: [Badge, NgIcon, RouterLink],
  viewProviders: [provideIcons({ phosphorBookOpenLight, phosphorStarLight })],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './book-card.html',
})
export class BookCard {
  readonly book = input.required<Book>();

  protected tone(difficulty: string): BadgeTone {
    return difficulty.toLowerCase() as BadgeTone;
  }

  protected label(value: string): string {
    return value
      .toLowerCase()
      .split('_')
      .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
      .join(' ');
  }
}
