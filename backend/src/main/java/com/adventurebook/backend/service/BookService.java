package com.adventurebook.backend.service;

import com.adventurebook.backend.persistence.BookEntity;
import com.adventurebook.backend.persistence.types.Difficulty;
import com.adventurebook.backend.persistence.types.Genre;
import com.adventurebook.backend.repository.BookRepository;
import com.adventurebook.backend.repository.spec.BookFilterSpec;
import com.adventurebook.backend.response.BookDto;
import com.adventurebook.backend.response.BookListDto;
import com.adventurebook.backend.response.MetadataDto;
import com.adventurebook.backend.response.PaginationDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;

@Service
public class BookService {
    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    /**
     * GetBooks is an optionally queryable and optionally paged method.
     * One caveat worth mentioning is that tags are lazy, so for each book we'd map, a query to fetch the tags would be
     * made. This is solved by performing tag fetches in batches ({@link BookEntity#getTags()}).
     * The batch size is configured as 10, which means for a default pageable, whose size is 10, we would be performing
     * the two initial queries to count the matches and read the page, and 1 more query for all the tags.
     * For comparison, without batch queries, those same 20 books would cost 20 queries just for the tags.
     * <p>
     * The transaction has to span the mapping due to the lazy loaded tags, which are read when the books are mapped, so we
     * need an open session.
     * It uses a readOnly transaction, because the operation itself is read only.
     * With this configuration:
     * 1. Hibernate will skip dirty checking and won't perform a flush. This means that Hibernate won't keep a snapshot
     * of the entity when loading it, and by consequence there's no need to perform a flush, since it compares the
     * snapshot with the updated entity to determine what changed;
     * 2. The JDBC connection is set to read-only mode, so the database will reject writes.
     * Overall, this configuration is useful for large reads, since it shaves off the time that Hibernate would spend
     * on comparisons, and also in terms of memory since we won't be loading a snapshot.
     *
     * @param query        optional value to filter results - only queries based on title.
     * @param difficulties optional value to filter results based on difficulty.
     * @param genres       optional value to filter results based on genre.
     * @param pageable     page number and size to read. Any sort requested by the caller is ignored, results are
     *                     always ordered by title so that paging stays stable.
     * @return a mapped DTO object with the filtered books and pagination details.
     */
    @Transactional(readOnly = true)
    public BookListDto getBooks(String query, List<Difficulty> difficulties, List<Genre> genres, Pageable pageable) {
        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("title"));
        Page<BookEntity> page = bookRepository.findAll(new BookFilterSpec(query, difficulties, genres), pageRequest);

        return new BookListDto(
                page.getContent().stream().map(this::mapBook).toList(),
                new PaginationDto(page.getTotalPages(), page.getNumber(), page.getTotalElements())
        );
    }

    @Transactional(readOnly = true)
    public MetadataDto getMetadata() {
        long count = bookRepository.count();
        List<Genre> genres = stabilizeOrder(bookRepository.getReferencedGenres(), Genre.class);
        List<Difficulty> difficulties = stabilizeOrder(bookRepository.getReferencedDifficulties(), Difficulty.class);
        return new MetadataDto(count, genres, difficulties);
    }

    private <T extends Enum<T>> List<T> stabilizeOrder(List<T> entries, Class<T> clazz) {
        EnumSet<T> ordered = EnumSet.noneOf(clazz);
        ordered.addAll(entries);
        return List.copyOf(ordered);
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
