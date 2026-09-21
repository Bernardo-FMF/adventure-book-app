import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { headerInterceptor } from './header-interceptor';
import { PlayerState } from './player';

describe('headerInterceptor', () => {
  let http: HttpClient;
  let backend: HttpTestingController;
  let player: PlayerState;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([headerInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    http = TestBed.inject(HttpClient);
    backend = TestBed.inject(HttpTestingController);
    player = TestBed.inject(PlayerState);
  });

  afterEach(() => {
    backend.verify();
    localStorage.clear();
  });

  const send = (): ReturnType<HttpTestingController['expectOne']> => {
    http.get('/api/games').subscribe();
    const request = backend.expectOne('/api/games');
    request.flush([]);
    return request;
  };

  it('leaves a request alone when the username is not present', () => {
    expect(send().request.headers.has('Authorization')).toBe(false);
  });

  it('adds the authorization header when a username is present', () => {
    player.store('Bernardo');

    expect(send().request.headers.get('Authorization')).toBe('Bearer Bernardo');
  });
});
