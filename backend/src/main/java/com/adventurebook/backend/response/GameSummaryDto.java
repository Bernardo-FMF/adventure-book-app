package com.adventurebook.backend.response;

import java.util.UUID;

public record GameSummaryDto(
        UUID gameId,
        long bookId
) {
}
