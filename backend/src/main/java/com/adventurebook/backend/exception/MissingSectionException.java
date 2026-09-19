package com.adventurebook.backend.exception;

public class MissingSectionException extends RuntimeException {
    public MissingSectionException(String message) {
        super(message);
    }
}
