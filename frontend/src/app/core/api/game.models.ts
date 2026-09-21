export type GameStatus = 'IN_PROGRESS' | 'DEAD' | 'FINISHED';
export type SectionType = 'BEGIN' | 'NODE' | 'END';
export type ConsequenceType = 'LOSE_HEALTH' | 'GAIN_HEALTH';

export const MAX_HEALTH = 10;

export interface GameSummary {
  gameId: string;
  bookId: number;
}

export interface GameState {
  id: string;
  bookSummary: BookSummary;
  status: GameStatus;
  health: number;
  section: Section;
  consequence: Consequence;
}

export interface BookSummary {
  id: number;
  title: string;
}

export interface Consequence {
  type: ConsequenceType;
  amount: number;
  text: string | null;
}

export interface Section {
  sectionRef: string;
  text: string;
  type: SectionType;
  options: Option[];
}

export interface Option {
  id: number;
  description: string;
}
