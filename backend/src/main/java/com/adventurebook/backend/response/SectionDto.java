package com.adventurebook.backend.response;

import com.adventurebook.backend.persistence.types.SectionType;

import java.util.List;

public record SectionDto(
        String sectionRef,
        String text,
        SectionType type,
        List<OptionDto> options
) {
}
