import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { NgIcon, provideIcons } from '@ng-icons/core';
import { phosphorBooksLight, phosphorFunnelLight } from '@ng-icons/phosphor-icons/light';
import { BookApi } from '../core/api/book.api';
import { Book, Difficulty, Genre, Pagination } from '../core/api/book.models';
import { BookCard } from '../ui/book-card/book-card';
import { HeroBanner } from '../ui/hero-banner/hero-banner';
import { Pagination as PaginationControl } from '../ui/pagination/pagination';
import {
  catchError,
  debounceTime,
  distinctUntilChanged,
  map,
  merge,
  of,
  Subject,
  switchMap,
  tap,
} from 'rxjs';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { SearchInput } from '../ui/search-input/search-input';
import { FilterChips } from '../ui/filter-chips/filter-chips';
import { GameApi } from '../core/api/game.api';
import { ActiveGames } from '../core/state/active-games';
import { Router } from '@angular/router';
import { PlayerState } from '../core/state/player';
import { HttpErrorResponse } from '@angular/common/http';

// Small value to be able to display the pagination
const PAGE_SIZE = 3;

const SEARCH_DEBOUNCE_MS = 300;

@Component({
  selector: 'ab-books-page',
  imports: [BookCard, HeroBanner, NgIcon, PaginationControl, SearchInput, FilterChips],
  viewProviders: [provideIcons({ phosphorBooksLight, phosphorFunnelLight })],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './books-page.html',
})
export class BooksPage {
  private readonly bookApi = inject(BookApi);
  private readonly gameApi = inject(GameApi);
  private readonly activeGames = inject(ActiveGames);
  private readonly router = inject(Router);
  private readonly playerState = inject(PlayerState);

  private readonly metadata = toSignal(
    this.bookApi.getMetadata().pipe(catchError(() => of(null))),
    {
      initialValue: null,
    },
  );
  protected readonly bookCount = computed(() => this.metadata()?.bookCount ?? null);
  protected readonly genreOptions = computed(() => this.metadata()?.genres ?? []);
  protected readonly difficultyOptions = computed(() => this.metadata()?.difficulties ?? []);

  protected readonly books = signal<Book[]>([]);
  protected readonly pagination = signal<Pagination | null>(null);
  protected readonly loading = signal(false);
  protected readonly failed = signal(false);
  protected readonly loadingGameStartOp = signal<number | null>(null);

  // Query variables
  private readonly page = signal(0);
  protected readonly searchTerm = signal('');
  protected readonly genres = signal<Genre[]>([]);
  protected readonly difficulties = signal<Difficulty[]>([]);

  // How book state changes are identified
  private readonly pageRequests = new Subject<number>();
  private readonly searchInput = new Subject<string>();

  constructor() {
    const searches = this.searchInput.pipe(
      debounceTime(SEARCH_DEBOUNCE_MS),
      map((term) => term.trim()),
      distinctUntilChanged(),
      tap((term) => {
        this.searchTerm.set(term);
        this.page.set(0);
      }),
    );

    const pages = this.pageRequests.pipe(tap((page) => this.page.set(page)));

    merge(searches, pages)
      .pipe(
        tap(() => {
          this.loading.set(true);
          this.failed.set(false);
        }),
        switchMap(() =>
          this.bookApi
            .getBooks({
              page: this.page(),
              size: PAGE_SIZE,
              query: this.searchTerm(),
              genres: this.genres(),
              difficulties: this.difficulties(),
            })
            .pipe(catchError(() => of(null))),
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

  protected onSearch(term: string): void {
    this.searchInput.next(term);
  }

  protected toggleDifficulty(difficulty: Difficulty): void {
    this.difficulties.update((curr) => this.toggle(curr, difficulty));
    this.loadPage(0);
  }

  protected toggleGenre(genre: Genre): void {
    this.genres.update((curr) => this.toggle(curr, genre));
    this.loadPage(0);
  }

  private toggle<T extends string>(current: readonly T[], value: NoInfer<T>): T[] {
    return current.includes(value) ? current.filter((v) => v !== value) : [...current, value];
  }

  protected hasGame(bookId: number): boolean {
    return this.activeGames.findGame(bookId) !== undefined;
  }

  protected play(book: Book): void {
    if (!this.playerState.hasSession()) {
      this.router.navigate(['/adventurer']);
      return;
    }

    const gameId = this.activeGames.findGame(book.id);
    if (gameId !== undefined) {
      this.router.navigate(['/games', gameId]);
      return;
    }

    if (this.loadingGameStartOp() !== null) {
      return;
    }

    this.loadingGameStartOp.set(book.id);

    this.gameApi.start(book.id).subscribe({
      next: (game) => {
        this.activeGames.remember({ gameId: game.id, bookId: book.id });
        this.router.navigate(['/games', game.id]);
      },
      error: (error: HttpErrorResponse) => {
        if (error.status === 409) {
          this.activeGames.fetch();
          this.loadingGameStartOp.set(null);
          return;
        }
        this.failed.set(true);
        this.loadingGameStartOp.set(null);
      },
    });
  }
}
