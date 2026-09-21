import { HttpErrorResponse } from '@angular/common/http';

export function errorMessage(failure: unknown, fallback: string): string {
  if (failure instanceof HttpErrorResponse) {
    if (failure.status === 0) {
      return 'The server could not be reached.';
    }

    const detail = (failure.error as { detail?: unknown } | null)?.detail;
    if (typeof detail === 'string' && detail.length > 0) {
      return detail;
    }
  }

  return fallback;
}
