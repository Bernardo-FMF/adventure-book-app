package com.adventurebook.backend.exception;

public class InvalidGameSessionException extends RuntimeException {
    public InvalidGameSessionException(String message) {
        super(message);
    }
}
