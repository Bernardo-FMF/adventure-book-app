package com.adventurebook.backend.repository;

import com.adventurebook.backend.persistence.GameSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GameRepository extends JpaRepository<GameSessionEntity, UUID> {
}
