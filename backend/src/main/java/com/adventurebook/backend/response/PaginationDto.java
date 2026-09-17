package com.adventurebook.backend.response;

public record PaginationDto(
        long totalPages,
        long currentPage,
        long totalElements
) {
}
