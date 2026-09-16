package com.adventurebook.backend.importer.validator;

public record ValidationError(
        ValidationErrorType type,
        String message
) {
    public static ValidationError of(ValidationErrorType type, String message) {
        return new ValidationError(type, message);
    }
}
