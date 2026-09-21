import { Injectable, signal } from '@angular/core';

const STORAGE_KEY = 'adventure-book.username';

@Injectable({ providedIn: 'root' })
export class PlayerState {
  private readonly nameState = signal<string | null>(localStorage.getItem(STORAGE_KEY));
  readonly name = this.nameState.asReadonly();

  hasSession(): boolean {
    return this.name() !== null;
  }

  store(username: string): void {
    const trimmedUsername = username.trim();
    localStorage.setItem(STORAGE_KEY, username);
    this.nameState.set(trimmedUsername);
  }

  remove(): void {
    localStorage.removeItem(STORAGE_KEY);
    this.nameState.set(null);
  }
}
