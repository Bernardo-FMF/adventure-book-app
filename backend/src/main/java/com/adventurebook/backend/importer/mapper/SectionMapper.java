package com.adventurebook.backend.importer.mapper;

import com.adventurebook.backend.exception.BookFileParseException;
import com.adventurebook.backend.importer.dto.OptionDto;
import com.adventurebook.backend.importer.dto.SectionDto;
import com.adventurebook.backend.persistence.SectionEntity;
import com.adventurebook.backend.persistence.types.SectionType;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static com.adventurebook.backend.utils.StringUtils.blankToNull;
import static com.adventurebook.backend.utils.StringUtils.upperCaseOrNull;

@Component
public class SectionMapper {
    private final OptionMapper optionMapper;

    public SectionMapper(
            OptionMapper optionMapper
    ) {
        this.optionMapper = optionMapper;
    }

    public SectionEntity toEntity(SectionDto section) {
        String sectionRef = blankToNull(section.id());
        if (Objects.isNull(sectionRef)) {
            throw new BookFileParseException("Section without an id");
        }

        SectionEntity entity = new SectionEntity(sectionRef, typeOf(section.type(), sectionRef), section.text());

        if (Objects.nonNull(section.options())) {
            for (OptionDto option : section.options()) {
                if (Objects.nonNull(option)) {
                    entity.addOption(optionMapper.toEntity(option));
                }
            }
        }
        return entity;
    }

    private SectionType typeOf(String type, String sectionRef) {
        String normalized = upperCaseOrNull(type);
        if (Objects.isNull(normalized)) {
            throw new BookFileParseException("Section '" + sectionRef + "' has no type");
        }
        try {
            return SectionType.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new BookFileParseException("Section '" + sectionRef + "' has unknown type '" + type + "'");
        }
    }
}
