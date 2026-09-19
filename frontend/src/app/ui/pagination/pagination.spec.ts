import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Pagination } from './pagination';

describe('Pagination', () => {
  let fixture: ComponentFixture<Pagination>;

  const render = async (currentPage: number, totalPages: number) => {
    fixture = TestBed.createComponent(Pagination);
    fixture.componentRef.setInput('currentPage', currentPage);
    fixture.componentRef.setInput('totalPages', totalPages);
    await fixture.whenStable();
    return fixture.nativeElement as HTMLElement;
  };

  const pageButtons = (el: HTMLElement) =>
    [...el.querySelectorAll('nav button')]
      .map((b) => b.textContent?.trim())
      .filter((text) => text !== 'Previous' && text !== 'Next');

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [Pagination] }).compileComponents();
  });

  it('stays visible but disabled when everything fits on one page', async () => {
    const el = await render(0, 1);
    const buttons = [...el.querySelectorAll('nav button')] as HTMLButtonElement[];

    expect(el.querySelector('nav')).not.toBeNull();
    expect(pageButtons(el)).toEqual(['1']);
    expect(buttons[0].disabled).toBe(true);
    expect(buttons[buttons.length - 1].disabled).toBe(true);
  });

  it('emits the zero based index of the page clicked', async () => {
    const el = await render(0, 3);
    const emitted: number[] = [];
    fixture.componentInstance.pageChange.subscribe((page) => emitted.push(page));

    const third = [...el.querySelectorAll('nav button')].find((b) => b.textContent?.trim() === '3');
    (third as HTMLButtonElement).click();

    expect(emitted).toEqual([2]);
  });

  it('disables previous on the first page and next on the last', async () => {
    const first = await render(0, 5);
    expect((first.querySelector('nav button') as HTMLButtonElement).disabled).toBe(true);

    const last = await render(4, 5);
    const buttons = [...last.querySelectorAll('nav button')] as HTMLButtonElement[];
    expect(buttons[buttons.length - 1].disabled).toBe(true);
  });

  it('collapses long ranges around the current page', async () => {
    const el = await render(10, 20);

    expect(pageButtons(el)).toEqual(['1', '10', '11', '12', '20']);
    expect(el.textContent).toBe(' Previous  1 … 10  11  12 … 20  Next ');
  });
});
