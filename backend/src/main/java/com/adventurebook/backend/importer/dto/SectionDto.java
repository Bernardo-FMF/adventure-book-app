package com.adventurebook.backend.importer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SectionDto(
        String id,
        String text,
        String type,
        List<OptionDto> options
) {
}
