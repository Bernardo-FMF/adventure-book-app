import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { Book } from '../../core/api/book.models';
import { BookCard } from './book-card';

const BOOK: Book = {
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
      providers: [provideRouter([])],
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

  it('links to the book', async () => {
    const el = await render(BOOK);

    expect(el.querySelector('a')?.getAttribute('href')).toBe('/books/crystal-caverns');
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
