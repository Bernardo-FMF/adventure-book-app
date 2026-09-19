import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { BookApi } from './book.api';

describe('BookApi', () => {
  let api: BookApi;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    api = TestBed.inject(BookApi);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  const paramsOf = (): URLSearchParams => {
    const request = http.expectOne((r) => r.url === '/api/books');
    request.flush({ books: [], pagination: { totalPages: 0, currentPage: 0, totalElements: 0 } });
    return new URLSearchParams(request.request.params.toString());
  };

  it('trims the query and omits it when it holds nothing', () => {
    api.getBooks({ page: 0, query: '  jade  ' }).subscribe();
    expect(paramsOf().get('query')).toBe('jade');

    api.getBooks({ page: 0, query: '   ' }).subscribe();
    expect(paramsOf().has('query')).toBe(false);
  });

  it('repeats a parameter per value, under the name the backend binds', () => {
    api.getBooks({ page: 0, difficulties: ['EASY', 'HARD'], genres: ['FANTASY'] }).subscribe();

    const params = paramsOf();
    expect(params.getAll('difficulty')).toEqual(['EASY', 'HARD']);
    expect(params.getAll('genre')).toEqual(['FANTASY']);
  });

  it('omits the filters entirely when none are chosen', () => {
    api.getBooks({ page: 0, difficulties: [], genres: [] }).subscribe();

    const params = paramsOf();
    expect(params.has('difficulty')).toBe(false);
    expect(params.has('genre')).toBe(false);
  });
});
