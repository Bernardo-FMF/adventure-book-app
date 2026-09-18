import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { NgIcon, provideIcons } from '@ng-icons/core';
import { phosphorBooksLight } from '@ng-icons/phosphor-icons/light';
import { BookApi } from '../core/api/book.api';
import { Book, BookList, Pagination } from '../core/api/book.models';
import { BookCard } from '../ui/book-card/book-card';
import { HeroBanner } from '../ui/hero-banner/hero-banner';
import { catchError, map, of, Subject, switchMap, tap } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

const PAGE_SIZE = 12;

@Component({
  selector: 'ab-books-page',
  imports: [BookCard, HeroBanner, NgIcon],
  viewProviders: [provideIcons({ phosphorBooksLight })],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './books-page.html',
})
export class BooksPage {
  private readonly api = inject(BookApi);

  protected readonly pageRequests = new Subject<number>();

  protected readonly bookCount = signal<number | null>(null);

  protected readonly books = signal<Book[]>([]);
  protected readonly pagination = signal<Pagination | null>(null);
  protected readonly loading = signal(false);
  protected readonly failed = signal(false);

  constructor() {
    this.api.getMetadata().subscribe({
      next: (metadata) => this.bookCount.set(metadata.bookCount),
      error: () => this.bookCount.set(null),
    });

    this.pageRequests
      .pipe(
        tap(() => {
          this.loading.set(true);
          this.failed.set(false);
        }),
        switchMap((page) =>
          this.api.getBooks({ page, size: PAGE_SIZE }).pipe(
            map((result): BookList | null => result),
            catchError(() => of(null)),
          ),
        ),
        takeUntilDestroyed(),
      )
      .subscribe((result) => {
        this.books.set(result?.books ?? []);
        this.pagination.set(result?.pagination ?? null);
        this.failed.set(result === null);
        this.loading.set(false);
      });

    this.loadPage(0);
  }

  protected loadPage(page: number): void {
    this.pageRequests.next(page);
  }
}
