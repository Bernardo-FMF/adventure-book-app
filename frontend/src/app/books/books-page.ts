import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { BookApi } from '../core/api/book.api';
import { HeroBanner } from '../ui/hero-banner/hero-banner';

@Component({
  selector: 'ab-books-page',
  imports: [HeroBanner],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './books-page.html',
})
export class BooksPage {
  private readonly api = inject(BookApi);

  protected readonly bookCount = signal<number | null>(null);

  constructor() {
    this.api.getMetadata().subscribe({
      next: (metadata) => this.bookCount.set(metadata.bookCount),
      error: () => this.bookCount.set(null),
    });
  }
}
