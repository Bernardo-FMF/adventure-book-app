package com.adventurebook.backend.importer.mapper;

import com.adventurebook.backend.exception.BookFileParseException;
import com.adventurebook.backend.importer.dto.ConsequenceDto;
import com.adventurebook.backend.persistence.ConsequenceEntity;
import com.adventurebook.backend.persistence.types.ConsequenceType;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static com.adventurebook.backend.utils.StringUtils.upperCaseOrNull;

@Component
public class ConsequenceMapper {
    public ConsequenceEntity toEntity(ConsequenceDto consequence) {
        ConsequenceType type = typeOf(consequence.type());

        if (Objects.isNull(consequence.value())) {
            throw new BookFileParseException("Consequence without a value");
        }
        return new ConsequenceEntity(type, consequence.value(), consequence.text());
    }

    private ConsequenceType typeOf(String type) {
        String normalized = upperCaseOrNull(type);
        if (Objects.isNull(normalized)) {
            throw new BookFileParseException("Consequence without a type");
        }
        try {
            return ConsequenceType.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new BookFileParseException("Unknown consequence type '" + type + "'");
        }
    }
}
