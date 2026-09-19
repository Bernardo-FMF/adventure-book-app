import { ChangeDetectionStrategy, Component, computed, input, output } from '@angular/core';
import { NgIcon, provideIcons } from '@ng-icons/core';
import { phosphorCaretLeftLight, phosphorCaretRightLight } from '@ng-icons/phosphor-icons/light';

const GAP = null;

@Component({
  selector: 'ab-pagination',
  imports: [NgIcon],
  viewProviders: [provideIcons({ phosphorCaretLeftLight, phosphorCaretRightLight })],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './pagination.html',
})
export class Pagination {
  readonly currentPage = input.required<number>();
  readonly totalPages = input.required<number>();
  readonly pageChange = output<number>();

  protected readonly isFirst = computed(() => this.currentPage() <= 0);
  protected readonly isLast = computed(() => this.currentPage() >= this.totalPages() - 1);

  protected readonly pages = computed<(number | null)[]>(() => {
    const total = this.totalPages();
    const current = this.currentPage();

    if (total <= 7) {
      return Array.from({ length: total }, (_, page) => page);
    }

    const shown = [0, current - 1, current, current + 1, total - 1]
      .filter((page) => page >= 0 && page < total)
      .sort((a, b) => a - b)
      .filter((page, index, all) => page !== all[index - 1]);

    return shown.flatMap((page, index) =>
      index > 0 && page - shown[index - 1] > 1 ? [GAP, page] : [page],
    );
  });
}
