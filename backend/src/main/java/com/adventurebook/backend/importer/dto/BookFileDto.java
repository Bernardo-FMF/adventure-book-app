package com.adventurebook.backend.importer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BookFileDto(
        String title,
        String author,
        String difficulty,
        String genre,
        String description,
        List<String> tags,
        List<SectionDto> sections
) {
}
