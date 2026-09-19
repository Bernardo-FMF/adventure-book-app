package com.adventurebook.backend.controller;

import com.adventurebook.backend.request.StartGameRequestDto;
import com.adventurebook.backend.response.GameStateDto;
import com.adventurebook.backend.service.GameService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/games")
public class GameController {
    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping
    public ResponseEntity<GameStateDto> start(@RequestBody StartGameRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(gameService.start(request.bookId()));
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<GameStateDto> get(@PathVariable UUID gameId) {
        return ResponseEntity.ok(gameService.get(gameId));
    }
}
