package com.minilang.lexer;

public class LexerException extends RuntimeException {

    private final int line;

    public LexerException(String message, int line) {
        super("Error lexico en linea " + line + ": " + message);
        this.line = line;
    }

    public int getLine() {
        return line;
    }
}