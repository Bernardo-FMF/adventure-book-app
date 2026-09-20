import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Book } from '../../core/api/book.models';
import { BookCard } from './book-card';

const BOOK: Book = {
  id: 3,
  slug: 'crystal-caverns',
  title: 'The Crystal Caverns',
  author: 'Evelyn Brightwater',
  difficulty: 'MEDIUM',
  genre: 'HIGH_FANTASY',
  tags: ['Magic', 'Crystals'],
  sectionsCount: 12,
  description: 'Deep beneath the mountain lies a network of crystal caves.',
};

describe('BookCard', () => {
  let fixture: ComponentFixture<BookCard>;

  const render = async (book: Book) => {
    fixture = TestBed.createComponent(BookCard);
    fixture.componentRef.setInput('book', book);
    await fixture.whenStable();
    return fixture.nativeElement as HTMLElement;
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BookCard],
    }).compileComponents();
  });

  it('shows the title, author and chapter count', async () => {
    const el = await render(BOOK);

    expect(el.querySelector('h3')?.textContent).toContain('The Crystal Caverns');
    expect(el.textContent).toContain('by Evelyn Brightwater');
    expect(el.textContent).toContain('12 chapters');
  });

  it('renders enum values as words', async () => {
    const el = await render(BOOK);

    expect(el.textContent).toContain('High Fantasy');
    expect(el.textContent).toContain('Medium');
  });

  it('reports the click rather than navigating itself', async () => {
    const el = await render(BOOK);
    let clicks = 0;
    fixture.componentInstance.startPlay.subscribe(() => (clicks += 1));

    (el.querySelector('button') as HTMLButtonElement).click();

    expect(clicks).toBe(1);
  });

  it('offers to continue a book that already has a game', async () => {
    fixture = TestBed.createComponent(BookCard);
    fixture.componentRef.setInput('book', BOOK);
    fixture.componentRef.setInput('inProgress', true);
    await fixture.whenStable();
    const el = fixture.nativeElement as HTMLElement;

    expect(el.querySelector('button')?.textContent).toContain('Continue Quest');
    expect(el.querySelector('button')?.getAttribute('aria-label')).toBe(
      'Continue The Crystal Caverns',
    );
  });

  it('does not offer the button while a game is being started', async () => {
    fixture = TestBed.createComponent(BookCard);
    fixture.componentRef.setInput('book', BOOK);
    fixture.componentRef.setInput('disabled', true);
    await fixture.whenStable();

    expect((fixture.nativeElement.querySelector('button') as HTMLButtonElement).disabled).toBe(
      true,
    );
  });

  it('omits the optional parts a book may not have', async () => {
    const el = await render({
      ...BOOK,
      author: null,
      description: null,
      difficulty: null,
      genre: null,
      tags: [],
    });

    expect(el.textContent).not.toContain('by ');
    expect(el.querySelectorAll('ab-badge').length).toBe(0);
    expect(el.textContent).toContain('12 chapters');
  });
});
