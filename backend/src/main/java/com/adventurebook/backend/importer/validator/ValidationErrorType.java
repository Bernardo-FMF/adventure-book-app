package com.adventurebook.backend.importer.validator;

public enum ValidationErrorType {
    UNPARSEABLE,
    NO_SECTIONS,
    NO_BEGIN,
    MULTIPLE_BEGIN,
    NO_END,
    UNRESOLVED_GOTO,
    DEAD_END_NODE
}
