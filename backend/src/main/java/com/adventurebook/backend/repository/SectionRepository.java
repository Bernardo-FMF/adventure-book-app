package com.adventurebook.backend.repository;

import com.adventurebook.backend.persistence.SectionEntity;
import com.adventurebook.backend.persistence.types.SectionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SectionRepository extends JpaRepository<SectionEntity, Long> {
    Optional<SectionEntity> findByBookIdAndType(Long id, SectionType sectionType);
}
