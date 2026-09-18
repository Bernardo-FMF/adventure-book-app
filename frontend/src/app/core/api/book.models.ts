export type Difficulty = 'EASY' | 'MEDIUM' | 'HARD';
export type Genre = 'FANTASY' | 'HIGH_FANTASY' | 'ADVENTURE' | 'STEAMPUNK_MYSTERY' | 'MYSTERY';

export interface Book {
  slug: string;
  title: string;
  author: string | null;
  difficulty: Difficulty | null;
  genre: Genre | null;
  tags: string[];
  sectionsCount: number;
  description: string | null;
}

export interface Pagination {
  totalPages: number;
  currentPage: number;
  totalElements: number;
}

export interface BookList {
  books: Book[];
  pagination: Pagination;
}

export interface Metadata {
  bookCount: number;
}

export interface BookQuery {
  query?: string;
  difficulties?: Difficulty[];
  genres?: Genre[];
  page?: number;
  size?: number;
}
