package com.adventurebook.backend.controller;

import com.adventurebook.backend.exception.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(MissingPlayerException.class)
    public ProblemDetail onMissingPlayer(MissingPlayerException exception) {
        log.warn("Request without a usable player name: {}", exception.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, exception.getMessage());
    }

    @ExceptionHandler(BookNotFoundException.class)
    public ProblemDetail onBookNotFound(BookNotFoundException exception) {
        log.warn("Book not found: {}", exception.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(GameSessionNotFoundException.class)
    public ProblemDetail onGameSessionNotFound(GameSessionNotFoundException exception) {
        log.warn("Game session not found: {}", exception.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(InvalidChoiceException.class)
    public ProblemDetail onInvalidChoice(InvalidChoiceException exception) {
        log.warn("Chosen option is invalid: {}", exception.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(InvalidGameSessionException.class)
    public ProblemDetail onInvalidGameSession(InvalidGameSessionException exception) {
        log.warn("Game session cannot be started: {}", exception.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(MissingSectionException.class)
    public ProblemDetail onMissingSection(MissingSectionException exception) {
        log.warn("Stored book is unplayable: {}", exception.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "This book cannot be played");
    }
}
