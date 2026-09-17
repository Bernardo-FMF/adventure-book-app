package com.adventurebook.backend.repository;

import com.adventurebook.backend.persistence.BookEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<BookEntity, Long> {
    boolean existsBySlug(String slug);
}
