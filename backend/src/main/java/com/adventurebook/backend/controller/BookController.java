package com.adventurebook.backend.controller;

import com.adventurebook.backend.persistence.types.Difficulty;
import com.adventurebook.backend.persistence.types.Genre;
import com.adventurebook.backend.response.BookListDto;
import com.adventurebook.backend.response.MetadataDto;
import com.adventurebook.backend.service.BookService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {
    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public ResponseEntity<BookListDto> getBooks(
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(name = "difficulty", required = false) List<Difficulty> difficulties,
            @RequestParam(name = "genre", required = false) List<Genre> genres,
            @PageableDefault Pageable pageable
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(bookService.getBooks(query, difficulties, genres, pageable));
    }

    @GetMapping("/metadata")
    public ResponseEntity<MetadataDto> getMetadata() {
        return ResponseEntity.status(HttpStatus.OK).body(bookService.getMetadata());
    }
}
