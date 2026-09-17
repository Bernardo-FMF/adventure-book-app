package com.adventurebook.backend.response;

import com.adventurebook.backend.persistence.types.Difficulty;
import com.adventurebook.backend.persistence.types.Genre;

import java.util.List;

public record BookDto(
        String slug,
        String title,
        String author,
        Difficulty difficulty,
        Genre genre,
        List<String> tags,
        int sectionsCount,
        String description
) {
}
