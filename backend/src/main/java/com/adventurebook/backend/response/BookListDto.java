package com.adventurebook.backend.response;

import java.util.List;

public record BookListDto(
        List<BookDto> books,
        PaginationDto pagination
) {
}
