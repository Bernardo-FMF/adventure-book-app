package com.adventurebook.backend.response;

import com.adventurebook.backend.persistence.types.ConsequenceType;

public record ConsequenceDto(
        ConsequenceType type,
        int amount,
        String text
) {
}
