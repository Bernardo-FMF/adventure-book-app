import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { BookList } from '../core/api/book.models';
import { BooksPage } from './books-page';

const DEBOUNCE_MS = 300;

const emptyPage: BookList = {
  books: [],
  pagination: { totalPages: 3, currentPage: 0, totalElements: 9 },
};

describe('BooksPage', () => {
  let fixture: ComponentFixture<BooksPage>;
  let http: HttpTestingController;

  const start = () => {
    fixture = TestBed.createComponent(BooksPage);
    http.expectOne('/api/books/metadata').flush({
      bookCount: 9,
      genres: ['FANTASY', 'ADVENTURE'],
      difficulties: ['EASY', 'HARD'],
    });
    http.expectOne((r) => r.url === '/api/books').flush(emptyPage);
    fixture.detectChanges();
  };

  const pendingParams = (): URLSearchParams => {
    const request = http.expectOne((r) => r.url === '/api/books');
    request.flush(emptyPage);
    return new URLSearchParams(request.request.params.toString());
  };

  const type = (text: string) => {
    const input = fixture.nativeElement.querySelector('input[type="search"]') as HTMLInputElement;
    input.value = text;
    input.dispatchEvent(new Event('input'));
  };

  const clickChip = (label: string) => {
    const chip = [...fixture.nativeElement.querySelectorAll('ab-filter-chips button')].find(
      (button) => (button as HTMLElement).textContent?.trim() === label,
    );
    (chip as HTMLButtonElement).click();
  };

  beforeEach(async () => {
    vi.useFakeTimers();
    await TestBed.configureTestingModule({
      imports: [BooksPage],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    }).compileComponents();
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
    vi.useRealTimers();
  });

  it('asks for the first page as soon as it is shown', () => {
    fixture = TestBed.createComponent(BooksPage);
    http.expectOne('/api/books/metadata').flush({ bookCount: 0, genres: [], difficulties: [] });

    expect(pendingParams().get('page')).toBe('0');
  });

  it('waits for typing to settle, then searches once', () => {
    start();

    type('j');
    type('ja');
    type('jade');

    http.expectNone((r) => r.url === '/api/books');

    vi.advanceTimersByTime(DEBOUNCE_MS);

    expect(pendingParams().get('query')).toBe('jade');
  });

  it('does not search again when the text has not really changed', () => {
    start();

    type('jade');
    vi.advanceTimersByTime(DEBOUNCE_MS);
    expect(pendingParams().get('query')).toBe('jade');

    type('jade ');
    vi.advanceTimersByTime(DEBOUNCE_MS);

    http.expectNone((r) => r.url === '/api/books');
  });

  it('returns to the first page when a search is made', () => {
    start();

    fixture.componentInstance['loadPage'](2);
    expect(pendingParams().get('page')).toBe('2');

    type('jade');
    vi.advanceTimersByTime(DEBOUNCE_MS);

    const params = pendingParams();
    expect(params.get('page')).toBe('0');
    expect(params.get('query')).toBe('jade');
  });

  it('sends a filter and returns to the first page', () => {
    start();

    fixture.componentInstance['loadPage'](2);
    pendingParams();

    clickChip('Adventure');

    const params = pendingParams();
    expect(params.getAll('genre')).toEqual(['ADVENTURE']);
    expect(params.get('page')).toBe('0');
  });

  it('drops a filter when its chip is clicked again', () => {
    start();

    clickChip('Easy');
    expect(pendingParams().getAll('difficulty')).toEqual(['EASY']);

    clickChip('Easy');
    expect(pendingParams().has('difficulty')).toBe(false);
  });

  it('keeps the search when a filter is added', () => {
    start();

    type('jade');
    vi.advanceTimersByTime(DEBOUNCE_MS);
    pendingParams();

    clickChip('Adventure');

    const params = pendingParams();
    expect(params.get('query')).toBe('jade');
    expect(params.getAll('genre')).toEqual(['ADVENTURE']);
  });
});
