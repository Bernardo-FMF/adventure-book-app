package com.adventurebook.backend.service;

import com.adventurebook.backend.persistence.BookEntity;
import com.adventurebook.backend.persistence.types.Difficulty;
import com.adventurebook.backend.persistence.types.Genre;
import com.adventurebook.backend.repository.BookRepository;
import com.adventurebook.backend.repository.spec.BookFilterSpec;
import com.adventurebook.backend.response.BookDto;
import com.adventurebook.backend.response.BookListDto;
import com.adventurebook.backend.response.PaginationDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BookService {
    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    // TODO-5: add comment to explain why the use of readOnly
    @Transactional(readOnly = true)
    public BookListDto getBooks(String query, List<Difficulty> difficulties, List<Genre> genres, Pageable pageable) {
        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("title"));
        Page<BookEntity> page = bookRepository.findAll(new BookFilterSpec(query, difficulties, genres), pageRequest);

        return new BookListDto(
                page.getContent().stream().map(this::mapBook).toList(),
                new PaginationDto(page.getTotalPages(), page.getNumber(), page.getTotalElements())
        );
    }

    private BookDto mapBook(BookEntity entity) {
        return new BookDto(
                entity.getSlug(),
                entity.getTitle(),
                entity.getAuthor(),
                entity.getDifficulty(),
                entity.getGenre(),
                entity.getTags().stream().sorted().toList(),
                entity.getSectionsCount(),
                entity.getDescription()
        );
    }
}
