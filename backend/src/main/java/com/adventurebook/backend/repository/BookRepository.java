package com.adventurebook.backend.repository;

import com.adventurebook.backend.persistence.BookEntity;
import com.adventurebook.backend.persistence.types.Difficulty;
import com.adventurebook.backend.persistence.types.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BookRepository extends JpaRepository<BookEntity, Long>, JpaSpecificationExecutor<BookEntity> {
    boolean existsBySlug(String slug);

    @Query("select distinct book.genre from BookEntity book where book.genre is not null")
    List<Genre> getReferencedGenres();

    @Query("select distinct book.difficulty from BookEntity book where book.difficulty is not null")
    List<Difficulty> getReferencedDifficulties();
}
