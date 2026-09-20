package com.adventurebook.backend.exception;

public class MissingPlayerException extends RuntimeException {
    public MissingPlayerException(String message) {
        super(message);
    }
}
