package com.adventurebook.backend.importer.validator;

import com.adventurebook.backend.exception.BookFileParseException;
import com.adventurebook.backend.importer.dto.BookFileDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BookValidator {
    private final BookParser parser;

    public BookValidator(
            BookParser parser
    ) {
        this.parser = parser;
    }

    public ParseResult validate(String json) {
        BookFileDto book;
        try {
            book = parser.parse(json);
        } catch (BookFileParseException ex) {
            return ParseResult.of(List.of(
                    ValidationError.of(
                            ValidationErrorType.UNPARSEABLE,
                            "Book file could not be parsed: " + ex.getMessage()
                    )
            ));
        }

        return ParseResult.of(book);
    }
}
