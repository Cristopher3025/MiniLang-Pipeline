package com.minilang.lexer;

public enum TokenType {

    // Reserved words
    DATA,
    FILTER,
    MAP,
    REDUCE,
    PRINT,

    // REDUCE operations
    SUM,
    MAX,
    MIN,

    // Comparison operators
    GREATER,
    LESS,
    GREATER_EQUAL,
    LESS_EQUAL,
    EQUAL,

    // Arithmetic operators
    PLUS,
    MINUS,
    MULTIPLY,

    // Values
    NUMBER,

    // Control tokens
    NEWLINE,
    EOF
}