package com.adventurebook.backend.importer.validator;

import com.adventurebook.backend.exception.BookFileParseException;
import com.adventurebook.backend.importer.dto.BookFileDto;
import com.adventurebook.backend.importer.dto.OptionDto;
import com.adventurebook.backend.importer.dto.SectionDto;
import org.springframework.stereotype.Component;

import static com.adventurebook.backend.utils.StringUtils.blankToNull;
import static com.adventurebook.backend.utils.StringUtils.upperCaseOrNull;

import java.util.*;

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

        return validate(book);
    }

    /**
     * A book is invalid when any of the following holds:
     * 1. it has no beginning section, or more than one.
     * 2. it has no ending section.
     * 3. an option points to a section id that does not exist.
     * 4. a non-ending section offers no options.
     * All rules are evaluated even when one error is found, which is useful when adding new books to provide
     * feedback regarding what is wrong.
     */
    private ParseResult validate(BookFileDto book) {
        List<SectionDto> sections = book.sections();
        if (Objects.isNull(sections) || sections.isEmpty()) {
            return ParseResult.of(List.of(
                    ValidationError.of(ValidationErrorType.NO_SECTIONS, "Book has no sections")
            ));
        }

        List<ValidationError> errors = new ArrayList<>();

        Map<String, SectionDto> refs = LinkedHashMap.newLinkedHashMap(sections.size());
        String beginId = null;
        boolean bookHasEnd = false;

        for (SectionDto section : sections) {
            // Ids in the seed data are either strings or numeric values. Numeric values are already turned into strings by Jackson.
            String id = blankToNull(section.id());
            if (Objects.isNull(id)) {
                continue;
            }

            String type = upperCaseOrNull(section.type());
            boolean ending = isEnding(type);

            // In the case of duplicate section ids, store only its first appearance, and record an error.
            if (Objects.nonNull(refs.putIfAbsent(id, section))) {
                errors.add(ValidationError.of(ValidationErrorType.DUPLICATE_ID, "Book has more than one section with id '" + id + "'"));
            }

            // Rule 1: if the amount of BEGIN sections is different from 1, we have an invalid book.
            if (isBegin(type)) {
                if (Objects.isNull(beginId)) {
                    beginId = id;
                } else {
                    errors.add(ValidationError.of(ValidationErrorType.MULTIPLE_BEGIN, "Book has multiple begin sections"));
                }
            }

            // Rule 2: the existence of a single END section means a book can be valid.
            if (ending) {
                bookHasEnd = true;
            }
        }

        // Rule 1: No BEGIN section means the book is invalid.
        if (Objects.isNull(beginId)) {
            errors.add(ValidationError.of(ValidationErrorType.NO_BEGIN, "Book has no begin section"));
        }

        // Rule 2: the book has no END section, meaning the book is invalid.
        if (!bookHasEnd) {
            errors.add(ValidationError.of(ValidationErrorType.NO_END, "Book has no end section"));
        }

        for (SectionDto section : refs.values()) {
            List<String> targets = targetsOf(section);

            // Rule 4: a non-ending section with no options is invalid.
            // This rule is enforced strictly, ignoring reachability. this means that even if this section isn't reachable from
            // other sections, it's still invalid.
            if (!isEnding(upperCaseOrNull(section.type())) && targets.isEmpty()) {
                errors.add(ValidationError.of(ValidationErrorType.DEAD_END_NODE, "Non-ending section '" + section.id() + "' has no options"));
            }

            // Rule 3: a target option is only valid the book declares a section with that id.
            // A null target option is a specific validation, since it's a different error than a missing section.
            for (String target : targets) {
                if (Objects.isNull(target)) {
                    errors.add(ValidationError.of(ValidationErrorType.UNRESOLVED_GOTO, "Section '" + section.id() + "' has an option with no target section"));
                } else if (!refs.containsKey(target)) {
                    errors.add(ValidationError.of(ValidationErrorType.UNRESOLVED_GOTO, "Section '" + section.id() + "' points to a missing section '" + target + "'"));
                }
            }
        }

        return errors.isEmpty() ? ParseResult.of(book) : ParseResult.of(errors);
    }

    // Absent and empty option lists collapse to the same empty result, so rule 4 has a single case to test.
    private List<String> targetsOf(SectionDto section) {
        List<OptionDto> options = section.options();
        if (Objects.isNull(options) || options.isEmpty()) {
            return List.of();
        }

        List<String> targets = new ArrayList<>(options.size());
        for (OptionDto option : options) {
            if (Objects.nonNull(option)) {
                targets.add(blankToNull(option.gotoId()));
            }
        }
        return targets;
    }

    private boolean isEnding(String type) {
        return "END".equals(type);
    }

    private boolean isBegin(String type) {
        return "BEGIN".equals(type);
    }
}
