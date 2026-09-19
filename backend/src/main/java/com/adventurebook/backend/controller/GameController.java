package com.adventurebook.backend.controller;

import com.adventurebook.backend.request.StartGameRequestDto;
import com.adventurebook.backend.response.GameStateDto;
import com.adventurebook.backend.service.GameService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
