package com.adventurebook.backend.importer.validator;

import com.adventurebook.backend.importer.dto.BookFileDto;

import java.util.List;
import java.util.Objects;

public record ParseResult(
        BookFileDto book,
        List<ValidationError> errors
) {
    public static ParseResult of(BookFileDto book) {
        return new ParseResult(book, List.of());
    }

    public static ParseResult of(List<ValidationError> errors) {
        return new ParseResult(null, errors);
    }

    public boolean isValid() {
        return this.errors.isEmpty() && Objects.nonNull(book);
    }
}
