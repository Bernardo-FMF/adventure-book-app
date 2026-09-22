package com.adventurebook.backend.service;

import com.adventurebook.backend.persistence.PlayerEntity;
import com.adventurebook.backend.repository.PlayerRepository;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public class PlayerService {
    private static final Logger log = LoggerFactory.getLogger(PlayerService.class);

    private static final int MAX_USERNAME_LENGTH = 80;

    private final PlayerRepository playerRepository;

    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @Transactional
    public PlayerEntity findOrCreate(@NotBlank @Size(max = MAX_USERNAME_LENGTH) String username) {
        return playerRepository.findByUsername(username)
                .orElseGet(() -> {
                    PlayerEntity created = playerRepository.save(new PlayerEntity(username));
                    log.info("New player {} registered", created.getId());
                    return created;
                });
    }
}
