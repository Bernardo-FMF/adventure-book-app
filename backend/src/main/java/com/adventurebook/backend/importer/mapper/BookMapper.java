package com.adventurebook.backend.importer.mapper;

import com.adventurebook.backend.exception.BookFileParseException;
import com.adventurebook.backend.importer.dto.BookFileDto;
import com.adventurebook.backend.importer.dto.SectionDto;
import com.adventurebook.backend.persistence.BookEntity;
import com.adventurebook.backend.persistence.types.Difficulty;
import com.adventurebook.backend.persistence.types.Genre;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.adventurebook.backend.utils.StringUtils.blankToNull;
import static com.adventurebook.backend.utils.StringUtils.upperCaseOrNull;

@Component
public class BookMapper {
    private final SectionMapper sectionMapper;

    public BookMapper(
            SectionMapper sectionMapper
    ) {
        this.sectionMapper = sectionMapper;
    }

    public BookEntity toEntity(BookFileDto book, String slug) {
        String title = blankToNull(book.title());
        if (Objects.isNull(title)) {
            throw new BookFileParseException("Book without a title");
        }

        BookEntity entity = new BookEntity(slug, title);
        entity.setAuthor(blankToNull(book.author()));
        entity.setDifficulty(optionalEnum(Difficulty.class, book.difficulty(), "difficulty"));
        entity.setGenre(optionalEnum(Genre.class, book.genre(), "genre"));
        entity.setDescription(blankToNull(book.description()));

        if (Objects.nonNull(book.tags())) {
            List<String> tags = new ArrayList<>();
            for (String tag : book.tags()) {
                String normalized = blankToNull(tag);
                if (Objects.nonNull(normalized)) {
                    tags.add(normalized);
                }
            }

            entity.getTags().addAll(tags);
        }

        for (SectionDto section : book.sections()) {
            entity.addSection(sectionMapper.toEntity(section));
        }
        return entity;
    }

    private <E extends Enum<E>> E optionalEnum(Class<E> type, String value, String field) {
        String normalized = upperCaseOrNull(value);
        if (Objects.isNull(normalized)) {
            return null;
        }
        try {
            return Enum.valueOf(type, normalized);
        } catch (IllegalArgumentException ex) {
            throw new BookFileParseException("Unknown " + field + " '" + value + "'");
        }
    }
}
