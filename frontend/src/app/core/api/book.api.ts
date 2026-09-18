import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { BookList, BookQuery, Metadata } from './book.models';

@Injectable({ providedIn: 'root' })
export class BookApi {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/books';

  getBooks(query: BookQuery): Observable<BookList> {
    let params = new HttpParams();

    if (query.page) {
      params = params.set('page', query.page);
    }

    if (query.size) {
      params = params.set('size', query.size);
    }

    if (query.query?.trim()) {
      params = params.set('query', query.query.trim());
    }

    for (const difficulty of query.difficulties ?? []) {
      params = params.append('difficulty', difficulty);
    }

    for (const genre of query.genres ?? []) {
      params = params.append('genre', genre);
    }

    return this.http.get<BookList>(`${this.baseUrl}`, { params });
  }

  getMetadata(): Observable<Metadata> {
    return this.http.get<Metadata>(`${this.baseUrl}/metadata`);
  }
}
