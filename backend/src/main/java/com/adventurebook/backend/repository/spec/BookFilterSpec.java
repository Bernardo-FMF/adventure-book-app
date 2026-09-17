package com.adventurebook.backend.repository.spec;

import com.adventurebook.backend.persistence.BookEntity;
import com.adventurebook.backend.persistence.types.Difficulty;
import com.adventurebook.backend.persistence.types.Genre;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

// TODO-6: add comment to explain why the use of specs
public class BookFilterSpec implements Specification<BookEntity> {
    private final String query;
    private final List<Difficulty> difficulties;
    private final List<Genre> genres;

    public BookFilterSpec(String query, List<Difficulty> difficulties, List<Genre> genres) {
        this.query = query;
        this.difficulties = difficulties;
        this.genres = genres;
    }

    @Override
    public @Nullable Predicate toPredicate(@NonNull Root<BookEntity> root, @NonNull CriteriaQuery<?> query, @NonNull CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();
        if (Objects.nonNull(this.query) && !this.query.isBlank()) {
            predicates.add(
                    criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("title")),
                            "%" + this.query.trim().toLowerCase(Locale.ROOT) + "%"
                    )
            );
        }
        // An absent request parameter arrives as null, which means the filter was not asked for at all.
        if (Objects.nonNull(this.difficulties) && !this.difficulties.isEmpty()) {
            predicates.add(root.get("difficulty").in(this.difficulties));
        }
        if (Objects.nonNull(this.genres) && !this.genres.isEmpty()) {
            predicates.add(root.get("genre").in(this.genres));
        }

        return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
    }
}
