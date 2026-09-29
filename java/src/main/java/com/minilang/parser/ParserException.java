package com.minilang.parser;

public class ParserException extends RuntimeException {

    private final int line;

    public ParserException(String message, int line) {
        super("Error sintactico en linea " + line + ": " + message);
        this.line = line;
    }

    public int getLine() {
        return line;
    }
}