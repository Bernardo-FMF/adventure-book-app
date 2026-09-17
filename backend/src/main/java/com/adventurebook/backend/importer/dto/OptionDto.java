package com.adventurebook.backend.importer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OptionDto(
        String description,
        String gotoId,
        ConsequenceDto consequence
) {
}
