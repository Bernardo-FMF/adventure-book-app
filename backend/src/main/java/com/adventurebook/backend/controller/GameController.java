package com.adventurebook.backend.controller;

import com.adventurebook.backend.persistence.PlayerEntity;
import com.adventurebook.backend.player.Player;
import com.adventurebook.backend.request.StartGameRequestDto;
import com.adventurebook.backend.response.GameStateDto;
import com.adventurebook.backend.response.GameSummaryDto;
import com.adventurebook.backend.service.GameService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/games")
public class GameController {
    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping
    public ResponseEntity<GameStateDto> start(@Player PlayerEntity player, @RequestBody StartGameRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(gameService.start(player, request.bookId()));
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<GameStateDto> get(@Player PlayerEntity player, @PathVariable UUID gameId) {
        return ResponseEntity.ok(gameService.get(player, gameId));
    }

    @PostMapping("/{gameId}/choices/{optionId}")
    public ResponseEntity<GameStateDto> makeChoice(@Player PlayerEntity player, @PathVariable UUID gameId, @PathVariable long optionId) {
        return ResponseEntity.ok(gameService.makeChoice(player, gameId, optionId));
    }

    @GetMapping
    public ResponseEntity<List<GameSummaryDto>> listActive(@Player PlayerEntity player) {
        return ResponseEntity.ok(gameService.listActive(player));
    }
}
