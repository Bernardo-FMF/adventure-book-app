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
import { errorMessage } from '../core/api/error-message';

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
  protected readonly error = signal<string | null>(null);
  protected readonly loadingGameStartOp = signal<number | null>(null);

  // Query variables
  private readonly page = signal(0);
  protected readonly searchTerm = signal('');
  protected readonly genres = signal<Genre[]>([]);
  protected readonly difficulties = signal<Difficulty[]>([]);

  // How book state changes are identified
  // We'll use subjects instead of signals, because with signals we aren't able to express debouncing and cancellation.
  // The search functionality will make multiple requests depending on user input, so a request that has an old search term
  // has to be canceled so that slow responses don't overwrite the newer state.
  private readonly pageRequests = new Subject<number>();
  private readonly searchInput = new Subject<string>();

  constructor() {
    // Divided the searches and regular paginated loading into two separate sources.
    // The search loading uses a debounce time so that we don't make a request for every input the user makes.
    // The term is trimmed so that requests are only made for distinct values, so 'xpto' and 'xpto ' are the same search term
    // and a new request will not be made.
    // An input change means that the search term is different, se we reset the page to 0.
    const searches = this.searchInput.pipe(
      debounceTime(SEARCH_DEBOUNCE_MS),
      map((term) => term.trim()),
      distinctUntilChanged(),
      tap((term) => {
        this.searchTerm.set(term);
        this.page.set(0);
      }),
    );

    //This is used for hte regular paginated loading and also the filter chips.
    // The two sources are differentiated because reusing them meant that we'd be debouncing on the regular loading.
    const pages = this.pageRequests.pipe(tap((page) => this.page.set(page)));

    // We can merge both pipes because besides the debouncing, they have the same logic: Fetch the books using the
    // defined page and filters.
    // SwitchMap is used because it starts the request and can cancel the request as later requests arrive, meaning we're
    // safe from overwriting the latest requests response with an earlier response that was slow to reach the client.
    // The api response is piped so that inner errors don't reach the other stream. If that happened, the stream would end
    // and the page would stop responding.
    // When the component is destroyed, it's possible to have ongoing requests, so we unsubscribe the pipe to avoid having
    // responses reach a dead component.
    merge(searches, pages)
      .pipe(
        tap(() => {
          this.loading.set(true);
          this.error.set(null);
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
            .pipe(
              catchError((failure: unknown) => {
                this.error.set(errorMessage(failure, 'Failed to obtain book listing'));
                return of(null);
              }),
            ),
        ),
        takeUntilDestroyed(),
      )
      .subscribe((result) => {
        this.books.set(result?.books ?? []);
        this.pagination.set(result?.pagination ?? null);
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

    // If we have a valid response to the start request, we'll store it in the active games cache without refetching.
    // This keeps the client consistent without making extra requests, but having multiple tabs open will show inconsistencies,
    // since this cache storage won't be present in the other tab.
    // A 409 error means that the game already exists for the player. However, we don't know the id of the game, so we
    // refetch to make sure the active games list is up to date.
    this.gameApi.start(book.id).subscribe({
      next: (game) => {
        this.activeGames.remember({ gameId: game.id, bookId: book.id });
        this.router.navigate(['/games', game.id]);
      },
      error: (error: HttpErrorResponse) => {
        if (error.status === 409) {
          this.activeGames.load().subscribe({
            next: () => this.loadingGameStartOp.set(null),
            error: (failure: unknown) => {
              this.error.set(errorMessage(failure, 'That adventure is already underway.'));
              this.loadingGameStartOp.set(null);
            },
          });
          return;
        }
        this.error.set(errorMessage(error, 'The adventure could not be started.'));
        this.loadingGameStartOp.set(null);
      },
    });
  }
}
