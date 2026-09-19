package com.adventurebook.backend.exception;

public class InvalidChoiceException extends RuntimeException {
    public InvalidChoiceException(String message) {
        super(message);
    }
}
