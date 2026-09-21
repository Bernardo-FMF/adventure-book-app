import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { PlayerState } from './player';

export const playerGuard: CanActivateFn = () => {
  const player = inject(PlayerState);
  const router = inject(Router);
  return player.hasSession() || router.createUrlTree(['/adventurer']);
}
