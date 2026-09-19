package com.adventurebook.backend.controller;

import com.adventurebook.backend.exception.BookNotFoundException;
import com.adventurebook.backend.exception.GameSessionNotFoundException;
import com.adventurebook.backend.exception.MissingSectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(BookNotFoundException.class)
    public ProblemDetail onBookNotFound(BookNotFoundException exception) {
        log.error("Book not found: {}", exception.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(GameSessionNotFoundException.class)
    public ProblemDetail onGameSessionNotFound(GameSessionNotFoundException exception) {
        log.error("Game session not found: {}", exception.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(MissingSectionException.class)
    public ProblemDetail onMissingSection(MissingSectionException exception) {
        log.error("Stored book is unplayable: {}", exception.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "This book cannot be played");
    }
}
