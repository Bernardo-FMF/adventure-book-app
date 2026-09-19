import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { NgIcon, provideIcons } from '@ng-icons/core';
import { phosphorBooksLight } from '@ng-icons/phosphor-icons/light';
import { BookApi } from '../core/api/book.api';
import { Book, Pagination } from '../core/api/book.models';
import { BookCard } from '../ui/book-card/book-card';
import { HeroBanner } from '../ui/hero-banner/hero-banner';
import { Pagination as PaginationControl } from '../ui/pagination/pagination';
import { catchError, of, Subject, switchMap, tap } from 'rxjs';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';

// Small value to be able to display the pagination
const PAGE_SIZE = 3;

@Component({
  selector: 'ab-books-page',
  imports: [BookCard, HeroBanner, NgIcon, PaginationControl],
  viewProviders: [provideIcons({ phosphorBooksLight })],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './books-page.html',
})
export class BooksPage {
  private readonly api = inject(BookApi);

  private readonly pageRequests = new Subject<number>();

  private readonly metadata = toSignal(this.api.getMetadata().pipe(catchError(() => of(null))), {
    initialValue: null,
  });
  protected readonly bookCount = computed(() => this.metadata()?.bookCount ?? null);

  protected readonly books = signal<Book[]>([]);
  protected readonly pagination = signal<Pagination | null>(null);
  protected readonly loading = signal(false);
  protected readonly failed = signal(false);

  constructor() {
    this.pageRequests
      .pipe(
        tap(() => {
          this.loading.set(true);
          this.failed.set(false);
        }),
        switchMap((page) =>
          this.api.getBooks({ page, size: PAGE_SIZE }).pipe(catchError(() => of(null))),
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
