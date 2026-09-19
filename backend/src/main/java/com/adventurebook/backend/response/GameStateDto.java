package com.adventurebook.backend.response;

import com.adventurebook.backend.persistence.types.GameStatus;

import java.util.UUID;

public record GameStateDto(
        UUID id,
        BookSummaryDto bookSummary,
        GameStatus status,
        int health,
        SectionDto section,
        ConsequenceDto consequence
) {
}
