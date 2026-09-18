package com.adventurebook.backend.importer.validator;

import com.adventurebook.backend.exception.BookFileParseException;
import com.adventurebook.backend.importer.dto.BookFileDto;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.Objects;

@Component
public class BookParser {
    private final ObjectMapper mapper;

    public BookParser(
            ObjectMapper mapper
    ) {
        this.mapper = mapper;
    }

    public BookFileDto parse(String json) {
        if (Objects.isNull(json) || json.isBlank()) {
            throw new BookFileParseException("File is empty");
        }
        try {
            return mapper.readValue(json, BookFileDto.class);
        } catch (Exception ex) {
            throw new BookFileParseException("Invalid JSON: " + ex.getMessage());
        }
    }
}
