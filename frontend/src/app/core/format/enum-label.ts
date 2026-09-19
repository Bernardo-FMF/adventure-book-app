import { Pipe, PipeTransform } from '@angular/core';

@Pipe({ name: 'enumLabel' })
export class EnumLabel implements PipeTransform {
  transform(value: string): string {
    return normalize(value);
  }
}

function normalize(value: string): string {
  return value
    .toLowerCase()
    .split('_')
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
    .join(' ');
}
