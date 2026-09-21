import { TestBed } from '@angular/core/testing';
import { PlayerState } from './player';

describe('PlayerState', () => {
  const create = () => {
    TestBed.resetTestingModule();
    return TestBed.inject(PlayerState);
  };

  beforeEach(() => localStorage.clear());
  afterEach(() => localStorage.clear());

  it('starts with no session when nothing was stored', () => {
    const player = create();

    expect(player.name()).toBeNull();
    expect(player.hasSession()).toBe(false);
  });

  it('reports a session once a name is stored', () => {
    const player = create();

    player.store('Bernardo');

    expect(player.name()).toBe('Bernardo');
    expect(player.hasSession()).toBe(true);
  });

  it('keeps the name for the next reload', () => {
    create().store('Bernardo');

    expect(create().name()).toBe('Bernardo');
  });

  it('removes all traces of the session upon calling remove', () => {
    const player = create();
    player.store('Bernardo');

    player.remove();

    expect(player.name()).toBeNull();
    expect(player.hasSession()).toBe(false);
    expect(create().name()).toBeNull();
  });
});
