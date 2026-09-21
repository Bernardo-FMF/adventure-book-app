package com.adventurebook.backend.service;

import com.adventurebook.backend.exception.MissingPlayerException;
import com.adventurebook.backend.persistence.PlayerEntity;
import com.adventurebook.backend.repository.PlayerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlayerService {
    private final PlayerRepository playerRepository;

    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @Transactional
    public PlayerEntity findOrCreate(String username) {
        if (username.isEmpty()) {
            throw new MissingPlayerException("A player name is required to play");
        }

        return playerRepository.findByUsername(username)
                .orElseGet(() -> playerRepository.save(new PlayerEntity(username)));
    }
}
