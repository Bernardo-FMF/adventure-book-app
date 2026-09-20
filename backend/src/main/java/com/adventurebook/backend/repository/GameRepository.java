package com.adventurebook.backend.repository;

import com.adventurebook.backend.persistence.GameSessionEntity;
import com.adventurebook.backend.persistence.types.GameStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GameRepository extends JpaRepository<GameSessionEntity, UUID> {
    List<GameSessionEntity> findByPlayerIdAndStatus(long playerId, GameStatus status);

    boolean existsByPlayerIdAndBookIdAndStatus(long playerId, long bookId, GameStatus status);
}
