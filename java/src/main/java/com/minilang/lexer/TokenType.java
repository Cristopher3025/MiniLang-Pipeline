package com.minilang.lexer;

public enum TokenType {

    // Palabras reservadas
    DATA,
    FILTER,
    MAP,
    REDUCE,
    PRINT,

    // Operaciones de REDUCE
    SUM,
    MAX,
    MIN,

    // Comparadores
    GREATER,          // >
    LESS,             // <
    GREATER_EQUAL,    // >=
    LESS_EQUAL,       // <=
    EQUAL,            // ==

    // Operadores aritméticos
    PLUS,             // +
    MINUS,            // -
    MULTIPLY,         // *

    // Valores
    NUMBER,

    // Control
    NEWLINE,
    EOF
}