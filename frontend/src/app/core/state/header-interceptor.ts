import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { PlayerState } from './player';

export const headerInterceptor: HttpInterceptorFn = (req, next) => {
  const username = inject(PlayerState).name();

  return username !== null
    ? next(req.clone({ setHeaders: { Authorization: `Bearer ${username}` } }))
    : next(req);
};
