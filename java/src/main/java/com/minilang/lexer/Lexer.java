package com.minilang.lexer;

import java.util.ArrayList;
import java.util.List;

public class Lexer {

    private final String source;
    private final List<Token> tokens;

    private int current;
    private int line;

    public Lexer(String source) {
        this.source = source;
        this.tokens = new ArrayList<>();
        this.current = 0;
        this.line = 1;
    }

    public List<Token> tokenize() {

        while (!isAtEnd()) {

            char c = advance();

            switch (c) {

                case ' ':
                case '\t':
                case '\r':
                    break;

                case '\n':
                    tokens.add(new Token(
                            TokenType.NEWLINE,
                            "\\n",
                            line
                    ));
                    line++;
                    break;

                case '+':
                    addToken(TokenType.PLUS, "+");
                    break;

                case '-':
                    addToken(TokenType.MINUS, "-");
                    break;

                case '*':
                    addToken(TokenType.MULTIPLY, "*");
                    break;

                case '>':
                    if (match('=')) {
                        addToken(TokenType.GREATER_EQUAL, ">=");
                    } else {
                        addToken(TokenType.GREATER, ">");
                    }
                    break;

                case '<':
                    if (match('=')) {
                        addToken(TokenType.LESS_EQUAL, "<=");
                    } else {
                        addToken(TokenType.LESS, "<");
                    }
                    break;

                case '=':
                    if (match('=')) {
                        addToken(TokenType.EQUAL, "==");
                    } else {
                        throw new LexerException(
                                "se esperaba '=' despues de '='",
                                line
                        );
                    }
                    break;

                default:

                    if (Character.isDigit(c)) {
                        readNumber(c);

                    } else if (Character.isLetter(c)) {
                        readWord(c);

                    } else {
                        throw new LexerException(
                                "simbolo no reconocido '" + c + "'",
                                line
                        );
                    }

                    break;
            }
        }

        tokens.add(new Token(
                TokenType.EOF,
                "",
                line
        ));

        return tokens;
    }

    private void readNumber(char firstCharacter) {

        StringBuilder number = new StringBuilder();

        number.append(firstCharacter);

        while (!isAtEnd()
                && Character.isDigit(peek())) {

            number.append(advance());
        }

        tokens.add(new Token(
                TokenType.NUMBER,
                number.toString(),
                line
        ));
    }

    private void readWord(char firstCharacter) {

        StringBuilder word = new StringBuilder();

        word.append(firstCharacter);

        while (!isAtEnd()
                && Character.isLetter(peek())) {

            word.append(advance());
        }

        String lexeme = word.toString();

        TokenType type = getKeywordType(lexeme);

        if (type == null) {
            throw new LexerException(
                    "palabra no reconocida '" + lexeme + "'",
                    line
            );
        }

        tokens.add(new Token(
                type,
                lexeme,
                line
        ));
    }

    private TokenType getKeywordType(String word) {

        return switch (word) {

            case "DATA" -> TokenType.DATA;
            case "FILTER" -> TokenType.FILTER;
            case "MAP" -> TokenType.MAP;
            case "REDUCE" -> TokenType.REDUCE;
            case "PRINT" -> TokenType.PRINT;

            case "SUM" -> TokenType.SUM;
            case "MAX" -> TokenType.MAX;
            case "MIN" -> TokenType.MIN;

            default -> null;
        };
    }

    private void addToken(
            TokenType type,
            String lexeme) {

        tokens.add(new Token(
                type,
                lexeme,
                line
        ));
    }

    private char advance() {
        return source.charAt(current++);
    }

    private char peek() {

        if (isAtEnd()) {
            return '\0';
        }

        return source.charAt(current);
    }

    private boolean match(char expected) {

        if (isAtEnd()) {
            return false;
        }

        if (source.charAt(current) != expected) {
            return false;
        }

        current++;
        return true;
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }
}