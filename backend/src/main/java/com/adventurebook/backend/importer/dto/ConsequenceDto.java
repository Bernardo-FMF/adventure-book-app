package com.adventurebook.backend.importer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ConsequenceDto(
        String type,
        Object amount,
        String text
) {
}
