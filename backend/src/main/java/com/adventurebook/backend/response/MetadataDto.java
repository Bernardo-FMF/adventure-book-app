package com.adventurebook.backend.response;

import com.adventurebook.backend.persistence.types.Difficulty;
import com.adventurebook.backend.persistence.types.Genre;

import java.util.List;

public record MetadataDto(
        long bookCount,
        List<Genre> genres,
        List<Difficulty> difficulties
) {
}
